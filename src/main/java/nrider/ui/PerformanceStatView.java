package nrider.ui;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class PerformanceStatView {
    public static final String METRIC_REF_TEXT = "33.333";

    private RecentPerformanceView _performanceGraphView;
    private JLabel _value;
    private long _initialTime;
    private NumberFormat _numberFormat;


    public PerformanceStatView(double minY, double maxY, double window, NumberFormat numberFormat) {
        _performanceGraphView = new RecentPerformanceView(window, minY, maxY);

        _value = new FontScalingLabel("", METRIC_REF_TEXT, 30, 80);
        _value.setHorizontalAlignment(SwingConstants.RIGHT);
        _value.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
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
