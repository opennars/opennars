package nars.meter;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.concept.Concept;
import nars.io.Texts;
import nars.term.Term;
import nars.util.meter.Signal;
import nars.util.meter.Signals;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/** snapshot of a Memory's budget at a particular time */
public class MemoryBudgetState extends EnumMap<MemoryBudgetState.Budgeted,Object>  {



    public enum Budgeted {
        //Unitary
        Priority,
        Durability,
        Quality,

        //Aggregate
        ActiveConcepts,
        ActiveConceptPrioritySum,
        ActiveConceptPriorityVariance,
        ActiveTaskLinkPrioritySum,
        //ActiveTaskLinkPrioritySumNormalized, //multiplied by its concept's priority before summing
        ActiveTermLinkPrioritySum,
        //ActiveTermLinkPrioritySumNormalized, //multiplied by its concept's priority before summing
    }

    public MemoryBudgetState() {
        super(Budgeted.class);
    }

    public MemoryBudgetState(Memory m) {
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

    public static Signals on(String prefix, Consumer<MemoryBudgetState> c) {
        Signals s = new Signals() {

            MemoryBudgetState b = new MemoryBudgetState();

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
                        b.get(Budgeted.ActiveConceptPriorityVariance)
                };
            }
        };

        return s;
    }







    public void update(Memory m) {

        SummaryStatistics prisum = new SummaryStatistics();

        double tActiveTaskLinkPriority = 0, tActiveTermLinkPriority = 0;

        for (Concept c : m.getControl()) {
            double p = c.getPriority();

            prisum.addValue(p);

            tActiveTaskLinkPriority += c.getTaskLinks().getPrioritySum();
            tActiveTermLinkPriority += c.getTermLinks().getPrioritySum();
        }

        put(Budgeted.ActiveConceptPrioritySum, prisum.getSum());
        put(Budgeted.ActiveConcepts, prisum.getN());
        put(Budgeted.ActiveConceptPriorityVariance, prisum.getVariance());
        put(Budgeted.ActiveTaskLinkPrioritySum, tActiveTaskLinkPriority);
        put(Budgeted.ActiveTermLinkPrioritySum, tActiveTermLinkPriority);
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
