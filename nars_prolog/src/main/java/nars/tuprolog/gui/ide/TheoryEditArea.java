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
 * An interface to an edit area for Prolog theories.<br> This interface is not public, since it is intended for internal use, and could be subject to several changes in future releases.
 * @author     <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version     1.0 - 13-nov-02
 */

interface TheoryEditArea {

    /**
	 * Display a theory in the edit area.
	 * @param theory  The theory to be displayed in the edit area.
	 */
    public void setTheory(String theory);

    /**
	 * Get the displayed theory as a <code>java.lang.String</code>.
	 * @return  the displayed theory as a <code>java.lang.String</code>.
	 */
    public String getTheory();

    /**
     * Set the line number corresponding to the caret's current position.
     *
     * @param caretLine The line number corresponding to the caret's current position.
     */
    public void setCaretLine(int caretLine);

    /**
     * Get the line number corresponding to the caret's current position.
     *
     * @return the line number corresponding to the caret's current position.
     */
    public int getCaretLine();

    /**
     * Set the dirty flag for the theory contained in the edit area.
     *
     * @param flag <code>true</code> if the theory has been modified after
     * the last Set Theory operation,
     * <code>false</code> otherwise.
     */
    public void setDirty(boolean flag);

    /**
     * Check if the theory in the edit area has been modified after the
     * last Set Theory operation.
     *
     * @return <code>true</code> if the theory has been modified after
     * the last Set Theory operation,
     * <code>false</code> otherwise.
     */
    public boolean isDirty();

    /**
     * Undo last action in the edit area.
     */
    public void undoAction();

    /**
     * Redo last action in the edit area.
     */
    public void redoAction();

} // end TheoryEditArea interface