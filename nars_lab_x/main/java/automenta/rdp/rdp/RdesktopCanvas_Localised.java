/* RdesktopCanvas_Localised.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Java 1.4 specific extension of RdesktopCanvas class
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
package automenta.rdp.rdp;

import automenta.rdp.Options;
import automenta.rdp.RdesktopCanvas;
import automenta.rdp.cv.RDPCV;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Created on 03-Sep-2003

public class RdesktopCanvas_Localised extends RdesktopCanvas {

	private static final long serialVersionUID = -6806580381785981945L;

	private Robot robot = null;


	public RdesktopCanvas_Localised(int width, int height) {
		super(width, height);


		new RDPCV(this);
	}



	public static void saveToFile(Image image) {
		if (Options.server_bpp == 8)
			return;

		BufferedImage img = null;

		img = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.drawImage(image, 0, 0, null);

		// Write generated image to a file
		try {
			// Save as JPEG
			File file = new File("./testimages/" + Options.imgCount + ".jpg");
			Options.imgCount++;
			ImageIO.write(img, "jpg", file);
		} catch (IOException e) {
		}

		g.dispose();
	}



	public void movePointer(int x, int y) {
		Point p = this.getLocationOnScreen();
		x = x + p.x;
		y = y + p.y;
		robot.mouseMove(x, y);
	}

	protected Cursor createCustomCursor(Image wincursor, Point p, String s,
			int cache_idx) {
		return Toolkit.getDefaultToolkit().createCustomCursor(wincursor, p, "");
	}

	public void addNotify() {
		super.addNotify();

		if (robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				logger.warn("Pointer movement not allowed");
			}
		}
	}


	public void update(Graphics g) {

		//super.paintComponent(g);



//
//


		if (!isShowing())
			return;

		Rectangle r = g.getClipBounds();

		int rw = r.width;
		int rh = r.height;
		int bw = backstore.getWidth();
		int bh = backstore.getHeight();
		if (r.x + rw > bw)
			rw = bw - r.x;
		if (r.y + rh > bh)
			rh = bh - r.y;

//		g.drawImage(backstore.getSubimage(r.x, r.y, rw, rh), r.x,
//				r.y, null);

		/*public abstract boolean drawImage(Image img,
		int dx1, int dy1, int dx2, int dy2,
		int sx1, int sy1, int sx2, int sy2,
		ImageObserver observer);*/



////		//draw only updated region:
		final int rxw = r.x + rw;
		final int ryh = r.y + rh;
		g.drawImage(backstore.getImage(), r.x, r.y, rxw, ryh,
				r.x, r.y, rxw, ryh,
				null);

//		g.setClip(0,0,bw, bh);
//
//		//draw everything
//		g.drawImage(backstore.getImage(), 0, 0, null);



//		if (Options.save_graphics) {
//			RdesktopCanvas_Localised.saveToFile(backstore.getSubimage(r.x, r.y,
//					r.width, r.height));
//		}

		// }

	}

}
