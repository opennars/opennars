/* KeyCode_FileBased_Localised.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Java 1.4 specific extension of KeyCode_FileBased class
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
import automenta.rdp.keymapping.KeyCode_FileBased;
import automenta.rdp.keymapping.KeyMapException;

import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.HashMap;

public class KeyCode_FileBased_Localised extends KeyCode_FileBased {

	private HashMap keysCurrentlyDown = new HashMap();

	/**
	 * @param fstream
	 * @throws KeyMapException
	 */
	public KeyCode_FileBased_Localised(InputStream fstream)
			throws KeyMapException {
		super(fstream);
	}

	public KeyCode_FileBased_Localised(String s) throws KeyMapException {
		super(s);
	}

	private void updateCapsLock(KeyEvent e) {
		if (Options.useLockingKeyState) {
			try {
				Options.useLockingKeyState = true;
				capsLockDown = e.getComponent().getToolkit()
						.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
			} catch (Exception uoe) {
				Options.useLockingKeyState = false;
			}
		}
	}

}
