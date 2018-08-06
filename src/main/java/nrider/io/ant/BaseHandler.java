package nrider.io.ant;

import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.IPerformanceDataListener;
import nrider.monitor.IMonitorable;

public abstract class BaseHandler implements IMessageHandler, IMonitorable {
    private EventPublisher<IPerformanceDataListener> _eventPublisher;
    private volatile long _lastMessageTime;
    private int _lastSeq = -1;
    private volatile boolean _receiving;

    public BaseHandler(EventPublisher<IPerformanceDataListener> publisher) {
        _eventPublisher = publisher;
    }

    public void publishEvent(IEvent<IPerformanceDataListener> event) {
        registerMessage();
        _eventPublisher.publishEvent(event);
    }

    protected boolean isReceiving() {
        return _receiving;
    }

    protected void setReceiving(boolean receiving) {
        _receiving = receiving;
    }

    protected long getLastMessageTime() {
        return _lastMessageTime;
    }

    protected void registerMessage() {
        _lastMessageTime = System.currentTimeMillis();
    }

    protected int getLastSeq() {
        return _lastSeq;
    }

    protected void setLastSeq(int lastSeq) {
        _lastSeq = lastSeq;
    }

    protected abstract void sendZero();

    public void monitorCheck() {
        if (_receiving) {
            if (System.currentTimeMillis() - _lastMessageTime > 3000) {
                sendZero();
            }
        }
    }
}
