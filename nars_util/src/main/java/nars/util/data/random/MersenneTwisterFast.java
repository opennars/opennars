/* 
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package nars.util.data.random;

import objenome.solver.evolve.RandomSequence;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * The Mersenne twister is a pseudorandom number generator developed in 1997 by
 * Makoto Matsumoto and Takuji Nishimura that is based on a matrix linear
 * recurrence over a finite binary field <code>F<sub>2</sub></code>. It provides
 * for fast generation of very high-quality pseudorandom numbers, having been
 * designed specifically to rectify many of the flaws found in older algorithms.
 *
 * <p>
 * This implementation of the Mersenne twister algorithm was written by Sean
 * Luke, and released under the following license which takes priority over any
 * EpochX license. It has been adapted for use in EpochX.
 *
 * <h3>License</h3>
 *
 * Copyright (c) 2003 by Sean Luke. <br>
 * Portions copyright (c) 1993 by Michael Lecuyer. <br>
 * All rights reserved. <br>
 *
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <li>Neither the name of the copyright owners, their employers, nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * </ul>
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class MersenneTwisterFast implements Serializable, Cloneable, RandomSequence {

    /**
     *
     */
    private static final long serialVersionUID = 7948923816122472519L;

    // Period parameters
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df; // private static final *
    // constant vector a
    private static final int UPPER_MASK = 0x80000000; // most significant w-r
    // bits
    private static final int LOWER_MASK = 0x7fffffff; // least significant r
    // bits

    // Tempering parameters
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;

    private int[] mt; // the array for the state vector
    private int mti; // mti==N+1 means mt[N] is not initialized
    private int[] mag01;

    // a good initial seed (of int size, though stored in a long)
    // private static final long GOOD_SEED = 4357;
    private double __nextNextGaussian;
    private boolean __haveNextNextGaussian;

    /*
     * We're overriding all internal data, to my knowledge, so this should be
     * okay
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        MersenneTwisterFast f = (MersenneTwisterFast) (super.clone());
        f.mt = mt.clone();
        f.mag01 = mag01.clone();
        return f;
    }

    public boolean stateEquals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MersenneTwisterFast)) {
            return false;
        }
        MersenneTwisterFast other = (MersenneTwisterFast) o;
        if (mti != other.mti) {
            return false;
        }
        for (int x = 0; x < mag01.length; x++) {
            if (mag01[x] != other.mag01[x]) {
                return false;
            }
        }
        for (int x = 0; x < mt.length; x++) {
            if (mt[x] != other.mt[x]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads the entire state of the MersenneTwister RNG from the stream
     *
     * @param stream input stream
     * @throws IOException exception from stream reading
     */
    public void readState(DataInputStream stream) throws IOException {
        int len = mt.length;
        for (int x = 0; x < len; x++) {
            mt[x] = stream.readInt();
        }

        len = mag01.length;
        for (int x = 0; x < len; x++) {
            mag01[x] = stream.readInt();
        }

        mti = stream.readInt();
        __nextNextGaussian = stream.readDouble();
        __haveNextNextGaussian = stream.readBoolean();
    }

    /**
     * Writes the entire state of the MersenneTwister RNG to the stream
     *
     * @param stream output stream
     * @throws IOException exception from stream reading
     */
    public void writeState(DataOutputStream stream) throws IOException {
        int len = mt.length;
        for (int x = 0; x < len; x++) {
            stream.writeInt(mt[x]);
        }

        len = mag01.length;
        for (int x = 0; x < len; x++) {
            stream.writeInt(mag01[x]);
        }

        stream.writeInt(mti);
        stream.writeDouble(__nextNextGaussian);
        stream.writeBoolean(__haveNextNextGaussian);
    }

    /**
     * Constructor using the default seed.
     */
    public MersenneTwisterFast() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructor using a given seed. Though you pass this seed in as a long,
     * it's best to make sure it's actually an integer.
     *
     * @param seed seed
     */
    public MersenneTwisterFast(long seed) {
        setSeed(seed);
    }

    /**
     * Constructor using an array of integers as seed. Your array must have a
     * non-zero length. Only the first 624 integers in the array are used; if
     * the array is shorter than this then integers are repeatedly used in a
     * wrap-around fashion.
     *
     * @param array seed array
     */
    public MersenneTwisterFast(int[] array) {
        setSeed(array);
    }

    /**
     * Initialize the pseudo random number generator. Don't pass in a long
     * that's bigger than an int (Mersenne Twister only uses the first 32 bits
     * for its seed).
     */
    @Override
    public synchronized void setSeed(long seed) {
        // Due to a bug in java.util.Random clear up to 1.2, we're
        // doing our own Gaussian variable.
        __haveNextNextGaussian = false;

        mt = new int[N];

        mag01 = new int[2];
        mag01[0] = 0x0;
        mag01[1] = MATRIX_A;

        mt[0] = (int) (seed);
        for (mti = 1; mti < N; mti++) {
            mt[mti] = ((1812433253 * (mt[mti - 1] ^ (mt[mti - 1] >>> 30))) + mti);
            /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
            /* In the previous versions, MSBs of the seed affect */
            /* only MSBs of the array mt[]. */
            /* 2002/01/09 modified by Makoto Matsumoto */
            mt[mti] &= 0xffffffff;
            /* for >32 bit machines */
        }
    }

    /**
     * Sets the seed of the MersenneTwister using an array of integers. Your
     * array must have a non-zero length. Only the first 624 integers in the
     * array are used; if the array is shorter than this then integers are
     * repeatedly used in a wrap-around fashion.
     */
    public synchronized void setSeed(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array length must be greater than zero");
        }
        int i, j, k;
        setSeed(19650218);
        i = 1;
        j = 0;
        k = (N > array.length ? N : array.length);
        for (; k != 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1664525)) + array[j] + j; /*
             * non
             * linear
             */

            mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */

            i++;
            j++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
            if (j >= array.length) {
                j = 0;
            }
        }
        for (k = N - 1; k != 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1566083941)) - i; /*
             * non
             * linear
             */

            mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */

            i++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
        }
        mt[0] = 0x80000000; /* MSB is 1; assuring non-zero initial array */

    }

    @Override
    public final int nextInt() {
        int y;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return y;
    }

    public final short nextShort() {
        int y;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return (short) (y >>> 16);
    }

    public final char nextChar() {
        int y;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return (char) (y >>> 16);
    }

    @Override
    public final boolean nextBoolean() {
        int y;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return (y >>> 31) != 0;
    }

    /**
     * This generates a coin flip with a probability <code>probability</code> of
     * returning true, else returning false. <code>probability</code> must be
     * between 0.0 and 1.0, inclusive. Not as precise a random real event as
     * nextBoolean(double), but twice as fast. To explicitly use this, remember
     * you may need to cast to float first.
     *
     * @param probability flip probability
     * @return next boolean
     */
    public final boolean nextBoolean(float probability) {
        int y;

        if ((probability < 0.0f) || (probability > 1.0f)) {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        }
        if (probability == 0.0f) {
            return false; // fix half-open issues
        }
        if (probability == 1.0f) {
            return true; // fix half-open issues
        }
        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return ((y >>> 8) / ((float) (1 << 24))) < probability;
    }

    /**
     * This generates a coin flip with a probability <code>probability</code> of
     * returning true, else returning false. <code>probability</code> must be
     * between 0.0 and 1.0, inclusive.
     */
    public final boolean nextBoolean(double probability) {
        int y;
        int z;

        if ((probability < 0.0) || (probability > 1.0)) {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        }
        if (probability == 0.0) {
            return false; // fix half-open issues
        }
        if (probability == 1.0) {
            return true; // fix half-open issues
        }
        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
            }
            z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

            mti = 0;
        }

        z = mt[mti++];
        z ^= z >>> 11; // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18); // TEMPERING_SHIFT_L(z)

        /* derived from nextDouble documentation in jdk 1.2 docs, see top */
        return (((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53)) < probability;
    }

    public final byte nextByte() {
        int y;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return (byte) (y >>> 24);
    }

    public final void nextBytes(byte[] bytes) {
        int y;

        for (int x = 0; x < bytes.length; x++) {
            if (mti >= N) // generate N words at one time
            {
                int kk;
                int[] mt = this.mt; // locals are slightly faster
                int[] mag01 = this.mag01; // locals are slightly faster

                for (kk = 0; kk < (N - M); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                for (; kk < (N - 1); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

                mti = 0;
            }

            y = mt[mti++];
            y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

            bytes[x] = (byte) (y >>> 24);
        }
    }

    @Override
    public final long nextLong() {
        int y;
        int z;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
            }
            z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

            mti = 0;
        }

        z = mt[mti++];
        z ^= z >>> 11; // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18); // TEMPERING_SHIFT_L(z)

        return (((long) y) << 32) + z;
    }

    /**
     * Returns a long drawn uniformly from 0 to n-1. Suffice it to say, n must
     * be > 0, or an IllegalArgumentException is raised.
     *
     * @param n max long
     * @return next long
     */
    @Override
    public final long nextLong(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        long bits, val;
        do {
            int y;
            int z;

            if (mti >= N) // generate N words at one time
            {
                int kk;
                int[] mt = this.mt; // locals are slightly faster
                int[] mag01 = this.mag01; // locals are slightly faster

                for (kk = 0; kk < (N - M); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                for (; kk < (N - 1); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

                mti = 0;
            }

            y = mt[mti++];
            y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

            if (mti >= N) // generate N words at one time
            {
                int kk;
                int[] mt = this.mt; // locals are slightly faster
                int[] mag01 = this.mag01; // locals are slightly faster

                for (kk = 0; kk < (N - M); kk++) {
                    z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
                for (; kk < (N - 1); kk++) {
                    z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
                z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

                mti = 0;
            }

            z = mt[mti++];
            z ^= z >>> 11; // TEMPERING_SHIFT_U(z)
            z ^= (z << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(z)
            z ^= (z << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(z)
            z ^= (z >>> 18); // TEMPERING_SHIFT_L(z)

            bits = (((((long) y) << 32) + z) >>> 1);
            val = bits % n;
        } while (((bits - val) + (n - 1)) < 0);
        return val;
    }

    /**
     * Returns a random double in the half-open range from [0.0,1.0). Thus 0.0
     * is a valid result but 1.0 is not.
     */
    @Override
    public final double nextDouble() {
        int y;
        int z;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
            }
            z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

            mti = 0;
        }

        z = mt[mti++];
        z ^= z >>> 11; // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18); // TEMPERING_SHIFT_L(z)

        /* derived from nextDouble documentation in jdk 1.2 docs, see top */
        return ((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53);
    }

    public final double nextGaussian() {
        if (__haveNextNextGaussian) {
            __haveNextNextGaussian = false;
            return __nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                int y;
                int z;
                int a;
                int b;

                if (mti >= N) // generate N words at one time
                {
                    int kk;
                    int[] mt = this.mt; // locals are slightly faster
                    int[] mag01 = this.mag01; // locals are slightly
                    // faster

                    for (kk = 0; kk < (N - M); kk++) {
                        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                    for (; kk < (N - 1); kk++) {
                        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                    y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

                    mti = 0;
                }

                y = mt[mti++];
                y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
                y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
                y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
                y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

                if (mti >= N) // generate N words at one time
                {
                    int kk;
                    int[] mt = this.mt; // locals are slightly faster
                    int[] mag01 = this.mag01; // locals are slightly
                    // faster

                    for (kk = 0; kk < (N - M); kk++) {
                        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
                    }
                    for (; kk < (N - 1); kk++) {
                        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                    }
                    z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

                    mti = 0;
                }

                z = mt[mti++];
                z ^= z >>> 11; // TEMPERING_SHIFT_U(z)
                z ^= (z << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(z)
                z ^= (z << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(z)
                z ^= (z >>> 18); // TEMPERING_SHIFT_L(z)

                if (mti >= N) // generate N words at one time
                {
                    int kk;
                    int[] mt = this.mt; // locals are slightly faster
                    int[] mag01 = this.mag01; // locals are slightly
                    // faster

                    for (kk = 0; kk < (N - M); kk++) {
                        a = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + M] ^ (a >>> 1) ^ mag01[a & 0x1];
                    }
                    for (; kk < (N - 1); kk++) {
                        a = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + (M - N)] ^ (a >>> 1) ^ mag01[a & 0x1];
                    }
                    a = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N - 1] = mt[M - 1] ^ (a >>> 1) ^ mag01[a & 0x1];

                    mti = 0;
                }

                a = mt[mti++];
                a ^= a >>> 11; // TEMPERING_SHIFT_U(a)
                a ^= (a << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(a)
                a ^= (a << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(a)
                a ^= (a >>> 18); // TEMPERING_SHIFT_L(a)

                if (mti >= N) // generate N words at one time
                {
                    int kk;
                    int[] mt = this.mt; // locals are slightly faster
                    int[] mag01 = this.mag01; // locals are slightly
                    // faster

                    for (kk = 0; kk < (N - M); kk++) {
                        b = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + M] ^ (b >>> 1) ^ mag01[b & 0x1];
                    }
                    for (; kk < (N - 1); kk++) {
                        b = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                        mt[kk] = mt[kk + (M - N)] ^ (b >>> 1) ^ mag01[b & 0x1];
                    }
                    b = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N - 1] = mt[M - 1] ^ (b >>> 1) ^ mag01[b & 0x1];

                    mti = 0;
                }

                b = mt[mti++];
                b ^= b >>> 11; // TEMPERING_SHIFT_U(b)
                b ^= (b << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(b)
                b ^= (b << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(b)
                b ^= (b >>> 18); // TEMPERING_SHIFT_L(b)

                /*
                 * derived from nextDouble documentation in jdk 1.2 docs, see
                 * top
                 */
                v1 = (2 * (((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53))) - 1;
                v2 = (2 * (((((long) (a >>> 6)) << 27) + (b >>> 5)) / (double) (1L << 53))) - 1;
                s = (v1 * v1) + (v2 * v2);
            } while ((s >= 1) || (s == 0));
            double multiplier = /* Strict */ Math.sqrt((-2 * /* Strict */ Math.log(s)) / s);
            __nextNextGaussian = v2 * multiplier;
            __haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    /**
     * Returns a random float in the half-open range from [0.0f,1.0f). Thus 0.0f
     * is a valid result but 1.0f is not.
     *
     * @return next float
     */
    public final float nextFloat() {
        int y;

        if (mti >= N) // generate N words at one time
        {
            int kk;
            int[] mt = this.mt; // locals are slightly faster
            int[] mag01 = this.mag01; // locals are slightly faster

            for (kk = 0; kk < (N - M); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < (N - 1); kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
        }

        y = mt[mti++];
        y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

        return (y >>> 8) / ((float) (1 << 24));
    }

    /**
     * Returns an integer drawn uniformly from 0 to n-1. Suffice it to say, n
     * must be > 0, or an IllegalArgumentException is raised.
     */
    @Override
    public final int nextInt(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be positive or zero");
        }
        if (n == 1) {
            // Will always be the value 0.
            return 0;
        }

        if ((n & -n) == n) // i.e., n is a power of 2
        {
            int y;

            if (mti >= N) // generate N words at one time
            {
                int kk;
                int[] mt = this.mt; // locals are slightly faster
                int[] mag01 = this.mag01; // locals are slightly faster

                for (kk = 0; kk < (N - M); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                for (; kk < (N - 1); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

                mti = 0;
            }

            y = mt[mti++];
            y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

            return (int) ((n * (long) (y >>> 1)) >> 31);
        }

        int bits, val;
        do {
            int y;

            if (mti >= N) // generate N words at one time
            {
                int kk;
                int[] mt = this.mt; // locals are slightly faster
                int[] mag01 = this.mag01; // locals are slightly faster

                for (kk = 0; kk < (N - M); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                for (; kk < (N - 1); kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
                y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

                mti = 0;
            }

            y = mt[mti++];
            y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

            bits = (y >>> 1);
            val = bits % n;
        } while (((bits - val) + (n - 1)) < 0);
        return val;
    }
}
