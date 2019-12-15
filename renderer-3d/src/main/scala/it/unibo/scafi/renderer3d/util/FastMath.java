package it.unibo.scafi.renderer3d.util;

public class FastMath {

    private static final float PI = (float) Math.PI;
    private static final float PI_2 = PI / 2;
    private static double PI_OVER_2 = Math.PI / 2.0;

    /**
     * Diamond's atan2 ( https://stackoverflow.com/questions/1427422/cheap-algorithm-to-find-measure-of-angle-between-vectors/14675998#14675998 )
     * Finalized code is from http://www.java-gaming.org/index.php?topic=38409.0
     * Using java because it seems to be a bit faster.
     * */
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

    public static double acos( double x ) {
        return PI_OVER_2 - asin( x );
    }

    /** Code is from:
     * https://github.com/metsci/glimpse/blob/master/util/src/main/java/com/metsci/glimpse/util/math/fast/PolynomialApprox.java
     * */
    public static double asin( double x ) {
        boolean isNeg = x < 0;
        x = Math.abs(x);

        double y1 = x * ( -.0170881256 + ( x * ( .0066700901 + ( x * -.0012624911 ) ) ) );
        double y2 = x * ( -.0501743046 + ( x * ( .0308918810 + y1 ) ) );
        double y = 1.5707963050 + ( x * ( -.2145988016 + ( x * ( .0889789874 + y2 ) ) ) );
        double theta = PI_OVER_2 - ( Math.sqrt( 1.0 - x ) * y );

        if(isNeg){
            theta = -theta;
        }
        return theta;
    }
}