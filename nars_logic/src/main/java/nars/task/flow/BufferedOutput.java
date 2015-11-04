//package nars.io;
//
//import nars.Events;
//import nars.Global;
//import nars.NAR;
//import nars.io.out.Output;
//import nars.io.out.TextOutput;
//import nars.task.Task;
//import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
//
//import java.util.*;
//
///**
// * Buffers a stream of output objects (ex: tasks), filtering
// * items according to a cost function.  The resulting stream
// * remains in order but without high-cost items
// * according to constraint parameters limting throughput
// * in both quantity and time.
// */
//abstract public class BufferedOutput extends Output {
//
//
//    final SummaryStatistics itemCosts = new SummaryStatistics();
//
//
//    public static class OutputItem implements Comparable<OutputItem> {
//        public final Class channel;
//        public final Object object;
//        public final float cost;
//
//        public OutputItem(Class channel, Object o, float c) {
//            this.channel = channel;
//            this.object = o;
//            this.cost = c;
//        }
//
//        @Override
//        public int hashCode() {
//            return channel.hashCode() * 37 + object.hashCode();
//        }
//
//        @Override
//        public boolean equals(final Object obj) {
//            if (this == obj) return true;
//            if (obj instanceof OutputItem) {
//                OutputItem oi = (OutputItem) obj;
//                return channel.equals(oi.channel) && object.equals(oi.object);
//            }
//            return false;
//        }
//
//        @Override
//        public int compareTo(final OutputItem o) {
//            if (equals(o)) return 0;
//
//            final float oCost = o.cost;
//            if (oCost == cost) {
//                return -1;
//            }
//
//            //arrange by highest cost first
//            else if (oCost > cost)
//                return 1;
//            else
//                return -1;
//        }
//
//        @Override
//        public String toString() {
//            return object.toString();
//        }
//    }
//
//    final SortedSet<OutputItem> bufferCosts = new TreeSet<>();
//    final LinkedHashSet<OutputItem> buffer = new LinkedHashSet();
//
//    private long lastOutputEvent; //when last the buffer was flushed
//    private boolean showInput;
//    private final NAR nar;
//    private final float maxBudgetToEmit; //upper throughput amount bound
//    float minCyclesPerEmit; //upper throughput frequency bound
//
//    public BufferedOutput(NAR n, float addPerCycle, float minOutputInterval, float maxCostOfBuffer) {
//        this(n, addPerCycle, minOutputInterval, maxCostOfBuffer, false);
//    }
//
//    public BufferedOutput(NAR n, float addPerCycle, float minOutputInterval, float maxCostOfBuffer, boolean includeInput) {
//        super(n);
//
//        this.nar = n;
//        this.showInput = includeInput;
//        this.minCyclesPerEmit = addPerCycle * minOutputInterval;
//        this.maxBudgetToEmit = addPerCycle * maxCostOfBuffer;
//        resetBuffer();
//    }
//
//    protected void resetBuffer() {
//        this.lastOutputEvent = nar.time();
//        itemCosts.clear();
//        buffer.clear();
//        bufferCosts.clear();
//    }
//
//    public void setShowInput(boolean includeInput) {
//        this.showInput = includeInput;
//    }
//
//    //TODO make this a pluggable function
//
//    /**
//     * cost of emitting this signal; return Float.NaN to discard it completely
//     */
//    public float cost(Class event, Object o) {
//
//        if (!showInput && event == Events.IN.class)
//            return Float.NaN;
//
//        //1. prevent subsequent duplicates of existing content but decrease its cost as a way of making it more important to ensure it will be output
//        if (buffer.contains(o)) {
//            if (Global.DEBUG) {
//                boolean found = false;
//                for (OutputItem e : buffer) {
//                    if (e.channel.equals(event) && e.object.equals(o)) {
//                        //discount the cost?
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found)
//                    throw new RuntimeException("duplicate item reported by buffer but it was not actually found");
//            }
//            return Float.NaN;
//        }
//
//
//        float c = 1.0f;
//        if (o instanceof Task) {
//            Task t = (Task) o;
//
//            float conf;
//            if (t.getTruth() != null)
//                conf = (t.getConfidence());
//            else
//                conf = 1.0f;
//
//            c = 1.0f + 0.2f * (1.0f - t.getPriority());
//            c *= 1.0f + 0.02f * (float) Math.sqrt(t.getTerm().complexity());
//            c *= 1.0f + 0.2f * (1.0f - conf);
//            c *= 1.0f + 0.1f * t.getOriginality();
//        }
//
//
//        if (event == Events.Answer.class) {
//            c /= 0.5f;
//        }
//        if (event == Events.ERR.class) {
//            c *= 2.0f;
//        }
//
//        return c;
//    }
//
//    protected void included(OutputItem o) {
//    }
//
//    protected void excluded(OutputItem o) {
//    }
//
//    public void queue(long now, Class channel, Object signal, float cost) {
//        OutputItem i = new OutputItem(channel, signal, cost);
//
//        int b1 = buffer.size();
//        int b2 = bufferCosts.size();
//        if (buffer.add(i)) {
//            if (!bufferCosts.add(i) || (bufferCosts.size() != buffer.size())) {
//                resetBuffer();
//                throw new RuntimeException("buffer fault when trying to add: " + signal); //should have added a unique value to the set and so both should be +1 item
//            }
//            itemCosts.addValue(cost);
//        } else {
//            return; //duplicated
//        }
//
//        if (now - lastOutputEvent < minCyclesPerEmit)
//            return;
//
//        //create output
//        float totalCost = (float) itemCosts.getSum();
//
//        if (totalCost > maxBudgetToEmit) {
//            Iterator<OutputItem> f = bufferCosts.iterator();
//            while ((totalCost > maxBudgetToEmit) && (f.hasNext())) {
//                OutputItem x = f.next();
//                totalCost -= x.cost;
//                buffer.remove(x);
//                excluded(x);
//            }
//        }
//
//        if (totalCost > maxBudgetToEmit) {
//            resetBuffer();
//            throw new RuntimeException("buffer overflow: " + totalCost + " > " + maxBudgetToEmit);
//        }
//
//        if (!buffer.isEmpty()) {
//            List<OutputItem> l = new ArrayList(buffer.size());
//            for (OutputItem oi : buffer) {
//                included(oi);
//                l.add(oi);
//            }
//            output(l);
//        }
//
//        resetBuffer();
//
//    }
//
//    public String toString(Collection<OutputItem> l) {
//        return toString(l, -1);
//    }
//
//    //TODO add max length parameter
//    public String toString(Collection<OutputItem> l, int charLimit) {
//        StringBuilder sb = new StringBuilder();
//
//        Set<String> strings = Global.newHashSet(l.size());
//
//        StringBuilder buffer = new StringBuilder();
//
//        String lastChannel = "";
//        for (OutputItem i : l) {
//
//            String nextChannel = i.channel.getSimpleName();
//
//            buffer.setLength(0);
//            StringBuilder content = TextOutput.append(
//                    buffer, i.channel, i.object, false /* showchannel*/, false /* show stamp */, 0, nar);
//
//
//            String prefix = nextChannel + (": ");
//
//            String entry = prefix + content;
//
//            if (strings.add(entry)) {
//
//                if (!nextChannel.equals(lastChannel)) {
//                    if (!lastChannel.isEmpty())
//                        sb.append("  "); //additional space between channel change, could be a newline also
//                    if (!nextChannel.equals("OUT"))
//                        sb.append(entry); //the entire entry (prefix+content)
//                    else
//                        sb.append(content);
//
//                    lastChannel = nextChannel;
//                } else {
//                    sb.append(content); //just the content
//                }
//
//                sb.append("  ");
//            }
//
//        }
//
//
//        //optional: removing " %1.00;"
//        String s = sb.toString().replace(" %1.00;0.", " 1;");
//
//
//        if (charLimit != -1 && sb.length() > charLimit) {
//            return sb.substring(0, charLimit - 2) + "..";
//        }
//
//        return s;
//    }
//
//    abstract protected void output(List<OutputItem> buffer);
//
//    @Override
//    public void event(Class channel, Object[] args) {
//
//
//        Object signal;
//        if ((args.length >= 1) && (args[0] instanceof Task))
//            signal = args[0];
//        else
//            signal = args;
//
//        float cost = cost(channel, signal);
//        if (Float.isNaN(cost))
//            queue(nar.time(), channel, signal, cost);
//        /*else
//            excluded(signal); */
//    }
//}
