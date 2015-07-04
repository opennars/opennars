/* RdpPacket_Localised.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Java 1.4 specific extension of RdpPacket class
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */
package automenta.rdp.rdp;

import automenta.rdp.RdpPacket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RdpPacket_Localised extends RdpPacket {

	private ByteBuffer bb = null;

	private int size = 0;

	public RdpPacket_Localised(int capacity) {
		bb = ByteBuffer.allocateDirect(capacity);
		size = capacity;
	}

	public void reset(int length) {
		// logger.info("RdpPacket_Localised.reset(" + length + "), capacity = "
		// + bb.capacity());
		this.end = 0;
		this.start = 0;
		if (bb.capacity() < length)
			bb = ByteBuffer.allocateDirect(length);
		size = length;
		bb.clear();
	}

	public void set8(int where, int what) {
		if (where < 0 || where >= bb.capacity()) {
			throw new ArrayIndexOutOfBoundsException(
					"memory accessed out of Range!");
		}
		bb.put(where, (byte) what);
	}

	public void set8(int what) {
		if (bb.position() >= bb.capacity()) {
			throw new ArrayIndexOutOfBoundsException(
					"memory accessed out of Range!");
		}
		bb.put((byte) what);
	}

	// where is a 8-bit offset
	public int get8(int where) {
		if (where < 0 || where >= bb.capacity()) {
			throw new ArrayIndexOutOfBoundsException(
					"memory accessed out of Range!");
		}
		return bb.get(where) & 0xff; // treat as unsigned byte
	}

	// where is a 8-bit offset
	public int get8() {
		if (bb.position() >= bb.capacity()) {
			throw new ArrayIndexOutOfBoundsException(
					"memory accessed out of Range!");
		}
		return bb.get() & 0xff; // treat as unsigned byte
	}

	public void copyFromByteArray(byte[] array, int array_offset,
			int mem_offset, int len) {
		if ((array_offset >= array.length)
				|| (array_offset + len > array.length)
				|| (mem_offset + len > bb.capacity())) {
			throw new ArrayIndexOutOfBoundsException(
					"memory accessed out of Range!");
		}
		// store position
		int oldpos = getPosition();

		setPosition(mem_offset);
		bb.put(array, array_offset, len);

		// restore position
		setPosition(oldpos);
	}

	public void copyToByteArray(byte[] array, int array_offset, int mem_offset,
			int len) {
		if ((array_offset >= array.length))
			throw new ArrayIndexOutOfBoundsException(
					"Array offset beyond end of array!");
		if (array_offset + len > array.length)
			throw new ArrayIndexOutOfBoundsException(
					"Not enough bytes in array to copy!");
		if (mem_offset + len > bb.capacity())
			throw new ArrayIndexOutOfBoundsException(
					"Memory accessed out of Range!");

		int oldpos = getPosition();
		setPosition(mem_offset);
		bb.get(array, array_offset, len);
		setPosition(oldpos);
	}

	public void copyToPacket(RdpPacket_Localised dst, int srcOffset,
			int dstOffset, int len) {
		int olddstpos = dst.getPosition();
		int oldpos = getPosition();
		dst.setPosition(dstOffset);
		setPosition(srcOffset);
		for (int i = 0; i < len; i++)
			dst.set8(bb.get());
		dst.setPosition(olddstpos);
		setPosition(oldpos);
	}

	public void copyFromPacket(RdpPacket_Localised src, int srcOffset,
			int dstOffset, int len) {
		int oldsrcpos = src.getPosition();
		int oldpos = getPosition();
		src.setPosition(srcOffset);
		setPosition(dstOffset);
		for (int i = 0; i < len; i++)
			bb.put((byte) src.get8());
		src.setPosition(oldsrcpos);
		setPosition(oldpos);
	}

	public int capacity() {
		return bb.capacity();
	}

	// return size in bytes
	public int size() {
		return size;
		// return bb.capacity(); //this.end - this.start;
	}

	public int getPosition() {
		return bb.position();
	}

	public int getLittleEndian16(int where) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort(where);
	}

	public int getLittleEndian16() {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort();
	}

	public int getBigEndian16(int where) {
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.getShort(where);
	}

	public int getBigEndian16() {
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.getShort();
	}

	public void setLittleEndian16(int where, int what) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putShort(where, (short) what);
	}

	public void setLittleEndian16(int what) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putShort((short) what);
	}

	public void setBigEndian16(int where, int what) {
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(where, (short) what);
	}

	public void setBigEndian16(int what) {
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort((short) what);
	}

	public int getLittleEndian32(int where) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt(where);
	}

	public int getLittleEndian32() {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}

	public int getBigEndian32(int where) {
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.getInt(where);
	}

	public int getBigEndian32() {
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.getInt();
	}

	public void setLittleEndian32(int where, int what) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(where, what);
	}

	public void setLittleEndian32(int what) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(what);
	}

	public void setBigEndian32(int where, int what) {
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(where, what);
	}

	public void setBigEndian32(int what) {
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(what);
	}

	public void incrementPosition(int length) {

		if (length > bb.capacity() || length + bb.position() > bb.capacity()
				|| length < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		bb.position(bb.position() + length);
	}

	public void setPosition(int position) {
		if (position > bb.capacity() || position < 0) {
			logger.warn("stream position =" + getPosition() + " end ="
					+ getEnd() + " capacity =" + capacity());
			logger.warn("setPosition(" + position + ") failed");
			throw new ArrayIndexOutOfBoundsException();
		}
		bb.position(position);
	}

}
