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
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 9:16:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class Rider
{
	private String _name;
	private int _thresholdPower;
	private List<String> _devices = new ArrayList<String>();

	public String getIdentifier()
	{
		return getName();
	}

	public String getName()
	{
		return _name;
	}

	public void setName( String name )
	{
		_name = name;
	}

	public int getThresholdPower()
	{
		return _thresholdPower;
	}

	public void setThresholdPower( int thresholdPower )
	{
		_thresholdPower = thresholdPower;
	}

	public List<String> getDevices()
	{
		return _devices;
	}

	public void addDevice( String deviceId )
	{
		_devices.add( deviceId );
	}


}