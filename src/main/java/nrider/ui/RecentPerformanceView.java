package nrider.ui;

import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.LinkedList;

public class RecentPerformanceView extends GraphView {
    public enum DataType {NORMAL, EXTREME, WARNING}

    private double _window;
    private LinkedList<GraphPoint> _history = new LinkedList<GraphPoint>();
    private double _initialMaxY;

    public RecentPerformanceView(double window, double minY, double maxY) {
        _window = window;
        setMinY(minY);
        _initialMaxY = maxY;
        setMaxY(maxY);
        setMaxX(window);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setGraph(_history);
    }

    public void addData(double x, double y, DataType type) {
        _history.add(new GraphPoint(x, y, getColor(type)));
        while (x - _history.peek().getX() > _window) {
            _history.remove();
        }

        double maxY = _initialMaxY;

        for (GraphPoint graphPoint : _history) {
            if (graphPoint.getY() > maxY) {
                maxY = graphPoint.getY();
            }
        }

        setMinX(x - _window);
        setMaxX(x);
        setMaxY(maxY);
        repaint();
    }

    private Color getColor(DataType type) {
        switch (type) {
            case NORMAL:
                return Color.GREEN;
            case EXTREME:
                return Color.RED;
            case WARNING:
                return Color.YELLOW;
        }
        return null;

    }

}
