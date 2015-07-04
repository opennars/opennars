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
import automenta.vnc.rfb.protocol.ProtocolContext;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandshakeState extends ProtocolState {

	public static final String PROTOCOL_VERSION_3_8 = "3.8";
	public static final String PROTOCOL_VERSION_3_7 = "3.7";
	public static final String PROTOCOL_VERSION_3_3 = "3.3";
	private static final int PROTOCOL_STRING_LENGTH = 12;
	private static final String PROTOCOL_STRING_REGEXP = "^RFB (\\d\\d\\d).(\\d\\d\\d)\n$";

	private static final int MIN_SUPPORTED_VERSION_MAJOR = 3;
	private static final int MIN_SUPPORTED_VERSION_MINOR = 3;

	private static final int MAX_SUPPORTED_VERSION_MAJOR = 3;
	private static final int MAX_SUPPORTED_VERSION_MINOR = 8;

	public HandshakeState(ProtocolContext context) {
		super(context);
	}

	@Override
	public boolean next() throws UnsupportedProtocolVersionException, TransportException {
		handshake();
		return true;
	}

	private void handshake() throws TransportException, UnsupportedProtocolVersionException {
		String protocolString = reader.readString(PROTOCOL_STRING_LENGTH);
        Logger.getLogger(getClass().getName()).info("Server sent protocol string: " + protocolString.substring(0, protocolString.length() - 1));
		Pattern pattern = Pattern.compile(PROTOCOL_STRING_REGEXP);
		final Matcher matcher = pattern.matcher(protocolString);
		if ( ! matcher.matches())
			throw new UnsupportedProtocolVersionException(
					"Unsupported protocol version: " + protocolString);
		int major = Integer.parseInt(matcher.group(1));
		int minor = Integer.parseInt(matcher.group(2));
		if (major < MIN_SUPPORTED_VERSION_MAJOR ||
				MIN_SUPPORTED_VERSION_MAJOR == major && minor <MIN_SUPPORTED_VERSION_MINOR)
			throw new UnsupportedProtocolVersionException(
					"Unsupported protocol version: " + major + '.' + minor);
		if (major > MAX_SUPPORTED_VERSION_MAJOR) {
			major = MAX_SUPPORTED_VERSION_MAJOR;
			minor = MAX_SUPPORTED_VERSION_MINOR;
		}

		if (minor >= MIN_SUPPORTED_VERSION_MINOR && minor < 7) {
			changeStateTo(new SecurityType33State(context));
			context.setProtocolVersion(PROTOCOL_VERSION_3_3);
			minor = 3;
		} else if (7 == minor) {
			changeStateTo(new SecurityType37State(context));
			context.setProtocolVersion(PROTOCOL_VERSION_3_7);
			minor = 7;
		} else if (minor >= MAX_SUPPORTED_VERSION_MINOR) {
			changeStateTo(new SecurityTypeState(context));
			context.setProtocolVersion(PROTOCOL_VERSION_3_8);
			minor = 8;
		} else
			throw new UnsupportedProtocolVersionException(
					"Unsupported protocol version: " + protocolString);
		writer.write(("RFB 00" + major + ".00" + minor + '\n').getBytes());
        Logger.getLogger(getClass().getName()).info("Set protocol version to: " + context.getProtocolVersion());
	}

}
