package nrider.io;

/**
 * point in time sample of rider performance measuremennts (speed, power, hr, etc)
 */
public class PerformanceData {
    public enum Type {POWER, SPEED, CADENCE, HEART_RATE, DISTANCE, EXT_HEART_RATE, EXT_CADENCE, EXT_POWER, CALIBRATION}

    private Type _type;
    private float _value;
    private long _timeStamp;

    public PerformanceData() {
        _timeStamp = System.currentTimeMillis();
    }

    public PerformanceData(Type type, float value) {
        this();
        _type = type;
        _value = value;
    }

    public PerformanceData(Type type, float value, long timeStamp) {
        _type = type;
        _value = value;
        _timeStamp = timeStamp;
    }

    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        _type = type;
    }

    public float getValue() {
        return _value;
    }

    public void setValue(float value) {
        _value = value;
    }

    public long getTimeStamp() {
        return _timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        _timeStamp = timeStamp;
    }
}
