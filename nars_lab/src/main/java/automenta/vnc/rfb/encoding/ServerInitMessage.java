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

/**
 * Struct filled from the ServerInit message
 * 2  - U16         - framebuffer-width
 * 2  - U16         - framebuffer-height
 * 16 - PixelFormat - server-pixel-format
 * 4  - U32         - name-length
 * name-length - U8 array - name-string
 */
public class ServerInitMessage {
	protected int frameBufferWidth;
    protected int frameBufferHeight;
    protected PixelFormat pixelFormat;
    protected String name;

	public ServerInitMessage(Reader reader) throws TransportException {
		frameBufferWidth = reader.readUInt16();
		frameBufferHeight = reader.readUInt16();
		pixelFormat = new PixelFormat();
		pixelFormat.fill(reader);
		name = reader.readString();
	}

	protected ServerInitMessage() {
		// empty
	}

	public int getFrameBufferWidth() {
		return frameBufferWidth;
	}

	public int getFrameBufferHeight() {
		return frameBufferHeight;
	}

	public PixelFormat getPixelFormat() {
		return pixelFormat;
	}

	public String getName() {
		return name;
	}

    @Override
    public String toString() {
    	return "ServerInitMessage: [name: "+ name +
    	", framebuffer-width: " + String.valueOf(frameBufferWidth) +
    	", framebuffer-height: " + String.valueOf(frameBufferHeight) +
    	", server-pixel-format: " + pixelFormat +
                ']';
    }
}
