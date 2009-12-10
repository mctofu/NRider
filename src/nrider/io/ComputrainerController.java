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
import nrider.event.EventPublisher;
import nrider.event.IEvent;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Code to communicate with a CompuTrainer
 * Most of this is from Mark Liversedge's CompCs For Mac code.
 */
public class ComputrainerController extends SerialDevice implements IPerformanceDataSource, IControlDataSource, IWorkoutController
{
	private final static Logger LOG = Logger.getLogger( ComputrainerController.class );

	private static byte[] ERGO_INIT_COMMAND = {
        0x6D, 0x00, 0x00, 0x0A, 0x08, 0x00, (byte) 0xE0,   // unknown
        0x65, 0x00, 0x00, 0x0A, 0x10, 0x00, (byte) 0xE0,   // unknown
        0x00, 0x00, 0x00, 0x0A, 0x18, 0x5D, (byte) 0xC1,   // unknown
        0x33, 0x00, 0x00, 0x0A, 0x24, 0x1E, (byte) 0xE0,   // unknown
        0x6A, 0x00, 0x00, 0x0A, 0x2C, 0x5F, (byte) 0xE0,   // unknown
        0x41, 0x00, 0x00, 0x0A, 0x34, 0x00, (byte) 0xE0,   // unknown
        0x2D, 0x00, 0x00, 0x0A, 0x38, 0x10, (byte) 0xC2   // unknown
	};

	private EventPublisher<IControlDataListener> _controlPublisher = EventPublisher.directPublisher();
	private PerformanceDataChangePublisher _performanceDataPublisher = new PerformanceDataChangePublisher( "CompuTrainer:Unassigned" );
	private boolean _connected;
	private TrainerMode _mode = TrainerMode.ERG;
	private int _load = 50;
	private int _receivedMessagesSinceLastSend = 0;
	private final Object _msgCountLock = new Object();
	private byte[] _msgBuffer = new byte[7];
	private int _msgBufferPos = 0;
	private int _buttons;
	// TODO: does every computrainer really need it's own write thread or can they be shared?
	private ExecutorService _writeExecutor = Executors.newSingleThreadExecutor();

	public String getType()
	{
		return "CompuTrainer";
	}

	public String getIdentifier()
	{
		if( getCommPortId() != null )
		{
			return "CompuTrainer:" + getCommPortId().getName();
		}
		return "CompuTrainer:Unassigned";
	}

	public TrainerMode getMode()
	{
		return _mode;
	}

	public void setMode( TrainerMode _mode )
	{
		this._mode = _mode;
	}

	public int getLoad()
	{
		return _load;
	}

	public void setLoad( double load )
	{
		if( load > 1500 )
		{
			_load = 1500;
		}
		else if( load < 50 )
		{
			_load = 50;
		}
		else
		{
			_load = (int) load;
		}
	}

	@Override
	protected void commPortSet()
	{
		_performanceDataPublisher.setIdentifier( "CompuTrainer:" + getCommPortId().getName() );		
	}

