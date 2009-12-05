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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class DebugAppender extends AppenderSkeleton
{
	private JFrame _window;
	private JTextArea _text;
	private AtomicBoolean _init = new AtomicBoolean( false );

	public DebugAppender()
	{
		
		super();
	}

	public void close()
	{

	}

	public boolean requiresLayout()
	{
		return true;
	}

	private void init()
	{
		_window = new JFrame();
		_window.setTitle( getName() );
		_window.setSize(500, 500);
		_window.setLocation( 500, 0 );

		_text = new JTextArea();
		_text.setLineWrap( true );
		DefaultCaret caret = (DefaultCaret)_text.getCaret();
		caret.setUpdatePolicy( DefaultCaret.ALWAYS_UPDATE);
		_window.getContentPane().add( new JScrollPane( _text ) );
		_window.setVisible(true);
		_init.set( true );
	}

	@Override
	protected void append( LoggingEvent loggingEvent )
	{
		if( !_init.get() )
		{
			init();
		}
		_text.append( loggingEvent.getRenderedMessage() + "\n" );
	}
}
