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
package nrider.datalog;

import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import nrider.ride.IRide;

import java.util.HashMap;

/**
 *
 */
public class RideLogger implements IPerformanceDataListener, IWorkoutListener
{
	private HashMap<String,BaseLogger> _loggers = new HashMap();
	private volatile boolean _logging = false;

	public void handlePerformanceData( String identifier, PerformanceData data )
	{
		BaseLogger logger;


		synchronized( _loggers )
		{
			logger = _loggers.get( identifier );
			if( logger == null )
			{
				logger = new TxtLogger( identifier );
				_loggers.put( identifier, logger );
			}
		}

		synchronized( logger )
		{
			if( _logging )
			{
				logger.logData( data );
			}
		}
	}

	public void handleLoadAdjust( String riderId, RideLoad newLoad )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleAddRider( Rider rider )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleRiderThresholdAdjust( String riderId, double newThreshold )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleRideLoaded( IRide ride )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleRideTimeUpdate( long rideTime )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleAddRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleRemoveRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleRideStatusUpdate( IRide.Status status )
	{
		switch( status )
		{
			case RUNNING:
				System.out.println( "logging" );
				_logging = true;
				break;
			case PAUSED:
				_logging = false;
				break;
			case STOPPED:
				_logging = false;
				synchronized( _loggers )
				{
					for( BaseLogger tcxLogger : _loggers.values() )
					{
						tcxLogger.close();
					}
				}
				break;
		}
	}
}
