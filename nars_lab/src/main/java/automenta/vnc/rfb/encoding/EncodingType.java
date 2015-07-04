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

package automenta.vnc.rfb.encoding;

import java.util.LinkedHashSet;

/**
 * Encoding types
 */
public enum EncodingType {
	/**
	 * Desktop data representes as raw bytes stream
	 */
	RAW_ENCODING(0, "Raw"),
	/**
	 * Specfies encodings which allow to copy part of image in client's
	 * framebuffer from one place to another.
	 */
	COPY_RECT(1, "CopyRect"),
	RRE(2, "RRE"),
	/**
	 *  Hextile encoding, uses palettes, filling and raw subencoding
	 */
    HEXTILE(5, "Hextile"),
    /**
     * This encoding is like raw but previously all data compressed with zlib.
     */
    ZLIB(6, "ZLib"),
	/**
	 * Tight Encoding for slow connection. It is uses raw data, palettes, filling
	 * and jpeg subencodings
	 */
	TIGHT(7, "Tight"),
    //ZlibHex(8),
	/**
	 * ZRLE Encoding is like Hextile but previously all data compressed with zlib.
	 */
    ZRLE(16, "ZRLE"),

    /**
     * Rich Cursor pseudo encoding which allows to transfer cursor shape
     * with transparency
     */
    RICH_CURSOR(0xFFFFFF11, "RichCursor"),
    /**
     * Desktop Size Pseudo encoding allows to notificate client about
     *  remote screen resolution changed.
     */
    DESKTOP_SIZE(0xFFFFFF21, "DesctopSize"),
    /**
     * Cusros position encoding allows to transfer remote cursor position to
     * client side.
     */
	CURSOR_POS(0xFFFFFF18, "CursorPos"),

	COMPRESS_LEVEL_0(0xFFFFFF00 + 0, "CompressionLevel0"),
	COMPRESS_LEVEL_1(0xFFFFFF00 + 1, "CompressionLevel1"),
	COMPRESS_LEVEL_2(0xFFFFFF00 + 2, "CompressionLevel2"),
	COMPRESS_LEVEL_3(0xFFFFFF00 + 3, "CompressionLevel3"),
	COMPRESS_LEVEL_4(0xFFFFFF00 + 4, "CompressionLevel4"),
	COMPRESS_LEVEL_5(0xFFFFFF00 + 5, "CompressionLevel5"),
	COMPRESS_LEVEL_6(0xFFFFFF00 + 6, "CompressionLevel6"),
	COMPRESS_LEVEL_7(0xFFFFFF00 + 7, "CompressionLevel7"),
	COMPRESS_LEVEL_8(0xFFFFFF00 + 8, "CompressionLevel8"),
	COMPRESS_LEVEL_9(0xFFFFFF00 + 9, "CompressionLevel9"),

	JPEG_QUALITY_LEVEL_0(0xFFFFFFE0 + 0, "JpegQualityLevel0"),
	JPEG_QUALITY_LEVEL_1(0xFFFFFFE0 + 1, "JpegQualityLevel1"),
	JPEG_QUALITY_LEVEL_2(0xFFFFFFE0 + 2, "JpegQualityLevel2"),
	JPEG_QUALITY_LEVEL_3(0xFFFFFFE0 + 3, "JpegQualityLevel3"),
	JPEG_QUALITY_LEVEL_4(0xFFFFFFE0 + 4, "JpegQualityLevel4"),
	JPEG_QUALITY_LEVEL_5(0xFFFFFFE0 + 5, "JpegQualityLevel5"),
	JPEG_QUALITY_LEVEL_6(0xFFFFFFE0 + 6, "JpegQualityLevel6"),
	JPEG_QUALITY_LEVEL_7(0xFFFFFFE0 + 7, "JpegQualityLevel7"),
	JPEG_QUALITY_LEVEL_8(0xFFFFFFE0 + 8, "JpegQualityLevel8"),
	JPEG_QUALITY_LEVEL_9(0xFFFFFFE0 + 9, "JpegQualityLevel9");

	private final int id;
	private final String name;
	private EncodingType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public static final LinkedHashSet<EncodingType> ordinaryEncodings = new LinkedHashSet<>();
	static {
		ordinaryEncodings.add(TIGHT);
		ordinaryEncodings.add(HEXTILE);
		ordinaryEncodings.add(ZRLE);
		ordinaryEncodings.add(ZLIB);
		ordinaryEncodings.add(RRE);
		ordinaryEncodings.add(COPY_RECT);
//		ordinaryEncodings.add(RAW_ENCODING);
	}

	public static final LinkedHashSet<EncodingType> pseudoEncodings = new LinkedHashSet<>();
	static {
		pseudoEncodings.add(RICH_CURSOR);
		pseudoEncodings.add(CURSOR_POS);
		pseudoEncodings.add(DESKTOP_SIZE);
	}

	public static final LinkedHashSet<EncodingType> compressionEncodings = new LinkedHashSet<>();
	static {
		compressionEncodings.add(COMPRESS_LEVEL_0);
		compressionEncodings.add(JPEG_QUALITY_LEVEL_0);
	}

	public static EncodingType byId(int id) {
		// TODO needs to speedup with hash usage?
		for (EncodingType type : values()) {
			if (type.getId() == id)
				return type;
		}
		throw new IllegalArgumentException("Unsupported encoding id: " + id);
	}

}
