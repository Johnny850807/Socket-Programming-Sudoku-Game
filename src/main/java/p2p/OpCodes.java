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
     * [1 OpCodes][...]
     */
    byte GAME_STARTED = -101;

    /**
     * [1 OpCodes][1 row][1 col][1 number]
     */
    byte FILL_IN_NUMBER = -102;

    /**
     * [1 OpCodes][1 Timeout Player's number]
     */
    byte TIME_OUT = -104;

    /**
     * [1 OpCodes]
     */
    byte GAME_OVER = -103;

}
