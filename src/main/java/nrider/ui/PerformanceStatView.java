package nrider.ui;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class PerformanceStatView {
    private RecentPerformanceView _performanceGraphView;
    private JLabel _value;
    private long _initialTime;
    private NumberFormat _numberFormat;


    public PerformanceStatView(double minY, double maxY, double window, NumberFormat numberFormat) {
        _performanceGraphView = new RecentPerformanceView(window, minY, maxY);
        _performanceGraphView.setMinimumSize(new Dimension(50, 25));
        _performanceGraphView.setPreferredSize(new Dimension(50, 25));

        _value = new JLabel("", SwingConstants.RIGHT);
        _value.setPreferredSize(new Dimension(50, 25));
        _value.setFont(new Font("Serif", Font.PLAIN, 30));
        _initialTime = System.currentTimeMillis();
        _numberFormat = numberFormat;
    }

    public Container getLabel() {
        return _value;
    }

    public void setVisible(boolean visible) {
        _value.setVisible(visible);
        _performanceGraphView.setVisible(visible);
    }


    public Container getGraph() {
        return _performanceGraphView;
    }

    public void updateValue(double value, RecentPerformanceView.DataType type) {
        _performanceGraphView.addData((System.currentTimeMillis() - _initialTime) / 1000, value, type);
        _value.setText(_numberFormat.format(value));
    }
}
