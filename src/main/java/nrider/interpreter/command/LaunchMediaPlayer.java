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
import nrider.ui.MediaPlayerView;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Feb 18, 2010
 * Time: 9:46:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class LaunchMediaPlayer extends BaseCommand
{
	@Override
	public String run( String[] args ) throws Exception
	{
		MediaPlayerView mpv = new MediaPlayerView();
		mpv.launch();
		WorkoutSession.instance().addMediaEventListner( mpv );
		WorkoutSession.instance().addWorkoutListener( mpv );

		return null;
	}

	public String getDescription()
	{
		return "Open media player window";
	}
}
