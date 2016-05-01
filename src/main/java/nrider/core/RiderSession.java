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
package nrider.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 10:51:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class RiderSession
{
	private Rider _rider;
	private int _handicap;
	private RideLoad _currentLoad;
	private List<String> _associations = new ArrayList<String>();
	private String _source;
	private HashMap<String,String> _metadata = new HashMap<String, String>();

	public RiderSession( Rider rider, String source )
	{
		_rider = rider;
		_source = source;
	}

	public Rider getRider()
	{
		return _rider;
	}

	public int getHandicap()
	{
		return _handicap;
	}

	public void setHandicap( int handicap )
	{
		_handicap = handicap;
	}

	public RideLoad getCurrentLoad()
	{
		return _currentLoad;
	}

	public void setCurrentLoad( RideLoad currentLoad )
	{
		_currentLoad = currentLoad;
	}

	public void addAssociation( String identifier )
	{
		_associations.add( identifier );		
	}

	public String getSource()
	{
		return _source;
	}

	/*
	 * target load for the rider not including handicap
	 */
	public double getLoadForWorkoutWithoutHandicap()
	{
		return getLoadForWorkout( 0 );
	}

	/*
	 * actual load for the rider including handicap
	 */
	public double getLoadForWorkout()
	{
		return getLoadForWorkout( _handicap );
	}

	private double getLoadForWorkout( int handicap )
	{
		if( _currentLoad == null )
		{
			return 0;
		}
		else if( _currentLoad.getType() == RideLoad.Type.PERCENT_THRESHOLD )
		{
			return ( _rider.getThresholdPower() - handicap ) * _currentLoad.getValue() / 100;
		}
		else if( _currentLoad.getType() == RideLoad.Type.WATTS )
		{
			return _currentLoad.getValue() - handicap;
		}
		return 0;
	}
}
