package nars.nal;

import java.util.Comparator;

/**
 * Created by me on 4/24/15.
 */
public class TaskComparator implements Comparator<Task> {

    public final static TaskComparator the = new TaskComparator();

    @Override
    public int compare(final Task o1, final Task o2) {
        if (o1 == o2) return 0;

        if (o1.sentence.equals(o2.sentence)) {
            o1.merge(o2);
            o2.merge(o1);
            return 0;
        }

        //o2, o1 = highest first
        final int priorityComparison = Float.compare(o2.getPriority(), o1.getPriority());
        if (priorityComparison != 0)
            return priorityComparison;

        final int complexityComparison = Integer.compare(o1.getTerm().complexity, o2.getTerm().getComplexity());
        if (complexityComparison != 0)
            return complexityComparison;
        else
            return Integer.compare(o1.hashCode(), o2.hashCode());
    }
}
