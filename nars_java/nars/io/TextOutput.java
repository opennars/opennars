
/*
 * ExperienceReader.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.io;

import java.awt.FileDialog;
import java.io.*;

import nars.core.NAR;

/**
 * To read and write experience as Task streams
 */
public class TextOutput implements Output {

    private final NAR reasoner;
    /**
     * Input experience from a file
     */
    private PrintWriter outExp;
    

    public interface LineOutput {
        public void println(String s);
    }
    
    private LineOutput outExp2;
    
    /**
     * Default constructor
     *
     * @param reasoner
     */
    public TextOutput(NAR reasoner) {
        this.reasoner = reasoner;
        reasoner.addOutputChannel(this);
    }
    public TextOutput(NAR reasoner, LineOutput outExp2) {
        this(reasoner);
        this.outExp2 = outExp2;
    }

    public TextOutput(NAR reasoner, PrintWriter outExp) {
        this(reasoner);
        this.outExp = outExp;
    }
    public TextOutput(NAR reasoner, PrintStream ps) {
        this(reasoner, new PrintWriter(ps));
    }

    /**
     * Open an output experience file
     */
    public void openSaveFile() {
        FileDialog dialog = new FileDialog((FileDialog) null, "Save experience", FileDialog.SAVE);
        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        String fileName = dialog.getFile();
        try {
            outExp = new PrintWriter(new FileWriter(directoryName + fileName));
        } catch (IOException ex) {
            System.out.println("i/o error: " + ex.getMessage());
        }
    }

    /**
     * Close an output experience file
     */
    public void closeSaveFile() {
        outExp.close();
        reasoner.removeOutputChannel(this);
    }

    /**
     * Process the next chunk of output data
     *
     * @param lines The text to be displayed
     */
    @Override
    public void output(final Channel c, final Object o) {
        final String s = process(c, o);
        if (outExp != null) {
            outExp.println(s);
            outExp.flush();
        }
        if (outExp2 != null) {
            outExp2.println(s);          
        }
    }

    public String process(final Channel c, final Object o) {
        return c.toString() + ": " + o.toString();
    }
}
