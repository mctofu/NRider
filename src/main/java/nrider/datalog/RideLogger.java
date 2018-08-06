package nrider.datalog;

import nrider.core.IWorkoutListener;
import nrider.core.RideLoad;
import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import nrider.ride.IRide;

import java.util.HashMap;

public class RideLogger implements IPerformanceDataListener, IWorkoutListener {
    private HashMap<String, BaseLogger> _loggers = new HashMap<>();
    private volatile boolean _logging = false;

    public void handlePerformanceData(String identifier, PerformanceData data) {
        BaseLogger logger;


        synchronized (_loggers) {
            logger = _loggers.get(identifier);
            if (logger == null) {
                logger = new TxtLogger(identifier);
                _loggers.put(identifier, logger);
            }
        }

        synchronized (logger) {
            if (_logging) {
                logger.logData(data);
            }
        }
    }

    public void handleLoadAdjust(String riderId, RideLoad newLoad) {

    }

    public void handleAddRider(Rider rider) {

    }

    public void handleRiderThresholdAdjust(String riderId, double newThreshold) {

    }

    public void handleRideLoaded(IRide ride) {

    }

    public void handleRideTimeUpdate(long rideTime) {

    }

    public void handleAddRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {

    }

    public void handleRemoveRiderAlert(String riderId, WorkoutSession.RiderAlertType type) {

    }

    public void handleRideStatusUpdate(IRide.Status status) {
        switch (status) {
            case RUNNING:
                _logging = true;
                break;
            case PAUSED:
                _logging = false;
                break;
            case STOPPED:
                _logging = false;
                synchronized (_loggers) {
                    for (BaseLogger tcxLogger : _loggers.values()) {
                        tcxLogger.close();
                    }
                }
                break;
        }
    }
}
