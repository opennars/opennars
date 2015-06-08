package nars.util.data.random;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2013-2015 Sebastiano Vigna 
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

/** A fast, top-quality {@linkplain Random pseudorandom number generator} that
 * combines a long-period instance of George Marsaglia's Xorshift generators (described in <a
 * href="http://www.jstatsoft.org/v08/i14/paper/">&ldquo;Xorshift RNGs&rdquo;</a>, <i>Journal of
 * Statistical Software</i>, 8:1&minus;6, 2003) with a multiplication.
 * 
 * <p><strong>Warning</strong>: in version 2.2.3 the seeding procedure has been changed, as
 * it now uses a {@link SplitMix64RandomGenerator}.
 *  
 * <p>More details can be found on the <a href="http://xorshift.di.unimi.it/"><code>xorshift*</code>/<code>xorshift+</code> generators and the PRNG shootout</a> page.
 *
 * <p>Note that this is <strong>not</strong> a cryptographic-strength pseudorandom number generator. Its period is
 * 2<sup>1024</sup>&nbsp;&minus;&nbsp;1, which is more than enough for any massive parallel application (it is actually
 * possible to define analogously a generator with period 2<sup>4096</sup>&nbsp;&minus;&nbsp;1,
 * but its interest is eminently academic). 
 * 
 * @see it.unimi.dsi.util
 * @see Random
 * @see XorShift1024StarRandomGenerator
 */
public class XorShift1024StarRandom extends Random {
	private static final long serialVersionUID = 1L;

	/** 2<sup>-53</sup>. */
	private static final double NORM_53 = 1. / ( 1L << 53 );
	/** 2<sup>-24</sup>. */
	private static final double NORM_24 = 1. / ( 1L << 24 );

	/** The internal state of the algorithm. */
	private long[] s;
	private int p;
	

	/** Creates a new generator using a given seed.
	 * 
	 * @param seed a nonzero seed for the generator (if zero, the generator will be seeded with -1).
	 */
	public XorShift1024StarRandom( final long seed ) {
		super( seed );
	}

	@Override
	protected int next( int bits ) {
		return (int)( nextLong() >>> 64 - bits );
	}
	
	@Override
	public long nextLong() {
		final long s0 = s[ p ];
		long s1 = s[ p = ( p + 1 ) & 15 ];
		s1 ^= s1 << 31;
		return ( s[ p ] = s1 ^ s0 ^ ( s1 >>> 11 ) ^ ( s0 >>> 30 ) ) * 1181783497276652981L;
	}

	@Override
	public int nextInt() {
		return (int)nextLong();
	}
	
	@Override
	public int nextInt( final int n ) {
		return (int)nextLong( n );
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
	public long nextLong( final long n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
		// No special provision for n power of two: all our bits are good.
		for(;;) {
			final long bits = nextLong() >>> 1;
			final long value = bits % n;
			if ( bits - value + ( n - 1 ) >= 0 ) return value;
		}
	}
	
	@Override
	 public double nextDouble() {
		return ( nextLong() >>> 11 ) * NORM_53;
	}
	
	@Override
	public float nextFloat() {
		return (float)( ( nextLong() >>> 40 ) * NORM_24 );
	}

	@Override
	public boolean nextBoolean() {
		return nextLong() < 0;
	}
	
	@Override
	public void nextBytes( final byte[] bytes ) {
		int i = bytes.length, n = 0;
		while( i != 0 ) {
			n = Math.min( i, 8 );
			for ( long bits = nextLong(); n-- != 0; bits >>= 8 ) bytes[ --i ] = (byte)bits;
		}
	}

//	/** Sets the seed of this generator.
//	 *
//	 * <p>The argument will be used to seed a {@link SplitMix64RandomGenerator}, whose output
//	 * will in turn be used to seed this generator. This approach makes &ldquo;warmup&rdquo; unnecessary,
//	 * and makes the possibility of starting from a state
//	 * with a large fraction of bits set to zero astronomically small.
//	 *
//	 * @param seed a nonzero seed for the generator (if zero, the generator will be seeded with -1).
//	 */
//	@Override
//	public void setSeed( final long seed ) {
//		if ( s == null ) s = new long[ 16 ];
//		p = 0;
//		final SplitMix64RandomGenerator r = new SplitMix64RandomGenerator( seed );
//		for( int i = s.length; i-- != 0; ) s[ i ] = r.nextLong();
//	}

	/** Sets the state of this generator.
	 * 
	 * <p>The internal state of the generator will be reset, and the state array filled with the provided array.
	 * 
	 * @param state an array of 16 longs; at least one must be nonzero.
	 * @param p the internal index. 
	 */
	public void setState( final long[] state, final int p ) {
		if ( state.length != s.length ) throw new IllegalArgumentException( "The argument array contains " + state.length + " longs instead of " + s.length );
		System.arraycopy( state, 0, s, 0, s.length );
		this.p = p;
	}
}
