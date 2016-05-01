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

import nrider.core.RideLoad;
import nrider.ride.RideEvent;
import nrider.ride.RideScript;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 */
public class RideScriptView extends GraphView
{
	private RideScript _rideScript;

	public RideScriptView()
	{
		setMinimumSize( new Dimension( 600, 100 ) );
		setPreferredSize( new Dimension( 600, 100 ) );
		setBorder( new BevelBorder( BevelBorder.LOWERED ) );
	}

	public void setRideScript( RideScript rideScript )
	{
		_rideScript = rideScript;
		ArrayList<GraphPoint> graph = new ArrayList<GraphPoint>( );
		for( RideEvent re : _rideScript )
		{
			graph.add( new GraphPoint( re.getPosition(), re.getLoad().getValue(), getColor( re.getLoad() ) ) );
		}

		setMaxY( _rideScript.getMaxLoad() );
		setMaxX( _rideScript.getPeriod() );
		setShowXIndicator( true );
		setGraph( graph );
		repaint();
	}

	private Color getColor( RideLoad load )
	{
		return new Color( 211, 211, 211 );
	}

	public void setRideTime( long rideTime )
	{
		setXIndicator( rideTime );
	}
}
