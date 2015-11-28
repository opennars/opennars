/* UnicodeHandler.java
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
import automenta.rdp.rdp.RdpPacket;
import automenta.rdp.rdp.Utilities_Localised;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class UnicodeHandler extends TypeHandler {

	public boolean formatValid(int format) {
		return (format == CF_UNICODETEXT);
	}

	public boolean mimeTypeValid(String mimeType) {
		return mimeType.equals("text");
	}

	public int preferredFormat() {
		return CF_UNICODETEXT;
	}

	public void handleData(AbstractRdpPacket data, int length, ClipInterface c) {
		String thingy = "";
		for (int i = 0; i < length; i += 2) {
			int aByte = data.getLittleEndian16();
			if (aByte != 0)
				thingy += (char) (aByte);
		}
		c.copyToClipboard(new StringSelection(thingy));
		// return(new StringSelection(thingy));
	}

	public String name() {
		return "CF_UNICODETEXT";
	}

	public static byte[] fromTransferable(Transferable in) {
		String s;
		if (in != null) {
			try {
				s = (String) (in.getTransferData(DataFlavor.stringFlavor));
			} catch (Exception e) {
				s = e.toString();
			}

			// TODO: think of a better way of fixing this
			s = s.replace('\n', (char) 0x0a);
			// s = s.replaceAll("" + (char) 0x0a, "" + (char) 0x0d + (char)
			// 0x0a);
			s = Utilities_Localised.strReplaceAll(s, "" + (char) 0x0a, ""
					+ (char) 0x0d + (char) 0x0a);
			byte[] sBytes = s.getBytes();
			int length = sBytes.length;
			int lengthBy2 = length * 2;
			AbstractRdpPacket p = new RdpPacket(lengthBy2);
			for (int i = 0; i < sBytes.length; i++) {
				p.setLittleEndian16(sBytes[i]);
			}
			sBytes = new byte[length * 2];
			p.copyToByteArray(sBytes, 0, 0, lengthBy2);
			return sBytes;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.propero.rdp.rdp5.cliprdr.TypeHandler#send_data(java.awt.datatransfer.Transferable)
	 */
	public void send_data(Transferable in, ClipInterface c) {
		byte[] data = fromTransferable(in);
		c.send_data(data, data.length);
	}

}