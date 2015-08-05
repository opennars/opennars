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

package automenta.vnc.viewer;

import automenta.vnc.rfb.client.ClientToServerMessage;
import automenta.vnc.rfb.protocol.ProtocolSettings;
import automenta.vnc.viewer.swing.Surface;

import java.net.Socket;

/**
 * @author dime at tightvnc.com
 */
public interface RfbConnectionWorker extends ConnectionWorker<Void> {

    void setWorkingSocket(Socket workingSocket);

    void setRfbSettings(ProtocolSettings rfbSettings);

    void setUiSettings(UiSettings uiSettings);

    void setConnectionString(String connectionString);

    Surface getSurface();

    void onMessageSend(ClientToServerMessage message);

}
