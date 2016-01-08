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

    /*
    testname="belief_table_full_revision"
tester="patham9_"
narnick = "mr_nars"
include_comments = True

history = """
[17:44] <patham9_> <a --> b>?
[17:44] <patham9_> <a --> b>.
[17:44] <mr_nars> $0.05;0.90;1.00$ <a --> b>? :6219::$0.50;0.80;0.95$ <a --> b>. :8470: %1.00;0.90%
[17:44] <patham9_> <a --> b>.
[17:44] <patham9_> <a --> b>.
[17:44] <mr_nars> $0.02;0.90;1.00$ <a --> b>? :6219::$0.49;0.40;0.99$ <a --> b>. :12255: %1.00;0.97%
[17:44] <patham9_> <a --> b>.
[17:44] <mr_nars> $0.02;0.90;1.00$ <a --> b>? :6219::$0.49;0.39;0.99$ <a --> b>. :13567: %1.00;0.98%
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>.
[17:45] <patham9_> <a --> b>?
[17:45] <mr_nars> $0.02;0.90;1.00$ <a --> b>? :23033::$0.48;0.38;0.99$ <a --> b>. :18941: %1.00;0.98%
[17:45] <patham9_> belief table is now full
[17:45] <SquareOfTwo> >so I can create unit tests
[17:45] <SquareOfTwo> :S
[17:45] <SquareOfTwo> write first the tests, then the code :/
[17:45] <SquareOfTwo> (i never do this for experimental code btw)
[17:45] <patham9_> and still it is able to revise to revise with new beliefs:
[17:45] <patham9_> <a --> b>. %0%
[17:45] <patham9_> <a --> b>. %0%
[17:45] <patham9_> <a --> b>. %0%
[17:46] <patham9_> <a --> b>?
[17:46] <mr_nars> $0.02;0.90;1.00$ <a --> b>? :77114::$0.23;0.18;0.60$ <a --> b>. :73865: %0.60;0.98%
[17:46] <patham9_> <a --> b>. %0%
[17:46] <patham9_> <a --> b>?
[17:46] <mr_nars> $0.02;0.90;1.00$ <a --> b>? :84094::$0.27;0.22;0.51$ <a --> b>. :82864: %0.51;0.98%
[17:46] <patham9_> see?
[17:46] <patham9_> <a --> b>.
[17:46] <patham9_> <a --> b>?
[17:46] <mr_nars> $0.02;0.90;1.00$ <a --> b>? :91756::$0.32;0.26;0.59$ <a --> b>. :89779: %0.59;0.98%
"""

print """@Test
public void """+testname+"() throws Narsese.NarseseException { \nTestNAR tester = test();"

#input the things the user input
lines = history.split("\n")
for Li in [h for h in lines if h.replace(" ","")!=""]:
    Li = Li.split("] ")[1]
    nick = Li.split(">")[0].split("<")[1]
    Input = "> ".join(Li.split("> ")[1:])
    if nick == tester and True in [z in Input for z in ["<",">","("]]:
        print "nar.input(\""+Input+"\");"
    elif include_comments and nick == tester:
        print "//"+Input

#determine what to test for:
for Li in reversed(lines):
    if narnick in Li:
        truth = Li.split("%")[1].replace(";","f,")+"f"
        expected_truth_typ = "mustBelieve"
        splitter = ". "
        if ">!" in Li or ")!" in Li:
            expected_truth_typ = "mustDesire"
            splitter = "! "
        term = Li.split(splitter)[0].split("$ ")[2]
        print "tester."+expected_truth_typ+"(cycles,"+term+","+truth+");\n}"
        break


#http://sagecell.sagemath.org/?z=eJzlVt9v4kYQfo_E_zDZJhIIzrENxj8SSFv1Hk46XaVe3gJCxixle2bN7a7D5ar-751ZG8UJCSRV-1QezNrzzcw3szOza7g2Ml3zEZvzXPDlzKTznM-WZZ7PFL8TWhSStU4MwrgasU1qVuk6nuEnmSopsi8wArZWM3zT-FHILC8XfJYV6zWXRqP0RpW8ddI6WQltCnVPeIbIWy9MBoMpXO1MjuEqhXfvxjAfXx8WOw1x7XoMZ67jBpeuE7uXnuO6Zw1rkAx9L04ShAQuQiJ6xEED4kASDUI3gXPStVbOX8_hLRT9oxQHMUIGlkT8mKLn-0HQ4Bi-geM_IdGPnyPRD4Zhg0S0IxEcJvF_E183xK9Lvt93-_0q-xFlP3ou-1E88I5nv-plsL0MQoMstkAt3QB__lqmiv-6vNkWYxjrAj5AlkrIFE8Nh1IKA9T0-iWV5PNLkq0SaGEplEYTK16Z6dFS2vesWHBILl5SbwuQ_I4rWBQIR_LLQgH_tuFK0EhJ88rA3Gw7z8aeygVoI_IcMARUtzkwBdhp1lxthVmhq22dLZ0c2W84d4_V-tsgw8OVM3x15YSh5w1s5fh9xHhUOUP3ceWE_WhIw4MkjyrnBRb_CdFo4MY10RAxPgED78kg9qPhwBINvANENefXRyL4t9nHXhgMLfs-YfwhsX_SoFEchnHFPt6xt-dd62SjhDR0-P14gx2B7-U8FxncFWJBX7umPoq7rN3BylfFVsMnZMU1d-r_998yvjF4JMOfMJFk5tNPv0F1OOPBSot259J6-0HITVk34ErI37VdlhqBVtI6yYXkdEDXB7OjN7kwbTaRDBuLmu6jQCjcrmwHrmhdqYglrBzFN3ma8TYD1mOsczpibIodBPhDvRE-dganwDq33rSS1VeGB-kYhe5093bVgH6wAeB1YQzM-aMQst3QsjaTaaeCIqPK8GiXDJoCdPOwEXynZ2WOQrGvt-irh957DBnumNOv3iasB8cmqj3BvbHKXTZhNrsEw5mxhL3rDrl9zGTf8sVFbc9u04IjaI15he0qNTSeSI94Js1dUDQSNV-07RZ0koe462sYYj6KhjOjShxvzUyf29w-bNwlxr7ssU6XLdmDHk3azPDFzBqYmfuNveGV2vxMY_KON7DWcFV6zIGGAHmx8SmrSAEGgQXC9igedvcL10I1vT31eNr0SElsRrsDNovrjIrGn-5tSLVRDuvuk8FezO6znOse9adadxktSNrFUpjIvxoU5nh0fvkbNG8H4w==&lang=sage

     */

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
            nar.input(msg);
        }

    }
}
