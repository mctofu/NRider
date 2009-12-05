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
	private float _power;
	private float _speed;
	private float _cadence;
	private float _heartRate;
	private float _extHeartRate;

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
		if( power != _power )
		{
			SendUpdate( PerformanceData.Type.POWER, power );
		}
		_power = power;
	}

	public void setSpeed( float speed )
	{
		if( speed != _speed )
		{
			SendUpdate( PerformanceData.Type.SPEED, speed );
		}

		_speed = speed;
	}

	public void setCadence( float cadence )
	{
		if( cadence != _cadence )
		{
			SendUpdate( PerformanceData.Type.CADENCE, cadence );
		}

		_cadence = cadence;
	}

	public void setHeartRate( float heartRate )
	{
		if( heartRate != _heartRate )
		{
			SendUpdate( PerformanceData.Type.HEART_RATE, heartRate );
		}

		_heartRate = heartRate;
	}

	public void setExtHeartRate( float heartRate )
	{
		if( heartRate != _extHeartRate )
		{
			SendUpdate( PerformanceData.Type.EXT_HEART_RATE, heartRate );
		}

		_extHeartRate = heartRate;
	}

	private void SendUpdate( PerformanceData.Type type, float value )
	{
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
