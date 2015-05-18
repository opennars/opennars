package nars.irc;


import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.ReflectPanel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import nars.Global;
import nars.NAR;
import nars.event.NARReaction;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.model.impl.Default;
import nars.nal.concept.Concept;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.op.io.say;
import nars.rl.example.MarkovObservationsGraph;
import nars.util.language.Twokenize;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class IRCBot {

    private final MarkovObservationsGraph m;
    boolean outputting = false;

    // The server to connect to and our details.
    String server = "irc.freenode.net";
    String nick = "NARchy";
    String login = "narchy";

    // The channel which the bot will join.
    String channel = "#fukushima";
    private final NAR nar;
    private BufferedWriter writer = null;

    public void setOutputting(boolean outputting) {
        this.outputting = outputting;
    }

    public static void main(String[] args) throws Exception {
        Global.DEBUG = false;


        Default d = new Default();
        //Default d = new Solid(4, 64, 0,5, 0,3);
        d.setConceptBagSize(2048);
        d.decisionThreshold.set(0.7);
        d.temporalRelationsMax.set(2);
        d.shortTermMemoryHistory.set(4);
        d.duration.set(5);
        d.termLinkMaxReasoned.set(6);
        d.conceptsFiredPerCycle.set(9);
        //d.setTiming(Memory.Timing.RealMS);


        //d.temporalPlanner(16f,8,8,2);

        NAR n = new NAR( d );


        File corpus = new File("/tmp/h.nal");
        n.input(corpus);
        System.out.print("initializing...");
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            n.run(10);
        }
        System.out.println("ok");




        Video.themeInvert();
        new NARSwing(n).setSpeed(0.1f);


        IRCBot i = new IRCBot(n);

        i.loop(corpus, 200);

        /*String[] book = String.join(" ", Files.readAllLines(Paths.get("/home/me/battle.txt"))).split("\\. ");
        i.read(book, 1200, 0.5f);*/
        String[] book2 = String.join(" ", Files.readAllLines(Paths.get("/home/me/meta.txt"))).split("\\. ");
        i.read(book2, 1300, 0.25f);

    }

    private void loop(File corpus, int lineDelay) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                List<String> lines = null;
                try {
                    lines = Files.readAllLines(Paths.get(corpus.toURI()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                while (true)  {

                    for (String s : lines) {
                        s = s.trim();
                        if (s.isEmpty())continue;


                        nar.input(s);

                        try {
                            Thread.sleep(lineDelay);
                        } catch (InterruptedException e) {
                        }
                    }

                }
            }
        }).start();

    }

    private void read(String[] sentences, int delayMS, float priority) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (String s : sentences) {

                    s = s.trim();
                    if (s.length() < 2) continue;

                    if (!s.endsWith(".")  && !s.endsWith("?") && !s.endsWith("!")) s=s+'.';
                    if (hear("book", s, priority) == 0) continue;

                    try {
                        Thread.sleep(delayMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public int hear(String channel, String m, float priority) {
        final int delay = 25 /*cycles */, endDelay = 1000, tokenMax = 16, tokenMin = 1;
        List<Twokenize.Span> tokens = Twokenize.twokenize(m);
        int nonPunc = Iterables.size(Iterables.filter(tokens, new Predicate<Twokenize.Span>() {

            @Override
            public boolean apply(Twokenize.Span input) {
                return !input.pattern.equals("punct");
            }
        }));

        if (nonPunc > tokenMax) return 0;
        if (nonPunc < tokenMin) return 0;



        //String i = "<language --> hear>. :|: \n " + delay + "\n";

        Iterable<String> s = Iterables.transform(tokens, new Function<Twokenize.Span, String>() {

            @Override
            public String apply(Twokenize.Span input) {
                String a = "";
                String pattern = "";
                Term wordTerm;
                if (input.pattern.equals("word")) {
                    a = input.content.toLowerCase().toString();
                    wordTerm = Atom.get(a);
                    pattern = "word";
                }
                //TODO apostrophe words
                else if (input.pattern.equals("punct")) {
                    String b = input.content;
                    wordTerm = Atom.quoted(b);

                    a = input.content;
                    pattern = "word";
                }
                else {
                    return "";
                }
                //else
                  //  a = "\"" + input.content.toLowerCase() + "\"";
                //String r = "<" + a + " --> " + pattern + ">. :|:\n";

                //Term tt = Inheritance.make(wordTerm, Term.get(pattern));
                //char punc = '.';

                //Term tt = Operation.make(nar.memory.operate("^say"), new Term[] {wordTerm});
                //char punc = '!';

                //nar.input(new Sentence(tt, punc, new TruthValue(1.0f, 0.9f), new Stamp(nar.memory, Tense.Present)));
                //nar.think(delay);
                //r += "say(" + a + ")!\n";
                //r += delay + "\n";
//                try {
//                    Thread.sleep(delay);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if (a.isEmpty()) return "";
                //return "<{\"" + a + "\"}-->WORD>.";
                return "say(\"" + a + "\")!";
            }
        });
        String xs = "say()!\n" + delay + "\n"; //clear the buffer before
        for (String w : s) {
            xs += "$" + Texts.n2(priority) + "$ " + w + "\n";
            xs += delay + "\n";

        }
        //System.err.println(xs);
        nar.input(xs);
//
//        System.out.println(nar.time() + " HEAR: " + tokens);
//        //System.out.println("HEAR: " + i);
//
//        String i = "<(*," + c + ") --> PHRASE>.";
//        nar.input(i);
//        String j = "<(&/," + c + ") --> PHRASE>. :|:";
//        nar.input(j);
//
//        try {
//            Thread.sleep(endDelay);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return tokens.size();
    }

    public IRCBot(NAR n) throws Exception {

        m = new MarkovObservationsGraph(n) {
            @Override
            public boolean contains(Concept c) {
                return true;
            }
        };
        m.setCyclesPerEpisode(5);

        new NWindow("Say", new ReflectPanel(this)).show(500,300);

        this.nar = n;
        new NARReaction(nar, say.class) {

            public String last = "";


            int minSpokenWords = 2;

            @Override
            public void event(Class event, Object[] args) {
                if (event == say.class) {
                    //Operator.ExecutionResult er = (Operator.ExecutionResult)args[0];

                    Term a = (Term)args[0]; //er.getOperation().getArguments().term;

                    String s = a.toString();
                    s = s.replace("{\"", "");
                    s = s.replace("\"}", "");
                    s = s.trim();
                    if (s.length() == 1) {
                        if (s.equals("¸")) s = "."; //hotfix for the period
                        if (s.equals(last))
                            return; //dont repeat punctuation
                    }
                    else {

                    }

                    if (!s.isEmpty()) {
                        say(s);
                        last = s;
                    }

//                    if (a.length >= minSpokenWords)  {
//                        String m = "";
//                        int n = 0;
//                        for (int i = 0; i < a.length; i++) {
//                            Term x = a[i];
//                            if (x.equals(nar.memory.getSelf()))
//                                continue;
//                            m += x.toString().replace("\"", "").trim() + " ";
//                        }
//                        m = m.trim();
//
//                        if (!m.equals(lastMessage))
//                            say(m);
//
//                        lastMessage = m;
//                    }
//                    else {
//                        //System.out.println("not SAY: " + Arrays.toString(a));
//
//                    }
                }
            }
        };

        /*
        new BufferedOutput(nar, 1, 1000, 64) {

            @Override
            protected void output(List<BufferedOutput.OutputItem> buffer) {
               System.out.println(buffer);
            }
        };
        */


        //new TextOutput(nar, System.out).setShowErrors(true).setOutputPriorityMin(0.95f);

        // Connect directly to the IRC server.
        Socket socket = new Socket(server, 6667);
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream( )));
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream( )));

        // Log on to the server.
        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + login + " 8 * : NARchy\r\n");
        writer.flush( );

        new Thread(new Runnable() {

            @Override
            public void run() {

                // Join the channel.
                try {
                    // Read lines from the server until it tells us we have connected.
                    String line = null;
                    while ((line = reader.readLine( )) != null) {
                        if (line.indexOf("004") >= 0) {
                            // We are now logged in.
                            break;
                        }
                        else if (line.indexOf("433") >= 0) {
                            System.out.println("Nickname is already in use.");
                            return;
                        }
                    }


                    writer.write("JOIN " + channel + "\r\n");
                    writer.flush();
                    // Keep reading lines from the server.
                    while ((line = reader.readLine( )) != null) {
                        if (line.toLowerCase( ).startsWith("PING ")) {
                            // We must respond to PINGs to avoid being disconnected.
                            writer.write("PONG " + line.substring(5) + "\r\n");
                            //writer.write("PRIVMSG " + channel + " :I got pinged!\r\n");
                            //writer.flush( );
                        }
                        else {
                            // Print the raw line received by the bot.
                            //System.out.println(line);
                            if (line.contains(" PRIVMSG " )) {
                                String part = "PRIVMSG " + channel;
                                int s = line.indexOf(part);
                                if (s!=-1) {
                                    String msg = line.substring(s + part.length() + 2);
                                    hear(channel, msg, 0.8f);
                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    String buffer = "";
    int outputBufferLength = 100;

    public synchronized void say(String s) {

        System.out.println("say: " + s);
        buffer += " " + s;

        if (buffer.length() > outputBufferLength) {


            buffer = buffer.trim();
            buffer = buffer.replace(" .", ". ");

            System.out.println("SAY: " + buffer);
            if ((writer!=null) && (outputting)) {
                try {
                    writer.write("PRIVMSG " + channel + " :" + buffer + "\r\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            buffer = "";
        }

    }
}
