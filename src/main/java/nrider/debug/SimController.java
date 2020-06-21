package nrider.debug;

import nrider.event.EventPublisher;
import nrider.io.*;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SimController implements IWorkoutController, IPerformanceDataSource, IControlDataSource {
    private final String _identifier;
    private double _load;
    private TrainerMode _trainerMode;
    private boolean _active;
    private final EventPublisher<IPerformanceDataListener> _performancePublisher = EventPublisher.directPublisher();
    private final Timer _timer = new Timer();

    public SimController(String identifier) {
        _identifier = identifier;
        _timer.scheduleAtFixedRate(new DataOutputTask(), 0, 1000);
    }

    public String getType() {
        return "Simulator";
    }

    public String getIdentifier() {
        return _identifier;
    }

    public void setLoad(double load) {
        _load = load;
    }

    public double getLoad() {
        return _load;
    }

    public void setMode(TrainerMode mode) {
        _trainerMode = mode;
    }

    public TrainerMode getMode() {
        return _trainerMode;
    }

    public void disconnect() {
        _active = false;
    }

    public void connect() {
        _active = true;
    }

    public void close() {
        _active = false;
        _timer.cancel();
    }

    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _performancePublisher.addListener(listener);
    }

    public void addControlDataListener(IControlDataListener listener) {

    }

    public void publishPerformanceData(final PerformanceData data) {
        _performancePublisher.publishEvent(
                t -> t.handlePerformanceData(getIdentifier(), data)
        );
    }

    class DataOutputTask extends TimerTask {
        private double _currentPower;
        private static final double _currentSpeed = 21 / 2.237;


        @Override
        public void run() {
            if (_active) {
                if (_currentPower != _load) {
                    _currentPower += (_load - _currentPower) / 2;
                }

                publishPerformanceData(new PerformanceData(PerformanceData.Type.POWER, (float) _currentPower));
                publishPerformanceData(new PerformanceData(PerformanceData.Type.SPEED, (float) (_currentSpeed + ((new Random().nextFloat() * 2 - 1) / 2.237))));
            }
        }
    }
}
