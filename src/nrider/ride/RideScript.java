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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class RideScript implements Iterable<RideEvent>
{
	private List<RideEvent> _script = new ArrayList<RideEvent>();
	private double _maxLoad;
	private long _period;

	public void addEvent( RideEvent event )
	{
		_script.add( event );
		if( event.getLoad().getValue() > _maxLoad )
		{
			_maxLoad = event.getLoad().getValue();
		}

		if( event.getPosition() > _period )
		{
			_period = event.getPosition();
		}
	}

	public double getMaxLoad()
	{
		return _maxLoad;
	}

	public long getPeriod()
	{
		return _period;
	}

	public RideLoad getLoad( long position )
	{
		// todo: could optimize if too slow.
		for( int i = 0; i < _script.size(); i++ )
		{
			if( _script.get( i ).getPosition() > position )
			{
			    return _script.get( i- 1 ).getLoad();
			}
		}
		return null;
	}

	public Iterator<RideEvent> iterator()
	{
		return _script.iterator();
	}
}
