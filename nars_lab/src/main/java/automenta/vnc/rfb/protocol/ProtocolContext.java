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

package automenta.vnc.rfb.protocol;

import automenta.vnc.rfb.IPasswordRetriever;
import automenta.vnc.rfb.client.ClientToServerMessage;
import automenta.vnc.rfb.encoding.PixelFormat;
import automenta.vnc.rfb.protocol.state.ProtocolState;
import automenta.vnc.transport.Reader;
import automenta.vnc.transport.Writer;

public interface ProtocolContext {

	void changeStateTo(ProtocolState state);

	IPasswordRetriever getPasswordRetriever();

	ProtocolSettings getSettings();

    Writer getWriter();
	Reader getReader();

	int getFbWidth();
	void setFbWidth(int frameBufferWidth);

	int getFbHeight();
	void setFbHeight(int frameBufferHeight);

	PixelFormat getPixelFormat();
	void setPixelFormat(PixelFormat pixelFormat);

	void setRemoteDesktopName(String name);

	void sendMessage(ClientToServerMessage message);

	String getRemoteDesktopName();

	void sendRefreshMessage();
	
	void cleanUpSession(String message);

    void setTight(boolean isTight);
	boolean isTight();

    void setProtocolVersion(String protocolVersion);
    String getProtocolVersion();

}