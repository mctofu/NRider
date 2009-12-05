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
public class ScriptedRide implements IRide
{
	private Timer _timer;
	private Date _startTime;
	private int _elapsed;
	private Status _status = Status.PENDING;
	private List<TimeEvent> _timeline = new ArrayList<TimeEvent>();

	public void start()
	{
		if( _status == Status.READY || _status == Status.PAUSED )
		{
			_timer = new Timer( "ScriptedRide", true );
			_startTime = new Date();
			if( _status == Status.READY )
			{
				_elapsed = 0;
			}
			_status = Status.RUNNING;
			for( TimeEvent te : _timeline )
			{
				if( te.getTime() >= _elapsed )
				{
					_timer.schedule( new LoadTask( te.getLoad() ), te.getTime() - _elapsed );
				}
			}
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

	public void load( String fileName ) throws IOException
	{
		BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
		try
		{
			String line = null;
			boolean atData = false;
			long lastTime = 0;
			long lastLoad = 0;
			boolean readOne = false;
			// TODO: we can read this from the file
			RideLoad.Type loadType = RideLoad.Type.PERCENT_THRESHOLD;
			while( ( line = reader.readLine() ) != null )
			{
				if( !atData )
				{
					if( "[COURSE DATA]".equals( line ) )
					{
						atData = true;
					}
				}
				else if( !"[END COURSE DATA]".equals( line ) )
				{
					StringTokenizer parser = new StringTokenizer( line );
					long offset = (long) ( Double.parseDouble( parser.nextToken( "\t" ) ) * 60 * 1000 );
					long load = Integer.parseInt( parser.nextToken( "\t" ) );
					if( !readOne )
					{
						readOne = true;
						// assuming not two entries at 0s, need to set initial load for ride
						_timeline.add( new TimeEvent( offset, new RideLoad( loadType, load ) ) );
					}
					else
					{
						long period = offset - lastTime;
						if( period < 500 )
						{
							// two entries at the same time mean an instantaneous change
							// or if the period is less than our 500 ms rate of adjusting
							_timeline.add( new TimeEvent( offset, new RideLoad( loadType, load ) ) );
						}
						else if( lastLoad != load )
						{
						    // gradual shift from last load to current
							// we will adjust the load every 500 ms

							int steps = (int) period / 500;
							double loadChangePerStep = ( load - lastLoad ) / steps;

							if( Math.abs( load - lastLoad ) > 10 || loadChangePerStep > 1 )
							{
								for( int i = 1; i < steps; i++ )
								{
									_timeline.add( new TimeEvent( lastTime + 500 * i, new RideLoad( loadType, lastLoad + i * loadChangePerStep ) ) );
								}
							}
							// schedule peak slightly early so it won't collide with the next entry if there is an instantaneous change
							_timeline.add( new TimeEvent( offset - 50, new RideLoad( loadType, load ) ) );
						}
					}
					lastTime = offset;
					lastLoad = load;
				}
			}
			_status = Status.READY;
		}
		finally
		{
			reader.close();
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

	class TimeEvent
	{
		long _time;
		RideLoad _load;

		private TimeEvent( long time, RideLoad load )
		{
			_time = time;
			_load = load;
		}

		public long getTime()
		{
			return _time;
		}

		public RideLoad getLoad()
		{
			return _load;
		}
	}
}
