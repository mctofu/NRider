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

import java.util.List;

/**
 * Simple interface to a persistent rider store.
 */
public interface IRiderDb
{
	public List<String> getRiderIds();
	public Rider getRider( String riderId );
	public List<String> getGroupIds();
	public List<String> getGroupRiderIds( String groupId );
	public void addRider( Rider rider );
	public void updateRider( Rider rider );
	public void removeRider( String riderId );
	public void createGroup( String groupId );
	public void removeGroup( String groupId );
	public void addRiderToGroup( String groupId, String riderId );
	public void removeRiderFromGroup( String groupId, String riderId );
}
