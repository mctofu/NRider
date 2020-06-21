package nrider.io;

import nrider.event.EventPublisher;

import java.util.HashMap;

public class PerformanceDataChangePublisher implements IPerformanceDataSource {
    private String _identifier;
    private final EventPublisher<IPerformanceDataListener> _performancePublisher = EventPublisher.directPublisher();
    private long _lastSpeedUpdateTime;

    private final HashMap<PerformanceData.Type, Float> _lastValue = new HashMap<>();
    private final HashMap<PerformanceData.Type, Long> _lastSent = new HashMap<>();

    public PerformanceDataChangePublisher(String identifier) {
        _identifier = identifier;
    }

    public void setIdentifier(String identifier) {
        _identifier = identifier;
    }

    public String getIdentifier() {
        return _identifier;
    }

    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _performancePublisher.addListener(listener);
    }

    public void setPower(float power) {
        considerUpdate(PerformanceData.Type.POWER, power);
    }

    public void setSpeed(float speed) {
        long time = System.currentTimeMillis();
        float distance = (float) (speed / (time - _lastSpeedUpdateTime) / 1000.0);
        _lastSpeedUpdateTime = time;
        sendUpdate(PerformanceData.Type.DISTANCE, distance, time);
        considerUpdate(PerformanceData.Type.SPEED, speed);
    }

    public void setCadence(float cadence) {
        considerUpdate(PerformanceData.Type.CADENCE, cadence);
    }

    public void setHeartRate(float heartRate) {
        considerUpdate(PerformanceData.Type.HEART_RATE, heartRate);
    }

    public void setCalibration(float calibration) {
        considerUpdate(PerformanceData.Type.CALIBRATION, calibration, 60000);
    }

    private void considerUpdate(PerformanceData.Type type, float value) {
        considerUpdate(type, value, 1000);
    }

    private void considerUpdate(PerformanceData.Type type, float value, long threshold) {
        Float lastValue = _lastValue.get(type);

        long now = System.currentTimeMillis();

        if (lastValue != null) {
            if (lastValue != value || (now - _lastSent.get(type) > threshold)) {
                sendUpdate(type, value, now);
            }
        } else if (value != 0) {
            sendUpdate(type, value, now);
        }
    }

    private void sendUpdate(PerformanceData.Type type, float value, long time) {
        _lastValue.put(type, value);
        _lastSent.put(type, time);
        final PerformanceData data = new PerformanceData();
        data.setType(type);
        data.setValue(value);
        _performancePublisher.publishEvent(
                target -> target.handlePerformanceData(getIdentifier(), data)
        );
    }
}
