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

/**
 * To read and write experience as Task streams
 */
public class TextInput extends Symbols implements Input {


    
    /**
     * Input experience from a file
     */
    private BufferedReader input;
    
    private boolean finished = false;
    
    private final StringBuilder text = new StringBuilder();   
    
    private int linesPerCycle = 1;
    

    public TextInput(String input) {
        this(new BufferedReader(new StringReader(input)));
    }
    
    public TextInput(File input) throws FileNotFoundException {
        this(new BufferedReader(new FileReader(input)));
    }
    
    public TextInput(URL u) throws IOException {
        this(new BufferedReader(new InputStreamReader(u.openStream())));
    }
    
    public TextInput(BufferedReader input) {        
        this();
        setInput(input);
    }
    
    public TextInput() {
        
    }
    
    protected void setInput(BufferedReader input) {
        this.input = input;
    }

    /** how many input lines to process each cycle.  default=1 */
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
            finished = true;
        }
        return finished;
    }

    @Override
    public Object next() throws IOException {
        String line;
        
        if (input==null) {
            finished = true;
            return null;
        }
        
        text.setLength(0);
        
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
