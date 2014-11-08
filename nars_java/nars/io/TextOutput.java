
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
import nars.core.Events.Solved;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output.ECHO;
import nars.io.Output.ERR;
import nars.io.Output.EXE;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.language.Statement;
import nars.operator.Operator;

/**
 * To read and write experience as Task streams
 */
public class TextOutput extends Output {


    private final NAR nar;
    
    private String prefix = "";
    private LineOutput outExp2;
    private PrintWriter outExp;
    private boolean showErrors = true;
    private boolean showStackTrace = false;
    private boolean showStamp = true;
    private boolean showInput = true;
    private float minPriority = 0;

    public interface LineOutput {
        public void println(String s);
    }

    
    /**
     * Default constructor; adds the reasoner to a NAR's outptu channels
     *
     * @param n
     */
    public TextOutput(NAR n) {
        super(n, true);
        this.nar = n;
    }
    public TextOutput(NAR n, LineOutput outExp2) {
        this(n);
        this.outExp2 = outExp2;
    }

    public TextOutput(NAR n, PrintWriter outExp) {
        this(n, outExp, 0.0f);
    }
    public TextOutput(NAR n, PrintWriter outExp, float minPriority) {
        this(n);
        this.outExp = outExp;
        this.minPriority = minPriority;
    }
    public TextOutput(NAR n, PrintStream ps) {
        this(n, new PrintWriter(ps));
    }
    public TextOutput(NAR n, PrintStream ps, float minPriority) {
        this(n, ps);
        this.minPriority = minPriority;
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
        stop();
    }

    /**
     * Process the next chunk of output data
     *
     * @param lines The text to be displayed
     */
    @Override
    public void event(final Class channel, final Object... oo) {
        if (!showErrors && (channel == ERR.class))
            return;
        
        if (!showInput && (channel == IN.class))
            return;
        
        if ((outExp!=null) || (outExp2!=null)) {
            Object o = oo[0];
            final String s = process(channel, o);
            if (s!=null) {
                if (outExp != null) {
                    outExp.println(prefix + s);
                    outExp.flush();
                }
                if (outExp2 != null) {
                    outExp2.println(prefix + s);            
                }
            }
        }
    }
    
    final StringBuilder result = new StringBuilder(16 /* estimate */);
    
    public String process(final Class c, final Object o) {
        return getOutputString(c, o, true, showStamp, nar, result, minPriority);
    }

    public TextOutput setErrors(boolean errors) {
        this.showErrors = errors;
        return this;
    }    

    public TextOutput setShowInput(boolean showInput) {
        this.showInput = showInput;
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

    public static String getOutputString(final Class channel, Object signal, final boolean showChannel, final boolean showStamp, final NAR nar, final StringBuilder buffer) {
        return getOutputString(channel, signal, showChannel, showStamp, nar, buffer, 0);
    }
            
    /** generates a human-readable string from an output channel and signal */
    public static String getOutputString(final Class channel, Object signal, final boolean showChannel, final boolean showStamp, final NAR nar, final StringBuilder buffer, float minPriority) {
        buffer.setLength(0);
        
        if (showChannel)
            buffer.append(channel.getSimpleName()).append(": ");        
        
        if (channel == ERR.class) {
            if (signal instanceof Throwable) {
                Throwable e = (Throwable)signal;

                buffer.append(e.toString());

                /*if (showStackTrace)*/ {
                    //buffer.append(" ").append(Arrays.asList(e.getStackTrace()));
                }
            }
            else {
                buffer.append(signal.toString());
            }      
            
        }        
        else if ((channel == OUT.class) || (channel == IN.class) || (channel == ECHO.class) || (channel == EXE.class) || (channel == Solved.class))  {



            
            if (signal instanceof Task) {
                Task t = (Task)signal;                
                if (t.getPriority() < minPriority)
                    return null;
                
                buffer.append(t.sentence.toString(nar, showStamp));                    
                
                
                /*
                Task root = t.getRootTask();
                if (root!=null)
                    buffer.append(" {{").append(root.sentence).append("}}");
                */
//            }            
//            else if (signal instanceof Sentence) {
//                Sentence s = (Sentence)signal;                
//                buffer.append(s.toString(nar, showStamp));                        
            } else {
                buffer.append(signal.toString());
            }
            
        }
        else {
            buffer.append(signal.toString());
        }
        
        return Texts.unescape(buffer).toString();
        
    }
    
    public static CharSequence getOutputString(Class channel, Object signal, boolean showChannel, boolean showStamp, NAR nar) {
        CharSequence s = getOutputString(channel, signal, showStamp, nar);
        if (showChannel) {            
            String channelName = channel.getSimpleName();
            StringBuilder r = new StringBuilder(s.length() + 2 + channelName.length());
            return r.append(channel.getSimpleName()).append(": ").append(s);
        }
        else {
            return s;
        }
    }

    public static CharSequence getOutputString(Class channel, Object signal, final boolean showStamp, final NAR nar) {
        return getOutputString(channel, signal, showStamp, nar, new StringBuilder());
    }
    
    /** generates a human-readable string from an output channel and signal */
    public static CharSequence getOutputString(Class channel, Object signal, final boolean showStamp, final NAR nar, final StringBuilder buffer) {
        buffer.setLength(0);
        
        if (signal instanceof Exception) {
            Exception e = (Exception)signal;

            buffer.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());

            /*if (showStackTrace)*/ 
            {
                buffer.append(" ").append(Arrays.asList(e.getStackTrace()));
            }
        }
        else if (signal instanceof Task) {
            Task t = (Task)signal;
            
            Sentence s = t.sentence;

            buffer.append(s.toString(nar, showStamp));
        }            
        else if (signal instanceof Sentence) {
            Sentence s = (Sentence)signal;                
            buffer.append(s.toString(nar, showStamp));                        
        }                    
        else if (signal instanceof Object[]) {
            if (channel == Solved.class) {
                Object[] o = (Object[])signal;
                Task task = (Task)o[0];
                Sentence belief = (Sentence)o[1];
                
                Sentence question = task.sentence;
                Sentence answer = belief;
                
                buffer.append(answer.toString(nar, false));
            }
            else            
                buffer.append( Arrays.toString((Object[])signal) );
        }
        else {
            buffer.append(signal.toString());
        }
        
        return Texts.unescape(buffer);
        
    }
    
    
    public void stop() {
        setActive(false);
    }
    
