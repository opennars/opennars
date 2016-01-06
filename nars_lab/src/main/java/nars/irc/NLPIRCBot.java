//package nars.irc;
//
//import com.google.common.base.Function;
//import com.google.common.base.Predicate;
//import com.google.common.collect.Iterables;
//import nars.Global;
//import nars.NAR;
//import nars.Video;
//import nars.clock.RealtimeMSClock;
//import nars.event.NARReaction;
//import nars.gui.NARSwing;
//import nars.io.Texts;
//import nars.io.out.TextOutput;
//import nars.nar.Default;
//import nars.op.io.say;
//import nars.term.Atom;
//import nars.term.Term;
//import nars.util.language.Twokenize;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
///**
// * Created by me on 6/20/15.
// */
//public class NLPIRCBot extends IRCBot {
//
//
//    private final NAR nar;
//
//    public NLPIRCBot(NAR n) throws Exception {
//        super("irc.freenode.net", "NARchy", "#nars");
//
//        this.nar = n;
//
//        new NARReaction(nar, say.class) {
//
//            public String last = "";
//
//
//            int minSpokenWords = 2;
//
//            @Override
//            public void event(Class event, Object[] args) {
//                if (event == say.class) {
//                    //Operator.ExecutionResult er = (Operator.ExecutionResult)args[0];
//
//                    Term a = (Term)args[0]; //er.getOperation().getArguments().term;
//
//                    String s = a.toString();
//                    s = s.replace("{\"", "");
//                    s = s.replace("\"}", "");
//                    s = s.trim();
//                    if (s.length() == 1) {
//                        if (s.equals("¸")) s = "."; //hotfix for the period
//                        if (s.equals(last))
//                            return; //dont repeat punctuation
//                    }
//                    else {
//
//                    }
//
//                    if (!s.isEmpty()) {
//                        say(s);
//                        last = s;
//                    }
//
////                    if (a.length >= minSpokenWords)  {
////                        String m = "";
////                        int n = 0;
////                        for (int i = 0; i < a.length; i++) {
////                            Term x = a[i];
////                            if (x.equals(nar.memory.getSelf()))
////                                continue;
////                            m += x.toString().replace("\"", "").trim() + " ";
////                        }
////                        m = m.trim();
////
////                        if (!m.equals(lastMessage))
////                            say(m);
////
////                        lastMessage = m;
////                    }
////                    else {
////                        //System.out.println("not SAY: " + Arrays.toString(a));
////
////                    }
//                }
//            }
//        };
//
//    }
//
//    public static void main(String[] args) throws Exception {
//        Global.DEBUG = false;
//
//
//        Default d = new Default();
//        //Default d = new Solid(4, 64, 0,5, 0,3);
//        d.setActiveConcepts(768);
//
//        d.executionThreshold.set(0.5);
//
//        d.temporalRelationsMax.set(4);
//
//        d.shortTermMemoryHistory.set(4);
//
//        d.conceptTaskTermProcessPerCycle.set(4);
//
//        d.conceptsFiredPerCycle.set(64);
//
//        d.duration.set(100 /* ms */);
//        d.setClock(new RealtimeMSClock(false));
//
//
//        //d.temporalPlanner(16f,8,8,2);
//
//        NAR n = new NAR( d );
//
//        TextOutput.out(n);
//
//        File corpus = new File("/tmp/h.nal");
//        n.input(corpus);
//
//        System.out.print("initializing...");
//        for (int i = 0; i < 10; i++) {
//            System.out.print(i + " ");
//            n.frame(10);
//        }
//        System.out.println("ok");
//
//
//
//
//        Video.themeInvert();
//        new NARSwing(n).setSpeed(0.04f);
//
//
//        NLPIRCBot i = new NLPIRCBot(n);
//
//        i.loop(corpus, 200);
//
//        /*String[] book = String.join(" ", Files.readAllLines(Paths.get("/home/me/battle.txt"))).split("\\. ");
//        i.read(book, 1200, 0.5f);*/
//        String[] book2 = String.join(" ", Files.readAllLines(Paths.get("/home/me/meta.txt"))).split("\\. ");
//        i.read(book2, 1300, 0.25f);
//
//    }
//
//    public void loop(File corpus, int lineDelay) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                List<String> lines = null;
//                try {
//                    lines = Files.readAllLines(Paths.get(corpus.toURI()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                while (true)  {
//
//                    for (String s : lines) {
//                        s = s.trim();
//                        if (s.isEmpty())continue;
//
//
//                        nar.input(s);
//
//                        try {
//                            Thread.sleep(lineDelay);
//                        } catch (InterruptedException e) {
//                        }
//                    }
//
//                }
//            }
//        }).start();
//
//    }
//
//    public void read(String[] sentences, int delayMS, float priority) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                for (String s : sentences) {
//
//                    s = s.trim();
//                    if (s.length() < 2) continue;
//
//                    if (!s.endsWith(".")  && !s.endsWith("?") && !s.endsWith("!")) s=s+'.';
//                    if (hear("book", s, priority, delayMS) == 0) continue;
//
//                }
//            }
//        }).start();
//    }
//
//
//    public int hear(String channel, String m, float priority, long wordDelay) {
//        final int delay = 25 /*cycles */, endDelay = 1000, tokenMax = 16, tokenMin = 1;
//        List<Twokenize.Span> tokens = Twokenize.twokenize(m);
//        int nonPunc = Iterables.size(Iterables.filter(tokens, new Predicate<Twokenize.Span>() {
//
//            @Override
//            public boolean apply(Twokenize.Span input) {
//                return !input.pattern.equals("punct");
//            }
//        }));
//
//        if (nonPunc > tokenMax) return 0;
//        if (nonPunc < tokenMin) return 0;
//
//
//
//        //String i = "<language --> hear>. :|: \n " + delay + "\n";
//
//        Iterable<String> s = Iterables.transform(tokens, new Function<Twokenize.Span, String>() {
//
//            @Override
//            public String apply(Twokenize.Span input) {
//                String a = "";
//                String pattern = "";
//                Term wordTerm;
//                if (input.pattern.equals("word")) {
//                    a = input.content.toLowerCase().toString();
//                    wordTerm = Atom.the(a);
//                    pattern = "word";
//                }
//                //TODO apostrophe words
//                else if (input.pattern.equals("punct")) {
//                    String b = input.content;
//                    wordTerm = Atom.quote(b);
//
//                    a = input.content;
//                    pattern = "word";
//                }
//                else {
//                    return "";
//                }
//                //else
//                //  a = "\"" + input.content.toLowerCase() + "\"";
//                //String r = "<" + a + " --> " + pattern + ">. :|:\n";
//
//                //Term tt = Inheritance.make(wordTerm, Term.get(pattern));
//                //char punc = '.';
//
//                //Term tt = Operation.make(nar.memory.operate("^say"), new Term[] {wordTerm});
//                //char punc = '!';
//
//                //nar.input(new Sentence(tt, punc, new TruthValue(1.0f, 0.9f), new Stamp(nar.memory, Tense.Present)));
//                //nar.think(delay);
//                //r += "say(" + a + ")!\n";
//                //r += delay + "\n";
////                try {
////                    Thread.sleep(delay);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//                if (a.isEmpty()) return "";
//                //return "<{\"" + a + "\"}-->WORD>.";
//                return "(say, \"" + a + "\", " + channel + "). :|:";
//            }
//        });
//        //String xs = "say()!\n" + delay + "\n"; //clear the buffer before
//        for (String w : s) {
//            String xs = "$" + Texts.n2(priority) + "$ " + w + "\n";
//
//            System.err.println(xs);
//            nar.input(xs);
//
//            try {
//                Thread.sleep(wordDelay);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
////
////        System.out.println(nar.time() + " HEAR: " + tokens);
////        //System.out.println("HEAR: " + i);
////
////        String i = "<(*," + c + ") --> PHRASE>.";
////        nar.input(i);
////        String j = "<(&/," + c + ") --> PHRASE>. :|:";
////        nar.input(j);
////
////        try {
////            Thread.sleep(endDelay);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//
//        return tokens.size();
//    }
//
//
//    String buffer = "";
//    int outputBufferLength = 100;
//
//    public synchronized void say(String s) {
//
//        System.out.println("say: " + s);
//        buffer += " " + s;
//
//        if (buffer.length() > outputBufferLength) {
//
//
//            buffer = buffer.trim();
//            buffer = buffer.replace(" .", ". ");
//
//            System.out.println("SAY: " + buffer);
//            if ((writer!=null) && (outputting)) {
//                send(channel, buffer);
//            }
//            buffer = "";
//        }
//
//    }
//
//    @Override
//    protected void onMessage(IRCBot bot, String channel, String nick, String msg) {
//        new Thread( () -> { hear(channel, msg, 0.7f, 100); } ).start();
//    }
// }
