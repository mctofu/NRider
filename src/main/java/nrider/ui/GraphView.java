package nrider.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * TODO: replace with an existing graphing library
 */
public class GraphView extends JComponent {
    private double _maxX;
    private double _minX;
    private double _maxY;
    private double _minY;
    private Collection<GraphPoint> _graph;
    private boolean _showXIndicator;
    private double _xIndicator;

    public Collection<GraphPoint> getGraph() {
        return _graph;
    }

    public void setGraph(Collection<GraphPoint> graph) {
        _graph = graph;
    }

    public double getMinY() {
        return _minY;
    }

    public void setMinY(double minY) {
        _minY = minY;
    }

    public double getMaxY() {
        return _maxY;
    }

    public void setMaxY(double maxY) {
        _maxY = maxY;
    }

    public double getMinX() {
        return _minX;
    }

    public void setMinX(double minX) {
        _minX = minX;
    }

    public double getMaxX() {
        return _maxX;
    }

    public void setMaxX(double maxX) {
        _maxX = maxX;
    }

    public boolean isShowXIndicator() {
        return _showXIndicator;
    }

    public void setShowXIndicator(boolean showXIndicator) {
        _showXIndicator = showXIndicator;
    }

    public double getXIndicator() {
        return _xIndicator;
    }

    public void setXIndicator(double xIndicator) {
        if (_xIndicator != xIndicator) {
            _xIndicator = xIndicator;
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (_graph != null) {
            Rectangle r = g.getClipBounds();

            double yTrim = 0;
            double yHeight = _maxY - _minY;
            if (yHeight > r.getHeight()) {
                yTrim = yHeight * .1;
            }

            double yScale = r.height / (yHeight - yTrim);

            double xScale = r.width / (_maxX - _minX);


            int lastX = -1, lastY = -1, lastDrawnY = -1;
            boolean lastLineVertical = false;
            for (GraphPoint pt : _graph) {
                int nextX = (int) ((pt.getX() - _minX) * xScale);
                int nextY = r.height - (int) ((pt.getY() - yTrim - _minY) * yScale);

                if (lastX != -1 && nextX != lastX) {
                    if (lastDrawnY == -1) {
                        lastDrawnY = lastY;
                    } else if (lastDrawnY != lastY) {
                        draw(g, lastX, lastDrawnY, lastX, lastY, pt.getColor(), r, lastLineVertical);
                        lastLineVertical = true;
                        lastDrawnY = lastY;
                    }
                    draw(g, lastX, lastDrawnY, nextX, nextY, pt.getColor(), r, lastLineVertical);
                    lastLineVertical = false;
                    lastDrawnY = nextY;
                }

                lastX = nextX;
                lastY = nextY;
            }

            lastX = -1;
            lastY = -1;
            // draw line in separate pass since it's problematic keeping the fill from overwriting it.
            for (GraphPoint pt : _graph) {
                int nextX = (int) ((pt.getX() - _minX) * xScale);
                int nextY = r.height - (int) ((pt.getY() - yTrim - _minY) * yScale);

                if (lastX != -1) {
                    g.drawLine(lastX, lastY, nextX, nextY);
                }

                lastX = nextX;
                lastY = nextY;
            }

            if (_showXIndicator) {
                g.setXORMode(Color.GREEN);

                int xIndicatorPixel = (int) ((_xIndicator - _minX) * xScale);
                g.fillRect(xIndicatorPixel, 0, 2, r.height);

                g.setPaintMode();
            }
        }
    }

    private void draw(Graphics g, int x1, int y1, int x2, int y2, Color color, Rectangle bounds, boolean lastLineVertical) {
        Color currentColor = g.getColor();
        g.setColor(color);

        int minY = y2 < y1 ? y1 : y2;

        if (x1 == x2) {
            g.drawLine(x1, minY, x1, bounds.height);
        } else {
            Polygon polygon = new Polygon();
            polygon.addPoint(x1, y1);
            polygon.addPoint(x2, y2);
            if (y2 < y1) {
                polygon.addPoint(x2, y1);
            } else {
                polygon.addPoint(x1, y2);
            }
            g.fillPolygon(polygon);

            int offset = lastLineVertical ? 1 : 0;
            g.fillRect(x1 + offset, minY, x2 - x1 - offset, bounds.height - minY);
        }

        g.setColor(currentColor);
    }


    public class GraphPoint {
        double _x;
        double _y;
        Color _color;

        public GraphPoint(double x, double y) {
            _x = x;
            _y = y;
        }

        public GraphPoint(double x, double y, Color color) {
            _x = x;
            _y = y;
            _color = color;
        }

        public double getX() {
            return _x;
        }

        public void setX(double x) {
            _x = x;
        }

        public double getY() {
            return _y;
        }

        public void setY(double y) {
            _y = y;
        }

        public Color getColor() {
            return _color;
        }

        public void setColor(Color color) {
            _color = color;
        }
    }
}
