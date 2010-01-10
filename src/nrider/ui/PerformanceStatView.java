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

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 */
public class PerformanceStatView
{
	private RecentPerformanceView _performanceGraphView;
	private JLabel _value;
	private long _initialTime;
	private NumberFormat _numberFormat;


	public PerformanceStatView( double minY, double maxY, double window, NumberFormat numberFormat )
	{
		_performanceGraphView = new RecentPerformanceView( window, minY, maxY );
		_performanceGraphView.setMinimumSize( new Dimension( 50, 25 ) );
		_performanceGraphView.setPreferredSize( new Dimension( 50, 25 ) );

		_value = new JLabel( "", SwingConstants.RIGHT );
		_value.setPreferredSize( new Dimension( 50,25 ) );
		_value.setFont( new Font( "Serif", Font.PLAIN, 30 ) );
		_initialTime = System.currentTimeMillis();
		_numberFormat = numberFormat;
	}

	public Container getLabel()
	{
		return _value;
	}

	public void setVisible( boolean visible )
	{
		_value.setVisible( visible );
		_performanceGraphView.setVisible( visible );
	}


	public Container getGraph()
	{
		return _performanceGraphView;
	}

	public void updateValue( double value, RecentPerformanceView.DataType type )
	{
		_performanceGraphView.addData( ( System.currentTimeMillis() - _initialTime ) / 1000, value, type );
		_value.setText( _numberFormat.format( value ));
	}
}
