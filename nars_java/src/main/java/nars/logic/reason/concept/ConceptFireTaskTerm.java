package nars.logic.reason.concept;

import nars.logic.LogicRule;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.reason.ConceptProcess;
import reactor.event.Event;
import reactor.function.Consumer;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm extends LogicRule<ConceptProcess> implements Consumer<Event<ConceptProcess>> {

    public ConceptFireTaskTerm() {
        super(ConceptProcess.class, null);
    }

    abstract public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink);

    @Override
    public void accept(Event<ConceptProcess> o) {
        ConceptProcess f = o.getData();
        if (f!=null && f.getCurrentBeliefLink()!=null) {
            boolean result = apply(f, f.getCurrentTaskLink(), f.getCurrentBeliefLink());
            if (!result) {
                o.recycle();
            }
        }
    }

}

