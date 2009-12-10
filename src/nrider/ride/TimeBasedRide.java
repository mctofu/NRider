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
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 2:11:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeBasedRide implements IRide
{
	private Timer _timer;
	private Date _startTime;
	private int _elapsed;
	private Status _status = Status.PENDING;
	private RideScript _timeline;

	public TimeBasedRide( RideScript timeline )
	{
		_timeline = timeline;
		_status = Status.READY;
	}

	public void start()
	{
		if( _status == Status.READY || _status == Status.PAUSED )
		{
			_timer = new Timer( "TimeBasedRide", true );
			_startTime = new Date();
			if( _status == Status.READY )
			{
				_elapsed = 0;
			}
			_status = Status.RUNNING;
			for( RideEvent te : _timeline )
			{
				if( te.getPosition() >= _elapsed )
				{
					_timer.schedule( new LoadTask( te.getLoad() ), te.getPosition() - _elapsed );
				}
			}
			_timer.scheduleAtFixedRate( new TimeUpdateTask(), 500, 500 );
		}
	}

	public void pause()
	{
		if( _status == Status.RUNNING )
		{
			_timer.cancel();
			_elapsed += new Date().getTime() - _startTime.getTime();
			_status = Status.PAUSED;
		}
	}

	public void stop()
	{

	}

	public Status getStatus()
	{
		return _status;
	}

	public RideScript getScript()
	{
		return _timeline;
	}

	public class TimeUpdateTask extends TimerTask
	{
		public void run()
		{
			long elapsed = _elapsed + new Date().getTime() - _startTime.getTime();
			WorkoutSession.instance().setRideElapsedTime( elapsed );
		}
	}

	public class CompleteTask extends TimerTask
	{
		public void run()
		{
			System.out.println( "Done" );
			WorkoutSession session = WorkoutSession.instance();

			session.setRideLoad( new RideLoad( RideLoad.Type.WATTS , 50 ) );
		}
	}

	public class LoadTask extends TimerTask
	{
		private RideLoad _load;

		public LoadTask( RideLoad load )
		{
			_load = load;
		}

		public void run()
		{
			System.out.println( "load: " + _load );
			try
			{
				WorkoutSession session = WorkoutSession.instance();
				session.setRideLoad( _load );
			}
			catch( Exception e )
			{
				e.printStackTrace( );
			}
		}
	}


}
