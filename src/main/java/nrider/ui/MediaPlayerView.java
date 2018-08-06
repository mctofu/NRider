package nrider.ui;

import com.sun.jna.Memory;
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
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.*;
import uk.co.caprica.vlcj.player.direct.*;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Use jmc classes from javafx to render video
 */
public class MediaPlayerView implements IMediaEventListener, IWorkoutListener {
    private final static Logger LOG = Logger.getLogger(MediaPlayerView.class);

    private JFrame _window;
    private JPanel _mediaPanel;
    private BufferedImage _image;
    private DirectMediaPlayerComponent _mediaPlayerComponent;
    private int _seekTo;
    private boolean _startedPlaying;
    private int _currentWidth;
    private int _currentHeight;
    private RenderCallback _currentCallback;

    public void launch(final String vlcPath) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                init(vlcPath);
            }
        });
    }

    private void init(String vlcPath) {
        _window = new JFrame();
        _mediaPanel = new VideoSurfacePanel();
        _mediaPanel.setLayout(new BorderLayout());

        _window.add(_mediaPanel, BorderLayout.CENTER);

        _window.setSize(640, 480);

        _window.pack();
        _window.setLocationRelativeTo(null);
        _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (vlcPath != null) {
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
        }

        boolean found = new NativeDiscovery().discover();
        System.out.println(found);
        System.out.println(LibVlc.INSTANCE.libvlc_get_version());

        BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                return new RV32BufferFormat(_currentWidth, _currentHeight);
            }
        };

        _mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new SwitchingCallback();
            }
        };

        _window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                _mediaPlayerComponent.release();
            }
        });

        _mediaPanel.setVisible(true);

        _window.setVisible(true);

    }

    public void handleMediaEvent(final MediaEvent me) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doHandleMediaEvent(me);
            }
        });
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
                new Runnable() {
                    public void run() {
                        doHandleRideStatusUpdate(status);
                    }
                });
    }

    private void doHandleRideStatusUpdate(IRide.Status status) {
        switch (status) {
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

        for (TrackInfo track : _mediaPlayerComponent.getMediaPlayer().getTrackInfo(TrackType.VIDEO)) {
            VideoTrackInfo videoTrack = (VideoTrackInfo) track;

            int width = videoTrack.width();
            int height = videoTrack.height();
            _mediaPanel.setPreferredSize(new Dimension(width,
                    height));
            _mediaPanel.setMinimumSize(new Dimension(width, height));
            _mediaPanel.setMaximumSize(new Dimension(width, height));

            _window.pack();
            _window.setLocationRelativeTo(null);
            _currentWidth = width;
            _currentHeight = height;
            _currentCallback = new PlayerRenderCallbackAdapter();
            _image = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .createCompatibleImage(width, height);
            break;
        }

        _mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                _startedPlaying = true;
                if (_seekTo > 0) {
                    int seekTo = _seekTo;
                    _seekTo = 0;
                    mediaPlayer.setTime(seekTo);
                }
            }
        });
    }

    private class VideoSurfacePanel extends JPanel {

        private VideoSurfacePanel() {
            setBackground(Color.black);
            setOpaque(true);
//			setPreferredSize(new Dimension(width, height));
//			setMinimumSize(new Dimension(width, height));
//			setMaximumSize(new Dimension(width, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(_image, null, 0, 0);
        }
    }

    private class SwitchingCallback implements RenderCallback {

        @Override
        public void display(DirectMediaPlayer directMediaPlayer, Memory[] memories, BufferFormat bufferFormat) {
            _currentCallback.display(directMediaPlayer, memories, bufferFormat);
        }
    }

    private class PlayerRenderCallbackAdapter extends RenderCallbackAdapter {

        private PlayerRenderCallbackAdapter() {
            super(new int[_currentWidth * _currentHeight]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            // Simply copy buffer to the image and repaint
            _image.setRGB(0, 0, _currentWidth, _currentHeight, rgbBuffer, 0, _currentWidth);
            _mediaPanel.repaint();
        }
    }

}