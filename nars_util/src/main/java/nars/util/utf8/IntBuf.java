package nars.util.utf8;

import org.apache.commons.math3.util.FastMath;
import sun.nio.cs.Surrogate;

import java.util.Arrays;

/*
 Experimental utf8 encoding in int[] for faster comparison and copying than byte[]
 TODO efficient non-byte[] decoding
 TODO write/append building
 TODO p(,,) byte access sequential storing index, not calculate everytime as it currently does in encode
 */
public class IntBuf {
	private static final Surrogate.Parser sgp = new Surrogate.Parser();

	protected int capacity;

	protected int length = 0;

	protected int[] buffer;

	public static IntBuf create(int capacity) {
		return new IntBuf(capacity);
	}

	protected IntBuf(int capacity) {
		this.capacity = capacity;
		buffer = new int[capacity];
	}

	public static void p(int[] x, int i, int v) {
		p(x, i, (byte) v);
	}
	static void p(int[] x, int i, byte v) {
		int e = i / 4; // >> 2
		int o = i % 4;
		int b = 8 * o;
		p(x, e, b, v);
	}
	static void p(int[] x, int e, int b, byte v) {
		int c = x[e] & ~(0xff << b);
		x[e] = c | (v << b);
	}

	public static int encode(String src, int[] dst) {

		int dp = 0;
		int dl = dst.length;

		int spCurr = 0;
		int sl = src.length();

		// TODO avoid toCharArray
		int bytes = encode(src.toCharArray(), spCurr, sl, dst, dp, dl);

		// only move the position if we fit the whole thing in.
		return (int) FastMath.ceil(bytes / 4.0f);
	}

	public static int encode(char[] sa, int spCurr, int sl, int[] da, int dp,
			int dl) {
		int lastSp = spCurr;
		int lastDp = dp;
		int dlASCII = dp + Math.min(sl - lastSp, dl - dp);
		// handle ascii encoded strings in an optimised loop
		while (dp < dlASCII && sa[lastSp] < 128)
			p(da, dp++, (byte) sa[lastSp++]);

		// we are counting on the JVM array boundary checks to throw an
		// exception rather then
		// checkin boundaries ourselves... no nice, and potentailly not that
		// much of a
		// performance enhancement.
		while (lastSp < sl) {
			int c = sa[lastSp];
			// noinspection IfStatementWithTooManyBranches
			if (c < 128) {
				p(da, dp++, (byte) c);
			} else if (c < 2048) {
				p(da, dp++, (byte) (0xC0 | (c >> 6)));
				p(da, dp++, (byte) (0x80 | (c & 0x3F)));
			} else if (Surrogate.is(c)) {
				int uc = sgp.parse((char) c, sa, lastSp, sl);
				if (uc < 0) {
					return dp - lastDp;
				}
				p(da, dp++, (byte) (0xF0 | uc >> 18));
				p(da, dp++, (byte) (0x80 | uc >> 12 & 0x3F));
				p(da, dp++, (byte) (0x80 | uc >> 6 & 0x3F));
				p(da, dp++, (byte) (0x80 | uc & 0x3F));
				++lastSp;
			} else {
				p(da, dp++, (byte) (0xE0 | c >> 12));
				p(da, dp++, (byte) (0x80 | c >> 6 & 0x3F));
				p(da, dp++, (byte) (0x80 | c & 0x3F));
			}
			++lastSp;
		}

		return dp - lastDp;
	}

