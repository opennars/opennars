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


public class HextileDecoder extends Decoder {
	private static final int DEFAULT_TILE_SIZE = 16;
	private static final int RAW_MASK = 1;
	private static final int BACKGROUND_SPECIFIED_MASK = 2;
	private static final int FOREGROUND_SPECIFIED_MASK = 4;
	private static final int ANY_SUBRECTS_MASK = 8;
	private static final int SUBRECTS_COLOURED_MASK = 16;
	private static final int FG_COLOR_INDEX = 0;
	private static final int BG_COLOR_INDEX = 1;

	@Override
	public void decode(Reader reader, Renderer renderer,
			FramebufferUpdateRectangle rect) throws TransportException {
		if (rect.width == 0  || rect.height == 0) return;
		int[] colors = new int[] {-1, -1};
		int maxX = rect.x + rect.width;
		int maxY = rect.y + rect.height;
		for (int tileY = rect.y; tileY < maxY;
		tileY += DEFAULT_TILE_SIZE) {
			int tileHeight = Math.min(maxY - tileY, DEFAULT_TILE_SIZE);
			for (int tileX = rect.x; tileX < maxX;
			tileX += DEFAULT_TILE_SIZE) {
				int tileWidth = Math.min(maxX - tileX, DEFAULT_TILE_SIZE);
				decodeHextileSubrectangle(reader, renderer, colors, tileX,
						tileY, tileWidth, tileHeight);
			}
		}

	}

	private static void decodeHextileSubrectangle(Reader reader,
                                                  Renderer renderer, int[] colors,
                                                  int tileX, int tileY, int tileWidth, int tileHeight)
			throws TransportException {

		int subencoding = reader.readUInt8();

		if ((subencoding & RAW_MASK) != 0) {
			RawDecoder.decode(reader, renderer,
                    tileX, tileY, tileWidth, tileHeight);
			return;
		}

		if ((subencoding & BACKGROUND_SPECIFIED_MASK) != 0) {
			colors[BG_COLOR_INDEX] = renderer.readPixelColor(reader);
		}
		assert colors[BG_COLOR_INDEX] != -1;
		renderer.fillRect(colors[BG_COLOR_INDEX],
				tileX, tileY, tileWidth, tileHeight);

		if ((subencoding & FOREGROUND_SPECIFIED_MASK) != 0) {
			colors[FG_COLOR_INDEX] = renderer.readPixelColor(reader);
		}

		if ((subencoding & ANY_SUBRECTS_MASK) == 0)
			return;

		int numberOfSubrectangles = reader.readUInt8();
		boolean colorSpecified =
			(subencoding & SUBRECTS_COLOURED_MASK) != 0;
		for (int i = 0; i < numberOfSubrectangles; ++i) {
			if (colorSpecified) {
				colors[FG_COLOR_INDEX] = renderer.readPixelColor(reader);
			}
			byte dimensions = reader.readByte(); // bits 7-4 for x, bits 3-0 for y
			int subtileX = dimensions >> 4 & 0x0f;
			int subtileY = dimensions & 0x0f;
			dimensions = reader.readByte(); // bits 7-4 for w, bits 3-0 for h
			int subtileWidth = 1 + (dimensions >> 4 & 0x0f);
			int subtileHeight = 1 + (dimensions & 0x0f);
			assert colors[FG_COLOR_INDEX] != -1;
			renderer.fillRect(colors[FG_COLOR_INDEX],
					tileX + subtileX, tileY + subtileY,
					subtileWidth, subtileHeight);
		}
	}


}
