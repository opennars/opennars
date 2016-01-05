

/* Generic definitions */




/* Assertions (useful to generate conditional code) */
/* Current type and class (and size, if applicable) */
/* Value methods */
/* Interfaces (keys) */
/* Interfaces (values) */
/* Abstract implementations (keys) */
/* Abstract implementations (values) */
/* Static containers (keys) */
/* Static containers (values) */
/* Implementations */
/* Synchronized wrappers */
/* Unmodifiable wrappers */
/* Other wrappers */
/* Methods (keys) */
/* Methods (values) */
/* Methods (keys/values) */
/* Methods that have special names depending on keys (but the special names depend on values) */
/* Equality */
/* Object/Reference-only definitions (keys) */
/* Primitive-type-only definitions (keys) */
/* Object/Reference-only definitions (values) */
/*		 
 * Copyright (C) 2009-2010 Sebastiano Vigna 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 *
 *
 * Copyright (C) 1999 CERN - European Organization for Nuclear Research.
 *
 *   Permission to use, copy, modify, distribute and sell this software and
 *   its documentation for any purpose is hereby granted without fee,
 *   provided that the above copyright notice appear in all copies and that
 *   both that copyright notice and this permission notice appear in
 *   supporting documentation. CERN makes no representations about the
 *   suitability of this software for any purpose. It is provided "as is"
 *   without expressed or implied warranty. 
 */
package nars.util.data.array;

import java.util.Arrays;

import static nars.util.data.array.BigArrays.*;

/**
 * A class providing static methods and objects that do useful things with {@linkplain BigArrays big arrays}.
 * <p>
 * <p>In particular, the <code>ensureCapacity()</code>, <code>grow()</code>,
 * <code>trim()</code> and <code>setLength()</code> methods allow to handle
 * big arrays much like array lists.
 * <p>
 * <P>Note that {@link it.unimi.dsi.fastutil.io.BinIO} and {@link it.unimi.dsi.fastutil.io.TextIO}
 * contain several methods make it possible to load and save big arrays of primitive types as sequences
 * of elements in {@link java.io.DataInput} format (i.e., not as objects) or as sequences of lines of text.
 *
 * @see BigArrays
 */
public enum IntBigArrays {
    ;
    /**
     * The inverse of the golden ratio times 2<sup>16</sup>.
     */
    public static final long ONEOVERPHI = 106039;

    /**
     * A static, final, empty big array.
     */
    public static final int[][] EMPTY_BIG_ARRAY = {};

    /**
     * Returns the element of the given big array of specified index.
     *
     * @param array a big array.
     * @param index a position in the big array.
     * @return the element of the big array at the specified position.
     */
    public static int get(int[][] array, long index) {
        return array[segment(index)][displacement(index)];
    }

    /**
     * Sets the element of the given big array of specified index.
     *
     * @param array a big array.
     * @param index a position in the big array.
     */
    public static void set(int[][] array, long index, int value) {
        array[segment(index)][displacement(index)] = value;
    }

    /**
     * Swaps the element of the given big array of specified indices.
     *
     * @param array  a big array.
     * @param first  a position in the big array.
     * @param second a position in the big array.
     */
    public static void swap(int[][] array, long first, long second) {
        int t = array[segment(first)][displacement(first)];
        array[segment(first)][displacement(first)] = array[segment(second)][displacement(second)];
        array[segment(second)][displacement(second)] = t;
    }

    /**
     * Adds the specified increment the element of the given big array of specified index.
     *
     * @param array a big array.
     * @param index a position in the big array.
     * @param incr  the increment
     */
    public static void add(int[][] array, long index, int incr) {
        array[segment(index)][displacement(index)] += incr;
    }

    /**
     * Multiplies by the specified factor the element of the given big array of specified index.
     *
     * @param array  a big array.
     * @param index  a position in the big array.
     * @param factor the factor
     */
    public static void mul(int[][] array, long index, int factor) {
        array[segment(index)][displacement(index)] *= factor;
    }

    /**
     * Increments the element of the given big array of specified index.
     *
     * @param array a big array.
     * @param index a position in the big array.
     */
    public static void incr(int[][] array, long index) {
        array[segment(index)][displacement(index)]++;
    }

