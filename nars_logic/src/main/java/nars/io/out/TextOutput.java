
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

import nars.NAR;

import java.io.PrintStream;

/**
 * To read and write Task stream experiences in a Text (string) representation,
 * for print, display, and other serialization purposes.
 */
public class TextOutput {

//    private final NAR nar;
//
//    private final PrintWriter out;
//    private boolean showErrors = true;
//    //privte boolean showStackTrace = false;
//    private boolean showStamp = true;
//    private boolean showInput = true;
//    private boolean showOutput = true;
//
//    private float outputPriorityMin = 0;
//    protected boolean enabled = true;
//
//    boolean flushAfterEachOutput = true;
//
//
//    public static TextOutput out(NAR n) {
//        return new TextOutput(n, System.out);
//    }
//
//    public static TextOutput err(NAR n) {
//        return new TextOutput(n, System.err);
//    }
//
//
//    /**
//     * Default constructor; adds the reasoner to a NAR's outptu channels
//     *
//     * @param n
//     */
//    public TextOutput(NAR n, PrintWriter out) {
//        this(n, out, 0);
//    }
//    public TextOutput(NAR n) {
//        this(n, (PrintWriter)null);
//    }




//    public TextOutput(NAR n, PrintWriter outExp, float outputPriorityMin) {
//        super(n);
//        this.nar = n;
//        channel.put(Events.IN.class, new TaskChannel("IN"));
//        channel.put(Events.OUT.class, new TaskChannel("OUT"));
//        channel.put(Events.Answer.class, new TaskChannel("ANS"));
//        channel.put(echo.class, new TaskChannel("ECH"));
//        channel.put(Events.EXE.class, new TaskChannel("EXE"));
//
//        this.out = outExp;
//
//        this.outputPriorityMin = outputPriorityMin;
//    }
//
//    public TextOutput(NAR n, PrintStream ps) {
//        this(n, new PrintWriter(ps));
//    }
//
//
//    public TextOutput(NAR n, StringWriter s) {
//        this(n, new PrintWriter(s));
//    }
//
//    /**
//     * Open an output experience file
//     */
//    public static TextOutput openOutputFile(NAR n, String path) {
//        try {
//            return new TextOutput(n, new PrintWriter(new FileWriter(path)));
//        } catch (IOException ex) {
//            System.out.println("i/o error: " + ex.getMessage());
//        }
//        return null;
//    }
//
//    /**
//     * Close an output experience file
//     */
//    public void closeSaveFile() {
//        out.close();
//        stop();
//    }
//


//    @Override
//    protected boolean output(final Channel channel, final Class event, final Object... args) {
//
//        if (out == null || !isEnabled())
//            return false;
//
//        if (!showInput && event == Events.IN.class) return false;
//        if (!showOutput && event == Events.OUT.class) return false;
//
//        final String prefix = channel.getLinePrefix(event, args);
//        final CharSequence s = channel.get(event, args);
//
//        if (s != null) {
//            return output(prefix, s);
//        }
//
//
//        return false;
//    }

    protected static boolean output(PrintStream out, final String prefix, final CharSequence s) {
        if ((prefix == null) && (s == null))
            return false;


                if (prefix != null)
                    out.print(prefix);

                out.append(": ");

                if (s != null)
                    out.println(s);

                //if (flushAfterEachOutput)
                out.flush();


            return true;
    }

