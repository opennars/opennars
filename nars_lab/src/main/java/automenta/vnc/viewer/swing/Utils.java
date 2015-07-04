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

package automenta.vnc.viewer.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Utils for Swing GUI
 */
public class Utils {
	private static List<Image> icons;

	private static List<Image> getApplicationIcons() {
		if (icons != null) {
			return icons;
		}
		icons = new LinkedList<>();
		URL resource = Utils.class.getResource("/com/glavsoft/viewer/images/tightvnc-logo-16x16.png");
		Image image = resource != null ?
				Toolkit.getDefaultToolkit().getImage(resource) :
				null;
		if (image != null) {
			icons.add(image);
		}
		resource = Utils.class.getResource("/com/glavsoft/viewer/images/tightvnc-logo-32x32.png");
		image = resource != null ?
				Toolkit.getDefaultToolkit().getImage(resource) :
				null;
		if (image != null) {
			icons.add(image);
		}
		return icons;
	}

	public static ImageIcon getButtonIcon(String name) {
		URL resource = Utils.class.getResource("/com/glavsoft/viewer/images/button-"+name+".png");
		return resource != null ? new ImageIcon(resource) : null;
	}

    private static final Map<LocalMouseCursorShape, Cursor> cursorCash = new EnumMap(LocalMouseCursorShape.class);
    public static Cursor getCursor(LocalMouseCursorShape cursorShape) {
        Cursor cursor = cursorCash.get(cursorShape);
        if (cursor != null) return cursor;
        String name = cursorShape.getCursorName();
        URL resource = Utils.class.getResource("/com/glavsoft/viewer/images/cursor-"+name+".png");
        if (resource != null) {
            Image image = Toolkit.getDefaultToolkit().getImage(resource);
            if (image != null) {
                final CountDownLatch done = new CountDownLatch(1);
                image.getWidth(new ImageObserver() {
                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        boolean isReady = (infoflags & (ALLBITS | ABORT)) != 0;
                        if (isReady) {
                            done.countDown();
                        }
                        return ! isReady;
                    }
                });
                try {
                    done.await(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return Cursor.getDefaultCursor();
                }
                int w = image.getWidth(null);
                int h = image.getHeight(null);
                if (w < 0 || h < 0) return Cursor.getDefaultCursor();
                w = (int)((w-0.5) / 2);
                h = (int)((h-0.5) / 2);
                cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                        image, new Point(w > 0 ? w: 0, h > 0 ? h : 0), name);
                if (cursor != null) cursorCash.put(cursorShape, cursor);
            }
        }
        return cursor != null ? cursor : Cursor.getDefaultCursor();
    }

    public static void decorateDialog(Window dialog) {
        try {
            dialog.setAlwaysOnTop(true);
        } catch (SecurityException e) {
            // nop
        }
		dialog.pack();
        if (dialog instanceof JDialog) {
		    ((JDialog)dialog).setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        }
        dialog.toFront();
		Utils.setApplicationIconsForWindow(dialog);
	}

	public static void setApplicationIconsForWindow(Window window) {
		List<Image> icons = getApplicationIcons();
		if (icons.size() != 0) {
			window.setIconImages(icons);
		}
	}

	public static void centerWindow(Window window) {
        Point locationPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		Rectangle bounds = window.getBounds();
		locationPoint.setLocation(locationPoint.x - bounds.width/2, locationPoint.y - bounds.height/2);
		window.setLocation(locationPoint);
	}
}
