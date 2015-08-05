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

import automenta.vnc.exceptions.TransportException;
import automenta.vnc.exceptions.UnsupportedProtocolVersionException;
import automenta.vnc.exceptions.UnsupportedSecurityTypeException;
import automenta.vnc.rfb.CapabilityContainer;
import automenta.vnc.rfb.protocol.ProtocolContext;
import automenta.vnc.rfb.protocol.auth.AuthHandler;
import automenta.vnc.rfb.protocol.auth.SecurityType;
import automenta.vnc.utils.Strings;

import java.util.logging.Logger;

public class SecurityTypeState extends ProtocolState {

	public SecurityTypeState(ProtocolContext context) {
		super(context);
	}

	@Override
	public boolean next() throws UnsupportedProtocolVersionException, TransportException, UnsupportedSecurityTypeException {
		negotiateAboutSecurityType();
		return true;
	}

	protected void negotiateAboutSecurityType() throws TransportException,
			UnsupportedSecurityTypeException {
		int secTypesNum = reader.readUInt8();
		if (0 == secTypesNum)
			// throw exception with rule
			throw new UnsupportedSecurityTypeException(reader.readString());
		byte[] secTypes = reader.readBytes(secTypesNum);
        Logger.getLogger(getClass().getName()).info("Security Types received (" + secTypesNum + "): "
                + Strings.toString(secTypes));
		AuthHandler typeSelected = selectAuthHandler(
				secTypes, context.getSettings().authCapabilities);
		setUseSecurityResult(typeSelected);
		writer.writeByte(typeSelected.getId());
        Logger.getLogger(getClass().getName()).info("Security Type accepted: " + typeSelected.getName());

		changeStateTo(new AuthenticationState(context, typeSelected));
	}

	/**
	 * Select apropriate security type we supporded from types which server sent
	 *
	 * @param secTypes - byte array with security types server supported
	 * @param authCapabilities
	 * @return {@link AuthHandler} of selected type
	 * @throws UnsupportedSecurityTypeException when no security types server sent we support
	 */
	public static AuthHandler selectAuthHandler(byte[] secTypes, CapabilityContainer authCapabilities)
	throws UnsupportedSecurityTypeException {
		AuthHandler typeSelected = null;
		// Tigh Authentication first
		for (byte type : secTypes) {
			if (SecurityType.TIGHT_AUTHENTICATION.getId() == (0xff & type)) {
				typeSelected = SecurityType.implementedSecurityTypes
					.get(SecurityType.TIGHT_AUTHENTICATION.getId());
				if (typeSelected != null)
					return typeSelected;
			}
		}
		for (byte type : secTypes) {
			typeSelected = SecurityType.implementedSecurityTypes.get(0xff & type);
			if (typeSelected != null &&
					authCapabilities.isSupported(typeSelected.getId()))
				return typeSelected;
		}
		throw new UnsupportedSecurityTypeException(
				"No security types supported. Server sent '"
						+ Strings.toString(secTypes)
						+ "' security types, but we do not support any of their.");
	}

	/**
	 * @param typeSelected
	 */
	protected void setUseSecurityResult(AuthHandler typeSelected) {
		// nop for Protocol version 3.8 and above
	}

}