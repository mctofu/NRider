package nrider.core;

import nrider.event.EventPublisher;
import nrider.io.ControlData;
import nrider.io.IControlDataListener;
import nrider.io.IPerformanceDataListener;
import nrider.io.IPerformanceDataSource;
import nrider.io.IWorkoutController;
import nrider.io.PerformanceData;
import nrider.media.IMediaEventListener;
import nrider.media.MediaEvent;
import nrider.ride.IRide;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central control for a workout.  Manages riders and trainers in a workout.
 * TODO: review/fix thread safety after sorting out how everything should work
 */
public class WorkoutSession implements
        IPerformanceDataListener, IPerformanceDataSource, IControlDataListener, IMediaEventListener {

    private final static Logger LOG = Logger.getLogger(WorkoutSession.class);

    private final static WorkoutSession _instance = new WorkoutSession();

    public static WorkoutSession instance() {
        return _instance;
    }

    public enum RiderAlertType {
        SPEED_HIGH("SpdHi"), SPEED_LOW("SpdLo"), POWER_ASSIST("PwrBst");

        private final String _shortName;

        RiderAlertType(String shortName) {
            _shortName = shortName;
        }

        public String getShortName() {
            return _shortName;
        }
    }

    private final List<Rider> _riders = new ArrayList<>();
    private final List<IWorkoutController> _controllers = new ArrayList<>();
    private final List<Closeable> _resourcesToCleanup = new ArrayList<>();
    private final Map<String, RiderSession> _deviceMap = new HashMap<>();
    private final Map<String, RiderSession> _riderMap = new HashMap<>();
    private final Set<String> _unmappedIdentifiers = new HashSet<>();
    private final Set<String> _extMetricDevices = new HashSet<>();

    private final EventPublisher<IPerformanceDataListener> _localPerformancePublisher =
            EventPublisher.singleThreadPublisher(WorkoutSession.class.getName());
    private final EventPublisher<IMediaEventListener> _mediaEventPublisher =
            EventPublisher.singleThreadPublisher(WorkoutSession.class.getName());
    private final EventPublisher<IWorkoutListener> _workoutPublisher =
            EventPublisher.singleThreadPublisher(WorkoutSession.class.getName());

    private final RiderPerformanceMonitor _riderPerformanceMonitor = new RiderPerformanceMonitor(this);

    private IRide _ride;

    public WorkoutSession() {
        addLocalPerformanceDataListener(_riderPerformanceMonitor);
    }

    public void setRideElapsedTime(final long elapsed) {
        _workoutPublisher.publishEvent(
                target -> target.handleRideTimeUpdate(elapsed));
    }

    public void setRide(final IRide ride) {
        _ride = ride;
        _workoutPublisher.publishEvent(
                target -> target.handleRideLoaded(ride));
    }

    public IRide getRide() {
        return _ride;
    }

    public void addLocalRider(final Rider rider) {
        addRider(rider, "local");
    }

    private void addRider(final Rider rider, String source) {
        synchronized (_riders) {
            _riders.add(rider);
            RiderSession session = new RiderSession(rider, source);
            _riderMap.put(rider.getIdentifier(), session);
            _workoutPublisher.publishEvent(
                    target -> target.handleAddRider(rider));
        }
    }

    public void addRiderAlert(final String identifier, final RiderAlertType alert) {
        _workoutPublisher.publishEvent(
                target -> target.handleAddRiderAlert(identifier, alert));
    }

    public void removeRiderAlert(final String identifier, final RiderAlertType alert) {
        _workoutPublisher.publishEvent(
                target -> target.handleRemoveRiderAlert(identifier, alert));
    }

    public List<Rider> getRiders() {
        synchronized (_riders) {
            return new ArrayList<>(_riders);
        }
    }

    public Rider getRider(String identifier) {
        synchronized (_riders) {
            for (Rider r : _riders) {
                if (r.getIdentifier().equals(identifier)) {
                    return r;
                }
            }
        }
        return null;
    }

    public void addWorkoutController(IWorkoutController controller) {
        synchronized (_controllers) {
            _controllers.add(controller);
        }
    }

    public List<IWorkoutController> getControllers() {
        synchronized (_controllers) {
            return Collections.unmodifiableList(_controllers);
        }
    }

    public void addResourceToCleanup(Closeable resource) {
        synchronized (_resourcesToCleanup) {
            _resourcesToCleanup.add(resource);
        }
    }

    public void associateRider(String riderId, String identifier, boolean extMetrics) {
        synchronized (_riders) {
            RiderSession session = _riderMap.get(riderId);
            session.addAssociation(identifier);
            _deviceMap.put(identifier, session);
            if (extMetrics) {
                _extMetricDevices.add(identifier);
            }
            _unmappedIdentifiers.remove(identifier);
        }
    }

    public void setRideLoad(final RideLoad load) {
        setRideLoad(null, load);
    }

    public void setRideLoad(String riderId, final RideLoad load) {
        // TODO: Support gradient
        synchronized (_controllers) {
            synchronized (_riders) {
                for (IWorkoutController controller : _controllers) {
                    final RiderSession rider = _deviceMap.get(controller.getIdentifier());

                    if (rider == null) {
                        continue;
                    }

                    if (riderId == null || (rider.getRider().getIdentifier().equals(riderId))) {
                        rider.setCurrentLoad(load);
                        controller.setLoad(rider.getLoadForWorkout());
                        _workoutPublisher.publishEvent(
                                target -> target.handleLoadAdjust(rider.getRider().getIdentifier(), load));
                    }
                }
            }
        }
    }

    public double getTargetWatts(String riderId) {
        synchronized (_riders) {
            return _riderMap.get(riderId).getLoadForWorkoutWithoutHandicap();
        }
    }

    public void startRide() {
        if (_ride != null) {
            synchronized (_ride) {
                if ((_ride.getStatus() == IRide.Status.READY || _ride.getStatus() == IRide.Status.PAUSED)) {
                    _ride.start();
                    _riderPerformanceMonitor.activate();
                    _workoutPublisher.publishEvent(
                            target -> target.handleRideStatusUpdate(_ride.getStatus()));
                }
            }
        }
    }

    public void pauseRide() {
        if (_ride != null) {
            synchronized (_ride) {
                if (_ride.getStatus() == IRide.Status.RUNNING || _ride.getStatus() == IRide.Status.PAUSED) {
                    if (_ride.getStatus() == IRide.Status.PAUSED) {
                        _ride.stop();
                    } else {
                        _ride.pause();
                    }

                    _riderPerformanceMonitor.deactivate();
                    _workoutPublisher.publishEvent(
                            target -> target.handleRideStatusUpdate(_ride.getStatus()));
                }
            }
        }
    }

    public String getIdentifier() {
        return "NRider WorkoutSession";
    }

    /**
     * subscribe to performance data from local riders
     */
    public void addLocalPerformanceDataListener(IPerformanceDataListener listener) {
        _localPerformancePublisher.addListener(listener);
    }

    /**
     * subscribe to performance data from riders
     */
    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _localPerformancePublisher.addListener(listener);
    }

    /**
     * handle raw perf data from a controller and publish rider perf data
     */
    public void handlePerformanceData(final String identifier, final PerformanceData data) {
        synchronized (_riders) {
            if (_deviceMap.containsKey(identifier)) {
                final RiderSession rs = _deviceMap.get(identifier);
                final PerformanceData publishData = _extMetricDevices.contains(identifier) ?
                        extAdjust(data) : data;
                getPublisher(rs).publishEvent(
                        target -> target.handlePerformanceData(rs.getRider().getIdentifier(), publishData));
            } else {
                _unmappedIdentifiers.add(identifier);
            }
        }
    }

    private PerformanceData extAdjust(PerformanceData data) {
        PerformanceData adjusted = new PerformanceData();
        adjusted.setValue(data.getValue());
        adjusted.setTimeStamp(data.getTimeStamp());

        switch(data.getType()) {
            case POWER:
                adjusted.setType(PerformanceData.Type.EXT_POWER);
                break;
            case CADENCE:
                adjusted.setType(PerformanceData.Type.EXT_CADENCE);
                break;
            case HEART_RATE:
                adjusted.setType(PerformanceData.Type.EXT_HEART_RATE);
                break;
            default:
                adjusted.setType(data.getType());
                break;
        }

        return adjusted;
    }

    public String[] getUnmappedIdentifiers() {
        synchronized (_riders) {
            return _unmappedIdentifiers.toArray(new String[0]);
        }
    }

    public Map<String, String> getMappedIdentifiers() {
        synchronized (_riders) {
            HashMap<String, String> result = new HashMap<>();
            for (Map.Entry<String, RiderSession> entry : _deviceMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getRider().getName());
            }
            return result;
        }
    }

    private EventPublisher<IPerformanceDataListener> getPublisher(RiderSession session) {
        return _localPerformancePublisher;
    }

    public void handleControlData(String identifier, ControlData data) {
        try {
            synchronized (_controllers) {
                synchronized (_riders) {
                    if (_deviceMap.containsKey(identifier)) {
                        RiderSession rs = _deviceMap.get(identifier);
                        switch (data.getType()) {
                            case PLUS:
                                // TODO: more sense to just set on rider which triggers an event for the workout to adjust the load?
                                setRiderThreshold(rs.getRider().getIdentifier(), rs.getRider().getThresholdPower() + 5);
                                break;
                            case MINUS:
                                // TODO: more sense to just set on rider which triggers an event for the workout to adjust the load?
                                setRiderThreshold(rs.getRider().getIdentifier(), rs.getRider().getThresholdPower() - 5);
                                break;
                            case START:
                                LOG.info("Received control: START");
                                startRide();
                                break;
                            case STOP:
                                LOG.info("Received control: STOP");
                                pauseRide();
                                break;
                            case RECALIBRATE:
                                LOG.info("Received control: RECALIBRATE");
                                recalibrateRider(rs.getRider().getIdentifier());
                                break;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("Control data handling error", e);
        }
    }

    public void addWorkoutListener(IWorkoutListener listener) {
        _workoutPublisher.addListener(listener);
    }

    public void addMediaEventListner(IMediaEventListener listener) {
        _mediaEventPublisher.addListener(listener);
    }

    public void handleMediaEvent(final MediaEvent me) {
        _mediaEventPublisher.publishEvent(
                target -> target.handleMediaEvent(me));
    }

    public void close() throws IOException {
        synchronized (_controllers) {
            for (IWorkoutController controller : _controllers) {
                controller.close();
            }
        }
        synchronized (_resourcesToCleanup) {
            for (Closeable resource : _resourcesToCleanup) {
                resource.close();
            }
        }
    }

    public void recalibrateRider(String riderId) {
        synchronized (_controllers) {
            synchronized (_riders) {
                final IWorkoutController riderController = getRiderController(riderId);
                if (riderController != null) {
                    riderController.recalibrate();
                }
            }
        }
    }

    public void setRiderThreshold(final String identifier, final int thresholdPower) {
        synchronized (_controllers) {
            synchronized (_riders) {
                Rider rider = getRider(identifier);
                rider.setThresholdPower(thresholdPower);
                reapplyRiderLoad(identifier);
                _workoutPublisher.publishEvent(
                        target -> target.handleRiderThresholdAdjust(identifier, thresholdPower));
            }
        }
    }

    public void setRiderHandicap(String identifier, final int handicap) {
        synchronized (_controllers) {
            synchronized (_riders) {
                RiderSession rider = _riderMap.get(identifier);
                rider.setHandicap(handicap);
                reapplyRiderLoad(identifier);
            }
        }
    }

    private void reapplyRiderLoad(String riderId) {
        synchronized (_controllers) {
            synchronized (_riders) {
                IWorkoutController riderController = getRiderController(riderId);
                if (riderController != null) {
                    RiderSession rider = _riderMap.get(riderId);
                    riderController.setLoad(rider.getLoadForWorkout());
                }
            }
        }
    }

    private IWorkoutController getRiderController(String riderId) {
        // need sync of controllers & riders before calling
        for (IWorkoutController controller : _controllers) {
            RiderSession rider = _deviceMap.get(controller.getIdentifier());
            if (rider != null && rider.getRider().getIdentifier().equals(riderId)) {
                return controller;
            }
        }
        return null;
    }
}
