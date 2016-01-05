package nars.util.data.bit;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2007-2015 Sebastiano Vigna 
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


/** All-purpose optimised bit-fiddling static-method container class.
  *
  * <p>This class used to contain a large number of bits hacks. Intrinsification of methods
  * such as {@link Long#bitCount(long)}, {@link Long#numberOfTrailingZeros(long)}, etc. using
  * SSE instructions has made such hacks obsolete.
  * 
  * <p>The main highlight is now a new algorithm for {@linkplain #select(long, int) selection} that is twice as
  * fast as the one previously implemented, but that will behave impredictably if there is no bit with the requested rank; the 
  * algorithm is based on the one presented
  * by Sebastiano Vigna in &ldquo;<a href="http://vigna.dsi.unimi.it/papers.php#VigBIRSQ">Broadword Implementation of Rank/Select Queries</a>&rdquo;,
  * <i>Proc. of the 7th International Workshop on Experimental Algorithms, WEA 2008</i>,
  * number 5038 in Lecture Notes in Computer Science, pages 154&minus;168. Springer&ndash;Verlag, 2008, but it
  * has been improved with ideas from Simon Gog's <a href="https://github.com/simongog/sdsl">SDSL library</a>. 
  * 
  * @author Sebastiano Vigna
  * @since 0.1
  */

public enum Fast {
	;

	public static final long ONES_STEP_4 = 0x1111111111111111L;
	public static final long ONES_STEP_8 = 0x0101010101010101L;
	public static final long MSBS_STEP_8 = 0x80L * ONES_STEP_8;
	@Deprecated
	public static final long INCR_STEP_8 = 0x80L << 56 | 0x40L << 48 | 0x20L << 40 | 0x10L << 32 | 0x8L << 24 | 0x4L << 16 | 0x2L << 8 | 0x1;

	/** Precomputed least significant bits for bytes (-1 for 0 ). */
	public static final int[] BYTELSB = {
		-1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
		4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0
	};
	    

	/** Precomputed most significant bits for bytes (-1 for 0 ). */
	public static final int[] BYTEMSB = {
		-1, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
	};
	
