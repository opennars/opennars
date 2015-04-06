
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

import nars.Events;
import nars.Events.Answer;
import nars.NAR;
import nars.nal.Concept;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.operate.io.Echo;

import java.io.*;
import java.util.Arrays;

/**
 * To read and write experience as Task streams
 */
public class TextOutput extends Output {


    private final NAR nar;

    private String prefix = "";
    private LineOutput outExp2;
    private PrintWriter outExp;
    private boolean showErrors = true;
    //privte boolean showStackTrace = false;
    private boolean showStamp = true;
    private boolean showInput = true;
    private float outputPriorityMin = 0;

    public interface LineOutput {
        public void println(CharSequence s);
    }

    public static TextOutput out(NAR n) {
        return new TextOutput(n, System.out);
    }

    public static TextOutput err(NAR n) {
        return new TextOutput(n, System.err);
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

    public TextOutput(NAR n, PrintWriter outExp, float outputPriorityMin) {
        this(n);
        this.outExp = outExp;
        this.outputPriorityMin = outputPriorityMin;
    }

    public TextOutput(NAR n, PrintStream ps) {
        this(n, new PrintWriter(ps));
    }

    public TextOutput(NAR n, PrintStream ps, float outputPriorityMin) {
        this(n, ps);
        this.outputPriorityMin = outputPriorityMin;
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

    final StringBuilder buffer = new StringBuilder();

    /**
     * Process the next chunk of output data
     */
    @Override
    public void event(final Class channel, final Object... oo) {
        if (!showErrors && (channel == Events.ERR.class))
            return;

        if (!showInput && (channel == Events.IN.class))
            return;
        
        if ((outExp!=null) && (outExp2!=null)) {
            throw new RuntimeException("why does this TextOuput exist?");
        }

        synchronized (buffer) {
            final CharSequence s = process(channel, oo);
            if (s != null) {
                if (outExp != null) {
                    if (prefix != null)
                        outExp.print(prefix);
                    outExp.println(s);
                    outExp.flush();
                }
                if (outExp2 != null) {
                    if (prefix != null)
                        outExp2.println(prefix + s);
                    else
                        outExp2.println(s);
                }
            }
        }
    }


    public CharSequence process(final Class c, final Object[] o) {
        if (o[0] instanceof Task) {
            if (!allowTask((Task) o[0]))
                return null;
        }
        return getOutputString(c, o, true, showStamp, nar, buffer, outputPriorityMin);
    }

    /**
     * may be overridden in subclass to filter certain tasks
     */
    protected boolean allowTask(Task t) {
        return true;
    }

    public void setShowStamp(boolean showStamp) {
        this.showStamp = showStamp;
    }

    public TextOutput setShowErrors(boolean errors) {
        this.showErrors = errors;
        return this;
    }

    public TextOutput setShowInput(boolean showInput) {
        this.showInput = showInput;
        return this;
    }


//    public TextOutput setErrorStackTrace(boolean b) {
//        this.showStackTrace = true;
//        return this;
//    }

    public TextOutput setLinePrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public void setOutputPriorityMin(float minPriority) {
        this.outputPriorityMin = minPriority;
    }

    /**
     * generates a human-readable string from an output channel and signals
     */
    public static StringBuilder getOutputString(final Class channel, Object signalOrSignals, final boolean showChannel, final boolean showStamp, final NAR nar, final StringBuilder buffer, float outputMinPriority) {
        buffer.setLength(0);


        Object signal;
        Object[] signals;
        if (signalOrSignals instanceof Object[]) {
            signals = (Object[]) signalOrSignals;
            signal = signals[0];
        } else {
            signal = signalOrSignals;
            signals = null;
        }

        if (showChannel) {
            buffer.append(channel.getSimpleName()).append(": ");
        }

        if (channel == Events.ERR.class) {

            if (signal instanceof Throwable) {
                Throwable e = (Throwable) signal;

                buffer.append(e.toString());

                /*if (showStackTrace)*/
                {
                    //buffer.append(" ").append(Arrays.asList(e.getStackTrace()));
                }
            } else {
                if (signals != null)
                    buffer.append(Arrays.toString(signals));
                else
                    buffer.append(signal);
            }

        } else if ((channel == Answer.class) && (signals != null)) {
            Task question = (Task) signals[0];
            Sentence answer = (Sentence) signals[1];
            question.sentence.toString(buffer, nar.memory, showStamp).append(" = ").append(answer.toString(nar, showStamp));

        } else if ((signal instanceof Task) && ((channel == Events.OUT.class) || (channel == Events.IN.class) || (channel == Echo.class) || (channel == Events.EXE.class))) {


            Task t = (Task) signal;
            if (t.budget != null && t.sentence != null) {
                if (channel == Events.OUT.class && t.getPriority() <= outputMinPriority)
                    return null;

                t.sentence.toString(buffer, nar.memory, showStamp);
            } else {
                buffer.append(t.toString());
            }
                
                
                /*
                Task root = t.getRootTask();
                if (root!=null)
                    buffer.append(" {{").append(root.sentence).append("}}");
                */
//            }            
//            else if (signals instanceof Sentence) {
//                Sentence s = (Sentence)signals;
//                buffer.append(s.toString(nar, showStamp));                        


        } else {
            if ((signals != null) && (signals.length > 1))
                buffer.append(Arrays.toString(signals));
            else
                buffer.append(signal.toString());
        }

        return buffer;
    }

    /*
    public static CharSequence getOutputString(Class channel, Object signal, boolean showChannel, boolean showStamp, NAR nar) {
        CharSequence s = getOutputString(channel, signal, showStamp, nar);
        if (showChannel) {
            String channelName = channel.getSimpleName();
            StringBuilder r = new StringBuilder(s.length() + 2 + channelName.length());
            return r.append(channel.getSimpleName()).append(": ").append(s);
        } else {
            return s;
        }
    }*/

    public static CharSequence getOutputString(Class channel, Object signal, final boolean showStamp, final NAR nar, final StringBuilder buffer) {
        return getOutputString(channel, signal, false, showStamp, nar, buffer, 0);
    }




    public void stop() {
        setActive(false);
    }


//    @Deprecated static CharSequence _getOutputString(Class channel, Object signal, final boolean showStamp, final NAR nar, final StringBuilder buffer) {
//        buffer.setLength(0);
//
//        if (signal instanceof Exception) {
//            Exception e = (Exception) signal;
//
//            buffer.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
//
//            /*if (showStackTrace)*/
//            {
//                buffer.append(' ').append(Arrays.asList(e.getStackTrace()));
//            }
//        } else if (signal instanceof Task) {
//            Task t = (Task) signal;
//
//            Sentence s = t.sentence;
//
//            if (s != null)
//                s.toString(buffer, nar.memory, showStamp);
//            else
//                buffer.append(t.toString());
//
//        } else if (signal instanceof Sentence) {
//            Sentence s = (Sentence) signal;
//            s.toString(buffer, nar.memory, showStamp);
//        } else if (signal instanceof Object[]) {
//            if (channel == Answer.class) {
//                Object[] o = (Object[]) signal;
//                //Task task = (Task)o[0];
//                Sentence belief = (Sentence) o[1];
//
//                //Sentence question = task.sentence;
//                Sentence answer = belief;
//
//                buffer.append(answer.toString(buffer, nar.memory, showStamp));
//            } else {
//                //TODO use repeat buffer.append(..) rather than Array.toString
//                buffer.append(Arrays.toString((Object[]) signal));
//            }
//        } else {
//            buffer.append(signal.toString());
//        }
//
//        return Texts.unescape(buffer);
//
//    }

}

//NOT IMPLEMENTED YET
class HTMLOutput {
    /** generates a human-readable string from an output channel and signal */
    public static CharSequence getOutputHTML(final Class channel, Object signal, final boolean showChannel, final boolean showStamp, final NAR nar) {
        final StringBuilder buffer = new StringBuilder();
        
        
        if (channel == Events.OUT.class) {
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
            return buffer;
        }

        if (showChannel)
            buffer.append(channel.getSimpleName()).append(": ");        
        
        if (channel == Events.ERR.class) {
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
        else if ((channel == Events.IN.class) || (channel == Echo.class)) {
            buffer.append(signal.toString());
        }
        else if (channel == Events.EXE.class) {
            /*if (signal instanceof Statement)
                buffer.append(Operator.operationExecutionString((Statement)signal));
            else {*/
                buffer.append(signal.toString());
            //}
        }
        else {
            buffer.append(signal.toString());
        }
        
        return "<div style='clear: both'>" + escapeHTML(buffer.toString()) + "</div>";
        
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
        //final String background = "rgba(255,255,255,0.15)";
        String foreground  = value < 0.5 ? 
                "rgba(" + (int)(255*(0.5 - value)*2) + ",0,0," + (0.5 - value)*2 + ')' :
                "rgba(0," + (int)(255*(value - 0.5)*2) + ",0," + (value-0.5)*2 + ')';
                
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

        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
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
            s.append(c.toString()).append('\n');
        }
        return s;
    }

    
}
