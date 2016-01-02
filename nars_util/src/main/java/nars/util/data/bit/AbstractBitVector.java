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

//import it.unimi.dsi.fastutil.BigList;
//import it.unimi.dsi.fastutil.booleans.AbstractBooleanBigList;
//import it.unimi.dsi.fastutil.longs.AbstractLongBidirectionalIterator;
//import it.unimi.dsi.fastutil.longs.AbstractLongBigList;
//import it.unimi.dsi.fastutil.longs.AbstractLongBigListIterator;
//import it.unimi.dsi.fastutil.longs.AbstractLongSortedSet;
//import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
//import it.unimi.dsi.fastutil.longs.LongBigList;
//import it.unimi.dsi.fastutil.longs.LongComparator;
//import it.unimi.dsi.fastutil.longs.LongSet;
//import it.unimi.dsi.fastutil.longs.LongSortedSet;

/** An abstract implementation of a {@link BitVector}.
 * 
 * <P>This abstract implementation provides almost all methods: you have to provide just
 * {@link it.unimi.dsi.fastutil.booleans.BooleanList#getBoolean(int)} and
 * {@link java.util.List#size()}. No attributes are defined.
 * 
 * <P>Note that the integer-set view provided by {@link #asLongSet()} is not cached: if you
 * want to cache the result of the first call, you must do your own caching.
 * 
 * <p><strong>Warning</strong>: this class has several optimised methods
 * that assume that {@link #getLong(long, long)} is implemented efficiently when its
 * arguments are multiples of {@link Long#SIZE} (see, e.g., the implementation
 * of {@link #compareTo(BitVector)} and {@link #longestCommonPrefixLength(BitVector)}).
 * If you want speed up the processing of your own {@link BitVector} implementations,
 * just implement {@link #getLong(long, long)} so that it is fast under the above conditions. 
 */
public abstract class AbstractBitVector implements BitVector {
	
    protected void ensureRestrictedIndex( long index ) {
        if ( index < 0 )  throw new IndexOutOfBoundsException( "Index (" + index + ") is negative" );
        if ( index >= length() ) throw new IndexOutOfBoundsException( "Index (" + index + ") is greater than or equal to length (" + ( length() ) + ')');
    }
	
    protected void ensureIndex( long index ) {
        if ( index < 0 )  throw new IndexOutOfBoundsException( "Index (" + index + ") is negative" );
        if ( index > length() ) throw new IndexOutOfBoundsException( "Index (" + index + ") is greater than length (" + ( length() ) + ')');
    }

    public void set( int index ) { set( index, true ); }
	public void clear( int index ) { set( index, false ); }
	public void flip( int index ) { set( index, ! getBoolean( index ) ); }

	@Override
	public void set(long index ) { set( index, true ); }
	@Override
	public void clear(long index ) { set( index, false ); }
	@Override
	public void flip(long index ) { set( index, ! getBoolean( index ) ); }
	
	@Override
	public void fill(boolean value ) { for(long i = length(); i-- != 0; ) set( i, value ); }
	@Override
	public void fill(int value ) { fill( value != 0 ); }
	@Override
	public void flip() { for(long i = length(); i-- != 0; ) flip( i ); }

	@Override
	public void fill(long from, long to, boolean value ) { BitVectors.ensureFromTo( length(), from, to ); for(long i = to; i-- != from; ) set( i, value ); }
	@Override
	public void fill(long from, long to, int value ) { fill( from, to, value != 0 ); }
	@Override
	public void flip(long from, long to ) { BitVectors.ensureFromTo( length(), from, to ); for(long i = to; i-- != from; ) flip( i ); }

	@Override
	public int getInt(long index ) { return getBoolean( index ) ? 1 : 0; }
	@Override
	public long getLong(long from, long to ) {
		if ( to - from > 64 ) throw new IllegalArgumentException( "Range too large for a long: [" + from + ".." + to + ')');
		long result = 0;
		for( long i = from; i < to; i++ ) if ( getBoolean( i ) ) result |= 1L << i - from;
		return result;
	}
	public boolean getBoolean( int index ) { return getBoolean( (long)index ); }

