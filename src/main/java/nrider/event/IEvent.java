package nrider.event;

public interface IEvent<T> {
    void trigger(T target);
}
