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

import java.io.ByteArrayInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZlibDecoder extends Decoder {
	private Inflater decoder;

	@Override
	public void decode(Reader reader, Renderer renderer,
			FramebufferUpdateRectangle rect) throws TransportException {
		int zippedLength = (int) reader.readUInt32();
		if (0 == zippedLength) return;
		int length = rect.width * rect.height * renderer.getBytesPerPixel();
		byte[] bytes = unzip(reader, zippedLength, length);
		Reader unzippedReader =
			new Reader(
					new ByteArrayInputStream(bytes, zippedLength, length));
		RawDecoder.getInstance().decode(unzippedReader, renderer, rect);
	}

	protected byte[] unzip(Reader reader, int zippedLength, int length)
			throws TransportException {
		byte [] bytes = ByteBuffer.getInstance().getBuffer(zippedLength + length);
		reader.readBytes(bytes, 0, zippedLength);
		if (null == decoder) {
			decoder = new Inflater();
		}
		decoder.setInput(bytes, 0, zippedLength);
		try {
			decoder.inflate(bytes, zippedLength, length);
		} catch (DataFormatException e) {
			throw new TransportException("cannot inflate Zlib data", e);
		}
		return bytes;
	}

	@Override
	public void reset() {
		decoder = null;
	}

}
