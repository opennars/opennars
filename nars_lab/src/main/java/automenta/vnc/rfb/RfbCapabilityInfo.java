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
import automenta.vnc.transport.Reader;

/**
 * Structure used to describe protocol options such as tunneling methods,
 * authentication schemes and message types (protocol versions 3.7t, 3.8t).
 * typedef struct _rfbCapabilityInfo {
 *  CARD32 code;                // numeric identifier
 *  CARD8 vendorSignature[4];   // vendor identification
 *  CARD8 nameSignature[8];     // abbreviated option name
 * } rfbCapabilityInfo;
 */
public class RfbCapabilityInfo {
	/*
	 * Vendors known by TightVNC: standard VNC/RealVNC, TridiaVNC, and TightVNC.
	 * #define rfbStandardVendor "STDV"
	 * #define rfbTridiaVncVendor "TRDV"
	 * #define rfbTightVncVendor "TGHT"
	 */
	public static final String VENDOR_STANDARD = "STDV";
	public static final String VENDOR_TRIADA = "TRDV";
	public static final String VENDOR_TIGHT = "TGHT";

	public static final String TUNNELING_NO_TUNNELING = "NOTUNNEL";

	public static final String AUTHENTICATION_NO_AUTH = "NOAUTH__";
	public static final String AUTHENTICATION_VNC_AUTH ="VNCAUTH_";

	public static final String ENCODING_COPYRECT = "COPYRECT";
	public static final String ENCODING_HEXTILE = "HEXTILE_";
	public static final String ENCODING_ZLIB = "ZLIB____";
	public static final String ENCODING_ZRLE = "ZRLE____";
	public static final String ENCODING_RRE = "RRE_____";
	public static final String ENCODING_TIGHT = "TIGHT___";
	// "Pseudo" encoding types
	public static final String ENCODING_RICH_CURSOR = "RCHCURSR";
	public static final String ENCODING_CURSOR_POS = "POINTPOS";
	public static final String ENCODING_DESKTOP_SIZE = "NEWFBSIZ";

	private int code;
	private String vendorSignature;
	private String nameSignature;
	private boolean enable;

	public RfbCapabilityInfo(int code, String vendorSignature, String nameSignature) {
		this.code = code;
		this.vendorSignature = vendorSignature;
		this.nameSignature = nameSignature;
		enable = true;
	}

	public RfbCapabilityInfo(Reader reader) throws TransportException {
		code = reader.readInt32();
		vendorSignature = reader.readString(4);
		nameSignature = reader.readString(8);
	}

	@Override
	public boolean equals(Object otherObj) {
		if (this == otherObj) { return true; }
		if (null == otherObj) { return false; }
		if (getClass() != otherObj.getClass()) { return false; }
		RfbCapabilityInfo other = (RfbCapabilityInfo) otherObj;
		return code == other.code &&
			vendorSignature.equals(other.vendorSignature) &&
			nameSignature.equals(other.nameSignature);
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getCode() {
		return code;
	}

	public String getVendorSignature() {
		return vendorSignature;
	}

	public String getNameSignature() {
		return nameSignature;
	}

	public boolean isEnabled() {
		return enable;
	}

	@Override
	public String toString() {
		return "RfbCapabilityInfo: [code: " + code +
		", vendor: " + vendorSignature +
		", name: " + nameSignature +
                ']';
	}
}
