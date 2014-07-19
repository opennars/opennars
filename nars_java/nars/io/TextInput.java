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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import static nars.io.Symbols.ARGUMENT_SEPARATOR;
import static nars.io.Symbols.BUDGET_VALUE_MARK;
import static nars.io.Symbols.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.COMPOUND_TERM_OPENER;
import static nars.io.Symbols.INPUT_LINE;
import static nars.io.Symbols.JUDGMENT_MARK;
import static nars.io.Symbols.OUTPUT_LINE;
import static nars.io.Symbols.PREFIX_MARK;
import static nars.io.Symbols.QUESTION_MARK;
import static nars.io.Symbols.SET_EXT_CLOSER;
import static nars.io.Symbols.SET_EXT_OPENER;
import static nars.io.Symbols.SET_INT_CLOSER;
import static nars.io.Symbols.SET_INT_OPENER;
import static nars.io.Symbols.STAMP_CLOSER;
import static nars.io.Symbols.STAMP_OPENER;
import static nars.io.Symbols.STATEMENT_CLOSER;
import static nars.io.Symbols.STATEMENT_OPENER;
import static nars.io.Symbols.TRUTH_VALUE_MARK;
import static nars.io.Symbols.VALUE_SEPARATOR;
import nars.language.CompoundTerm;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variable;

import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Output.ERR;
import nars.storage.Memory;

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
        this.nar = reasoner;
        this.input = input;
        nar.addInput(this);        
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
        
        text.setLength(0);
        
        try {

            for (int i = 0; i < linesPerCycle; i++) {
                line = input.readLine();
                text.append(line).append('\n');

                if (line == null) {
                    input.close();
                    input = null;
                    finished = true;
                    break;
                }
            }

        } catch (IOException ex) {
            nar.output(ERR.class, ex);
            input = null;
        }        

        return process(text.toString());
    }

    
    /** can be overridden in subclasses to preprocess addInput */
    public String process(String input) {
        return input;
    }


    
}
