package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.helpers.BinaryHelper;

public class Piece {

    /* Pieces */

    public static final byte NONE = 0;
    public static final byte WHITE_KING = 1;
    public static final byte WHITE_QUEEN = 2;
    public static final byte WHITE_BISHOP = 3;
    public static final byte WHITE_KNIGHT = 4;
    public static final byte WHITE_ROOK = 5;
    public static final byte WHITE_PAWN = 6;
    public static final byte BLACK_KING = 7;
    public static final byte BLACK_QUEEN = 8;
    public static final byte BLACK_BISHOP = 9;
    public static final byte BLACK_KNIGHT = 10;
    public static final byte BLACK_ROOK = 11;
    public static final byte BLACK_PAWN = 12;

    /* Directions */

    private static final int noEa = 0;
    private static final int east = 1;
    private static final int soEa = 2;
    private static final int south = 3;
    private static final int soWe = 4;
    private static final int west = 5;
    private static final int noWe = 6;
    private static final int north = 7;

    /* Pre-computation arrays */

    //private final long[][] rayAttacks = new long[8][64];
    public static final long[][] rayAttacks = new long[][]{
            {0x8040201008040200L, 0x0080402010080400L, 0x0000804020100800L, 0x0000008040201000L, 0x0000000080402000L, 0x0000000000804000L, 0x0000000000008000L, 0x0000000000000000L, 0x4020100804020000L, 0x8040201008040000L, 0x0080402010080000L, 0x0000804020100000L, 0x0000008040200000L, 0x0000000080400000L, 0x0000000000800000L, 0x0000000000000000L, 0x2010080402000000L, 0x4020100804000000L, 0x8040201008000000L, 0x0080402010000000L, 0x0000804020000000L, 0x0000008040000000L, 0x0000000080000000L, 0x0000000000000000L, 0x1008040200000000L, 0x2010080400000000L, 0x4020100800000000L, 0x8040201000000000L, 0x0080402000000000L, 0x0000804000000000L, 0x0000008000000000L, 0x0000000000000000L, 0x0804020000000000L, 0x1008040000000000L, 0x2010080000000000L, 0x4020100000000000L, 0x8040200000000000L, 0x0080400000000000L, 0x0000800000000000L, 0x0000000000000000L, 0x0402000000000000L, 0x0804000000000000L, 0x1008000000000000L, 0x2010000000000000L, 0x4020000000000000L, 0x8040000000000000L, 0x0080000000000000L, 0x0000000000000000L, 0x0200000000000000L, 0x0400000000000000L, 0x0800000000000000L, 0x1000000000000000L, 0x2000000000000000L, 0x4000000000000000L, 0x8000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L},
            {0x00000000000000FEL, 0x00000000000000FCL, 0x00000000000000F8L, 0x00000000000000F0L, 0x00000000000000E0L, 0x00000000000000C0L, 0x0000000000000080L, 0x0000000000000000L, 0x000000000000FE00L, 0x000000000000FC00L, 0x000000000000F800L, 0x000000000000F000L, 0x000000000000E000L, 0x000000000000C000L, 0x0000000000008000L, 0x0000000000000000L, 0x0000000000FE0000L, 0x0000000000FC0000L, 0x0000000000F80000L, 0x0000000000F00000L, 0x0000000000E00000L, 0x0000000000C00000L, 0x0000000000800000L, 0x0000000000000000L, 0x00000000FE000000L, 0x00000000FC000000L, 0x00000000F8000000L, 0x00000000F0000000L, 0x00000000E0000000L, 0x00000000C0000000L, 0x0000000080000000L, 0x0000000000000000L, 0x000000FE00000000L, 0x000000FC00000000L, 0x000000F800000000L, 0x000000F000000000L, 0x000000E000000000L, 0x000000C000000000L, 0x0000008000000000L, 0x0000000000000000L, 0x0000FE0000000000L, 0x0000FC0000000000L, 0x0000F80000000000L, 0x0000F00000000000L, 0x0000E00000000000L, 0x0000C00000000000L, 0x0000800000000000L, 0x0000000000000000L, 0x00FE000000000000L, 0x00FC000000000000L, 0x00F8000000000000L, 0x00F0000000000000L, 0x00E0000000000000L, 0x00C0000000000000L, 0x0080000000000000L, 0x0000000000000000L, 0xFE00000000000000L, 0xFC00000000000000L, 0xF800000000000000L, 0xF000000000000000L, 0xE000000000000000L, 0xC000000000000000L, 0x8000000000000000L, 0x0000000000000000L},
            {0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000002L, 0x0000000000000004L, 0x0000000000000008L, 0x0000000000000010L, 0x0000000000000020L, 0x0000000000000040L, 0x0000000000000080L, 0x0000000000000000L, 0x0000000000000204L, 0x0000000000000408L, 0x0000000000000810L, 0x0000000000001020L, 0x0000000000002040L, 0x0000000000004080L, 0x0000000000008000L, 0x0000000000000000L, 0x0000000000020408L, 0x0000000000040810L, 0x0000000000081020L, 0x0000000000102040L, 0x0000000000204080L, 0x0000000000408000L, 0x0000000000800000L, 0x0000000000000000L, 0x0000000002040810L, 0x0000000004081020L, 0x0000000008102040L, 0x0000000010204080L, 0x0000000020408000L, 0x0000000040800000L, 0x0000000080000000L, 0x0000000000000000L, 0x0000000204081020L, 0x0000000408102040L, 0x0000000810204080L, 0x0000001020408000L, 0x0000002040800000L, 0x0000004080000000L, 0x0000008000000000L, 0x0000000000000000L, 0x0000020408102040L, 0x0000040810204080L, 0x0000081020408000L, 0x0000102040800000L, 0x0000204080000000L, 0x0000408000000000L, 0x0000800000000000L, 0x0000000000000000L, 0x0002040810204080L, 0x0004081020408000L, 0x0008102040800000L, 0x0010204080000000L, 0x0020408000000000L, 0x0040800000000000L, 0x0080000000000000L, 0x0000000000000000L},
            {0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000001L, 0x0000000000000002L, 0x0000000000000004L, 0x0000000000000008L, 0x0000000000000010L, 0x0000000000000020L, 0x0000000000000040L, 0x0000000000000080L, 0x0000000000000101L, 0x0000000000000202L, 0x0000000000000404L, 0x0000000000000808L, 0x0000000000001010L, 0x0000000000002020L, 0x0000000000004040L, 0x0000000000008080L, 0x0000000000010101L, 0x0000000000020202L, 0x0000000000040404L, 0x0000000000080808L, 0x0000000000101010L, 0x0000000000202020L, 0x0000000000404040L, 0x0000000000808080L, 0x0000000001010101L, 0x0000000002020202L, 0x0000000004040404L, 0x0000000008080808L, 0x0000000010101010L, 0x0000000020202020L, 0x0000000040404040L, 0x0000000080808080L, 0x0000000101010101L, 0x0000000202020202L, 0x0000000404040404L, 0x0000000808080808L, 0x0000001010101010L, 0x0000002020202020L, 0x0000004040404040L, 0x0000008080808080L, 0x0000010101010101L, 0x0000020202020202L, 0x0000040404040404L, 0x0000080808080808L, 0x0000101010101010L, 0x0000202020202020L, 0x0000404040404040L, 0x0000808080808080L, 0x0001010101010101L, 0x0002020202020202L, 0x0004040404040404L, 0x0008080808080808L, 0x0010101010101010L, 0x0020202020202020L, 0x0040404040404040L, 0x0080808080808080L},
            {0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000001L, 0x0000000000000002L, 0x0000000000000004L, 0x0000000000000008L, 0x0000000000000010L, 0x0000000000000020L, 0x0000000000000040L, 0x0000000000000000L, 0x0000000000000100L, 0x0000000000000201L, 0x0000000000000402L, 0x0000000000000804L, 0x0000000000001008L, 0x0000000000002010L, 0x0000000000004020L, 0x0000000000000000L, 0x0000000000010000L, 0x0000000000020100L, 0x0000000000040201L, 0x0000000000080402L, 0x0000000000100804L, 0x0000000000201008L, 0x0000000000402010L, 0x0000000000000000L, 0x0000000001000000L, 0x0000000002010000L, 0x0000000004020100L, 0x0000000008040201L, 0x0000000010080402L, 0x0000000020100804L, 0x0000000040201008L, 0x0000000000000000L, 0x0000000100000000L, 0x0000000201000000L, 0x0000000402010000L, 0x0000000804020100L, 0x0000001008040201L, 0x0000002010080402L, 0x0000004020100804L, 0x0000000000000000L, 0x0000010000000000L, 0x0000020100000000L, 0x0000040201000000L, 0x0000080402010000L, 0x0000100804020100L, 0x0000201008040201L, 0x0000402010080402L, 0x0000000000000000L, 0x0001000000000000L, 0x0002010000000000L, 0x0004020100000000L, 0x0008040201000000L, 0x0010080402010000L, 0x0020100804020100L, 0x0040201008040201L},
            {0x0000000000000000L, 0x0000000000000001L, 0x0000000000000003L, 0x0000000000000007L, 0x000000000000000FL, 0x000000000000001FL, 0x000000000000003FL, 0x000000000000007FL, 0x0000000000000000L, 0x0000000000000100L, 0x0000000000000300L, 0x0000000000000700L, 0x0000000000000F00L, 0x0000000000001F00L, 0x0000000000003F00L, 0x0000000000007F00L, 0x0000000000000000L, 0x0000000000010000L, 0x0000000000030000L, 0x0000000000070000L, 0x00000000000F0000L, 0x00000000001F0000L, 0x00000000003F0000L, 0x00000000007F0000L, 0x0000000000000000L, 0x0000000001000000L, 0x0000000003000000L, 0x0000000007000000L, 0x000000000F000000L, 0x000000001F000000L, 0x000000003F000000L, 0x000000007F000000L, 0x0000000000000000L, 0x0000000100000000L, 0x0000000300000000L, 0x0000000700000000L, 0x0000000F00000000L, 0x0000001F00000000L, 0x0000003F00000000L, 0x0000007F00000000L, 0x0000000000000000L, 0x0000010000000000L, 0x0000030000000000L, 0x0000070000000000L, 0x00000F0000000000L, 0x00001F0000000000L, 0x00003F0000000000L, 0x00007F0000000000L, 0x0000000000000000L, 0x0001000000000000L, 0x0003000000000000L, 0x0007000000000000L, 0x000F000000000000L, 0x001F000000000000L, 0x003F000000000000L, 0x007F000000000000L, 0x0000000000000000L, 0x0100000000000000L, 0x0300000000000000L, 0x0700000000000000L, 0x0F00000000000000L, 0x1F00000000000000L, 0x3F00000000000000L, 0x7F00000000000000L},
            {0x0000000000000000L, 0x0000000000000100L, 0x0000000000010200L, 0x0000000001020400L, 0x0000000102040800L, 0x0000010204081000L, 0x0001020408102000L, 0x0102040810204000L, 0x0000000000000000L, 0x0000000000010000L, 0x0000000001020000L, 0x0000000102040000L, 0x0000010204080000L, 0x0001020408100000L, 0x0102040810200000L, 0x0204081020400000L, 0x0000000000000000L, 0x0000000001000000L, 0x0000000102000000L, 0x0000010204000000L, 0x0001020408000000L, 0x0102040810000000L, 0x0204081020000000L, 0x0408102040000000L, 0x0000000000000000L, 0x0000000100000000L, 0x0000010200000000L, 0x0001020400000000L, 0x0102040800000000L, 0x0204081000000000L, 0x0408102000000000L, 0x0810204000000000L, 0x0000000000000000L, 0x0000010000000000L, 0x0001020000000000L, 0x0102040000000000L, 0x0204080000000000L, 0x0408100000000000L, 0x0810200000000000L, 0x1020400000000000L, 0x0000000000000000L, 0x0001000000000000L, 0x0102000000000000L, 0x0204000000000000L, 0x0408000000000000L, 0x0810000000000000L, 0x1020000000000000L, 0x2040000000000000L, 0x0000000000000000L, 0x0100000000000000L, 0x0200000000000000L, 0x0400000000000000L, 0x0800000000000000L, 0x1000000000000000L, 0x2000000000000000L, 0x4000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L},
            {0x0101010101010100L, 0x0202020202020200L, 0x0404040404040400L, 0x0808080808080800L, 0x1010101010101000L, 0x2020202020202000L, 0x4040404040404000L, 0x8080808080808000L, 0x0101010101010000L, 0x0202020202020000L, 0x0404040404040000L, 0x0808080808080000L, 0x1010101010100000L, 0x2020202020200000L, 0x4040404040400000L, 0x8080808080800000L, 0x0101010101000000L, 0x0202020202000000L, 0x0404040404000000L, 0x0808080808000000L, 0x1010101010000000L, 0x2020202020000000L, 0x4040404040000000L, 0x8080808080000000L, 0x0101010100000000L, 0x0202020200000000L, 0x0404040400000000L, 0x0808080800000000L, 0x1010101000000000L, 0x2020202000000000L, 0x4040404000000000L, 0x8080808000000000L, 0x0101010000000000L, 0x0202020000000000L, 0x0404040000000000L, 0x0808080000000000L, 0x1010100000000000L, 0x2020200000000000L, 0x4040400000000000L, 0x8080800000000000L, 0x0101000000000000L, 0x0202000000000000L, 0x0404000000000000L, 0x0808000000000000L, 0x1010000000000000L, 0x2020000000000000L, 0x4040000000000000L, 0x8080000000000000L, 0x0100000000000000L, 0x0200000000000000L, 0x0400000000000000L, 0x0800000000000000L, 0x1000000000000000L, 0x2000000000000000L, 0x4000000000000000L, 0x8000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L},
    };

