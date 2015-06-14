package nars.io;

import nars.core.task.PauseInput;
import nars.entity.Task;
import nars.storage.Memory;

/**
 *  Default handlers for text perception
 */
public class DefaultTextPerception extends TextPerception {
    
    public DefaultTextPerception(Memory memory) {
    
        super(memory);
        
        //integer, # of cycles to step
        parsers.add(new TextReaction() {
            final String spref = Symbols.INPUT_LINE_PREFIX + ':';
            
            @Override
            public Object react(String input) {
                try {
                    input = input.trim();
                    
                    
                    if (input.startsWith(spref)) {
                        input = input.substring(spref.length());
                    }
                    
                    int cycles = Integer.parseInt(input);
                    return new PauseInput(cycles);                    
                }
                catch (NumberFormatException e) {
                }
                return null;
            }
        });
        
        //reset
        parsers.add(new TextReaction() {
            @Override
            public Object react(String input) {                
                if (input.equals(Symbols.RESET_COMMAND)) {
                    memory.reset();
                    memory.output(Output.IN.class, input);
                    return Boolean.TRUE;                    
                }
                return null;
            }
        });
        
        //stop
        parsers.add(new TextReaction() {
            @Override
            public Object react(String input) {
                if (!memory.isWorking())  {
                    if (input.equals(Symbols.STOP_COMMAND)) {
                        memory.output(Output.IN.class, input);
                        memory.setWorking(false);
                        return Boolean.TRUE;                        
                    }
                }
                return null;                
            }
        });    
        
        //start
        parsers.add(new TextReaction() {
            @Override public Object react(String input) {                
                if (memory.isWorking()) {
                    if (input.equals(Symbols.START_COMMAND)) {
                        memory.setWorking(true);
                        memory.output(Output.IN.class, input);
                        return Boolean.TRUE;                        
                    }
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
                        int noiseLevel = Integer.parseInt(p[1]);
                        memory.param.noiseLevel.set(noiseLevel);                        
                        memory.output(Output.IN.class, input);
                        return Boolean.TRUE;                        
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
                    memory.output(Output.ECHO.class, '\"' + echoString + '\"');
                    return Boolean.TRUE;
                }
                final String it = input.trim();
                if (it.startsWith("OUT:") || it.startsWith("//") || it.startsWith("***") ) {
                    memory.output(Output.ECHO.class, input);
                    return Boolean.TRUE;
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
                        Task task = parseNarsese(new StringBuilder(input));
                        if (task != null) {
                            return task;
                        }
                    } catch (InvalidInputException ex) {
                        /*System.err.println(ex.toString());
                        ex.printStackTrace();*/
                    }
                }
                return null;
            }
        });             

                   
    }

    
}
