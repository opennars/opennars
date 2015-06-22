package nars.irc;

import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.io.nlp.Twenglish;
import nars.io.out.TextOutput;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.rdfowl.NQuadsInput;
import nars.util.language.Twokenize;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

/**
 * Created by me on 6/20/15.
 */
public class NarseseIRCBot extends IRCBot {


    private final NAR n;
    private final ThrottledTextOutput throttle;
    int updateMS = 3;

    public static void main(String[] arg) throws Exception {
        new NarseseIRCBot(new Default());
    }


    public static class ThrottledTextOutput extends TextOutput implements Runnable {

        final int windowSize = 64;
        private float maxMessagesPerSecond;
        private float idealMessagesPerSecond;

        float minPercentile = 5; //cutoff, high pass

        long lastSend = 0;
        //DescriptiveStatistics intervals = new DescriptiveStatistics(windowSize);
        DescriptiveStatistics prioritiesSendable = new DescriptiveStatistics(windowSize);
        //DescriptiveStatistics prioritiesSent = new DescriptiveStatistics(windowSize);

        public ThrottledTextOutput(NAR n, float idealMsgPerSec, float maxMessagesPerSecond) {
            super(n);
            this.idealMessagesPerSecond = idealMsgPerSec;
            this.maxMessagesPerSecond = maxMessagesPerSecond;
        }

        /*
        @Override
        public boolean isEnabled() {
            return true;
        }
        */

        @Override
        public void run() {
            //check for pending items and try send them
        }

        @Override
        protected synchronized boolean output(Channel channel, Class event, Object... args) {

            if (!(args[0] instanceof Task)) {
                return false;
            }

            Task t = (Task)args[0];
            float pri = t.summary();

            prioritiesSendable.addValue(pri);

            long now = System.currentTimeMillis();
            float elapsed = now - lastSend;
            float idealDelayMS = 1000.0f / idealMessagesPerSecond;

            boolean sending = true;
            if (elapsed < (1000f/maxMessagesPerSecond))
                sending = false;
            if (elapsed < idealDelayMS) {
                //decide if the priority is high enough to break the limit



                double percentileNeeded = (1.0 - (elapsed / idealDelayMS)) * 100f;

                if (percentileNeeded > 100) percentileNeeded = 100;
                if (percentileNeeded < minPercentile) percentileNeeded = minPercentile;

                double priNeeded = prioritiesSendable.getPercentile(percentileNeeded);

                //System.out.println(pri + " % " + priNeeded + " "  + percentileNeeded + " " + elapsed);
                if (!(pri < priNeeded)) {
                    sending = false;
                }
            }

            if (!sending) {
                buffer(channel, event, args );
            }
            else {
                if (super.output(channel, event, args)) {
                    lastSend = System.currentTimeMillis();
                    return true;
                }
            }

            return false;
        }

        private void buffer(Channel channel, Class event, Object[] args) {
            //TODO

        }

        public void set(float ideal, float max) {
            this.idealMessagesPerSecond = ideal;
            this.maxMessagesPerSecond = max;
        }

        //TODO add a 'demand' factor that offsets cost, like cost/benefit
        public static class OutputItem implements Comparable<OutputItem> {
            public final Class channel;
            public final Object object;
            public final float cost;

            public OutputItem(Class channel, Object o, float c) {
                this.channel = channel;
                this.object = o;
                this.cost = c;
            }

            @Override
            public int hashCode() {
                return channel.hashCode() * 37 + object.hashCode();
            }

            @Override
            public boolean equals(final Object obj) {
                if (this == obj) return true;
                if (obj instanceof OutputItem) {
                    OutputItem oi = (OutputItem)obj;
                    return channel.equals(oi.channel) && object.equals(oi.object);
                }
                return false;
            }