    //private final long[] kingAttacksTableStatic = new long[64];
    public static final long[] kingAttacksTableStatic = new long[]
            {
                    0x0000000000000302L, 0x0000000000000705L, 0x0000000000000E0AL, 0x0000000000001C14L, 0x0000000000003828L, 0x0000000000007050L, 0x000000000000E0A0L, 0x000000000000C040L,
                    0x0000000000030203L, 0x0000000000070507L, 0x00000000000E0A0EL, 0x00000000001C141CL, 0x0000000000382838L, 0x0000000000705070L, 0x0000000000E0A0E0L, 0x0000000000C040C0L,
                    0x0000000003020300L, 0x0000000007050700L, 0x000000000E0A0E00L, 0x000000001C141C00L, 0x0000000038283800L, 0x0000000070507000L, 0x00000000E0A0E000L, 0x00000000C040C000L,
                    0x0000000302030000L, 0x0000000705070000L, 0x0000000E0A0E0000L, 0x0000001C141C0000L, 0x0000003828380000L, 0x0000007050700000L, 0x000000E0A0E00000L, 0x000000C040C00000L,
                    0x0000030203000000L, 0x0000070507000000L, 0x00000E0A0E000000L, 0x00001C141C000000L, 0x0000382838000000L, 0x0000705070000000L, 0x0000E0A0E0000000L, 0x0000C040C0000000L,
                    0x0003020300000000L, 0x0007050700000000L, 0x000E0A0E00000000L, 0x001C141C00000000L, 0x0038283800000000L, 0x0070507000000000L, 0x00E0A0E000000000L, 0x00C040C000000000L,
                    0x0302030000000000L, 0x0705070000000000L, 0x0E0A0E0000000000L, 0x1C141C0000000000L, 0x3828380000000000L, 0x7050700000000000L, 0xE0A0E00000000000L, 0xC040C00000000000L,
                    0x0203000000000000L, 0x0507000000000000L, 0x0A0E000000000000L, 0x141C000000000000L, 0x2838000000000000L, 0x5070000000000000L, 0xA0E0000000000000L, 0x40C0000000000000L
            };

