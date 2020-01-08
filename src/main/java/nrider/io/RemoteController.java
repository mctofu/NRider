package nrider.io;

import computrainer.ControllerGrpc;
import computrainer.ControllerOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import nrider.event.EventPublisher;
import nrider.monitor.IMonitorable;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Connects to a controller service implementing the grpc Controller service
 */
public class RemoteController implements IPerformanceDataSource, IControlDataSource, IWorkoutController, IMonitorable {
    private final static Logger LOG = Logger.getLogger(ComputrainerController.class);

    private final String _id;
    private final String _target;
    private final PerformanceDataChangePublisher _performancePublisher;
    private final EventPublisher<IControlDataListener> _controlPublisher = EventPublisher.directPublisher();
    private ManagedChannel _channel;
    private ControllerGrpc.ControllerBlockingStub _controllerStub;
    private Thread _readerThread;
    private int _load;

    public RemoteController(String target, String id) {
        _target = target;
        _id = id;
        _performancePublisher = new PerformanceDataChangePublisher(id);
    }

    @Override
    public void addControlDataListener(IControlDataListener listener) {
        _controlPublisher.addListener(listener);
    }

    @Override
    public String getType() {
        return "Remote";
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
                .setLoad(ControllerOuterClass.LoadRequest.newBuilder()
                .setTargetLoad(_load)
                .build());
    }

    @Override
    public double getLoad() {
        return _load;
    }

    @Override
    public void setMode(TrainerMode mode) {

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

        Iterator<ControllerOuterClass.ControllerData> results = _controllerStub.getData(ControllerOuterClass.DataRequest.newBuilder().build());

        Runnable reader = () -> results
                .forEachRemaining(d -> {
                    if (d.hasPerformanceData()) {
                        ControllerOuterClass.PerformanceData pd = d.getPerformanceData();
                        switch (pd.getType()) {
                            case POWER:
                                _performancePublisher.setPower(pd.getValue());
                                break;
                            case SPEED:
                                _performancePublisher.setSpeed(pd.getValue());
                                break;
                            case CALIBRATION:
                                _performancePublisher.setCalibration(pd.getValue());
                                break;
                        }
                    }
                    if (d.hasControlData()) {
                        ControllerOuterClass.ControlData cd = d.getControlData();
                        for (ControllerOuterClass.ControlData.Button button : cd.getPressedList()) {
                            switch (button) {
                                case PLUS:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(_id, new ControlData(ControlData.Type.PLUS)));
                                    break;
                                case MINUS:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(_id, new ControlData(ControlData.Type.MINUS)));
                                    break;
                                case F1:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(_id, new ControlData(ControlData.Type.START)));
                                    break;
                                case RESET:
                                    _controlPublisher.publishEvent(l -> l.handleControlData(_id, new ControlData(ControlData.Type.STOP)));
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
}
