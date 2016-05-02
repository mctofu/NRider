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

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public abstract class SerialDevice implements SerialPortEventListener
{
	private final static Logger LOG = Logger.getLogger( ComputrainerController.class );
	private String _commPortId;
	private SerialPort _serialPort;
	private static ArrayList<String> _commPortIdentifiers = new ArrayList<>();

	private Object _lock = new Object();
	private AtomicBoolean _open = new AtomicBoolean();

	public String getCommPortId()
	{
		return _commPortId;
	}

	public int read() throws IOException
	{
		synchronized( _open )
		{
			if( _open.get() )
			{
				synchronized( _lock )
				{
					try {
						return _serialPort.readBytes(1)[0];
					} catch (SerialPortException e) {
						throw new IOException(e);
					}
				}
			}
			return -1;
		}
	}

	public void write( byte[] bytes ) throws IOException
	{
		synchronized( _open )
		{
			if( _open.get() )
			{
				synchronized( _lock )
				{
					try {
						_serialPort.writeBytes(bytes);
					} catch (SerialPortException e) {
						throw new IOException(e);
					}
				}
			}
		}
	}
	
	public static ArrayList<String> getPortIdentifiers()
	{
		synchronized( _commPortIdentifiers )
		{
			if( _commPortIdentifiers.size() == 0 )
			{
				_commPortIdentifiers.addAll( Arrays.asList( SerialPortList.getPortNames() ) );
			}
		}
		return _commPortIdentifiers;
	}


	public void setCommPortName( String name )
	{
		for( String commPortId : getPortIdentifiers() )
		{
			if( commPortId.equals( name ) )
			{
				_commPortId = commPortId;
				break;
			}
		}
		if( _commPortId != null )
		{
			commPortSet();
		}
	}

	public void connect()
	{
		synchronized( _open )
		{
			synchronized( _lock )
			{
				_serialPort = new SerialPort( _commPortId );
				try
				{
					_serialPort.openPort();
					setupCommParams( _serialPort );
				}
				catch( SerialPortException e )
				{
					throw new Error( "Unhandled serial port setup error", e );
				}
				try
				{
					_open.set( true );
					_serialPort.addEventListener( this, SerialPort.MASK_RXCHAR );
				}
				catch( SerialPortException e )
				{
					throw new Error( "Unhandled serial port communication error", e );
				}
			}
		}
		connected();
	}

	public void close() throws IOException
	{
		synchronized( _open )
		{
			_open.set( false );
		}
		synchronized( _lock )
		{
			if( _serialPort != null )
			{
				try {
					_serialPort.removeEventListener();
					_serialPort.closePort();
				} catch (SerialPortException e) {
					throw new Error( e );
				}
			}
		}
	}

	protected abstract void commPortSet();
	protected abstract void setupCommParams( SerialPort serialPort ) throws SerialPortException;
	protected abstract void connected();
}