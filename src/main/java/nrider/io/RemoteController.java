package nrider.io;

import computrainer.ControllerGrpc;
import computrainer.ControllerOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import nrider.event.EventPublisher;
import nrider.monitor.IMonitorable;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Connects to a controller service implementing the grpc Controller service
 */
public class RemoteController implements IPerformanceDataSource, IControlDataSource, IWorkoutController, IMonitorable {
    private final String _id;
    private final ManagedChannel _channel;
    private final ControllerGrpc.ControllerBlockingStub _controllerStub;
    private final PerformanceDataChangePublisher _performancePublisher;
    private final EventPublisher<IControlDataListener> _controlPublisher = EventPublisher.directPublisher();
    private int _load;

    public RemoteController(String target, String id) {
        _id = id;
        _channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        _controllerStub = ControllerGrpc.newBlockingStub(_channel);
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
        _controllerStub.setLoad(ControllerOuterClass.LoadRequest.newBuilder()
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
    public void disconnect() throws IOException {
    }

    @Override
    public void connect() {
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

        new Thread(reader).start();
    }

    @Override
    public void close() throws IOException {
        try {
            _channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _performancePublisher.addPerformanceDataListener(listener);
    }

    @Override
    public void monitorCheck() {

    }
}
