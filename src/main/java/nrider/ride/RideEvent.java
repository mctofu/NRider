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
package nrider.ride;

import nrider.core.RideLoad;

/**
 *
 */
public class RideEvent implements Comparable
{
	long _position;
	RideLoad _load;

	public RideEvent( long position, RideLoad load )
	{
		_position = position;
		_load = load;
	}

	public long getPosition()
	{
		return _position;
	}

	public RideLoad getLoad()
	{
		return _load;
	}

	public int compareTo( Object o )
	{
		return ((Long)_position).compareTo( (Long) o );
	}
}