package nrider.net;

import nrider.core.IWorkoutListener;
import nrider.event.EventPublisher;
import nrider.io.IPerformanceDataListener;
import nrider.io.IPerformanceDataSource;

public abstract class NetSource implements IPerformanceDataSource, IPerformanceDataListener, IWorkoutListener {
    private EventPublisher<IPerformanceDataListener> _dataPublisher = EventPublisher.singleThreadPublisher(NetSource.class.getName());
    private EventPublisher<IWorkoutListener> _workoutPublisher = EventPublisher.singleThreadPublisher(NetSource.class.getName());

    public void addPerformanceDataListener(IPerformanceDataListener listener) {
        _dataPublisher.addListener(listener);
    }

    public void addWorkoutListener(IWorkoutListener listener) {
        _workoutPublisher.addListener(listener);
    }

    protected EventPublisher<IPerformanceDataListener> getDataPublisher() {
        return _dataPublisher;
    }


}
