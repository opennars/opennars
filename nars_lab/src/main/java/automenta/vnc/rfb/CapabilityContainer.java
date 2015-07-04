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

package automenta.vnc.rfb;

import automenta.vnc.exceptions.TransportException;
import automenta.vnc.rfb.encoding.EncodingType;
import automenta.vnc.transport.Reader;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Container for Tight extention protocol capabilities
 *
 * Needed to set what capabilities we support, which of them supported
 * by server and for enabling/disabling ops on them.
 */
public class CapabilityContainer {
	public CapabilityContainer() { }

	private final Map<Integer, RfbCapabilityInfo> caps = new HashMap<>();

	public void add(RfbCapabilityInfo capabilityInfo) {
		caps.put(capabilityInfo.getCode(), capabilityInfo);
	}

	public void add(int code, String vendor, String name) {
		caps.put(code, new RfbCapabilityInfo(code, vendor, name));
	}

	public void addEnabled(int code, String vendor, String name) {
		RfbCapabilityInfo capability = new RfbCapabilityInfo(code, vendor, name);
		capability.setEnable(true);
		caps.put(code, capability);
	}

	public void setEnable(int id, boolean enable) {
		RfbCapabilityInfo c = caps.get(id);
		if (c != null) {
			c.setEnable(enable);
		}
	}

	public void setAllEnable(boolean enable) {
		for (RfbCapabilityInfo c : caps.values()) {
			c.setEnable(enable);
		}
	}

	public Collection<EncodingType> getEnabledEncodingTypes() {
		Collection<EncodingType> types = new LinkedList<>();
		for (RfbCapabilityInfo c : caps.values()) {
			if (c.isEnabled()) {
				types.add(EncodingType.byId(c.getCode()));
			}
		}
		return types;
	}

	public void read(Reader reader, int count) throws TransportException {
		while (count-- > 0) {
			RfbCapabilityInfo capInfoReceived = new RfbCapabilityInfo(reader);
			Logger.getLogger("com.glavsoft.rfb").fine(capInfoReceived.toString());
			RfbCapabilityInfo myCapInfo = caps.get(capInfoReceived.getCode());
			if (myCapInfo != null) {
				myCapInfo.setEnable(true);
			}
		}
	}

	public boolean isSupported(int code) {
		RfbCapabilityInfo myCapInfo = caps.get(code);
		if (myCapInfo != null)
			return myCapInfo.isEnabled();
		return false;
	}

	public boolean isSupported(RfbCapabilityInfo rfbCapabilityInfo) {
		return isSupported(rfbCapabilityInfo.getCode());
	}

}
