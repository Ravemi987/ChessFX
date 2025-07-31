package fr.chessproject.chessfx.helpers;

public class BitboardUtilities {

    public static String toBinaryString(long hexValue) {
        String binaryString = Long.toBinaryString(hexValue);
        return String.format("%64s", binaryString).replace(' ', '0');
    }

    public static String toHexString(long hexValue) {
        return String.format("0x%016XL", hexValue);
    }

    public static byte bitScanForward(long b) {
        return (b != 0) ? (byte)Long.numberOfTrailingZeros(b) : -1;
    }

    public static byte bitScanReverse(long b) {
        return (b != 0) ? (byte)(63 - Long.numberOfLeadingZeros(b)) : -1;
    }

    public static byte bitScan(long b, boolean isNegativeDir) {
        if (isNegativeDir) {
            return bitScanReverse(b);
        } else {
            return bitScanForward(b);
        }
    }

    public static void printBitboard(long bitboard)
    {
        System.out.print("\n");

        for (int rank = 7; rank >= 0; rank--)  {
            for (int file = 0; file < 8; file++)
            {
                int square = rank * 8 + file;

                if (file == 0)
                    System.out.printf("  %d ", 8 - rank);

                System.out.printf(" %d",  (bitboard & (1L << square)) != 0 ? 1 : 0);

            }
            System.out.print("\n");
        }
        System.out.print("\n     a b c d e f g h\n\n");
    }
}
