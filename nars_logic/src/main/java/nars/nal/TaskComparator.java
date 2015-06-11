package nars.nal;

import java.util.Comparator;

/**
 * Compares tasks, and accumulates their budget priority additively
 */
public class TaskComparator implements Comparator<Task> {


    /** duplication mode */
    public enum Merging {

        /** merges the tasks using Accumulate budget formulas (plus priority, merge durability, merge quality) */
        Plus,

        /** merges the tasks using OR budget formula */
        Or,

        /** allows additional copies of equivalent tasks */
        Duplicate
    }

    Merging merging;

    public TaskComparator(Merging mode) {
        super();
        this.merging = mode;
    }

    @Override
    public int compare(final Task o1, final Task o2) {
        if (o1 == o2) return 0;

        if (o1.sentence.equals(o2.sentence)) {
            switch (merging) {
                case Duplicate:
                    return -1;
                case Plus:
                    o1.accumulate(o2);
                    break;
                case Or:
                    o1.orPriority(o2.getPriority());
                    o1.maxDurability(o2.getPriority());
                    o1.maxQuality(o2.getPriority());
                    break;
            }
            o2.merge(o1);
            return 0;
        }

        //o2, o1 = highest first
        final int priorityComparison = Float.compare(o2.getPriority(), o1.getPriority());
        if (priorityComparison != 0)
            return priorityComparison;

        final int complexityComparison = Integer.compare(o1.getTerm().getComplexity(), o2.getTerm().getComplexity());
        if (complexityComparison != 0)
            return complexityComparison;
        else
            return Integer.compare(o1.hashCode(), o2.hashCode());
    }
}
