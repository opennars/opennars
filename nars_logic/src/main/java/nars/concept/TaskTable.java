package nars.concept;

import com.google.common.collect.Iterators;
import javolution.util.function.Equality;
import nars.task.Task;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

/** holds a set of ranked question/quests tasks
 *  top ranking items are stored in the lower indexes so they will be first iterated
 * */
public interface TaskTable extends Iterable<Task> {

    public void setCapacity(int newCapacity);

    /** number of items in this collection */
    public int size();

    public void clear();

    public boolean isEmpty();

    /** attempt to insert a task.
     *
     * @param c the concept in which this occurrs
     * @return:
     *      the input value, 'q' if it it was added to the table
     *      a previous stored task if this was a duplicate
     *
     */
    public Task add(Task q, Equality<Task> e, Concept c);

    /**
     *
     * @return null if no duplicate was discovered, or the first Task that matched if one was
     */
    default public Task getFirstEquivalent(final Task t, final Equality<Task> e) {
        for (final Task a : this) {
            if (e.areEqual(a, t))
                return a;
        }
        return null;
    }

    default public int inputNum() {
        return Iterators.size( Iterators.filter(iterator(), t -> t.isInput() ));
    }

    /** value in 0.,1.0 */
    default public float inputPercent() {
        if (isEmpty()) return 0;
        return ((float)inputNum())/size();
    }

    default public BivariateGridInterpolator getWaveFrequencyConfidenceTime() {
        return null;
    }

    default public UnivariateInterpolator getWaveFrequencyConfidence() {
        return null;
    }

    default public UnivariateInterpolator getWaveConfidenceTime() {
        return null;
    }
}
