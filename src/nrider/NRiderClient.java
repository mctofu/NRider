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
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import nrider.ride.IRide;
import nrider.ui.RideScriptView;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 8:06:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class NRiderClient implements IPerformanceDataListener, IWorkoutListener
{
	private static Font _bigAFont = new Font( "Serif", Font.PLAIN, 30 );
	private JFrame _window;
	private HashMap<String,RiderView> _riderMap = new HashMap<String, RiderView>();
	private Container _riderContainer;
	private JLabel _workoutLoad;
	private RideScriptView _rideScriptView;

	public void start()
	{
		// TODO: Handle multiple riders
		_window = new JFrame();
		_window.setSize(500, 500);

		Container content = _window.getContentPane();
		content.setLayout( new BoxLayout( content, BoxLayout.Y_AXIS ) );
		_workoutLoad = CreateLabel("Workout Load:");
		content.add( _workoutLoad );
		_rideScriptView = new RideScriptView();
		content.add( _rideScriptView );

		_riderContainer = new Box( BoxLayout.X_AXIS );
		content.add( _riderContainer );
		_window.setVisible(true);
	}

	public void handleRideLoad( IRide ride )
	{
		_rideScriptView.setRideScript( ride.getScript() );
	}

	public void handlePerformanceData( String identifier, PerformanceData data )
	{
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
		}
	}

	public void handleLoadAdjust( String riderId, RideLoad newLoad )
	{
		_workoutLoad.setText( "Workout Load:" + newLoad.toString() );
	}

	public void handleAddRider( Rider rider )
	{
		RiderView riderView = new RiderView( rider );
		_riderContainer.add( riderView.getContainer() );
		_riderMap.put( rider.getIdentifier(), riderView );
		_window.getRootPane().revalidate();
	}

	public void handleRiderThresholdAdjust( String identifier, double newThreshold )
	{
		_riderMap.get( identifier ).setThreshold( newThreshold );
	}

	private JLabel CreateLabel( String text )
	{
		JLabel label = new JLabel(text);
		label.setFont( _bigAFont );
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



		public RiderView( Rider rider )
		{
			_name =CreateLabel("Name:" + rider.getName() );
			_container.add( _name );
			_riderThreshold = CreateLabel("Threshold:" + rider.getThresholdPower());
			_container.add( _riderThreshold );
			_speed = CreateLabel("Speed:");
			_container.add(_speed);
			_cadence = CreateLabel("Cadence:");
			_container.add(_cadence);
			_power = CreateLabel( "Power:");
			_container.add(_power);
			_extHeartRate = CreateLabel( "Ext HR:");
			_container.add(_extHeartRate);
			_extCadence = CreateLabel( "Ext Cadence:");
			_container.add(_extCadence);
			_extPower = CreateLabel( "Ext Power:");
			_container.add(_extPower);
		}

		public Container getContainer()
		{
			return _container;
		}

		public void setSpeed( float speed )
		{
			_speed.setText( "Speed:" + speed );
		}

		public void setCadence( float cadence )
		{
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
			_extHeartRate.setText( "Ext HR:" + hr );
		}

		public void setExtCadence( float cadence )
		{
			_extCadence.setText( "Ext Cadence:" + cadence );
		}

		public void setExtPower( float power )
		{
			_extPower.setText( "Ext Power:" + power );
		}

	}

}
