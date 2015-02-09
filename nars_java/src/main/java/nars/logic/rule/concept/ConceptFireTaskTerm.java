package nars.logic.rule.concept;

import nars.logic.FireConcept;
import nars.logic.LogicRule;
import nars.logic.Terms;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import reactor.event.Event;
import reactor.function.Consumer;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm extends LogicRule<FireConcept> implements Consumer<Event<FireConcept>> {

    public ConceptFireTaskTerm() {
        super(FireConcept.class, null);
        setAction(this);
    }

    abstract public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink);

    @Override
    public void accept(Event<FireConcept> o) {
        FireConcept f = o.getData();
        if (f==null) {
            return;
        }

        if (f.getCurrentBeliefLink()!=null) {
            boolean result = apply(f, f.getCurrentTaskLink(), f.getCurrentBeliefLink());
            if (!result) {
                o.recycle();
            }
        }
    }

}