	/** A precomputed table containing in position 256<var>i</var> + <var>j</var> the position of the <var>i</var>-th one (0 &le; <var>j</var> &lt; 8) in the binary representation of <var>i</var>
	 * (0 &le; <var>i</var> &lt; 256), or -1 if no such bit exists. */
	public static final byte[] selectInByte = {
		-1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, -1, -1, -1, 1, -1, 2, 2, 1, -1, 3, 3, 1, 3, 2, 2, 1, -1, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, -1, 5, 5, 1, 5, 2, 2, 1, 5, 3, 3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, -1, 6, 6, 1, 6, 2, 2, 1, 6, 3, 3, 1, 3, 2, 2, 1, 6, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 6, 5, 5, 1, 5, 2, 2, 1, 5, 3, 3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, -1, 7, 7, 1, 7, 2, 2, 1, 7, 3, 3, 1, 3, 2, 2, 1, 7, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 7, 5, 5, 1, 5, 2, 2, 1, 5, 3, 3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 7, 6, 6, 1, 6, 2, 2, 1, 6, 3, 3, 1, 3, 2, 2, 1, 6, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 6, 5, 5, 1, 5, 2, 2, 1, 5, 3, 3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, -1, -1, -1, -1, -1, -1, -1, 2, -1, -1, -1, 3, -1, 3, 3, 2, -1, -1, -1, 4, -1, 4, 4, 2, -1, 4, 4, 3, 4, 3, 3, 2, -1, -1, -1, 5, -1, 5, 5, 2, -1, 5, 5, 3, 5, 3, 3, 2, -1, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3, 3, 2, -1, -1, -1, 6, -1, 6, 6, 2, -1, 6, 6, 3, 6, 3, 3, 2, -1, 6, 6, 4, 6, 4, 4, 2, 6, 4, 4, 3, 4, 3, 3, 2, -1, 6, 6, 5, 6, 5, 5, 2, 6, 5, 5, 3, 5, 3, 3, 2, 6, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3, 3, 2, -1, -1, -1, 7, -1, 7, 7, 2, -1, 7, 7, 3, 7, 3, 3, 2, -1, 7, 7, 4, 7, 4, 4, 2, 7, 4, 4, 3, 4, 3, 3, 2, -1, 7, 7, 5, 7, 5, 5, 2, 7, 5, 5, 3, 5, 3, 3, 2, 7, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3, 3, 2, -1, 7, 7, 6, 7, 6, 6, 2, 7, 6, 6, 3, 6, 3, 3, 2, 7, 6, 6, 4, 6, 4, 4, 2, 6, 4, 4, 3, 4, 3, 3, 2, 7, 6, 6, 5, 6, 5, 5, 2, 6, 5, 5, 3, 5, 3, 3, 2, 6, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3, 3, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, 4, -1, 4, 4, 3, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, 5, -1, 5, 5, 3, -1, -1, -1, 5, -1, 5, 5, 4, -1, 5, 5, 4, 5, 4, 4, 3, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, 6, -1, 6, 6, 3, -1, -1, -1, 6, -1, 6, 6, 4, -1, 6, 6, 4, 6, 4, 4, 3, -1, -1, -1, 6, -1, 6, 6, 5, -1, 6, 6, 5, 6, 5, 5, 3, -1, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 3, -1, -1, -1, 7, -1, 7, 7, 4, -1, 7, 7, 4, 7, 4, 4, 3, -1, -1, -1, 7, -1, 7, 7, 5, -1, 7, 7, 5, 7, 5, 5, 3, -1, 7, 7, 5, 7, 5, 5, 4, 7, 5, 5, 4, 5, 4, 4, 3, -1, -1, -1, 7, -1, 7, 7, 6, -1, 7, 7, 6, 7, 6, 6, 3, -1, 7, 7, 6, 7, 6, 6, 4, 7, 6, 6, 4, 6, 4, 4, 3, -1, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 3, 7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, 5, -1, 5, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, 6, -1, 6, 6, 4, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, 6, -1, 6, 6, 5, -1, -1, -1, 6, -1, 6, 6, 5, -1, 6, 6, 5, 6, 5, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 4, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 5, -1, -1, -1, 7, -1, 7, 7, 5, -1, 7, 7, 5, 7, 5, 5, 4, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, 7, -1, 7, 7, 6, -1, 7, 7, 6, 7, 6, 6, 4, -1, -1, -1, 7, -1, 7, 7, 6, -1, 7, 7, 6, 7, 6, 6, 5, -1, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, 6, -1, 6, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, 7, -1, 7, 7, 6, -1, 7, 7, 6, 7, 6, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7
	};


	/** Maps integers bijectively into natural numbers.
	 * 
	 * <P>This method will map a negative integer <var>x</var> to -2<var>x</var>-1 and
	 * a nonnegative integer <var>x</var> to 2<var>x</var>. It can be used to save 
	 * integers in the range [{@link Integer#MIN_VALUE}/2..{@link Integer#MAX_VALUE}/2] 
	 * (i.e., [-2<sup>30</sup>..2<sup>30</sup>-1])
	 * using the standard coding methods (which all work on natural numbers). Note
	 * that no range checks are performed.
	 * 
	 * <P>The inverse of the above map is computed by {@link #nat2int(int)}.
	 *
	 * @param x an integer.
	 * @return the argument mapped into a natural number.
	 * @see #nat2int(int)
	 */

	public static int int2nat( int x ) {
		return x >= 0 ? x << 1 : -( ( x << 1 ) + 1 );
	}

	/** Maps natural numbers bijectively into integers.
	 * 
	 * <P>This method computes the inverse of {@link #int2nat(int)}.
	 *
	 * @param x a natural  number.
	 * @return the argument mapped into an integer.
	 * @see #int2nat(int)
	 */

	public static int nat2int( int x ) {
		return x % 2 == 0 ? x >> 1 : -( x >> 1 ) - 1;
	}

	/** Maps longs bijectively into long natural numbers.
	 * 
	 * <P>This method will map a negative long <var>x</var> to -2<var>x</var>-1 and
	 * a nonnegative long <var>x</var> to 2<var>x</var>. It can be used to save 
	 * longs in the range [{@link Long#MIN_VALUE}/2..{@link Long#MAX_VALUE}/2] 
	 * (i.e., [-2<sup>62</sup>..2<sup>62</sup>-1])
	 * using the standard coding methods (which all work on natural numbers). Note
	 * that no range checks are performed.
	 * 
	 * <P>The inverse of the above map is computed by {@link #nat2int(long)}.
	 *
	 * @param x a long.
	 * @return the argument mapped into a long natural number.
	 * @see #int2nat(int)
	 */

