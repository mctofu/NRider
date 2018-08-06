package nrider.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventPublisher<T> implements IEventPublisher<T> {
    private List<T> _listeners = new ArrayList<>();
    private static HashMap<String, Executor> _executorMap = new HashMap<>();

    private Executor _executor;

    public EventPublisher(Executor executor) {
        _executor = executor;
    }

    public static <T> EventPublisher<T> directPublisher() {
        return new EventPublisher<>(
                new Executor() {
                    public void execute(Runnable r) {
                        r.run();
                    }
                }
        );
    }

    public static <T> EventPublisher<T> singleThreadPublisher() {
        return new EventPublisher<>(Executors.newSingleThreadExecutor());
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

    public void removeListener(T listener) {
        synchronized (_listeners) {
            _listeners.remove(listener);
        }
    }

    public void publishEvent(final IEvent<T> event) {
        synchronized (_listeners) {
            for (final T listener : _listeners) {
                _executor.execute(
                        new Runnable() {
                            public void run() {
                                event.trigger(listener);
                            }
                        });
            }
        }
    }
}
