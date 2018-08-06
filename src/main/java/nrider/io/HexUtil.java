package nrider.io;

/**
 * utilities for dealing with base 16
 */
public class HexUtil {
    public static String toHexString(byte b) {
        String hex = Integer.toHexString((int) b & 0xFF);
        if (hex.length() == 1) {
            hex = "0" + hex;
        }
        return hex;
    }

}