    // private final long[] knightAttacksTableStatic = new long[64];
    public static final long[] knightAttacksTableStatic = new long[]
            {
                    0x0000000000020400L, 0x0000000000050800L, 0x00000000000A1100L, 0x0000000000142200L, 0x0000000000284400L, 0x0000000000508800L, 0x0000000000A01000L, 0x0000000000402000L,
                    0x0000000002040004L, 0x0000000005080008L, 0x000000000A110011L, 0x0000000014220022L, 0x0000000028440044L, 0x0000000050880088L, 0x00000000A0100010L, 0x0000000040200020L,
                    0x0000000204000402L, 0x0000000508000805L, 0x0000000A1100110AL, 0x0000001422002214L, 0x0000002844004428L, 0x0000005088008850L, 0x000000A0100010A0L, 0x0000004020002040L,
                    0x0000020400040200L, 0x0000050800080500L, 0x00000A1100110A00L, 0x0000142200221400L, 0x0000284400442800L, 0x0000508800885000L, 0x0000A0100010A000L, 0x0000402000204000L,
                    0x0002040004020000L, 0x0005080008050000L, 0x000A1100110A0000L, 0x0014220022140000L, 0x0028440044280000L, 0x0050880088500000L, 0x00A0100010A00000L, 0x0040200020400000L,
                    0x0204000402000000L, 0x0508000805000000L, 0x0A1100110A000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xA0100010A0000000L, 0x4020002040000000L,
                    0x0400040200000000L, 0x0800080500000000L, 0x1100110A00000000L, 0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L, 0x100010A000000000L, 0x2000204000000000L,
                    0x0004020000000000L, 0x0008050000000000L, 0x00110A0000000000L, 0x0022140000000000L, 0x0044280000000000L, 0x0088500000000000L, 0x0010A00000000000L, 0xFFFFC00000000000L
            };


