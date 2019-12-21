/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibo.scafi.renderer3d.util;

/**
 * This class implements some of Math's methods but in an optimized and approximated way, keeping the same method
 * signatures. Using java because it seems to be a bit faster.
 * */
public class FastMath {

    private static final double PI_OVER_2 = Math.PI / 2.0;
    private static final float PI_2 = (float) PI_OVER_2;

    private FastMath(){}

    /**
     * Diamond's implementation of atan2:
     * https://stackoverflow.com/questions/1427422/cheap-algorithm-to-find-measure-of-angle-between-vectors/14675998#14675998
     * Code is from http://www.java-gaming.org/index.php?topic=38409.0
     * @param y the y component
     * @param x the x component
     * @return the value that approximates Math.atan2(y, x)
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
}