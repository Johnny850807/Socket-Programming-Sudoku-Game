package p2p;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface OpCodes {
    /**
     * [1 OpCodes][1 n:= Name's length][n Name]
     */
    byte SUBMIT_NAME = -100;

    /**
     * [1 OpCodes][1 p:= length of puzzled points][p all of puzzled points]
     * [p/2 all of puzzled points' numbers]
     * all of puzzled points:
     *   Every puzzled point's row and col sharing one byte [4 bits row | 4 bits col]
     * all of puzzled points' numbers:
     *   Pair every 2 puzzled points (a, b) to share one byte [4 bits|4 bits], e.g. (8, 5) ==> [1000 0101]
     */
    byte GAME_STARTED = -101;

    /**
     * [1 OpCodes][1 row][1 col][1 number]
     */
    byte FILL_IN_NUMBER = -102;

    /**
     * [1 OpCodes]
     */
    byte GAME_OVER = -103;

}