    /* One Step Only // Generalized Shift */

    private static final int[] shiftOffset = new int[]{9, 1, -7, -8, -9, -1, 7, 8};
    /*
        Les fous se deplacent en diagonale : valeurs de decalage 9 et 7. Pour les mouvements vers le haut, il faut
        utiliser un decalage a gauche (<<) et pour ceux vers le bas, un decalage a droite (>>). Pour simplifier,
        Le fonctionnement est le meme pour les tours avec les valeurs 1 et 8. Pour simplifier, on peut donc creer
        un tableau qui contient toutes les directions possibles. Si la valeur est positive, on effectue un shift gauche,
        sinon, on effectue un shit droit sans signe avec la valeur opposee : bitboard >>> -direction.
      */

    private static final long[] avoidWrap = new long[] {
            0xFEFEFEFEFEFEFE00L,
            0xFEFEFEFEFEFEFEFEL,
            0x00FEFEFEFEFEFEFEL,
            0x00FFFFFFFFFFFFFFL,
            0x007F7F7F7F7F7F7FL,
            0x7F7F7F7F7F7F7F7FL,
            0x7F7F7F7F7F7F7F00L,
            0xFFFFFFFFFFFFFF00L
    };

    /*
        En theorie, deux masks seulement sont necessaires : NOT_A_FILE et NOT_H_FILE car les debordements vers le haut
        et vers le bas sont traites naturellement par les proprietes des nombres binaires : sur un entier de 64bits, un
        decalage a gauche (multiplication par une puissance de 2), fait disparaitre le bit car le nombre devient
        trop grand (> 2^64). De meme, un decalage a droite (division par une puissance de 2) fait disparaitre le bit
        car le nombre devient trop petit (inferieur a 2^0).
        En pratique, utiliser plus de masks permet d'elaguer les deplacements impossibles avant d'effectuer le nombre
        maximum d'operations.
     */