	public static long int2nat( long x ) {
		return x >= 0 ? x << 1 : -( ( x << 1 ) + 1 );
	}

	/** Maps long natural numbers bijectively into longs.
	 * 
	 * <P>This method computes the inverse of {@link #int2nat(long)}.
	 *
	 * @param x a long natural  number.
	 * @return the argument mapped into a long.
	 * @see #nat2int(int)
	 */

	public static long nat2int( long x ) {
		return x % 2 == 0 ? x >> 1 : -( x >> 1 ) - 1;
	}

	/** Returns the base-two logarithm of the argument.
	 * 
	 * @param x a double.
	 * @return the base-2 logarithm of <code>x</code>.
	 */
	public static double log2( double x ) {
		return Math.log( x ) / 0.6931471805599453;
	}

	/** Computes the ceiling of the base-two logarithm of the argument.
	 *
	 * <p>This method relies on {@link #mostSignificantBit(int)}, and thus is pretty fast.
	 * 
	 * @param x an integer.
	 * @return the ceiling of the base-two logarithm of the argument, or -1 if <code>x</code> is zero.
	 */
	public static int ceilLog2( int x ) {
		if ( x <= 2 ) return x - 1;
		return Integer.SIZE - Integer.numberOfLeadingZeros( x - 1 );
	}

	/** Computes the ceiling of the base-two logarithm of the argument.
	 * 
	 * <p>This method relies on {@link #mostSignificantBit(long)}, and thus is pretty fast.
	 * 
	 * @param x an integer.
	 * @return the ceiling of the base-two logarithm of the argument, or -1 if <code>x</code> is zero.
	 */
	public static int ceilLog2( long x ) {
		if ( x <= 2 ) return (int)( x - 1 );
		return Long.SIZE - Long.numberOfLeadingZeros( x - 1 );
	}

	/** Computes an approximate integer base-2 logarithm of the argument.
	 * 
	 * <p>This method relies on {@link Double#doubleToRawLongBits(double)}, and thus is very
	 * fast if the former is intrinsified by the JVM.
	 * 
	 * @param x a double.
	 * @return an integer approximation of the base-two logarithm of the argument.
	 */
	public static int approximateLog2( double x ) {
		long bits = Double.doubleToRawLongBits( x );
		// The exponent is corrected by one if the first significand digits are big enough.
		return (int)( ( bits >>> 52 ) & 0x7FF ) - 1023 + ( ( bits >>> 48 & 0xF ) > 6 ? 1 : 0 );
	}

	/** Quickly raises 2 to the argument.
	 * 
	 * @param exponent an integer exponent between -62 ad 62.
	 * @return 2<sup><code>exponent</code></sup>.
	 */
	public static double pow2( int exponent ) {
		//return fixedValues[ approximate + ( 1 << EXPONENT_BITS ) - ADJUSTMENT - 1 ];
		if ( exponent < 0 ) return 1.0 / ( 1L  << -exponent );
		return 1L << exponent;
	}

	
	/** Returns the number of bits that are necessary to encode the argument.
	 * 
	 * @param x an integer.
	 * @return the number of bits that are necessary to encode <code>x</code>.
	 */
	public static int length( int x ) {
		return x == 0 ? 1 : mostSignificantBit( x ) + 1;
	}

	/** Returns the number of bits that are necessary to encode the argument.
	 * 
	 * @param x a long.
	 * @return the number of bits that are necessary to encode <code>x</code>.
	 */
	public static int length( long x ) {
		return x == 0 ? 1 : mostSignificantBit( x ) + 1;
	}

