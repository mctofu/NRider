package nrider.ui;

import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.media.IMediaEventListener;
import nrider.media.MediaEvent;
import nrider.ride.IRide;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Uses vlcj to render video
 */
public class MediaPlayerView implements IMediaEventListener, IWorkoutListener {
    private final static Logger LOG = Logger.getLogger(MediaPlayerView.class);

    private JFrame _window;
    private EmbeddedMediaPlayerComponent _mediaPlayerComponent;
    private int _seekTo;
    private boolean _startedPlaying;

    public void launch() {
        SwingUtilities.invokeLater(() -> init());
    }

    private void init() {
        _mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        _window = new JFrame();
        _window.setSize(640, 480);
        _window.setLocationRelativeTo(null);
        _window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        _window.setContentPane(_mediaPlayerComponent);

        _window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                _mediaPlayerComponent.release();
            }
        });

        _window.setVisible(true);
    }

    public void handleMediaEvent(final MediaEvent me) {
        SwingUtilities.invokeLater(() -> doHandleMediaEvent(me));
    }

    private void doHandleMediaEvent(MediaEvent me) {
        switch (me.getType()) {
            case LOAD:
                loadMedia(me.getMedia());
                break;
            case PLAY:
                play();
                break;
            case SEEK:
                if (!_startedPlaying) {
                    _seekTo = me.getPosition() * 1000;
                } else {
                    _mediaPlayerComponent.mediaPlayer().controls().setTime(me.getPosition() * 1000);
                }
                break;
            case PAUSE:
                _mediaPlayerComponent.mediaPlayer().controls().pause();
                break;
        }
    }

    private void play() {
        _mediaPlayerComponent.mediaPlayer().controls().play();
    }

    public void handleLoadAdjust(String riderId, RideLoad newLoad) {
    }

    public void handleAddRider(Rider rider) {
    }

    public void handleRiderThresholdAdjust(String riderId, double newThreshold) {
    }

    public void handleRideLoaded(IRide ride) {
    }

    public void handleRideTimeUpdate(long rideTime) {
    }

    public void handleAddRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {
    }

    public void handleRemoveRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {
    }

    public void handleRideStatusUpdate(final IRide.Status status) {
        SwingUtilities.invokeLater(
                () -> doHandleRideStatusUpdate(status));
    }

    private void doHandleRideStatusUpdate(IRide.Status status) {
        switch (status) {
            case RUNNING:
                play();
                break;
            case STOPPED:
            case PAUSED:
                _mediaPlayerComponent.mediaPlayer().controls().pause();
                break;
        }
    }

    private void loadMedia(String path) {
        _mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(final MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    _startedPlaying = true;
                    if (_seekTo > 0) {
                        int seekTo = _seekTo;
                        _seekTo = 0;
                        mediaPlayer.controls().setTime(seekTo);
                    }
                });
            }
        });
        if (!_mediaPlayerComponent.mediaPlayer().media().prepare(path)) {
            throw new RuntimeException("couldn't prepare media");
        }
    }
}