	public boolean removeBoolean( int index ) { return removeBoolean( (long)index ); }
	public boolean set( int index, boolean value ) { return set( (long)index, value ); }
	public void add( int index, boolean value ) { add( (long)index, value ); }

	@Override
	public boolean removeBoolean(long index ) { throw new UnsupportedOperationException(); }
	@Override
	public boolean set(long index, boolean value ) { throw new UnsupportedOperationException(); }
	@Override
	public void add(long index, boolean value ) { throw new UnsupportedOperationException(); }
	
	@Override
	public void set(long index, int value ) { set( index, value != 0 ); }
	@Override
	public void add(long index, int value ) { add( index, value != 0 ); }
	public boolean add( boolean value ) { add( length(), value ); return true; }
	@Override
	public void add(int value ) { add( value != 0 ); }

	@Override
	public BitVector append(long value, int k ) {
		for( int i = 0; i < k; i++ ) add( ( value & 1L << i ) != 0 );
		return this;
	}

	@Override
	public BitVector append(BitVector bv ) {
		long length = bv.length();
		long l = length - length % Long.SIZE;
		
		long i;
		for( i = 0; i < l; i += Long.SIZE ) append( bv.getLong( i, i + Long.SIZE ), Long.SIZE );
		if ( i < length ) append( bv.getLong( i, length ), (int)( length - i ) );
		return this;
	}

	@Override
	public BitVector copy() { return copy( 0, size() ); }

	@Override
	public BitVector copy(long from, long to ) {
		BitVectors.ensureFromTo( length(), from, to );
		long length = to - from;
		long l = length - length % Long.SIZE;
		long[] bits = new long[(int) ((length + Long.SIZE - 1) / Long.SIZE)];
		long i;
		for( i = 0; i < l; i += Long.SIZE ) bits[ (int)( i / Long.SIZE ) ] = getLong( from + i, from + i + Long.SIZE );
		if ( i < length ) bits[ (int)( i / Long.SIZE ) ] = getLong( from + i, to );
		return LongArrayBitVector.wrap( bits, length );
	}
	
	/** Returns an instance of {@link LongArrayBitVector} containing a copy of this bit vector.
	 *
	 * @return an instance of {@link LongArrayBitVector} containing a copy of this bit vector.
	 */
	
	@Override
	public BitVector fast() {
		return copy();
	}
	
	@Override
	public long count() {
		long c = 0;
		for( long i = length(); i-- != 0; ) c += getInt( i );
		return c;
	}
	
	@Override
	public long firstOne() {
		return nextOne( 0 );
	}
	
	@Override
	public long lastOne() {
		return previousOne( length() );
	}
	
	@Override
	public long firstZero() {
		return nextZero( 0 );
	}
	
	@Override
	public long lastZero() {
		return previousZero( length() );
	}
	
	@Override
	public long nextOne(long index ) {
		long length = length();
		for( long i = index; i < length; i++ ) if ( getBoolean( i ) ) return i;
		return -1;
	}
	
	@Override
	public long previousOne(long index ) {
		for ( long i = index; i-- != 0; ) if ( getBoolean( i ) ) return i;
		return -1;
	}
	
	@Override
	public long nextZero(long index ) {
		long length = length();
		for( long i = index; i < length; i++ ) if ( ! getBoolean( i ) ) return i;
		return -1;
	}
	
	@Override
	public long previousZero(long index ) {
		for ( long i = index; i-- != 0; ) if ( ! getBoolean( i ) ) return i;
		return -1;
	}
	
	@Override
	public long longestCommonPrefixLength(BitVector v ) {
		long minLength = Math.min( length(), v.length() );
		long l = minLength - minLength % Long.SIZE;
		long w0, w1;
		
		long i;
		for( i = 0; i < l; i += Long.SIZE ) {
			w0 = getLong( i, i + Long.SIZE );
			w1 = v.getLong( i, i + Long.SIZE );
			if ( w0 != w1 ) return i + Long.numberOfTrailingZeros( w0 ^ w1 );
		}

		w0 = getLong( i, minLength );
		w1 = v.getLong( i, minLength );

		if ( w0 != w1 ) return i + Long.numberOfTrailingZeros( w0 ^ w1 );
		return minLength;
	}	
	
	@Override
	public boolean isPrefix(BitVector v ) {
		return longestCommonPrefixLength( v ) == length();
	}
	
