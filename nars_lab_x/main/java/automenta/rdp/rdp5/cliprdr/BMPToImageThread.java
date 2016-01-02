/* BMPToImageThread.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */
package automenta.rdp.rdp5.cliprdr;

import automenta.rdp.AbstractRdpPacket;

import java.io.ByteArrayInputStream;

public class BMPToImageThread extends Thread {

	AbstractRdpPacket data;

	int length;

	ClipInterface c;

	public BMPToImageThread(AbstractRdpPacket data, int length, ClipInterface c) {
		super();
		this.data = data;
		this.length = length;
		this.c = c;
	}

	public void run() {
		int origin = data.position();

		int head_len = data.getLittleEndian32();

		data.position(origin);

		byte[] content = new byte[length];

		for (int i = 0; i < length; i++) {
			content[i] = (byte) (data.get8() & 0xFF);
		}

		Image img = ClipBMP.loadbitmap(new ByteArrayInputStream(content));
		ImageSelection imageSelection = new ImageSelection(img);
		c.copyToClipboard(imageSelection);
	}

}
