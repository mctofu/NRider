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

import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.*;
import nrider.ride.IRide;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 9:15:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class WorkoutSession implements IPerformanceDataListener, IPerformanceDataSource, IControlDataListener
{
	private final static Logger LOG = Logger.getLogger( WorkoutSession.class );

	private static WorkoutSession _instance = new WorkoutSession();

	public static WorkoutSession instance()
	{
		return _instance;
	}

	private List<Rider> _riders = new ArrayList<Rider>();
	private List<IWorkoutController> _controllers = new ArrayList<IWorkoutController>();
	private Map<String, RiderSession> _deviceMap = new HashMap<String, RiderSession>();
	private Map<String, RiderSession> _riderMap = new HashMap<String, RiderSession>();
	private EventPublisher<IPerformanceDataListener> _performancePublisher = EventPublisher.singleThreadPublisher( WorkoutSession.class.getName() );
	private EventPublisher<IWorkoutListener> _workoutPublisher = EventPublisher.singleThreadPublisher( WorkoutSession.class.getName() );
	private IRide _ride;

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

	public void addRider( final Rider rider )
	{
		_riders.add( rider );
		RiderSession session = new RiderSession( rider );
		_riderMap.put( rider.getIdentifier(), session );
		_workoutPublisher.publishEvent(
			new IEvent<IWorkoutListener>() {
				public void trigger( IWorkoutListener target )
				{
					target.handleAddRider( rider );
				}
			});
	}

	public List<Rider> getRiders()
	{
		return Collections.unmodifiableList( _riders );
	}

	public Rider getRider( String identifier )
	{
		for( Rider r : _riders )
		{
			if( r.getIdentifier().equals( identifier ) )
			{
				return r;
			}
		}
		return null;
	}

	public void addWorkoutController( IWorkoutController controller )
	{
		_controllers.add( controller );
	}

	public void addPerformanceDataSource( IPerformanceDataSource source )
	{
		source.addPerformanceDataListener( this );
	}

	public List<IWorkoutController> getControllers()
	{
		return Collections.unmodifiableList( _controllers );
	}

	public void associateRider( String riderId, String identifier )
	{
		RiderSession session = _riderMap.get( riderId );
		session.addAssociation( identifier );
		_deviceMap.put( identifier, session );
	}

	public void setRideLoad( final RideLoad load )
	{
		setRideLoad( null, load );
	}

	public void setRideLoad( String riderId, final RideLoad load )
	{
		// TODO: Support gradient
		for( IWorkoutController controller : _controllers )
		{
			final RiderSession rider = _deviceMap.get( controller.getIdentifier() );

			if( rider == null )
			{
				continue;
			}

			if( riderId == null || ( rider.getRider().getIdentifier().equals( riderId ) ) )
			{
				// move this into RideLoad implementation?
				switch( load.getType() )
				{
					case PERCENT_THRESHOLD:
						controller.setLoad( rider.getRider().getLoadForWorkout( load.getValue() ) );
						break;
					case WATTS:
						controller.setLoad( load.getValue() );
						break;
				}
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

	public void startRide()
	{
		if( _ride != null )
		{
			synchronized( _ride )
			{
				if( ( _ride.getStatus() == IRide.Status.READY || _ride.getStatus() == IRide.Status.PAUSED ) )
				{
					_ride.start();
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
				}
			}
		}
	}

	public String getIdentifier()
	{
		return "NRider WorkoutSession";
	}

	public void addPerformanceDataListener( IPerformanceDataListener listener )
	{
		_performancePublisher.addListener( listener );
	}

	public void handlePerformanceData( String identifier, final PerformanceData data )
	{
		// TODO: Need to examine all event handling and evaluate need for a separate thread so a slow handler won't hang up everything.
		if( _deviceMap.containsKey( identifier ) )
		{
			final RiderSession rs = _deviceMap.get( identifier );
			_performancePublisher.publishEvent(
				new IEvent<IPerformanceDataListener>() {
					public void trigger( IPerformanceDataListener target )
					{
						target.handlePerformanceData( rs.getRider().getIdentifier(), data );
					}
				});
		}
	}

	public void handleControlData( String identifier, ControlData data )
	{
		try
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
		catch( Throwable e )
		{
			LOG.error( "Control data handling error", e );
		}
	}

	public void addWorkoutListener( IWorkoutListener listener )
	{
		_workoutPublisher.addListener( listener );
	}

	public void close() throws IOException
	{
		for( IWorkoutController controller : _controllers )
		{
			controller.close();
		}
	}

	public void setRiderThreshold( final String identifier, final int thresholdPower )
	{
		Rider rider = getRider( identifier );
		rider.setThresholdPower( thresholdPower );
		// TODO: if in a workout should adjust load on their trainer
		_workoutPublisher.publishEvent(
			new IEvent<IWorkoutListener>() {
				public void trigger( IWorkoutListener target )
				{
					target.handleRiderThresholdAdjust( identifier, thresholdPower );
				}
			});
	}
}
