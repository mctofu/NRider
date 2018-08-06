package nrider;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple appender that logs to a jframe.
 */
public class DebugAppender extends AppenderSkeleton {
    private JFrame _window;
    private JTextArea _text;
    private AtomicBoolean _init = new AtomicBoolean(false);

    public DebugAppender() {

        super();
    }

    public void close() {

    }

    public boolean requiresLayout() {
        return true;
    }

    private void init() {
        _window = new JFrame();
        _window.setTitle(getName());
        _window.setSize(500, 500);
        _window.setLocation(500, 0);

        _text = new JTextArea();
        _text.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) _text.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        _window.getContentPane().add(new JScrollPane(_text));
        _window.setVisible(true);
        _init.set(true);
    }

    @Override
    protected void append(final LoggingEvent loggingEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!_init.get()) {
                    init();
                }

                if (_window.isVisible()) {
                    _text.append(loggingEvent.getRenderedMessage() + "\n");
                }
            }
        });
    }
}