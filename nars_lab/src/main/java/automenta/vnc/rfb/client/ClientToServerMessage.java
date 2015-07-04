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

public interface ClientToServerMessage {
	byte SET_PIXEL_FORMAT = 0;
	byte SET_ENCODINGS = 2;
	byte FRAMEBUFFER_UPDATE_REQUEST = 3;
	byte KEY_EVENT = 4;
	byte POINTER_EVENT = 5;
	byte CLIENT_CUT_TEXT = 6;

	void send(Writer writer) throws TransportException;
}