	@Override
	protected void setupCommParams( SerialPort serialPort ) throws UnsupportedCommOperationException
	{
		serialPort.setSerialPortParams(2400,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode( SerialPort.FLOWCONTROL_RTSCTS_OUT | SerialPort.FLOWCONTROL_RTSCTS_IN );
	}

	@Override
	protected void connected()
	{
		try
		{
			getOutput().write( "RacerMate".getBytes() );
			getOutput().flush();
		}
		catch( IOException e )
		{
			throw new Error( "Unhandled serial port communication error", e );
		}
	}

	public void serialEvent( SerialPortEvent serialPortEvent )
	{
		try
		{
			int data;
			while( ( data = getInput().read() ) > -1 )
			{
				_msgBuffer[_msgBufferPos++] = (byte) data;
				if( !_connected && _msgBufferPos == 6 ) // handshake message is different from everything else
				{
					byte[] msg = new byte[6];
					System.arraycopy( _msgBuffer, 0, msg, 0, 6 );
					handleMessageReceived( msg );
					_msgBufferPos = 0;
				}
				else if( _msgBufferPos == 7 )
				{
					byte[] msg = new byte[7];
					System.arraycopy( _msgBuffer, 0, msg, 0, 7 );
					handleMessageReceived( msg );
					_msgBufferPos = 0;
				}
			}
		}
		catch ( IOException e )
		{
			LOG.error( "Read error on " + getIdentifier(), e );
		}
	}

	public void handleMessageReceived( byte[] msg ) throws IOException
	{
//		System.out.print( "Got message: "  );
//		for( byte b : msg )
//		{
//			System.out.print( (int) b + "," );
//		}
//		System.out.println();
		if( !_connected )
		{
			if( "LinkUp".equals( new String(msg) ) )
			{
				_connected = true;
				System.out.println( "Connected, sending init" );
				sendInitMessage();
				sendControlMessage( _load );
			}
		}
		else
		{
		   	ComputrainerData data = new ComputrainerData( msg );
			handleData( data );
			boolean needControlMessage;
			// TODO: this probably doesn't always work right, revisit when cleaning up the send message
			synchronized( _msgCountLock )
			{
				_receivedMessagesSinceLastSend++;
				needControlMessage = _receivedMessagesSinceLastSend >= 4;
			}
			if( needControlMessage )
			{
				sendControlMessage( _load );
			}
		}
	}

	public void handleData( ComputrainerData data )
	{
//		System.out.println("Got data:" + data.getType() );
		if( data.getType() == null )
		{
			return;
		}
		switch( data.getType() )
		{
			case POWER:
				_performanceDataPublisher.setPower( data.getValue12() );
				break;
			case CADENCE:
				_performanceDataPublisher.setCadence( data.getValue8() );
				break;
			case SPEED:
				_performanceDataPublisher.setSpeed( (float) ( data.getValue12() * .01 * .9 ) );
				break;
			case HEARTRATE:
				_performanceDataPublisher.setHeartRate( data.getValue8() );
				break;
		}

		if( _buttons != data.getButtons() )
		{
			// TODO: Workout strategy for detecting button hold? or not being too sensitive to button touches?

			_buttons = data.getButtons();

			// convert buttons.  so far i'm thinking pressing two keys at once results in a new key type.
			if( data.isF1Pressed() )
			{
				sendControlEvent( ControlData.Type.START );
			}
			else if( data.isResetPressed() )
			{
				sendControlEvent( ControlData.Type.STOP );
			}
			else if( data.isF2Pressed() )
			{
				sendControlEvent( ControlData.Type.F2 );
			}
			else if( data.isF3Pressed() )
			{
				sendControlEvent( ControlData.Type.F3 );
			}
			else if( data.isPlusPressed() )
			{
				sendControlEvent( ControlData.Type.PLUS );
			}
			else if( data.isMinusPressed() )
			{
				sendControlEvent( ControlData.Type.MINUS );
			}
		}
	}

	private void sendControlEvent( ControlData.Type type )
	{
		final ControlData data = new ControlData( type );

		_controlPublisher.publishEvent(
				new IEvent<IControlDataListener>()
				{
					public void trigger( IControlDataListener target )
					{
						target.handleControlData( getIdentifier(), data );
					}
				});
	}

	private synchronized void sendInitMessage() throws IOException
	{
		getOutput().write( ERGO_INIT_COMMAND );
		sendControlMessage( _load );
	}

	private synchronized void sendControlMessage( int load ) throws IOException
	{
		final byte[] msg = new byte[7];

		int crc = calcCRC(load);

		// BYTE 0 - 49 is b0, 53 is b4, 54 is b5, 55 is b6
		msg[0] = (byte) ( crc >> 1 ); // set byte 0

		msg[3] = 0x0A;

		// BYTE 4 - command and highbyte
		msg[4]  = 0x40; // set command
		msg[4] |= (load&(2048+1024+512)) >> 9;

		// BYTE 5 - low 7
		msg[5] = 0;
		msg[5] |= (load&(128+64+32+16+8+4+2)) >> 1;

		// BYTE 6 - sync + z set
		msg[6] = (byte) ( 128+64 );

		// low bit of supplement in bit 6 (32)
		msg[6] |= ( crc & 1 ) > 0 ? 32 : 0;
		// Bit 2 (0x02) is low bit of high byte in load (bit 9 0x256)
		msg[6] |= (load & 256) > 0 ? 2 : 0;
		// Bit 1 (0x01) is low bit of low byte in load (but 1 0x01)
		msg[6] |= load&1;

		_writeExecutor.execute(
			new Runnable()
			{
				public void run()
				{
					synchronized( _msgCountLock )
					{
						_receivedMessagesSinceLastSend = 0;
					}
					try
					{
						getOutput().write( msg );
						getOutput().flush();
					}
					catch( IOException e )
					{
						LOG.error( "Write error on " + getIdentifier(), e );
					}
				}
			}
			);
	}

	private int calcCRC(int value)
	{
	    return (0xff & (107 - (value & 0xff) - (value >> 8)));
	}

	@Override
	public void close() throws IOException
	{
		_writeExecutor.shutdownNow();
		super.close();
	}

	public void addControlDataListener( IControlDataListener listener )
	{
		_controlPublisher.addListener( listener );
	}

	public void addPerformanceDataListener( IPerformanceDataListener listener )
	{
		_performanceDataPublisher.addPerformanceDataListener( listener );
	}
}
