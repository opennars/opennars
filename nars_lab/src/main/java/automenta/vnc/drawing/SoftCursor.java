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

package automenta.vnc.drawing;

/**
 * Abstract class for operations with soft cursor positions, dimensions and
 * hot point position.
 */
public abstract class SoftCursor {

	protected int hotX, hotY;
	private int x, y;
	public int width, height;
	public int rX, rY;
	public int oldRX, oldRY;
	public int oldWidth, oldHeight;
    private final Object lock = new Object();

    public SoftCursor(int hotX, int hotY, int width, int height) {
		this.hotX = hotX;
		this.hotY = hotY;
		oldWidth = this.width = width;
		oldHeight = this.height = height;
		oldRX = rX = 0;
		oldRY = rY = 0;
	}

	/**
	 * Update cursor position
	 *
	 * @param newX
	 * @param newY
	 */
	public void updatePosition(int newX, int newY) {
		oldRX = rX; oldRY = rY;
		oldWidth = width; oldHeight = height;
		x = newX; y = newY;
		rX = x - hotX; rY = y - hotY;
	}

	/**
	 * Set new cursor dimensions and hot point position
	 *
	 * @param hotX
	 * @param hotY
	 * @param width
	 * @param height
	 */
	public void setNewDimensions(int hotX, int hotY, int width,	int height) {
		this.hotX = hotX;
		this.hotY = hotY;
		oldWidth = this.width;
		oldHeight = this.height;
		oldRX = rX; oldRY = rY;
		rX = x - hotX; rY = y - hotY;
		this.width = width;
		this.height = height;
	}

	public void createCursor(int[] cursorPixels, int hotX, int hotY, int width, int height) {
		createNewCursorImage(cursorPixels, hotX, hotY, width, height);
		setNewDimensions(hotX, hotY, width, height);
	}

	protected abstract void createNewCursorImage(int[] cursorPixels, int hotX, int hotY, int width, int height);

    public Object getLock() {
        return lock;
    }
}