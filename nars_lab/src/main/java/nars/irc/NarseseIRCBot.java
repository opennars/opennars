package nars.irc;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.model.impl.Default;
import nars.nal.Task;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by me on 6/20/15.
 */
public class NarseseIRCBot extends IRCBot {


    private final NAR nar;
    int updateMS = 50;

    public static void main(String[] arg) throws Exception {
        new NarseseIRCBot(new NAR(new Default()));
    }


    public static class ThrottledTextOutput extends TextOutput {

        final int windowSize = 32;
        private final float maxMessagesPerSecond;

        long lastSend = 0;
        //DescriptiveStatistics intervals = new DescriptiveStatistics(windowSize);
        DescriptiveStatistics priorities = new DescriptiveStatistics(windowSize);

        public ThrottledTextOutput(NAR n, float maxMessagesPerSecond) {
            super(n);
            this.maxMessagesPerSecond = maxMessagesPerSecond;
        }

        /*
        @Override
        public boolean isEnabled() {
            return true;
        }
        */

        @Override
        protected synchronized void output(Channel channel, Class event, Object... args) {

            if (!(args[0] instanceof Task)) {
                return;
            }

            Task t = (Task)args[0];
            float pri = t.getPriority();

            priorities.addValue(pri);

            long now = System.currentTimeMillis();
            if (now - lastSend < (1000/maxMessagesPerSecond)) {
                //decide if the priority is high enough to break the limit
                double percentile = priorities.getPercentile(pri);
                if (!(percentile < 0.6)) {
                    return;
                }
            }

            lastSend = System.currentTimeMillis();

            super.output(channel, event, args);
        }
    }

    public NarseseIRCBot(NAR n) throws Exception {
        super("irc.freenode.net", "NARcho", "#nars");
        this.nar = n;

        new ThrottledTextOutput(nar, 0.25f) {

            @Override
            protected void output(String prefix, CharSequence s) {
                CharSequence x = s;
                switch (prefix) {
                    case "IN": return;
                    case "OUT":
                        break;
                    default:
                        x = prefix + ": " + s;
                        break;
                }

                send(x.toString());
            }
        }.setOutputPriorityMin(0.5f);

        TextOutput.out(nar);

        System.out.println(nar + " starting");
        while (true) {
            nar.frame();

            Thread.sleep(updateMS);
        }
    }

    @Override
    protected void onMessage(IRCBot bot, String channel, String msg) {
        try {
            nar.input(msg);
        }
        catch (Exception e) { }
    }
}
