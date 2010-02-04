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

    private int _rTrack;
    private int _pTrack;
	private int _tTrack;
	private int _seqTrack;

    public PowerHandler( EventPublisher<IPerformanceDataListener> publisher )
    {
        super( publisher );
    }

    public void handleMessage( AntData antData )
    {
        byte[] data = antData.getData();

        if( data[0] == 0x12 )
        {
			int rRRD = (int) data[2] & 0xFF;
			int r = rRRD - _rTrack;
			if( rRRD < _rTrack )
			{
				r += 256;
			}
			_rTrack = rRRD;

            int pRRD = ( ( (int) data[4] & 0xFF ) + ( (int) data[5] & 0xFF ) * 256 );
            int p = pRRD - _pTrack;
			if( pRRD < _pTrack )
			{
				p += 65536;
			}
			_pTrack = pRRD;

			int tRRD = ( ( (int) data[6] & 0xFF ) + ( (int) data[7] & 0xFF ) * 256 );
			int t = tRRD - _tTrack;
			if( tRRD < _tTrack )
			{
				t += 65536;
			}
			_tTrack = tRRD;

            int seqRRD = ( (int) data[1] & 0xFF );
			int seqDiff = seqRRD - _seqTrack;
			_seqTrack = seqRRD;

			int msgCadence = (int) data[3] & 0xFF;

			double force = t / ( r * 32 );
			double cadence = r * 122880.0 / p;
			double watts = cadence * force * 2 * Math.PI / 60;

			StringBuilder pDebug = new StringBuilder();
			pDebug.append( "seq: " + seqRRD );
			pDebug.append( "r: " + rRRD );
			pDebug.append( " p: " + pRRD );
			pDebug.append( " t: " + tRRD );
			pDebug.append( " cad: " + msgCadence );
			pDebug.append( " watt: " + watts );
			LOG.debug( pDebug );

            final PerformanceData pd2 = new PerformanceData();
            pd2.setType( PerformanceData.Type.EXT_CADENCE );
            pd2.setValue( (float) cadence );
            publishEvent( new IEvent<IPerformanceDataListener>()
                {
                public void trigger( IPerformanceDataListener target )
                {
                    target.handlePerformanceData( "pwr", pd2 );
                }
            }      );

            final PerformanceData powerData = new PerformanceData();
            powerData.setType( PerformanceData.Type.EXT_POWER );
            powerData.setValue( (float) watts );
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
