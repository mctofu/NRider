package nrider.ride;

import nrider.core.RideLoad;

public class RideEvent implements Comparable {
    long _position;
    RideLoad _load;

    public RideEvent(long position, RideLoad load) {
        _position = position;
        _load = load;
    }

    public long getPosition() {
        return _position;
    }

    public RideLoad getLoad() {
        return _load;
    }

    public int compareTo(Object o) {
        return ((Long) _position).compareTo((Long) o);
    }
}
