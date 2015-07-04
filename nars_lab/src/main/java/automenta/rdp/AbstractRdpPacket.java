/* RdpPacket.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Encapsulates data from a single received packet.
 *          Provides methods for reading from and writing to
 *          an individual packet at all relevant levels.
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
// Created on 03-Sep-2003
package automenta.rdp;

import automenta.rdp.rdp.RdpPacket;
import org.apache.log4j.Logger;

public abstract class AbstractRdpPacket {
	public static final Logger logger = Logger.getLogger(AbstractRdpPacket.class);

	/* constants for Packet */
	public static final int MCS_HEADER = 1;

	public static final int SECURE_HEADER = 2;

	public static final int RDP_HEADER = 3;

	public static final int CHANNEL_HEADER = 4;

	protected int mcs = -1;

	protected int secure = -1;

	protected int rdp = -1;

	protected int channel = -1;

	protected int start = -1;

	protected int end = -1;

	/**
	 * Read an 8-bit integer value from the packet (at current read/write
	 * position)
	 * 
	 * @return Value read from packet
	 */
	public abstract int get8();

	/**
	 * Read an 8-bit integer value from a specified offset in the packet
	 * 
	 * @param where
	 *            Offset to read location
	 * @return Value read from packet
	 */
	public abstract int get8(int where);

	/**
	 * Write 8-bit value to packet at current read/write position
	 * 
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void set8(int what);

	/**
	 * Write 8-bit value to packet at specified offset
	 * 
	 * @param where
	 *            Offset in packet to write location
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void set8(int where, int what);

	/**
	 * Read a 2-byte, little-endian integer value from the packet (at current
	 * read/write position)
	 * 
	 * @return Value read from packet
	 */
	public abstract int getLittleEndian16();

	/**
	 * Read a 2-byte, little-endian integer value from a specified offset in the
	 * packet
	 * 
	 * @param where
	 *            Offset to read location
	 * @return Value read from packet
	 */
	public abstract int getLittleEndian16(int where);

	/**
	 * Write a 2-byte, little-endian integer value to packet at current
	 * read/write position
	 * 
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setLittleEndian16(int what);

	/**
	 * Write a 2-byte, little-endian integer value to packet at specified offset
	 * 
	 * @param where
	 *            Offset in packet to write location
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setLittleEndian16(int where, int what);

	/**
	 * Read a 2-byte, big-endian integer value from the packet (at current
	 * read/write position)
	 * 
	 * @return Value read from packet
	 */
	public abstract int getBigEndian16();

	/**
	 * Read a 2-byte, big-endian integer value from a specified offset in the
	 * packet
	 * 
	 * @param where
	 *            Offset to read location
	 * @return Value read from packet
	 */
	public abstract int getBigEndian16(int where);

	/**
	 * Write a 2-byte, big-endian integer value to packet at current read/write
	 * position
	 * 
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setBigEndian16(int what);

	/**
	 * Write a 2-byte, big-endian integer value to packet at specified offset
	 * 
	 * @param where
	 *            Offset in packet to write location
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setBigEndian16(int where, int what);

	/**
	 * Read a 3-byte, little-endian integer value from the packet (at current
	 * read position)
	 * 
	 * @return Value read from packet
	 */
	public abstract int getLittleEndian32();

	/**
	 * Read a 3-byte, little-endian integer value from a specified offset in the
	 * packet
	 * 
	 * @param where
	 *            Offset to read location
	 * @return Value read from packet
	 */
	public abstract int getLittleEndian32(int where);

	/**
	 * Write a 3-byte, little-endian integer value to packet at current
	 * read/write position
	 * 
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setLittleEndian32(int what);

	/**
	 * Write a 3-byte, little-endian integer value to packet at specified offset
	 * 
	 * @param where
	 *            Offset in packet to write location
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setLittleEndian32(int where, int what);

	/**
	 * Read a 3-byte, big-endian integer value from the packet (at current
	 * read/write position)
	 * 
	 * @return Value read from packet
	 */
	public abstract int getBigEndian32();

	/**
	 * Read a 3-byte, big-endian integer value from a specified offset in the
	 * packet
	 * 
	 * @param where
	 *            Offset to read location
	 * @return Value read from packet
	 */
	public abstract int getBigEndian32(int where);

	/**
	 * Write a 3-byte, big-endian integer value to packet at current read/write
	 * position
	 * 
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setBigEndian32(int what);

	/**
	 * Write a 3-byte, big-endian integer value to packet at specified offset
	 * 
	 * @param where
	 *            Offset in packet to write location
	 * @param what
	 *            Value to write to packet
	 */
	public abstract void setBigEndian32(int where, int what);

	/**
	 * Copy data from this packet to an array of bytes
	 * 
	 * @param array
	 *            Array of bytes to which data should be copied
	 * @param array_offset
	 *            Offset into array for start of data
	 * @param mem_offset
	 *            Offset into packet for start of data
	 * @param len
	 *            Length of data to be copied
	 */
	public abstract void copyToByteArray(byte[] array, int array_offset,
			int mem_offset, int len);

	/**
	 * Copy data to this packet from an array of bytes
	 * 
	 * @param array
	 *            Array of bytes containing source data
	 * @param array_offset
	 *            Offset into array for start of data
	 * @param mem_offset
	 *            Offset into packet for start of data
	 * @param len
	 *            Length of data to be copied
	 */
	public abstract void copyFromByteArray(byte[] array, int array_offset,
			int mem_offset, int len);

	/**
	 * Copy data from this packet to another packet
	 * 
	 * @param dst
	 *            Destination packet
	 * @param srcOffset
	 *            Offset into this packet (source) for start of data
	 * @param dstOffset
	 *            Offset into destination packet for start of data
	 * @param len
	 *            Length of data to be copied
	 */
	public abstract void copyToPacket(RdpPacket dst, int srcOffset,
			int dstOffset, int len);

	/**
	 * Copy data to this packet from another packet
	 * 
	 * @param src
	 *            Source packet
	 * @param srcOffset
	 *            Offset into source packet for start of data
	 * @param dstOffset
	 *            Offset into this packet (destination) for start of data
	 * @param len
	 *            Length of data to be copied
	 */
	public abstract void copyFromPacket(RdpPacket src, int srcOffset,
			int dstOffset, int len);

	/**
	 * Retrieve size of this packet
	 * 
	 * @return Packet size
	 */
	public abstract int size();

	/**
	 * Retrieve offset to current read/write position
	 * 
	 * @return Current read/write position (as byte offset from start)
	 */
	public abstract int position();

	/**
	 * Set current read/write position
	 * 
	 * @param position
	 *            New read/write position (as byte offset from start)
	 */
	public abstract void position(int position);

	/**
	 * Advance the read/write position
	 * 
	 * @param length
	 *            Number of bytes to advance read position by
	 */
	public abstract void positionAdd(int length);

	/**
	 * Mark current read/write position as end of packet
	 */
	public void markEnd() {
		this.end = position();
	}

	/**
	 * Retrieve capacity of this packet
	 * 
	 * @return Packet capacity (in bytes)
	 */
	public abstract int capacity();

	/**
	 * Mark specified position as end of packet
	 * 
	 * @param position
	 *            New end position (as byte offset from start)
	 */
	public void markEnd(int position) {
		if (position > capacity()) {
			throw new ArrayIndexOutOfBoundsException("Mark > size!");
		}
		this.end = position;
	}

	/**
	 * Retrieve location of packet end
	 * 
	 * @return Position of packet end (as byte offset from start)
	 */
	public int getEnd() {
		return this.end;
	}

	/**
	 * Reserve space within this packet for writing of headers for a specific
	 * communications layer. Move read/write position ready for adding data for
	 * a higher communications layer.
	 * 
	 * @param header
	 *            ID of header type
	 * @param increment
	 *            Required size to be reserved for header
	 * @throws RdesktopException
	 */
	public void pushLayer(int header, int increment)  {
		this.setHeader(header);
		this.positionAdd(increment);
		// this.setStart(this.getPosition());
	}

	/**
	 * Get location of the header for a specific communications layer
	 * 
	 * @param header
	 *            ID of header type
	 * @return Location of header, as byte offset from start of packet
	 * @throws RdesktopException
	 */
	public int getHeader(int header) throws RdesktopException {
		switch (header) {
		case AbstractRdpPacket.MCS_HEADER:
			return this.mcs;
		case AbstractRdpPacket.SECURE_HEADER:
			return this.secure;
		case AbstractRdpPacket.RDP_HEADER:
			return this.rdp;
		case AbstractRdpPacket.CHANNEL_HEADER:
			return this.channel;
		default:
			throw new RdesktopException("Wrong Header!");
		}
	}

	/**
	 * Set current read/write position as the start of a layer header
	 * 
	 * @param header
	 *            ID of header type
	 * @throws RdesktopException
	 */
	public void setHeader(int header)  {
		switch (header) {
		case AbstractRdpPacket.MCS_HEADER:
			this.mcs = this.position();
			break;
		case AbstractRdpPacket.SECURE_HEADER:
			this.secure = this.position();
			break;
		case AbstractRdpPacket.RDP_HEADER:
			this.rdp = this.position();
			break;
		case AbstractRdpPacket.CHANNEL_HEADER:
			this.channel = this.position();
			break;
		default:
			throw new RdesktopException("Wrong Header!");
		}
	}

	/**
	 * Retrieve start location of this packet
	 * 
	 * @return Start location of packet (as byte offset from location 0)
	 */
	public int getStart() {
		return this.start;
	}

	/**
	 * Set start position of this packet
	 * 
	 * @param position
	 *            New start position (as byte offset from location 0)
	 */
	public void setStart(int position) {
		this.start = position;
	}

	/**
	 * Add a unicode string to this packet at the current read/write position
	 * 
	 * @param str
	 *            String to write as unicode to packet
	 * @param len
	 *            Desired length of output unicode string
	 *
	 */
	public void outUnicodeString(String str, int len) {
		int i = 0, j = 0;

		if (str.length() != 0) {
			//TODO use use charAt dont call toCharArray which allocates a duplicate array
			char[] name = str.toCharArray();
			while (i < len) {
				this.setLittleEndian16((short) name[j++]);
				i += 2;
			}
			this.setLittleEndian16(0); // Terminating Null Character
		} else {
			this.setLittleEndian16(0);
		}
	}

	/**
	 * Write an ASCII string to this packet at current read/write position
	 * 
	 * @param str
	 *            String to be written
	 * @param length
	 *            Length in bytes to be occupied by string (may be longer than
	 *            string itself)
	 */
	public void out_uint8p(String str, int length) {
		byte[] bStr = str.getBytes();
		this.copyFromByteArray(bStr, 0, this.position(), bStr.length);
		this.positionAdd(length);
	}
}
