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
package nrider;

import nrider.core.WorkoutSession;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 30, 2009
 * Time: 11:50:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class NRider
{
	public void start( String[] args )
	{
		DOMConfigurator.configure( "logConfig.xml" );
		NRiderClient client = new NRiderClient();
		try
		{
			client.start();
			WorkoutSession.instance().addPerformanceDataListener( client );
			WorkoutSession.instance().addWorkoutListener( client );
			NRiderConsole console = new NRiderConsole();
			if( args.length > 0 )
			{
				console.runScript( args[0] );
			}

			console.start();
		}
		catch( Exception e )
		{
			e.printStackTrace(  );
		}
		finally
		{
			try
			{
				WorkoutSession.instance().close();
			}
			catch( Exception e )
			{
				e.printStackTrace(  );
			}
		}
		System.exit(0);
	}

	public static void main ( String[] args )
	{
		new NRider().start( args );
	}
}