    /*
        6 == noWe -> +7     7 == north -> +8    0 == noEa -> +9
        0x7F7F7F7F7F7F7F00  0xFFFFFFFFFFFFFF00  0xFEFEFEFEFEFEFE00
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        . . . . . . . .     . . . . . . . .     . . . . . . . .

        5 == west -> -1                         1 == east -> +1
        0x7F7F7F7F7F7F7F7F                      0xFEFEFEFEFEFEFEFE
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .                         . 1 1 1 1 1 1 1

        4 == soWe -> -9     3 == south -> -8    2 == soEa -> -7
        0x007F7F7F7F7F7F7F  0x00FFFFFFFFFFFFFF  0x00FEFEFEFEFEFEFE
        . . . . . . . .     . . . . . . . .     . . . . . . . .
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
        1 1 1 1 1 1 1 .     1 1 1 1 1 1 1 1     . 1 1 1 1 1 1 1
     */

    private static long shift(long b, int dir8) {
        if (shiftOffset[dir8] > 0) {
            return (b << shiftOffset[dir8]) & avoidWrap[dir8];
        } else {
            return (b >>> -shiftOffset[dir8]) & avoidWrap[dir8];
        }
    }

    /* Pre-computation methods */

    // Function to initialize rays[][] bitboard
    public static void precomputeRays() {
        System.out.print("private final long[][] rayAttacks = new long[][]{");
        for (int dir = 0; dir < 8; dir++) {
            System.out.print("{");
            for (byte square = 0; square < 64; square++) {
                rayAttacks[dir][square] = computeRay(dir, square);
                System.out.print(BinaryHelper.toHexString(rayAttacks[dir][square]) + ", ");
            }
            System.out.println("},");
        }
        System.out.print("}");
    }

    // Compute the ray from a given square in a specific direction
    private static long computeRay(int dir8, byte square) {
        long bitboard = 0L;
        long pos = Square.bitboardForSquare(square);

        while (pos != 0) {
            long nextPos = shift(pos, dir8);
            bitboard |= nextPos;
            pos = nextPos;
        }
        return bitboard;
    }