	@Override
	public boolean isProperPrefix(BitVector v ) {
		return isPrefix( v ) && length() < v.length();
	}
	
	@Override
	public BitVector and(BitVector v ) {
		for( long i = Math.min( size64(), v.size64() ); i-- != 0; ) if ( ! v.getBoolean( i ) ) clear( i );
		return this;
	}
	
	@Override
	public BitVector or(BitVector v ) {
		for( long i = Math.min( size64(), v.size64() ); i-- != 0; ) if ( v.getBoolean( i ) ) set( i );
		return this;
	}

	@Override
	public BitVector xor(BitVector v ) {
		for( long i = Math.min( size64(), v.size64() ); i-- != 0; ) if ( v.getBoolean( i ) ) flip( i );
		return this;
	}

	@Deprecated
	public int size() {
		long length = length();
		if ( length > Integer.MAX_VALUE ) throw new IllegalStateException( "The number of bits of this bit vector (" + length + ") exceeds Integer.MAX_INT" );
		return (int)length;
	}
	
	public void size( int newSize ) {
		length( newSize );
	}
	
	@Override
	public long size64() {
		return length();
	}
	
	public void clear() {
		length( 0 );
	}
	
	@Override
	public BitVector replace(BitVector bv ) {
		clear();
		long fullBits = bv.length() - bv.length() % Long.SIZE;
		for( long i = 0; i < fullBits; i += Long.SIZE ) append( bv.getLong( i, i + Long.SIZE ), Long.SIZE );
		if ( bv.length() % Long.SIZE != 0 ) append( bv.getLong( fullBits, bv.length() ), (int)( bv.length() - fullBits ) );
		return this;
	}

	public boolean equals( Object o ) {
		if ( ! ( o instanceof BitVector ) ) return false;
		BitVector v = (BitVector)o;
		long length = length();
		if ( length != v.length() ) return false;
		long fullLength = length - length % Long.SIZE;
		for( long i = 0; i < fullLength; i += Long.SIZE ) if ( getLong( i, i + Long.SIZE ) != v.getLong( i, i + Long.SIZE ) ) return false;
		return getLong( fullLength, length ) == v.getLong( fullLength, length );
	}

	@Override
	public boolean equals(BitVector v, long start, long end ) {
		long startFull = start - start % LongArrayBitVector.BITS_PER_WORD;
		long endFull = end - end % LongArrayBitVector.BITS_PER_WORD;
		int startBit = (int)( start & LongArrayBitVector.WORD_MASK );
		int endBit = (int)( end & LongArrayBitVector.WORD_MASK );
		
		if ( startFull == endFull )
				return ( ( getLong( startFull, Math.min( length(), startFull + Long.SIZE ) ) 
						^ v.getLong( startFull, Math.min( v.length(), startFull + Long.SIZE ) ) )
							& ( ( 1L << ( endBit - startBit ) ) - 1 ) << startBit ) == 0;
		
		if ( ( ( getLong( startFull, startFull + Long.SIZE ) ^ v.getLong( startFull, startFull += Long.SIZE ) ) & ( -1L << startBit ) ) != 0 ) return false; 

		while( startFull < endFull ) if ( getLong( startFull, startFull + Long.SIZE ) != v.getLong( startFull, startFull += Long.SIZE ) ) return false;
		
		return ( ( getLong( startFull, Math.min( length(), startFull + Long.SIZE ) ) 
				^ v.getLong( startFull, Math.min( v.length(), startFull + Long.SIZE ) ) )
					& ( 1L << endBit) - 1 ) == 0;
	}


	
	public int hashCode() {
		long length = length();
		long fullLength = length - length % Long.SIZE;
		long h = 0x9e3779b97f4a7c13L ^ length;

		for( long i = 0; i < fullLength; i += Long.SIZE ) h ^= ( h << 5 ) + getLong( i, i + Long.SIZE ) + ( h >>> 2 );
		if ( length != fullLength ) h ^= ( h << 5 ) + getLong( fullLength, length ) + ( h >>> 2 );

		return (int)( ( h >>> 32 ) ^ h );
	}
	
