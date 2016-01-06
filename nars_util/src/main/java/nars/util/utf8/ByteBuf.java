package nars.util.utf8;

/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

/**
 * 
 * Byte Array Buffer Manipulation Tools, Adapted from Boon
 * 
 * Methods: "add": adds content to the end, but will not grow the internal
 * array. FAST "append": adds content to the end, growing as necessary "write":
 * like append but can do some interpretation of the input value
 */
public final class ByteBuf {

	private int length = 0;

	private byte[] buffer;

	// public static ByteBuf createExact( final int capacity ) {
	// return new ByteBuf( capacity ) {
	// public ByteBuf add( byte[] chars ) {
	// Byt._idx( buffer, length, chars );
	// length += chars.length;
	// return this;
	// }
	// };
	// }

	public static ByteBuf create(int capacity) {
		return new ByteBuf(capacity);
	}

	public static ByteBuf create(byte[] buffer) {
		ByteBuf buf = new ByteBuf(buffer.length);
		buf.buffer = buffer;
		return buf;
	}

	protected ByteBuf(int capacity) {
		buffer = new byte[capacity];
	}

	public static void unsignedByteTo(byte[] buffer, int off, short value) {
		buffer[off] = (byte) (value);

	}

	public static void unsignedShortTo(byte[] buffer, int off, int value) {

		buffer[off++] = (byte) (value);
		buffer[off] = (byte) (value >>> 8);

	}

	public static void unsignedIntTo(byte[] b, int off, long val) {
		b[off++] = (byte) (val >>> 24);
		b[off++] = (byte) (val >>> 16);
		b[off++] = (byte) (val >>> 8);
		b[off] = (byte) (val);
	}

	public static int _idx(byte[] output, int outputOffset, byte[] input) {
		return Byt._idx(output, outputOffset, input, input.length);
	}

	public ByteBuf append(String str) {
		append(Utf8.toUtf8(str));
		return this;
	}

	public ByteBuf add(String str) {
		add(Utf8.toUtf8(str));
		return this;
	}

	public ByteBuf append(int value) {

		grow(4);
		Byt.intTo(buffer, length, value);
		length += 4;
		return this;

	}

	public ByteBuf append(float value) {

		grow(4);
		Byt.floatTo(buffer, length, value);

		length += 4;
		return this;

	}

	public ByteBuf add(char value) {
		Byt.charTo(buffer, length, value);
		length += 2;
		return this;
	}

	public ByteBuf append(char value) {

		grow(2);
		add(value);

		return this;
	}

	public ByteBuf append(short value) {

		if (2 + length < getCapacity()) {
			Byt.shortTo(buffer, length, value);
		} else {
			buffer = Byt.grow(buffer, buffer.length * 2 + 2);
			Byt.shortTo(buffer, length, value);
		}

		length += 2;
		return this;

	}

	public ByteBuf addByte(int value) {
		append((byte) value);
		return this;
	}

	public ByteBuf append(long value) {

		grow(8);
		Byt.longTo(buffer, length, value);

		length += 8;
		return this;

	}

	void grow(int bytes) {
		if (bytes + length > getCapacity()) {
			buffer = Byt.grow(buffer, buffer.length * 2 + bytes);
		}
	}

	public ByteBuf addUnsignedInt(long value) {

		grow(4);
		unsignedIntTo(buffer, length, value);
		length += 4;
		return this;

	}

	public ByteBuf append(double value) {

		grow(8);

		Byt.doubleTo(buffer, length, value);
		length += 8;
		return this;

	}

	public int _idx(byte[] buf, byte[] input) {
		return _idx(buf, length, input);
	}

	public ByteBuf add(byte value) {
		if (length >= buffer.length)
			throw new RuntimeException("overflow");
		buffer[length++] = value;
		return this;
	}

	public ByteBuf append(byte value) {

		if (1 + length > getCapacity()) {
			buffer = Byt.grow(buffer);

		}

		return add(value);
	}

