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
package nrider.io;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 7:11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PerformanceData
{
	public enum Type { POWER, SPEED, CADENCE, HEART_RATE, DISTANCE, EXT_HEART_RATE, EXT_CADENCE, EXT_POWER }

	private Type _type;
	private float _value;

	public Type getType()
	{
		return _type;
	}

	public void setType( Type type )
	{
		_type = type;
	}

	public float getValue()
	{
		return _value;
	}

	public void setValue( float value )
	{
		_value = value;
	}
}
