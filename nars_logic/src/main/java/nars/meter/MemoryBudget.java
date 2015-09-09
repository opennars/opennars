package nars.meter;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.io.Texts;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.task.Sentence;
import nars.term.Term;
import nars.util.meter.Signal;
import nars.util.meter.Signals;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/** snapshot of a Memory's budget at a particular time */
public class MemoryBudget extends EnumMap<MemoryBudget.Budgeted,Object>  {


    public int getInt(Budgeted b) {
        Integer i = (Integer)get(b);
        if (i != null)
            return i.intValue();
        return 0;
    }

    public long getLong(Budgeted b) {
        Long l = (Long) get(b);
        if (l!=null)
            return l.longValue();
        return 0;
    }

    public double getDouble(Budgeted b) {
        Double l = (Double ) get(b);
        if (l!=null)
            return l.doubleValue();
        return 0;
        //return (double)get(b);
    }

    public Object getDoubleFinite(Budgeted b, double defaultVal) {
        double d = getDouble(b);
        if (Double.isFinite(d)) return d;
        return defaultVal;
    }

    public enum Budgeted {
        //Unitary
        Priority,
        Durability,
        Quality,

        //Aggregate
        ActiveConcepts,

        ActiveConceptPrioritySum,
        ActiveConceptPriorityStdDev,

        ActiveTaskLinkPriorityStdDev,
        ActiveTaskLinkPrioritySum,
        //ActiveTaskLinkPrioritySumNormalized, //multiplied by its concept's priority before summing
        ActiveTermLinkPriorityStdDev,
        ActiveTermLinkPrioritySum,
        //ActiveTermLinkPrioritySumNormalized, //multiplied by its concept's priority before summing
    }

    public MemoryBudget() {
        super(Budgeted.class);
    }

    public MemoryBudget(Memory m) {
        this();
        update(m);
    }

    public static Signals onConcept(NARMetrics nm, Term termConcept) {
        Memory m = nm.nar.memory;
        final String prefix = termConcept.toString();
        Signals s = new Signals() {


            @Override
            public List<Signal> getSignals() {
                return Lists.newArrayList(
                        new Signal(prefix + "_Priority"),
                        new Signal(prefix + "_Durability"),
                        new Signal(prefix + "_Quality")
                        //...
                );
            }

            final Object[] empty = { 0, 0, 0 };

            @Override
            public Object[] sample(Object key) {
                Concept c = m.concept(termConcept);
                if (c == null) {
                    return empty;
                }
                return c.toBudgetArray();
            }
        };
        nm.metrics.add(s);
        return s;
    }



    public static Signals on(String prefix, NARMetrics m) {
        Signals s = on(prefix, b -> b.update(m.nar.memory));
        m.metrics.add(s);
        return s;
    }

    public static Signals on(String prefix, Consumer<MemoryBudget> c) {
        Signals s = new Signals() {

            MemoryBudget b = new MemoryBudget();

            @Override
            public List<Signal> getSignals() {
                return Lists.newArrayList(
                        new Signal(prefix + "_ActiveConceptPrioritySum"),
                        new Signal(prefix + "_ActiveConceptPriorityVariance")
                        //...
                );
            }
            @Override
            public Object[] sample(Object key) {
                c.accept(b);
                return new Object[] {
                        b.get(Budgeted.ActiveConceptPrioritySum),
                        b.get(Budgeted.ActiveConceptPriorityStdDev)
                };
            }
        };

        return s;
    }


    final SummaryStatistics prisum = new SummaryStatistics();
    final Mean termLinkStdDev = new Mean();
    final Mean taskLinkStdDev = new Mean();

    public /*synchronized*/ void update(Memory m) {

        prisum.clear();
        termLinkStdDev.clear();
        taskLinkStdDev.clear();

        final double[] tActiveTaskLinkPriority = {0};
        final double[] tActiveTermLinkPriority = {0};

        StandardDeviation s = new StandardDeviation();

        m.getCycleProcess().forEachConcept(c -> {
            double p = c.getPriority();

            prisum.addValue(p);

            Bag<Sentence, TaskLink> tasklinks = c.getTaskLinks();
            tActiveTaskLinkPriority[0] += tasklinks.getPrioritySum();
            if (tasklinks.size() > 1)
                taskLinkStdDev.increment( tasklinks.getStdDev(s) );

            Bag<TermLinkKey, TermLink> termlinks = c.getTermLinks();
            tActiveTermLinkPriority[0] += termlinks.getPrioritySum();
            if (termlinks.size() > 1)
                termLinkStdDev.increment( termlinks.getStdDev(s) );
        });

        long N = prisum.getN();
        put(Budgeted.ActiveConceptPrioritySum, prisum.getSum());
        put(Budgeted.ActiveConcepts, N);
        put(Budgeted.ActiveConceptPriorityStdDev, prisum.getStandardDeviation());

        put(Budgeted.ActiveTaskLinkPrioritySum, tActiveTaskLinkPriority[0]);
        put(Budgeted.ActiveTaskLinkPriorityStdDev, taskLinkStdDev.getResult());

        put(Budgeted.ActiveTermLinkPrioritySum, tActiveTermLinkPriority[0]);
        put(Budgeted.ActiveTermLinkPriorityStdDev, termLinkStdDev.getResult());
    }

    @Override
    public String toString() {
        Iterator<Entry<Budgeted,Object>> i = entrySet().iterator();
        if (! i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<Budgeted,Object> e = i.next();
            Budgeted key = e.getKey();
            Object value = e.getValue();
            if (value instanceof Number)
                value = Texts.n3(((Number) value).floatValue());
            sb.append(key);
            sb.append('=');
            sb.append(value);
            if (! i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
}
