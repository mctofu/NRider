package nrider.io;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import nrider.event.EventPublisher;
import nrider.monitor.IMonitorable;
import org.apache.log4j.Logger;
import sportgrpc.ControllerGrpc;
import sportgrpc.Sport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Connects to a sport-grpc service which can provide access to multiple workout controllers or data sources.
 */
public class SportGrpcController implements IPerformanceDataSource, IControlDataSource, IWorkoutController, IMonitorable {
    private final static Logger LOG = Logger.getLogger(ComputrainerController.class);

    private final String _id;
    private final String _target;
    private final MultiDevicePerformancePublisher _performancePublisher = new MultiDevicePerformancePublisher();
    private final EventPublisher<IControlDataListener> _controlPublisher = EventPublisher.directPublisher();
    private final EventPublisher<IWorkoutControllerDiscoveryListener> _discoveryPublisher = EventPublisher.directPublisher();
    private final Map<String, IWorkoutController> _controllers = new HashMap<>();

    private ManagedChannel _channel;
    private ControllerGrpc.ControllerBlockingStub _controllerStub;
    private Thread _readerThread;

    public SportGrpcController(String target, String id) {
        _target = target;
        _id = id;
    }

    public void addWorkoutControllerDiscoveryListener(IWorkoutControllerDiscoveryListener listener) {
        _discoveryPublisher.addListener(listener);
    }

    @Override
    public void addControlDataListener(IControlDataListener listener) {
        _controlPublisher.addListener(listener);
    }

    @Override
    public String getType() {
        return "SportGrpc";
    }

    @Override
    public String getIdentifier() {
        return _id;
    }

    @Override
    public void setLoad(double load) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public double getLoad() {
        return 0;
    }

    @Override
    public void setMode(TrainerMode mode) {
        // not implemented yet
    }

    @Override
    public TrainerMode getMode() {
        return TrainerMode.ERG;
    }

    @Override
    public void disconnect() {
        close();
    }

    @Override
    public void connect() {
        _channel = ManagedChannelBuilder.forTarget(_target)
                .usePlaintext()
                .build();
        _controllerStub = ControllerGrpc.newBlockingStub(_channel);

        Iterator<Sport.SportData> results = _controllerStub.readData(Sport.DataRequest.newBuilder().build());

        Runnable reader = () -> results
                .forEachRemaining(d -> {
                    String id = _id + ":" + d.getDeviceId();

                    checkDevice(id, d.getDeviceId());

                    if (d.hasPerformanceData()) {
                        Sport.PerformanceData pd = d.getPerformanceData();
                        switch (pd.getType()) {
                            case PERFORMANCE_TYPE_POWER:
                                _performancePublisher.setPower(id, (float) pd.getValue());
                                break;
                            case PERFORMANCE_TYPE_SPEED:
                                _performancePublisher.setSpeed(id, (float) pd.getValue());
                                break;
                            case PERFORMANCE_TYPE_CALIBRATION:
                                _performancePublisher.setCalibration(id, (float) pd.getValue());
                                break;
                            case PERFORMANCE_TYPE_HEART_RATE:
                                _performancePublisher.setExtHeartRate(id, (float) pd.getValue());
                                break;
                            case PERFORMANCE_TYPE_CADENCE:
                                _performancePublisher.setCadence(id, (float) pd.getValue());
                                break;
                        }
                    }
                    if (d.hasControlData()) {
                        Sport.ControlData cd = d.getControlData();
                        for (Sport.Button button : cd.getPressedList()) {
                            switch (button) {
                                case BUTTON_PLUS:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(id, new ControlData(ControlData.Type.PLUS)));
                                    break;
                                case BUTTON_MINUS:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(id, new ControlData(ControlData.Type.MINUS)));
                                    break;
                                case BUTTON_F1:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(id, new ControlData(ControlData.Type.START)));
                                    break;
                                case BUTTON_RESET:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(id, new ControlData(ControlData.Type.STOP)));
                                    break;
                            }
                        }
                    }
                });

        _readerThread = new Thread(() -> {
            try {
                reader.run();
            } catch (StatusRuntimeException e) {
                LOG.info("StatusException: " + e.getStatus());
            } catch( Exception e) {
                LOG.error("Unhandled reader exception", e);
            }
            LOG.info("reader completed");
        });
        _readerThread.start();
    }

    private void checkDevice(String id, String deviceId) {
        if (_controllers.containsKey(id)) {
            return;
        }

        WorkoutController controller = new WorkoutController(_controllerStub, id, deviceId);
        _controllers.put(id, controller);
        _discoveryPublisher.publishEvent(t -> t.handleWorkoutController(controller));
    }

    @Override
    public void close() {
        if (_channel == null) {
            return;
        }
        _controllerStub = null;
        _channel.shutdownNow();
        try {
            if (!_channel.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warn("Channel did not terminate within time limit");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        _readerThread = null;
    }

    @Override
    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _performancePublisher.addPerformanceDataListener(listener);
    }

    @Override
    public void monitorCheck() {

    }

    private static class WorkoutController implements IWorkoutController {
        private final ControllerGrpc.ControllerBlockingStub _controllerStub;
        private final String _id;
        private final String _deviceId;
        private int _load;

        public WorkoutController(ControllerGrpc.ControllerBlockingStub stub, String id, String deviceId) {
            _controllerStub = stub;
            _id = id;
            _deviceId = deviceId;
        }

        @Override
        public String getType() {
            return "SportGrpcController";
        }

        @Override
        public String getIdentifier() {
            return _id;
        }

        @Override
        public void setLoad(double load) {
            _load = (int) load;
            if (_controllerStub == null) {
                return;
            }
            _controllerStub.withDeadlineAfter(1, TimeUnit.SECONDS)
                    .setLoad(Sport.LoadRequest.newBuilder()
                            .setDeviceId(_deviceId)
                            .setTargetLoad(_load)
                            .build());
        }

        @Override
        public double getLoad() {
            return _load;
        }

        @Override
        public void setMode(TrainerMode mode) {
            // not implemented yet
        }

        @Override
        public TrainerMode getMode() {
            return TrainerMode.ERG;
        }

        @Override
        public void disconnect() throws IOException {

        }

        @Override
        public void connect() {

        }

        @Override
        public void close() throws IOException {

        }
    }
}
