
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
import java.util.Arrays;

import nars.core.NAR;
import nars.entity.Sentence;

/**
 * To read and write experience as Task streams
 */
public class TextOutput implements Output {

    private final NAR reasoner;
    /**
     * Input experience from a file
     */
    private PrintWriter outExp;
    private boolean errors = true;
    private boolean errorStack;

    

    public interface LineOutput {
        public void println(String s);
    }

    private String prefix = "";
    private LineOutput outExp2;
    
    /**
     * Default constructor
     *
     * @param reasoner
     */
    public TextOutput(NAR reasoner) {
        this.reasoner = reasoner;
        reasoner.addOutput(this);
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
    public TextOutput(NAR reasoner, StringWriter s) {
        this(reasoner, new PrintWriter(s));
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
        reasoner.removeOutput(this);
    }

    /**
     * Process the next chunk of output data
     *
     * @param lines The text to be displayed
     */
    @Override
    public synchronized void output(final Class channel, final Object o) {
        if ((!errors) && (channel == ERR.class))
            return;
        
        final String s = process(channel, o);
        if (outExp != null) {
            outExp.println(prefix + s);
            outExp.flush();
        }
        if (outExp2 != null) {
            outExp2.println(prefix + s);            
        }
    }

    StringBuilder result = new StringBuilder(16 /* estimate */);
    
    public String process(final Class c, final Object o) {
        result.setLength(0);
        result.append(c.getSimpleName()).append(": ");
        if (o instanceof Sentence) {
            result.append(((Sentence) o).display(reasoner.memory.getTime()));
        } else {
            result.append(o.toString());
        }
        if (errorStack && (c == ERR.class)) {
            if (o instanceof Exception) {
                Exception e = (Exception)o;
                result.append(' ').append(Arrays.asList(e.getStackTrace()));
            }
        }
        return result.toString();
    }

    public TextOutput setErrors(boolean errors) {
        this.errors = errors;
        return this;
    }    
    
    public TextOutput setErrorStackTrace(boolean b) {
        this.errorStack = true;
        return this;
    }

    public TextOutput setLinePrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }    
    
}
