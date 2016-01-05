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


import java.io.*;


/** A class providing static methods and objects that do useful things with bit vectors.
 * 
 * @see BitVector
 */

public enum BitVectors {
	;


	public static void ensureFromTo( long bitVectorLength, long from, long to ) {
        if ( from < 0 ) throw new ArrayIndexOutOfBoundsException( "Start index (" + from + ") is negative" );
        if ( from > to ) throw new IllegalArgumentException( "Start index (" + from + ") is greater than end index (" + to + ')');
        if ( to > bitVectorLength ) throw new ArrayIndexOutOfBoundsException( "End index (" + to + ") is greater than bit vector length (" + bitVectorLength + ')');
    }
 	
	/** An immutable, singleton empty bit vector. */
	public static final BitVector EMPTY_VECTOR = new AbstractBitVector() {
		@Override
		public long length() { return 0; }
		@Override
		public BitVector copy(long from, long to ) {
			ensureFromTo( 0, from, to );
			return EMPTY_VECTOR; 
		}
		@Override
		public boolean getBoolean(long index ) { throw new IndexOutOfBoundsException(); }
		@Override
		public BitVector copy() {
			return this;
		}
		public Object readResolve() {
			return EMPTY_VECTOR;
		}
	};

	/** An immutable bit vector of length one containing a zero. */
	public static final BitVector ZERO = new AbstractBitVector() {
		@Override
		public long length() { return 1; }
		@Override
		public BitVector copy(long from, long to ) {
			ensureFromTo( 1, from, to );
			return from == to ? EMPTY_VECTOR : this; 
		}
		@Override
		public boolean getBoolean(long index ) { if ( index > 0 ) throw new IndexOutOfBoundsException(); else return false; }
		@Override
		public BitVector copy() {
			return this;
		}
		public Object readResolve() {
			return ZERO;
		}
	};

	/** An immutable bit vector of length one containing a one. */
	public static final BitVector ONE = new AbstractBitVector() {
		@Override
		public long length() { return 1; }
		@Override
		public BitVector copy(long from, long to ) {
			ensureFromTo( 1, from, to );
			return from == to ? EMPTY_VECTOR : this; 
		}
		@Override
		public boolean getBoolean(long index ) { if ( index > 0 ) throw new IndexOutOfBoundsException(); else return true; }
		@Override
		public BitVector copy() {
			return this;
		}
		public Object readResolve() {
			return ONE;
		}
	};
	
	/** Writes quickly a bit vector to a {@link DataOutputStream}. 
	 * 
	 * <p>This method writes a bit vector in a simple format: first, a long representing the length.
	 * Then, as many longs as necessary to write the bits in the bit vectors (i.e.,
	 * {@link LongArrayBitVector#numWords(long)} of the bit vector length), obtained via {@link BitVector#getLong(long, long)}.
	 * 
	 * <p>The main purpose of this function is to support {@link OfflineIterable} (see {@link #OFFLINE_SERIALIZER}).
	 * 
	 * @param v a bit vector.
	 * @param dos a data output stream.
	 */
	
	public static void writeFast( BitVector v, DataOutput dos ) throws IOException {
		long length = v.length();
		long l = length - length % Long.SIZE;
		dos.writeLong( length );
		long i;
		for( i = 0; i < l; i += Long.SIZE ) dos.writeLong( v.getLong( i, i + Long.SIZE ) );
		if ( i < length ) dos.writeLong( v.getLong( i, length ) );
	}
	
	/** Reads quickly a bit vector from a {@link DataInputStream}.
	 * 
	 * <p>This method is the dual of {@link #writeFast(BitVector, DataOutput)}. If you
	 * need to avoid creating a bit vector at each call, please have a look at {@link #readFast(DataInput, LongArrayBitVector)}.
	 * 
	 * @param dis a data input stream.
	 * @return the next bit vector in the stream, as saved by {@link  #writeFast(BitVector, DataOutput)}.
	 * @see #writeFast(BitVector, DataOutput)
	 * @see #readFast(DataInput, LongArrayBitVector)
	 */
	
	public static LongArrayBitVector readFast( DataInput dis ) throws IOException {
		long length = dis.readLong();
		long[] bits = new long[LongArrayBitVector.numWords(length)];
		int l = bits.length;
		for( int i = 0; i < l; i++ ) bits[ i ] = dis.readLong();
		return LongArrayBitVector.wrap( bits, length );
	}
	
	/** Reads quickly a bit vector from a {@link DataInputStream}.
	 * 
	 * <p>This method is similar in purpose to {@link #readFast(DataInput)}, but it allows reuse of the bit vector.
	 * 
	 * @param dis a data input stream.
	 * @param bv a long-array bit vector.
	 * @return <code>bv</code>, filled with the next bit vector in the stream, as saved by {@link  #writeFast(BitVector, DataOutput)}.
	 * @see #writeFast(BitVector, DataOutput)
	 * @see #readFast(DataInput)
	 */
	public static LongArrayBitVector readFast( DataInput dis, LongArrayBitVector bv ) throws IOException {
		long length = dis.readLong();
		bv.ensureCapacity( length );
		int l = LongArrayBitVector.numWords( length );
		for( int i = 0; i < l; i++ ) bv.bits[ i ] = dis.readLong();
		bv.length( length );
		return bv;
	}

//	private static class BitVectorOfflineSerializer implements OfflineIterable.Serializer<BitVector,LongArrayBitVector> {
//		public void write( BitVector bv, DataOutput dos ) throws IOException {
//			writeFast( bv, dos );
//		}
//
//		public void read( DataInput dis, LongArrayBitVector bv ) throws IOException {
//			readFast( dis, bv );
//		}
//	}

	/** A serializer for {@link LongArrayBitVector} instances that can be used with {@link it.unimi.dsi.io.OfflineIterable}. It can serialize
	 * any implementation of {@link BitVector}, and requires at construction time an instance of {@link LongArrayBitVector} that
	 * will be used to return deserialized elements. */
//	public static BitVectorOfflineSerializer OFFLINE_SERIALIZER = new BitVectorOfflineSerializer();
}