    /**
     * Decrements the element of the given big array of specified index.
     *
     * @param array a big array.
     * @param index a position in the big array.
     */
    public static void decr(int[][] array, long index) {
        array[segment(index)][displacement(index)]--;
    }

    /**
     * Returns the length of the given big array.
     *
     * @param array a big array.
     * @return the length of the given big array.
     */
    public static long length(int[][] array) {
        int length = array.length;
        return length == 0 ? 0 : start(length - 1) + array[length - 1].length;
    }

    /**
     * Copies a big array from the specified source big array, beginning at the specified position, to the specified position of the destination big array.
     * Handles correctly overlapping regions of the same big array.
     *
     * @param srcArray  the source big array.
     * @param srcPos    the starting position in the source big array.
     * @param destArray the destination big array.
     * @param destPos   the starting position in the destination data.
     * @param length    the number of elements to be copied.
     */
    public static void copy(int[][] srcArray, long srcPos, int[][] destArray, long destPos, long length) {
        if (destPos <= srcPos) {
            int srcSegment = segment(srcPos);
            int destSegment = segment(destPos);
            int srcDispl = displacement(srcPos);
            int destDispl = displacement(destPos);
            int l;
            while (length > 0) {
                l = (int) Math.min(length, Math.min(srcArray[srcSegment].length - srcDispl, destArray[destSegment].length - destDispl));
                System.arraycopy(srcArray[srcSegment], srcDispl, destArray[destSegment], destDispl, l);
                if ((srcDispl += l) == SEGMENT_SIZE) {
                    srcDispl = 0;
                    srcSegment++;
                }
                if ((destDispl += l) == SEGMENT_SIZE) {
                    destDispl = 0;
                    destSegment++;
                }
                length -= l;
            }
        } else {
            int srcSegment = segment(srcPos + length);
            int destSegment = segment(destPos + length);
            int srcDispl = displacement(srcPos + length);
            int destDispl = displacement(destPos + length);
            int l;
            while (length > 0) {
                if (srcDispl == 0) {
                    srcDispl = SEGMENT_SIZE;
                    srcSegment--;
                }
                if (destDispl == 0) {
                    destDispl = SEGMENT_SIZE;
                    destSegment--;
                }
                l = (int) Math.min(length, Math.min(srcDispl, destDispl));
                System.arraycopy(srcArray[srcSegment], srcDispl - l, destArray[destSegment], destDispl - l, l);
                srcDispl -= l;
                destDispl -= l;
                length -= l;
            }
        }
    }

