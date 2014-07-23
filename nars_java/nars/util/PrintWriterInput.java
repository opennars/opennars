package nars.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import nars.core.NAR;
import nars.io.TextInput;

/**
 * Creates a PrintWriter that pipes into a TextInput's BufferedReader.
 * Useful for TextInput implementations that dynamically generate new input from Java execution.
 */ 
public class PrintWriterInput extends TextInput {
    
    /** Printing to out will be piped into TextInput */
    public final PrintWriter out;

    public PrintWriterInput(NAR n) throws IOException  {
        super(n);
        
        PipedWriter output = new PipedWriter();
        setInput(new BufferedReader(new PipedReader(output)));        
        out = new PrintWriter(output);

        n.addInput(this);
    }

    protected void close() {
        out.close();
    }
}
