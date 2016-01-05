package nars.irc;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import nars.Global;
import nars.NAR;
import nars.NARLoop;
import nars.Video;
import nars.guifx.NARide;
import nars.nar.Default;
import nars.op.io.say;
import nars.term.Atom;
import nars.term.Term;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by me on 6/20/15.
 */
public class NLPIRCBot extends IRCBot {


    private NAR nar;

    public NLPIRCBot() throws Exception {
        super("irc.freenode.net", "mr_nars", "#nars");

    }

    public static void main(String[] args) throws Exception {
        Global.DEBUG = false;


        /*DNARide.show(n.loop(), (i) -> {
        });*/

        NLPIRCBot i = new NLPIRCBot();
        i.go();
    }

    public void loop(File corpus, int lineDelay) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                nar.frame();
               /* List<String> lines = null;
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

                }*/
            }
        }).start();

    }

   /* public void read(String[] sentences, int delayMS, float priority) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (String s : sentences) {

                    s = s.trim();
                    if (s.length() < 2) continue;

                    if (!s.endsWith(".")  && !s.endsWith("?") && !s.endsWith("!")) s=s+'.';
                    if (hear("book", s, priority, delayMS) == 0) continue;

                }
            }
        }).start();
    }*/


   /* public int hear(String channel, String m, float priority, long wordDelay) {
        final int delay = 25, endDelay = 1000, tokenMax = 16, tokenMin = 1;
        List<Twokenize.Span> tokens = Twokenize.twokenize(m);
        int nonPunc = Iterables.size(Iterables.filter(tokens, new Predicate<Twokenize.Span>() {

            @Override
            public boolean apply(Twokenize.Span input) {
                return !input.pattern.equals("punct");
            }
        }));

        if (nonPunc > tokenMax) return 0;
        if (nonPunc < tokenMin) return 0;



        String i = "<language --> hear>. :|: \n " + delay + "\n";

        Iterable<String> s = Iterables.transform(tokens, new Function<Twokenize.Span, String>() {

            @Override
            public String apply(Twokenize.Span input) {
                String a = "";
                String pattern = "";
                Term wordTerm;
                if (input.pattern.equals("word")) {
                    a = input.content.toLowerCase().toString();
                    wordTerm = Atom.the(a);
                    pattern = "word";
                }
                TODO apostrophe words
                else if (input.pattern.equals("punct")) {
                    String b = input.content;
                    wordTerm = Atom.quote(b);

                    a = input.content;
                    pattern = "word";
                }
                else {
                    return "";
                }
                else
                a = "\"" + input.content.toLowerCase() + "\"";
                String r = "<" + a + " --> " + pattern + ">. :|:\n";

                Term tt = Inheritance.make(wordTerm, Term.get(pattern));
                char punc = '.';

                Term tt = Operation.make(nar.memory.operate("^say"), new Term[] {wordTerm});
                char punc = '!';

                nar.input(new Sentence(tt, punc, new TruthValue(1.0f, 0.9f), new Stamp(nar.memory, Tense.Present)));
                nar.think(delay);
                r += "say(" + a + ")!\n";
                r += delay + "\n";
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (a.isEmpty()) return "";
                return "<{\"" + a + "\"}-->WORD>.";
                return "(say, \"" + a + "\", " + channel + "). :|:";
            }
        });
        String xs = "say()!\n" + delay + "\n"; clear the buffer before
        for (String w : s) {
            String xs = "$" + Texts.n2(priority) + "$ " + w + "\n";

            System.err.println(xs);
            nar.input(xs);

            try {
                Thread.sleep(wordDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        System.out.println(nar.time() + " HEAR: " + tokens);
        System.out.println("HEAR: " + i);

        String i = "<(*," + c + ") --> PHRASE>.";
        nar.input(i);
        String j = "<(&/," + c + ") --> PHRASE>. :|:";
        nar.input(j);

        try {
            Thread.sleep(endDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return tokens.size();
    }*/


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
                send(channel, buffer);
            }
            buffer = "";
        }

    }

    public void go() {
        if(oldnar!=null) {
            oldnar.stop();
        }
        nar = new Default(1000, 1, 1, 3);
        nar.memory.eventExecute.on(c -> {
            send(c.getTask().toString());
        });

        nar.memory.eventAnswer.on(c -> {
            send(c.toString());
        });

        oldnar = nar.loop(1000);

      //  NARide.show(oldnar,p -> {});
    }

    static NARLoop oldnar = null;
    @Override
    protected void onMessage(IRCBot bot, String channel, String nick, String msg) {
        if (msg.equals("RESET")) {
            go();
        } else {
            if(!msg.contains("$")) {
                nar.input(msg);
            }
        }

    }
}
