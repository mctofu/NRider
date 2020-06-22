package nrider.io;

import com.google.rpc.Code;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import nrider.event.EventPublisher;
import org.apache.log4j.Logger;
import sportgrpc.ControllerGrpc;
import sportgrpc.Sport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Connects to a sport-grpc service which can provide access to multiple workout controllers or data sources.
 */
public class SportGrpcController implements IPerformanceDataSource, IControlDataSource, IWorkoutController {
    private final static Logger LOG = Logger.getLogger(SportGrpcController.class);

    private final static Sport.LoadRequest CANCEL_REQUEST = Sport.LoadRequest.newBuilder()
            .setTargetLoad(-12345)
            .setDeviceId("cancel")
            .build();

    private final String _id;
    private final String _target;
    private final MultiDevicePerformancePublisher _performancePublisher = new MultiDevicePerformancePublisher();
    private final EventPublisher<IControlDataListener> _controlPublisher = EventPublisher.directPublisher();
    private final EventPublisher<IWorkoutControllerDiscoveryListener> _discoveryPublisher = EventPublisher.directPublisher();
    private final Map<String, IWorkoutController> _controllers = new HashMap<>();

    // Connection related
    private ManagedChannel _channel;
    private ControllerGrpc.ControllerBlockingStub _controllerStub;
    private Context.CancellableContext _controllerCtx;
    private Thread _readerThread;
    private Thread _writerThread;
    private BlockingQueue<Sport.LoadRequest> _loadQueue = new LinkedBlockingQueue<>(10);

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
        _controllerStub = ControllerGrpc.newBlockingStub(_channel)
                .withWaitForReady();
        _controllerCtx = Context.current().withCancellation();

        Runnable reader = () -> {
            while (!_channel.isShutdown()) {
                try {
                    readData(_controllerStub);
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.CANCELLED) {
                        LOG.info("reader cancelled");
                        return;
                    }
                    LOG.error("StatusException: " + e.getStatus());
                }
            }
        };

        _readerThread = new Thread(() -> {
            try {
                _controllerCtx.run(reader);
            } catch (Exception e) {
                LOG.error("Unhandled reader exception", e);
            } finally {
                _controllerCtx.cancel(null);
            }
            LOG.info("reader completed");
        });
        _readerThread.start();

        _writerThread = new Thread(() -> {
            while (true) {
                Sport.LoadRequest loadReq;

                try {
                    loadReq = _loadQueue.take();
                    if (loadReq == CANCEL_REQUEST) {
                        LOG.info("writer cancelled");
                        return;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    _controllerStub.withDeadlineAfter(1, TimeUnit.SECONDS)
                            .setLoad(loadReq);
                } catch (Exception e) {
                    LOG.error("setLoad", e);
                }
            }
        });
        _writerThread.start();
    }

    private void readData(ControllerGrpc.ControllerBlockingStub stub) {
        LOG.info("readData");
        Iterator<Sport.SportData> results = stub.readData(Sport.DataRequest.newBuilder().build());

        results.forEachRemaining(d -> {
            String id = _id + ":" + d.getDeviceId();

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
                        // TODO: should get some sort of capabilities info from sport-grpc?
                        checkDevice(id, d.getDeviceId());
                        _performancePublisher.setCalibration(id, (float) pd.getValue());
                        break;
                    case PERFORMANCE_TYPE_HEART_RATE:
                        _performancePublisher.setHeartRate(id, (float) pd.getValue());
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
    }

    private void checkDevice(String id, String deviceId) {
        if (_controllers.containsKey(id)) {
            return;
        }

        WorkoutController controller = new WorkoutController(_loadQueue, id, deviceId);
        _controllers.put(id, controller);
        _discoveryPublisher.publishEvent(t -> t.handleWorkoutController(controller));
    }

    @Override
    public void close() {
        if (_channel == null) {
            return;
        }
        _controllerStub = null;
        _channel.shutdown();
        // cancel reader
        if (_controllerCtx != null) {
            _controllerCtx.cancel(null);
        }
        // cancel writer
        try {
            _loadQueue.put(CANCEL_REQUEST);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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

    private static class WorkoutController implements IWorkoutController {
        private BlockingQueue<Sport.LoadRequest> _loadQueue;
        private final String _id;
        private final String _deviceId;
        private int _load;

        public WorkoutController(BlockingQueue<Sport.LoadRequest> loadQueue, String id, String deviceId) {
            _loadQueue = loadQueue;
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

            Sport.LoadRequest loadReq = Sport.LoadRequest.newBuilder()
                    .setDeviceId(_deviceId)
                    .setTargetLoad(_load)
                    .build();

            if (!_loadQueue.offer(loadReq)) {
                LOG.warn("load queue full");
            }
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
        public void disconnect() {

        }

        @Override
        public void connect() {

        }

        @Override
        public void close() {

        }
    }
}
