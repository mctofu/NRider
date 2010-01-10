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
import nrider.debug.SimController;
import nrider.interpreter.BaseCommand;

/**
 *
 */
public class AddSimulator extends BaseCommand
{
	public String getDescription()
	{
		return "Add a Simulator. - <identifier>";
	}

	public String run( String[] args )
	{
		if( args.length != 1 )
		{
			return "Invalid syntax";
		}
		SimController simTrainer = new SimController( args[0] );
		try
		{
			simTrainer.connect();
		}
		catch( Exception e )
		{
			return "Error connecting to computrainer: " + e.getMessage();
		}

		WorkoutSession session = WorkoutSession.instance();

		session.addWorkoutController( simTrainer );
		simTrainer.addPerformanceDataListener( session );
		simTrainer.addControlDataListener( session );
		return "Added " + simTrainer.getIdentifier();
	}
}