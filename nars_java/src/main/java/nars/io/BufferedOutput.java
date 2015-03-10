package nars.io;

import nars.core.NAR;
import nars.logic.entity.Task;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;

/**
 * Buffers a stream of output objects (ex: tasks), filtering
 * items according to a cost function.  The resulting stream
 * remains in order but without high-cost items
 * according to constraint parameters limting throughput
 * in both quantity and time.
*/
abstract public class BufferedOutput extends Output {


    final SummaryStatistics itemCosts = new SummaryStatistics();


    static class OutputItem implements Comparable<OutputItem> {
        public Object object;
        public float cost;

        public OutputItem(Object o, float c) {
            this.object = o;
            this.cost = c;
        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return object.equals(obj);
        }

        @Override
        public int compareTo(final OutputItem o) {
            if (this == o) return 0;
            final float oCost = o.cost;
            if (oCost == cost)
                return o.hashCode() - hashCode();

            //arrange by highest cost first
            else if (oCost > cost)
                return 1;
            else
                return -1;
        }
    }

    final SortedSet<OutputItem> bufferCosts = new TreeSet<>();
    final LinkedHashSet<OutputItem> buffer = new LinkedHashSet();

    private long lastOutputEvent; //when last the buffer was flushed

    private final NAR nar;
    private final float maxBudgetToEmit; //upper throughput amount bound
    float minCyclesPerEmit; //upper throughput frequency bound

    public BufferedOutput(NAR n, float addPerCycle, float minOutputInterval, float maxCostOfBuffer) {
        super(n);

        this.nar = n;
        this.minCyclesPerEmit = addPerCycle * minOutputInterval;
        this.maxBudgetToEmit = addPerCycle * maxCostOfBuffer;
        resetBuffer();
    }

    protected void resetBuffer() {
        this.lastOutputEvent = nar.time();
        itemCosts.clear();
        buffer.clear();
        bufferCosts.clear();
    }


    //TODO make this a pluggable function
    /** cost of emitting this signal; return Float.NaN to discard it completely */
    public float cost(Class event, Object o) {

        //1. prevent subsequent duplicates of existing content but decrease its cost as a way of making it more important to ensure it will be output
        if (buffer.contains(o)) {
            boolean found = false;
            for (OutputItem e : buffer) {
                if (e.object.equals(o)) {
                    //discount the cost?
                    found = true;
                    break;
                }
            }
            if (!found)
                throw new RuntimeException("duplicate item reported by buffer but it was not actually found");
            return Float.NaN;
        }

        if (o instanceof Task) {
            Task t = (Task)o;
            if (t.sentence != null)
                return (1f + (1f - t.getPriority())) * (float)Math.sqrt(t.getTerm().complexity);
        }

        return 1;
    }

    protected void included(OutputItem o) { }
    protected void excluded(OutputItem o) { }

    public void queue(long now, Object signal, float cost) {
        OutputItem i = new OutputItem(signal, cost);

        buffer.add(i);
        bufferCosts.add(i);
        itemCosts.addValue(cost);
        if (bufferCosts.size() != buffer.size())
            throw new RuntimeException("buffer fault");

        if (now - lastOutputEvent >= minCyclesPerEmit) {
            //create output
            float totalCost = (float)itemCosts.getSum();
            if (totalCost > maxBudgetToEmit) {
                Iterator<OutputItem> f = bufferCosts.iterator();
                while ((totalCost > maxBudgetToEmit) && (f.hasNext())) {
                    OutputItem x = f.next();
                    totalCost -= x.cost;
                    buffer.remove(x);
                    excluded(x);
                }
            }

            if (!buffer.isEmpty()) {
                List<Object> l = new ArrayList(buffer.size());
                for (OutputItem oi : buffer) {
                    included(oi);
                    l.add(oi.object);
                }
                output(l);
            }

            resetBuffer();
        }

    }

    abstract protected void output(List<Object> buffer);

    @Override
    public void event(Class event, Object[] args) {


        Object signal;
        if ((args.length >= 1) && (args[0] instanceof Task))
            signal = ((Task) args[0]);
        else
            signal = args;

        float cost = cost(event, signal);
        if (Float.isFinite(cost))
            queue(nar.time(), signal, cost);
        /*else
            excluded(signal); */
    }
}
