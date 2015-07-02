package nars.io;

import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.nal.Task;
import nars.narsese.NarseseParser;
import nars.op.io.Echo;
import nars.op.io.Reset;
import nars.op.io.SetVolume;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 *  Default handlers for text perception.
 *  Parses input text into sequences of Task's which input into 
 *      Memory via NAR input channel & buffer port.
 *  
 *  TODO break into separate subclasses for each text mode
 */
public class TextPerception  {

    public final Memory memory;
    
    public final List<TextReaction<Task>> parsers;

    //public Englisch englisch;
    //public Twenglish twenglish;
    
    private final boolean enableNarsese = true;

    //private boolean enableNaturalLanguage = false; //the NLP mode we should strive for
    //private boolean enableEnglisch = false;
    
    //private boolean enableTwenglish = false; //the events should be introduced event-wise
    //or with a higher order copula a1...an-1 =/> an, because a &/ statement alone is useless for temporal logic


    public TextPerception(NAR n, NarseseParser parser) {
        this.memory = n.memory;

        //this.englisch = new Englisch();
        //this.twenglish = new Twenglish(memory);
        this.parsers = new ArrayList();

//        //integer, # of cycles to step
//        parsers.add(new TextReaction<Task>() {
//            final String spref = Symbols.INPUT_LINE_PREFIX + ':';
//
//            @Override public void react(String input, Consumer<Task> recv) {
//
//                input = input.trim();
//                if (input.startsWith(spref))
//                    input = input.substring(spref.length());
//
//                if (input.isEmpty()) return;
//                if (!Character.isDigit(input.charAt(0)))
//                    return;
//                if (input.length() > 8) {
//                    //if input > ~8 chars it wont fit as 32bit integer anyway so terminate early.
//                    //parseInt is sorted of expensive
//                    return;
//                }
//
//                try {
//                    int cycles = Integer.parseInt(input);
//                    recv.accept( new PauseInput(cycles).newTask() );
//                }
//                catch (NumberFormatException e) {
//                }
//            }
//        });

        //reset
        parsers.add(new TextReaction<Task>() {
            @Override public void react(String input, Consumer<Task> recv) {
                if (input.equals(Symbols.RESET_COMMAND) || (input.startsWith("*") && !input.startsWith("*start")
                        && !input.startsWith("*stop") && !input.startsWith("*volume"))) //TODO!
                    recv.accept( new Reset(false).newTask() );
            }
        });
        //reboot
        parsers.add(new TextReaction<Task>() {
            @Override public void react(String input, Consumer<Task> recv) {
                if (input.equals(Symbols.REBOOT_COMMAND)) {
                    //immediately reset the memory
                    recv.accept(new Reset(true).newTask() );
                }
            }
        });

//      TODO implement these with Task's
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
        parsers.add(new TextReaction<Task>() {
            @Override public void react(String input, Consumer<Task> recv) {
                if (input.indexOf(Symbols.SET_NOISE_LEVEL_COMMAND)==0) {
                    String[] p = input.split("=");
                    if (p.length == 2) {
                        int noiseLevel = Integer.parseInt(p[1].trim());
                        recv.accept(new SetVolume(noiseLevel).newTask());
                    }
                }
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

//        //echo
//        //TODO standardize on an echo/comment format
//        parsers.add(new TextReaction<Task>() {
//            @Override
//            public void react(String input, Consumer<Task> recv) {
//                char c = input.charAt(0);
//                Echo e = null;
//                if (c == Symbols.ECHO_MARK) {
//                    String echoString = input.substring(1);
//                    e = new Echo(Echo.class, echoString);
//                }
//                final String it = input.trim();
//                if (it.startsWith("OUT:") || it.startsWith("//") || it.startsWith("***") ) {
//                    e = new Echo(Echo.class, input);
//                }
//                if (e!=null)
//                    recv.accept(e.newTask());
//            }
//        });
//


        //narsese
        parsers.add(new TextReaction<Task>() {
            @Override
            public void react(String input, Consumer<Task> recv) {
                if (enableNarsese) {
                    char c = input.charAt(0);
                    if (c != Symbols.COMMENT_MARK) {
                        try {
                            parser.parse(input, recv);
                        } catch (Exception ex) {
                            recv.accept( new Echo(ex).newTask() );
                        }
                    }
                }
            }
        });
//
//
//        //englisch
//        parsers.add(new TextReaction() {
//            @Override
//            public Object react(String line) {
//
//                if (enableEnglisch) {
//                    /*if (!possiblyNarsese(line))*/ {
//                        try {
//                            List<Task> l = englisch.parse(line, narsese, true);
//                            if ((l == null) || (l.isEmpty()))
//                                return null;
//                            return l;
//                        } catch (InvalidInputException ex) {
//                            return null;
//                        }
//                    }
//                }
//                return null;
//            }
//        });
//
//        //englisch
//        parsers.add(new TextReaction() {
//            @Override
//            public Object react(String line) {
//
//                if (enableTwenglish) {
//                    /*if (!possiblyNarsese(line))*/ {
//                        try {
//                            List<Task> l = twenglish.parse(line, narsese, true);
//                            if ((l == null) || (l.isEmpty()))
//                                return null;
//                            return l;
//                        } catch (InvalidInputException ex) {
//                            return null;
//                        }
//                    }
//                }
//                return null;
//            }
//        });
//
//        // natural language
//        parsers.add(new TextReaction() {
//            @Override
//            public Object react(String line) {
//
//                if (enableNaturalLanguage) {
//                    /*if (!possiblyNarsese(line))*/ {
//                        List<Task> l = NaturalLanguagePerception.parseLine(line, narsese, "word");
//                        if ((l == null) || (l.isEmpty()))
//                            return null;
//                        return l;
//                    }
//                }
//                return null;
//            }
//        });


    }


    public void perceive(final String line, Consumer<Task> receiver) {

        
        for (final TextReaction p : parsers) {            
            
            p.react(line, receiver);
        }

    }

}
