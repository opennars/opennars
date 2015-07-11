package nars.meter;

import nars.Memory;
import nars.concept.Concept;
import nars.io.Texts;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.EnumMap;
import java.util.Iterator;

/** snapshot of a Memory's budget at a particular time */
public class BudgetStatus extends EnumMap<BudgetStatus.Budgeted,Object> {

    public enum Budgeted {
        ActiveConcepts,
        ActiveConceptPrioritySum,
        ActiveConceptPriorityVariance,
        ActiveTaskLinkPrioritySum,
        //ActiveTaskLinkPrioritySumNormalized, //multiplied by its concept's priority before summing
        ActiveTermLinkPrioritySum,
        //ActiveTermLinkPrioritySumNormalized, //multiplied by its concept's priority before summing
    }

    public BudgetStatus(Memory m) {
        super(Budgeted.class);
        add(m);
    }

    public void add(Memory m) {

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
