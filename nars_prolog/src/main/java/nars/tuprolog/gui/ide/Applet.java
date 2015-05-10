/*
 * tuProlog - Copyright (C) 2001-2004  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog.gui.ide;

import nars.tuprolog.InvalidLibraryException;

import javax.swing.*;
import java.awt.event.WindowListener;

/**
 * A class for serving tuProlog as an applet.
 * Note that since tuProlog cannot run under JVM 1.1, the launched IDE is directly
 * the one for the Java2 platform, without passing through GUILauncher.
 *
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.1 - 27-lug-04
 */

@SuppressWarnings("serial")
public class Applet extends JApplet {

    public void init() {



        try {
            JavaIDE ide = new JavaIDE();

            // Remove the attached window listener, causing the applet to
            // exit and the browser to close due to a System.exit() call.
            WindowListener[] listeners = ide.getWindowListeners();
            for (int i = 0; i < listeners.length; i++)
                ide.removeWindowListener(listeners[i]);

            ide.pack();
            ide.setVisible(true);

        } catch (InvalidLibraryException e) {
            e.printStackTrace();
        }
    }

} // end Applet class