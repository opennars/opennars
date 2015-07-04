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
import automenta.vnc.rfb.IPasswordRetriever;
import automenta.vnc.transport.Reader;
import automenta.vnc.transport.Writer;
import automenta.vnc.exceptions.UnsupportedSecurityTypeException;
import automenta.vnc.rfb.CapabilityContainer;

public abstract class AuthHandler {
	/**
	 * Authenticate using apropriate auth scheme
	 * @param reader
	 * @param writer
	 * @param passwordRetriever interface that realise callback function for password retrieving,
	 * ex. by asking user with dialog frame etc.
	 * @param authCaps authentication capabilities
	 *
	 * @return true if there was Tight protocol extention used, false - in the other way
	 * @throws TransportException
	 * @throws FatalException
	 * @throws UnsupportedSecurityTypeException
	 */
	public abstract boolean authenticate(Reader reader, Writer writer,
			CapabilityContainer authCaps, IPasswordRetriever passwordRetriever)
		throws TransportException, FatalException, UnsupportedSecurityTypeException;
	protected boolean useSecurityResult = true;
	public abstract SecurityType getType();
	public int getId() {
		return getType().getId();
	}
	public String getName() {
		return getType().name();
	}
	public boolean useSecurityResult() {
		return useSecurityResult;
	}
	public void setUseSecurityResult(boolean enabled) {
		useSecurityResult = enabled;
	}
}
