package nrider.io;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 29, 2009
 * Time: 12:26:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class HexUtil
{
    public static String toHexString( byte b )
    {
        return Integer.toHexString( (int) b & 0xFF );
    }

}
