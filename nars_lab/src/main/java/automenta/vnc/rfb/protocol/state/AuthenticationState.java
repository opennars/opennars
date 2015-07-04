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

import automenta.vnc.exceptions.*;
import automenta.vnc.rfb.protocol.auth.AuthHandler;
import automenta.vnc.rfb.protocol.ProtocolContext;

public class AuthenticationState extends ProtocolState {

	private static final int AUTH_RESULT_OK = 0;
//	private static final int AUTH_RESULT_FAILED = 1;
//	private static final int AUTH_RESULT_TOO_MANY = 2;
	private final AuthHandler authHandler;

	public AuthenticationState(ProtocolContext context,
			AuthHandler authHandler) {
		super(context);
		this.authHandler = authHandler;
	}

	@Override
	public boolean next() throws UnsupportedProtocolVersionException, TransportException,
			UnsupportedSecurityTypeException, AuthenticationFailedException, FatalException {
		authenticate();
		return true;
	}

	private void authenticate() throws TransportException, AuthenticationFailedException,
			FatalException, UnsupportedSecurityTypeException {
		boolean isTight = authHandler.authenticate(reader, writer,
				context.getSettings().authCapabilities, context.getPasswordRetriever());
		// skip when protocol < 3.8 and NONE_AUTH
		if (authHandler.useSecurityResult()) {
			checkSecurityResult();
		}
		changeStateTo(isTight ? new InitTightState(context) : new InitState(context));
		context.setTight(isTight);
	}

	/**
	 * Check Security Result received from server
	 * May be:
	 * * 0 - OK
	 * * 1 - Failed
	 * @throws TransportException
	 * @throws AuthenticationFailedException
	 */
	protected void checkSecurityResult() throws TransportException,
	AuthenticationFailedException {
		if (reader.readInt32() != AUTH_RESULT_OK) {
			try {
				String reason = reader.readString();
				throw new AuthenticationFailedException(reason);
			} catch (ClosedConnectionException e) {
				// protocol version 3.3 and 3.7 does not send rule string,
				// but silently closes the connection
				throw new AuthenticationFailedException("Authentication failed");
			}
		}
	}
}
