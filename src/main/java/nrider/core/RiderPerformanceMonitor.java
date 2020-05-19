package nrider.core;

import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;

import java.util.HashMap;

public class RiderPerformanceMonitor implements IPerformanceDataListener {
    private final float MIN_SPEED = 16.5f;

    private boolean _active;
    private HashMap<String, RiderMonitor> _riderMap = new HashMap<>();

    public void handlePerformanceData(String identifier, PerformanceData data) {
        if (_active) {
            switch (data.getType()) {
                case SPEED:
                    float speed = (float) (data.getValue() * 2.237);
                    if (speed < MIN_SPEED) {
                        sendSpeedLow(identifier);
                    } else if (speed > 25) {
                        sendSpeedHigh(identifier);
                    } else {
                        clearSpeed(identifier);
                    }
                    getMonitor(identifier).setLastSpeed(speed);
                    break;
                case POWER:
                    float actualWatts = data.getValue();
                    double targetWatts = WorkoutSession.instance().getTargetWatts(identifier);
                    int threshold = WorkoutSession.instance().getRider(identifier).getThresholdPower();
                    RiderMonitor monitor = getMonitor(identifier);
                    if (monitor.getLastSpeed() > 3 && (monitor.getLastSpeed() < MIN_SPEED || (actualWatts < targetWatts * .5))) {
                        if (monitor.getAssisted().setAlert()) {
                            WorkoutSession.instance().addRiderAlert(identifier, WorkoutSession.RiderAlertType.POWER_ASSIST);
                            WorkoutSession.instance().setRiderHandicap(identifier, threshold / 2);
                        }
                    } else {
                        if (monitor.getAssisted().clearAlert()) {
                            WorkoutSession.instance().removeRiderAlert(identifier, WorkoutSession.RiderAlertType.POWER_ASSIST);
                            WorkoutSession.instance().setRiderHandicap(identifier, 0);
                        }
                    }
                    break;
            }
        }
    }

    private void sendSpeedLow(String identifier) {
        RiderMonitor monitor = getMonitor(identifier);
        if (monitor.getOverSpeed().clearAlert(true)) {
            WorkoutSession.instance().removeRiderAlert(identifier, WorkoutSession.RiderAlertType.SPEED_HIGH);
        }
        if (monitor.getUnderSpeed().setAlert()) {
            WorkoutSession.instance().addRiderAlert(identifier, WorkoutSession.RiderAlertType.SPEED_LOW);
        }
    }

    private void sendSpeedHigh(String identifier) {
        RiderMonitor monitor = getMonitor(identifier);
        if (monitor.getUnderSpeed().clearAlert(true)) {
            WorkoutSession.instance().removeRiderAlert(identifier, WorkoutSession.RiderAlertType.SPEED_LOW);
        }
        if (monitor.getOverSpeed().setAlert()) {
            WorkoutSession.instance().addRiderAlert(identifier, WorkoutSession.RiderAlertType.SPEED_HIGH);
        }
    }

    private void clearSpeed(String identifier) {
        RiderMonitor monitor = getMonitor(identifier);
        if (monitor.getOverSpeed().clearAlert()) {
            WorkoutSession.instance().removeRiderAlert(identifier, WorkoutSession.RiderAlertType.SPEED_HIGH);
        }
        if (monitor.getUnderSpeed().clearAlert()) {
            WorkoutSession.instance().removeRiderAlert(identifier, WorkoutSession.RiderAlertType.SPEED_LOW);
        }
    }

    private RiderMonitor getMonitor(String identifier) {
        if (!_riderMap.containsKey(identifier)) {
            _riderMap.put(identifier, new RiderMonitor());
        }

        return _riderMap.get(identifier);
    }

    public void activate() {
        _active = true;
    }

    public void deactivate() {
        _active = false;
    }

    class RiderMonitor {
        private AlertMonitor _isUnderSpeed = new AlertMonitor(3, 3);
        private AlertMonitor _isOverSpeed = new AlertMonitor(3, 3);
        private AlertMonitor _isAssisted = new AlertMonitor(2, 4);
        private double _lastSpeed;

        public AlertMonitor getUnderSpeed() {
            return _isUnderSpeed;
        }

        public AlertMonitor getOverSpeed() {
            return _isOverSpeed;
        }

        public AlertMonitor getAssisted() {
            return _isAssisted;
        }

        public double getLastSpeed() {
            return _lastSpeed;
        }

        public void setLastSpeed(double lastSpeed) {
            _lastSpeed = lastSpeed;
        }
    }

    class AlertMonitor {
        private int _thresholdSet;
        private int _currentSet;
        private int _thresholdReset;
        private int _currentReset;
        private boolean _alarmActive;

        AlertMonitor(int thresholdSet, int thresholdReset) {
            _thresholdSet = thresholdSet;
            _thresholdReset = thresholdReset;
        }

        public boolean setAlert() {
            if (_currentReset > 0) {
                _currentReset--;
            }

            if (!_alarmActive) {
                _currentSet++;
                if (_currentSet == _thresholdSet) {
                    _alarmActive = true;
                    _currentReset = 0;
                    return true;
                }
            }

            return false;
        }

        public boolean clearAlert() {
            return clearAlert(false);
        }

        public boolean clearAlert(boolean immediate) {
            if (!_alarmActive) {
                if (_currentSet > 0) {
                    _currentSet--;
                }
            }

            if (_alarmActive) {
                _currentReset++;
                if (_currentReset == _thresholdReset || immediate) {
                    _alarmActive = false;
                    _currentSet = 0;
                    return true;
                }
            }
            return false;
        }

        public boolean isAlert() {
            return _alarmActive;
        }
    }
}
