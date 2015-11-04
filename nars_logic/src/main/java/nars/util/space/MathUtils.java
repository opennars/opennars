/*
 *   __               .__       .__  ._____.           
 * _/  |_  _______  __|__| ____ |  | |__\_ |__   ______
 * \   __\/  _ \  \/  /  |/ ___\|  | |  || __ \ /  ___/
 *  |  | (  <_> >    <|  \  \___|  |_|  || \_\ \\___ \ 
 *  |__|  \____/__/\_ \__|\___  >____/__||___  /____  >
 *                   \/       \/             \/     \/ 
 *
 * Copyright (c) 2006-2011 Karsten Schmidt
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * http://creativecommons.org/licenses/LGPL/2.1/
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package nars.util.space;

import java.util.Random;

/**
 * Miscellaneous math utilities.
 */
public final class MathUtils {

    public static final float THIRD = 1f / 3;

    /**
     * Square root of 2
     */
    public static final float SQRT2 = (float) Math.sqrt(2);

    /**
     * Square root of 3
     */
    public static final float SQRT3 = (float) Math.sqrt(3);

    /**
     * Log(2)
     */
    public static final float LOG2 = (float) Math.log(2);

    /**
     * PI
     */
    public static final float PI = 3.14159265358979323846f;

    /**
     * The reciprocal of PI: (1/PI)
     */
    public static final float INV_PI = 1f / PI;

    /**
     * PI/2
     */
    public static final float HALF_PI = PI / 2;

    /**
     * PI/3
     */
    public static final float THIRD_PI = PI / 3;

    /**
     * PI/4
     */
    public static final float QUARTER_PI = PI / 4;

    /**
     * PI*2
     */
    public static final float TWO_PI = PI * 2;

    /**
     * PI*1.5
     */
    public static final float THREE_HALVES_PI = TWO_PI - HALF_PI;

    /**
     * PI*PI
     */
    public static final float PI_SQUARED = PI * PI;

    /**
     * Epsilon value
     */
    public static final float EPS = 1.1920928955078125E-7f;

    /**
     * Degrees to radians conversion factor
     */
    public static final float DEG2RAD = PI / 180;

    /**
     * Radians to degrees conversion factor
     */
    public static final float RAD2DEG = 180 / PI;

    private static final float SHIFT23 = 1 << 23;

    private static final float INV_SHIFT23 = 1.0f / SHIFT23;
    private final static double SIN_A = -4d / (PI * PI);

    private final static double SIN_B = 4d / PI;
    private final static double SIN_P = 9d / 40;
    /**
     * Default random number generator used by random methods of this class
     * which don't use a passed in {@link Random} instance.
     */
    public static Random RND = new Random();

    /**
     * @param x
     * @return absolute value of x
     */
    public static final double abs(double x) {
        return x < 0 ? -x : x;
    }

    /**
     * @param x
     * @return absolute value of x
     */
    public static final float abs(float x) {
        return x < 0 ? -x : x;
    }

    /**
     * @param x
     * @return absolute value of x
     */
    public static final int abs(int x) {
        int y = x >> 31;
        return (x ^ y) - y;
    }

    /**
     * Rounds up the value to the nearest higher power^2 value.
     * 
     * @param x
     * @return power^2 value
     */
    public static final int ceilPowerOf2(int x) {
        int pow2 = 1;
        while (pow2 < x) {
            pow2 <<= 1;
        }
        return pow2;
    }

    public static final double clip(double a, double min, double max) {
        return a < min ? min : (a > max ? max : a);
    }

    public static final float clip(float a, float min, float max) {
        return a < min ? min : (a > max ? max : a);
    }

    public static final int clip(int a, int min, int max) {
        return a < min ? min : (a > max ? max : a);
    }

    public static double clipNormalized(double a) {
        if (a < 0) {
            return 0;
        } else if (a > 1) {
            return 1;
        }
        return a;
    }

