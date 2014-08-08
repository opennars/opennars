package nars.io;

import java.io.IOException;
import java.net.URL;
import nars.core.NAR;
import nars.entity.Task;
import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.storage.Memory;

/**
 *  Default handlers for text perception
 */
public class DefaultTextPerception extends TextPerception {
    
    public DefaultTextPerception(NAR nar) {
    
        super(nar);
        
        //integer, # of cycles to step
        parsers.add(new TextReaction() {
            final String spref = Symbols.INPUT_LINE_PREFIX + ':';
            
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {
                try {
                    input = input.trim();
                    
                    
                    if (input.startsWith(spref)) {
                        input = input.substring(spref.length());
                    }
                    input = input.trim();
                    
                    int cycles = Integer.parseInt(input);
                    m.output(IN.class, cycles);
                    
                    //TODO queue the cycles, invoke call from here
                    m.stepLater(cycles);
                    return true;
                }
                catch (NumberFormatException e) {
                    return false;
                }                
            }
        });
        
        //reset
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {                
                if (input.equals(Symbols.RESET_COMMAND)) {
                    nar.reset();
                    m.output(Output.IN.class, input);
                    return true;
                }
                return false;
            }
        });
        
        //stop
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {
                if (!m.isWorking())  {
                    if (input.equals(Symbols.STOP_COMMAND)) {
                        m.output(Output.IN.class, input);
                        m.setWorking(false);
                        return true;
                    }
                }
                return false;                
            }
        });    
        
        //start
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {                
                if (m.isWorking()) {
                    if (input.equals(Symbols.START_COMMAND)) {
                        m.setWorking(true);
                        m.output(Output.IN.class, input);
                        return true;
                    }
                }
                return false;                
            }
        });
        
        //silence
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {                

                if (input.indexOf(Symbols.SET_NOISE_LEVEL_COMMAND)==0) {
                    String[] p = input.split("=");
                    if (p.length == 2) {
                        int noiseLevel = Integer.parseInt(p[1]);
                        m.param.noiseLevel.set(noiseLevel);                        
                        m.output(Output.IN.class, input);
                    }
                    
                    return true;
                }

                return false;                
            }
        });
        
        //URL include
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {
                char c = input.charAt(0);
                if (c == Symbols.URL_INCLUDE_MARK) {            
                    try {
                        nar.addInput(new TextInput(new URL(input.substring(1))));
                    } catch (IOException ex) {
                        m.output(ERR.class, ex);
                    }
                    return true;
                }
                return false;                
            }
        });        

        //echo
        //TODO standardize on an echo/comment format
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {
                char c = input.charAt(0);
                if (c == Symbols.ECHO_MARK) {            
                    String echoString = input.substring(1);
                    nar.output(Output.ECHO.class, '\"' + echoString + '\"');
                    return true;
                }
                final String it = input.trim();
                if (it.startsWith("OUT:") || it.startsWith("//") || it.startsWith("****") ) {
                    nar.output(Output.ECHO.class, input);
                    return true;
                }
                return false;                
            }
        });
        
        //narsese
        parsers.add(new TextReaction() {
            @Override
            public boolean react(Memory m, String input, TextReaction lastHandler) {
                if (lastHandler != null)
                    return false;

                char c = input.charAt(0);
                if (c != Symbols.COMMENT_MARK) {
                    try {
                        Task task = parseNarsese(new StringBuilder(input));
                        if (task != null) {
                            nar.output(Output.IN.class, task.sentence);    // report addInput
                            nar.memory.inputTask(task);
                            return true;
                        }
                    } catch (InvalidInputException ex) {
                        /*System.err.println(ex.toString());
                        ex.printStackTrace();*/
                        return false;
                    }
                }
                return false;                
            }
        });             

                   
    }


    
    
}
