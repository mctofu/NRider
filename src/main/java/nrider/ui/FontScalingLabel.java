package nrider.ui;

import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * JLabel extension that adjusts the font size based on the label resizing
 */
public class FontScalingLabel extends JLabel {
    private final String _referenceText;
    private final int _minFontSize;
    private final int _maxFontSize;
    private Graphics _g;

    public FontScalingLabel(String text, String referenceText, int minSize, int maxSize) {
        super(text);
        _minFontSize = minSize;
        _maxFontSize = maxSize;
        _referenceText = referenceText;
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                adaptLabelFont(FontScalingLabel.this);
            }
        });
    }

    protected void adaptLabelFont(JLabel l) {
        if (_g == null) {
            return;
        }
        int currFontSize = getFont().getSize();

        Rectangle r  = l.getBounds();
        r.x          = 0;
        r.y          = 0;
        int fontSize = Math.max(_minFontSize, currFontSize);
        Font f       = l.getFont();

        Rectangle r1 = new Rectangle(getTextSize(l.getFont()));
        while (!r.contains(r1)) {
            fontSize--;
            if (fontSize <= _minFontSize)
                break;
            r1 = new Rectangle(getTextSize(f.deriveFont(f.getStyle(), fontSize)));
        }

        Rectangle r2 = new Rectangle();
        while (fontSize < _maxFontSize) {
            r2.setSize(getTextSize(f.deriveFont(f.getStyle(), fontSize + 1)));
            if (!r.contains(r2)) {
                break;
            }
            fontSize++;
        }

        setFont(f.deriveFont(f.getStyle(), fontSize));
        repaint();
    }

    private Dimension getTextSize(Font f) {
        Dimension size = new Dimension();
        FontMetrics fm = _g.getFontMetrics(f);
        size.width = fm.stringWidth(_referenceText);
        size.height = fm.getHeight();
        return size;
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        _g = g;
    }
}
