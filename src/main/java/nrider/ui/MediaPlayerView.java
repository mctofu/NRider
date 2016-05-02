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

import com.sun.jna.NativeLibrary;
import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.media.IMediaEventListener;
import nrider.media.MediaEvent;
import nrider.ride.IRide;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.*;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Use jmc classes from javafx to render video
 */
public class MediaPlayerView implements IMediaEventListener, IWorkoutListener
{
	private final static Logger LOG = Logger.getLogger( MediaPlayerView.class );

	private JFrame _window;
	private JPanel _mediaPanel;
	private EmbeddedMediaPlayerComponent _mediaPlayerComponent;
	private int _seekTo;
	private boolean _startedPlaying;

	public void launch( final String vlcPath )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				init( vlcPath );
			}
		});
	}

	private void init( String vlcPath )
	{
		_window = new JFrame();
		_mediaPanel = new JPanel();
		_mediaPanel.setLayout(new BorderLayout());

		_window.add(_mediaPanel, BorderLayout.CENTER);

		_window.setSize( 640, 480 );

		_window.pack();
		_window.setLocationRelativeTo(null);
		_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		if( vlcPath != null )
		{
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
		}

		boolean found = new NativeDiscovery().discover();
		System.out.println(found);
		System.out.println(LibVlc.INSTANCE.libvlc_get_version());

		_mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		_mediaPanel.add(_mediaPlayerComponent);

		_window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				_mediaPlayerComponent.release();
			}
		});

		_window.setVisible( true );

	}

	public void handleMediaEvent( final MediaEvent me )
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				doHandleMediaEvent( me );
			}
		});
	}

	private void doHandleMediaEvent( MediaEvent me )
	{
		switch( me.getType() )
		{
			case LOAD:
				loadMedia( me.getMedia() );
				break;
			case PLAY:
				play();
				break;
			case SEEK:
				if( !_startedPlaying )
				{
					_seekTo = me.getPosition() * 1000;
				}
				else
				{
					_mediaPlayerComponent.getMediaPlayer().setTime(me.getPosition() * 1000);
				}
				break;
			case PAUSE:
				_mediaPlayerComponent.getMediaPlayer().pause();
				break;
		}
	}

	private void play() {
		_mediaPlayerComponent.getMediaPlayer().play();
	}

	public void handleLoadAdjust( String riderId, RideLoad newLoad )
	{
	}

	public void handleAddRider( Rider rider )
	{
	}

	public void handleRiderThresholdAdjust( String riderId, double newThreshold )
	{
	}

	public void handleRideLoaded( IRide ride )
	{
	}

	public void handleRideTimeUpdate( long rideTime )
	{
	}

	public void handleAddRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
	{
	}

	public void handleRemoveRiderAlert( String riderId, WorkoutSession.RiderAlertType type )
	{
	}

	public void handleRideStatusUpdate( final IRide.Status status )
	{
		SwingUtilities.invokeLater(
			new Runnable()
			{
				public void run()
				{
					doHandleRideStatusUpdate( status );
				}
			});
	}

	private void doHandleRideStatusUpdate( IRide.Status status )
	{
		switch( status )
		{
			case RUNNING:
				play();
				break;
			case STOPPED:
			case PAUSED:
				_mediaPlayerComponent.getMediaPlayer().pause();
				break;
		}
	}

	private void loadMedia(String path) {
		_mediaPlayerComponent.getMediaPlayer().pause();

		_mediaPlayerComponent.getMediaPlayer().prepareMedia(path);
		_mediaPlayerComponent.getMediaPlayer().parseMedia();

		for (TrackInfo track : _mediaPlayerComponent.getMediaPlayer().getTrackInfo( TrackType.VIDEO )) {
			VideoTrackInfo videoTrack = (VideoTrackInfo) track;

			int width = videoTrack.width();
			int height = videoTrack.height();
			_mediaPanel.setPreferredSize(new Dimension(width,
					height));
			_window.pack();
			_window.setLocationRelativeTo(null);
			break;
		}

		_mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter()
		{

			@Override
			public void playing(MediaPlayer mediaPlayer) {
				_startedPlaying = true;
				if (_seekTo > 0)
				{
					int seekTo = _seekTo;
					_seekTo = 0;
					mediaPlayer.setTime(seekTo);
				}
			}
		});
	}
}