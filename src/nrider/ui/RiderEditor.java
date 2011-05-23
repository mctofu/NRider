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

import nrider.core.Rider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**

 */
public class RiderEditor implements ActionListener
{
	private JFrame _window;
	private JTextField _id;
	private JTextField _threshold;
	private RiderManager _riderManager;

	public RiderEditor( RiderManager riderManager )
	{
		_riderManager = riderManager;
	}

	public void launch()
	{
		launch( new Rider() );
	}


	public void launch( Rider rider )
	{
		init( rider );
	}

	private void init( Rider rider )
	{
		_window = new JFrame();
		_window.setSize(500, 600);

		Container content = _window.getContentPane();
		content.setLayout( new GridLayout( 3, 2 ) );

		JLabel idLabel = new JLabel( "Id" );
		content.add( idLabel );

		_id = new JTextField( rider.getIdentifier() );
		content.add( _id );

		JLabel thresholdLabel = new JLabel( "Threshold" );
		content.add( thresholdLabel );

		_threshold = new JTextField( rider.getThresholdPower() );
		content.add( _threshold );

		JButton actionButton = new JButton( "Create" );
		actionButton.addActionListener( this );
		content.add( actionButton );

		_window.setVisible( true );
	}

	public void actionPerformed( ActionEvent e )
	{
		Rider rider = new Rider();
		rider.setName( _id.getText() );
		rider.setThresholdPower( Integer.parseInt( _threshold.getText() ) );
		_riderManager.handleCreateRider( rider );
		_window.dispose();
	}
}
