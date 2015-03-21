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

package vnc.rfb.encoding.decoder;

import vnc.exceptions.TransportException;
import vnc.rfb.encoding.EncodingType;
import vnc.transport.Reader;

import java.awt.*;

/**
 * Header for framebuffer-update-rectangle header server message
 * 2 - U16 - x-position
 * 2 - U16 - y-position
 * 2 - U16 - width
 * 2 - U16 - height
 * 4 - S32 - encoding-type
 * and then follows the pixel data in the specified encoding
 */
public class FramebufferUpdateRectangle {
	public int x;
	public int y;
	public int width;
	public int height;
    private EncodingType encodingType;
    public final long createdAt;

    public FramebufferUpdateRectangle() {
		this.createdAt = System.currentTimeMillis();
	}

	public FramebufferUpdateRectangle(int x, int y, int w, int h) {
        this();
		this.x = x; this.y = y;
		width = w; height = h;
	}

	public void fill(Reader reader) throws TransportException {
    	x = reader.readUInt16();
        y = reader.readUInt16();
        width = reader.readUInt16();
        height = reader.readUInt16();
        int encoding = reader.readInt32();
		encodingType = EncodingType.byId(encoding);
    }

	public EncodingType getEncodingType() {
		return encodingType;
	}

	@Override
	public String toString() {
		return "FramebufferUpdateRect: [x: " + x + ", y: " + y +
			", width: " + width + ", height: " + height +
			", encodingType: " + encodingType +
                ']';
	}

    public Rectangle newRectangle() {
        return new Rectangle(x, y, width, height);
    }
}
