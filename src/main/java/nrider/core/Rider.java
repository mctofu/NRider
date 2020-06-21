package nrider.core;

public class Rider {
    private String _name;
    private int _thresholdPower;

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
}
