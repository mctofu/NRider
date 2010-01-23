package nrider.io.ant;

import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.IPerformanceDataListener;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 28, 2009
 * Time: 11:46:05 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseHandler implements IMessageHandler
{
    private EventPublisher<IPerformanceDataListener> _eventPublisher;

    public BaseHandler( EventPublisher<IPerformanceDataListener> publisher )
    {
        _eventPublisher = publisher;
    }

    public void publishEvent( IEvent<IPerformanceDataListener> event )
    {
        _eventPublisher.publishEvent( event );
    }

}
