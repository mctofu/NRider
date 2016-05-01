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
import java.util.StringTokenizer;

/**
 *
 */
public class RideLoader
{

	public IRide loadRide( String path ) throws IOException
	{
		RideScript script = new RideScript();

		BufferedReader reader = new BufferedReader( new FileReader( path ) );
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
						script.addEvent( new RideEvent( offset, new RideLoad( loadType, load ) ) );
					}
					else
					{
						long period = offset - lastTime;
						if( period < 500 )
						{
							// two entries at the same time mean an instantaneous change
							// or if the period is less than our 500 ms rate of adjusting
							script.addEvent( new RideEvent( offset, new RideLoad( loadType, load ) ) );
						}
						else
						{
							// gradual shift from last load to current
							// we will adjust the load every 500 ms

							int steps = (int) period / 500;
							double loadChangePerStep = ( load - lastLoad ) / steps;

							if( Math.abs( load - lastLoad ) > 10 || loadChangePerStep > 1 )
							{
								for( int i = 1; i < steps; i++ )
								{
									script.addEvent( new RideEvent( lastTime + 500 * i, new RideLoad( loadType, lastLoad + i * loadChangePerStep ) ) );
								}
							}
							// schedule peak slightly early so it won't collide with the next entry if there is an instantaneous change
							script.addEvent( new RideEvent( offset - 50, new RideLoad( loadType, load ) ) );
						}
					}
					lastTime = offset;
					lastLoad = load;
				}
			}
		}
		finally
		{
			reader.close();
		}

		return new TimeBasedRide( script );
	}
}
