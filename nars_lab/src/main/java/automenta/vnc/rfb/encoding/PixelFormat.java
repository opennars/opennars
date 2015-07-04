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

package automenta.vnc.rfb.encoding;

import automenta.vnc.exceptions.TransportException;
import automenta.vnc.transport.Reader;
import automenta.vnc.transport.Writer;

/**
 * Pixel Format:
 * 1 - U8  - bits-per-pixel
 * 1 - U8  - depth
 * 1 - U8  - big-endian-flag
 * 1 - U8  - true-color-flag
 * 2 - U16 - red-max
 * 2 - U16 - green-max
 * 2 - U16 - blue-max
 * 1 - U8  - red-shift
 * 1 - U8  - green-shift
 * 1 - U8  - blue-shift
 * 3 -     - padding
 */
public class PixelFormat {
	public byte bitsPerPixel;
	public byte depth;
	public byte bigEndianFlag;
	public byte trueColourFlag;
	public short redMax;
	public short greenMax;
	public short blueMax;
	public byte redShift;
	public byte greenShift;
	public byte blueShift;

	public void fill(Reader reader) throws TransportException {
		bitsPerPixel = reader.readByte();
		depth = reader.readByte();
	    bigEndianFlag = reader.readByte();
	    trueColourFlag = reader.readByte();
	    redMax = reader.readInt16();
	    greenMax = reader.readInt16();
	    blueMax = reader.readInt16();
	    redShift = reader.readByte();
	    greenShift = reader.readByte();
	    blueShift = reader.readByte();
	    reader.readBytes(3); // skip padding bytes
	}

    public void send(Writer writer) throws TransportException {
    	writer.write(bitsPerPixel);
    	writer.write(depth);
    	writer.write(bigEndianFlag);
    	writer.write(trueColourFlag);
    	writer.write(redMax);
    	writer.write(greenMax);
    	writer.write(blueMax);
    	writer.write(redShift);
    	writer.write(greenShift);
    	writer.write(blueShift);
    	writer.writeInt16(0); // padding bytes
    	writer.writeByte(0); // padding bytes
    }

    public static PixelFormat create24bitColorDepthPixelFormat(int bigEndianFlag) {
    	final PixelFormat pixelFormat = new PixelFormat();
    	pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
		pixelFormat.bitsPerPixel = 32;
		pixelFormat.blueMax = 255;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 255;
		pixelFormat.greenShift = 8;
		pixelFormat.redMax = 255;
		pixelFormat.redShift = 16;
		pixelFormat.depth = 24;
		pixelFormat.trueColourFlag = 1;
		return pixelFormat;
    }

    /**
     * specifies 65536 colors, 5bit per Red, 6bit per Green, 5bit per Blue
     */
    public static PixelFormat create16bitColorDepthPixelFormat(int bigEndianFlag) {
    	final PixelFormat pixelFormat = new PixelFormat();
    	pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
		pixelFormat.bitsPerPixel = 16;
		pixelFormat.blueMax = 31;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 63;
		pixelFormat.greenShift = 5;
		pixelFormat.redMax = 31;
		pixelFormat.redShift = 11;
		pixelFormat.depth = 16;
		pixelFormat.trueColourFlag = 1;
		return pixelFormat;
    }

    /**
     * specifies 256 colors, 2bit per Blue, 3bit per Green & Red
     */
    public static PixelFormat create8bitColorDepthBGRPixelFormat(int bigEndianFlag) {
    	final PixelFormat pixelFormat = new PixelFormat();
    	pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
		pixelFormat.bitsPerPixel = 8;
		pixelFormat.redMax = 7;
		pixelFormat.redShift = 0;
		pixelFormat.greenMax = 7;
		pixelFormat.greenShift = 3;
		pixelFormat.blueMax = 3;
		pixelFormat.blueShift = 6;
		pixelFormat.depth = 8;
		pixelFormat.trueColourFlag = 1;
		return pixelFormat;
    }

    /**
     * specifies 64 colors, 2bit per Red, Green & Blue
     */
    public static PixelFormat create6bitColorDepthPixelFormat(int bigEndianFlag) {
    	final PixelFormat pixelFormat = new PixelFormat();
    	pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
		pixelFormat.bitsPerPixel = 8;
		pixelFormat.blueMax = 3;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 3;
		pixelFormat.greenShift = 2;
		pixelFormat.redMax = 3;
		pixelFormat.redShift = 4;
		pixelFormat.depth = 6;
		pixelFormat.trueColourFlag = 1;
		return pixelFormat;
    }

    /**
     * specifies 8 colors, 1bit per Red, Green & Blue
     */
    public static PixelFormat create3bppPixelFormat(int bigEndianFlag) {
    	final PixelFormat pixelFormat = new PixelFormat();
    	pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
		pixelFormat.bitsPerPixel = 8;
		pixelFormat.blueMax = 1;
		pixelFormat.blueShift = 0;
		pixelFormat.greenMax = 1;
		pixelFormat.greenShift = 1;
		pixelFormat.redMax = 1;
		pixelFormat.redShift = 2;
		pixelFormat.depth = 3;
		pixelFormat.trueColourFlag = 1;
		return pixelFormat;
    }

    @Override
    public String toString() {
    	return "PixelFormat: [bits-per-pixel: " + String.valueOf(0xff & bitsPerPixel) +
    	", depth: " + String.valueOf(0xff & depth) +
    	", big-endian-flag: " + String.valueOf(0xff & bigEndianFlag) +
    	", true-color-flag: " + String.valueOf(0xff & trueColourFlag) +
    	", red-max: " + String.valueOf(0xffff & redMax) +
    	", green-max: " + String.valueOf(0xffff & greenMax) +
    	", blue-max: " + String.valueOf(0xffff & blueMax) +
    	", red-shift: " + String.valueOf(0xff & redShift) +
    	", green-shift: " + String.valueOf(0xff & greenShift) +
    	", blue-shift: " + String.valueOf(0xff & blueShift) +
                ']';
    }
}
