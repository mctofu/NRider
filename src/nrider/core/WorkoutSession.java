/*
 * Copyright (c) 2009 David McIntosh (david.mcintosh@yahoo.com)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package nrider.core;

import gnu.io.PortInUseException;
import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.*;
import nrider.media.IMediaEventListener;
import nrider.media.MediaEvent;
import nrider.net.NetSource;
import nrider.ride.IRide;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Central control for a workout.  Manages riders and trainers in a workout.
 * TODO: review/fix thread safety after sorting out how everything should work
 */
public class WorkoutSession implements IPerformanceDataListener, IPerformanceDataSource, IControlDataListener, IMediaEventListener
{
	private final static Logger LOG = Logger.getLogger( WorkoutSession.class );

	private static WorkoutSession _instance = new WorkoutSession();

	public static WorkoutSession instance()
	{
		return _instance;
	}

	public enum RiderAlertType {
		SPEED_HIGH( "SpdHi" ), SPEED_LOW( "SpdLo" ), POWER_ASSIST( "PwrBst" );

		private String _shortName;

		RiderAlertType( String shortName )
		{
			_shortName = shortName;
		}

		public String getShortName()
		{
			return _shortName;
		}
	}

	private List<Rider> _riders = new ArrayList<Rider>();
	private List<IWorkoutController> _controllers = new ArrayList<IWorkoutController>();
	private Map<String, RiderSession> _deviceMap = new HashMap<String, RiderSession>();
	private Map<String, RiderSession> _riderMap = new HashMap<String, RiderSession>();
	private EventPublisher<IPerformanceDataListener> _netPerformancePublisher = EventPublisher.singleThreadPublisher( WorkoutSession.class.getName() );
	private EventPublisher<IPerformanceDataListener> _localPerformancePublisher = EventPublisher.singleThreadPublisher( WorkoutSession.class.getName() );

	private EventPublisher<IMediaEventListener> _mediaEventPublisher = EventPublisher.singleThreadPublisher( WorkoutSession.class.getName() );

	private EventPublisher<IWorkoutListener> _workoutPublisher = EventPublisher.singleThreadPublisher( WorkoutSession.class.getName() );
	private IRide _ride;
	private RiderPerformanceMonitor _riderPerformanceMonitor = new RiderPerformanceMonitor();
	private NetSource _netSource;

	public WorkoutSession()
	{
		addLocalPerformanceDataListener( _riderPerformanceMonitor );
	}

	public void setRideElapsedTime( final long elapsed )
	{
		_workoutPublisher.publishEvent(
			new IEvent<IWorkoutListener>() {
				public void trigger( IWorkoutListener target )
				{
					target.handleRideTimeUpdate( elapsed );
				}
			});
	}

	public void setRide( final IRide ride )
	{
		_ride = ride;
		_workoutPublisher.publishEvent(
			new IEvent<IWorkoutListener>() {
				public void trigger( IWorkoutListener target )
				{
					target.handleRideLoaded( ride );
				}
			});

	}

	public void addLocalRider( final Rider rider )
	{
		addRider( rider, "local" );
	}

	public void addNetRider( final Rider rider, String source )
	{
		addRider( rider, source );
	}

	private void addRider( final Rider rider, String source )
	{
        synchronized( _riders )
        {
            _riders.add( rider );
            RiderSession session = new RiderSession( rider, source );
            _riderMap.put( rider.getIdentifier(), session );
            _workoutPublisher.publishEvent(
                new IEvent<IWorkoutListener>() {
                    public void trigger( IWorkoutListener target )
                    {
                        target.handleAddRider( rider );
                    }
                });
        }
	}

	public void addRiderAlert( final String identifier, final RiderAlertType alert )
	{
		_workoutPublisher.publishEvent(
			new IEvent<IWorkoutListener>() {
				public void trigger( IWorkoutListener target )
				{
					target.handleAddRiderAlert( identifier, alert );
				}
			});
	}

	public void removeRiderAlert( final String identifier, final RiderAlertType alert )
	{
		_workoutPublisher.publishEvent(
			new IEvent<IWorkoutListener>() {
				public void trigger( IWorkoutListener target )
				{
					target.handleRemoveRiderAlert( identifier, alert );
				}
			});
	}


