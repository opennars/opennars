package nars.io;

import static com.google.common.collect.Iterators.singletonIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.AbstractTask;
import nars.io.Output.IN;
import nars.io.narsese.Narsese;
import nars.io.narsese.Narsese.InvalidInputException;
import nars.io.nlp.Englisch;
import nars.operator.io.Echo;
import nars.operator.io.PauseInput;
import nars.operator.io.Reboot;
import nars.operator.io.Reset;
import nars.operator.io.SetVolume;

/**
 *  Default handlers for text perception
 */
public class DefaultTextPerception  {
    
    
    public final List<TextReaction> parsers;
    private final Memory memory;
    
    public final Narsese narsese;    
    public final Englisch englisch;
    private boolean enableEnglisch = true;
    private boolean enableNarsese = true;
    
    public DefaultTextPerception(final NAR nar) {
        super();
        
        this.memory = nar.memory;
        this.narsese = new Narsese(memory);
        this.englisch = new Englisch();
        this.parsers = new ArrayList();
        
        //integer, # of cycles to step
        parsers.add(new TextReaction() {
            final String spref = Symbols.INPUT_LINE_PREFIX + ':';
            
            @Override
            public Object react(String input) {
                try {
                    input = input.trim();
                    if (input.startsWith(spref))
                        input = input.substring(spref.length());                    
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
                if (input.equals(Symbols.RESET_COMMAND))
                    return new Reset();
                return null;
            }
        });
        //reboot
        parsers.add(new TextReaction() {
            @Override public Object react(String input) {                
                if (input.equals(Symbols.REBOOT_COMMAND)) {
                    //immediately reset the memory
                    memory.output(IN.class, "reboot");
                    memory.reset();
                    return new Reboot();
                }
                return null;
            }
        });

//      TODO implement these with AbstractTask's        
//        //stop
//        parsers.add(new TextReaction() {
//            @Override
//            public Object react(String input) {
//                if (!memory.isWorking())  {
//                    if (input.equals(Symbols.STOP_COMMAND)) {
//                        memory.output(Output.IN.class, input);
//                        memory.setWorking(false);
//                        return Boolean.TRUE;                        
//                    }
//                }
//                return null;                
//            }
//        });    
//        
//        //start
//        parsers.add(new TextReaction() {
//            @Override public Object react(String input) {                
//                if (memory.isWorking()) {
//                    if (input.equals(Symbols.START_COMMAND)) {
//                        memory.setWorking(true);
//                        memory.output(Output.IN.class, input);
//                        return Boolean.TRUE;                        
//                    }
//                }
//                return null;                
//            }
//        });
        
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
                return null;                
            }
        });
        
//        //URL include
//        parsers.add(new TextReaction() {
//            @Override
//            public Object react(Memory m, String input) {
//                char c = input.charAt(0);
//                if (c == Symbols.URL_INCLUDE_MARK) {            
//                    try {
//                        nar.addInput(new TextInput(new URL(input.substring(1))));
//                    } catch (IOException ex) {
//                        m.output(ERR.class, ex);
//                    }
//                    return true;
//                }
//                return false;                
//            }
//        });        

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

                if (enableNarsese) {
                    char c = input.charAt(0);
                    if (c != Symbols.COMMENT_MARK) {
                        try {
                            AbstractTask task = narsese.parseNarsese(new StringBuilder(input));
                            if (task != null) {
                                return task;
                            }
                        } catch (InvalidInputException ex) {
                            return ex;
                        }
                    }
                }
                return null;
            }
        });             

        //englisch
        parsers.add(new TextReaction() {
            @Override
            public Object react(String line) {
                
                if (enableEnglisch) {
                    /*if (!possiblyNarsese(line))*/ {                    
                        List<AbstractTask> l = englisch.parse(line, narsese, true);
                        if ((l == null) || (l.isEmpty())) 
                            return null;
                        return l;
                    }
                }
                return null;            
            }
        });             
        
                   
    }
    
    public Iterator<AbstractTask> perceive(final String line) {

        Exception lastException = null;
        
        for (final TextReaction p : parsers) {            
            
            Object result = p.react(line);
            
            if (result!=null) {
                if (result instanceof Iterator) {
                    return (Iterator<AbstractTask>)result;
                }
                if (result instanceof Collection) {
                    return ((Collection<AbstractTask>)result).iterator();
                }
                if (result instanceof AbstractTask) {
                    return singletonIterator((AbstractTask)result);
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
        
        memory.output(Output.ERR.class, errorMessage);
        return null;
    }

    public void enableEnglisch(boolean enableEnglisch) {
        this.enableEnglisch = enableEnglisch;
    }

    public void enableNarsese(boolean enableNarsese) {
        this.enableNarsese = enableNarsese;
    }

    
    
}