	public ByteBuf add(byte[] array) {
		length += _idx(buffer, array);
		return this;
	}

	public ByteBuf append(byte[] array) {
		int al = array.length;
		if (al + length > getCapacity()) {
			buffer = Byt.grow(buffer, buffer.length + al);

		}
		add(array);
		return this;
	}

	public ByteBuf add(byte[] array, int subLen) {
		length += _idx(buffer, subLen, array);
		return this;
	}

	public ByteBuf append(byte[] array, int length) {
		if ((this.length + length) > getCapacity()) {
			buffer = Byt.grow(buffer, buffer.length * 2 + length);

		}
		Byt._idx(buffer, length, array, length);
		this.length += length;
		return this;
	}

	public ByteBuf append(byte[] array, int offset, int length) {
		if ((this.length + length) > getCapacity()) {
			buffer = Byt.grow(buffer, buffer.length * 2 + length);

		}

		return add(array, offset, length);
	}

	public ByteBuf add(byte[] array, int offset, int length) {
		Byt._idx(buffer, length, array, offset, length);
		this.length += length;
		return this;
	}

	public byte[] readAndReset() {
		byte[] bytes = buffer;
		buffer = null;
		return bytes;
	}

	public byte[] readForRecycle() {
		length = 0;
		return buffer;
	}

	public int len() {
		return length;
	}

	// public ByteBuf addUrlEncodedByteArray( byte[] value ) {
	//
	//
	// final byte[] encoded = new byte[ 2 ];
	//
	// for ( int index = 0; index < value.length; index++ ) {
	// int i = value[ index ];
	//
	// if ( i >= 'a' && i <= 'z' ) {
	// this.addByte( i );
	// } else if ( i >= 'A' && i <= 'Z' ) {
	// this.addByte( i );
	// } else if ( i >= '0' && i <= '9' ) {
	// this.addByte( i );
	// } else if ( i == '_' || i == '-' || i == '.' || i == '*' ) {
	// this.addByte( i );
	// } else if ( i == ' ' ) {
	// this.addByte( '+' );
	// } else {
	// ByteScanner.encodeByteIntoTwoAsciiCharBytes( i, encoded );
	// this.addByte( '%' );
	// this.addByte( encoded[ 0 ] );
	// this.addByte( encoded[ 1 ] );
	// }
	//
	// }
	// return this;
	// }
	//
	// public ByteBuf addJSONEncodedByteArray( byte[] value ) {
	//
	// if ( value == null ) {
	// this.add( "null" );
	// return this;
	// }
	//
	//
	// this.addByte( '"' );
	//
	// for ( int index = 0; index < value.length; index++ ) {
	// int ch = value[ index ];
	//
	//
	// switch ( ch ) {
	// case '"':
	// this.addByte( '\\' );
	// this.addByte( '"' );
	// break;
	//
	// case '\\':
	// this.addByte( '\\' );
	// this.addByte( '\\' );
	// break;
	//
	// case '/':
	// this.addByte( '\\' );
	// this.addByte( '/' );
	// break;
	//
	// case '\n':
	// this.addByte( '\\' );
	// this.addByte( 'n' );
	// break;
	//
	// case '\t':
	// this.addByte( '\\' );
	// this.addByte( 't' );
	// break;
	//
	// case '\r':
	// this.addByte( '\\' );
	// this.addByte( 'r' );
	// break;
	//
	// case '\b':
	// this.addByte( '\\' );
	// this.addByte( 'b' );
	// break;
	//
	// case '\f':
	// this.addByte( '\\' );
	// this.addByte( 'f' );
	// break;
	//
	//
	// default:
	// if ( ch > 127 ) {
	// this.addByte( '\\' );
	// this.addByte( 'u' );
	// this.addByte( '0' );
	// this.addByte( '0' );
	// final byte[] encoded = new byte[ 2 ];
	// ByteScanner.encodeByteIntoTwoAsciiCharBytes( ch, encoded );
	// this.addByte( encoded[ 0 ] );
	// this.addByte( encoded[ 1 ] );
	//
	// } else {
	// this.addByte( ch );
	// }
	//
	// }
	// }
	//
	// this.addByte( '"' );
	// return this;
	// }

