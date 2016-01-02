/* DIBHandler.java
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
import automenta.rdp.Common;
import automenta.rdp.Input;
import automenta.rdp.rdp.Utilities_Localised;
import org.apache.log4j.Logger;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.ImageObserver;
import java.io.IOException;

public class DIBHandler extends TypeHandler implements ImageObserver {

	protected static Logger logger = Logger.getLogger(Input.class);

	public boolean formatValid(int format) {
		return (format == CF_DIB);
	}

	public boolean mimeTypeValid(String mimeType) {
		return mimeType.equals("image");
	}

	public int preferredFormat() {
		return CF_DIB;
	}

	public String name() {
		return "CF_DIB";
	}

	public void handleData(AbstractRdpPacket data, int length, ClipInterface c) {
		// System.out.println("DIBHandler.handleData");
		BMPToImageThread t = new BMPToImageThread(data, length, c);
		t.start();
	}

	public void send_data(Transferable in, ClipInterface c) {
		byte[] out = null;

		try {
			if (in != null
					&& in
							.isDataFlavorSupported(Utilities_Localised.imageFlavor)) {
				Image img = (Image) in
						.getTransferData(Utilities_Localised.imageFlavor);
				ClipBMP b = new ClipBMP();

				MediaTracker mediaTracker = new MediaTracker(new Frame());
				mediaTracker.addImage(img, 0);

				try {
					mediaTracker.waitForID(0);
				} catch (InterruptedException ie) {
					System.err.println(ie);
					if (!Common.underApplet)
						System.exit(1);
				}
				if (img == null)
					return;

				int width = img.getWidth(this);
				int height = img.getHeight(this);
				out = b.getBitmapAsBytes(img, width, height);

				c.send_data(out, out.length);
			}
		} catch (UnsupportedFlavorException e) {
			System.err
					.println("Failed to send DIB: UnsupportedFlavorException");
		} catch (IOException e) {
			System.err.println("Failed to send DIB: IOException");
		}

	}

	public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5) {
		return false;
	}

}