package fr.chessproject.chessfx.helpers;

public class BinaryHelper {

    public static String toBinaryString(long hexValue) {
        String binaryString = Long.toBinaryString(hexValue);
        return String.format("%64s", binaryString).replace(' ', '0');
    }

    public static String toHexString(long hexValue) {
        return String.format("0x%016XL", hexValue);
    }

    public static byte bitScanForward(long b) {
        return (b != 0) ? (byte)Long.numberOfTrailingZeros(Long.lowestOneBit(b)) : 0;
    }

    public static byte bitScanReverse(long b) {
        return (b != 0) ? (byte)(63 - Long.numberOfLeadingZeros(Long.highestOneBit(b))) : 0;
    }

    public static byte bitScan(long b, boolean isNegativeDir) {
        if (isNegativeDir) {
            return bitScanReverse(b);
        } else {
            return bitScanForward(b);
        }
    }
}