	// final public IntBuf add( final String str ) {
	// this.add( Byt.bytes( str ) );
	// return this;
	// }
	// public IntBuf add( final int value ) {
	//
	// if ( 4 + length < capacity ) {
	// Byt.intTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 4 );
	// capacity = buffer.length;
	//
	// Byt.intTo( buffer, length, value );
	// }
	//
	// length += 4;
	// return this;
	//
	//
	// }
	//
	//
	// public IntBuf add( final float value ) {
	//
	// if ( 4 + length < capacity ) {
	// Byt.floatTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 4 );
	// capacity = buffer.length;
	//
	// Byt.floatTo( buffer, length, value );
	// }
	//
	// length += 4;
	// return this;
	//
	//
	// }
	//
	//
	// public IntBuf add( final char value ) {
	//
	// if ( 2 + length < capacity ) {
	// Byt.charTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 2 );
	// capacity = buffer.length;
	//
	// Byt.charTo( buffer, length, value );
	// }
	//
	// length += 2;
	// return this;
	//
	//
	// }
	//
	//
	// public IntBuf add( final short value ) {
	//
	// if ( 2 + length < capacity ) {
	// Byt.shortTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 2 );
	// capacity = buffer.length;
	//
	// Byt.shortTo( buffer, length, value );
	// }
	//
	// length += 2;
	// return this;
	//
	//
	// }
	//
	// public IntBuf addByte( int value ) {
	// this.add( ( byte ) value );
	// return this;
	// }
	//
	// public IntBuf add( final byte value ) {
	//
	// if ( 1 + length < capacity ) {
	// Byt.idx( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer );
	// capacity = buffer.length;
	//
	// Byt.idx( buffer, length, value );
	// }
	//
	// length += 1;
	//
	// return this;
	//
	// }
	//
	// public IntBuf add( long value ) {
	//
	// if ( 8 + length < capacity ) {
	// Byt.longTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 8 );
	// capacity = buffer.length;
	//
	// Byt.longTo( buffer, length, value );
	// }
	//
	// length += 8;
	// return this;
	//
	// }
	//
	// public IntBuf addUnsignedInt( long value ) {
	//
	// if ( 4 + length < capacity ) {
	// Byt.unsignedIntTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 4 );
	// capacity = buffer.length;
	//
	// Byt.unsignedIntTo( buffer, length, value );
	// }
	//
	// length += 4;
	// return this;
	//
	// }
	//
	// public IntBuf add( double value ) {
	//
	// if ( 8 + length < capacity ) {
	// Byt.doubleTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 8 );
	// capacity = buffer.length;
	//
	// Byt.doubleTo( buffer, length, value );
	// }
	//
	// length += 8;
	// return this;
	//
	// }
	//
	//
	// public IntBuf add( byte[] array ) {
	// if ( array.length + this.length < capacity ) {
	// Byt._idx( buffer, length, array );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + array.length );
	// capacity = buffer.length;
	//
	// Byt._idx( buffer, length, array );
	//
	// }
	// length += array.length;
	// return this;
	// }
	//
	//
	// public IntBuf add( final byte[] array, final int length ) {
	// if ( ( this.length + length ) < capacity ) {
	// Byt._idx( buffer, this.length, array, length );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + length );
	// capacity = buffer.length;
	//
	// Byt._idx( buffer, length, array, length );
	//
	// }
	// this.length += length;
	// return this;
	// }
	//
	// public IntBuf add( byte[] array, final int offset, final int length ) {
	// if ( ( this.length + length ) < capacity ) {
	// Byt._idx( buffer, length, array, offset, length );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + length );
	// capacity = buffer.length;
	//
	// Byt._idx( buffer, length, array, offset, length );
	//
	// }
	// this.length += length;
	// return this;
	// }
	//
	//
	// public int len() {
	// return length;
	// }
	//
	//
	// public void write( int b ) {
	// this.addByte( b );
	// }
	//
	// public void write( byte[] b ) {
	// this.add( b );
	// }
	//
	// public void write( byte[] b, int off, int len ) {
	// this.add( b, len );
	// }
	//
	// public void writeBoolean( boolean v ) {
	// if ( v == true ) {
	// this.addByte( 1 );
	// } else {
	// this.addByte( 0 );
	// }
	// }
	//
	// public void writeByte( byte v ) {
	// this.addByte( v );
	// }
	//
	// public void writeUnsignedByte( short v ) {
	// this.addUnsignedByte( v );
	// }
	//
	// public void addUnsignedByte( short value ) {
	// if ( 1 + length < capacity ) {
	// Byt.unsignedByteTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 1 );
	// capacity = buffer.length;
	//
	// Byt.unsignedByteTo( buffer, length, value );
	// }
	//
	// length += 1;
	//
	// }
	//
	//
	// public void writeShort( short v ) {
	// this.add( v );
	// }
	//
	//
	// public void writeUnsignedShort( int v ) {
	// this.addUnsignedShort( v );
	// }
	//
	// public void addUnsignedShort( int value ) {
	//
	// if ( 2 + length < capacity ) {
	// Byt.unsignedShortTo( buffer, length, value );
	// } else {
	// buffer = Byt.grow( buffer, buffer.length * 2 + 2 );
	// capacity = buffer.length;
	//
	// Byt.unsignedShortTo( buffer, length, value );
	// }
	//
	// length += 2;
	//
	//
	// }
	//
	//
	// public void writeChar( char v ) {
	//
	// this.add( v );
	// }
	//
	//
	// public void writeInt( int v ) {
	// this.add( v );
	// }
	//
	//
	// public void writeUnsignedInt( long v ) {
	// this.addUnsignedInt( v );
	// }
	//
	//
	// public void writeLong( long v ) {
	// this.add( v );
	// }
	//
	//
	// public void writeFloat( float v ) {
	// this.add( v );
	// }
	//
	//
	// public void writeDouble( double v ) {
	// this.add( v );
	// }
	//
	//
	// public void writeLargeString( String s ) {
	// final byte[] bytes = Byt.bytes( s );
	// this.add( bytes.length );
	// this.add( bytes );
	// }
	//
	//
	// public void writeSmallString( String s ) {
	// final byte[] bytes = Byt.bytes( s );
	// this.addUnsignedByte( ( short ) bytes.length );
	// this.add( bytes );
	// }
	//
	//
	// public void writeMediumString( String s ) {
	// final byte[] bytes = Byt.bytes( s );
	// this.addUnsignedShort( bytes.length );
	// this.add( bytes );
	// }
	//
	//
	// public void writeLargeByteArray( byte[] bytes ) {
	// this.add( bytes.length );
	// this.add( bytes );
	// }
	//
	//
	// public void writeSmallByteArray( byte[] bytes ) {
	// this.addUnsignedByte( ( short ) bytes.length );
	// this.add( bytes );
	// }
	//
	//
	// public void writeMediumByteArray( byte[] bytes ) {
	// this.addUnsignedShort( bytes.length );
	// this.add( bytes );
	// }
	//
	//
	// public void writeLargeShortArray( short[] values ) {
	// int byteSize = values.length * 2 + 4;
	// this.add( values.length );
	// doWriteShortArray( values, byteSize );
	// }
	//
	//
	// public void writeSmallShortArray( short[] values ) {
	// int byteSize = values.length * 2 + 1;
	// this.addUnsignedByte( ( short ) values.length );
	// doWriteShortArray( values, byteSize );
	// }
	//
	//
	// public void writeMediumShortArray( short[] values ) {
	// int byteSize = values.length * 2 + 2;
	// this.addUnsignedShort( values.length );
	// doWriteShortArray( values, byteSize );
	// }
	//
	//
	// private void doWriteShortArray( short[] values, int byteSize ) {
	// if ( !( byteSize + length < capacity ) ) {
	// buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
	// }
	// for ( int index = 0; index < values.length; index++ ) {
	// this.add( values[ index ] );
	// }
	// }
	//
	//
	//
	// public void writeLargeIntArray( int[] values ) {
	// int byteSize = values.length * 4 + 4;
	// this.add( values.length );
	// doWriteIntArray( values, byteSize );
	// }
	//
	//
	// public void writeSmallIntArray( int[] values ) {
	// int byteSize = values.length * 4 + 1;
	// this.addUnsignedByte( ( short ) values.length );
	// doWriteIntArray( values, byteSize );
	// }
	//
	//
	// public void writeMediumIntArray( int[] values ) {
	// int byteSize = values.length * 4 + 2;
	// this.addUnsignedShort( values.length );
	// doWriteIntArray( values, byteSize );
	// }
	//
	//
	// private void doWriteIntArray( int[] values, int byteSize ) {
	// if ( !( byteSize + length < capacity ) ) {
	// buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
	// }
	// for ( int index = 0; index < values.length; index++ ) {
	// this.add( values[ index ] );
	// }
	// }
	// //
	// // public Input input() {
	// // return new InputByteArray( this.buffer );
	// // }
	//
	//
	//
	// public void writeLargeLongArray( long[] values ) {
	// int byteSize = values.length * 8 + 4;
	// this.add( values.length );
	// doWriteLongArray( values, byteSize );
	// }
	//
	//
	// public void writeSmallLongArray( long[] values ) {
	// int byteSize = values.length * 8 + 1;
	// this.addUnsignedByte((short) values.length);
	// doWriteLongArray( values, byteSize );
	// }
	//
	//
	// public void writeMediumLongArray( long[] values ) {
	// int byteSize = values.length * 8 + 2;
	// this.addUnsignedShort(values.length);
	// doWriteLongArray( values, byteSize );
	// }
	//
	//
	// private void doWriteLongArray( long[] values, int byteSize ) {
	// if ( !( byteSize + length < capacity ) ) {
	// buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
	// }
	// for ( int index = 0; index < values.length; index++ ) {
	// this.add( values[ index ] );
	// }
	// }
	//
	//
	//
	// public void writeLargeFloatArray( float[] values ) {
	// int byteSize = values.length * 4 + 4;
	// this.add( values.length );
	// doWriteFloatArray( values, byteSize );
	//
	// }
	//
	//
	// public void writeSmallFloatArray( float[] values ) {
	// int byteSize = values.length * 4 + 1;
	// this.addUnsignedByte((short) values.length);
	// doWriteFloatArray( values, byteSize );
	// }
	//
	//
	// public void writeMediumFloatArray( float[] values ) {
	// int byteSize = values.length * 4 + 2;
	// this.addUnsignedShort(values.length);
	// doWriteFloatArray( values, byteSize );
	//
	// }
	//
	// private void doWriteFloatArray( float[] values, int byteSize ) {
	// if ( !( byteSize + length < capacity ) ) {
	// buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
	// }
	// for ( int index = 0; index < values.length; index++ ) {
	// this.add( values[ index ] );
	// }
	// }
	//
	//
	//
	// public void writeLargeDoubleArray( double[] values ) {
	// int byteSize = values.length * 8 + 4;
	// this.add( values.length );
	// doWriteDoubleArray( values, byteSize );
	//
	//
	// }
	//
	//
	// public void writeSmallDoubleArray( double[] values ) {
	// int byteSize = values.length * 8 + 1;
	// this.addUnsignedByte((short) values.length);
	// doWriteDoubleArray( values, byteSize );
	//
	// }
	//
	//
	// public void writeMediumDoubleArray( double[] values ) {
	// int byteSize = values.length * 8 + 2;
	// this.addUnsignedShort(values.length);
	// doWriteDoubleArray( values, byteSize );
	//
	// }
	//
	//
	// private void doWriteDoubleArray( double[] values, int byteSize ) {
	// if ( !( byteSize + length < capacity ) ) {
	// buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
	// }
	// for ( int index = 0; index < values.length; index++ ) {
	// this.add( values[ index ] );
	// }
	// }