    /** generates a human-readable string from an output channel and signal */
    public static String getOutputHTML(final Class channel, Object signal, final boolean showChannel, final boolean showStamp, final NAR nar) {
        final StringBuilder buffer = new StringBuilder();
        
        
        if (channel == OUT.class) {
            buffer.append("<div style='clear: both; float:left'>OUT:</div>");
            if (signal instanceof Task) {
                Task t = (Task)signal;
                Sentence s = t.getBestSolution();
                if (s == null)
                    s = t.sentence;

                if (s.truth!=null) {
                    buffer.append(getTruthHTML(s.truth));
                }
                buffer.append("<div style='float:left'><pre>").append(escapeHTML(s.toString(nar, showStamp))).append("</pre></div>");
                buffer.append("<br/>");
                /*
                Task root = t.getRootTask();
                if (root!=null)
                    buffer.append(" {{").append(root.sentence).append("}}");
                */
            }            
            else if (signal instanceof Sentence) {
                Sentence s = (Sentence)signal;                
                buffer.append(s.toString(nar, showStamp));                        
            } else {
                buffer.append(signal.toString());
            }
            return Texts.unescape(buffer).toString();
        }

        if (showChannel)
            buffer.append(channel.getSimpleName()).append(": ");        
        
        if (channel == ERR.class) {
            if (signal instanceof Exception) {
                Exception e = (Exception)signal;

                buffer.append(e.toString());

                /*if (showStackTrace)*/
                
                /*for (int i = 0; i < )
                    buffer.append(" ").append(Arrays.asList(e.getStackTrace() ));
                }*/
            }
            else {
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
        
        return "<div style='clear: both'>" + escapeHTML(Texts.unescape(buffer).toString()) + "</div>";
        
    }    

    protected static StringBuilder getTruthHTML(TruthValue truth) {
        StringBuilder b = new StringBuilder();
        
        //http://css-tricks.com/html5-meter-element/
        
        //b.append("<meter value='" + truth.getFrequency() + "' min='0' max='1.0'></meter>");
        //b.append("<meter value='" + truth.getConfidence()+ "' min='0' max='1.0' style='background: none'></meter>");
        b.append(freqColor(truth.getFrequency(), "4em"));
        b.append(meter(truth.getConfidence(), "4em", "#00F"));
            
        return b;
    }
    
    public static String freqColor(float value, String width) {
        String pct = ((int)(Math.round(value * 100.0))) + "%";
        final String background = "rgba(255,255,255,0.15)";
        String foreground  = value < 0.5 ? 
                "rgba(" + (int)(255*(0.5 - value)*2) + ",0,0," + (0.5 - value)*2 + ")" : 
                "rgba(0," + (int)(255*(value - 0.5)*2) + ",0," + (value-0.5)*2 + ")";
                
        return "<div style='float:left;width: " + width + ";padding:2px;'>" +
          "<div style='width:100%;background-color:" + foreground + ";text-align:center;'>" +
            "<span>" + pct + "</span></div></div>";
    }
    
    public static String meter(float value, String width, String foreground) {
        String pct = ((int)(Math.round(value * 100.0))) + "%";
        final String background = "rgba(255,255,255,0.15)";
        return "<div style='float:left;width: " + width + ";padding:2px;background:" + background + ";'>" +
          "<div style='width:" + pct + ";background:" + foreground + ";text-align:center;'>" +
            "<span>" + pct + "</span></div></div>";
        
    }
    
    /**
     * @author http://www.rgagnon.com/javadetails/java-0306.html
     */
    public static String escapeHTML(CharSequence string) {
        StringBuilder sb = new StringBuilder(string.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++) {
            c = string.charAt(i);
            if (c == ' ') {
            // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss 
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                } else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            } else {
                lastWasBlankChar = false;
            //
                // HTML Special Chars
                if (c == '"') {
                    sb.append("&quot;");
                }
                else if (c == '\'') {
                    sb.append("&#39;");
                } else if (c == '&') {
                    sb.append("&amp;");
                } else if (c == '<') {
                    sb.append("&lt;");
                } else if (c == '>') {
                    sb.append("&gt;");
                } else if (c == '\n') { // Handle Newline
                    //sb.append("&lt;br/&gt;");
                } else {
                    int ci = 0xffff & c;
                    if (ci < 160) // nothing special only 7 Bit
                    {
                        sb.append(c);
                    } else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(Integer.toString(ci));
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }
    
    public static CharSequence summarize( Iterable<? extends Concept> concepts) {
        StringBuilder s = new StringBuilder();        
        for (Concept c : concepts) {
            s.append(c.toString() + "\n");
        }
        return s;
    }

    
}
