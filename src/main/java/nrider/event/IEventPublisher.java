package nrider.event;

public interface IEventPublisher<T> {
    void addListener(T listener);

    void publishEvent(IEvent<T> event);
}
