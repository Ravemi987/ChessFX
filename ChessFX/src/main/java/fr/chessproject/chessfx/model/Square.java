package fr.chessproject.chessfx.model;

public class Square {

    public static final byte NONE = -1;

    /* Important squares (castling capabilities) */

    public static final byte A1 = 0;
    public static final byte A8 = 56;
    public static final byte B1 = 1;
    public static final byte B8 = 57;
    public static final byte C1 = 2;
    public static final byte C8 = 58;
    public static final byte D1 = 3;
    public static final byte D8 = 59;
    public static final byte E1 = 4;
    public static final byte E8 = 60;
    public static final byte F1 = 5;
    public static final byte F8 = 61;
    public static final byte G1 = 6;
    public static final byte G8 = 62;
    public static byte H1 = 7;
    public static byte H8 = 63;

    /* Files masks */

    public static final long NOT_A_FILE = 0xFEFEFEFEFEFEFEFEL;
    public static final long NOT_H_FILE = 0x7F7F7F7F7F7F7F7FL;
    public static final long NOT_AB_FILES = 0xFCFCFCFCFCFCFCFCL;
    public static final long NOT_GH_FILES = 0x3F3F3F3F3F3F3F3FL;

    /* Ranks masks */

    public static final long RANK_3 = 0x0000000000FF0000L;
    public static final long RANK_6 = 0x0000FF0000000000L;

    public static long bitboardForSquare(byte sq) {
        return 0x1L << sq;
    }

    public static boolean isEmpty(byte sq, long occ) {
        return ((0x1L << sq) & occ) == 0;
    }
}
