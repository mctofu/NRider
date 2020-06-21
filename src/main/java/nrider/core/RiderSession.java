package nrider.core;

import java.util.ArrayList;
import java.util.List;

public class RiderSession {
    private final Rider _rider;
    private int _handicap;
    private RideLoad _currentLoad;
    private final List<String> _associations = new ArrayList<>();
    private final String _source;

    public RiderSession(Rider rider, String source) {
        _rider = rider;
        _source = source;
    }

    public Rider getRider() {
        return _rider;
    }

    public int getHandicap() {
        return _handicap;
    }

    public void setHandicap(int handicap) {
        _handicap = handicap;
    }

    public RideLoad getCurrentLoad() {
        return _currentLoad;
    }

    public void setCurrentLoad(RideLoad currentLoad) {
        _currentLoad = currentLoad;
    }

    public void addAssociation(String identifier) {
        _associations.add(identifier);
    }

    public String getSource() {
        return _source;
    }

    /*
     * target load for the rider not including handicap
     */
    public double getLoadForWorkoutWithoutHandicap() {
        return getLoadForWorkout(0);
    }

    /*
     * actual load for the rider including handicap
     */
    public double getLoadForWorkout() {
        return getLoadForWorkout(_handicap);
    }

    private double getLoadForWorkout(int handicap) {
        if (_currentLoad == null) {
            return 0;
        } else if (_currentLoad.getType() == RideLoad.Type.PERCENT_THRESHOLD) {
            return (_rider.getThresholdPower() - handicap) * _currentLoad.getValue() / 100;
        } else if (_currentLoad.getType() == RideLoad.Type.WATTS) {
            return _currentLoad.getValue() - handicap;
        }
        return 0;
    }
}
