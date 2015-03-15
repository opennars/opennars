package nars.irc;


import automenta.vivisect.Video;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import nars.build.Default;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.AbstractReaction;
import nars.gui.NARSwing;
import nars.io.nlp.Twokenize;
import nars.logic.entity.Sentence;
import nars.logic.entity.Term;
import nars.logic.entity.TruthValue;
import nars.logic.entity.stamp.Stamp;
import nars.logic.nal1.Inheritance;
import nars.logic.nal7.Tense;
import nars.logic.nal8.Operator;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class IRCBot {

    boolean outputting = false;

    // The server to connect to and our details.
    String server = "irc.freenode.net";
    String nick = "NARchy";
    String login = "narchy";

    // The channel which the bot will join.
    String channel = "#nars";
    private final NAR nar;
    private BufferedWriter writer = null;


    public static void main(String[] args) throws Exception {
        Parameters.DEBUG = true;


        Default d = new Default();
        //Default d = new Solid(1024, 10, 1);
        /*d.param.shortTermMemoryHistory.set(3);
        d.param.termLinkMaxReasoned.set(4);
        d.param.conceptsFiredPerCycle.set(2);
        d.param.decisionThreshold.set(0.3);
        d.param.temporalRelationsMax.set(2);*/

        NAR n = new NAR( d );


        n.input(new File("/tmp/h.nal"));
        System.out.print("initializing...");
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            n.run(1000);
        }
        System.out.println("ok");
        //n.start(10, 1);

        Video.themeInvert();
        new NARSwing(n).controls.setSpeed(0.1f);


        IRCBot i = new IRCBot(n);

        i.hear("chan", "i exist now.");
        n.input("say(i,exist,now)!");

        String[] book = String.join(" ", Files.readAllLines(Paths.get("/home/me/meta.txt"))).split("\\. ");
        i.read(book, 1000);
    }

    private void read(String[] sentences, int delayMS) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (String s : sentences) {

                    s = s.trim();
                    if (s.length() < 2) continue;

                    if (!s.endsWith(".")  && !s.endsWith("?") && !s.endsWith("!")) s=s+'.';
                    if (hear("book", s) == 0) continue;

                    try {
                        Thread.sleep(delayMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public int hear(String channel, String m) {
        final int delay = 7, endDelay = 100, tokenMax = 7, tokenMin = 2;
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
        String i = "";
        i += String.join("", Iterables.transform(tokens, new Function<Twokenize.Span, String>() {

            @Override
            public String apply(Twokenize.Span input) {
                String a = "";
                String pattern = "";
                if (input.pattern.equals("word")) {
                    a = input.content.toLowerCase().toString();
                    pattern = "word";
                }
                //TODO apostrophe words
                else if (input.pattern.equals("punct")) {
                    a = input.content;
                    pattern = "word";
                }
                else {
                    return "";
                }
                //else
                  //  a = "\"" + input.content.toLowerCase() + "\"";
                //String r = "<" + a + " --> " + pattern + ">. :|:\n";
                nar.input(new Sentence(Inheritance.make(Term.text(a), Term.get(pattern)), '.', new TruthValue(1.0f, 0.9f), new Stamp(nar.memory, Tense.Present)));
                nar.think(delay);
                //r += "say(" + a + ")!\n";
                //r += delay + "\n";
                return "";
            }
        }));
        //i += "<silence --> hear>. :|: \n" + endDelay + "\n";
        //i += endDelay + "\n";

        System.out.println(nar.time() + " HEAR: " + tokens);
        //System.out.println("HEAR: " + i);

        //nar.input(i);

        return tokens.size();
    }

    public IRCBot(NAR n) throws Exception {

        this.nar = n;
        new AbstractReaction(nar, Events.EXE.class) {

            private String lastMessage = "";

            int minSpokenWords = 3;

            @Override
            public void event(Class event, Object[] args) {
                if (event == Events.EXE.class) {
                    Operator.ExecutionResult er = (Operator.ExecutionResult)args[0];
                    Term[] a = er.getOperation().getArguments().term;

                    if (a.length >= minSpokenWords)  {
                        String m = "";
                        int n = 0;
                        for (int i = 0; i < a.length; i++) {
                            Term x = a[i];
                            if (x.equals(nar.memory.getSelf()))
                                continue;
                            m += x.toString().replace("\"", "").trim() + " ";
                        }
                        m = m.trim();

                        if (!m.equals(lastMessage))
                            say(m);

                        lastMessage = m;
                    }
                    else {
                        //System.out.println("not SAY: " + Arrays.toString(a));

                    }
                }
            }
        };

        /*new BufferedOutput(nar, 1, 5000, 64) {

            @Override
            protected void output(List<OutputItem> buffer) {
               System.out.println(buffer);
            }
        };*/

        //new TextOutput(nar, System.out).setShowErrors(true).setPriorityMin(0.95f);

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
                                if (s!=-1)
                                    hear(channel, line.substring(s + part.length() + 2));

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    public void say(String s) {
        System.out.println("SAY: " + s);
        if ((writer!=null) && (outputting)) {
            try {
                writer.write("PRIVMSG " + channel + " :" + s + "\r\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
