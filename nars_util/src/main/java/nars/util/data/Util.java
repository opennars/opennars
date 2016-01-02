/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.data;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.function.Consumer;

/**
 *
 *
 *
 */
public class Util {


    /**
     * syntactic complexity of the compound, the sum of those of its term
     * plus 1
     * TODO make final again
     */


    protected Util() {
    }

    public static final int PRIME2 = 92821;


    /**
     * Fetch the Unsafe.  Use With Caution.
     */
    public static Unsafe getUnsafe() {
        // Not on bootclasspath
        if (Util.class.getClassLoader() == null)
            return Unsafe.getUnsafe();
        try {
            Field fld = Unsafe.class.getDeclaredField("theUnsafe");
            fld.setAccessible(true);
            return (Unsafe) fld.get(Util.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not obtain access to sun.misc.Unsafe", e);
        }
    }
    public static int hash(int a, int b) {
        return PRIME2 * (PRIME2 + a) + b;
    }

    public static int hash(int a, int b, int c) {
        return PRIME2 * (PRIME2 * (PRIME2 + a) + b) + c;
    }

    public static int hash(Object a, Object b) {
        return hash(a.hashCode(), b.hashCode());
    }

    public static int hash(Object a, Object b, Object c) {
        return hash(a.hashCode(), b.hashCode(), c.hashCode());
    }

    public static void assertNotNull(Object test, String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
    }



/*
 **************************************************************************
 *                                                                        *
 *          General Purpose Hash Function Algorithms Library              *
 *                                                                        *
 * Author: Arash Partow - 2002                                            *
 * URL: http://www.partow.net                                             *
 * URL: http://www.partow.net/programming/hashfunctions/index.html        *
 *                                                                        *
 * Copyright notice:                                                      *
 * Free use of the General Purpose Hash Function Algorithms Library is    *
 * permitted under the guidelines and in accordance with the most current *
 * version of the Common Public License.                                  *
 * http://www.opensource.org/licenses/cpl1.0.php                          *
 *                                                                        *
 **************************************************************************
*/


    /*class GeneralHashFunctionLibrary
    {*/


    public static long RSHash(String str) {
        int b = 378551;
        int a = 63689;
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = hash * a + str.charAt(i);
            a = a * b;
        }

        return hash;
    }
   /* End Of RS Hash Function */


    public static long JSHash(String str) {
        long hash = 1315423911;

        for (int i = 0; i < str.length(); i++) {
            hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
        }

        return hash;
    }
   /* End Of JS Hash Function */


    public static long PJWHash(String str) {
        long BitsInUnsignedInt = (4 * 8);
        long ThreeQuarters = (long) ((BitsInUnsignedInt * 3) / 4);
        long OneEighth = (long) (BitsInUnsignedInt / 8);
        long HighBits = (0xFFFFFFFFL) << (BitsInUnsignedInt - OneEighth);
        long hash = 0;
        long test;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash << OneEighth) + str.charAt(i);

