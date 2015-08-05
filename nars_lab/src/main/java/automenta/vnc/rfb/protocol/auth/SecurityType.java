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

import automenta.vnc.exceptions.UnsupportedSecurityTypeException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Security types that implemented
 */
public enum SecurityType {
	NONE_AUTHENTICATION(1),
	VNC_AUTHENTICATION(2),
//	int RA2_AUTHENTICATION = 5;
//	int RA2NE_AUTHENTICATION = 6;
	TIGHT_AUTHENTICATION(16);
//	int ULTRA_AUTHENTICATION = 17;
//	int TLS_AUTHENTICATION = 18;
//	int VENCRYPT_AUTHENTICATION = 19;

	private final int id;
	private SecurityType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@SuppressWarnings("serial")
	public
	static final Map<Integer, AuthHandler> implementedSecurityTypes =
		new LinkedHashMap<Integer, AuthHandler>() {{
			put(TIGHT_AUTHENTICATION.getId(), new TightAuthentication());
			put(VNC_AUTHENTICATION.getId(), new VncAuthentication());
			put(NONE_AUTHENTICATION.getId(), new NoneAuthentication());
	}};

	public static AuthHandler getAuthHandlerById(int id) throws UnsupportedSecurityTypeException {
		AuthHandler typeSelected = null;
		typeSelected = implementedSecurityTypes.get(id);
		if (null == typeSelected) {
			throw new UnsupportedSecurityTypeException("Not supported: " + id);
		}
		return typeSelected;
	}

}
