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

/**
 * A key press or release. Down-ﬂag is non-zero (true) if the key is now pressed, zero
 * (false) if it is now released. The key itself is speciﬁed using the “keysym” values
 * deﬁned by the X Window System.
 * 1 - U8  - message-type
 * 1 - U8  - down-ﬂag
 * 2 - -   - padding
 * 4 - U32 - key
 * For most ordinary keys, the “keysym” is the same as the corresponding ASCII value.
 * For full details, see The Xlib Reference Manual, published by O’Reilly & Associates,
 * or see the header ﬁle <X11/keysymdef.h> from any X Window System installa-
 * tion.
 */
public class KeyEventMessage implements ClientToServerMessage {

	private final int key;
	private final boolean downFlag;

	public KeyEventMessage(int key, boolean downFlag) {
		this.downFlag = downFlag;
		this.key = key;
	}

	@Override
	public void send(Writer writer) throws TransportException {
		writer.writeByte(KEY_EVENT);
		writer.writeByte(downFlag ? 1 : 0);
		writer.writeInt16(0); // padding
		writer.write(key);
		writer.flush();
	}

	@Override
	public String toString() {
		return "[KeyEventMessage: [down-flag: "+downFlag + ", key: " + key + '(' +Integer.toHexString(key)+")]";
	}

}
