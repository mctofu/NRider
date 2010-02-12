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
package nrider.io.ant;

import gnu.io.*;
import nrider.event.EventPublisher;
import nrider.io.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class AntReceiver extends SerialDevice implements IPerformanceDataSource
{
	private final static Logger LOG = Logger.getLogger( AntReceiver.class );

	private enum Device
	{
		HRM ( ANT_PLUS_SPORT_HRM_TYPE, ANT_PLUS_SPORT_HRM_PERIOD ),
		PM ( ANT_PLUS_SPORT_PM_TYPE, ANT_PLUS_SPORT_PM_PERIOD );

		private byte _type;
		private int _period;

		private Device( byte type, int period )
		{
			_type = type;
			_period = period;
		}

		public byte getType()
		{
			return _type;
		}

		public int getPeriod()
		{
			return _period;
		}
	};

	private static final Device[] SEARCH_DEVICES = { Device.HRM, Device.PM };

	private static final byte[] ANT_PLUS_SPORT_CHANNEL_KEY = new byte[] { (byte) 0xB9, (byte) 0xA5, 0x21, (byte) 0xFB, (byte) 0xBD, 0x72, (byte) 0xC3, 0x45 };
	private static final int ANT_PLUS_SPORT_FREQUENCY = 0x39;
	private static final byte ANT_PLUS_SPORT_HRM_TYPE = 0x78;
	private static final byte ANT_PLUS_SPORT_PM_TYPE =  0x0B;
	private static final int ANT_PLUS_SPORT_HRM_PERIOD = 0x1f86;
	private static final int ANT_PLUS_SPORT_PM_PERIOD = 0x1ff6;
	private static final byte ANT_MSG_RESET_SYSTEM = (byte) 0x4a;
	private static final byte ANT_MSG_REQUEST_MESSAGE = (byte) 0x4d;
	private static final byte ANT_MSG_ASSIGN_CHANNEL = (byte) 0x42;
	private static final byte ANT_MSG_SET_CHANNEL_ID = (byte) 0x51;
	private static final byte ANT_MSG_SET_NETWORK_KEY = (byte) 0x46;
	private static final byte ANT_MSG_SET_CHANNEL_SEARCH_TIMEOUT = (byte) 0x44;
	private static final byte ANT_MSG_SET_RADIO_FREQUENCY = (byte) 0x45;
	private static final byte ANT_MSG_SET_MESSAGE_PERIOD = (byte) 0x43;
	private static final byte ANT_MSG_OPEN_CHANNEL = (byte) 0x4B;

	private static final byte ANT_RESP_BROADCAST = (byte) 0x4e;
	private static final byte ANT_RESP_CHANNEL_EVENT = (byte) 0x40;
	
	private ArrayList<Byte> _msgBuffer = new ArrayList<Byte>();
	private Semaphore _responseWaitSemaphore = new Semaphore( 1 );
	private AtomicInteger _expectedMsgId = new AtomicInteger();
	private HashMap<Integer,DeviceInfo> _channelDevices = new HashMap<Integer, DeviceInfo>();
    private IMessageHandler[] _channelHandlers = new IMessageHandler[20];
	private HashMap<Device,Integer> _activeSearches = new HashMap<Device,Integer>( );
	private Queue<Integer> _availableChannels = new LinkedList<Integer>( );
	private int _maxChannelId = -1;

	private EventPublisher<IPerformanceDataListener> _eventPublisher = EventPublisher.directPublisher();

	private Executor _msgHandlerExecutor = Executors.newSingleThreadExecutor();

	public String getIdentifier()
	{
		return "ANT:" + ( getCommPortId() != null ? getCommPortId().getName() : "Unassigned" );
	}

	public void serialEvent( SerialPortEvent serialPortEvent )
	{
		try
		{
			int data;
			StringBuilder sb = new StringBuilder();
			while( ( data = getInput().read() ) > -1 )
			{
				if( _msgBuffer.size() == 0 )
				{
					sb.append( "Read: " );
				}
				sb.append( Integer.toHexString( data )+ " " );
				_msgBuffer.add( (byte) data );

				if( _msgBuffer.size() == 1  && _msgBuffer.get( 0 ) != (byte) 0xA4 )
				{
					LOG.error( "Bad input from ant" );
				}
				else if( _msgBuffer.size() > 2 )
				{
					int msgLength = _msgBuffer.get( 1 );
					if( _msgBuffer.size() == msgLength + 4 )
					{

						byte[] msg =  new byte[_msgBuffer.size()];
						for( int i = 0; i < _msgBuffer.size(); i++ )
						{
							msg[i] = _msgBuffer.get( i );
						}

						_msgBuffer.clear();

						LOG.debug( sb.toString() );
						sb = new StringBuilder();
						try
						{
							if( handleMessage( msg ) )
							{
								_responseWaitSemaphore.release();
							}
						} catch( Exception e )
						{
							LOG.error( "Error handling ant message", e );
						}
					}
				}
			}
		}
		catch ( IOException e )
		{
			LOG.error( "Read error on ANT", e );
		}
	}

	private boolean handleMessage( byte[] msg )
	{
		final AntData antData = new AntData( msg );

		if( antData.getMessageId() == ANT_RESP_BROADCAST )
		{
            if( _channelHandlers[ antData.getChannelId() ] != null )
            {
                _channelHandlers[ antData.getChannelId()].handleMessage( antData );
            }
			if( !_channelDevices.containsKey( antData.getChannelId() ) )
			{
				// TODO: Is there still any point in doing this since we are doing specific device searches?
				// query to find the device type
				_msgHandlerExecutor.execute( new Runnable()
					{
						public void run() {
							sendMessage( ANT_MSG_REQUEST_MESSAGE, new byte[] { (byte) antData.getChannelId(), ANT_MSG_SET_CHANNEL_ID } );						}
					});
			}

			return false;
		}
		else if( antData.getMessageId() == ANT_RESP_CHANNEL_EVENT )
		{
			int responseId = antData.getData()[0];
			if(  responseId == _expectedMsgId.get() )
			{
				return true;
			}
			else if( responseId == 1 )
			{
				switch( msg[5] )
				{
					case 0:
						LOG.debug("Success");
						break;
					case 1:
						LOG.debug("Search timeout");  // search timeout or lost communication with a device.
						synchronized( _activeSearches )
						{
							// if we were searching for a device and didn't find anything clear state so we can try again
							Device removeDevice = null;
							for( Map.Entry<Device, Integer> deviceChannelEntry : _activeSearches.entrySet() )
							{
								if( antData.getChannelId() == deviceChannelEntry.getValue() )
								{
									removeDevice = deviceChannelEntry.getKey();
									break;
								}
							}
							if( removeDevice != null )
							{
								clearSearch( removeDevice );
							}

							// cleanup if a device was previously found
							_channelDevices.remove( antData.getChannelId() );
							_channelHandlers[antData.getChannelId()] = null;

							// this channel id is available for use again.
							_availableChannels.add( antData.getChannelId() );

							_msgHandlerExecutor.execute( new Runnable() {
								public void run()
								{
									searchDevices();
								}
							});
						}
						break;
					case 2:
						LOG.debug("Event RX Fail");
						break;
					case 3:
						LOG.debug("Event TX");
						break;
					case 4:
						LOG.debug("Event Transfer RX Fail");
						break;
					case 5:
						LOG.debug("Event Transfer TX Complete");
						break;
					case 6:
						LOG.debug("Event Transfer TX Fail");
						break;
					case 7:
						LOG.debug("Event Channel Closed");
						break;
					case 8:
						LOG.debug("Event RX Fail go to search");
						break;
					case 9:
						LOG.debug("Event channel collision");
						break;
					case 10:
						LOG.debug("Event Tranfer Tx Start");
						break;
					case 21:
						LOG.debug("Channel in wrong state");
						break;
					case 22:
						LOG.debug("Channel not opened");
						break;
					case 24:
						LOG.debug("Channel id not set");
						break;
					case 25:
						LOG.debug("Close all channels");
						break;
					case 40:
						LOG.debug("Invalid message");
						break;
					case 41:
						LOG.debug("Invalid network number");
						break;
					case 49:
						LOG.debug("Invalid Scan TX Channel");
						break;
					default:
						LOG.debug("Event " + msg[5] );
				}
			}
		}
		else if( antData.getMessageId() == ANT_MSG_SET_CHANNEL_ID )
		{
			DeviceInfo deviceInfo = new DeviceInfo( ( (int) antData.getData()[0] & 0xFF ) + ( (int) antData.getData()[1] & 0xFF ) * 255, (int) antData.getData()[2] & 0xFF );
			LOG.debug("Found device " + deviceInfo.getDeviceNumber() + "/" + deviceInfo.getDeviceType() + " on channel " + antData.getChannelId() );
			synchronized( _activeSearches )
			{
				_channelDevices.put( antData.getChannelId(), deviceInfo );
				switch( deviceInfo.getDeviceType() )
				{
					case ANT_PLUS_SPORT_HRM_TYPE:
						LOG.debug( "HRM" );
						_channelHandlers[antData.getChannelId()] = new HrmHandler( _eventPublisher );
						break;
					case ANT_PLUS_SPORT_PM_TYPE:
						LOG.debug( "Power Meter" );
						_channelHandlers[antData.getChannelId()] = new PowerHandler( _eventPublisher );
						break;
				}

				clearSearch( deviceInfo.getDevice() );
			}

			_msgHandlerExecutor.execute( new Runnable() {
				public void run()
				{
					searchDevices();
				}
			});

			return true;
		}

		return false;
	}



	@Override
	protected void commPortSet()
	{

	}

	@Override
	protected void setupCommParams( SerialPort serialPort ) throws UnsupportedCommOperationException
	{
		serialPort.setSerialPortParams(4800,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode( SerialPort.FLOWCONTROL_NONE  );
	}

	@Override
	protected void connected()
	{
		sendResetSystem();
		try
		{
			Thread.sleep( 500 );
		}
		catch( InterruptedException e )
		{
	        LOG.error( e );
		}

		byte[] networkKeyMsg = new byte[9];
		networkKeyMsg[0] = 1; // network 1
		System.arraycopy( ANT_PLUS_SPORT_CHANNEL_KEY, 0, networkKeyMsg, 1, 8 );
		sendMessage( ANT_MSG_SET_NETWORK_KEY, networkKeyMsg );

		searchDevices();

	}

	private void searchDevices()
	{
		synchronized( _activeSearches )
		{
			for( final Device d : SEARCH_DEVICES )
			{
				if( !_activeSearches.containsKey( d ) )
				{
					final int channelId = obtainOpenChannelId();

					_activeSearches.put( d, channelId );

					_msgHandlerExecutor.execute( new Runnable() {
							public void run()
							{
								searchChannel( (byte) ( channelId ), d );
							}
						});
				}
			}
		}
	}

	private void clearSearch( Device device )
	{
		synchronized( _activeSearches )
		{
			_activeSearches.remove( device );
		}
	}

	private int obtainOpenChannelId()
	{
		synchronized( _activeSearches )
		{
			if( _availableChannels.size() > 0 )
			{
				return _availableChannels.remove();
			}
			_maxChannelId++;
			return _maxChannelId;
		}
	}

	private void searchChannel( byte channelId, Device device )
	{
		sendMessage( ANT_MSG_ASSIGN_CHANNEL, new byte[] { channelId, 0, 1 } ); // channel, receive channel, ant + sport net
		sendMessage( ANT_MSG_SET_CHANNEL_ID, new byte[] { channelId, 0, 0, device.getType(), 0 } ); // channel, any device (2 byte), no pairing (bit), any device type (7 bits), any tranmission type
		sendMessage( ANT_MSG_SET_CHANNEL_SEARCH_TIMEOUT, new byte[] { channelId, 0x1e });
		sendMessage( ANT_MSG_SET_RADIO_FREQUENCY, new byte[] { channelId, ANT_PLUS_SPORT_FREQUENCY });
		sendMessage( ANT_MSG_SET_MESSAGE_PERIOD, new byte[] { channelId, (byte) ( device.getPeriod() % 256 ), (byte) ( device.getPeriod() / 256 ) } );
		sendMessage( ANT_MSG_OPEN_CHANNEL, new byte[] { channelId });
	}

	private void sendResetSystem()
	{
		sendMessage( ANT_MSG_RESET_SYSTEM, new byte[] { 0 }, false );
	}

	private void sendRequestVersion()
	{
		sendMessage( ANT_MSG_REQUEST_MESSAGE, new byte[] { 0, 0x54 } );
	}

	private void sendMessage( byte msgId, byte[] data )
	{
		sendMessage( msgId, data, true );
	}

	private synchronized void sendMessage( byte msgId, byte[] data, boolean waitForResponse )
	{
		//Byte # Name Length Description
		//0 SYNC 1 Byte Fixed value of 10100100 (MSB:LSB)
		//1 MSG LENGTH 1 Byte Number of data bytes in the message. 1 < N < 9
		//2 MSG ID 1 Byte Data Type Identifier
		//	0: Invalid
		//	1..255: Valid Data Type (See Section 9 for details)
		//3..N+2 DATA_1..DATA_N N Bytes Data bytes
		//N+3 CHECKSUM 1 Byte XOR of all previous bytes including the SYNC byte		

		byte[] msg = new byte[ data.length + 4 ];
		msg[0] = (byte) 0xA4;
		msg[1] = (byte) data.length;
		msg[2] = msgId;
		System.arraycopy( data, 0, msg, 3, data.length );

		for( int i = 0; i < msg.length -1 ; i++ )
		{
			msg[msg.length - 1] ^= msg[i];
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Output: ");
		for( byte b : msg )
		{
			sb.append( HexUtil.toHexString( b ) + " " );
		}
		LOG.debug(sb);

		if( waitForResponse )
		{
			try
			{
				_responseWaitSemaphore.acquire();
			}
			catch( InterruptedException e )
			{
				LOG.error( e );
			}
			_expectedMsgId.set( msgId );
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

		if( waitForResponse )
		{
			try
			{
				// this will block until the read thread has read a response
				if( !_responseWaitSemaphore.tryAcquire( 1, 2, TimeUnit.SECONDS) )
				{
					LOG.error( "Ant device not responding or deadlocked" );
					throw new Error("Ant device did not respond in time");
				}
				_responseWaitSemaphore.release();
			}
			catch( InterruptedException e )
			{
				LOG.error( e );
			}
		}
	}



	@Override
	public void close() throws IOException
	{
		sendResetSystem();
	}

	public void addPerformanceDataListener( IPerformanceDataListener listener )
	{
		_eventPublisher.addListener( listener );
	}

	class DeviceInfo
	{
		private int _deviceNumber;
		private int _deviceType;
		private Device _device;
		private String _serialNumber;

		DeviceInfo( int deviceNumber, int deviceType )
		{
			_deviceNumber = deviceNumber;
			_deviceType = deviceType;
			for( Device d : SEARCH_DEVICES )
			{
				if( d.getType() == _deviceType )
				{
					_device = d;
				}
			}
		}

		public int getDeviceNumber()
		{
			return _deviceNumber;
		}

		public int getDeviceType()
		{
			return _deviceType;
		}

		public Device getDevice()
		{
			return _device;
		}

		public String getSerialNumber()
		{
			return _serialNumber;
		}

		public void setSerialNumber( String serialNumber )
		{
			_serialNumber = serialNumber;
		}
	}

}