            @Override
            public int compareTo(final OutputItem o) {
                if (equals(o)) return 0;

                final float oCost = o.cost;
                if (oCost == cost) {
                    return -1;
                }

                //arrange by highest cost first
                else if (oCost > cost)
                    return 1;
                else
                    return -1;
            }

            @Override
            public String toString() {
                return object.toString();
            }
        }
    }

    public synchronized void reset() throws Exception {
        n.reset();

        new NQuadsInput(n, "/home/me/Downloads/dbpedia.n4", 0.94f /* conf */) {

            @Override
            protected void believe(Compound assertion) {
                float freq = 1.0f;
                float beliefConfidence = 0.95f;

                //insert with zero priority to bypass main memory go directly to subconcepts
                n.believe(0f, Global.DEFAULT_JUDGMENT_DURABILITY/4f, assertion, Tense.Eternal, freq, beliefConfidence);
            }
        };


        n.runWhileNewInput(1);
        n.frame(1); //one more to be sure

    }

    public NarseseIRCBot(Default d) throws Exception {
        super("irc.freenode.net", "NARchy", "#nars");

        d.setInternalExperience(null);

        d.inputsMaxPerCycle.set(1024);
        d.setTermLinkBagSize(32);
        d.conceptsFiredPerCycle.set(16);


        this.n = new NAR(d);
        n.memory.setSelf(Atom.the(nick));

        reset();



        throttle = new ThrottledTextOutput(n, 0.1f, 0.25f) {

            @Override
            protected boolean output(String prefix, CharSequence s) {
                CharSequence x = s;
                switch (prefix) {
                    case "IN": return false;
                    case "OUT":
                        break;
                    default:
                        x = prefix + ": " + s;
                        break;
                }

                return send(x.toString());
            }
        };



        //TextOutput.out(n);

        System.out.println(n + " starting");

        System.out.println(n.memory.concepts.size() + " concepts loaded");

        while (true) {
            try {
                n.frame();
            }
            catch (Exception e) {
                e.printStackTrace();
                n.reset();
                send(e.toString() + "... resetting.");
            }

            Thread.sleep(updateMS);
        }
    }

    @Override
    protected void onMessage(IRCBot bot, String channel, String nick, String msg) {
        if (msg.equals("quiet()!")) {
            throttle.set(1f/(60*10), 1f/(60*5));
            send("now quiet. use 'ready()!' for noise");
        }
        else if (msg.equals("ready()!")) {
            throttle.set(0.05f, 0.15f);
            send("ready. use 'quiet()!' to stfu");
        }
        else if (msg.equals("status()!")) {
            throttle.set(0.1f, 0.25f);
            send(n.memory.concepts.size() + " concepts total");
        }
        else if (msg.equals("reset()!")) {
            send("resetting..");
            try {
                reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            send("ready.");
        }
        else {
            try {
                Task t = n.task(msg);
                n.input(t);
            } catch (Throwable e) {

                if (msg.indexOf('>')==-1 && msg.indexOf('<') == -1 && msg.indexOf('(') == -1) {
                    List<Twokenize.Span> sp = Twokenize.tokenize(msg);

                    List<Term> ll = Lists.transform(sp, x -> Twenglish.spanToTerm(x));
                    List<List<Term>> m = Lists.partition(ll, 8);
                    for (List<Term> l : m) {


                        /*Implication subj = Implication.make(Product.make(Atom.the(nick), Atom.the("say")),
                                cc, TemporalRules.ORDER_FORWARD);*/
                        Similarity subj = Similarity.make(
                                Product.make( Atom.the("say"), Atom.the(nick) ),
                                SetExt.make(Conjunction.make(l, TemporalRules.ORDER_FORWARD)));
                        if (subj != null) {
                            Task b = n.believe(
                                    Global.DEFAULT_JUDGMENT_PRIORITY / 8f,
                                    subj,
                                    n.time(),
                                    0.9f, 0.85f / (1 + m.size()));
                            System.err.println(b);
                        }
                    }
                }

            }
        }
    }
}
