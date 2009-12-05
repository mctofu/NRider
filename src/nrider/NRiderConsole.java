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

import nrider.interpreter.CommandInterpreter;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 10:20:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class NRiderConsole
{
	private CommandInterpreter _interpreter = new CommandInterpreter();

	public void runScript( String scriptFilePath ) throws IOException
	{
		System.out.println("Run script " + scriptFilePath);
		BufferedReader reader = new BufferedReader( new FileReader( scriptFilePath ) );
		try
		{
			String line = null;
			while( ( line = reader.readLine() ) != null )
			{
				System.out.println( line );
				processLine( line );
			}
		}
		finally
		{
			reader.close();				
		}
	}

	public void start() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );

		while( true )
		{
			String input = reader.readLine();
			if( "exit".equals( input ) )
			{
				break;
			}
			processLine( input );
		}
	}

	private void processLine( String input )
	{
		String output = _interpreter.executeCommand( input );
		if( output != null )
		{
			System.out.println( output );
		}
	}
}
