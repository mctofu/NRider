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
package nrider.media;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Feb 21, 2010
 * Time: 12:38:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class MediaEvent
{
	public enum Type
	{
		LOAD, PLAY, PAUSE, SEEK, STOP
	}

	private Type _type;
	private String _media;
	private int _position;
	private String _value;

	public MediaEvent( Type type, String media, int position, String value )
	{
		_type = type;
		_media = media;
		_position = position;
		_value = value;
	}

	public Type getType()
	{
		return _type;
	}

	public String getMedia()
	{
		return _media;
	}

	public int getPosition()
	{
		return _position;
	}

	public String getValue()
	{
		return _value;
	}
}