    /**
     * Clips the value to the 0.0 .. 1.0 interval.
     * 
     * @param a
     * @return clipped value
     * @since 0012
     */
    public static final float clipNormalized(float a) {
        if (a < 0) {
            return 0;
        } else if (a > 1) {
            return 1;
        }
        return a;
    }

    public static final double cos(final double theta) {
        return sin(theta + HALF_PI);
    }

    /**
     * Returns fast cosine approximation of a value. Note: code from <a
     * href="http://wiki.java.net/bin/view/Games/JeffGems">wiki posting on
     * java.net by jeffpk</a>
     * 
     * @param theta
     *            angle in radians.
     * @return cosine of theta.
     */
    public static final float cos(final float theta) {
        return sin(theta + HALF_PI);
    }

    public static final double degrees(double radians) {
        return radians * RAD2DEG;
    }

    public static final float degrees(float radians) {
        return radians * RAD2DEG;
    }

    public static double dualSign(double a, double b) {
        double x = (a >= 0 ? a : -a);
        return (b >= 0 ? x : -x);
    }

    /**
     * Fast cosine approximation.
     * 
     * @param x
     *            angle in -PI/2 .. +PI/2 interval
     * @return cosine
     */
    public static final double fastCos(final double x) {
        return fastSin(x + ((x > HALF_PI) ? -THREE_HALVES_PI : HALF_PI));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static final float fastInverseSqrt(float x) {
        float half = 0.5F * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f375a86 - (i >> 1);
        x = Float.intBitsToFloat(i);
        return x * (1.5F - half * x * x);
    }

    /**
     * Computes a fast approximation to <code>Math.pow(a, b)</code>. Adapted
     * from http://www.dctsystems.co.uk/Software/power.html.
     * 
     * @param a
     *            a positive number
     * @param b
     *            a number
     * @return a^b
     * 
     */
    public static final float fastPow(float a, float b) {
        float x = Float.floatToRawIntBits(a);
        x *= INV_SHIFT23;
        x -= 127;
        float y = x - (x >= 0 ? (int) x : (int) x - 1);
        b *= x + (y - y * y) * 0.346607f;
        y = b - (b >= 0 ? (int) b : (int) b - 1);
        y = (y - y * y) * 0.33971f;
        return Float.intBitsToFloat((int) ((b + 127 - y) * SHIFT23));
    }

    /**
     * Fast sine approximation.
     * 
     * @param x
     *            angle in -PI/2 .. +PI/2 interval
     * @return sine
     */
    public static final double fastSin(double x) {
        // float B = 4/pi;
        // float C = -4/(pi*pi);
        // float y = B * x + C * x * abs(x);
        // y = P * (y * abs(y) - y) + y;
        x = SIN_B * x + SIN_A * x * abs(x);
        return SIN_P * (x * abs(x) - x) + x;
    }

    public static final boolean flipCoin() {
        return RND.nextBoolean();
    }

    public static final boolean flipCoin(Random rnd) {
        return rnd.nextBoolean();
    }

    public static final long floor(double x) {
        long y = (long) x;
        if (x < 0 && x != y) {
            y--;
        }
        return y;
    }

    /**
     * This method is a *lot* faster than using (int)Math.floor(x).
     * 
     * @param x
     *            value to be floored
     * @return floored value as integer
     * @since 0012
     */
    public static final int floor(float x) {
        int y = (int) x;
        if (x < 0 && x != y) {
            y--;
        }
        return y;
    }

    /**
     * Rounds down the value to the nearest lower power^2 value.
     * 
     * @param x
     * @return power^2 value
     */
    public static final int floorPowerOf2(int x) {
        return (int) Math.pow(2, (int) (Math.log(x) / LOG2));
    }

    /**
     * Computes the Greatest Common Devisor of integers p and q.
     *
     * @param p
     * @param q
     * @return gcd
     */
    public static final int gcd(int p, int q) {
        while (true) {
            if (q == 0) {
                return p;
            }
            p = q;
            q = p % q;
        }
    }

    /**
     * Creates a single normalized impulse signal with its peak at t=1/k. The
     * attack and decay period is configurable via the k parameter. Code from:
     * http://www.iquilezles.org/www/articles/functions/functions.htm
     * 
     * @param k
     *            smoothness
     * @param t
     *            time position (should be >= 0)
     * @return impulse value (as double)
     */
    public static double impulse(double k, double t) {
        double h = k * t;
        return h * Math.exp(1.0 - h);
    }

    /**
     * Creates a single normalized impulse signal with its peak at t=1/k. The
     * attack and decay period is configurable via the k parameter. Code from:
     * http://www.iquilezles.org/www/articles/functions/functions.htm
     * 
     * @param k
     *            smoothness
     * @param t
     *            time position (should be >= 0)
     * @return impulse value (as float)
     */
    public static float impulse(float k, float t) {
        float h = k * t;
        return (float) (h * Math.exp(1.0f - h));
    }

    public static final int lcm(int p, int q) {
        return abs(p * q) / gcd(p, q);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double mapInterval(double x, double minIn, double maxIn,
            double minOut, double maxOut) {
        return minOut + (maxOut - minOut) * (x - minIn) / (maxIn - minIn);
    }

    public static float mapInterval(float x, float minIn, float maxIn,
            float minOut, float maxOut) {
        return minOut + (maxOut - minOut) * (x - minIn) / (maxIn - minIn);
    }

    public static final double max(double a, double b) {
        return a > b ? a : b;
    }

    public static final double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    public static final double max(double[] values) {
        return max(values[0], values[1], values[2]);
    }

    public static final float max(float a, float b) {
        return a > b ? a : b;
    }

    /**
     * Returns the maximum value of three floats.
     * 
     * @param a
     * @param b
     * @param c
     * @return max val
     */
    public static final float max(float a, float b, float c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    public static final float max(float[] values) {
        return max(values[0], values[1], values[2]);
    }

    public static final int max(int a, int b) {
        return a > b ? a : b;
    }

    /**
     * Returns the maximum value of three ints.
     * 
     * @param a
     * @param b
     * @param c
     * @return max val
     */
    public static final int max(int a, int b, int c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    public static final int max(int[] values) {
        return max(values[0], values[1], values[2]);
    }

    public static final double min(double a, double b) {
        return a < b ? a : b;
    }

    public static final double min(double a, double b, double c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    public static final float min(float a, float b) {
        return a < b ? a : b;
    }

    /**
     * Returns the minimum value of three floats.
     * 
     * @param a
     * @param b
     * @param c
     * @return min val
     */
    public static final float min(float a, float b, float c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    public static final int min(int a, int b) {
        return a < b ? a : b;
    }

    /**
     * Returns the minimum value of three ints.
     * 
     * @param a
     * @param b
     * @param c
     * @return min val
     */
    public static final int min(int a, int b, int c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    /**
     * Returns a random number in the interval -1 .. +1.
     * 
     * @return random float
     */
    public static final float normalizedRandom() {
        return RND.nextFloat() * 2 - 1;
    }

    /**
     * Returns a random number in the interval -1 .. +1 using the {@link Random}
     * instance provided.
     * 
     * @return random float
     */
    public static final float normalizedRandom(Random rnd) {
        return rnd.nextFloat() * 2 - 1;
    }

    public static double radians(double degrees) {
        return degrees * DEG2RAD;
    }

    public static final float radians(float degrees) {
        return degrees * DEG2RAD;
    }

    public static final float random(float max) {
        return RND.nextFloat() * max;
    }

    public static final float random(float min, float max) {
        return RND.nextFloat() * (max - min) + min;
    }

    public static final int random(int max) {
        return (int) (RND.nextFloat() * max);
    }

    public static final int random(int min, int max) {
        return (int) (RND.nextFloat() * (max - min)) + min;
    }

    public static final double random(Random rnd, double max) {
        return rnd.nextDouble() * max;
    }

    public static final double random(Random rnd, double min, double max) {
        return rnd.nextDouble() * (max - min) + min;
    }

    public static final float random(Random rnd, float max) {
        return rnd.nextFloat() * max;
    }

    public static final float random(Random rnd, float min, float max) {
        return rnd.nextFloat() * (max - min) + min;
    }

    public static final int random(Random rnd, int max) {
        return rnd.nextInt((int) max);
    }

    public static final int random(Random rnd, int min, int max) {
        return rnd.nextInt((int) (max - min)) + min;
    }

    public static final boolean randomChance(double chance) {
        return RND.nextDouble() < chance;
    }

    public static final boolean randomChance(float chance) {
        return RND.nextFloat() < chance;
    }

    public static final boolean randomChance(Random rnd, double chance) {
        return rnd.nextDouble() < chance;
    }

    public static final boolean randomChance(Random rnd, float chance) {
        return rnd.nextFloat() < chance;
    }

    public static final double reduceAngle(double theta) {
        theta %= TWO_PI;
        if (abs(theta) > PI) {
            theta = theta - TWO_PI;
        }
        if (abs(theta) > HALF_PI) {
            theta = PI - theta;
        }
        return theta;
    }

    /**
     * Reduces the given angle into the -PI/4 ... PI/4 interval for faster
     * computation of sin/cos. This method is used by {@link #sin(float)} &
     * {@link #cos(float)}.
     * 
     * @param theta
     *            angle in radians
     * @return reduced angle
     * @see #sin(float)
     * @see #cos(float)
     */
    public static final float reduceAngle(float theta) {
        theta %= TWO_PI;
        if (abs(theta) > PI) {
            theta = theta - TWO_PI;
        }
        if (abs(theta) > HALF_PI) {
            theta = PI - theta;
        }
        return theta;
    }

    /**
     * Rounds a double precision value to the given precision.
     * 
     * @param val
     * @param prec
     * @return rounded value
     */
    public static final double roundTo(double val, double prec) {
        return floor(val / prec + 0.5) * prec;
    }

    /**
     * Rounds a single precision value to the given precision.
     * 
     * @param val
     * @param prec
     * @return rounded value
     */
    public static final float roundTo(float val, float prec) {
        return floor(val / prec + 0.5f) * prec;
    }

    /**
     * Rounds an integer value to the given precision.
     * 
     * @param val
     * @param prec
     * @return rounded value
     */
    public static final int roundTo(int val, int prec) {
        return floor((float) val / prec + 0.5f) * prec;
    }

    /**
     * Sets the default Random number generator for this class. This generator
     * is being reused by all future calls to random() method versions which
     * don't explicitly ask for a {@link Random} instance to be used.
     * 
     * @param rnd
     */
    public static void setDefaultRandomGenerator(Random rnd) {
        RND = rnd;
    }

    public static int sign(double x) {
        return x < 0 ? -1 : (x > 0 ? 1 : 0);
    }

    public static int sign(float x) {
        return x < 0 ? -1 : (x > 0 ? 1 : 0);
    }

    public static int sign(int x) {
        return x < 0 ? -1 : (x > 0 ? 1 : 0);
    }

    public static final double sin(double theta) {
        theta = reduceAngle(theta);
        if (abs(theta) <= QUARTER_PI) {
            return (float) fastSin(theta);
        }
        return (float) fastCos(HALF_PI - theta);
    }

    /**
     * Returns a fast sine approximation of a value. Note: code from <a
     * href="http://wiki.java.net/bin/view/Games/JeffGems">wiki posting on
     * java.net by jeffpk</a>
     * 
     * @param theta
     *            angle in radians.
     * @return sine of theta.
     */
    public static final float sin(float theta) {
        theta = reduceAngle(theta);
        if (abs(theta) <= QUARTER_PI) {
            return (float) fastSin(theta);
        }
        return (float) fastCos(HALF_PI - theta);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static final float sqrt(float x) {
        x = fastInverseSqrt(x);
        if (x > 0) {
            return 1.0f / x;
        } else {
            return 0;
        }
    }
}