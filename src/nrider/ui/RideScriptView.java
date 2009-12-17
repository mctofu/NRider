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
package nrider.ui;

import nrider.ride.RideEvent;
import nrider.ride.RideScript;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 *
 */
public class RideScriptView extends JComponent
{
	private RideScript _rideScript;
	private int _indicatorX;
	private double _posScale;

	public RideScriptView()
	{
		setMinimumSize( new Dimension( 600, 100 ) );
		setBorder( new BevelBorder( BevelBorder.LOWERED ) );
	}

	public void setRideScript( RideScript rideScript )
	{
		_rideScript = rideScript;
		repaint();
	}

	@Override
	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );

		if( _rideScript != null )
		{
			Rectangle r = g.getClipBounds();

			double maxLoad = _rideScript.getMaxLoad();
			double loadTrim = 0;
			if( maxLoad > r.getHeight() )
			{
				loadTrim = maxLoad * .1;
			}

			double loadScale = r.height / ( maxLoad - loadTrim );

			_posScale = r.width / (double) _rideScript.getPeriod();

			int lastX = 0, lastY = r.height;
			for( RideEvent re : _rideScript )
			{
				int nextX = (int) ( re.getPosition() * _posScale );
				int nextY = r.height - (int) ( ( re.getLoad().getValue() - loadTrim ) * loadScale );

				g.drawLine( lastX, lastY, nextX, nextY );
				lastX = nextX;
				lastY = nextY;
			}
			g.setXORMode( Color.GREEN );
			g.fillRect( _indicatorX, 0, 3, r.height );
			g.setPaintMode();

		}
	}

	public void setRideTime( long rideTime )
	{
		int nextIndicatorX = (int) ( rideTime * _posScale );
		if( _indicatorX != nextIndicatorX )
		{
		 	_indicatorX = nextIndicatorX;
			repaint();
		}
	}
}
