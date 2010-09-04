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
package nrider.io;

import nrider.event.EventPublisher;
import nrider.event.IEvent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 7:06:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class PerformanceDataChangePublisher implements IPerformanceDataSource
{
	private String _identifier;
	private EventPublisher<IPerformanceDataListener> _performancePublisher = EventPublisher.directPublisher();
	private long _lastSpeedUpdateTime;

	private HashMap<PerformanceData.Type,Float> _lastValue = new HashMap<PerformanceData.Type, Float>( );
	private HashMap<PerformanceData.Type,Long> _lastSent = new HashMap<PerformanceData.Type, Long>( );

	public PerformanceDataChangePublisher( String identifier )
	{
		_identifier = identifier;
	}

	public void setIdentifier( String identifier )
	{
		_identifier = identifier;
	}

	public String getIdentifier()
	{
		return _identifier;
	}

	public void addPerformanceDataListener( IPerformanceDataListener listener )
	{
		_performancePublisher.addListener( listener );
	}

	public void setPower( float power )
	{
		ConsiderUpdate( PerformanceData.Type.POWER, power );
	}

	public void setSpeed( float speed )
	{
		long time = System.currentTimeMillis();
		float distance = speed / ( ( time - _lastSpeedUpdateTime ) / 1000 );
		_lastSpeedUpdateTime = time;
		SendUpdate( PerformanceData.Type.DISTANCE, distance, time );
		ConsiderUpdate( PerformanceData.Type.SPEED, speed );
	}


	public void setCadence( float cadence )
	{
		ConsiderUpdate( PerformanceData.Type.CADENCE, cadence );
	}

	public void setHeartRate( float heartRate )
	{
		ConsiderUpdate( PerformanceData.Type.HEART_RATE, heartRate );
	}

	public void setExtHeartRate( float heartRate )
	{
		ConsiderUpdate( PerformanceData.Type.EXT_HEART_RATE, heartRate );	
	}

	public void setCalibration( float calibration )
	{
		ConsiderUpdate( PerformanceData.Type.CALIBRATION, calibration, 60000 );
	}

	private void ConsiderUpdate( PerformanceData.Type type, float value )
	{
		ConsiderUpdate( type, value, 1000 );
	}
	
	private void ConsiderUpdate( PerformanceData.Type type, float value, long threshold )
	{
		Float lastValue = _lastValue.get( type );

		long now = System.currentTimeMillis();

		if( lastValue != null )
		{
			if( lastValue != value || ( now - _lastSent.get( type ) > threshold ) )
			{
				SendUpdate( type, value, now );
			}
		}
		else if( value != 0 )
		{
			SendUpdate( type, value, now );
		}
	}

	private void SendUpdate( PerformanceData.Type type, float value, long time )
	{
		_lastValue.put( type, value );
		_lastSent.put( type, time );
		final PerformanceData data = new PerformanceData();
		data.setType( type );
		data.setValue( value );
		_performancePublisher.publishEvent(
				new IEvent<IPerformanceDataListener>()
				{
					public void trigger( IPerformanceDataListener target )
					{
						target.handlePerformanceData( getIdentifier(), data );
					}
				}
		);		
	}
}
