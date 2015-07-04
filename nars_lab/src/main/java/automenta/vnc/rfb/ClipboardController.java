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

package automenta.vnc.rfb;

/**
 * Interface for handling clipboard texts
 */
public interface ClipboardController extends IChangeSettingsListener {
	void updateSystemClipboard(byte... bytes);

	/**
	 * Get text clipboard contens when needed send to remote, or null vise versa
	 * Implement this method such a way in swing context, because swing's clipboard
	 * update listener invoked only on DataFlavor changes not content changes.
	 * Implement as returned null on systems where clipboard listeners work correctly.
	 *
	 * @return clipboad string contents if it is changed from last method call
	 * or null when clipboard contains non text object or clipboard contents didn't changed
	 */
	String getRenewedClipboardText();

	/**
	 * Returns clipboard text content previously retrieved frim system clipboard by
	 * updateSavedClippoardContent()
	 *
	 * @return clipboard text content
	 */
	String getClipboardText();

	/**
	 * Enable/disable clipboard transfer
	 *
	 * @param enable
	 */
	void setEnabled(boolean enable);

}
