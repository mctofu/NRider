package nrider.ui;

import nrider.core.RideLoad;
import nrider.ride.RideEvent;
import nrider.ride.RideScript;

import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;

public class RideScriptView extends GraphView {
    private RideScript _rideScript;

    public RideScriptView() {
        setMinimumSize(new Dimension(600, 100));
        setPreferredSize(new Dimension(600, 100));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public void setRideScript(RideScript rideScript) {
        _rideScript = rideScript;
        ArrayList<GraphPoint> graph = new ArrayList<>();
        for (RideEvent re : _rideScript) {
            graph.add(new GraphPoint(re.getPosition(), re.getLoad().getValue(), getColor(re.getLoad())));
        }

        setMaxY(_rideScript.getMaxLoad());
        setMaxX(_rideScript.getPeriod());
        setShowXIndicator(true);
        setGraph(graph);
        repaint();
    }

    private Color getColor(RideLoad load) {
        return new Color(211, 211, 211);
    }

    public void setRideTime(long rideTime) {
        setXIndicator(rideTime);
    }
}
