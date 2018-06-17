/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.io.events;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.Texts;
import org.opennars.io.events.Events.Answer;
import org.opennars.main.Nar;

import java.io.*;
import java.util.Arrays;

/**
 * To read and write experience as Task streams
 */
public class TextOutputHandler extends OutputHandler implements Serializable {


    private final Nar nar;
    
    private String prefix = "";
    private LineOutput outExp2;
    private PrintWriter outExp;
    private boolean showErrors = true;
    private boolean showStackTrace = false;
    private final boolean showStamp = true;
    private boolean showInput = true;
    private float minPriority = 0;

    public interface LineOutput {
        void println(String s);
    }

    /**
     * Default constructor; adds the reasoner to a Nar's outptu channels
     *
     * @param n
     */
    public TextOutputHandler(final Nar n) {
        super(n, true);
        this.nar = n;
    }
    public TextOutputHandler(final Nar n, final LineOutput outExp2) {
        this(n);
        this.outExp2 = outExp2;
    }

    public TextOutputHandler(final Nar n, final PrintWriter outExp) {
        this(n, outExp, 0.0f);
    }
    public TextOutputHandler(final Nar n, final PrintWriter outExp, final float minPriority) {
        this(n);
        this.outExp = outExp;
        this.minPriority = minPriority;
    }
    public TextOutputHandler(final Nar n, final PrintStream ps) {
        this(n, new PrintWriter(ps));
    }
    public TextOutputHandler(final Nar n, final PrintStream ps, final float minPriority) {
        this(n, ps);
        this.minPriority = minPriority;
    }
    public TextOutputHandler(final Nar n, final StringWriter s) {
        this(n, new PrintWriter(s));
    }
    /**
     * Open an output experience file
     */
    public void openSaveFile(final String path) {
        try {
            outExp = new PrintWriter(new FileWriter(path));
        } catch (final IOException ex) {
            throw new IllegalStateException("Could not open save file.", ex);
        }
    }

    /**
     * Close an output experience file
     */
    public void closeSaveFile() {
        outExp.close();
        setActive(false);
    }

    /**
     * Process the next chunk of output data
     *
     */
    @Override
    public void event(final Class channel, final Object... oo) {
        if (!showErrors && (channel == ERR.class))
            return;
        
        if (!showInput && (channel == IN.class))
            return;
        
        if ((outExp!=null) || (outExp2!=null)) {
            final Object o = oo[0];
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

    public TextOutputHandler setErrors(final boolean errors) {
        this.showErrors = errors;
        return this;
    }    

    public TextOutputHandler setShowInput(final boolean showInput) {
        this.showInput = showInput;
        return this;
    }
    
    public TextOutputHandler setErrorStackTrace(final boolean b) {
        this.showStackTrace = true;
        return this;
    }

    public TextOutputHandler setLinePrefix(final String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static String getOutputString(final Class channel, final Object signal, final boolean showChannel, final boolean showStamp, final Nar nar, final StringBuilder buffer) {
        return getOutputString(channel, signal, showChannel, showStamp, nar, buffer, 0);
    }
            
    /** generates a human-readable string from an output channel and signal */
    public static String getOutputString(final Class channel, final Object signal, final boolean showChannel, final boolean showStamp, final Nar nar, final StringBuilder buffer, final float minPriority) {
        buffer.setLength(0);
        
        if (showChannel)
            buffer.append(channel.getSimpleName()).append(": ");        
        
        if (channel == ERR.class) {
            if (signal instanceof Throwable) {
                final Throwable e = (Throwable)signal;

                buffer.append(e.toString());

                /*if (showStackTrace)*/ {
                    //buffer.append(" ").append(Arrays.asList(e.getStackTrace()));
                }
            }
            else {
                buffer.append(signal.toString());
            }      
            
        }        
        else if ((channel == OUT.class) || (channel == IN.class) || (channel == ECHO.class) || (channel == EXE.class) || (channel == Answer.class)
                || (channel == ANTICIPATE.class) || (channel == DISAPPOINT.class) || (channel == CONFIRM.class))  {

            if(channel == CONFIRM.class) {
                buffer.append(signal.toString());  
            }
            if (signal instanceof Task) {
                final Task t = (Task)signal;
                if (t.getPriority() < minPriority)
                    return null;
                
                if((channel == ANTICIPATE.class) || (channel == DISAPPOINT.class)) {
                    buffer.append(t.sentence.toString(nar, showStamp));  
                }
                else
                if (channel == Answer.class) {
                    final Task task = t; //server / NARRun
                    final Sentence answer = task.getBestSolution();
                    if(answer!=null)
                        buffer.append(answer.toString(nar, showStamp));
                    else
                        buffer.append(t.sentence.toString(nar, showStamp));  
                }
                else            
                    buffer.append(t.sentence.toString(nar, showStamp));         
            } else {
                buffer.append(signal.toString());
            }
            
        }
        else {
            buffer.append(signal.toString());
        }
        
        return Texts.unescape(buffer).toString();
        
    }
    
    public static CharSequence getOutputString(final Class channel, final Object signal, final boolean showChannel, final boolean showStamp, final Nar nar) {
        final CharSequence s = getOutputString(channel, signal, showStamp, nar);
        if (showChannel) {            
            final String channelName = channel.getSimpleName();
            final StringBuilder r = new StringBuilder(s.length() + 2 + channelName.length());
            return r.append(channel.getSimpleName()).append(": ").append(s);
        }
        else {
            return s;
        }
    }

    public static CharSequence getOutputString(final Class channel, final Object signal, final boolean showStamp, final Nar nar) {
        return getOutputString(channel, signal, showStamp, nar, new StringBuilder());
    }
    
    /** generates a human-readable string from an output channel and signal */
    public static CharSequence getOutputString(final Class channel, final Object signal, final boolean showStamp, final Nar nar, final StringBuilder buffer) {
        buffer.setLength(0);
        
        if (signal instanceof Exception) {
            final Exception e = (Exception)signal;

            buffer.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());

            /*if (showStackTrace)*/ 
            {
                buffer.append(" ").append(Arrays.asList(e.getStackTrace()));
            }
        }
        else if (signal instanceof Task) {
            final Task t = (Task)signal;
            
            final Sentence s = t.sentence;

            buffer.append(s.toString(nar, showStamp));
            
        }            
        else if (signal instanceof Sentence) {
            final Sentence s = (Sentence)signal;
            buffer.append(s.toString(nar, showStamp));                        
        }                    
        else if (signal instanceof Object[]) {
            if (channel == Answer.class) {
                final Object[] o = (Object[])signal;
                final Task task = (Task)o[0];
                final Sentence belief = (Sentence)o[1];
                
                final Sentence question = task.sentence;
                final Sentence answer = belief;
                
                buffer.append(answer.toString(nar, showStamp));
            }
            else            
                buffer.append( Arrays.toString((Object[])signal) );
        }
        else {
            buffer.append(signal.toString());
        }
        
        return Texts.unescape(buffer);
        
    }
}
