package nars.logic.reason.concept;

import nars.logic.LogicRule;
import nars.logic.entity.TaskLink;
import nars.logic.reason.ConceptProcess;
import reactor.event.Event;
import reactor.function.Consumer;

/**
 * when a concept fires a tasklink but before a termlink is selected
 */
abstract public class ConceptFireTask extends LogicRule<ConceptProcess> implements Consumer<Event<ConceptProcess>> {

    public ConceptFireTask() {
        super(ConceptProcess.class, null);
    }

    abstract public boolean apply(ConceptProcess f, TaskLink taskLink);

    @Override
    public void accept(Event<ConceptProcess> o) {
        ConceptProcess f = o.getData();
        if (f!=null && f.getCurrentTermLink()==null) {
            boolean result = apply(f, f.getCurrentTaskLink());
            if (!result) {
                o.setData(null);
            }
        }
    }

}

