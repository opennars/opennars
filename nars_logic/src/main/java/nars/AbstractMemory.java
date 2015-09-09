package nars;

import nars.budget.Budget;
import nars.clock.Clock;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;

/**
 * Created by me on 7/6/15.
 */
public interface AbstractMemory {


    Concept concept(Term t);

    Concept conceptualize(Termed term, Budget budget);

    Clock getClock();

    Param getParam();

    void emit(Class c, Object... ars);

    void removed(Task task, String reason);


    default public long time() {
        return getClock().time();
    }

    int duration();


    default boolean nal(int i) {
        return true;
    }

    Term self();

    long newStampSerial();

}
