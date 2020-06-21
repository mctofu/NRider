package nrider.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles data from multiple devices by creating a PerformanceDataChangePublisher for each discovered device.
 */
public class MultiDevicePerformancePublisher {
    private final Map<String, PerformanceDataChangePublisher> _devicePublishers = new HashMap<>();
    private final List<IPerformanceDataListener> _perfListeners = new ArrayList<>();

    public void setPower(String deviceId, float power) {
        getPublisher(deviceId).setPower(power);
    }

    public void setSpeed(String deviceId, float speed) {
        getPublisher(deviceId).setSpeed(speed);
    }

    public void setCadence(String deviceId, float cadence) {
        getPublisher(deviceId).setCadence(cadence);
    }

    public void setHeartRate(String deviceId, float heartRate) {
        getPublisher(deviceId).setHeartRate(heartRate);
    }

    public void setCalibration(String deviceId, float calibration) {
        getPublisher(deviceId).setCalibration(calibration);
    }

    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _perfListeners.add(listener);
        for (PerformanceDataChangePublisher p : _devicePublishers.values()) {
            p.addPerformanceDataListener(listener);
        }
    }

    private PerformanceDataChangePublisher getPublisher(String deviceId) {
        PerformanceDataChangePublisher publisher = _devicePublishers.get(deviceId);
        if (publisher == null) {
            publisher = new PerformanceDataChangePublisher(deviceId);
            for (IPerformanceDataListener l : _perfListeners) {
                publisher.addPerformanceDataListener(l);
            }
            _devicePublishers.put(deviceId, publisher);
        }
        return publisher;
    }
}
