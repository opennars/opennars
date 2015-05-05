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


import nars.tuprolog.Theory;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;

/**
 * A manager for Perceive/Output operations on the Java 2 platform.
 * 
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.0 - 16-dic-02
 */

public class JavaIOManager extends IOFileOperations {

    private static final long serialVersionUID = 1L;

    private PrologFileFilter fileFilter;
    
    /**
	 * The parent component to open the JFileChooser against.
	 */
    private java.awt.Component parent;

    public JavaIOManager(java.awt.Component parent)
    {
        super();
        this.parent = parent;
        fileFilter = new PrologFileFilter();
    }

    public void setTypeFileFilter(String type)
    {
        fileFilter = new PrologFileFilter();
        if (type.equals("csv"))
            fileFilter.setAsCSVFileFilter();
        if (type.equals("theory"))
            fileFilter.setAsTheoryFileFilter();
        if (type.equals("preferences"))
            fileFilter.setAsPreferencesFileFilter();
    }

    public FileIDE loadFile() throws Exception {
        JFileChooser chooser = new PrologFileChooser(currentLoadDirectory,"load");
        chooser.setFileFilter(fileFilter);
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentLoadDirectory = chooser.getCurrentDirectory().toString();
            String theoryFileName = chooser.getCurrentDirectory() + File.separator + chooser.getSelectedFile().getName();
            Theory theory = new Theory(new FileInputStream(theoryFileName));
            return new FileIDE(theory.toString(),theoryFileName);
        } else
            return new FileIDE("",null);
    }

    public FileIDE saveFileAs(FileIDE fileIDE) throws Exception {
        JFileChooser chooser = new PrologFileChooser(currentSaveDirectory,"save");
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileFilter(fileFilter);
        int returnVal = chooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentSaveDirectory = chooser.getCurrentDirectory().toString();
            fileIDE.setFilePath(currentSaveDirectory+File.separator);
            fileIDE.setFileName(chooser.getSelectedFile().getName());
            if(!hasValidExtension(fileIDE,fileFilter))
            {
                fileIDE.setFileName(fileIDE.getFileName()+ '.' +fileFilter.getDefaultExtension());
            }
            return save(fileIDE);
        } else
            return new FileIDE("",null);
    }

    /**
     * @return true if the fileIDE extension is included in the fileFilter extensions
     */
    private boolean hasValidExtension(FileIDE fileIDE,PrologFileFilter fileFilter)
    {
        String fileExtension = "";
        int i = fileIDE.getFileName().lastIndexOf('.');
        if (i > 0 && i < fileIDE.getFileName().length() - 1)
            fileExtension = fileIDE.getFileName().substring(i + 1).toLowerCase();
        return fileFilter.isMatchingExtension(fileExtension);
    }

    /**
	 * A convenience implementation of FileFilter that filters out all files except the ones supposed to be Prolog files, recognized by the ".pro" or ".pl" extensions.
	 * @author   <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
	 * @version  1.0 - Monday 16th December, 2002
	 */
    private class PrologFileFilter extends javax.swing.filechooser.FileFilter {

        private String[] extensions;
        private String description;
        private String defaultExtension;

        @SuppressWarnings("unused")
        public PrologFileFilter(String[] extensions, String description, String defaultExtension)
        {
            super();
            this.description = description;
            this.extensions = extensions;
            this.defaultExtension = defaultExtension;
        }

        public PrologFileFilter()
        {
            super();
        }

        /**
         * Return true if this file should be shown in the directory pane,
         * false if it shouldn't. Files that begin with "." are ignored.
         *
         * @see FileFilter#accept
         */
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory())
                    return true;
                String extension = getExtension(f);
                if (extension != null && isMatchingExtension(extension))
                    return true;
            }
            return false;
        }

        /**
         * Return the extension portion of the file's name.
         *
         * @param f The file whose name we want the extension of.
         * @return The extension of the file's name.
         */
        private String getExtension(File f) {
            if (f != null) {
                String filename = f.getName();
                int i = filename.lastIndexOf('.');
                if (i > 0 && i < filename.length() - 1)
                    return filename.substring(i + 1).toLowerCase();
            }
            return null;
        }

        private boolean isMatchingExtension(String extension)
        {
            boolean isMatch = false;
            for (int i=0;i<extensions.length && !isMatch;i++)
            {
                if (extension.equals(extensions[i]))
                    isMatch = true;
            }
            return isMatch;
        }

        /**
		 * Returns the human readable description of this filter.
		 * @return  A human readable description of this filter.
		 */
        public String getDescription() {
            return description;
        }

        public void setAsTheoryFileFilter()
        {
            String[] extensions = {"pl", "pro", "2p"};
            this.description = "Prolog files (*.pro, *.pl, *.2p)";
            this.extensions = extensions;
            this.defaultExtension = "pl";
        }

        public void setAsCSVFileFilter()
        {
            String[] extensions = {"csv"};
            this.description = "Comma separated values files (*.csv)";
            this.extensions = extensions;
            this.defaultExtension = "csv";
        }

        public void setAsPreferencesFileFilter()
        {
            String[] extensions = {"2p"};
            this.description = "tuProlog preferences files (*.2p)";
            this.extensions = extensions;
            this.defaultExtension = "2p";
        }

        public String getDefaultExtension()
        {
            return defaultExtension;
        }

       
        @SuppressWarnings("unused")
        public String[] getExtensions()
        {
            return extensions;
        }
    }// end PrologFileFilter class

} // end JavaIOManager class