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
package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.media.MediaEvent;
import nrider.ui.MediaPlayerView;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Feb 21, 2010
 * Time: 1:15:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class LaunchVideo extends BaseCommand
{
	@Override
	public String run( String[] args ) throws Exception
	{
		MediaEvent meLoad = new MediaEvent( MediaEvent.Type.LOAD, args[0], 0, null );
		WorkoutSession.instance().handleMediaEvent( meLoad );
		if( args.length > 1 )
		{
			MediaEvent meSeek = new MediaEvent( MediaEvent.Type.SEEK, args[0], Integer.parseInt( args[1] ), null );
			WorkoutSession.instance().handleMediaEvent( meSeek );
		}
//		MediaEvent mePlay = new MediaEvent( MediaEvent.Type.PLAY, args[0], 0, null );
//		WorkoutSession.instance().handleMediaEvent( mePlay );
		return null;
	}

	public String getDescription()
	{
		return "Start a video";
	}
}