	public String toString() {
		int len = length;

		char[] chars = new char[buffer.length];
		for (int index = 0; index < chars.length; index++) {
			chars[index] = (char) buffer[index];
		}
		return new String(chars, 0, len);
		// return new String ( this.buffer, 0, len, StandardCharsets.UTF_8 );
	}

	public byte[] toBytes() {
		return toBytes(buffer, length);
	}

	public static byte[] toBytes(int[] buffer) {
		return toBytes(buffer, buffer.length);
	}

	public static byte[] toBytes(int[] buffer, int numInts) {

		byte[] r = new byte[numInts * 4];

		int l = convert(r, buffer, numInts);
		if (l != r.length)
			return Arrays.copyOf(r, l);

		return r;
	}

	public static int convert(byte[] arrayDst, int[] arrayOrg, int maxOrg) {
		int i;
		int idxDst;
		int maxDst;
		//
		maxDst = maxOrg * 4;
		//
		if (arrayDst == null)
			return 0;
		if (arrayOrg == null)
			return 0;
		if (arrayDst.length < maxDst)
			return 0;
		if (arrayOrg.length < maxOrg)
			return 0;
		//
		idxDst = 0;

		int bTrim = 0;

		for (i = 0; i < maxOrg; i++) {
			// Copia o int, byte a byte.
			int I = arrayOrg[i];
			byte a = (byte) (I);
			byte b = (byte) (I >>> 8);
			byte c = (byte) (I >>> 16);
			byte d = (byte) (I >>> 24);

			if (d == 0) {
				bTrim++;
				if (c == 0) {
					bTrim++;
					if (b == 0) {
						bTrim++;
						if (a == 0) {
							throw new RuntimeException("zero char");
						}
					}
				}
			}

			arrayDst[idxDst++] = a;
			arrayDst[idxDst++] = b;
			arrayDst[idxDst++] = c;
			arrayDst[idxDst++] = d;

		}
		//
		return idxDst - bTrim;
	}

