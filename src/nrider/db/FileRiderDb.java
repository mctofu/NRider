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
package nrider.db;

import nrider.core.Rider;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple file based impl of rider db
 */
public class FileRiderDb implements IRiderDb
{
	private final static Logger LOG = Logger.getLogger( FileRiderDb.class );
	private static final String RIDERS_DB = "riders.db";
	private static final String GROUPS_DB = "groups.db";

	public List<String> getRiderIds()
	{
		return ReadLines( RIDERS_DB );
	}

	public Rider getRider( String riderId )
	{
		Rider rider = null;
		try {
			File riderDb = new File( riderDb( riderId ) );
			String line;
			if( riderDb.exists() )
			{
				BufferedReader input =  new BufferedReader( new FileReader( riderDb ) );
				try {
					line = input.readLine();
					rider = new Rider();
					rider.setName( riderId );
					rider.setThresholdPower( Integer.parseInt( line ) );
					while (( line = input.readLine()) != null)
					{
						rider.addDevice( line );
					}
				}
				finally {
					input.close();
				}
			}
		}
		catch ( IOException ex) {
		     LOG.error( ex );
		}
		return rider;
	}

	private String riderDb( String riderId )
	{
		return "rider-" + riderId + ".db";
	}

	public List<String> getGroupIds()
	{
		return ReadLines( GROUPS_DB );
	}

	public List<String> getGroupRiderIds( String groupId )
	{
		return ReadLines( groupDb( groupId ) );
	}

	private String groupDb( String groupId )
	{
		return "group-" + groupId + ".db";
	}

	public void addRider( Rider rider )
	{
		List<String> riderIds = getRiderIds();
		if( riderIds.contains( rider.getIdentifier() ) )
		{
			throw new Error( "Rider already exists" );
		}
		updateRider( rider );
		riderIds.add( rider.getIdentifier() );
		WriteLines( RIDERS_DB, riderIds );
	}

	public void updateRider( Rider rider )
	{
		List<String> lines = new ArrayList<String>();

		lines.add( Integer.toString( rider.getThresholdPower() ) );
		for( String device : rider.getDevices() )
		{
			lines.add( device );
		}
		WriteLines( riderDb( rider.getIdentifier() ), lines );
	}

	public void removeRider( String riderId )
	{
		List<String> riderIds = getRiderIds();
		riderIds.remove( riderId );
		WriteLines( RIDERS_DB, riderIds );
	}

	public void createGroup( String groupId )
	{
		List<String> groupIds = getGroupIds();
		if( groupIds.contains( groupId ) )
		{
			throw new Error( "Group already exists" );
		}
		groupIds.add( groupId );
		WriteLines( GROUPS_DB, groupIds );
	}

	public void removeGroup( String groupId )
	{
		List<String> groupIds = getGroupIds();
		groupIds.remove( groupId );
		WriteLines( GROUPS_DB, groupIds );
	}

	public void addRiderToGroup( String groupId, String riderId )
	{
		List<String> riderIds = getGroupRiderIds( groupId );
		if( riderIds.contains( riderId ) )
		{
			return;
		}
		riderIds.add( riderId );
		WriteLines( groupDb( groupId ), riderIds );
	}

	public void removeRiderFromGroup( String groupId, String riderId )
	{
		List<String> riderIds = getGroupRiderIds( groupId );
		riderIds.remove( riderId );
		WriteLines( groupDb( groupId ), riderIds );
	}

	private List<String> ReadLines( String file )
	{
		List<String> riders = new ArrayList<String>();

		try {
			File riderDb = new File( file );
			if( riderDb.exists() )
			{
				BufferedReader input =  new BufferedReader( new FileReader( riderDb ) );
				try {
					String line = null;
					while (( line = input.readLine()) != null)
					{
						riders.add( line );
					}
				}
				finally {
					input.close();
				}
			}
		}
		catch ( IOException ex) {
		     LOG.error( ex );
		}
		return riders;
	}

	private void WriteLines( String file, List<String> lines )
	{
		try {
			File riderDb = new File( file + ".tmp" );

			PrintWriter output =  new PrintWriter( new FileWriter( riderDb ) );
			try {
				for( String line : lines )
				{
					output.println( line );
				}
			}
			finally {
				output.close();
			}

			File targetPath = new File( file );
			if( targetPath.exists() )
			{
				targetPath.delete();
			}

			riderDb.renameTo( targetPath );
		}
		catch ( IOException ex) {
		     LOG.error( ex );
		}
	}
}