	@Override
	public long[] bits() {
		long[] bits = new long[ (int)( ( length() + LongArrayBitVector.BITS_PER_WORD - 1 ) >> LongArrayBitVector.LOG2_BITS_PER_WORD ) ];
		long length = length();
		for( long i = 0; i < length; i++ ) if ( getBoolean( i ) ) bits[ (int)( i >> LongArrayBitVector.LOG2_BITS_PER_WORD ) ] |= 1L << i; 
		return bits;
	}
	
//	/** An integer sorted set view of a bit vector.
//	 *
//	 * <P>This class implements in the obvious way an integer set view
//	 * of a bit vector. The vector is enlarged as needed (i.e., when
//	 * a one beyond the current size is set), but it is never shrunk.
//	 */
//
//	public static class LongSetView extends AbstractLongSortedSet implements LongSet, Serializable {
//
//		protected final BitVector bitVector;
//		private static final long serialVersionUID = 1L;
//		private final long from;
//		private final long to;
//
//		public LongSetView( final BitVector bitVector, final long from, final long to ) {
//			if ( from > to ) throw new IllegalArgumentException( "Start index (" + from + ") is greater than end index (" + to + ")" );
//			this.bitVector = bitVector;
//			this.from = from;
//			this.to = to;
//		}
//
//
//		public boolean contains( final long index ) {
//			if ( index < 0 ) throw new IllegalArgumentException( "The provided index (" + index + ") is negative" );
//			if ( index < from || index >= to ) return false;
//			return index < bitVector.size() && bitVector.getBoolean( index );
//		}
//
//		public boolean add( final long index ) {
//			if ( index < 0 ) throw new IllegalArgumentException( "The provided index (" + index + ") is negative" );
//			if ( index < from || index >= to ) return false;
//
//			final int size = bitVector.size();
//			if ( index >= size ) bitVector.length( index + 1 );
//			final boolean oldValue = bitVector.getBoolean( index );
//			bitVector.set( index );
//			return ! oldValue;
//		}
//
//		public boolean remove( final long index ) {
//			final int size = bitVector.size();
//			if ( index >= size ) return false;
//			final boolean oldValue = bitVector.getBoolean( index );
//			bitVector.clear( index );
//			return oldValue;
//		}
//
//		@Override
//		public void clear() {
//			bitVector.clear();
//		}
//
//		public int size() {
//			// This minimisation is necessary for implementations not supporting long indices.
//			final long size = bitVector.subVector( from, Math.min( to, bitVector.length() ) ).count();
//			if ( size > Integer.MAX_VALUE ) throw new IllegalStateException( "Set is too large to return an integer size" );
//			return (int)size;
//		}
//
//		public LongBidirectionalIterator iterator() {
//			return iterator( 0 );
//		}
//
//		private final class LongSetViewIterator extends AbstractLongBidirectionalIterator {
//			long pos, last = -1, nextPos = -1, prevPos = -1;
//
//			private LongSetViewIterator( long from ) {
//				pos = from;
//			}
//
//			public boolean hasNext() {
//				if ( nextPos == -1 && pos < bitVector.length() ) nextPos = bitVector.nextOne( pos );
//				return nextPos != -1;
//			}
//
//			public boolean hasPrevious() {
//				if ( prevPos == -1 && pos > 0 ) prevPos = bitVector.previousOne( pos );
//				return prevPos != -1;
//			}
//
//			public long nextLong() {
//				if ( ! hasNext() ) throw new NoSuchElementException();
//				last = nextPos;
//				pos = nextPos + 1;
//				nextPos = -1;
//				return last;
//			}
//
//			public long previousLong() {
//				if ( ! hasPrevious() ) throw new NoSuchElementException();
//				pos = prevPos;
//				prevPos = -1;
//				return last = pos;
//			}
//
//			public void remove() {
//				if ( last == -1 ) throw new IllegalStateException();
//				bitVector.clear( last );
//			}
//		}
//
//		public LongBidirectionalIterator iterator( final long from ) {
//			return new LongSetViewIterator( from );
//		}
//
//		public long firstLong() {
//			return bitVector.nextOne( from );
//		}
//
//		public long lastLong() {
//			return bitVector.previousOne( Math.min( bitVector.length(), to ) );
//		}
//
//		public LongComparator comparator() {
//			return null;
//		}
//
//		public LongSortedSet headSet( final long to ) {
//			return to < this.to ? new LongSetView( bitVector, from, to ) : this;
//		}
//
//		public LongSortedSet tailSet( final long from ) {
//			return from > this.from ? new LongSetView( bitVector, from, to ) : this;
//		}
//
//		public LongSortedSet subSet( long from, long to ) {
//			to = to < this.to ? to : this.to;
//			from = from > this.from ? from : this.from;
//			if ( from == this.from && to == this.to ) return this;
//			return new LongSetView( bitVector, from, to );
//		}
//	}
//
//	/** A list-of-integers view of a bit vector.
//	 *
//	 * <P>This class implements in the obvious way a view
//	 * of a bit vector as a list of integers of given width. The vector is enlarged as needed (i.e., when
//	 * adding new elements), but it is never shrunk.
//	 */
//
//	public static class LongBigListView extends AbstractLongBigList implements LongBigList, Serializable {
//		private static final long serialVersionUID = 1L;
//		/** The underlying bit vector. */
//		protected final BitVector bitVector;
//		/** The width in bit of an element of this list view. */
//		protected final int width;
//		/** A bit mask containing {@link #width} bits set to one. */
//		protected final long fullMask;
//
//		public LongBigListView( final BitVector bitVector, final int width ) {
//			this.width = width;
//			this.bitVector = bitVector;
//			fullMask = width == Long.SIZE ? -1 : ( 1L << width ) - 1;
//		}
//
//		public long length() {
//			return width == 0 ? 0 : bitVector.length() / width;
//		}
//
//		public int size() {
//			final long length = length();
//			if ( length > Integer.MAX_VALUE ) throw new IllegalStateException( "The number of elements of this bit list (" + length + ") exceeds Integer.MAX_INT" );
//			return (int)length;
//		}
//
//		public long size64() {
//			return length();
//		}
//
//		public LongBigList length( final long newSize ) {
//			bitVector.length( newSize * width );
//			return this;
//		}
//
//		public void size( final long newSize ) {
//			length( newSize );
//		}
//
//		// TODO: implement set()/remove()
//		private final class LongBigListIterator extends AbstractLongBigListIterator {
//			private long pos = 0;
//			public boolean hasNext() { return pos < length(); }
//			public boolean hasPrevious() { return pos > 0; }
//
//			@Override
//			public long nextLong() {
//				if ( ! hasNext() ) throw new NoSuchElementException();
//				return getLong( pos++ );
//			}
//
//			@Override
//			public long previousLong() {
//				if ( ! hasPrevious() ) throw new NoSuchElementException();
//				return getLong( --pos );
//			}
//
//			public long nextIndex() {
//				return pos;
//			}
//
//			public long previousIndex() {
//				return pos - 1;
//			}
//		}
//
//		@Override
//		public it.unimi.dsi.fastutil.longs.LongBigListIterator listIterator() {
//			return new LongBigListIterator();
//		}
//
//		public void add( int index, long value ) {
//			add( (long)index, value );
//		}
//
//		public void add( long index, long value ) {
//			if ( width != Long.SIZE && value > fullMask ) throw new IllegalArgumentException();
//			for( int i = 0; i < width; i++ ) bitVector.add( ( value & 1L << i ) != 0 );
//		}
//
//		public long getLong( long index ) {
//			final long start = index * width;
//			return bitVector.getLong( start, start + width );
//		}
//
//		public long getLong( int index ) {
//			return getLong( (long)index );
//		}
//
//		// TODO
//		public long removeLong( final long index ) {
//			throw new UnsupportedOperationException();
//		}
//
//		public long set( long index, long value ) {
//			if ( width != Long.SIZE && value > fullMask ) throw new IllegalArgumentException();
//			long oldValue = getLong( index );
//			final long start = index * width;
//			for( int i = width; i-- != 0; ) bitVector.set( i + start, ( value & 1L << i ) != 0 );
//			return oldValue;
//		}
//
//		public long set( int index, long value ) {
//			return set( (long)index, value );
//		}
//
//		public LongBigList subList( long from, long to ) {
//			return bitVector.subVector( from * width, to * width ).asLongBigList( width );
//		}
//	}
		
