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
package nars.tuprolog.gui;

import nars.tuprolog.InvalidLibraryException;
import nars.tuprolog.gui.ide.JavaIDE;

/**
 * The GUI launcher chooses the GUI to execute (the Java2 or .NET version)
 * based on the version of the Java Platform tuProlog is executed on.
 *
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.0 - Friday 20th December, 2002
 */

public class TuprologSwing {

    /**
     * Get the version number of the J2SE this program is running on.
     *
     * @param version The J2SE version ID as a <code>java.lang.String</code>.
     * @return The version number of the current J2SE platform (i.e. 1 for
     * the J2SE version 1.3.0).
     */
    private int getVersionNumber(String version) {
        // get the first occurrence of the '.' character
        int firstDotOccurrence = version.indexOf('.');
        // get the main version number: in Java 1.3.0, this number is 1
        String versionNumber = version.substring(0, firstDotOccurrence);
        return Integer.parseInt(versionNumber);
    }

    /**
     * Get the subversion number of the J2SE this program is running on.
     *
     * @param version The J2SE version ID as a <code>java.lang.String</code>.
     * @return The version number of the current J2SE platform (i.e. 3 for
     * the J2SE version 1.3.0).
     */
    private int getSubVersionNumber(String version) {
        // get the first occurrence of the '.' character
        int firstDotOccurrence = version.indexOf('.');
        // get the second occurrence of the '.' character
        int secondDotOccurrence = version.indexOf('.', firstDotOccurrence + 1);
        // get the subversion number: in Java 1.3.0, this number is 3
        String subversion = version.substring(firstDotOccurrence + 1, secondDotOccurrence);
        return Integer.parseInt(subversion);
    }

    /**
     * Launch a GUI suitable for the Java2 platform.
     */
    private void launchJavaGUI() throws InvalidLibraryException {
        JavaIDE ide = new JavaIDE();
        //ide.pack();
        ide.setVisible(true);
    }

    /**
     * Choose which GUI to launch based on the subversion number of the J2SE
     * this program is running on.
     */
    public static void main(String[] args) throws InvalidLibraryException {
        TuprologSwing launcher = new TuprologSwing();
        // Get J2SE version
        String version = System.getProperty("java.version");
        int versionNumber = launcher.getVersionNumber(version);
        int subVersionNumber = launcher.getSubVersionNumber(version);
        System.out.println("J2SE " + versionNumber + '.' + subVersionNumber + ".x plaftorm");

        launcher.launchJavaGUI();
    }

} // end GUILauncher class