	private static int selectBroadword( long x, int rank ) {
        // Phase 1: sums by byte
        long byteSums = x - ( ( x & 0xa * ONES_STEP_4 ) >>> 1 );
        byteSums = ( byteSums & 3 * ONES_STEP_4 ) + ( ( byteSums >>> 2 ) & 3 * ONES_STEP_4 );
        byteSums = ( byteSums + ( byteSums >>> 4 ) ) & 0x0f * ONES_STEP_8;
        byteSums *= ONES_STEP_8;
        
        // Phase 2: compare each byte sum with rank to obtain the relevant byte
        long rankStep8 = rank * ONES_STEP_8;
        long byteOffset = ( ( ( ( ( rankStep8 | MSBS_STEP_8 ) - byteSums ) & MSBS_STEP_8 ) >>> 7 ) * ONES_STEP_8 >>> 53 ) & ~0x7;

        // Phase 3: Locate the relevant byte and make 8 copies with incremental masks
        int byteRank = (int)( rank - ( ( ( byteSums << 8 ) >>> byteOffset ) & 0xFF ) );

        long spreadBits = ( x >>> byteOffset & 0xFF ) * ONES_STEP_8 & INCR_STEP_8;
        long bitSums = ( ( ( spreadBits | ( ( spreadBits | MSBS_STEP_8 ) - ONES_STEP_8 ) ) & MSBS_STEP_8 ) >>> 7 ) * ONES_STEP_8;

        // Compute the inside-byte location and return the sum
        long byteRankStep8 = byteRank * ONES_STEP_8;

        return (int)( byteOffset + ( ( ( ( ( byteRankStep8 | MSBS_STEP_8 ) - bitSums ) & MSBS_STEP_8 ) >>> 7 ) * ONES_STEP_8 >>> 56 ) );
	}

	private static final long[] overflow = {
			0x7f7f7f7f7f7f7f7fL,
			0x7e7e7e7e7e7e7e7eL,
			0x7d7d7d7d7d7d7d7dL,
			0x7c7c7c7c7c7c7c7cL,
			0x7b7b7b7b7b7b7b7bL,
			0x7a7a7a7a7a7a7a7aL,
			0x7979797979797979L,
			0x7878787878787878L,
			0x7777777777777777L,
			0x7676767676767676L,
			0x7575757575757575L,
			0x7474747474747474L,
			0x7373737373737373L,
			0x7272727272727272L,
			0x7171717171717171L,
			0x7070707070707070L,
			0x6f6f6f6f6f6f6f6fL,
			0x6e6e6e6e6e6e6e6eL,
			0x6d6d6d6d6d6d6d6dL,
			0x6c6c6c6c6c6c6c6cL,
			0x6b6b6b6b6b6b6b6bL,
			0x6a6a6a6a6a6a6a6aL,
			0x6969696969696969L,
			0x6868686868686868L,
			0x6767676767676767L,
			0x6666666666666666L,
			0x6565656565656565L,
			0x6464646464646464L,
			0x6363636363636363L,
			0x6262626262626262L,
			0x6161616161616161L,
			0x6060606060606060L,
			0x5f5f5f5f5f5f5f5fL,
			0x5e5e5e5e5e5e5e5eL,
			0x5d5d5d5d5d5d5d5dL,
			0x5c5c5c5c5c5c5c5cL,
			0x5b5b5b5b5b5b5b5bL,
			0x5a5a5a5a5a5a5a5aL,
			0x5959595959595959L,
			0x5858585858585858L,
			0x5757575757575757L,
			0x5656565656565656L,
			0x5555555555555555L,
			0x5454545454545454L,
			0x5353535353535353L,
			0x5252525252525252L,
			0x5151515151515151L,
			0x5050505050505050L,
			0x4f4f4f4f4f4f4f4fL,
			0x4e4e4e4e4e4e4e4eL,
			0x4d4d4d4d4d4d4d4dL,
			0x4c4c4c4c4c4c4c4cL,
			0x4b4b4b4b4b4b4b4bL,
			0x4a4a4a4a4a4a4a4aL,
			0x4949494949494949L,
			0x4848484848484848L,
			0x4747474747474747L,
			0x4646464646464646L,
			0x4545454545454545L,
			0x4444444444444444L,
			0x4343434343434343L,
			0x4242424242424242L,
			0x4141414141414141L,
			0x4040404040404040L
	};

//	private static int selectGogPetri( final long x, final int rank ) {
//		assert rank < Long.bitCount( x );
//		// Phase 1: sums by byte
//		long byteSums = x - ( ( x >>> 1 ) & 0x5 * ONES_STEP_4 );
//		byteSums = ( byteSums & 3 * ONES_STEP_4 ) + ( ( byteSums >>> 2 ) & 3 * ONES_STEP_4 );
//		byteSums = ( byteSums + ( byteSums >>> 4 ) ) & 0x0f * ONES_STEP_8;
//		byteSums *= ONES_STEP_8;
//
//		// Phase 2: compare each byte sum with rank to obtain the relevant byte
//		final int byteOffset = ( Long.numberOfTrailingZeros( byteSums + overflow[ rank ] & 0x8080808080808080L ) >> 3 ) << 3;
//
//		return byteOffset + selectInByte[ (int)( x >>> byteOffset & 0xFF ) | (int)( rank - ( ( ( byteSums << 8 ) >>> byteOffset ) & 0xFF ) ) << 8 ];
//	}


