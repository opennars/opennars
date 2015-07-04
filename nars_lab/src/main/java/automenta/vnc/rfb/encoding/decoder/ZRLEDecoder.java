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

package automenta.vnc.rfb.encoding.decoder;

import automenta.vnc.drawing.Renderer;
import automenta.vnc.exceptions.TransportException;
import automenta.vnc.transport.Reader;

public class ZRLEDecoder extends ZlibDecoder {
	private static final int MAX_TILE_SIZE = 64;
    private int[] decodedBitmap;
    private int[] palette;

    @Override
	public void decode(Reader reader, Renderer renderer,
			FramebufferUpdateRectangle rect) throws TransportException {
		int zippedLength = (int) reader.readUInt32();
		if (0 == zippedLength) return;
		int length = rect.width * rect.height * renderer.getBytesPerPixel();
		byte[] bytes = unzip(reader, zippedLength, length);
		int offset = zippedLength;
		int maxX = rect.x + rect.width;
		int maxY = rect.y + rect.height;
        if (null == palette) {
            palette = new int [128];
        }
        if (null == decodedBitmap) {
            decodedBitmap = new int[MAX_TILE_SIZE * MAX_TILE_SIZE];
        }
		for (int tileY = rect.y; tileY < maxY; tileY += MAX_TILE_SIZE) {
			int tileHeight = Math.min(maxY - tileY, MAX_TILE_SIZE);

			for (int tileX = rect.x; tileX < maxX; tileX += MAX_TILE_SIZE) {
				int tileWidth = Math.min(maxX - tileX, MAX_TILE_SIZE);
				int subencoding = bytes[offset++] & 0x0ff;
				// 128 -plain RLE, 130-255 - Palette RLE
				boolean isRle = (subencoding & 128) != 0;
				// 2 to 16 for raw packed palette data, 130 to 255 for Palette RLE (subencoding - 128)
				int paletteSize = subencoding & 127;
				offset += readPalette(bytes, offset, renderer, paletteSize);
				if (1 == subencoding) { // A solid tile consisting of a single colour
					renderer.fillRect(palette[0], tileX, tileY, tileWidth, tileHeight);
					continue;
				}
				if (isRle) {
					if (0 == paletteSize) { // subencoding == 128 (or paletteSize == 0) - Plain RLE
						offset += decodePlainRle(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
					} else {
						offset += decodePaletteRle(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
					}
				} else {
					if (0 == paletteSize) { // subencoding == 0 (or paletteSize == 0) - raw CPIXEL data
						offset += decodeRaw(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
					} else {
						offset += decodePacked(bytes, offset, renderer, paletteSize, tileX, tileY, tileWidth, tileHeight);
					}
				}
			}
		}
	}

	private int decodePlainRle(byte[] bytes, int offset, Renderer renderer,
			int tileX, int tileY, int tileWidth, int tileHeight) {
		int bytesPerCPixel = renderer.getBytesPerCPixel();
		int decodedOffset = 0;
		int decodedEnd = tileWidth * tileHeight;
		int index = offset;
		while (decodedOffset < decodedEnd) {
			int color = renderer.getCompactPixelColor(bytes, index);
			index += bytesPerCPixel;
			int rlength = 1;
			do {
				rlength += bytes[index] & 0x0ff;
			} while ((bytes[index++] & 0x0ff) == 255);
			assert rlength <= decodedEnd - decodedOffset;
			Renderer.fillColorBitmapWithColor(decodedBitmap, decodedOffset, rlength, color);
			decodedOffset += rlength;
		}
		renderer.drawColoredBitmap(decodedBitmap, tileX, tileY, tileWidth, tileHeight);
		return index - offset;
	}

	private int decodePaletteRle(byte[] bytes, int offset, Renderer renderer,
                                 int tileX, int tileY, int tileWidth, int tileHeight) {
		int decodedOffset = 0;
		int decodedEnd = tileWidth * tileHeight;
		int index = offset;
		while (decodedOffset < decodedEnd) {
			int colorIndex = bytes[index++];
			int color = palette[colorIndex & 127];
			int rlength = 1;
			if ((colorIndex & 128) != 0) {
				do {
					rlength += bytes[index] & 0x0ff;
				} while (bytes[index++] == (byte) 255);
			}
			assert rlength <= decodedEnd - decodedOffset;
			Renderer.fillColorBitmapWithColor(decodedBitmap, decodedOffset, rlength, color);
			decodedOffset += rlength;
		}
		renderer.drawColoredBitmap(decodedBitmap, tileX, tileY, tileWidth, tileHeight);
		return index - offset;
	}

	private int decodePacked(byte[] bytes, int offset, Renderer renderer,
                             int paletteSize, int tileX, int tileY, int tileWidth, int tileHeight) {
		int bitsPerPalletedPixel = paletteSize > 16 ? 8 : paletteSize > 4 ? 4
				: paletteSize > 2 ? 2 : 1;
		int packedOffset = offset;
		int decodedOffset = 0;
		for (int i = 0; i < tileHeight; ++i) {
			int decodedRowEnd = decodedOffset + tileWidth;
			int byteProcessed = 0;
			int bitsRemain = 0;

			while (decodedOffset < decodedRowEnd) {
				if (bitsRemain == 0) {
					byteProcessed = bytes[packedOffset++];
					bitsRemain = 8;
				}
				bitsRemain -= bitsPerPalletedPixel;
				int index = byteProcessed >> bitsRemain & (1 << bitsPerPalletedPixel) - 1 & 127;
				int color = palette[index];
				Renderer.fillColorBitmapWithColor(decodedBitmap, decodedOffset, 1, color);
				++decodedOffset;
			}
		}
		renderer.drawColoredBitmap(decodedBitmap, tileX, tileY, tileWidth, tileHeight);
		return packedOffset - offset;
	}

	private static int decodeRaw(byte[] bytes, int offset, Renderer renderer,
                                 int tileX, int tileY, int tileWidth, int tileHeight) throws TransportException {
		return renderer.drawCompactBytes(bytes, offset, tileX, tileY, tileWidth, tileHeight);
	}

	private int readPalette(byte[] bytes, int offset, Renderer renderer, int paletteSize) {
        final int bytesPerCPixel = renderer.getBytesPerCPixel();
		for (int i=0; i<paletteSize; ++i) {
            palette[i] = renderer.getCompactPixelColor(bytes, offset + i* bytesPerCPixel);
		}
		return paletteSize * bytesPerCPixel;
	}

}
