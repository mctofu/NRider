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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *  List identifiers publishing stats that aren't mapped to riders
 */
public class ListUnmapped extends BaseCommand
{
	public String getDescription()
	{
		return "List identifiers publishing stats that aren't mapped to riders";
	}

	public String run( String[] args )
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		for( String id : WorkoutSession.instance().getUnmappedIdentifiers() )
		{
			pw.println( id );
		}
		pw.flush();
		return sw.toString();
	}
}
