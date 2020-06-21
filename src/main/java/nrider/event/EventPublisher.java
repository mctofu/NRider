package nrider.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventPublisher<T> implements IEventPublisher<T> {
    private final List<T> _listeners = new ArrayList<>();
    private static final HashMap<String, Executor> _executorMap = new HashMap<>();

    private final Executor _executor;

    public EventPublisher(Executor executor) {
        _executor = executor;
    }

    public static <T> EventPublisher<T> directPublisher() {
        return new EventPublisher<>(Runnable::run);
    }

    public static <T> EventPublisher<T> singleThreadPublisher(String executorName) {
        Executor executor;
        synchronized (_executorMap) {
            if (!_executorMap.containsKey(executorName)) {
                _executorMap.put(executorName, Executors.newSingleThreadExecutor());
            }
            executor = _executorMap.get(executorName);
        }
        return new EventPublisher<>(executor);
    }

    public void addListener(T listener) {
        synchronized (_listeners) {
            _listeners.add(listener);
        }
    }

    public void publishEvent(final IEvent<T> event) {
        synchronized (_listeners) {
            for (final T listener : _listeners) {
                _executor.execute(() -> event.trigger(listener));
            }
        }
    }
}
