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
package nrider.net;

import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.IPerformanceDataListener;
import nrider.io.IPerformanceDataSource;
import nrider.io.PerformanceData;
import nrider.ride.IRide;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class NRiderServer extends NetSource
{
	private int _port;
	private ServerSocket _serverSocket;
	private final static Logger LOG = Logger.getLogger( NRiderServer.class );
	private List<ClientHandler> _clientHandlers = new ArrayList<ClientHandler>();

	public NRiderServer( int port )
	{
		_port = port;
	}

	public String getIdentifier()
	{
		return "NRiderServer";
	}

	public void handlePerformanceData( String identifier, PerformanceData data )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handlePerformanceData( identifier, data );
		}
	}

	public void handleLoadAdjust( String riderId, RideLoad newLoad )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleAddRider( Rider rider )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleAddRider( rider );
		}
	}

	public void handleRiderThresholdAdjust( String riderId, double newThreshold )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleRiderThresholdAdjust( riderId, newThreshold );
		}
	}

	public void handleRideLoaded( IRide ride )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleRideLoaded( ride );
		}
	}

	public void handleRideTimeUpdate( long rideTime )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleRideTimeUpdate( rideTime );
		}
	}

	public void handleAddRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleAddRiderAlert( riderId, type );
		}
	}

	public void handleRemoveRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleRemoveRiderAlert( riderId, type );
		}
	}

	public void handleRideStatusUpdate( IRide.Status status )
	{
		for( ClientHandler clientHandler : _clientHandlers )
		{
			clientHandler.handleRideStatusUpdate( status );
		}
	}

	public void start() throws IOException
	{
		if( _serverSocket == null )
		{
			_serverSocket = new ServerSocket( _port );
			new Thread( new AcceptThread( _serverSocket ) ).start();
		}
	}

	public void shutdown()
	{
		if( _serverSocket != null && !_serverSocket.isClosed() )
		{
			try
			{
				_serverSocket.close();
			}
			catch( IOException e )
			{
				LOG.error( "Error shutting down server", e );
			}
		}
	}

	protected void addHandler( ClientHandler handler )
	{
		synchronized( _clientHandlers )
		{
			_clientHandlers.add( handler );
		}
	}

	protected void removeHandler( ClientHandler handler )
	{
		synchronized( _clientHandlers )
		{
			_clientHandlers.remove( handler );
		}
	}


	public class AcceptThread implements Runnable
	{
		private ServerSocket _serverSocket;

		public AcceptThread( ServerSocket serverSocket )
		{
			_serverSocket = serverSocket;
		}

		public void run()
		{
			try
			{
				while( true )
				{
					Socket socket = _serverSocket.accept();
					ClientHandler handler = new ClientHandler( socket );
					new Thread( handler ).start();
					addHandler( handler );
				}
			}
			catch( IOException e )
			{
				LOG.error( e );
				shutdown();
			}

		}
	}

	public class ClientHandler implements Runnable, IPerformanceDataListener, IWorkoutListener
	{
		private Socket _socket;
		private HashSet<String> _identifiers = new HashSet<String>();
		private PrintWriter _writer;
		private MessageSerializer _serializer;

		public ClientHandler( Socket socket )
		{
			_socket = socket;
		}

		public void run()
		{
			BufferedReader reader = null;
			InputStream input = null;
			OutputStream output = null;
			try
			{
				input = _socket.getInputStream();
				output = _socket.getOutputStream();
				String id = _socket.getInetAddress().getHostAddress() + ":" + _socket.getPort();

				_writer = new PrintWriter( output );
				_serializer = new MessageSerializer( _writer );
				
				reader = new BufferedReader( new InputStreamReader( input ) );
				while( true )
				{
					String line = reader.readLine();

					handleClientData( id, line, _writer );

				}
			}
			catch( IOException e )
			{
				LOG.error( "Client read error", e );
			}
			finally
			{
				if( reader != null )
				{
					try
					{
						reader.close();
					}
					catch( IOException e )
					{
					}
				}

				if( _writer != null )
				{
					_writer.close();
				}

				try
				{
					_socket.close();
				}
				catch( IOException e )
				{

				}

			}
		}

		private void handleClientData( String id, String line, PrintWriter writer )
		{
			LOG.info( "recieved:" + line );
			String[] data = line.split( ":" );
			if( data.length > 0 )
			{
				MessageSerializer.MessageType type = MessageSerializer.MessageType.valueOf( data[0] );
				switch( type )
				{
					case PERFORMANCE_DATA:
						final String identifier = data[1];
						final PerformanceData pData = new PerformanceData( PerformanceData.Type.valueOf( data[2] ), Float.valueOf( data[3] ) );
						if( ! _identifiers.contains( identifier ) )
						{
							_identifiers.add( identifier );
						}
						getDataPublisher().publishEvent( new IEvent<IPerformanceDataListener>()
						{
							public void trigger( IPerformanceDataListener target )
							{
								target.handlePerformanceData( identifier, pData );
							}
						});
						break;
					case RIDER_THRESHOLD_ADJUST:
						WorkoutSession.instance().setRiderThreshold( data[1], Integer.valueOf( data[2] ) );
						break;
				}
			}
		}

		public void handlePerformanceData( String identifier, PerformanceData data )
		{
			if( !_identifiers.contains( identifier ) )
			{
				_serializer.sendPerformanceData( identifier, data );
			}
		}

		public void handleLoadAdjust( String riderId, RideLoad newLoad )
		{
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void handleAddRider( Rider rider )
		{
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void handleRiderThresholdAdjust( String riderId, double newThreshold )
		{
			if( !_identifiers.contains( riderId ) )
			{
				_serializer.sendRiderThresholdAdjust( riderId, newThreshold );
			}
		}

		public void handleRideLoaded( IRide ride )
		{
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void handleRideTimeUpdate( long rideTime )
		{
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void handleAddRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
		{
			if( !_identifiers.contains( riderId ) )
			{
				_serializer.sendAddRiderAlert( riderId, type );
			}
		}

		public void handleRemoveRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
		{
			if( !_identifiers.contains( riderId ) )
			{
				_serializer.sendRemoveRiderAlert( riderId, type );
			}
		}

		public void handleRideStatusUpdate( IRide.Status status )
		{
			_serializer.sendRideStatusUpdate( status );
		}

	}
}