    // Precompute all king moves
    public static void precomputeKingMoves() {
        System.out.print("private final long[] kingAttacksTableStatic = new long[]{");
        for (byte square = 0; square < 64; square++) {
            kingAttacksTableStatic[square] = computeKingMoves(square);
            System.out.print(BinaryHelper.toHexString(kingAttacksTableStatic[square]) + ", ");
        }
        System.out.print("}");
    }

    // Compute moves for the king from a given square
    private static long computeKingMoves(byte square) {
        long bitboard = 0L;
        long pos = Square.bitboardForSquare(square);

        for (int dir = 0; dir < 8; dir++) {
            long nextPos = shift(pos, dir);
            bitboard |= nextPos;
        }
        return bitboard;
    }

    // Precompute all knight moves
    public static void precomputeKnightMoves() {
        System.out.print("private final long[] knightAttacksTableStatic = new long[]{");
        for (byte square = 0; square < 64; square++) {
            knightAttacksTableStatic[square] = computeKnightMoves(square);
            System.out.print(BinaryHelper.toHexString(knightAttacksTableStatic[square]) + ", ");
        }
        System.out.print("}");
    }

    // Compute moves for the knight from a given square
    private static long computeKnightMoves(byte square) {
        long bitboard = 0L;
        long pos = Square.bitboardForSquare(square);
        bitboard |= (pos & Square.NOT_GH_FILES) << 10 | (pos & Square.NOT_H_FILE) << 17 | (pos & Square.NOT_AB_FILES) << 6 | (pos & Square.NOT_A_FILE) << 15;
        bitboard |= (pos & Square.NOT_GH_FILES) >> 6 | (pos & Square.NOT_H_FILE) >> 15  | (pos & Square.NOT_AB_FILES) >> 10 | (pos & Square.NOT_A_FILE) >> 17 ;
        return bitboard;
    }

    /* Attacks methods */

    private static long getRayAttacks(long occupied, int dir8, byte square) {
        long attacks = rayAttacks[dir8][square];
        long blocker = attacks & occupied;
        if (blocker != 0) {
            byte bSquare = BinaryHelper.bitScan(blocker, isNegative(dir8));
            attacks ^= rayAttacks[dir8][bSquare];
        }
        return attacks;
    }

    private static boolean isNegative(int dir) {
        return shiftOffset[dir] < 0;
    }

    /* Line attacks */

    private static long diagonalAttacks(long occ, byte sq) {
        return getRayAttacks(occ, noEa, sq) | getRayAttacks(occ, soWe, sq);
    }

    private static long antiDiagAttacks(long occ, byte sq) {
        return getRayAttacks(occ, noWe, sq) | getRayAttacks(occ, soEa, sq);
    }

    private static long fileAttacks(long occ, byte sq) {
        return getRayAttacks(occ, north, sq) | getRayAttacks(occ, south, sq);
    }

    private static long rankAttacks(long occ, byte sq) {
        return getRayAttacks(occ, east, sq) | getRayAttacks(occ, west, sq);
    }

    /* Piece attacks */

    public static long bishopAttacks(long occ, byte sq) {
        return diagonalAttacks(occ, sq) | antiDiagAttacks(occ, sq);
    }

    public static long rookAttacks(long occ, byte sq) {
        return fileAttacks(occ, sq) | rankAttacks(occ, sq);
    }

    public static long queenAttacks(long occ, byte sq) {
        return rookAttacks(occ, sq) | bishopAttacks(occ, sq);
    }

    public static long knightAttacks(byte square) {
        return knightAttacksTableStatic[square];
    }

    public static long kingAttacks(byte square) {
        return kingAttacksTableStatic[square];
    }


    public static byte fromChar(char chr) {
        int index = " KQBNRPkqbnrp".indexOf(chr);
        return (index < 1) ? NONE : (byte)index;
    }


    public static boolean isWhite(byte piece) {
        return piece > 0 && piece < 7;
    }

    public static boolean isPawn(byte piece) {
        return piece == WHITE_PAWN || piece == BLACK_PAWN;
    }

    public static boolean isKnight(byte piece) {
        return piece == WHITE_KNIGHT || piece == BLACK_KNIGHT;
    }

    public static boolean isBishop(byte piece) {
        return piece == WHITE_BISHOP || piece == BLACK_BISHOP;
    }

    public static boolean isRook(byte piece) {
        return piece == WHITE_ROOK || piece == BLACK_ROOK;
    }

    public static boolean isQueen(byte piece) {
        return piece == WHITE_QUEEN || piece == BLACK_QUEEN;
    }
}
