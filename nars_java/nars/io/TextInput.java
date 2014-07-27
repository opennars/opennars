/*
 * TextInput.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import nars.core.NAR;
import nars.io.Output.ERR;

/**
 * To read and write experience as Task streams
 */
public class TextInput extends Symbols implements Input {

    /**
     * Reference to the reasoner
     */
    private final NAR nar;
    
    /**
     * Input experience from a file
     */
    private BufferedReader input;
    
    private boolean finished = false;
    
    private StringBuffer text = new StringBuffer();   
    
    private int linesPerCycle = 1024;
    
    public TextInput(NAR reasoner) { 
        this.nar = reasoner;
        nar.addInput(this);        
    }
    

    public TextInput(NAR reasoner, String input) {
        this(reasoner, new BufferedReader(new StringReader(input)));
    }
    
    public TextInput(NAR reasoner, File input) throws FileNotFoundException {
        this(reasoner, new BufferedReader(new FileReader(input)));
    }
    
    public TextInput(NAR reasoner, URL u) throws IOException {
        this(reasoner, new BufferedReader(new InputStreamReader(u.openStream())));
    }
    
    public TextInput(NAR reasoner, BufferedReader input) {
        this(reasoner);
        setInput(input);
    }
    
    protected void setInput(BufferedReader input) {
        this.input = input;
    }

    public void setLinesPerCycle(int linesPerCycle) {
        this.linesPerCycle = linesPerCycle;
    }

    public int getLinesPerCycle() {
        return linesPerCycle;
    }    

    @Override
    public boolean finished(boolean forceStop) {
        if (forceStop) {
            if (input!=null) {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        }
        return finished;
    }

    @Override
    public Object next() {
        String line = null;
        
        if (input==null)
            return null;
        
        text.setLength(0);
        
        try {
            
            int i = linesPerCycle;
            while (i > 0)  {
                
                line = input.readLine();
                if (line == null) {
                    finished = true;
                    break;
                }
                else {
                    if (line.length() > 0) {
                        text.append(line).append('\n');
                        i--;
                    }
                }
            }

        } catch (IOException ex) {
            nar.output(ERR.class, ex);
            finished = true;
        }        
        
        if (finished) {
            try {
                input.close();
            } catch (IOException ex1) {            }
        }

        return process(text.toString());
    }

    
    /** can be overridden in subclasses to preprocess addInput */
    public String process(String input) {
        return input;
    }


    
}
