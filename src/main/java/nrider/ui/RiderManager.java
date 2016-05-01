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
import nrider.db.FileRiderDb;
import nrider.db.IRiderDb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**

 */
public class RiderManager implements ActionListener
{
	private JFrame _window;
	private JComboBox _allRiders;
	private DefaultComboBoxModel _allRiderModel = new DefaultComboBoxModel();
	private IRiderDb _riderDb = new FileRiderDb();

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

		JPanel riderDbControls = new JPanel();
		riderDbControls.setLayout( new BoxLayout( riderDbControls, BoxLayout.X_AXIS ) );

		populateAllRiders();

		_allRiders = new JComboBox( _allRiderModel );
		riderDbControls.add( _allRiders );
//		riderDbControls.setMaximumSize( riderDbControls.getPreferredSize() );

		JButton addToWorkout = new JButton( "Add to workout" );
		addToWorkout.setActionCommand( "addToWorkout" );
		addToWorkout.addActionListener( this );
		riderDbControls.add( addToWorkout );
		JButton createRider = new JButton( "Create new rider" );
		createRider.setActionCommand( "createRider" );
		createRider.addActionListener( this );
		riderDbControls.add( createRider );

		content.add( riderDbControls );

		_window.setVisible(true);
	}

	private void populateAllRiders()
	{
		_allRiderModel.removeAllElements();
		for( String riderId : _riderDb.getRiderIds() )
		{
			_allRiderModel.addElement( riderId );
		}
	}

	public void actionPerformed( ActionEvent e )
	{
		if( e.getActionCommand().equals( "addToWorkout" ) )
		{

		}
		else if( e.getActionCommand().equals( "createRider" ) )
		{
			new RiderEditor( this ).launch();
		}
	}

	public void handleCreateRider( Rider rider )
	{
		_riderDb.addRider( rider );
		populateAllRiders();
	}
}
