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
import nrider.io.ant.AntReceiver;

/**
 *
 */
public class AddAnt extends BaseCommand
{
	public String getDescription()
	{
		return "Add an ANT receiver.  - <COM PORT NAME>";
	}

	@Override
	public String run( String[] args ) throws Exception
	{
		if( args.length != 1 )
		{
			return "Invalid syntax";
		}
		AntReceiver ant = new AntReceiver();
		ant.setCommPortName( args[0] );
		try
		{
			ant.connect();
		}
		catch( Exception e )
		{
			return "Error connecting to ant: " + e.getMessage();
		}

		WorkoutSession session = WorkoutSession.instance();

		session.addPerformanceDataSource( ant );
//		ant.addPerformanceDataListener( session );
//		return "Added " + ant.getIdentifier();
		return "Added ant";		
	}
}
