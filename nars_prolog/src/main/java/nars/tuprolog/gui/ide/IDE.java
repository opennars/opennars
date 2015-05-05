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
 * A common interface to an IDE. This interface is not public, since it is intended for internal use, and could be subject to several changes in future releases.
 * @author     <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version     1.0 - 1-gen-03
 */

interface IDE {

    /**
     * Enable or disable theory-related commands.
     *
     * @param flag true if the commands have to be enabled, false otherwise.
     */
    public void enableTheoryCommands(boolean flag);

    /**
     * Check if the theory contained in the edit area of the IDE has been feeded
     * to the engine.
     *
     * @return <code>true</code> if the current theory has been feeded to the engine,
     * <code>false</code> otherwise.
     */
    public boolean isFeededTheory();
    
    /**
     * Set the status of the theory contained in the edit area of the IDE.
     * 
     * @param flag <code>true</code> if the theory has been feeded to the engine,
     * <code>false</code> otherwise.
     */
    public void setFeededTheory(boolean flag);

    /**
	 * Get the content of the IDE's editor.
	 * @return  the content of the edit area in the IDE's editor.
	 */
    public String getEditorContent();
    
    /**
	 * Set the content of the IDE's editor.
	 */
    public void setEditorContent(String text);


    public void newTheory();

    public void loadTheory();

    public void saveTheory();

    public void getTheory();
} // end IDE interface