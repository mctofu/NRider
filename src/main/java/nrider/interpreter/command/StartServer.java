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
import nrider.net.NRiderServer;

import java.io.IOException;

/**
 *
 */
public class StartServer extends BaseCommand
{
	public String getDescription()
	{
		return "Start a server for other NRider clients to connect to over the network. <port number>";  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String run( String[] args )
	{
		if( args.length != 1 )
		{
			return "Invalid usage";
		}
		try
		{
			NRiderServer server = new NRiderServer( Integer.parseInt( args[0] ) );
			server.start();
			WorkoutSession.instance().setNetSource( server );
		}
		catch( IOException e )
		{
			return "Unable to start server: " + e.getMessage();
		}
		
		return null;
	}

}
