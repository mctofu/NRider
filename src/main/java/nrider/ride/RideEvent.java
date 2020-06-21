package nrider.ride;

import nrider.core.RideLoad;

public class RideEvent {
    long _position;
    final RideLoad _load;

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
}
