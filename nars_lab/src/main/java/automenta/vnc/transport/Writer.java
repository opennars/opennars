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

import automenta.vnc.exceptions.TransportException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Writer {
	private final DataOutputStream os;

	public Writer(OutputStream os) {
		this.os = new DataOutputStream(os);
	}

	public void flush() throws TransportException {
		try {
			os.flush();
		} catch (IOException e) {
			throw new TransportException("Cannot flush output stream", e);
		}
	}

	public void writeByte(int b) throws TransportException {
		write((byte) (b & 0xff));
	}

	public void write(byte b) throws TransportException {
		try {
			os.writeByte(b);
		} catch (IOException e) {
			throw new TransportException("Cannot write byte", e);
		}
	}

	public void writeInt16(int sh) throws TransportException {
		write((short) (sh & 0xffff));
	}

	public void write(short sh) throws TransportException {
		try {
			os.writeShort(sh);
		} catch (IOException e) {
			throw new TransportException("Cannot write short", e);
		}
	}

	public void writeInt32(int i) throws TransportException {
		write(i);
	}

	public void writeInt64(long i) throws TransportException {
		try {
			os.writeLong(i);
		} catch (IOException e) {
			throw new TransportException("Cannot write long", e);
		}
	}

	public void write(int i) throws TransportException {
		try {
			os.writeInt(i);
		} catch (IOException e) {
			throw new TransportException("Cannot write int", e);
		}
	}

	public void write(byte... b) throws TransportException {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int length) throws TransportException {
		write(b, 0, length);
	}

	public void write(byte[] b, int offset, int length)
			throws TransportException {
		try {
			os.write(b, offset, length <= b.length ? length : b.length);
		} catch (IOException e) {
			throw new TransportException("Cannot write " + length + " bytes", e);
		}
	}
}