	/** Returns the position of a bit of given rank (starting from zero).
	 * 
	 * @param x a long.
	 * @param rank an integer smaller than the number of ones in {@code x}; impredictable
	 * results (including exceptions) might happen if this constraint is violated.
	 * @return the position in <code>x</code> of the bit of given rank.
	 */
	public static int select( long x, int rank ) {
		assert rank < Long.bitCount( x );
		// Phase 1: sums by byte
		long byteSums = x - ( ( x >>> 1 ) & 0x5 * ONES_STEP_4 );
		byteSums = ( byteSums & 3 * ONES_STEP_4 ) + ( ( byteSums >>> 2 ) & 3 * ONES_STEP_4 );
		byteSums = ( byteSums + ( byteSums >>> 4 ) ) & 0x0f * ONES_STEP_8;
		byteSums *= ONES_STEP_8;

		// Phase 2: compare each byte sum with rank to obtain the relevant byte
		int byteOffset = Long.bitCount( ( ( rank * ONES_STEP_8 | MSBS_STEP_8 ) - byteSums ) & MSBS_STEP_8 ) << 3;

		return byteOffset + selectInByte[ (int)( x >>> byteOffset & 0xFF ) | (int)( rank - ( ( ( byteSums << 8 ) >>> byteOffset ) & 0xFF ) ) << 8 ];
	}


	/** Returns the most significant bit of a long.
	 * 
	 * <p>This method returns 63 &minus; {@link Long#numberOfLeadingZeros(long) Long.numberOfLeadingZeroes( x )}.
	 * 
	 * @param x a long.
	 * @return the most significant bit of <code>x</code>, of <code>x</code> is nonzero; &minus;1, otherwise.
	 */
	public static int mostSignificantBit( long x ) {
		return 63 - Long.numberOfLeadingZeros( x );
	}

	/** Returns the most significant bit of an integer.
	 * 
	 * @param x an integer.
	 * @return the most significant bit of <code>x</code>, of <code>x</code> is nonzero; &minus;1, otherwise.
	 * @see #mostSignificantBit(long)
	 */
	public static int mostSignificantBit( int x ) {
		return 31 - Integer.numberOfLeadingZeros( x );
	}
	

//	public static void main( final String a[] ) {
//		final long n = Long.parseLong( a[ 0 ] );
//
//		long start, elapsed;
//
//		long w = 0xFFFFFFFFFFFFFFFFL;
//		int x = 0;
//
//		for( int k = 10; k-- !=0;  ) {
//			System.out.print( "Broadword select (new): " );
//
//			start = System.nanoTime();
//			for( long i = n; i-- != 0; ) x ^= Fast.select( w, (int)( i & 63 ) );
//			elapsed = System.nanoTime() - start;
//
//			System.out.println( "elapsed " + elapsed + ", " + (double)elapsed / n + " ns/call" );
//
//			System.out.print( "Broadword select (Gog & Petri): " );
//
//			start = System.nanoTime();
//			for( long i = n; i-- != 0; ) x ^= Fast.selectGogPetri( w, (int)( i & 63 ) );
//			elapsed = System.nanoTime() - start;
//
//			System.out.println( "elapsed " + elapsed + ", " + (double)elapsed / n + " ns/call" );
//
//			System.out.print( "Broadword select (old): " );
//
//			start = System.nanoTime();
//			for( long i = n; i-- != 0; ) x ^= Fast.selectBroadword( w, (int)( i & 63 ) );
//			elapsed = System.nanoTime() - start;
//
//			System.out.println( "elapsed " + elapsed + ", " + (double)elapsed / n + " ns/call" );
//
//		}
//
//		if ( x == 0 ) System.out.println( 0 );
//
//	}
	
}
