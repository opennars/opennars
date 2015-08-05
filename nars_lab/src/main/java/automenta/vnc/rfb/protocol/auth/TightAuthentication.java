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

package automenta.vnc.rfb.protocol.auth;

import automenta.vnc.exceptions.FatalException;
import automenta.vnc.exceptions.TransportException;
import automenta.vnc.exceptions.UnsupportedSecurityTypeException;
import automenta.vnc.rfb.CapabilityContainer;
import automenta.vnc.rfb.IPasswordRetriever;
import automenta.vnc.rfb.RfbCapabilityInfo;
import automenta.vnc.rfb.protocol.state.SecurityTypeState;
import automenta.vnc.transport.Reader;
import automenta.vnc.transport.Writer;

import java.util.logging.Logger;

/**
 *
 */
public class TightAuthentication extends AuthHandler {

	@Override
	public SecurityType getType() {
		return SecurityType.TIGHT_AUTHENTICATION;
	}

	@Override
	public boolean authenticate(Reader reader, Writer writer,
			CapabilityContainer authCaps, IPasswordRetriever passwordRetriever)
	throws TransportException, FatalException, UnsupportedSecurityTypeException {
	      initTunnelling(reader, writer);
	      initAuthorization(reader, writer, authCaps, passwordRetriever);
	      return true;
	}

	/**
	 * Negotiation of Tunneling Capabilities (protocol versions 3.7t, 3.8t)
	 *
	 * If the chosen security type is rfbSecTypeTight, the server sends a list of
	 * supported tunneling methods ("tunneling" refers to any additional layer of
	 * data transformation, such as encryption or external compression.)
	 *
	 * nTunnelTypes specifies the number of following rfbCapabilityInfo structures
	 * that list all supported tunneling methods in the order of preference.
	 *
	 * NOTE: If nTunnelTypes is 0, that tells the client that no tunneling can be
	 * used, and the client should not send a response requesting a tunneling
	 * method.
	 *
	 * typedef struct _rfbTunnelingCapsMsg {
	 *     CARD32 nTunnelTypes;
	 *     //followed by nTunnelTypes * rfbCapabilityInfo structures
	 *  } rfbTunnelingCapsMsg;
	 * #define sz_rfbTunnelingCapsMsg 4
	 * ----------------------------------------------------------------------------
	 * Tunneling Method Request (protocol versions 3.7t, 3.8t)
	 *
	 * If the list of tunneling capabilities sent by the server was not empty, the
	 * client should reply with a 32-bit code specifying a particular tunneling
	 * method.  The following code should be used for no tunneling.
	 *
	 * #define rfbNoTunneling 0
	 * #define sig_rfbNoTunneling "NOTUNNEL"
	 *
	 */
	private static void initTunnelling(Reader reader, Writer writer)
			throws TransportException {
		long tunnelsCount;
		tunnelsCount = reader.readUInt32();
		if (tunnelsCount > 0) {
			for (int i = 0; i < tunnelsCount; ++i) {
				RfbCapabilityInfo rfbCapabilityInfo = new RfbCapabilityInfo(reader);
				Logger.getLogger("com.glavsoft.rfb.protocol.auth").fine(rfbCapabilityInfo.toString());
			}
			writer.writeInt32(0); // NOTUNNEL
		}
	}

	/**
	 * Negotiation of Authentication Capabilities (protocol versions 3.7t, 3.8t)
	 *
	 * After setting up tunneling, the server sends a list of supported
	 * authentication schemes.
	 *
	 * nAuthTypes specifies the number of following rfbCapabilityInfo structures
	 * that list all supported authentication schemes in the order of preference.
	 *
	 * NOTE: If nAuthTypes is 0, that tells the client that no authentication is
	 * necessary, and the client should not send a response requesting an
	 * authentication scheme.
	 *
	 * typedef struct _rfbAuthenticationCapsMsg {
	 *     CARD32 nAuthTypes;
	 *     // followed by nAuthTypes * rfbCapabilityInfo structures
	 * } rfbAuthenticationCapsMsg;
	 * #define sz_rfbAuthenticationCapsMsg 4
	 * @param authCaps TODO
	 * @param passwordRetriever
	 * @throws UnsupportedSecurityTypeException
	 * @throws TransportException
	 * @throws FatalException
	 */
	private static void initAuthorization(Reader reader, Writer writer,
                                          CapabilityContainer authCaps, IPasswordRetriever passwordRetriever)
	throws UnsupportedSecurityTypeException, TransportException, FatalException {
		int authCount;
		authCount = reader.readInt32();
		byte[] cap = new byte[authCount];
		for (int i = 0; i < authCount; ++i) {
			RfbCapabilityInfo rfbCapabilityInfo = new RfbCapabilityInfo(reader);
			cap[i] = (byte) rfbCapabilityInfo.getCode();
			Logger.getLogger("com.glavsoft.rfb.protocol.auth").fine(rfbCapabilityInfo.toString());
		}
		AuthHandler authHandler = null;
		if (authCount > 0) {
			authHandler = SecurityTypeState.selectAuthHandler(cap, authCaps);
			for (int i = 0; i < authCount; ++i) {
				if (authCaps.isSupported(cap[i])) {
					//sending back RFB capability code
					writer.writeInt32(cap[i]);
					break;
				}
			}
    	} else {
    		authHandler = SecurityType.getAuthHandlerById(SecurityType.NONE_AUTHENTICATION.getId());
    	}
		Logger.getLogger("com.glavsoft.rfb.protocol.auth").info("Auth capability accepted: " + authHandler.getName());
		authHandler.authenticate(reader, writer, authCaps, passwordRetriever);
	}

}
