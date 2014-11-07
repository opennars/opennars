/*
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
package nars.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import nars.core.build.Default.CommandLineNARBuilder;
import nars.io.TextInput;
import nars.io.TextOutput;



/**
 * Run Reasoner
 * <p>
 Runs a NAR with addInput. useful for command line or batch functionality; 
 TODO check duplicated code with {@link nars.main.NARS}
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
public class NARRun {

    private final NAR nar;

    private boolean logging;
    private PrintStream out = System.out;
    private final boolean dumpLastState = true;
    int maxTime = 0;



    
    /**
     * The entry point of the standalone application.
     * <p>
     * @param args optional argument used : one addInput file
     */
    public static void main(String args[]) {
                
        NARRun nars = new NARRun(NAR.build(new CommandLineNARBuilder(args)));
        nars.run(args);
        
        // TODO only if single finish ( no reset in between )
        if (nars.dumpLastState) {
            System.out.println("\n==== Dump Last State ====\n"
                    + nars.nar.toString());
        }
    }

    public NARRun(NAR n) {
        this.nar = n;
    }

    /**
     * non-static equivalent to {@link #main(String[])} : finish to completion from
 an addInput file
     */
    public void run(String args[]) {
        TextOutput output = new TextOutput(nar, new PrintWriter(out, true));
        output.setErrors(true);
        output.setErrorStackTrace(true);
        
        if (args.length > 0) {
            try {
                nar.addInput(new TextInput(new File(args[0])));
            } catch (FileNotFoundException ex) {
                System.err.println("NARRun.init: " + ex);
            }
        }
        else {            
            nar.addInput(new TextInput(new BufferedReader(new InputStreamReader(System.in))));
        }
        
               while (true) {
            if (logging)
                log("NARSBatch.run():"
                        + " step " + nar.time()
                        + " " + nar.inputChannels.size());
            

            nar.step(1);
            
            
            if (logging)
                log("NARSBatch.run(): after tick"
                        + " step " + nar.time()
                        + " " + nar.inputChannels.size());
            
            if (maxTime > 0) {
                if ((nar.inputChannels.isEmpty()) || nar.time() == maxTime) {
                    break;
                }
            }
        }
               
        System.exit(0);
    }

    public void setPrintStream(PrintStream out) {
        this.out = out;
    }

    private void log(String mess) {
        if (logging) {
            System.out.println("/ " + mess);
        }
    }
    
    
    
    
    


}
