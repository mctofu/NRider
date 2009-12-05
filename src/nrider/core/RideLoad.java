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

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 3, 2009
 * Time: 11:49:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class RideLoad
{
	public enum Type {
		PERCENT_THRESHOLD
				{
					public String format( double value )
					{
						return value + "%";
					}
				},
		WATTS
				{
					public String format( double value )
					{
						return Double.toString( value );
					}
				},
		GRADIENT
				{
					public String format( double value )
					{
						return value + "%";
					}
				};

		public abstract String format( double value );
	};

	private Type _type;
	private double _value;

	public RideLoad( Type type, double value )
	{
		_type = type;
		_value = value;
	}

	public Type getType()
	{
		return _type;
	}

	public double getValue()
	{
		return _value;
	}

	@Override
	public String toString()
	{
		return _type.format( _value );
	}
}