	@Override
	public BitVector length(long newLength ) {
		long length = length();
		if ( length < newLength ) for( long i = newLength - length; i-- != 0; ) add( false );
		else for( long i = length; i-- != newLength; ) removeBoolean( i );
		return this;
	}

	public void size( long newSize ) {
		length( newSize );
	}

	/*public LongSortedSet asLongSet() {
		return new LongSetView( this, 0, Long.MAX_VALUE );
	}	
	
	public LongBigList asLongBigList( final int width ) {
		return new LongBigListView( this, width );
	}	
	*/

	public SubBitVector subList( int from, int to ) {
		return new SubBitVector( this, from, to );
	}

	@Override
	public BitVector subVector(long from, long to ) {
		return new SubBitVector( this, from, to );
	}

	@Override
	public BitVector subVector(long from ) {
		return subVector( from, length() );
	}

//	@Override
//	public int compareTo( final BigList<? extends Boolean> list ) {
//		if ( list instanceof BitVector ) return compareTo( (BitVector)list );
//		return super.compareTo( list );
//	}
	
	public int compareTo( BitVector v ) {
		long minLength = Math.min( length(), v.length() );
		long l = minLength - minLength % Long.SIZE;
		long w0, w1, xor;
		
		long i;
		for( i = 0; i < l; i += Long.SIZE ) {

			w0 = getLong( i, i + Long.SIZE );
			w1 = v.getLong( i, i + Long.SIZE );
			xor = w0 ^ w1;
			if ( xor != 0 ) return ( xor & -xor & w0 ) == 0 ? -1 : 1;
		}

		w0 = getLong( i, minLength );
		w1 = v.getLong( i, minLength );
		xor = w0 ^ w1;
		if ( xor != 0 ) return ( xor & -xor & w0 ) == 0 ? -1 : 1;

		return Long.signum( length() - v.length() );
	}	
	