	public List<Rider> getRiders()
	{
        synchronized( _riders )
        {
            return Collections.unmodifiableList( _riders );
        }
	}

	public Rider getRider( String identifier )
	{
        synchronized( _riders )
        {
            for( Rider r : _riders )
            {
                if( r.getIdentifier().equals( identifier ) )
                {
                    return r;
                }
            }
        }
		return null;
	}

	public void disconnectControllers()
	{
        synchronized( _controllers )
        {
            for( IWorkoutController controller : _controllers )
            {
                try
                {
                    controller.disconnect();
                }
                catch( IOException e )
                {
                    LOG.error( e );
                }
            }
        }
	}

	public void connectControllers()
	{
        synchronized( _controllers )
        {
            for( IWorkoutController controller : _controllers )
            {
                try
                {
                    controller.connect();
                }
                catch( PortInUseException e )
                {
                    LOG.error( e );
                }
            }
        }
	}

	public void addWorkoutController( IWorkoutController controller )
	{
        synchronized( _controllers )
        {
            _controllers.add( controller );
        }
	}

	public void addPerformanceDataSource( IPerformanceDataSource source )
	{
		source.addPerformanceDataListener( this );
	}

	public List<IWorkoutController> getControllers()
	{
        synchronized( _controllers )
        {
            return Collections.unmodifiableList( _controllers );
        }
	}

	public void associateRider( String riderId, String identifier )
	{
        synchronized( _riders )
        {
            RiderSession session = _riderMap.get( riderId );
            session.addAssociation( identifier );
            _deviceMap.put( identifier, session );
        }
	}

	public void setRideLoad( final RideLoad load )
	{
		setRideLoad( null, load );
	}

	public void setRideLoad( String riderId, final RideLoad load )
	{
		// TODO: Support gradient
        synchronized( _controllers )
        {
            synchronized( _riders )
            {
                for( IWorkoutController controller : _controllers )
                {
                    final RiderSession rider = _deviceMap.get( controller.getIdentifier() );

                    if( rider == null )
                    {
                        continue;
                    }

                    if( riderId == null || ( rider.getRider().getIdentifier().equals( riderId ) ) )
                    {
                        rider.setCurrentLoad( load );
                        controller.setLoad( rider.getLoadForWorkout() );
                        _workoutPublisher.publishEvent(
                            new IEvent<IWorkoutListener>() {
                                public void trigger( IWorkoutListener target )
                                {
                                    target.handleLoadAdjust( rider.getRider().getIdentifier(), load );
                                }
                            });
                    }
                }
            }
        }
	}

	public double getTargetWatts( String riderId )
	{
        synchronized( _riders )
        {
            return _riderMap.get( riderId ).getLoadForWorkoutWithoutHandicap();
        }
	}

	public void startRide()
	{
		if( _ride != null )
		{
			synchronized( _ride )
			{
				if( ( _ride.getStatus() == IRide.Status.READY || _ride.getStatus() == IRide.Status.PAUSED ) )
				{
					_ride.start();
					_riderPerformanceMonitor.activate();
					_workoutPublisher.publishEvent(
						new IEvent<IWorkoutListener>() {
							public void trigger( IWorkoutListener target )
							{
								target.handleRideStatusUpdate( _ride.getStatus() );
							}
						});

				}
			}
		}
	}

	public void pauseRide()
	{
		if( _ride != null )
		{
			synchronized( _ride )
			{
				if( _ride != null & _ride.getStatus() == IRide.Status.RUNNING )
				{
					_ride.pause();
					_riderPerformanceMonitor.deactivate();
					_workoutPublisher.publishEvent(
						new IEvent<IWorkoutListener>() {
							public void trigger( IWorkoutListener target )
							{
								target.handleRideStatusUpdate( _ride.getStatus() );
							}
						});
				}
			}
		}
	}

	public String getIdentifier()
	{
		return "NRider WorkoutSession";
	}

	/**
	 * subscribe to performance data from local riders
	 * @param listener
	 */
	public void addLocalPerformanceDataListener( IPerformanceDataListener listener )
	{
		_localPerformancePublisher.addListener( listener );
	}

