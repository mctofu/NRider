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

import nrider.NRiderClient;
import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.net.NRiderServer;
import nrider.net.NRiderSlave;

import java.io.IOException;

/**
 *
 */
public class ConnectServer extends BaseCommand
{
	public String getDescription()
	{
		return "Connect to a running NRider server. <ipaddress> <port number>";  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String run( String[] args )
	{
		if( args.length != 2 )
		{
			return "Invalid usage";
		}
		try
		{
			NRiderSlave client = new NRiderSlave( args[0], Integer.parseInt( args[1]) );
			client.connect();
			WorkoutSession.instance().setNetSource( client );
		}
		catch( IOException e )
		{
			return "Unable to start server: " + e.getMessage();
		}

		return null;
	}
}
