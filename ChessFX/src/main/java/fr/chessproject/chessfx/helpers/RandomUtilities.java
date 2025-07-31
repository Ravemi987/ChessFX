package fr.chessproject.chessfx.helpers;

public class RandomUtilities {

    public static int random_state = 1804289383;

    public static int getRandom32Bits() {
        int number = random_state;

        number ^= number << 13;
        number ^= number >> 17;
        number ^= number << 5;

        random_state = number;

        return number;
    }

    public static long getRandom64Bits() {
        long n1, n2, n3, n4;

        n1 = (long)(getRandom32Bits()) & 0xFFFF;
        n2 = (long)(getRandom32Bits()) & 0xFFFF;
        n3 = (long)(getRandom32Bits()) & 0xFFFF;
        n4 = (long)(getRandom32Bits()) & 0xFFFF;

        return n1 | (n2 << 16) | (n3 << 32) | (n4 << 48);
    }

    public static void resetSeed() {
        random_state = 1804289383;
    }
}
