package nrider.core;

import java.util.ArrayList;
import java.util.List;

public class Rider {
    private String _name;
    private int _thresholdPower;
    private final List<String> _devices = new ArrayList<>();

    public String getIdentifier() {
        return getName();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public int getThresholdPower() {
        return _thresholdPower;
    }

    public void setThresholdPower(int thresholdPower) {
        _thresholdPower = thresholdPower;
    }

    public List<String> getDevices() {
        return _devices;
    }

    public void addDevice(String deviceId) {
        _devices.add(deviceId);
    }


}