    /**
     * Copies a big array from the specified source big array, beginning at the specified position, to the specified position of the destination array.
     *
     * @param srcArray  the source big array.
     * @param srcPos    the starting position in the source big array.
     * @param destArray the destination array.
     * @param destPos   the starting position in the destination data.
     * @param length    the number of elements to be copied.
     */
    public static void copyFromBig(int[][] srcArray, long srcPos, int[] destArray, int destPos, int length) {
        int srcSegment = segment(srcPos);
        int srcDispl = displacement(srcPos);
        int l;
        while (length > 0) {
            l = Math.min(srcArray[srcSegment].length - srcDispl, length);
            System.arraycopy(srcArray[srcSegment], srcDispl, destArray, destPos, l);
            if ((srcDispl += l) == SEGMENT_SIZE) {
                srcDispl = 0;
                srcSegment++;
            }
            destPos += l;
            length -= l;
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination big array.
     *
     * @param srcArray  the source array.
     * @param srcPos    the starting position in the source array.
     * @param destArray the destination big array.
     * @param destPos   the starting position in the destination data.
     * @param length    the number of elements to be copied.
     */
    public static void copyToBig(int[] srcArray, int srcPos, int[][] destArray, long destPos, long length) {
        int destSegment = segment(destPos);
        int destDispl = displacement(destPos);
        int l;
        while (length > 0) {
            l = (int) Math.min(destArray[destSegment].length - destDispl, length);
            System.arraycopy(srcArray, srcPos, destArray[destSegment], destDispl, l);
            if ((destDispl += l) == SEGMENT_SIZE) {
                destDispl = 0;
                destSegment++;
            }
            srcPos += l;
            length -= l;
        }
    }

    /**
     * Creates a new big array.
     *
     * @param length the length of the new big array.
     * @return a new big array of given length.
     */
    public static int[][] newBigArray(long length) {
        if (length == 0) return EMPTY_BIG_ARRAY;
        int baseLength = (int) ((length + SEGMENT_MASK) / SEGMENT_SIZE);
        int[][] base = new int[baseLength][];
        int residual = (int) (length & SEGMENT_MASK);
        if (residual != 0) {
            for (int i = 0; i < baseLength - 1; i++) base[i] = new int[SEGMENT_SIZE];
            base[baseLength - 1] = new int[residual];
        } else for (int i = 0; i < baseLength; i++) base[i] = new int[SEGMENT_SIZE];
        return base;
    }

    /**
     * Turns a standard array into a big array.
     * <p>
     * <P>Note that the returned big array might contain as a segment the original array.
     *
     * @param array an array.
     * @return a new big array with the same length and content of <code>array</code>.
     */
    public static int[][] wrap(int[] array) {
        if (array.length == 0) return EMPTY_BIG_ARRAY;
        if (array.length <= SEGMENT_SIZE) return new int[][]{array};
        int[][] bigArray = newBigArray(array.length);
        for (int i = 0; i < bigArray.length; i++)
            System.arraycopy(array, (int) start(i), bigArray[i], 0, bigArray[i].length);
        return bigArray;
    }

    /**
     * Ensures that a big array can contain the given number of entries.
     * <p>
     * <P>If you cannot foresee whether this big array will need again to be
     * enlarged, you should probably use <code>grow()</code> instead.
     * <p>
     * <p><strong>Warning:</strong> the returned array might use part of the segments of the original
     * array, which must be considered read-only after calling this method.
     *
     * @param array  a big array.
     * @param length the new minimum length for this big array.
     * @return <code>array</code>, if it contains <code>length</code> entries or more; otherwise,
     * a big array with <code>length</code> entries whose first <code>length(array)</code>
     * entries are the same as those of <code>array</code>.
     */
    public static int[][] ensureCapacity(int[][] array, long length) {
        return ensureCapacity(array, length, length(array));
    }

    /**
     * Ensures that a big array can contain the given number of entries, preserving just a part of the big array.
     * <p>
     * <p><strong>Warning:</strong> the returned array might use part of the segments of the original
     * array, which must be considered read-only after calling this method.
     *
     * @param array    a big array.
     * @param length   the new minimum length for this big array.
     * @param preserve the number of elements of the big array that must be preserved in case a new allocation is necessary.
     * @return <code>array</code>, if it can contain <code>length</code> entries or more; otherwise,
     * a big array with <code>length</code> entries whose first <code>preserve</code>
     * entries are the same as those of <code>array</code>.
     */
    public static int[][] ensureCapacity(int[][] array, long length, long preserve) {
        long oldLength = length(array);
        if (length > oldLength) {
            int valid = array.length - (array.length == 0 || array.length > 0 && array[array.length - 1].length == SEGMENT_SIZE ? 0 : 1);
            int baseLength = (int) ((length + SEGMENT_MASK) / SEGMENT_SIZE);
            int[][] base = Arrays.copyOf(array, baseLength);
            int residual = (int) (length & SEGMENT_MASK);
            if (residual != 0) {
                for (int i = valid; i < baseLength - 1; i++) base[i] = new int[SEGMENT_SIZE];
                base[baseLength - 1] = new int[residual];
            } else for (int i = valid; i < baseLength; i++) base[i] = new int[SEGMENT_SIZE];
            if (preserve - (valid * (long) SEGMENT_SIZE) > 0)
                copy(array, valid * (long) SEGMENT_SIZE, base, valid * (long) SEGMENT_SIZE, preserve - (valid * (long) SEGMENT_SIZE));
            return base;
        }
        return array;
    }

    /**
     * Grows the given big array to the maximum between the given length and
     * the current length divided by the golden ratio, provided that the given
     * length is larger than the current length.
     * <p>
     * <P> Dividing by the golden ratio (&phi;) approximately increases the big array
     * length by 1.618. If you want complete control on the big array growth, you
     * should probably use <code>ensureCapacity()</code> instead.
     * <p>
     * <p><strong>Warning:</strong> the returned array might use part of the segments of the original
     * array, which must be considered read-only after calling this method.
     *
     * @param array  a big array.
     * @param length the new minimum length for this big array.
     * @return <code>array</code>, if it can contain <code>length</code>
     * entries; otherwise, a big array with
     * max(<code>length</code>,<code>length(array)</code>/&phi;) entries whose first
     * <code>length(array)</code> entries are the same as those of <code>array</code>.
     */
    public static int[][] grow(int[][] array, long length) {
        long oldLength = length(array);
        return length > oldLength ? grow(array, length, oldLength) : array;
    }

    /**
     * Grows the given big array to the maximum between the given length and
     * the current length divided by the golden ratio, provided that the given
     * length is larger than the current length, preserving just a part of the big array.
     * <p>
     * <P> Dividing by the golden ratio (&phi;) approximately increases the big array
     * length by 1.618. If you want complete control on the big array growth, you
     * should probably use <code>ensureCapacity()</code> instead.
     * <p>
     * <p><strong>Warning:</strong> the returned array might use part of the segments of the original
     * array, which must be considered read-only after calling this method.
     *
     * @param array    a big array.
     * @param length   the new minimum length for this big array.
     * @param preserve the number of elements of the big array that must be preserved in case a new allocation is necessary.
     * @return <code>array</code>, if it can contain <code>length</code>
     * entries; otherwise, a big array with
     * max(<code>length</code>,<code>length(array)</code>/&phi;) entries whose first
     * <code>preserve</code> entries are the same as those of <code>array</code>.
     */
    public static int[][] grow(int[][] array, long length, long preserve) {
        long oldLength = length(array);
        return length > oldLength ? ensureCapacity(array, Math.max((ONEOVERPHI * oldLength) >>> 16, length), preserve) : array;
    }

    /**
     * Trims the given big array to the given length.
     * <p>
     * <p><strong>Warning:</strong> the returned array might use part of the segments of the original
     * array, which must be considered read-only after calling this method.
     *
     * @param array  a big array.
     * @param length the new maximum length for the big array.
     * @return <code>array</code>, if it contains <code>length</code>
     * entries or less; otherwise, a big array with
     * <code>length</code> entries whose entries are the same as
     * the first <code>length</code> entries of <code>array</code>.
     */
    public static int[][] trim(int[][] array, long length) {
        long oldLength = length(array);
        if (length >= oldLength) return array;
        int baseLength = (int) ((length + SEGMENT_MASK) / SEGMENT_SIZE);
        int[][] base = Arrays.copyOf(array, baseLength);
        int residual = (int) (length & SEGMENT_MASK);
        if (residual != 0) base[baseLength - 1] = IntArrays.trim(base[baseLength - 1], residual);
        return base;
    }

    /**
     * Sets the length of the given big array.
     * <p>
     * <p><strong>Warning:</strong> the returned array might use part of the segments of the original
     * array, which must be considered read-only after calling this method.
     *
     * @param array  a big array.
     * @param length the new length for the big array.
     * @return <code>array</code>, if it contains exactly <code>length</code>
     * entries; otherwise, if it contains <em>more</em> than
     * <code>length</code> entries, a big array with <code>length</code> entries
     * whose entries are the same as the first <code>length</code> entries of
     * <code>array</code>; otherwise, a big array with <code>length</code> entries
     * whose first <code>length(array)</code> entries are the same as those of
     * <code>array</code>.
     */
    public static int[][] setLength(int[][] array, long length) {
        long oldLength = length(array);
        if (length == oldLength) return array;
        if (length < oldLength) return trim(array, length);
        return ensureCapacity(array, length);
    }

    /**
     * Returns a copy of a portion of a big array.
     *
     * @param array  a big array.
     * @param offset the first element to copy.
     * @param length the number of elements to copy.
     * @return a new big array containing <code>length</code> elements of <code>array</code> starting at <code>offset</code>.
     */
    public static int[][] copy(int[][] array, long offset, long length) {
        ensureOffsetLength(array, offset, length);
        int[][] a =
                newBigArray(length);
        copy(array, offset, a, 0, length);
        return a;
    }

    /**
     * Returns a copy of a big array.
     *
     * @param array a big array.
     * @return a copy of <code>array</code>.
     */
    public static int[][] copy(int[][] array) {
        int[][] base = array.clone();
        for (int i = base.length; i-- != 0; ) base[i] = array[i].clone();
        return base;
    }

    /**
     * Fills the given big array with the given value.
     * <p>
     * <P>This method uses a backward loop. It is significantly faster than the corresponding
     * method in {@link java.util.Arrays}.
     *
     * @param array a big array.
     * @param value the new value for all elements of the big array.
     */
    public static void fill(int[][] array, int value) {
        for (int i = array.length; i-- != 0; ) Arrays.fill(array[i], value);
    }

    /**
     * Fills a portion of the given big array with the given value.
     * <p>
     * <P>If possible (i.e., <code>from</code> is 0) this method uses a
     * backward loop. In this case, it is significantly faster than the
     * corresponding method in {@link java.util.Arrays}.
     *
     * @param array a big array.
     * @param from  the starting index of the portion to fill.
     * @param to    the end index of the portion to fill.
     * @param value the new value for all elements of the specified portion of the big array.
     */
    public static void fill(int[][] array, long from, long to, int value) {
        long length = length(array);
        BigArrays.ensureFromTo(length, from, to);
        int fromSegment = segment(from);
        int toSegment = segment(to);
        int fromDispl = displacement(from);
        int toDispl = displacement(to);
        if (fromSegment == toSegment) {
            Arrays.fill(array[fromSegment], fromDispl, toDispl, value);
            return;
        }
        if (toDispl != 0) Arrays.fill(array[toSegment], 0, toDispl, value);
        while (--toSegment > fromSegment) Arrays.fill(array[toSegment], value);
        Arrays.fill(array[fromSegment], fromDispl, SEGMENT_SIZE, value);
    }

    /**
     * Returns true if the two big arrays are elementwise equal.
     * <p>
     * <P>This method uses a backward loop. It is significantly faster than the corresponding
     * method in {@link java.util.Arrays}.
     *
     * @param a1 a big array.
     * @param a2 another big array.
     * @return true if the two big arrays are of the same length, and their elements are equal.
     */
    public static boolean equals(int[][] a1, int[][] a2) {
        if (length(a1) != length(a2)) return false;
        int i = a1.length, j;
        int[] t, u;
        while (i-- != 0) {
            t = a1[i];
            u = a2[i];
            j = t.length;
            while (j-- != 0) if (!((t[j]) == (u[j]))) return false;
        }
        return true;
    }

    /* Returns a string representation of the contents of the specified big array.
        *
        * The string representation consists of a list of the big array's elements, enclosed in square brackets ("[]"). Adjacent elements are separated by the characters ", " (a comma followed by a space). Returns "null" if <code>a</code> is null.
        * @param a the big array whose string representation to return.
        * @return the string representation of <code>a</code>.
        */
    public static String toString(int[][] a) {
        if (a == null) return "null";
        long last = length(a) - 1;
        if (last == -1) return "[]";
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (long i = 0; ; i++) {
            b.append(get(a, i));
            if (i == last) return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits a big array.
     * <p>
     * <P>This method may be used whenever a big array range check is needed.
     *
     * @param a    a big array.
     * @param from a start index (inclusive).
     * @param to   an end index (inclusive).
     * @throws IllegalArgumentException       if <code>from</code> is greater than <code>to</code>.
     * @throws ArrayIndexOutOfBoundsException if <code>from</code> or <code>to</code> are greater than the big array length or negative.
     */
    public static void ensureFromTo(int[][] a, long from, long to) {
        BigArrays.ensureFromTo(length(a), from, to);
    }

    /**
     * Ensures that a range given by an offset and a length fits a big array.
     * <p>
     * <P>This method may be used whenever a big array range check is needed.
     *
     * @param a      a big array.
     * @param offset a start index.
     * @param length a length (the number of elements in the range).
     * @throws IllegalArgumentException       if <code>length</code> is negative.
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code> is negative or <code>offset</code>+<code>length</code> is greater than the big array length.
     */
    public static void ensureOffsetLength(int[][] a, long offset, long length) {
        BigArrays.ensureOffsetLength(length(a), offset, length);
    }

//    /**
//     * A type-specific content-based hash strategy for big arrays.
//     */
//    private static final class BigArrayHashStrategy implements Hash.Strategy<int[][]>, java.io.Serializable {
//        public static final long serialVersionUID = -7046029254386353129L;
//
//        public int hashCode(final int[][] o) {
//            return java.util.Arrays.deepHashCode(o);
//        }
//
//        public boolean equals(final int[][] a, final int[][] b) {
//            return IntBigArrays.equals(a, b);
//        }
//    }

    /**
     * A type-specific content-based hash strategy for big arrays.
     * <p>
     * <P>This hash strategy may be used in custom hash collections whenever keys are
     * big arrays, and they must be considered equal by content. This strategy
     * will handle <code>null</code> correctly, and it is serializable.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    //public final static Hash.Strategy HASH_STRATEGY = new BigArrayHashStrategy();
    private static final int SMALL = 7;
    private static final int MEDIUM = 40;

    private static void vecSwap(int[][] x, long a, long b, long n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
    }

    private static long med3(int[][] x, long a, long b, long c, IntComparator comp) {
        int ab = comp.compare(get(x, a), get(x, b));
        int ac = comp.compare(get(x, a), get(x, c));
        int bc = comp.compare(get(x, b), get(x, c));
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    /**
     * Sorts the specified range of elements according to the order induced by the specified
     * comparator using quicksort.
     * <p>
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * @param x    the big array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void quickSort(int[][] x, long from, long to, IntComparator comp) {
        long len = to - from;
        // Insertion sort on smallest arrays
        if (len < SMALL) {
            for (long i = from; i < to; i++)
                for (long j = i; j > from && comp.compare(get(x, j - 1), get(x, j)) > 0; j--) swap(x, j, j - 1);
            return;
        }
        // Choose a partition element, v
        long m = from + len / 2; // Small arrays, middle element
        if (len > SMALL) {
            long l = from;
            long n = to - 1;
            if (len > MEDIUM) { // Big arrays, pseudomedian of 9
                long s = len / 8;
                l = med3(x, l, l + s, l + 2 * s, comp);
                m = med3(x, m - s, m, m + s, comp);
                n = med3(x, n - 2 * s, n - s, n, comp);
            }
            m = med3(x, l, m, n, comp); // Mid-size, med of 3
        }
        int v = get(x, m);
        // Establish Invariant: v* (<v)* (>v)* v*
        long a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = comp.compare(get(x, b), v)) <= 0) {
                if (comparison == 0) swap(x, a++, b);
                b++;
            }
            while (c >= b && (comparison = comp.compare(get(x, c), v)) >= 0) {
                if (comparison == 0) swap(x, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        long s, n = to;
        s = Math.min(a - from, b - a);
        vecSwap(x, from, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecSwap(x, b, n - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) quickSort(x, from, from + s, comp);
        if ((s = d - c) > 1) quickSort(x, n - s, n, comp);
    }

    @SuppressWarnings("unchecked")
    private static long med3(int[][] x, long a, long b, long c) {
        int ab = ((get(x, a)) < (get(x, b)) ? -1 : ((get(x, a)) == (get(x, b)) ? 0 : 1));
        int ac = ((get(x, a)) < (get(x, c)) ? -1 : ((get(x, a)) == (get(x, c)) ? 0 : 1));
        int bc = ((get(x, b)) < (get(x, c)) ? -1 : ((get(x, b)) == (get(x, c)) ? 0 : 1));
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    /**
     * Sorts the specified big array according to the order induced by the specified
     * comparator using quicksort.
     * <p>
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * @param x    the big array to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void quickSort(int[][] x, IntComparator comp) {
        quickSort(x, 0, IntBigArrays.length(x), comp);
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using quicksort.
     * <p>
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * @param x    the big array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */
    @SuppressWarnings("unchecked")
    public static void quickSort(int[][] x, long from, long to) {
        long len = to - from;
        // Insertion sort on smallest arrays
        if (len < SMALL) {
            for (long i = from; i < to; i++)
                for (long j = i; j > from && ((get(x, j - 1)) < (get(x, j)) ? -1 : ((get(x, j - 1)) == (get(x, j)) ? 0 : 1)) > 0; j--)
                    swap(x, j, j - 1);
            return;
        }
        // Choose a partition element, v
        long m = from + len / 2; // Small arrays, middle element
        if (len > SMALL) {
            long l = from;
            long n = to - 1;
            if (len > MEDIUM) { // Big arrays, pseudomedian of 9
                long s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        int v = get(x, m);
        // Establish Invariant: v* (<v)* (>v)* v*
        long a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = ((get(x, b)) < (v) ? -1 : ((get(x, b)) == (v) ? 0 : 1))) <= 0) {
                if (comparison == 0) swap(x, a++, b);
                b++;
            }
            while (c >= b && (comparison = ((get(x, c)) < (v) ? -1 : ((get(x, c)) == (v) ? 0 : 1))) >= 0) {
                if (comparison == 0) swap(x, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        long s, n = to;
        s = Math.min(a - from, b - a);
        vecSwap(x, from, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecSwap(x, b, n - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) quickSort(x, from, from + s);
        if ((s = d - c) > 1) quickSort(x, n - s, n);
    }

    /**
     * Sorts the specified big array according to the natural ascending order using quicksort.
     * <p>
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * @param x the big array to be sorted.
     */
    @SuppressWarnings("unchecked")
    public static void quickSort(int[][] x) {
        quickSort(x, 0, IntBigArrays.length(x));
    }

    /**
     * Searches a range of the specified big array for the specified value using
     * the binary search algorithm. The range must be sorted prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a    the big array to be searched.
     * @param from the index of the first element (inclusive) to be searched.
     * @param to   the index of the last element (exclusive) to be searched.
     * @param key  the value to be searched for.
     * @return index of the search key, if it is contained in the big array;
     * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the big array: the index of the first
     * element greater than the key, or the length of the big array, if all
     * elements in the big array are less than the specified key.  Note
     * that this guarantees that the return value will be &gt;= 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static long binarySearch(int[][] a, long from, long to, int key) {
        int midVal;
        while (from <= to) {
            long mid = (from + to) >>> 1;
            midVal = get(a, mid);
            if (midVal < key) from = mid + 1;
            else if (midVal > key) to = mid - 1;
            else return mid;
        }
        return -(from + 1);
    }

    /**
     * Searches a big array for the specified value using
     * the binary search algorithm. The range must be sorted prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a   the big array to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the big array;
     * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the big array: the index of the first
     * element greater than the key, or the length of the big array, if all
     * elements in the big array are less than the specified key.  Note
     * that this guarantees that the return value will be &gt;= 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    public static long binarySearch(int[][] a, int key) {
        return binarySearch(a, 0, IntBigArrays.length(a), key);
    }

    /**
     * Searches a range of the specified big array for the specified value using
     * the binary search algorithm and a specified comparator. The range must be sorted following the comparator prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a    the big array to be searched.
     * @param from the index of the first element (inclusive) to be searched.
     * @param to   the index of the last element (exclusive) to be searched.
     * @param key  the value to be searched for.
     * @param c    a comparator.
     * @return index of the search key, if it is contained in the big array;
     * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the big array: the index of the first
     * element greater than the key, or the length of the big array, if all
     * elements in the big array are less than the specified key.  Note
     * that this guarantees that the return value will be &gt;= 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    public static long binarySearch(int[][] a, long from, long to, int key, IntComparator c) {
        int midVal;
        while (from <= to) {
            long mid = (from + to) >>> 1;
            midVal = get(a, mid);
            int cmp = c.compare(midVal, key);
            if (cmp < 0) from = mid + 1;
            else if (cmp > 0) to = mid - 1;
            else return mid; // key found
        }
        return -(from + 1);
    }

    /**
     * Searches a big array for the specified value using
     * the binary search algorithm and a specified comparator. The range must be sorted following the comparator prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a   the big array to be searched.
     * @param key the value to be searched for.
     * @param c   a comparator.
     * @return index of the search key, if it is contained in the big array;
     * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the big array: the index of the first
     * element greater than the key, or the length of the big array, if all
     * elements in the big array are less than the specified key.  Note
     * that this guarantees that the return value will be &gt;= 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    public static long binarySearch(int[][] a, int key, IntComparator c) {
        return binarySearch(a, 0, IntBigArrays.length(a), key, c);
    }
}