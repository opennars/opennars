package nars.concept.util;

import nars.Memory;
import nars.budget.BudgetMerge;
import nars.task.Task;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * holds a set of ranked question/quests tasks
 * top ranking items are stored in the lower indexes so they will be first iterated
 */
public interface TaskTable extends Iterable<Task> {

    int getCapacity();

    void setCapacity(int newCapacity);

    /**
     * number of items in this collection
     */
    int size();

    void clear();

    boolean isEmpty();

    /**
     * attempt to insert a task.
     *
     * @return: the input task itself, it it was added to the table
     * an existing equivalent task if this was a duplicate
     */


    Task add(Task t, BiPredicate<Task,Task>  equality, BudgetMerge duplicateMerge, Memory m);

    /**
     * @return null if no duplicate was discovered, or the first Task that matched if one was
     */
    default Task getFirstEquivalent(Task t, BiPredicate<Task,Task>  e) {
        for (Task a : this) {
            if (e.test(a, t))
                return a;
        }
        return null;
    }


    default BivariateGridInterpolator getWaveFrequencyConfidenceTime() {
        return null;
    }

    default UnivariateInterpolator getWaveFrequencyConfidence() {
        return null;
    }

    default UnivariateInterpolator getWaveConfidenceTime() {
        return null;
    }

    default void top(int maxPerConcept, Consumer<Task> recip) {
        int s = size();
        if (s < maxPerConcept) maxPerConcept = s;
        for (Task t : this) {
            recip.accept(t);
            if (--maxPerConcept == 0) break;
        }
    }

    boolean add(Task t);

    boolean contains(Task t);

}
