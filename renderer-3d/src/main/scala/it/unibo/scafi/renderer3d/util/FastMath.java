package it.unibo.scafi.renderer3d.util;

/**
 * Diamond's atan2 ( https://stackoverflow.com/questions/1427422/cheap-algorithm-to-find-measure-of-angle-between-vectors/14675998#14675998 )
 * Finalized code is from http://www.java-gaming.org/index.php?topic=38409.0
 * Using java because it seems to be a bit faster.
 * */
public class FastMath {

    private static final float PI = (float) Math.PI;
    private static final float PI_2 = PI / 2;

    public static float atan2(float y, float x) {
        float angle;
        if (y == 0f && x >= 0f) {
            return 0;
        } else if (y >= 0f) {
            if (x >= 0f) {
                angle = y / (x + y);
            } else {
                angle = 1f - x / (-x + y);
            }
        } else {
            if (x < 0f) {
                angle = -2f + y / (x + y);
            } else {
                angle = -1f + x / (x - y);
            }
        }
        return angle * PI_2;
    }
}