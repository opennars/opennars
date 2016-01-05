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

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.stream;

/**
 *
 *
 *
 */
public enum Util {
    ;


    public static final int PRIME3 = 524287;
    public static final int PRIME2 = 92821;
    public static final int PRIME1 = 31;

    /**
     * It is basically the same as a lookup table with 2048 entries and linear interpolation between the entries, but all this with IEEE floating point tricks.
     * http://stackoverflow.com/questions/412019/math-optimization-in-c-sharp#412988
     */
    public static double expFast(double val) {
        long tmp = (long) (1512775 * val + (1072693248 - 60801));
        return Double.longBitsToDouble(tmp << 32);
    }


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

    public static String UUIDbase64() {
        long low = UUID.randomUUID().getLeastSignificantBits();
        long high = UUID.randomUUID().getMostSignificantBits();
        return new String(Base64.getEncoder().encode(
                Bytes.concat(
                        Longs.toByteArray(low),
                        Longs.toByteArray(high)
                )
        ));
    }

    public static int hash(int a, int b) {
        return PRIME2 * (PRIME2 + a) + b;
    }

    public static int hash(int a, int b, int c) {
        return PRIME2 * (PRIME2 * (PRIME2 + a) + b) + c;
    }

//    public final static int hash(int a, int b, int c, int d) {
//        return PRIME2 * (PRIME2 * (PRIME2 * (PRIME2 + a) + b) + c) + d;
//    }

//    public final static int hash(int a, int b, int c, int d, long e) {
//        long x = PRIME2 * (PRIME2 * (PRIME2 * (PRIME2 * (PRIME2 + a) + b) + c) + d) + e;
//        return (int)x;
//    }

    public static int hash(Object a, Object b) {
        return hash(a.hashCode(), b.hashCode());
    }

    public static int hash(Object a, Object b, Object c) {
        return hash(a.hashCode(), b.hashCode(), c.hashCode());
    }

//    public final static int hash(Object a, Object b, Object c, Object d) {
//        return hash(a.hashCode(), b.hashCode(), c.hashCode(), d.hashCode());
//    }

    public static void assertNotNull(Object test, String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
    }

    public static void assertNotEmpty(Object[] test, String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.length == 0) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static void assertNotEmpty(CharSequence test, String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.length() == 0) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static void assertNotBlank(CharSequence test, String varName) {
        if (test != null) {
            test = test.toString().trim();
        }
        assertNotEmpty(test, varName);
    }

    public static <E> void assertNotEmpty(Collection<E> test, String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.isEmpty()) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static <K, V> void assertNotEmpty(Map<K, V> test, String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.isEmpty()) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static boolean equalsNullAware(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;

        }
        if (obj2 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }

    public static String globToRegEx(String line) {

        line = line.trim();
        int strLen = line.length();
        StringBuilder sb = new StringBuilder(strLen);
        // Remove beginning and ending * globs because they're useless
        if (line.length() > 0 && line.charAt(0) == '*') {
            line = line.substring(1);
            strLen--;
        }
        if (line.length() > 0 && line.charAt(line.length() - 1) == '*') {
            line = line.substring(0, strLen - 1);
            strLen--;
        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : line.toCharArray()) {
            switch (currentChar) {
                case '*':
                    if (escaping)
                        sb.append("\\*");
                    else
                        sb.append(".*");
                    escaping = false;
                    break;
                case '?':
                    if (escaping)
                        sb.append("\\?");
                    else
                        sb.append('.');
                    escaping = false;
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(currentChar);
                    escaping = false;
                    break;
                case '\\':
                    if (escaping) {
                        sb.append("\\\\");
                        escaping = false;
                    } else
                        escaping = true;
                    break;
                case '{':
                    if (escaping) {
                        sb.append("\\{");
                    } else {
                        sb.append('(');
                        inCurlies++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inCurlies > 0 && !escaping) {
                        sb.append(')');
                        inCurlies--;
                    } else if (escaping)
                        sb.append("\\}");
                    else
                        sb.append('}');
                    escaping = false;
                    break;
                case ',':
                    if (inCurlies > 0 && !escaping) {
                        sb.append('|');
                    } else if (escaping)
                        sb.append("\\,");
                    else
                        sb.append(',');
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
        }
        return sb.toString();
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
        long ThreeQuarters = (BitsInUnsignedInt * 3) / 4;
        long OneEighth = BitsInUnsignedInt / 8;
        long HighBits = (0xFFFFFFFFL) << (BitsInUnsignedInt - OneEighth);
        long hash = 0;
        long test = 0;

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
        long x = 0;

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

        int len = str.length;

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



    public static long BKDRHash(String str) {
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }

        return hash;
    }
   /* End Of BKDR Hash Function */


    public static long SDBMHash(String str) {
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
        }

        return hash;
    }
   /* End Of SDBM Hash Function */


