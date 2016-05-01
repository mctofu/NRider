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
package nrider.ride;

import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class DistanceBasedRide implements IRide, IPerformanceDataListener
{
	private HashMap<String, RideSession> _rideSessionMap = new HashMap<String, RideSession>();
	private RideScript _script;
	private volatile Status _status;

	public DistanceBasedRide( RideScript script )
	{
		_script = script;
		for( Rider r : WorkoutSession.instance().getRiders() )
		{
			_rideSessionMap.put( r.getIdentifier(), new RideSession( r.getIdentifier() ) );
		}
		_status = Status.READY;
		resetLoad();
	}

	public void start()
	{
		synchronized( _status )
		{
			if( _status != Status.READY || _status != Status.PAUSED )
			{
				return;
			}
			_status = Status.RUNNING;
			for( RideSession rs : _rideSessionMap.values() )
			{
				updateLoad( rs );
			}
		}
	}

	public void pause()
	{
		_status = Status.PAUSED;
		resetLoad();
	}

	public void stop()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public Status getStatus()
	{
		synchronized( _status )
		{
			return _status;
		}
	}

	private void resetLoad()
	{
		RideLoad resetLoad = new RideLoad( RideLoad.Type.GRADIENT, 0 );

		for( RideSession rs : _rideSessionMap.values() )
		{
			setRiderLoad( rs, resetLoad );
		}
	}

	private void updateLoad( RideSession rs )
	{
		RideLoad load = _script.getLoad( (long) rs.getDistanceTotal() );
		if( load.getValue() != rs.getCurrentLoad().getValue() )
		{
			setRiderLoad( rs, load );
		}
	}

	private void setRiderLoad( RideSession rs, RideLoad load )
	{
		WorkoutSession.instance().setRideLoad( rs.getRiderId(), load );
		rs.setCurrentLoad( load );
	}


	public void handlePerformanceData( String identifier, PerformanceData data )
	{
		if( data.getType() == PerformanceData.Type.DISTANCE )
		{
			synchronized( _status )
			{
				if( _status != Status.RUNNING )
				{
					return;
				}
			}
			RideSession rideSession = _rideSessionMap.get( identifier );
			rideSession.addDistance( data.getValue() );
			// check if we need to update the workout
		}
	}

	public RideScript getScript()
	{
		return _script;
	}

	class RideSession
	{
		private String _riderId;
		private RideLoad _currentLoad;
		private float _distanceTotal;

		RideSession( String riderId )
		{
			_riderId = riderId;
			_currentLoad = new RideLoad( RideLoad.Type.GRADIENT, 0 );
		}

		public String getRiderId()
		{
			return _riderId;
		}

		public void addDistance( float additionalDistance )
		{
			_distanceTotal += additionalDistance;
		}

		public float getDistanceTotal()
		{
			return _distanceTotal;
		}

		public void setDistanceTotal( float distanceTotal )
		{
			_distanceTotal = distanceTotal;
		}

		public RideLoad getCurrentLoad()
		{
			return _currentLoad;
		}

		public void setCurrentLoad( RideLoad currentLoad )
		{
			_currentLoad = currentLoad;
		}
	}

}
