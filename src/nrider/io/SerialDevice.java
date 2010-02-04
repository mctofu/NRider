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

import gnu.io.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 *
 */
public abstract class SerialDevice implements SerialPortEventListener
{
	private final static Logger LOG = Logger.getLogger( ComputrainerController.class );
	private CommPortIdentifier _commPortId;
	private SerialPort _serialPort;
	private OutputStream _output;
	private InputStream _input;
	private static ArrayList<CommPortIdentifier> _commPortIdentifiers = new ArrayList<CommPortIdentifier>();

	public CommPortIdentifier getCommPortId()
	{
		return _commPortId;
	}

	public OutputStream getOutput()
	{
		return _output;
	}

	public InputStream getInput()
	{
		return _input;
	}

	public static ArrayList<CommPortIdentifier> getPortIdentifiers()
	{
		// errors happen if we call getPortIdentifiers more than once so cache the results.  maybe upgrading the rxtx library would help.
		synchronized( _commPortIdentifiers )
		{
			if( _commPortIdentifiers.size() == 0 )
			{
				Enumeration commPortIds = CommPortIdentifier.getPortIdentifiers();
				while( commPortIds.hasMoreElements() )
				{
					CommPortIdentifier commPortId = (CommPortIdentifier) commPortIds.nextElement();
					if( commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL )
					{
						_commPortIdentifiers.add( commPortId );
					}
				}
			}
		}
		return _commPortIdentifiers;
	}


	public void setCommPortName( String name )
	{
		ArrayList<CommPortIdentifier> commPortIds = getPortIdentifiers();
		for( CommPortIdentifier commPortId : commPortIds )
		{
			if( commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL && commPortId.getName().equals( name ) )
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

	public void connect() throws PortInUseException
	{
		_serialPort = (SerialPort) _commPortId.open( "nrider.NRider", 2000 );
		try
		{
			setupCommParams( _serialPort );
		}
		catch( UnsupportedCommOperationException e )
		{
			throw new Error( "Unhandled serial port setup error", e );
		}
		try
		{
			_input = _serialPort.getInputStream();
			_output = _serialPort.getOutputStream();
			_serialPort.addEventListener( this );
			_serialPort.notifyOnDataAvailable( true );
			connected();
		}
		catch( TooManyListenersException e )
		{
			throw new Error( "Unhandled serial port communication error", e );
		}
		catch( IOException e )
		{
			throw new Error( "Unhandled serial port communication error", e );
		}
	}

	public void close() throws IOException
	{
		if( _output != null )
		{
			_output.close();
		}
		if( _input != null )
		{
			_input.close();
		}
		if( _serialPort != null )
		{
			_serialPort.removeEventListener();
			_serialPort.close();
		}
	}

	protected abstract void commPortSet();
	protected abstract void setupCommParams( SerialPort serialPort ) throws UnsupportedCommOperationException;
	protected abstract void connected();

}
