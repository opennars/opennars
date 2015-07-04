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

package automenta.vnc.rfb.protocol;

import automenta.vnc.core.SettingsChangedEvent;
import automenta.vnc.rfb.CapabilityContainer;
import automenta.vnc.rfb.IChangeSettingsListener;
import automenta.vnc.rfb.RfbCapabilityInfo;
import automenta.vnc.rfb.encoding.EncodingType;
import automenta.vnc.rfb.protocol.auth.SecurityType;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Protocol Settings class
 */
public class ProtocolSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final EncodingType DEFAULT_PREFERRED_ENCODING = EncodingType.TIGHT;
	public static final int DEFAULT_JPEG_QUALITY = 6;
	private static final int DEFAULT_COMPRESSION_LEVEL = -6;

	// color depth constants
	public static final int COLOR_DEPTH_32 = 32;
    public static final int COLOR_DEPTH_24 = 24;
	public static final int COLOR_DEPTH_16 = 16;
	public static final int COLOR_DEPTH_8 = 8;
	public static final int COLOR_DEPTH_6 = 6;
	public static final int COLOR_DEPTH_3 = 3;

	public static final int COLOR_DEPTH_SERVER_SETTINGS = 0;

	private static final int DEFAULT_COLOR_DEPTH = COLOR_DEPTH_24;

	public static final int CHANGED_VIEW_ONLY                   = 1; // 1 << 0;
	public static final int CHANGED_ENCODINGS                   = 1 << 1;
	public static final int CHANGED_ALLOW_COPY_RECT             = 1 << 2;
	public static final int CHANGED_SHOW_REMOTE_CURSOR          = 1 << 3;
	public static final int CHANGED_MOUSE_CURSOR_TRACK          = 1 << 4;
	public static final int CHANGED_COMPRESSION_LEVEL           = 1 << 5;
	public static final int CHANGED_JPEG_QUALITY                = 1 << 6;
	public static final int CHANGED_ALLOW_CLIPBOARD_TRANSFER    = 1 << 7;
	public static final int CHANGED_CONVERT_TO_ASCII            = 1 << 8;
	public static final int CHANGED_COLOR_DEPTH = 1 << 9;
    public static final int CHANGED_SHARED                      = 1 << 10;

	private transient int changedSettingsMask;

    private boolean sharedFlag;
    private boolean viewOnly;
    private EncodingType preferredEncoding;
    private boolean allowCopyRect;
    private boolean showRemoteCursor;
    private LocalPointer mouseCursorTrack;
    private int compressionLevel;
    private int jpegQuality;
	private boolean allowClipboardTransfer;
	private boolean convertToAscii;
	private int colorDepth;

	public transient LinkedHashSet<EncodingType> encodings;
	private transient final List<IChangeSettingsListener> listeners;

    public final transient CapabilityContainer
		tunnelingCapabilities;
    public final transient CapabilityContainer authCapabilities;
    public final transient CapabilityContainer serverMessagesCapabilities;
    public final transient CapabilityContainer clientMessagesCapabilities;
    public final transient CapabilityContainer encodingTypesCapabilities;
	private transient String remoteCharsetName;

	public static ProtocolSettings getDefaultSettings() {
    	ProtocolSettings settings = new ProtocolSettings();
	    settings.initKnownAuthCapabilities(settings.authCapabilities);
	    settings.initKnownEncodingTypesCapabilities(settings.encodingTypesCapabilities);
        return settings;
    }

	private ProtocolSettings() {
        sharedFlag = true;
        viewOnly = false;
        showRemoteCursor = true;
        mouseCursorTrack = LocalPointer.ON;
        preferredEncoding = DEFAULT_PREFERRED_ENCODING;
        allowCopyRect = true;
        compressionLevel = DEFAULT_COMPRESSION_LEVEL;
        jpegQuality = DEFAULT_JPEG_QUALITY;
        convertToAscii = false;
        allowClipboardTransfer = true;
        colorDepth = COLOR_DEPTH_SERVER_SETTINGS;
        refine();

        listeners = new LinkedList<>();
		tunnelingCapabilities = new CapabilityContainer();
		authCapabilities = new CapabilityContainer();
		serverMessagesCapabilities = new CapabilityContainer();
		clientMessagesCapabilities = new CapabilityContainer();
		encodingTypesCapabilities = new CapabilityContainer();
		changedSettingsMask = 0;
   	}

	public ProtocolSettings(ProtocolSettings s) {
		this();
        copyDataFrom(s);
        changedSettingsMask = s.changedSettingsMask;
        encodings = s.encodings;
    }

    public void copyDataFrom(ProtocolSettings s) {
        copyDataFrom(s, 0);
    }

    public void copyDataFrom(ProtocolSettings s, int mask) {
        if (null == s) return;
        if ((mask & CHANGED_SHARED) == 0) setSharedFlag(s.sharedFlag);
        if ((mask & CHANGED_VIEW_ONLY) == 0) setViewOnly(s.viewOnly);
        if ((mask & CHANGED_ALLOW_COPY_RECT) == 0) setAllowCopyRect(s.allowCopyRect);
        if ((mask & CHANGED_SHOW_REMOTE_CURSOR) == 0) setShowRemoteCursor(s.showRemoteCursor);
        if ((mask & CHANGED_ALLOW_CLIPBOARD_TRANSFER) == 0) setAllowClipboardTransfer(s.allowClipboardTransfer);

        if ((mask & CHANGED_MOUSE_CURSOR_TRACK) == 0) setMouseCursorTrack(s.mouseCursorTrack);
        if ((mask & CHANGED_COMPRESSION_LEVEL) == 0) setCompressionLevel(s.compressionLevel);
        if ((mask & CHANGED_JPEG_QUALITY) == 0) setJpegQuality(s.jpegQuality);
        if ((mask & CHANGED_CONVERT_TO_ASCII) == 0) setConvertToAscii(s.convertToAscii);
        if ((mask & CHANGED_COLOR_DEPTH) == 0) setColorDepth(s.colorDepth);
        if ((mask & CHANGED_ENCODINGS) == 0) setPreferredEncoding(s.preferredEncoding);
    }

    private void initKnownAuthCapabilities(CapabilityContainer cc) {
		cc.addEnabled(SecurityType.NONE_AUTHENTICATION.getId(),
				RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.AUTHENTICATION_NO_AUTH);
		cc.addEnabled(SecurityType.VNC_AUTHENTICATION.getId(),
				RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.AUTHENTICATION_VNC_AUTH);
	    //cc.addEnabled( 19, "VENC", "VENCRYPT");
	    //cc.addEnabled( 20, "GTKV", "SASL____");
	    //cc.addEnabled(129, RfbCapabilityInfo.TIGHT_VNC_VENDOR, "ULGNAUTH");
	    //cc.addEnabled(130, RfbCapabilityInfo.TIGHT_VNC_VENDOR, "XTRNAUTH");
	}

	private void initKnownEncodingTypesCapabilities(CapabilityContainer cc) {
		cc.add(EncodingType.COPY_RECT.getId(),
				RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_COPYRECT);
		cc.add(EncodingType.HEXTILE.getId(),
				RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_HEXTILE);
		cc.add(EncodingType.ZLIB.getId(),
				RfbCapabilityInfo.VENDOR_TRIADA, RfbCapabilityInfo.ENCODING_ZLIB);
		cc.add(EncodingType.ZRLE.getId(),
				RfbCapabilityInfo.VENDOR_TRIADA, RfbCapabilityInfo.ENCODING_ZRLE);
		cc.add(EncodingType.RRE.getId(),
				RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_RRE);
		cc.add(EncodingType.TIGHT.getId(),
				RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_TIGHT);

		cc.add(EncodingType.RICH_CURSOR.getId(),
				RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_RICH_CURSOR);
		cc.add(EncodingType.CURSOR_POS.getId(),
				RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_CURSOR_POS);
		cc.add(EncodingType.DESKTOP_SIZE.getId(),
				RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_DESKTOP_SIZE);
	}

	public void addListener(IChangeSettingsListener listener) {
		listeners.add(listener);
	}

	public byte getSharedFlag() {
		return (byte) (sharedFlag ? 1 : 0);
	}

	public boolean isShared() {
		return sharedFlag;
	}

	public void setSharedFlag(boolean sharedFlag) {
        if (this.sharedFlag != sharedFlag) {
		    this.sharedFlag = sharedFlag;
            changedSettingsMask |= CHANGED_SHARED;
        }
	}

	public boolean isViewOnly() {
		return viewOnly;
	}

	public void setViewOnly(boolean viewOnly) {
		if (this.viewOnly != viewOnly) {
			this.viewOnly = viewOnly;
			changedSettingsMask |= CHANGED_VIEW_ONLY;
		}
	}

	public void enableAllEncodingCaps() {
		encodingTypesCapabilities.setAllEnable(true);

	}

	public int getColorDepth() {
		return colorDepth;
	}

	/**
	 * Set depth only in 3, 6, 8, 16, 32. When depth is wrong, it resets to {@link #DEFAULT_COLOR_DEPTH}
	 */
	public void setColorDepth(int depth) {
		if (colorDepth != depth) {
			changedSettingsMask |= CHANGED_COLOR_DEPTH;
			switch (depth) {
            case COLOR_DEPTH_32:
                colorDepth = COLOR_DEPTH_24;
                break;
			case COLOR_DEPTH_24:
			case COLOR_DEPTH_16:
			case COLOR_DEPTH_8:
			case COLOR_DEPTH_6:
			case COLOR_DEPTH_3:
			case COLOR_DEPTH_SERVER_SETTINGS:
				colorDepth = depth;
				break;
			default:
				colorDepth = DEFAULT_COLOR_DEPTH;
			}
			refine();
		}
	}

	public void refine() {
		LinkedHashSet<EncodingType> encodings = new LinkedHashSet<>();
		if (EncodingType.RAW_ENCODING == preferredEncoding) {
			// when RAW selected send no ordinary encodings so only default RAW encoding will be enabled
		} else {
			encodings.add(preferredEncoding); // preferred first
			encodings.addAll(EncodingType.ordinaryEncodings);
			if (compressionLevel > 0 && compressionLevel < 10) {
				encodings.add(EncodingType.byId(
						EncodingType.COMPRESS_LEVEL_0.getId() + compressionLevel));
			}
			if (jpegQuality > 0 && jpegQuality < 10 &&
					(colorDepth == COLOR_DEPTH_24 || colorDepth == COLOR_DEPTH_SERVER_SETTINGS)) {
				encodings.add(EncodingType.byId(
						EncodingType.JPEG_QUALITY_LEVEL_0.getId() + jpegQuality));
			}
			if (allowCopyRect) {
				encodings.add(EncodingType.COPY_RECT);
			}
		}
		switch(mouseCursorTrack) {
		case OFF:
			setShowRemoteCursor(false);
			break;
		case HIDE:
			setShowRemoteCursor(false);
			encodings.add(EncodingType.RICH_CURSOR);
			encodings.add(EncodingType.CURSOR_POS);
			break;
		case ON:
		default:
			setShowRemoteCursor(true);
			encodings.add(EncodingType.RICH_CURSOR);
			encodings.add(EncodingType.CURSOR_POS);
		}
		encodings.add(EncodingType.DESKTOP_SIZE);
		if ( isEncodingsChanged(this.encodings, encodings) || isChangedEncodings()) {
			this.encodings = encodings;
			changedSettingsMask |= CHANGED_ENCODINGS;
		}
	}

	private boolean isEncodingsChanged(LinkedHashSet<EncodingType> encodings1, LinkedHashSet<EncodingType> encodings2) {
		if (null == encodings1 || encodings1.size() != encodings2.size()) return true;
		Iterator<EncodingType> it1 = encodings1.iterator();
		Iterator<EncodingType> it2 = encodings2.iterator();
		while (it1.hasNext()) {
			EncodingType v1 = it1.next();
			EncodingType v2 = it2.next();
			if (v1 != v2) return true;
		}
		return false;
	}

	public void fireListeners() {
        if (null == listeners) return;
        final SettingsChangedEvent event = new SettingsChangedEvent(new ProtocolSettings(this));
		changedSettingsMask = 0;
		for (IChangeSettingsListener listener : listeners) {
			listener.settingsChanged(event);
		}
	}

	public static boolean isRfbSettingsChangedFired(SettingsChangedEvent event) {
		return event.getSource() instanceof ProtocolSettings;
	}

	public void setPreferredEncoding(EncodingType preferredEncoding) {
		if (this.preferredEncoding != preferredEncoding) {
			this.preferredEncoding = preferredEncoding;
			changedSettingsMask |= CHANGED_ENCODINGS;
			refine();
		}
	}

	public EncodingType getPreferredEncoding() {
		return preferredEncoding;
	}

	public void setAllowCopyRect(boolean allowCopyRect) {
		if (this.allowCopyRect != allowCopyRect) {
			this.allowCopyRect = allowCopyRect;
			changedSettingsMask |= CHANGED_ALLOW_COPY_RECT;
			refine();
		}
	}

	public boolean isAllowCopyRect() {
		return allowCopyRect;
	}

	private void setShowRemoteCursor(boolean showRemoteCursor) {
		if (this.showRemoteCursor != showRemoteCursor) {
			this.showRemoteCursor = showRemoteCursor;
			changedSettingsMask |= CHANGED_SHOW_REMOTE_CURSOR;
		}
	}

	public boolean isShowRemoteCursor() {
		return showRemoteCursor;
	}

	public void setMouseCursorTrack(LocalPointer mouseCursorTrack) {
		if (this.mouseCursorTrack != mouseCursorTrack) {
			this.mouseCursorTrack = mouseCursorTrack;
			changedSettingsMask |= CHANGED_MOUSE_CURSOR_TRACK;
			refine();
		}
	}

	public LocalPointer getMouseCursorTrack() {
		return mouseCursorTrack;
	}

	public void setCompressionLevel(int compressionLevel) {
		if (this.compressionLevel != compressionLevel) {
			this.compressionLevel = compressionLevel;
			changedSettingsMask |= CHANGED_COMPRESSION_LEVEL;
			refine();
		}
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}

	public void setJpegQuality(int jpegQuality) {
		if (this.jpegQuality != jpegQuality) {
			this.jpegQuality = jpegQuality;
			changedSettingsMask |= CHANGED_JPEG_QUALITY;
			refine();
		}
	}

	public int getJpegQuality() {
		return jpegQuality;
	}

	public void setAllowClipboardTransfer(boolean enable) {
		if (this.allowClipboardTransfer != enable) {
			this.allowClipboardTransfer = enable;
			changedSettingsMask |= CHANGED_ALLOW_CLIPBOARD_TRANSFER;
		}
	}

	public boolean isAllowClipboardTransfer() {
		return allowClipboardTransfer;
	}

	public boolean isConvertToAscii() {
		return convertToAscii;
	}

	public void setConvertToAscii(boolean convertToAscii) {
		if (this.convertToAscii != convertToAscii) {
			this.convertToAscii = convertToAscii;
			changedSettingsMask |= CHANGED_CONVERT_TO_ASCII;
		}
	}

	public boolean isChangedEncodings() {
		return (changedSettingsMask & CHANGED_ENCODINGS) == CHANGED_ENCODINGS;
	}

	public boolean isChangedColorDepth() {
		return (changedSettingsMask & CHANGED_COLOR_DEPTH) == CHANGED_COLOR_DEPTH;
	}

	public void setRemoteCharsetName(String remoteCharsetName) {
		this.remoteCharsetName = remoteCharsetName;
	}

	public  String getRemoteCharsetName() {
		return remoteCharsetName;
	}

    @Override
    public String toString() {
        return "ProtocolSettings{" +
                "sharedFlag=" + sharedFlag +
                ", viewOnly=" + viewOnly +
                ", preferredEncoding=" + preferredEncoding +
                ", allowCopyRect=" + allowCopyRect +
                ", showRemoteCursor=" + showRemoteCursor +
                ", mouseCursorTrack=" + mouseCursorTrack +
                ", compressionLevel=" + compressionLevel +
                ", jpegQuality=" + jpegQuality +
                ", allowClipboardTransfer=" + allowClipboardTransfer +
                ", convertToAscii=" + convertToAscii +
                ", colorDepth=" + colorDepth +
                '}';
    }
}