    public static long DJBHash(String str) {
        long hash = 5381;

        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) + hash) + str.charAt(i);
        }

        return hash;
    }
   /* End Of DJB Hash Function */


    public static long DEKHash(String str) {
        long hash = str.length();

        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
        }

        return hash;
    }
   /* End Of DEK Hash Function */


    public static long BPHash(String str) {
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = hash << 7 ^ str.charAt(i);
        }

        return hash;
    }
   /* End Of BP Hash Function */


    public static long FNVHash(String str) {
        long fnv_prime = 0x811C9DC5;
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash *= fnv_prime;
            hash ^= str.charAt(i);
        }

        return hash;
    }
   /* End Of FNV Hash Function */


    public static long APHash(String str) {
        long hash = 0xAAAAAAAA;

        for (int i = 0; i < str.length(); i++) {
            hash ^= (i & 1) == 0 ? (hash << 7) ^ str.charAt(i) * (hash >> 3) : ~((hash << 11) + str.charAt(i) ^ (hash >> 5));
        }

        return hash;
    }
   /* End Of AP Hash Function */

//    }


    /**
     * returns the next index
     */
    public static int long2Bytes(long l, byte[] target, int offset) {
        for (int i = offset + 7; i >= offset; i--) {
            target[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return offset + 8;
    }

    /**
     * returns the next index
     */
    public static int int2Bytes(int l, byte[] target, int offset) {
        for (int i = offset + 3; i >= offset; i--) {
            target[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return offset + 4;
    }

    /**
     * http://www.java-gaming.org/index.php?topic=24194.0
     */
    public static int floorInt(float x) {
        return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
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
     * Generic utility method for running a list of tasks in current thread (concurrency == 1) or in multiple threads (> 1, in which case it will block until they finish)
     */
    public static void run(Deque<Runnable> tasks, int maxTasksToRun, int threads) {

        //int concurrency = Math.min(threads, maxTasksToRun);
        //if (concurrency == 1) {
            tasks.forEach(Runnable::run);
//            return;
  //      }
//
//        ConcurrentContext ctx = ConcurrentContext.enter();
//        ctx.setConcurrency(concurrency);
//
//        try {
//            run(tasks, maxTasksToRun, ctx::execute);
//        } finally {
//            // Waits for all concurrent executions to complete.
//            // Re-exports any exception raised during concurrent executions.
//            if (ctx != null)
//                ctx.exit();
//        }

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

    public static byte[] intAsByteArray(int index) {

        if (index < 36) {
            byte x = base36(index);
            return new byte[] {  x};
        }
        else if (index < (36*36)){
            byte x1 = base36(index%36);
            byte x2 = base36(index/36);
            return new byte[] { x2, x1};
        }
        else {
            throw new RuntimeException("variable index out of range for this method");
        }



//        int digits = (index >= 256 ? 3 : ((index >= 16) ? 2 : 1));
//        StringBuilder cb  = new StringBuilder(1 + digits).append(type);
//        do {
//            cb.append(  Character.forDigit(index % 16, 16) ); index /= 16;
//        } while (index != 0);
//        return cb.toString();

    }

    public static int bin(float x, int bins) {
        return (int) Math.floor((x + (0.5f / bins)) * bins);
    }

    /** bins a priority value to an integer */
    public static int decimalize(float v) {
        return bin(v,10);
    }

    /** finds the mean value of a given bin */
    public static float unbinCenter(int b, int bins) {
        return ((float)b)/bins;
    }

    public static <D> D runProbability(Random rng, float[] probs, D[] choices) {
        float tProb = 0;
        for (int i = 0; i < probs.length; i++) {
            tProb += probs[i];
        }
        float s = rng.nextFloat() * tProb;
        int c = 0;
        for (int i = 0; i < probs.length; i++) {
            s -= probs[i];
            if (s <= 0) { c = i; break; }
        }
        return choices[c];
    }


    public static MethodHandle mhRef(Class<?> type, String name) {
        try {
            return MethodHandles
                    .lookup()
                    //.publicLookup(
                    .unreflect(stream(type.getMethods()).filter(m -> m.getName().equals(name)).findFirst().get());
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public static <F> MethodHandle mh(String name, F fun) {
        return mh(name, fun.getClass(), fun);
    }

    public static <F> MethodHandle mh(String name, Class<? extends F> type, F fun) {
        return mhRef(type, name).bindTo(fun);
    }
    public static <F> MethodHandle mh(String name, F... fun) {
        F fun0 = fun[0];
        MethodHandle m = mh(name, fun0.getClass(), fun0);
        for (int i = 1; i < fun.length; i++) {
            m = m.bindTo(fun[i]);
        }
        return m;
    }


    public static byte base36(int index) {
        if (index < 10)
            return (byte) ('0' + index);
        else if (index < (10 + 26))
            return (byte) ((index - 10) + 'a');
        else
            throw new RuntimeException("out of bounds");
    }
}
