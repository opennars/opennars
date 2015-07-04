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

package automenta.vnc.rfb.client;

import automenta.vnc.exceptions.TransportException;
import automenta.vnc.transport.Writer;


public class FramebufferUpdateRequestMessage implements ClientToServerMessage {
	private final boolean incremental;
	private final int height;
	private final int width;
	private final int y;
	private final int x;

	public FramebufferUpdateRequestMessage(int x, int y, int width,
			int height, boolean incremental) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.incremental = incremental;
	}

	@Override
	public void send(Writer writer) throws TransportException {
		writer.writeByte(FRAMEBUFFER_UPDATE_REQUEST);
		writer.writeByte(incremental ? 1 : 0);
		writer.writeInt16(x);
		writer.writeInt16(y);
		writer.writeInt16(width);
		writer.writeInt16(height);
		writer.flush();
	}

	@Override
	public String toString() {
		return "FramebufferUpdateRequestMessage: [x: " + x + " y: " + y
		+ " width: " + width + " height: " + height +
		" incremental: " + incremental + ']';
	}

}
