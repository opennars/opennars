/* MetafilepictHandler.java
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

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MetafilepictHandler extends TypeHandler {

	/* Mapping Modes */
	public static final int MM_TEXT = 1;

	public static final int MM_LOMETRIC = 2;

	public static final int MM_HIMETRIC = 3;

	public static final int MM_LOENGLISH = 4;

	public static final int MM_HIENGLISH = 5;

	public static final int MM_TWIPS = 6;

	public static final int MM_ISOTROPIC = 7;

	public static final int MM_ANISOTROPIC = 8;

	String[] mapping_modes = { "undefined", "MM_TEXT", "MM_LOMETRIC",
			"MM_HIMETRIC", "MM_LOENGLISH", "MM_HIENGLISH", "MM_TWIPS",
			"MM_ISOTROPIC", "MM_ANISOTROPIC" };

	public boolean formatValid(int format) {
		return (format == CF_METAFILEPICT);
	}

	public boolean mimeTypeValid(String mimeType) {
		return mimeType.equals("image");
	}

	public int preferredFormat() {
		return CF_METAFILEPICT;
	}

	public static Transferable handleData(AbstractRdpPacket data, int length) {
		String thingy = "";
		OutputStream out = null;

		// System.out.print("Metafile mapping mode = ");
		int mm = data.getLittleEndian32();
		// System.out.print(mapping_modes[mm]);
		int width = data.getLittleEndian32();
		// System.out.print(", width = " + width);
		int height = data.getLittleEndian32();
		// System.out.println(", height = " + height);

		try {
			out = new FileOutputStream("test.wmf");

			for (int i = 0; i < (length - 12); i++) {
				int aByte = data.get8();
				out.write(aByte);
				thingy += Integer.toHexString(aByte & 0xFF) + ' ';
			}
			// System.out.println(thingy);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (new StringSelection(thingy));
	}

	public String name() {
		return "CF_METAFILEPICT";
	}

	public static byte[] fromTransferable(Transferable in) {
		return null;
	}

	public void handleData(AbstractRdpPacket data, int length, ClipInterface c) {
		String thingy = "";
		OutputStream out = null;

		// System.out.print("Metafile mapping mode = ");
		int mm = data.getLittleEndian32();
		// System.out.print(mapping_modes[mm]);
		int width = data.getLittleEndian32();
		// System.out.print(", width = " + width);
		int height = data.getLittleEndian32();
		// System.out.println(", height = " + height);

		try {
			out = new FileOutputStream("test.wmf");

			for (int i = 0; i < (length - 12); i++) {
				int aByte = data.get8();
				out.write(aByte);
				thingy += Integer.toHexString(aByte & 0xFF) + ' ';
			}
			// System.out.println(thingy);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.propero.rdp.rdp5.cliprdr.TypeHandler#send_data(java.awt.datatransfer.Transferable,
	 *      net.propero.rdp.rdp5.cliprdr.ClipInterface)
	 */
	public void send_data(Transferable in, ClipInterface c) {
		c.send_null(ClipChannel.CLIPRDR_DATA_RESPONSE,
				ClipChannel.CLIPRDR_ERROR);
	}

}
