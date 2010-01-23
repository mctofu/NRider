package nrider.io.ant;

import nrider.event.EventPublisher;
import nrider.event.IEvent;
import nrider.io.IPerformanceDataListener;
import nrider.io.PerformanceData;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 29, 2009
 * Time: 12:27:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class PowerHandler extends BaseHandler
{
    private final static Logger LOG = Logger.getLogger( PowerHandler.class );

    private int _powerTrack;
    private int _seqTrack;    

    public PowerHandler( EventPublisher<IPerformanceDataListener> publisher )
    {
        super( publisher );
    }

    public void handleMessage( AntData antData )
    {
        byte[] data = antData.getData();

        StringBuilder pDebug = new StringBuilder();
        pDebug.append( "5: " + ( (int) data[1] & 0xFF ) );
//						System.out.print( " 6: " + ( (int) msg[6] & 0xFF ) );
//						System.out.print( " 7: " + ( (int) msg[7] & 0xFF ) );
        pDebug.append( " 8: " + ( (int) data[4] & 0xFF ) );
        pDebug.append( " 9: " + ( (int) data[5] & 0xFF ) );
        pDebug.append( " 10: " + ( (int) data[6] & 0xFF ) );
        pDebug.append( " 11: " + ( (int) data[7] & 0xFF ) );
        pDebug.append( " 10/11: " + ( ( (int) data[6] & 0xFF ) + ( (int) data[7] & 0xFF ) * 256 ) );
        pDebug.append( " 9/10: " + ( ( (int) data[5] & 0xFF ) + ( (int) data[6] & 0xFF ) * 256 ) );
        pDebug.append( " 8/9: " + ( ( (int) data[4] & 0xFF ) + ( (int) data[5] & 0xFF ) * 256 ) );
//						System.out.print( " 7/8: " + ( ( (int) msg[7] & 0xFF ) + ( (int) msg[8] & 0xFF ) * 256 ) );
//						System.out.print( " RRD: " + ( ( (int) msg[8] & 0xFF ) + ( (int) msg[9] & 0xFF ) * 256 ) );
        LOG.debug( pDebug );
        if( data[0] == 0x12 )
        {
            int powerRRD = ( ( (int) data[6] & 0xFF ) + ( (int) data[7] & 0xFF ) * 256 );
            int power = powerRRD - _powerTrack;
            int seq = ( (int) data[1] & 0xFF );
            if( seq - _seqTrack > 0 )
            {
                power = power / (seq - _seqTrack);
            }
            _seqTrack = seq;
            _powerTrack = powerRRD;

            final PerformanceData pd2 = new PerformanceData();
            pd2.setType( PerformanceData.Type.EXT_CADENCE );
            pd2.setValue( (int) data[3] & 0xFF );
            publishEvent( new IEvent<IPerformanceDataListener>()
                {
                public void trigger( IPerformanceDataListener target )
                {
                    target.handlePerformanceData( "pwr", pd2 );
                }
            }      );

            final PerformanceData powerData = new PerformanceData();
            powerData.setType( PerformanceData.Type.EXT_POWER );
            powerData.setValue( power / 3 );
            publishEvent( new IEvent<IPerformanceDataListener>()
                {
                public void trigger( IPerformanceDataListener target )
                {
                    target.handlePerformanceData( "pwr", powerData );
                }
            }      );

        }

    }
}