    public static void out(NAR nar) {
        //TODO System.err.println("TextOutput.out impl in progress");
        nar.stdout();
    }


//    static final ThreadLocal<StringBuilder> buffers = new ThreadLocal();
//
//    public class TaskChannel extends DefaultChannel {
//
//        public TaskChannel(String prefix) {
//            super(prefix);
//        }
//
//        @Override
//        public StringBuilder get(Class c, Object[] o) {
//
//
//            if (o[0] instanceof Task) {
//                Task tt = (Task)o[0];
//                if (!allowTask(tt))
//                    return null;
//            }
//
//            StringBuilder buffer = buffers.get();
//            if (buffer==null) {
//                buffers.set( buffer = new StringBuilder() );
//            }
//            else {
//                buffer.setLength(0);
//            }
//
//            return TextOutput.append(buffer, c, o, false, showStamp, outputPriorityMin, nar);
//        }
//    }

//    /**
//     * may be overridden in subclass to filter certain tasks
//     */
//    protected boolean allowTask(Task t) {
//        return true;
//    }
//
//    public TextOutput setShowStamp(boolean showStamp) {
//        this.showStamp = showStamp;
//        return this;
//    }
//
//    public TextOutput setShowErrors(boolean errors) {
//        this.showErrors = errors;
//        return this;
//    }
//
//    public TextOutput setShowInput(boolean showInput) {
//        this.showInput = showInput;
//        return this;
//    }
//
//
////    public TextOutput setErrorStackTrace(boolean b) {
////        this.showStackTrace = true;
////        return this;
////    }
//
//
//    public boolean isEnabled() {
//        return enabled;
//    }
//
//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//    }
//
//    public void setOutputPriorityMin(float minPriority) {
//        this.outputPriorityMin = minPriority;
//    }


//    public static Appendable append(
//            final Appendable out, final Class channel, Object signalOrSignals,
//            CharSequence delimeter,
//            final boolean showChannel, final boolean showStamp,
//            float outputMinPriority, final NAR nar) throws IOException {
//
//        StringBuilder buf = new StringBuilder();
//        append(buf, channel, signalOrSignals, showChannel, showStamp, outputMinPriority, nar);
//        out.append(buf);
//        if (delimeter!=null)
//            out.append(delimeter);
//
//        return out;
//    }


//    /**
//     * generates a human-readable string from an output channel and signals
//     */
//    public static StringBuilder append(
//            final StringBuilder buffer, final Class channel, Object signalOrSignals,
//            final boolean showChannel, final boolean showStamp,
//            float outputMinPriority, final NAR nar) {
//
//
//        Object signal;
//        Object[] signals;
//        if (signalOrSignals instanceof Object[]) {
//            signals = (Object[]) signalOrSignals;
//            signal = signals[0];
//        } else if (signalOrSignals instanceof CharSequence) {
//            return buffer.append((CharSequence)signalOrSignals);
//        } else {
//            signal = signalOrSignals;
//            signals = null;
//        }
//
//        if (showChannel) {
//            buffer.append(channel.getSimpleName()).append(": ");
//        }
//
//        if ((channel == Answer.class) && (signals != null)) {
//            Task question = (Task) signals[1];
//            Task answer = (Task) signals[0];
//            question.toString(buffer, nar.memory, showStamp).append(" =\n                        ");
//            answer.toString(buffer, nar.memory, true /*!question.getTerm().equals(answer.getTerm()) */, showStamp, false);
//
//        } else if ((signal instanceof Task) && ((channel == Events.OUT.class) || (channel == Events.IN.class) || (channel == echo.class) || (channel == Events.EXE.class))) {
//
//
//            Task t = (Task) signal;
//            if (t != null) {
//                if (channel == Events.OUT.class && t.getPriority() < outputMinPriority)
//                    return null;
//
//                t.toString(buffer, nar.memory, showStamp);
//            } else {
//                buffer.append(t.toString());
//            }
//
//
//                /*
//                Task root = t.getRootTask();
//                if (root!=null)
//                    buffer.append(" {{").append(root.sentence).append("}}");
//                */
////            }
////            else if (signals instanceof Sentence) {
////                Sentence s = (Sentence)signals;
////                buffer.append(s.toString(nar, showStamp));
//
//
//        } else {
//            buffer.append(Texts.arrayToString(signals));
//        }
//
//        return buffer;
//    }
//
//    /*
//    public static CharSequence getOutputString(Class channel, Object signal, boolean showChannel, boolean showStamp, NAR nar) {
//        CharSequence s = getOutputString(channel, signal, showStamp, nar);
//        if (showChannel) {
//            String channelName = channel.getSimpleName();
//            StringBuilder r = new StringBuilder(s.length() + 2 + channelName.length());
//            return r.append(channel.getSimpleName()).append(": ").append(s);
//        } else {
//            return s;
//        }
//    }*/
//
//    public static StringBuilder append(Class channel, Object signal, final boolean showStamp, final NAR nar, final StringBuilder buffer) {
//        return append(buffer, channel, signal, false, showStamp, 0, nar);
//    }




//    public void stop() {
//        setActive(false);
//    }


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

