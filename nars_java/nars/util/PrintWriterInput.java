package nars.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import nars.io.TextInput;

/**
 * Creates a PrintWriter that pipes into a TextInput's BufferedReader.
 * Useful for TextInput implementations that dynamically generate new input from Java execution.
 */ 
public class PrintWriterInput extends TextInput {
    
    /** Printing to out will be piped into TextInput */
    public final PipedWriter out;
    boolean outClosed = false;

    public PrintWriterInput() throws IOException  {
        super();
        
        
        
        out = new PipedWriter();        
        setInput(new BufferedReader(new PipedReader(out)));
        

    }

    @Override
    public boolean finished(boolean forceStop) {
        if (outClosed) return true;
        return super.finished(forceStop);
    }
    
    
    
    public void append(CharSequence c) {
        try {
            out.append(c);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            out.close();
            outClosed = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
