package nars.concept.util;

import com.gs.collections.api.block.procedure.Procedure2;
import javolution.util.function.Equality;
import nars.Memory;
import nars.budget.Budget;
import nars.task.Task;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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


    Task add(Task t, Equality<Task> equality, Procedure2<Budget, Budget> duplicateMerge, Memory m);

    /**
     * @return null if no duplicate was discovered, or the first Task that matched if one was
     */
    default Task getFirstEquivalent(final Task t, final Equality<Task> e) {
        for (final Task a : this) {
            if (e.areEqual(a, t))
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
        for (final Task t : this) {
            recip.accept(t);
            if (--maxPerConcept == 0) break;
        }
    }


    public default void writeValues(ObjectOutput output) throws IOException {
        int s = size(), c = getCapacity();
        try {
            output.writeInt(s);
            output.writeInt(c);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int written[] = new int[] { 0 }; //HACK to prevent writing more than the specified
        forEach(t -> {
            if (written[0] < s) {
                try {
                    output.writeObject(t);
                    written[0] = written[0] + 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    default public <T> void readValues(ObjectInput input) throws IOException {


        try {
            int num = input.readInt();
            int cap = input.readInt();
            setCapacity(cap);
            for (int i = 0; i < num; i++) {
                Task t = (Task) input.readObject();
                add(t);
            }
        } catch (Exception e) {
            System.err.println(e);
            //e.printStackTrace();
        }
    }

    boolean add(Task t);

    boolean contains(Task t);

}