	/** Returns a string representation of this vector.
	 * 
	 * <P>Note that this string representation shows the bit of index 0 at the leftmost position.
	 * @return a string representation of this vector, with the bit of index 0 on the left.
	 */
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		long size = size64();
		for( long i = 0; i < size; i++ ) s.append( getInt( i ) );
		return s.toString();
	}

	/** A subvector of a given bit vector, specified by an initial and a final bit. */
	
	public static class SubBitVector extends AbstractBitVector {
		protected final BitVector bitVector;
		protected long from;
		protected long to;
		
		public SubBitVector( BitVector l, long from, long to ) {
			BitVectors.ensureFromTo( l.length(), from, to );
			this.from = from;
			this.to = to;
			bitVector = l;
		}
		
		@Override
		public boolean getBoolean(long index ) {
			ensureIndex( index );
			return bitVector.getBoolean( from + index ); 
		}
		
		@Override
		public int getInt(long index ) { return getBoolean( index ) ? 1 : 0; }
		
		@Override
		public boolean set(long index, boolean value ) {
			ensureIndex( index );
			return bitVector.set( from + index, value ); 
		}
		
		@Override
		public void set(long index, int value ) { set( index, value != 0 ); }
		
		@Override
		public void add(long index, boolean value ) {
			ensureIndex( index );
			bitVector.add( from + index, value ); to++; 
		}
		
		@Override
		public void add(long index, int value ) { add( index, value != 0 ); to++; }
		@Override
		public void add(int value ) { bitVector.add( to++, value ); }
		
		@Override
		public boolean removeBoolean(long index ) {
			ensureIndex( index );
			to--; 
			return bitVector.removeBoolean( from + index ); 
		} 

		@Override
		public BitVector copy(long from, long to ) {
			BitVectors.ensureFromTo( length(), from, to );
			return bitVector.copy( this.from + from, this.from + to );
		}
		
		@Override
		public BitVector subVector(long from, long to ) {
			BitVectors.ensureFromTo( length(), from, to );
			return new SubBitVector( bitVector, this.from + from, this.from + to );
		}
		
		@Override
		public long getLong(long from, long to ) {
			BitVectors.ensureFromTo( length(), from, to );
			return bitVector.getLong( from + this.from, to + this.from );
		}
		
		@Override
		public long length() {
			return to - from;
		}

		@Override
		public long size64() {
			return length();
		}
}
}
