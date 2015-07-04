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
import automenta.vnc.transport.Writer;
import automenta.vnc.rfb.protocol.ProtocolContext;
import automenta.vnc.transport.Reader;

abstract public class ProtocolState {
	protected final ProtocolContext context;
	protected final Reader reader;
	protected final Writer writer;

	public ProtocolState(ProtocolContext context) {
		this.context = context;
		this.reader = context.getReader();
		this.writer = context.getWriter();
	}

	/**
	 * Change state of finite machine.
	 *
	 * @param state state, the Finite Machine will switched to
	 */
	protected void changeStateTo(ProtocolState state) {
		context.changeStateTo(state);
	}

	/**
	 * Carry out next step of protocol flow.
	 *
	 * @return false when no next protocol steps availabe, true - when need to continue
	 */
	abstract public boolean next() throws UnsupportedProtocolVersionException, TransportException,
			UnsupportedSecurityTypeException, AuthenticationFailedException, FatalException;

}
