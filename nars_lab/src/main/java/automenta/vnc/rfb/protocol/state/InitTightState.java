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

package automenta.vnc.rfb.protocol.state;

import automenta.vnc.rfb.encoding.ServerInitMessage;
import automenta.vnc.rfb.protocol.ProtocolSettings;
import automenta.vnc.exceptions.TransportException;
import automenta.vnc.rfb.protocol.ProtocolContext;

/**
 * Server Interaction Capabilities Message (protocol versions 3.7t, 3.8t)
 *
 * If TightVNC protocol extensions are enabled, the server informs the client
 * what message types it supports in addition to ones defined in the standard
 * RFB protocol.
 * Also, the server sends the list of all supported encodings (note that it's
 * not necessary to advertise the "raw" encoding sinse it MUST be supported in
 * RFB 3.x protocols).
 *
 * This data immediately follows the server initialisation message.
 */
public class InitTightState extends InitState {

	public InitTightState(ProtocolContext context) {
		super(context);
	}

	/**
	 * typedef struct _rfbInteractionCapsMsg {
	 * 		CARD16 nServerMessageTypes;
	 * 		CARD16 nClientMessageTypes;
	 * 		CARD16 nEncodingTypes;
	 * 		CARD16 pad;><------><------>// reserved, must be 0
	 * 		// followed by nServerMessageTypes * rfbCapabilityInfo structures
	 * 		// followed by nClientMessageTypes * rfbCapabilityInfo structures
	 * } rfbInteractionCapsMsg;
	 * #define sz_rfbInteractionCapsMsg 8
	 */
	@Override
	protected void clientAndServerInit() throws TransportException {
		ServerInitMessage serverInitMessage = getServerInitMessage();
		int nServerMessageTypes = reader.readUInt16();
		int nClientMessageTypes = reader.readUInt16();
		int nEncodingTypes = reader.readUInt16();
		reader.readUInt16(); //padding
		ProtocolSettings settings = context.getSettings();
		settings.serverMessagesCapabilities.read(reader, nServerMessageTypes);
		settings.clientMessagesCapabilities.read(reader, nClientMessageTypes);
		settings.encodingTypesCapabilities.read(reader, nEncodingTypes);
		completeContextData(serverInitMessage);
	}

}
