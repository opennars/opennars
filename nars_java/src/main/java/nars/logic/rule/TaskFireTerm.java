package nars.logic.rule;

import nars.logic.FireConcept;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import reactor.event.Event;
import reactor.event.selector.*;
import reactor.function.Consumer;

/**
 * simple if (true) condition where the test for boolean truth is implemented in subclass
 * this is the least efficient type of rule because it must be compared manualy each
 * time something is fired.
 * when possible, use the boolean predicate expression forms which can be unified.
 */
abstract public interface TaskFireTerm extends Consumer<Event<FireConcept>>, AbstractTaskFireTerm {

    @Override
    default public void accept(Event<FireConcept> o) {
        FireConcept f = o.getData();
        if (f==null) return;

        boolean result = apply(f, f.getCurrentTaskLink(), f.getCurrentBeliefLink());
        if (!result) {
            o.recycle();
        }
    }



//    abstract public static class If implements Selector {
//
//        public If() {
//            super();
//        }
//
//
//
//        public HeaderResolver getHeaderResolver() { return null; }
//
//        public static enum Match {
//            TRUE,  //this event matches this conditions
//            FALSE,  //this event does not match this condition
//            END, //this event should be terminated from further processing
//            //END_TRUE //should be terminated AFTER this is handled
//        }
//
//        @Override
//        public Object getObject() {
//            return this;
//        }
//
//        @Override
//        public boolean matches(Object key) {
//            if (key instanceof FireConcept) {
//                return eval((FireConcept)key);
//            }
//            return false;
//        }
//
//        public boolean eval(FireConcept f) {
//
//            Match m = match(f, f.getCurrentTaskLink(), f.getCurrentBeliefLink());
//
//            if (m == Match.TRUE) return true;
//            else {
//                if (m == Match.END) {
//                    //o.recycle();
//                }
//                return false;
//            }
//        }
//
//        abstract public Match match(FireConcept f, TaskLink taskLink, TermLink termLink);
//
//
//        @Override
//        public String toString() {
//            return getClass().getName();
//        }
//    }

//    abstract public static class Then implements Consumer<Event<FireConcept>> {


}