            if ((test = hash & HighBits) != 0) {
                hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
            }
        }

        return hash;
    }
   /* End Of  P. J. Weinberger Hash Function */


    public static long ELFHash(String str) {
        long hash = 0;
        long x;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 4) + str.charAt(i);

            if ((x = hash & 0xF0000000L) != 0) {
                hash ^= (x >> 24);
            }
            hash &= ~x;
        }

        return hash;
    }


    /** from clojure.Util */
    public static int hashCombine(int seed, int hash) {
        return seed ^ ( hash + 0x9e3779b9 + (seed << 6) + (seed >> 2) );

        //return seed * 31 + hash;
    }



    public static int ELFHashNonZero(byte[] str, int seed) {
        int i  = (int) ELFHash(str, seed);
        if (i == 0) i = 1;
        return i;
    }

    public static long ELFHash(byte[] str, long seed) {

        long hash = seed;

        for (byte aStr : str) {
            hash = (hash << 4) + aStr;

            long x;
            if ((x = hash & 0xF0000000L) != 0) {
                hash ^= (x >> 24);
            }
            hash &= ~x;
        }

        return hash;
    }




    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;


    /**
     * linear interpolate between target & current, factor is between 0 and 1.0
     */
    public static float lerp(float target, float current, float factor) {
        return target * factor + current * (1.0f - factor);
    }
    public static double lerp(double target, double current, double factor) {
        return target * factor + current * (1.0f - factor);
    }
    /**
     * maximum, simpler and faster than Math.max without its additional tests
     */
    public static float max(float a, float b) {
        return (a > b) ? a : b;
    }

    public static float mean(float a, float b) {
        return (a + b) * 0.5f;
    }


    public static short f2s(float conf) {
        return (short) (conf * Short.MAX_VALUE);
    }

    public static byte f2b(float conf) {
        return (byte) (conf * Byte.MAX_VALUE);
    }

    /**
     * removal rates are approximately monotonically increasing function;
     * tests first, mid and last for this  ordering
     * first items are highest, so it is actually descending order
     * TODO improve accuracy
     */
    public static boolean isSemiMonotonicallyDec(double[] count) {


        int cl = count.length;
        return
                (count[0] >= count[cl - 1]) &&
                        (count[cl / 2] >= count[cl - 1]);
    }

    /* TODO improve accuracy */
    public static boolean isSemiMonotonicallyInc(int[] count) {

        int cl = count.length;
        return
                (count[0] <= count[cl - 1]) &&
                        (count[cl / 2] <= count[cl - 1]);
    }

    /**
     * Generic utility method for running a list of tasks in current thread
     */
    public static void run(Deque<Runnable> tasks) {
        run(tasks, tasks.size(), Runnable::run);
    }

    public static void run(Deque<Runnable> tasks, int maxTasksToRun, Consumer<Runnable> runner) {
        while (!tasks.isEmpty() && maxTasksToRun-- > 0) {
            runner.accept( tasks.removeFirst() );
        }
    }

    /**
     * clamps a value to 0..1 range
     */
    public static float clamp(float p) {
        if (p > 1.0f)
            return 1.0f;
        if (p < 0.0f)
            return 0.0f;
        return p;
    }

    /**
     * discretizes values to nearest finite resolution real number determined by epsilon spacing
     */
    public static float round(float value, float epsilon) {
        return clamp(Math.round(value / epsilon) * epsilon);
    }

    public static int hash(float f, int discretness) {
        return (int) (f * discretness);
    }

    public static boolean equals(double a, double b) {
        return equal(a, b, Double.MIN_VALUE * 2);
    }

    public static boolean equals(float a, float b) {
        return equal(a, b, Float.MIN_VALUE * 2);
    }

    /**
     * tests equivalence (according to epsilon precision)
     */
    public static boolean equal(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    /**
     * tests equivalence (according to epsilon precision)
     */
    public static boolean equal(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    /** from boofcv: */
    public static void pause(long milli) {
        Thread t = Thread.currentThread();
        long start = System.currentTimeMillis();

        while(System.currentTimeMillis() - start < milli) {
            synchronized(t) {
                try {
                    long ignore = milli - (System.currentTimeMillis() - start);
                    if(ignore > 0L) {
                        t.wait(ignore);
                    }
                } catch (InterruptedException var9) {
                    ;
                }
            }
        }

    }

    /** applies a quick, non-lexicographic ordering compare
     * by first testing their lengths
     */
    public static int compare(long[] x, long[] y) {
        if (x == y) return 0;

        int xlen = x.length;

        if (xlen != y.length) {
            return Integer.compare(xlen, y.length);
        } else {

            for (int i = 0; i < xlen; i++) {
                int c = Long.compare(x[i], y[i]);
                if (c!=0)
                    return c; //first different chra
            }

            return 0; //equal
        }
    }

    public static int bin(float x, int bins) {
        return (int) Math.floor((x + (0.5f / bins)) * bins);
    }

}
