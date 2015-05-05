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

/**
 * This interface is not public, since it is intended for internal use, and could be subject to several changes in future releases.
 * @author     <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version     1.0 - 14-nov-02
 */

interface InputField {

    /**
     * Since the solve() method must be placed in a class implementing the
     * InputField interface, I need a reference to the Console where output,
     * solveInfo, tuProlog engine and the ProcessInput thread are placed.
     *
     * This behaviour will change as soon as there will be no need of
     * separate input components for .NET and Java2, i.e. as soon as
     * the AltGr bug in Thinlet, preventing the use of italian keycombo
     * AltGr + '?' and AltGr + '+' to write '[' and ']', will be solved.
     */
    void setConsole(ConsoleManager console);

    /**
	 * Get the goal displayed in the input field.
	 * @return  The goal displayed in the input field.
	 */
    public String getGoal();

    /**
     * Add the displayed goal to the history of the requested goals.
     */
    //public void addGoalToHistory();

    /**
     * Enable or disable the possibility of asking for goals to be solved.
     *
     * @param flag true if the query device has to be enabled, false otherwise.
     */
    //public void enableSolveCommands(boolean flag);

} // end InputField interface