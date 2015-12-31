package nars.util.data.random;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2015 Sebastiano Vigna 
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */


import java.util.Random;

/** A non-splittable version of the <span style="font-variant: small-caps">SplitMix</span> pseudorandom number generator used by Java 8's 
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/SplittableRandom.html"><code>SplittableRandom</code></a>. Due to 
 * the fixed increment constant and to different strategies in generating finite ranges, the methods of this generator
 * are faster than those of <code>SplittableRandom</code>. Indeed, this is the fastest generator of the collection that
 * passes the BigCrush battery of tests.
 *
 * @see it.unimi.dsi.util
 * @see Random
 * @see SplitMix64RandomGenerator
 */
public class SplitMix64Random extends Random {
	private static final long serialVersionUID = 1L;
	/** 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
	private static final long PHI = 0x9E3779B97F4A7C15L;
	/** 2<sup>53</sup> &minus; 1. */
	private static final long DOUBLE_MASK = ( 1L << 53 ) - 1;
	/** 2<sup>-53</sup>. */
	private static final double NORM_53 = 1.0 / ( 1L << 53 );
	/** 2<sup>24</sup> &minus; 1. */
	private static final long FLOAT_MASK = ( 1L << 24 ) - 1;
	/** 2<sup>-24</sup>. */
	private static final double NORM_24 = 1.0 / ( 1L << 24 );

	/** The internal state of the algorithm (a Weyl generator using the {@link #PHI} as increment). */
	private long x;

//	/** Creates a new generator seeded using {@link Util#randomSeed()}. */
//	public SplitMix64Random() {
//		this( Util.randomSeed() );
//	}

	/** Creates a new generator using a given seed.
	 * 
	 * @param seed a nonzero seed for the generator (if zero, the generator will be seeded with -1).
	 */
	public SplitMix64Random( long seed ) {
		setSeed( seed );
	}

	/* David Stafford's (http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html)
     * "Mix13" variant of the 64-bit finalizer in Austin Appleby's MurmurHash3 algorithm. */
	private static long staffordMix13( long z ) {
		z = ( z ^ ( z >>> 30 ) ) * 0xBF58476D1CE4E5B9L; 
		z = ( z ^ ( z >>> 27 ) ) * 0x94D049BB133111EBL;
		return z ^ ( z >>> 31 );
	}

	/* David Stafford's (http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html)
     * "Mix4" variant of the 64-bit finalizer in Austin Appleby's MurmurHash3 algorithm. */
	private static int staffordMix4Upper32( long z ) {
		z = ( z ^ ( z >>> 33 ) ) * 0x62A9D9ED799705F5L;
		return (int)( ( ( z ^ ( z >>> 28 ) ) * 0xCB24D0A5C88C35B3L ) >>> 32 );
	}

	@Override
	public long nextLong() {
		return staffordMix13( x += PHI );
	}

	@Override
	public int nextInt() {
		return staffordMix4Upper32( x += PHI );
	}
	
	/** Returns a pseudorandom, approximately uniformly distributed {@code int} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.
     * 
     * <p>The hedge &ldquo;approximately&rdquo; is due to the fact that to be always
     * faster than <a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadLocalRandom.html"><code>ThreadLocalRandom</code></a>
     * we return
     * the upper 63 bits of {@link #nextLong()} modulo {@code n} instead of using
     * {@link Random}'s fancy algorithm (which {@link #nextLong(long)} uses though).
     * This choice introduces a bias: the numbers from 0 to 2<sup>63</sup> mod {@code n}
     * are slightly more likely than the other ones. In the worst case, &ldquo;more likely&rdquo;
     * means 1.00000000023 times more likely, which is in practice undetectable. If for some reason you
     * need truly uniform generation, just use {@link #nextLong(long)}.
     * 
     * @param n the positive bound on the random number to be returned.
     * @return the next pseudorandom {@code int} value between {@code 0} (inclusive) and {@code n} (exclusive).
     */
	@Override
	public int nextInt( int n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
        return (int)( ( staffordMix13( x += PHI ) >>> 1 ) % n );
	}
	
	/** Returns a pseudorandom uniformly distributed {@code long} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence. The algorithm used to generate
     * the value guarantees that the result is uniform, provided that the
     * sequence of 64-bit values produced by this generator is. 
     * 
     * @param n the positive bound on the random number to be returned.
     * @return the next pseudorandom {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive).
     */
	public long nextLong( long n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
		// No special provision for n power of two: all our bits are good.
		while (true) {
			long bits = staffordMix13(x += PHI) >>> 1;
			long value = bits % n;
			if (bits - value + (n - 1) >= 0) return value;
		}
	}
	
	@Override
	 public double nextDouble() {
		return ( staffordMix13( x += PHI ) & DOUBLE_MASK ) * NORM_53;
	}
	
	@Override
	public float nextFloat() {
		return (float)( ( staffordMix4Upper32( x += PHI ) & FLOAT_MASK ) * NORM_24 );
	}

	@Override
	public boolean nextBoolean() {
		return staffordMix4Upper32( x += PHI ) < 0;
	}
	
	@Override
	public void nextBytes( byte[] bytes ) {
		int i = bytes.length, n = 0;
		while( i != 0 ) {
			n = Math.min( i, 8 );
			for ( long bits = staffordMix13( x += PHI ); n-- != 0; bits >>= 8 ) bytes[ --i ] = (byte)bits;
		}
	}


	/** Avalanches the bits of a long integer by applying the finalisation step of MurmurHash3.
	 *
	 * <p>This method implements the finalisation step of Austin Appleby's <a href="http://code.google.com/p/smhasher/">MurmurHash3</a>.
	 * Its purpose is to avalanche the bits of the argument to within 0.25% bias. It is used, among other things, to scramble quickly (but deeply) the hash
	 * values returned by {@link Object#hashCode()}.
	 *
	 * <p>Incidentally, iterating this method starting from a nonzero value will generate a sequence of nonzero
	 * values that <a href="http://prng.di.unimi.it/">passes strongest statistical tests</a>.
	 *
	 * @param x a long integer.
	 * @return a hash value with good avalanching properties.
	 */
	public static long murmurHash3(long x ) {
		x ^= x >>> 33;
		x *= 0xff51afd7ed558ccdL;
		x ^= x >>> 33;
		x *= 0xc4ceb9fe1a85ec53L;
		x ^= x >>> 33;
		return x;
	}

	/** Sets the seed of this generator.
	 * 
	 * <p>The seed will be passed through {@link HashCommon#murmurHash3(long)}.
	 * 
	 * @param seed a seed for this generator.
	 */
	@Override
	public void setSeed( long seed ) {
		x = murmurHash3( seed );
	}


	/** Sets the state of this generator.
	 * 
	 * @param state the new state for this generator (must be nonzero).
	 */
	public void setState( long state ) {
		x = state;
	}
}