	// public ByteBuf addUrlEncoded( String key ) {
	// try {
	// this.addUrlEncodedByteArray( key.getBytes( "UTF-8" ) );
	// } catch ( UnsupportedEncodingException e ) {
	// throw new RuntimeException(e);
	// }
	// return this;
	// }
	//
	// public ByteBuf addJSONEncodedString( String value ) {
	// try {
	// this.addJSONEncodedByteArray( value == null ? null : value.getBytes(
	// "UTF-8" ) );
	// } catch ( UnsupportedEncodingException e ) {
	// throw new RuntimeException( e );
	// }
	// return this;
	// }

	public void write(int b) {
		addByte(b);
	}

	public void write(byte[] b) {
		append(b);
	}

	// public void write( byte[] b, int off, int len ) {
	// this.append(b, len);
	// }

	public void writeBoolean(boolean v) {
		addByte(v ? 1 : 0);
	}

	public void writeByte(byte v) {
		addByte(v);
	}

	public void writeUnsignedByte(short v) {
		addUnsignedByte(v);
	}

	public void addUnsignedByte(short value) {
		if (1 + length < getCapacity()) {
			unsignedByteTo(buffer, length, value);
		} else {
			buffer = Byt.grow(buffer, buffer.length * 2 + 1);

			unsignedByteTo(buffer, length, value);
		}

		length += 1;

	}

	public void writeShort(short v) {
		append(v);
	}

	public void writeUnsignedShort(int v) {
		addUnsignedShort(v);
	}

	public void addUnsignedShort(int value) {

		if (2 + length < getCapacity()) {
			unsignedShortTo(buffer, length, value);
		} else {
			buffer = Byt.grow(buffer, buffer.length * 2 + 2);

			unsignedShortTo(buffer, length, value);
		}

		length += 2;

	}

	public void writeChar(char v) {
		append(v);
	}

	public void writeInt(int v) {
		append(v);
	}

	public void writeUnsignedInt(long v) {
		addUnsignedInt(v);
	}

	public void writeLong(long v) {
		append(v);
	}

	public void writeFloat(float v) {
		append(v);
	}

	public void writeDouble(double v) {
		append(v);
	}

	public void writeLargeString(String s) {
		byte[] bytes = Utf8.toUtf8(s);
		append(bytes.length);
		append(bytes);
	}

	public void writeSmallString(String s) {
		byte[] bytes = Utf8.toUtf8(s);
		addUnsignedByte((short) bytes.length);
		append(bytes);
	}

	public void writeMediumString(String s) {
		byte[] bytes = Utf8.toUtf8(s);
		addUnsignedShort(bytes.length);
		append(bytes);
	}

	public void writeLargeByteArray(byte[] bytes) {
		append(bytes.length);
		append(bytes);
	}

	public void writeSmallByteArray(byte[] bytes) {
		addUnsignedByte((short) bytes.length);
		append(bytes);
	}

	public void writeMediumByteArray(byte[] bytes) {
		addUnsignedShort(bytes.length);
		append(bytes);
	}

	public void writeLargeShortArray(short[] values) {
		int byteSize = values.length * 2 + 4;
		append(values.length);
		doWriteShortArray(values, byteSize);
	}

	public void writeSmallShortArray(short[] values) {
		int byteSize = values.length * 2 + 1;
		addUnsignedByte((short) values.length);
		doWriteShortArray(values, byteSize);
	}

	public void writeMediumShortArray(short[] values) {
		int byteSize = values.length * 2 + 2;
		addUnsignedShort(values.length);
		doWriteShortArray(values, byteSize);
	}

	private void doWriteShortArray( short[] values, int byteSize ) {
        if ( !( byteSize + length < getCapacity()) ) {
            buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
        }
        for (short value : values) {
            append(value);
        }
    }
	public void writeLargeIntArray(int[] values) {
		int byteSize = values.length * 4 + 4;
		append(values.length);
		doWriteIntArray(values, byteSize);
	}