	/**
	 * subscribe to performance data from local and net riders
	 * @param listener
	 */
	public void addPerformanceDataListener( IPerformanceDataListener listener )
	{
		_netPerformancePublisher.addListener( listener );
		_localPerformancePublisher.addListener( listener );
	}

	/**
	 * handle raw perf data from a controller and publish rider perf data
	 * @param identifier
	 * @param data
	 */
	public void handlePerformanceData( String identifier, final PerformanceData data )
	{
        synchronized( _riders )
        {
            if( _deviceMap.containsKey( identifier ) )
            {
                final RiderSession rs = _deviceMap.get( identifier );
                getPublisher( rs ).publishEvent(
                    new IEvent<IPerformanceDataListener>() {
                        public void trigger( IPerformanceDataListener target )
                        {
                            target.handlePerformanceData( rs.getRider().getIdentifier(), data );
                        }
                    });
            }
        }
	}

	private EventPublisher<IPerformanceDataListener> getPublisher( RiderSession session )
	{
		if( session.getSource().equals( "local" ) )
		{
			return _localPerformancePublisher;
		}
		return _netPerformancePublisher;
	}

	public void handleControlData( String identifier, ControlData data )
	{
		try
		{
            synchronized( _riders )
            {
                if( _deviceMap.containsKey( identifier ) )
                {
                    RiderSession rs = _deviceMap.get( identifier );
                    switch( data.getType() )
                    {
                        case PLUS:
                            // TODO: more sense to just set on rider which triggers an event for the workout to adjust the load?
                            setRiderThreshold( rs.getRider().getIdentifier(), rs.getRider().getThresholdPower() + 5 );
                            break;
                        case MINUS:
                            // TODO: more sense to just set on rider which triggers an event for the workout to adjust the load?
                            setRiderThreshold( rs.getRider().getIdentifier(), rs.getRider().getThresholdPower() - 5 );
                            break;
                        case START:
                            startRide();
                            break;
                        case STOP:
                            pauseRide();
                            break;
                    }
                }
            }
		}
		catch( Throwable e )
		{
			LOG.error( "Control data handling error", e );
		}
	}

	public void addWorkoutListener( IWorkoutListener listener )
	{
		_workoutPublisher.addListener( listener );
	}

	public void addMediaEventListner( IMediaEventListener listener )
	{
		_mediaEventPublisher.addListener( listener );
	}

	public void handleMediaEvent( final MediaEvent me )
	{
		_mediaEventPublisher.publishEvent(
			new IEvent<IMediaEventListener>() {
				public void trigger( IMediaEventListener target )
				{
					target.handleMediaEvent( me );
				}
			});
	}

	public void close() throws IOException
	{
        synchronized( _controllers )
        {
            for( IWorkoutController controller : _controllers )
            {
                controller.close();
            }
        }
	}

	public void setRiderThreshold( final String identifier, final int thresholdPower )
	{
        synchronized( _riders )
        {
            Rider rider = getRider( identifier );
            rider.setThresholdPower( thresholdPower );
            reapplyRiderLoad( identifier );
            _workoutPublisher.publishEvent(
                new IEvent<IWorkoutListener>() {
                    public void trigger( IWorkoutListener target )
                    {
                        target.handleRiderThresholdAdjust( identifier, thresholdPower );
                    }
                });
        }
	}

	public void setRiderHandicap( String identifier, final int handicap )
	{
        synchronized( _riders )
        {
            RiderSession rider = _riderMap.get( identifier );
            rider.setHandicap( handicap );
            reapplyRiderLoad( identifier );
        }
	}

	private void reapplyRiderLoad( String riderId )
	{
        synchronized( _controllers )
        {
            synchronized( _riders )
            {
                for( IWorkoutController controller : _controllers )
                {
                    RiderSession rider = _deviceMap.get( controller.getIdentifier() );
                    if( rider != null && rider.getRider().getIdentifier().equals( riderId ) )
                    {
                        controller.setLoad( rider.getLoadForWorkout() );
                        break;
                    }
                }
            }            
        }
	}

	public void setNetSource( NetSource netSource )
	{
		_netSource = netSource;
		addPerformanceDataListener( netSource );
		addPerformanceDataSource( netSource );
		addWorkoutListener( netSource );
	}

}
