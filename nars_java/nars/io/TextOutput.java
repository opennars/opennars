
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.language.Statement;
import nars.operator.Operator;

/**
 * To read and write experience as Task streams
 */
public class TextOutput implements Output {

    private final NAR nar;
    
    private String prefix = "";
    private LineOutput outExp2;
    private PrintWriter outExp;
    private boolean showErrors = true;
    private boolean showStackTrace;
    private boolean showStamp = true;


    public interface LineOutput {
        public void println(String s);
    }

    
    /**
     * Default constructor; adds the reasoner to a NAR's outptu channels
     *
     * @param n
     */
    public TextOutput(NAR n) {
        this.nar = n;
        n.addOutput(this);
    }
    public TextOutput(NAR n, LineOutput outExp2) {
        this(n);
        this.outExp2 = outExp2;
    }

    public TextOutput(NAR n, PrintWriter outExp) {
        this(n);
        this.outExp = outExp;
    }
    public TextOutput(NAR n, PrintStream ps) {
        this(n, new PrintWriter(ps));
    }
    public TextOutput(NAR n, StringWriter s) {
        this(n, new PrintWriter(s));
    }
    /**
     * Open an output experience file
     */
    public void openSaveFile(String path) {
        try {
            outExp = new PrintWriter(new FileWriter(path));
        } catch (IOException ex) {
            System.out.println("i/o error: " + ex.getMessage());
        }
    }

    /**
     * Close an output experience file
     */
    public void closeSaveFile() {
        outExp.close();
        nar.removeOutput(this);
    }

    /**
     * Process the next chunk of output data
     *
     * @param lines The text to be displayed
     */
    @Override
    public synchronized void output(final Class channel, final Object o) {
        if ((!showErrors) && (channel == ERR.class))
            return;
        
        
        if ((outExp!=null) || (outExp2!=null)) {
            final String s = process(channel, o);
            if (outExp != null) {
                outExp.println(prefix + s);
                outExp.flush();
            }
            if (outExp2 != null) {
                outExp2.println(prefix + s);            
            }
        }
    }
    
    final StringBuilder result = new StringBuilder(16 /* estimate */);
    
    public String process(final Class c, final Object o) {
        return getOutputString(c, o, true, showStamp, nar, result);
    }

    public TextOutput setErrors(boolean errors) {
        this.showErrors = errors;
        return this;
    }    
    
    public TextOutput setErrorStackTrace(boolean b) {
        this.showStackTrace = true;
        return this;
    }

    public TextOutput setLinePrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /** generates a human-readable string from an output channel and signal */
    public static String getOutputString(Class channel, Object signal, boolean showChannel, boolean showStamp, NAR nar, StringBuilder buffer) {
        buffer.setLength(0);
        
        if (showChannel)
            buffer.append(channel.getSimpleName()).append(": ");        
        
        if (channel == ERR.class) {
            if (signal instanceof Exception) {
                Exception e = (Exception)signal;

                buffer.append(e.toString());

                /*if (showStackTrace)*/ {
                    buffer.append(" ").append(Arrays.asList(e.getStackTrace()));
                }
            }
            else {
                buffer.append(signal.toString());
            }                            
        }        
        else if (channel == OUT.class) {
            if (signal instanceof Sentence) {
                Sentence s = (Sentence)signal;                
                buffer.append(s.toString(nar, showStamp));
                        
            } else {
                buffer.append(signal.toString());
            }
            
        }
        else if ((channel == IN.class) || (channel == ECHO.class)) {
            buffer.append(signal.toString());
        }
        else if (channel == EXE.class) {
            if (signal instanceof Statement)
                buffer.append(Operator.operationExecutionString((Statement)signal));
            else {
                buffer.append(signal.toString());
            }
        }
        else {
            buffer.append(signal.toString());
        }
        
        return Texts.unescape(buffer).toString();
        
    }
    
    public static String getOutputString(Class channel, Object signal, boolean showChannel, boolean showStamp, NAR nar) {
        return getOutputString(channel, signal, showChannel, showStamp, nar, new StringBuilder());
    }
    
    public void stop() {
        nar.removeOutput(this);
    }
    
    
}
