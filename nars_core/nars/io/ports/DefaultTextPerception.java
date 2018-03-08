package nars.io.ports;

import nars.io.commands.Echo;
import nars.io.commands.PauseInput;
import nars.io.commands.SetDecisionThreshold;
import nars.io.commands.SetVolume;
import nars.io.commands.Reset;
import nars.io.commands.Reboot;
import nars.language.Narsese.Narsese;
import nars.language.Narsese.Symbols;
import com.google.common.collect.Iterators;
import static com.google.common.collect.Iterators.singletonIterator;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.util.Events.Perceive;
import nars.storage.Memory;
import nars.NAR;
import nars.config.Parameters;
import nars.entity.Item;
import nars.util.Plugin;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.ports.Output.IN;
import nars.language.Narsese.Narsese.InvalidInputException;

/**
 *  Default handlers for text perception.
 *  Parses input text into sequences of AbstractTask's which input into 
 *      Memory via NAR input channel & buffer port.
 *  
 *  TODO break into separate subclasses for each text mode
 */
public class DefaultTextPerception implements Plugin, EventObserver, Serializable {
    
    private Memory memory;
    public List<TextReaction> parsers;    
    public Narsese narsese;    

    public interface TextReaction extends Serializable {
        public Object react(String input);
    }
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if (enabled) {
            this.memory = n.memory;
            this.narsese = new Narsese(memory);
            this.parsers = getParsers();
        }
        n.memory.event.set(this, enabled, Events.Perceive.class);
        return true;
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (event == Perceive.class) {            
            Object o = arguments[1];
            InPort i = (InPort)arguments[0];
            
            Iterator<Item> it = i.postprocess( perceive(o) ); 
            if (it!=null)
                while (it.hasNext())
                    i.queue(it.next());
        }
    }
    

    /* Perceive an input object by calling an appropriate perception system according to the object type. */
    public Iterator<? extends Item> perceive(final Object o) {
                
        Exception error;
        try {
            if (o instanceof String) {
                return perceive((String) o);
            } else if (o instanceof Sentence) {
                //TEMPORARY
                Sentence s = (Sentence) o;
                return perceive(s.term.toString() + s.punctuation + " " + s.truth.toString());
            } else if (o instanceof Task) {
                return Iterators.forArray((Task)o);
            }
            error = new IOException("Input unrecognized: " + o + " [" + o.getClass() + "]");
        }
        catch (Exception e) {
            if (Parameters.DEBUG)
                throw e;
            error = e;
        }
        
        return singletonIterator(new Echo(Output.ERR.class, error) );
    }
    
    public List<TextReaction> getParsers() {
   
        ArrayList<TextReaction> parsers = new ArrayList();
        
        //integer, # of cycles to step
        parsers.add(new TextReaction() {
            final String spref = Symbols.INPUT_LINE_PREFIX + ':';
            
            @Override public Object react(String input) {
                
                input = input.trim();
                if (input.startsWith(spref))
                    input = input.substring(spref.length());                    

                if (!Character.isDigit(input.charAt(0)))
                    return null;
                if (input.length() > 8) {
                    //if input > ~8 chars it wont fit as 32bit integer anyway so terminate early.
                    //parseInt is sort of expensive
                    return null;
                }
                    
                try {
                    int cycles = Integer.parseInt(input);
                    return new PauseInput(cycles);                    
                }
                catch (NumberFormatException e) {                }
                return null;
            }
        });
        
        //reset
        parsers.add(new TextReaction() {
            @Override public Object react(String input) {                
                if (input.equals(Symbols.RESET_COMMAND) || (input.startsWith("*") && !input.startsWith("*start") && !input.startsWith("*decisionthreshold") 
                        && !input.startsWith("*stop") && !input.startsWith("*volume"))) //TODO!
                    return new Reset(input);
                return null;
            }
        });
        //reboot
        parsers.add(new TextReaction() {
            @Override public Object react(String input) {                
                if (input.equals(Symbols.REBOOT_COMMAND)) {
                    //immediately reset the memory
                    memory.emit(IN.class, "reboot");
                    memory.reset();
                    return new Reboot();
                }
                return null;
            }
        });
        
        //silence
        parsers.add(new TextReaction() {
            @Override public Object react(String input) {                
                if (input.indexOf(Symbols.SET_NOISE_LEVEL_COMMAND)==0) {
                    String[] p = input.split("=");
                    if (p.length == 2) {
                        int noiseLevel = Integer.parseInt(p[1].trim());
                        return new SetVolume(noiseLevel);
                    }
                }
                if (input.indexOf(Symbols.SET_DECISION_LEVEL_COMMAND)==0) {
                    String[] p = input.split("=");
                    if (p.length == 2) {
                        double threshold = Double.parseDouble(p[1].trim());
                        return new SetDecisionThreshold(threshold);
                    }
                }
                return null;                
            }
        });
   
        //echo
        //TODO standardize on an echo/comment format
        parsers.add(new TextReaction() {
            @Override
            public Object react(String input) {
                char c = input.charAt(0);
                if (c == Symbols.ECHO_MARK) {            
                    String echoString = input.substring(1);
                    return new Echo(Echo.class, echoString);
                }
                final String it = input.trim();
                if (it.startsWith("OUT:") || it.startsWith("//") || it.startsWith("***") ) {
                    return new Echo(Echo.class, input);
                }
                return null;                
            }
        });
        
        //narsese
        parsers.add(new TextReaction() {
            @Override
            public Object react(String input) {
                char c = input.charAt(0);
                if (c != Symbols.COMMENT_MARK) {
                    try {
                        Item task = narsese.parseNarsese(new StringBuilder(input));
                        if (task != null) {
                            return task;
                        }
                    } catch (InvalidInputException ex) {
                        return ex;
                    }
                }
                return null;
            }
        });             
        
        return parsers;           
    }
    
    protected Iterator<Item> perceive(final String line) {

        Exception lastException = null;
        
        for (final TextReaction p : parsers) {            
            
            Object result = p.react(line);
            
            if (result!=null) {
                if (result instanceof Iterator) {
                    return (Iterator<Item>)result;
                }
                if (result instanceof Collection) {
                    return ((Collection<Item>)result).iterator();
                }
                if (result instanceof Item) {
                    return singletonIterator((Item)result);
                }
                else if (result.equals(Boolean.TRUE)) {
                    return null;
                }
                else if (result instanceof Exception) {
                    lastException = (Exception)result;
                }
            }
        }

        String errorMessage = "Invalid input: \'" + line + "\'";

        if (lastException!=null) {
            errorMessage += " : " + lastException.toString(); 
        }

        memory.emit(Output.ERR.class, errorMessage);
        
        return null;
    }
}
