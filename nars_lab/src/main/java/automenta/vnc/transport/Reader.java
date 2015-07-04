// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.transport;

import automenta.vnc.exceptions.ClosedConnectionException;
import automenta.vnc.exceptions.TransportException;

import java.io.*;
import java.nio.charset.Charset;

public class Reader {
	final static Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	final static Charset UTF8 = Charset.forName("UTF-8");
	private final DataInputStream is;

	public Reader(InputStream is) {
		this.is = new DataInputStream(new BufferedInputStream(is));
	}

	public byte readByte() throws TransportException {
		try {
			byte readByte = is.readByte();
			return readByte;
		} catch (EOFException e) {
			throw new ClosedConnectionException(e);
		} catch (IOException e) {
			throw new TransportException("Cannot read byte", e);
		}

	}

	public int readUInt8() throws TransportException {
		return readByte() & 0x0ff;
	}

	public int readUInt16() throws TransportException {
		return readInt16() & 0x0ffff;
	}

	public short readInt16() throws TransportException {
		try {
			short readShort = is.readShort();
			return readShort;
		} catch (EOFException e) {
			throw new ClosedConnectionException(e);
		} catch (IOException e) {
			throw new TransportException("Cannot read int16", e);
		}
	}

	public long readUInt32() throws TransportException {
		return readInt32() & 0xffffffffL;
	}

	public int readInt32() throws TransportException {
		try {
			int readInt = is.readInt();
			return readInt;
		} catch (EOFException e) {
			throw new ClosedConnectionException(e);
		} catch (IOException e) {
			throw new TransportException("Cannot read int32", e);
		}
	}

	public long readInt64() throws TransportException {
		try {
			return is.readLong();
		} catch (EOFException e) {
			throw new ClosedConnectionException(e);
		} catch (IOException e) {
			throw new TransportException("Cannot read int32", e);
		}
	}

	/**
	 * Read string by it length.
	 * Use this method only when sure no character accept ASCII will be read.
	 * Use readBytes and character encoding conversion instead.
	 *
	 * @return String read
	 */
	public String readString(int length) throws TransportException {
        return new String(readBytes(length));
	}

	/**
	 * Read 32-bit string length and then string themself by it length
	 * Use this method only when sure no character accept ASCII will be read.
	 * Use readBytes and character encoding conversion instead or {@link #readUtf8String} method
	 * when utf-8 encoding needed.
	 *
	 * @return String read
	 * @throws TransportException
	 */
	public String readString() throws TransportException {
		// unset most significant (sign) bit 'cause InputStream#readFully reads
		// [int] length bytes from stream. Change when really need read string more
		// than 2147483647 bytes length
		int length = readInt32() & Integer.MAX_VALUE;
		return readString(length);
	}

	/**
	 * Read 32-bit string length and then string themself by it length
	 * Assume UTF-8 character encoding used
	 *
	 * @return String read
	 * @throws TransportException
	 */
	public String readUtf8String() throws TransportException {
		// unset most significant (sign) bit 'cause InputStream#readFully  reads
		// [int] length bytes from stream. Change when really need read string more
		// than 2147483647 bytes length
		int length = readInt32() & Integer.MAX_VALUE;
		return new String(readBytes(length));
	}

	public byte[] readBytes(int length) throws TransportException {
		byte b[] = new byte[length];
		return readBytes(b, 0, length);
	}

	public byte[] readBytes(byte[] b, int offset, int length) throws TransportException {
		try {
			is.readFully(b, offset, length);
			return b;
		} catch (EOFException e) {
			throw new ClosedConnectionException(e);
		} catch (IOException e) {
			throw new TransportException("Cannot read " + length + " bytes array", e);
		}
	}

	public void skip(int length) throws TransportException {
		try {
			is.skipBytes(length);
		} catch (EOFException e) {
			throw new ClosedConnectionException(e);
		} catch (IOException e) {
			throw new TransportException("Cannot skip " + length + " bytes", e);
		}
	}
}