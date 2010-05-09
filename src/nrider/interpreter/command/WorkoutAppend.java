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
package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.ride.IRide;
import nrider.ride.RideLoader;
import nrider.ride.RideScript;
import nrider.ride.TimeBasedRide;

/**
 * appends a ride to the currently loaded ride.
 * todo: should allow to work for other types of ride but mixing types not supported.
 */
public class WorkoutAppend extends BaseCommand
{
	public String getDescription()
	{
		return "Loads a workout file and appends to the current workout.  <filename>";
	}

	@Override
	public String run( String[] args ) throws Exception
	{
		IRide ride = new RideLoader().loadRide( args[0] );
		if( args.length > 1 )
		{
			ride.getScript().adjustLoad( Double.parseDouble( args[1] ) );
		}

		if( args.length > 4 )
		{
			ride.getScript().crop( Long.parseLong( args[3] ), Long.parseLong( args[4]));
		}

		if( args.length > 2 )
		{
			ride.getScript().adjustLength( Double.parseDouble( args[2] ) );
		}

		WorkoutSession session = WorkoutSession.instance();

		RideScript script = new RideScript();
		script.append( session.getRide().getScript() );
		script.append( ride.getScript() );

		session.setRide( new TimeBasedRide( script ) );
		return null;
	}
}