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

import javax.swing.*;
import java.io.FileOutputStream;

/**
 * A common interface to Perceive/Output file operations managers. Note that this
 * interface is not public, since it is intended for internal use, and could be
 * subject to several changes in future releases.
 * 
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.0 - 16-dic-02
 */
@SuppressWarnings("serial")
abstract class IOFileOperations extends JFrame 
{
    /**
	 * The current directory where to load theories.
	 */
    protected String currentLoadDirectory;
    /**
	 * The current directory where to save theories.
	 */
    protected String currentSaveDirectory;

    IOFileOperations() {
        currentLoadDirectory = null;
        currentSaveDirectory = null;
    }

    public abstract void setTypeFileFilter(String type);

    /**
     * Load a theory from an input device, typically a file.
     *
     * @return the loaded theory as an <code>alice.tuprolog.Theory</code> object,
     * or <code>null</code> if the operation is cancelled.
     * @throws java.lang.Exception if something goes wrong.
     */
    public abstract FileIDE loadFile() throws Exception;

    /**
     * Save the engine's current theory to an output device, typically a file.
     *
     * @param theory The String containing the theory to be saved.
     * @return The identifier of the output device where the theory has been
     * saved (i.e. a filename if the output device is a file), or <code>null</code>
     * if the operation is cancelled.
     * @throws java.lang.Exception if something goes wrong.
     */
    public abstract FileIDE saveFileAs(FileIDE fileIDE) throws Exception;
    
    public FileIDE saveFile(FileIDE fileIDE) throws Exception {
        return save(fileIDE);
    }
    
    protected FileIDE save(FileIDE fileIDE) throws Exception {
        if (fileIDE.getFileName()!=null)
        {
            FileOutputStream file = new FileOutputStream(fileIDE.getFilePath()+fileIDE.getFileName());
            file.write(fileIDE.getContent().getBytes());
            file.close();
//            editArea.setSaved(true);
            return fileIDE;
        }
        else
            return saveFileAs(fileIDE);
    }

    public void resetDefaultFileName() {
//        currentTheoryFileName = null;
    }

} // end IOFileOperations interface