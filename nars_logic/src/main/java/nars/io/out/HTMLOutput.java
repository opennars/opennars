package nars.io.out;

import nars.Events;
import nars.NAR;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.truth.Truth;
import nars.nal.concept.Concept;
import nars.op.io.Echo;

/**
 NOT FULLY IMPLEMENTED AND TESTED YET
 */
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

    protected static StringBuilder getTruthHTML(Truth truth) {
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