	public void writeSmallIntArray(int[] values) {
		int byteSize = values.length * 4 + 1;
		addUnsignedByte((short) values.length);
		doWriteIntArray(values, byteSize);
	}

	public void writeMediumIntArray(int[] values) {
		int byteSize = values.length * 4 + 2;
		addUnsignedShort(values.length);
		doWriteIntArray(values, byteSize);
	}

	private void doWriteIntArray( int[] values, int byteSize ) {
        if ( !( byteSize + length < getCapacity()) ) {
            buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
        }
        for (int value : values) {
            append(value);
        }
    }
	//
	// public Input input() {
	// return new InputByteArray( this.buffer );
	// }

	public void writeLargeLongArray(long[] values) {
		int byteSize = values.length * 8 + 4;
		append(values.length);
		doWriteLongArray(values, byteSize);
	}

	public void writeSmallLongArray(long[] values) {
		int byteSize = values.length * 8 + 1;
		addUnsignedByte((short) values.length);
		doWriteLongArray(values, byteSize);
	}

	public void writeMediumLongArray(long[] values) {
		int byteSize = values.length * 8 + 2;
		addUnsignedShort(values.length);
		doWriteLongArray(values, byteSize);
	}

	private void doWriteLongArray( long[] values, int byteSize ) {
        if ( !( byteSize + length < getCapacity()) ) {
            buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
        }
        for (long value : values) {
            append(value);
        }
    }
	public void writeLargeFloatArray(float[] values) {
		int byteSize = values.length * 4 + 4;
		append(values.length);
		doWriteFloatArray(values, byteSize);

	}

	public void writeSmallFloatArray(float[] values) {
		int byteSize = values.length * 4 + 1;
		addUnsignedByte((short) values.length);
		doWriteFloatArray(values, byteSize);
	}

	public void writeMediumFloatArray(float[] values) {
		int byteSize = values.length * 4 + 2;
		addUnsignedShort(values.length);
		doWriteFloatArray(values, byteSize);

	}

	private void doWriteFloatArray( float[] values, int byteSize ) {
        if ( !( byteSize + length < getCapacity()) ) {
            buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
        }
        for (float value : values) {
            append(value);
        }
    }
	public void writeLargeDoubleArray(double[] values) {
		int byteSize = values.length * 8 + 4;
		append(values.length);
		doWriteDoubleArray(values, byteSize);

	}

	public void writeSmallDoubleArray(double[] values) {
		int byteSize = values.length * 8 + 1;
		addUnsignedByte((short) values.length);
		doWriteDoubleArray(values, byteSize);

	}

	public void writeMediumDoubleArray(double[] values) {
		int byteSize = values.length * 8 + 2;
		addUnsignedShort(values.length);
		doWriteDoubleArray(values, byteSize);

	}

	private void doWriteDoubleArray( double[] values, int byteSize ) {
        if ( !( byteSize + length < getCapacity()) ) {
            buffer = Byt.grow( buffer, buffer.length * 2 + byteSize );
        }
        for (double value : values) {
            append(value);
        }
    }
	public String toString() {
		int len = len();

		char[] chars = new char[buffer.length];
		for (int index = 0; index < chars.length; index++) {
			chars[index] = (char) buffer[index];
		}
		return new String(chars, 0, len);
		// return new String ( this.buffer, 0, len, StandardCharsets.UTF_8 );
	}

	public byte[] toBytes() {
		return Byt.slc(buffer, 0, length);
	}
	public byte[] toBytes(int start) {
		return Byt.slc(buffer, start, length - start + 1);
	}

	public byte[] slc(int startIndex, int endIndex) {
		return Byt.slc(buffer, startIndex, endIndex);
	}

	public ByteBuf space() {
		append((byte) ' ');
		return this;
	}

	public int getCapacity() {
		return buffer.length;
	}
}
