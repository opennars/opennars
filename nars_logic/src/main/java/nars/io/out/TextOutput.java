
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
package nars.io.out;

import nars.Events;
import nars.Events.Answer;
import nars.NAR;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.op.io.Echo;

import java.io.*;
import java.util.Arrays;

/**
 * To read and write Task stream experiences in a Text (string) representation,
 * for print, display, and other serialization purposes.
 */
public class TextOutput extends Output {

    private final NAR nar;

    private PrintWriter out;
    private boolean showErrors = true;
    //privte boolean showStackTrace = false;
    private boolean showStamp = true;
    private boolean showInput = true;
    private float outputPriorityMin = 0;
    protected boolean enabled = true;



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
        channel.put(Events.IN.class, new TaskChannel("IN"));
        channel.put(Events.OUT.class, new TaskChannel("OUT"));
        channel.put(Events.Answer.class, new TaskChannel("ANS"));
        channel.put(Echo.class, new TaskChannel("ECH"));
        channel.put(Events.EXE.class, new TaskChannel("EXE"));
    }


    public TextOutput(NAR n, PrintWriter outExp) {
        this(n, outExp, 0.0f);
    }

    public TextOutput(NAR n, PrintWriter outExp, float outputPriorityMin) {
        this(n);
        this.out = outExp;
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
            out = new PrintWriter(new FileWriter(path));
        } catch (IOException ex) {
            System.out.println("i/o error: " + ex.getMessage());
        }
    }

    /**
     * Close an output experience file
     */
    public void closeSaveFile() {
        out.close();
        stop();
    }



    @Override
    protected synchronized void output(final Channel channel, final Class event, final Object... args) {

        if (!isEnabled())
            return;

        final String prefix = channel.getLinePrefix(event, args);
        final CharSequence s = channel.get(event, args);

        if (s != null) {
            output(prefix, s);
        }

    }

    protected void output(final String prefix, final CharSequence s) {
        if (out != null) {
            if (prefix != null)
                out.print(prefix);

            out.append(": ");

            if (s != null)
                out.println(s);

            out.flush();
        }
    }

    public class TaskChannel extends DefaultChannel {

        public TaskChannel(String prefix) {
            super(prefix);
        }

        final StringBuilder buffer = new StringBuilder();

        @Override
        public synchronized CharSequence get(Class c, Object[] o) {
            if (o[0] instanceof Task) {
                Task tt = (Task)o[0];
                if (!allowTask(tt))
                    return null;
            }

            return TextOutput.getOutputString(c, o, false, showStamp, nar, buffer, outputPriorityMin);
        }
    }

    /**
     * may be overridden in subclass to filter certain tasks
     */
    protected boolean allowTask(Task t) {
        return true;
    }

    public TextOutput setShowStamp(boolean showStamp) {
        this.showStamp = showStamp;
        return this;
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


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

        if ((channel == Answer.class) && (signals != null)) {
            Task question = (Task) signals[1];
            Task answer = (Task) signals[0];
            question.sentence.toString(buffer, nar.memory, showStamp).append(" = ");
            answer.sentence.toString(buffer, nar.memory, false, showStamp);

        } else if ((signal instanceof Task) && ((channel == Events.OUT.class) || (channel == Events.IN.class) || (channel == Echo.class) || (channel == Events.EXE.class))) {


            Task t = (Task) signal;
            if (t.sentence != null) {
                if (channel == Events.OUT.class && t.getPriority() < outputMinPriority)
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

