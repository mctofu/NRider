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

import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;

import java.util.HashMap;

/**
 *
 */
public class RiderPerformanceMonitor implements IPerformanceDataListener
{
	private boolean _active;
	private HashMap<String, RiderMonitor> _riderMap = new HashMap<String, RiderMonitor>( );

	public void handlePerformanceData( String identifier, PerformanceData data )
	{
		if( _active )
		{
			switch( data.getType() )
			{
				case SPEED:
					float speed = (float) ( data.getValue() * 2.237 );
					if( speed < 18 )
					{
						sendSpeedLow( identifier );
					}
					else if( speed > 25 )
					{
						sendSpeedHigh( identifier );
					}
					else
					{
					 	clearSpeed( identifier );
					}
			}
		}
	}

	private void sendSpeedLow( String identifier )
	{
		RiderMonitor monitor = getMonitor( identifier );
		if( !monitor._isUnderSpeed )
		{
			monitor._isUnderSpeed = true;
			WorkoutSession.instance().addRiderAlert( identifier, WorkoutSession.RiderAlertType.SPEED_LOW );
			if( monitor._isOverSpeed )
			{
			    monitor._isOverSpeed = false;
				WorkoutSession.instance().removeRiderAlert( identifier, WorkoutSession.RiderAlertType.SPEED_HIGH );				
			}
		}
	}

	private void sendSpeedHigh( String identifier )
	{
		RiderMonitor monitor = getMonitor( identifier );
		if( !monitor._isOverSpeed )
		{
			monitor._isOverSpeed = true;
			WorkoutSession.instance().addRiderAlert( identifier, WorkoutSession.RiderAlertType.SPEED_HIGH );
			if( monitor._isUnderSpeed )
			{
			    monitor._isUnderSpeed = false;
				WorkoutSession.instance().removeRiderAlert( identifier, WorkoutSession.RiderAlertType.SPEED_LOW );
			}
		}
	}

	private void clearSpeed( String identifier )
	{
		RiderMonitor monitor = getMonitor( identifier );
		if( monitor._isOverSpeed )
		{
			monitor._isOverSpeed = false;
			WorkoutSession.instance().removeRiderAlert( identifier, WorkoutSession.RiderAlertType.SPEED_HIGH );
		}
		if( monitor._isUnderSpeed )
		{
			monitor._isUnderSpeed = false;
			WorkoutSession.instance().removeRiderAlert( identifier, WorkoutSession.RiderAlertType.SPEED_LOW );
		}
	}	

	private RiderMonitor getMonitor( String identifier )
	{
		if( !_riderMap.containsKey( identifier ))
		{
			_riderMap.put( identifier, new RiderMonitor() );
		}

		return _riderMap.get( identifier );
	}

	public void activate()
	{
		_active = true;
	}

	public void deactivate()
	{
		_active = false;
	}


	class RiderMonitor
	{
		boolean _isUnderSpeed;
		boolean _isOverSpeed;
		boolean _isAssisted;
	}

}
