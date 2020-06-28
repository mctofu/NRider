package nrider.io;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import nrider.event.EventPublisher;
import org.apache.log4j.Logger;
import sportgrpc.ControllerGrpc;
import sportgrpc.Sport;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Connects to a sport-grpc service which can provide access to multiple workout controllers or data sources.
 */
public class SportGrpcController implements IPerformanceDataSource, IControlDataSource, Closeable {
    private final static Logger LOG = Logger.getLogger(SportGrpcController.class);

    interface WriterAction {
        void run(ControllerGrpc.ControllerBlockingStub controller) throws Exception;
    }

    private final static WriterAction CANCEL_ACTION = c -> {};

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
    private BlockingQueue<WriterAction> _writerQueue = new LinkedBlockingQueue<>(10);

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
    public String getIdentifier() {
        return _id;
    }

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
                WriterAction action;

                try {
                    action = _writerQueue.take();
                    if (action == CANCEL_ACTION) {
                        LOG.info("writer cancelled");
                        return;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    action.run(_controllerStub.withDeadlineAfter(1, TimeUnit.SECONDS));
                } catch (Exception e) {
                    LOG.error("action failed", e);
                }
            }
        });
        _writerThread.start();
    }

    private void readData(ControllerGrpc.ControllerBlockingStub stub) {
        LOG.info("readData loop started");
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
                        case BUTTON_F2:
                            _controlPublisher.publishEvent(l -> l.handleControlData(id, new ControlData(ControlData.Type.RECALIBRATE)));
                    }
                }
            }
        });
    }

    private void checkDevice(String id, String deviceId) {
        if (_controllers.containsKey(id)) {
            return;
        }

        WorkoutController controller = new WorkoutController(_writerQueue, id, deviceId);
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
            _writerQueue.put(CANCEL_ACTION);
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
        private BlockingQueue<WriterAction> _writerQueue;
        private final String _id;
        private final String _deviceId;
        private int _load;

        public WorkoutController(BlockingQueue<WriterAction> writerQueue, String id, String deviceId) {
            _writerQueue = writerQueue;
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

            WriterAction action = c -> {
                Sport.LoadRequest loadReq = Sport.LoadRequest.newBuilder()
                        .setDeviceId(_deviceId)
                        .setTargetLoad(_load)
                        .build();

                c.setLoad(loadReq);
            };

            if (!_writerQueue.offer(action)) {
                LOG.warn("discarding setLoad request: action queue full");
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
        public void recalibrate() {
            WriterAction action = c -> {
                Sport.RecalibrateRequest loadReq = Sport.RecalibrateRequest.newBuilder()
                        .setDeviceId(_deviceId)
                        .build();

                c.recalibrate(loadReq);
            };

            if (!_writerQueue.offer(action)) {
                LOG.warn("discarding recalibrate request: action queue full");
            }
        }

        @Override
        public void close() {

        }
    }
}