	public static int convert(int[] arrayDst, byte[] arrayOrg, int maxOrg) {
		int i;
		int v;
		int idxOrg;
		int maxDst;
		//
		maxDst = maxOrg / 4;
		//
		if (arrayDst == null)
			return 0;
		if (arrayOrg == null)
			return 0;
		if (arrayDst.length < maxDst)
			return 0;
		if (arrayOrg.length < maxOrg)
			return 0;
		//
		idxOrg = 0;
		for (i = 0; i < maxDst; i++) {
			arrayDst[i] = 0;
			//
			v = 0x000000FF & arrayOrg[idxOrg];
			arrayDst[i] = arrayDst[i] | v;
			idxOrg++;
			//
			v = 0x000000FF & arrayOrg[idxOrg];
			arrayDst[i] = arrayDst[i] | (v << 8);
			idxOrg++;
			//
			v = 0x000000FF & arrayOrg[idxOrg];
			arrayDst[i] = arrayDst[i] | (v << 16);
			idxOrg++;
			//
			v = 0x000000FF & arrayOrg[idxOrg];
			arrayDst[i] = arrayDst[i] | (v << 24);
			idxOrg++;
		}
		//
		return maxDst;
	}

	public int[] toInts() {
		return slc(buffer, 0, length);
	}
	public int[] toInts(int start) {
		return slc(buffer, start, length - start + 1);
	}

