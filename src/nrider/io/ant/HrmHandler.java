package nrider.io.ant;

import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.HexUtil;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 28, 2009
 * Time: 11:41:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class HrmHandler extends BaseHandler
{
    private final static Logger LOG = Logger.getLogger( HrmHandler.class );
	private String _id;

    public HrmHandler( EventPublisher<IPerformanceDataListener> publisher )
    {
        super( publisher );
    }

    public void handleMessage( AntData antData )
    {
        byte[] data = antData.getData();
        StringBuilder sb = new StringBuilder( "hrm:" );
        sb.append( HexUtil.toHexString( data[0] ) );
        sb.append( HexUtil.toHexString( data[1] ) );
        sb.append( HexUtil.toHexString( data[2] ) );
        sb.append( HexUtil.toHexString( data[3] ) );
		final String id = sb.toString();
        StringBuilder debug = new StringBuilder();
        debug.append( "SEQ: " + ( (int) data[6] & 0xFF ) );
        debug.append( " HR: " + ( (int) data[7] & 0xFF ) );
        debug.append( " RRD: " + ( ( (int) data[4] & 0xFF ) + ( (int) data[5] & 0xFF ) * 256 ) );
        debug.append( " ID: " + id );

        LOG.debug(debug);
        final PerformanceData pd = new PerformanceData();
        pd.setType( PerformanceData.Type.EXT_HEART_RATE );
        pd.setValue( (int) data[7] & 0xFF );

		if( id.equals( _id ) )
		{
			publishEvent( new IEvent<IPerformanceDataListener>()
				{
					public void trigger( IPerformanceDataListener target )
					{
						target.handlePerformanceData( id, pd );
					}
				});
		}
		else
		{
			if( _id != null )
			{
				LOG.warn( "Inconsistent id on channel " + antData.getChannelId() + " " + _id + "/" + id );
			}
			_id = id;
		}
    }
}
