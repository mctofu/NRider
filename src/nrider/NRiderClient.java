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
package nrider;

import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import nrider.ride.IRide;
import nrider.ui.PerformanceStatView;
import nrider.ui.RecentPerformanceView;
import nrider.ui.RideScriptView;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Really basic UI for debugging.
 */
public class NRiderClient implements IPerformanceDataListener, IWorkoutListener
{
	private static Font _bigAFont = new Font( "Serif", Font.PLAIN, 30 );
	private JFrame _window;
	private RiderListView _riderListView;
	private JLabel _workoutLoad;
	private RideScriptView _rideScriptView;
	private JLabel _rideTime;

	public void start()
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				init();
			}
		});
	}

	private void init()
	{
		_window = new JFrame();
		_window.setSize(500, 600);

		Container content = _window.getContentPane();
		content.setLayout( new BoxLayout( content, BoxLayout.Y_AXIS ) );
		_rideTime = CreateLabel("00:00:00");
		content.add( _rideTime );
		_workoutLoad = CreateLabel("Workout Load:");
		content.add( _workoutLoad );
		_rideScriptView = new RideScriptView();
		content.add( _rideScriptView );

		_riderListView = new RiderListView();
		content.add( _riderListView.getContainer() );
		_window.setVisible(true);
	}

	public void handleRideLoaded( final IRide ride )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_rideScriptView.setRideScript( ride.getScript() );
			}
		});
	}

	public void handlePerformanceData( final String identifier, final PerformanceData data )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderListView.handlePerformanceData( identifier, data );
			}
		});
	}

	public void handleLoadAdjust( final String riderId, final RideLoad newLoad )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_workoutLoad.setText( "Workout Load:" + newLoad.toString() );
			}
		});
	}

	public void handleAddRider( final Rider rider )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderListView.addRider( rider );
				_window.getRootPane().revalidate();
			}
		});
	}

	public void handleRiderThresholdAdjust( final String identifier, final double newThreshold )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderListView.handleRiderThresholdAdjust( identifier, newThreshold );
			}
		});
	}

	private void setRideTime( long time )
	{
		DecimalFormat format = new DecimalFormat( "00" );
		long totalSeconds = time / 1000;
		String seconds = format.format( (int)(totalSeconds % 60));
		String minutes = format.format((int)((totalSeconds % 3600) / 60));
		String hours = format.format((int)(totalSeconds / 3600) );

		_rideTime.setText( hours + ":" + minutes + ":" + seconds );
	}

	public void handleRideTimeUpdate( final long rideTime )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				setRideTime( rideTime );
				_rideScriptView.setRideTime( rideTime );
			}
		});
	}

	public void handleRideStatusUpdate( IRide.Status status )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleAddRiderAlert( final String identifier, final WorkoutSession.RiderAlertType alert )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderListView.handleAddRiderAlert( identifier, alert );
			}
		});
	}

	public void handleRemoveRiderAlert( final String identifier, final WorkoutSession.RiderAlertType alert )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderListView.handleRemoveRiderAlert( identifier, alert );
			}
		});
	}

	private JLabel CreateLabel( String text )
	{
		return CreateLabel( text, true );
	}

	private JLabel CreateLabel( String text, boolean visible )
	{
		JLabel label = new JLabel(text);
		label.setFont( _bigAFont );
		label.setVisible( visible );
		return label;
	}

	class RiderListView
	{
		private Container _container = new JPanel( new GridBagLayout() );
		private HashMap<String,RiderView> _riderMap = new HashMap<String, RiderView>();
		private JLabel _riderName;
		private JLabel _threshold;
		private JLabel _alert;
		private JLabel _speed;
		private JLabel _cadence;
		private JLabel _power;
		private JLabel _extHr;
		private JLabel _extCadence;
		private JLabel _extPower;
		private JLabel _calibration;

		public RiderListView()
		{
			_container.setPreferredSize( new Dimension( 800, 200 ) );
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			_riderName = CreateLabel( "Name" );
			c.gridy = 0;
			_container.add( _riderName, c );

			_threshold = CreateLabel( "Threshold" );
			c.gridy = 1;
			_container.add( _threshold, c );

			_alert = CreateLabel( "" );
			c.gridy = 2;
			_container.add( _alert, c );

			_speed = CreateLabel( "Speed" );
			c.gridy = 3;
			_container.add( _speed, c );

			_cadence = CreateLabel( "Cadence", false );
			c.gridy = 4;
			_container.add( _cadence, c );

			_power = CreateLabel( "Power" );
			c.gridy = 5;
			_container.add( _power, c );

			_extHr = CreateLabel( "Ext HR", false );
			c.gridy = 6;
			_container.add( _extHr, c );

			_extCadence = CreateLabel( "Ext Cadence", false );
			c.gridy = 7;
			_container.add( _extCadence, c );

			_extPower = CreateLabel( "Ext Power", false );
			c.gridy = 8;
			_container.add( _extPower, c );

			_calibration = CreateLabel( "Calibration" );
			c.gridy = 9;
			_container.add( _calibration, c );
		}

		public Container getContainer()
		{
			return _container;
		}

		public void addRider( Rider rider )
		{
			RiderView riderView = new RiderView( rider, _container, _riderMap.size() * 2 + 1 );
			_riderMap.put( rider.getIdentifier(), riderView );
		}

		public void handlePerformanceData( String identifier, final PerformanceData data )
		{
			RiderView riderView = _riderMap.get( identifier );
			switch( data.getType() )
			{
				case POWER:
					riderView.setPower( data.getValue() );
					break;
				case CADENCE:
					_cadence.setVisible( true );
					riderView.setCadence( data.getValue() );
					break;
				case SPEED:
					// convert m/s to mph
					riderView.setSpeed( (float) ( data.getValue() * 2.237 ) );
					break;
				case EXT_HEART_RATE:
					_extHr.setVisible( true );
					riderView.setExtHeartRate( data.getValue() );
					break;
				case EXT_CADENCE:
					_extCadence.setVisible( true );
					riderView.setExtCadence( data.getValue() );
					break;
				case EXT_POWER:
					_extPower.setVisible( true );
					riderView.setExtPower( data.getValue() );
					break;
				case CALIBRATION:
					riderView.setCalibration( data.getValue() );
					break;
			}
		}

		public void handleAddRiderAlert( String identifier, WorkoutSession.RiderAlertType alert )
		{
			_riderMap.get( identifier ).addAlert( alert );
		}

		public void handleRemoveRiderAlert( String identifier, WorkoutSession.RiderAlertType alert )
		{
			_riderMap.get( identifier ).removeAlert( alert );
		}

		public void handleRiderThresholdAdjust( String identifier, double newThreshold )
		{
			_riderMap.get( identifier ).setThreshold( newThreshold );
		}
	}

	class RiderView
	{
		private Container _container;
		private JLabel _name;
		private PerformanceStatView _speed;
		private PerformanceStatView _cadence;
		private PerformanceStatView _power;
		private JLabel _riderThreshold;
		private PerformanceStatView _extHeartRate;
		private PerformanceStatView _extCadence;
		private PerformanceStatView _extPower;
		private JLabel _calibration;
		private JLabel _alert;
		private HashSet<WorkoutSession.RiderAlertType> _alerts = new HashSet<WorkoutSession.RiderAlertType>( );
		private Rider _rider;

		public RiderView( Rider rider, Container container, int columnNumber )
		{
			_container = container;
			GridBagConstraints c = new GridBagConstraints( );
			c.ipadx = 20;
			c.gridx = columnNumber;

			_name = CreateLabel( rider.getName() );
			c.gridy = 0;
			c.gridwidth = 2;
			_container.add( _name, c );

			_riderThreshold = CreateLabel( rider.getThresholdPower() + "" );
			c.gridy = 1;
			_container.add( _riderThreshold, c );

			_alert = CreateLabel( "", false );
			_alert.setBackground( Color.YELLOW );
			_alert.setForeground( Color.ORANGE );
			c.gridy = 2;
			_container.add(_alert, c);

			c.gridwidth = 1;

			_speed = new PerformanceStatView( 0, 30, 60, new DecimalFormat( "0.0" ) );
			c.gridy = 3;
			addPerformanceStatView( _speed, c );

			_cadence = new PerformanceStatView(  0, 150, 60, new DecimalFormat( "0" ) );
			c.gridy = 4;
			_cadence.setVisible( false );
			addPerformanceStatView( _cadence, c );

			_power = new PerformanceStatView( 0, rider.getThresholdPower() * 1.1, 60, new DecimalFormat( "0" ) );
			c.gridy = 5;
			addPerformanceStatView( _power, c );

			_extHeartRate = new PerformanceStatView( 0, 220, 60, new DecimalFormat( "0" ) );
			c.gridy = 6;
			_extHeartRate.setVisible( false );
			addPerformanceStatView( _extHeartRate, c );

			_extCadence = new PerformanceStatView( 0, 150, 60, new DecimalFormat( "0" ) );
			c.gridy = 7;
			_extCadence.setVisible( false );
			addPerformanceStatView( _extCadence, c );

			_extPower = new PerformanceStatView( 0, rider.getThresholdPower() * 1.1, 60, new DecimalFormat( "0" ) );
			c.gridy = 8;
			_extPower.setVisible( false );
			addPerformanceStatView( _extPower, c );

			_calibration = CreateLabel( "", false );
			c.gridy = 9;
			c.gridwidth = 2;
			_container.add( _calibration, c );

			_rider = rider;

		}

		private void addPerformanceStatView( PerformanceStatView perf, GridBagConstraints c )
		{
			int gridX = c.gridx;
			_container.add( perf.getLabel(), c );
			c.gridx = gridX + 1;
			_container.add( perf.getGraph(), c );
			c.gridx = gridX;


		}

		public Container getContainer()
		{
			return _container;
		}

		public void setSpeed( float speed )
		{
			RecentPerformanceView.DataType type;
			if( speed > 25 )
			{
				type = RecentPerformanceView.DataType.EXTREME;
			}
			else if( speed < 19 )
			{
				type = RecentPerformanceView.DataType.WARNING;
			}
			else
			{
				type = RecentPerformanceView.DataType.NORMAL;
			}
			_speed.updateValue( speed, type );
		}

		public void setCadence( float cadence )
		{
			_cadence.setVisible( true );
			_cadence.updateValue( cadence, RecentPerformanceView.DataType.NORMAL );
		}

		public void setPower( float power )
		{
			RecentPerformanceView.DataType type;
			if( power > _rider.getThresholdPower() * 1.1 )
			{
				type = RecentPerformanceView.DataType.EXTREME;
			}
			else if( power > _rider.getThresholdPower() * .9 )
			{
				type = RecentPerformanceView.DataType.WARNING;
			}
			else
			{
				type = RecentPerformanceView.DataType.NORMAL;
			}
			_power.updateValue( power, type );
		}

		public void setThreshold( double threshold )
		{
			_riderThreshold.setText( new DecimalFormat( "0" ).format( threshold ) );
		}

		public void setExtHeartRate( float hr )
		{
			_extHeartRate.setVisible( true );
			_extHeartRate.updateValue(  hr, RecentPerformanceView.DataType.NORMAL );
		}

		public void setExtCadence( float cadence )
		{
			_extCadence.setVisible( true );
			_extCadence.updateValue( cadence, RecentPerformanceView.DataType.NORMAL );
		}

		public void setExtPower( float power )
		{
			_extPower.setVisible( true );
			_extPower.updateValue( power, RecentPerformanceView.DataType.NORMAL );
		}

		public void setCalibration( float calibration )
		{
			_calibration.setVisible( true );
			_calibration.setText( new DecimalFormat( "0.00" ).format( calibration ) );
		}

		public void addAlert( WorkoutSession.RiderAlertType type )
		{
			_alerts.add( type );

			renderAlert();
		}


		public void removeAlert( WorkoutSession.RiderAlertType type )
		{
			_alerts.remove( type );

			renderAlert();
		}
		private void renderAlert()
		{
			StringBuilder sb = new StringBuilder();
			for( WorkoutSession.RiderAlertType alert : _alerts )
			{
				sb.append( alert.getShortName() );
				sb.append( " " );
			}
			_alert.setText( sb.toString() );

			_alert.setVisible( _alerts.size() > 0 );
		}
	}
}