	public int[] slc(int startIndex, int endIndex) {
		return slc(buffer, startIndex, endIndex);
	}

	public static int[] slc(int[] array, int startIndex, int endIndex) {

		int start = calculateIndex(array, startIndex);
		int end = calculateEndIndex(array, endIndex);
		int newLength = end - start;

		if (newLength < 0) {
			throw new ArrayIndexOutOfBoundsException(String.format(
					"start index %d, end index %d, length %d", startIndex,
					endIndex, array.length));
		}

		int[] newArray = new int[newLength];
		System.arraycopy(array, start, newArray, 0, newLength);
		return newArray;
	}

	private static int calculateIndex(int[] array, int originalIndex) {
		int length = array.length;

		int index = originalIndex;

		/*
		 * Adjust for reading from the right as in -1 reads the 4th element if
		 * the length is 5
		 */
		if (index < 0) {
			index = length + index;
		}

		/*
		 * Bounds check if it is still less than 0, then they have an negative
		 * index that is greater than length
		 */
		/*
		 * Bounds check if it is still less than 0, then they have an negative
		 * index that is greater than length
		 */
		if (index < 0) {
			index = 0;
		}
		if (index >= length) {
			index = length - 1;
		}
		return index;
	}

	/* End universal methods. */
	private static int calculateEndIndex(int[] array, int originalIndex) {
		int length = array.length;

		int index = originalIndex;

		/*
		 * Adjust for reading from the right as in -1 reads the 4th element if
		 * the length is 5
		 */
		if (index < 0) {
			index = length + index;
		}

		/*
		 * Bounds check if it is still less than 0, then they have an negative
		 * index that is greater than length
		 */
		/*
		 * Bounds check if it is still less than 0, then they have an negative
		 * index that is greater than length
		 */
		if (index < 0) {
			index = 0;
		}
		if (index > length) {
			index = length;
		}
		return index;
	}

	public static String asString(int[] i) {
		return asString(toBytes(i));
	}
	public static String asString(int[] i, int numInts) {
		return asString(toBytes(i, numInts));
	}

	public static String asString(byte[] b) {
		return Utf8.fromUtf8toString(b);
	}
	public static String asString(byte[] b, int bytes) {
		return Utf8.fromUtf8toString(b, bytes);
	}

	public String asString() {
		return asString(toBytes());
	}
}
