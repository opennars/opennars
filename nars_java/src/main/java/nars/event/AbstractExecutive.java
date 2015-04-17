package nars.event;

import nars.operate.AbstractOperator;
import nars.Events;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.Task;

/**
*/
abstract public class AbstractExecutive extends AbstractOperator {


    @Override
    public void onEnabled(NAR n) {

    }

    @Override
    public void onDisabled(NAR n) {

    }

    @Override
    public Class[] getEvents() {
        return new Class[] { Events.DecideExecution.class };
    }


    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.DecideExecution.class) {
            decide(((Concept)args[0]), ((Task)args[1]));
        }
    }

    /** returns boolean for chaining executives together */
    abstract protected boolean decide(Concept c, Task executableTask);


}
