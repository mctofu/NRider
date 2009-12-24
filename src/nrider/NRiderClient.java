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
	private HashMap<String,RiderView> _riderMap = new HashMap<String, RiderView>();
	private Container _riderContainer;
	private JLabel _workoutLoad;
	private RideScriptView _rideScriptView;
	private JLabel _rideTime;
	public void start()
	{
		// TODO: Handle multiple riders
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

		_riderContainer = new Box( BoxLayout.X_AXIS );
		content.add( _riderContainer );
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

				// TODO: Assign to right rider based on identifier
				RiderView riderView = _riderMap.get( identifier );
				switch( data.getType() )
				{
					case POWER:
						riderView.setPower( data.getValue() );
						break;
					case CADENCE:
						riderView.setCadence( data.getValue() );
						break;
					case SPEED:
						// convert m/s to mph
						riderView.setSpeed( (float) ( data.getValue() * 2.237 ) );
						break;
					case EXT_HEART_RATE:
						riderView.setExtHeartRate( data.getValue() );
						break;
					case EXT_CADENCE:
						riderView.setExtCadence( data.getValue() );
						break;
					case EXT_POWER:
						riderView.setExtPower( data.getValue() );
						break;
					case CALIBRATION:
						riderView.setCalibration( data.getValue() );
						break;
				}
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
				RiderView riderView = new RiderView( rider );
				_riderContainer.add( riderView.getContainer() );
				_riderMap.put( rider.getIdentifier(), riderView );
				_window.getRootPane().revalidate();
			}
		});
	}

	public void handleRiderThresholdAdjust( final String identifier, final double newThreshold )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderMap.get( identifier ).setThreshold( newThreshold );
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

	public void handleRiderDistanceUpdate( String riderId, double distance )
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void handleAddRiderAlert( final String identifier, final WorkoutSession.RiderAlertType alert )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderMap.get( identifier ).addAlert( alert );
			}
		});
	}

	public void handleRemoveRiderAlert( final String identifier, final WorkoutSession.RiderAlertType alert )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_riderMap.get( identifier ).removeAlert( alert );
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

	class RiderView
	{
		private Container _container = new Box( BoxLayout.Y_AXIS );
		private JLabel _name;
		private JLabel _speed;
		private JLabel _cadence;
		private JLabel _power;
		private JLabel _riderThreshold;
		private JLabel _extHeartRate;
		private JLabel _extCadence;
		private JLabel _extPower;
		private JLabel _calibration;
		private JLabel _alert;
		private HashSet<WorkoutSession.RiderAlertType> _alerts = new HashSet<WorkoutSession.RiderAlertType>( );

		public RiderView( Rider rider )
		{
			_name =CreateLabel("Name:" + rider.getName() );
			_container.add( _name );
			_riderThreshold = CreateLabel("Threshold:" + rider.getThresholdPower());
			_container.add( _riderThreshold );
			_alert = CreateLabel( "Alert", false );
			_alert.setBackground( Color.yellow );
			_container.add(_alert);
			_speed = CreateLabel("Speed:");
			_container.add(_speed);
			_cadence = CreateLabel("Cadence:", false );
			_container.add(_cadence);
			_power = CreateLabel( "Power:");
			_container.add(_power);
			_extHeartRate = CreateLabel( "Ext HR:", false );
			_container.add(_extHeartRate);
			_extCadence = CreateLabel( "Ext Cadence:", false );
			_container.add(_extCadence);
			_extPower = CreateLabel( "Ext Power:", false );
			_container.add(_extPower);
			_calibration = CreateLabel( "Calibration:", false );
			_container.add(_calibration);

		}

		public Container getContainer()
		{
			return _container;
		}

		public void setSpeed( float speed )
		{
			_speed.setVisible( true );
			_speed.setText( "Speed:" + speed );
		}

		public void setCadence( float cadence )
		{
			_cadence.setVisible( true );
			_cadence.setText( "Cadence:" + cadence );
		}

		public void setPower( float power )
		{
			_power.setText( "Power:" + power );
		}

		public void setThreshold( double threshold )
		{
			_riderThreshold.setText( "Threshold:" + threshold );
		}

		public void setExtHeartRate( float hr )
		{
			_extHeartRate.setVisible( true );
			_extHeartRate.setText( "Ext HR:" + hr );
		}

		public void setExtCadence( float cadence )
		{
			_extCadence.setVisible( true );
			_extCadence.setText( "Ext Cadence:" + cadence );
		}

		public void setExtPower( float power )
		{
			_extPower.setVisible( true );
			_extPower.setText( "Ext Power:" + power );
		}

		public void setCalibration( float calibration )
		{
			_calibration.setVisible( true );
			_calibration.setText( "Calibration: " + calibration );
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
			StringBuilder sb = new StringBuilder( "Alert:");
			for( WorkoutSession.RiderAlertType alert : _alerts )
			{
				sb.append( alert.toString() );
				sb.append( " " );
			}
			_alert.setText( sb.toString() );

			_alert.setVisible( _alerts.size() > 0 );
		}


	}
}
