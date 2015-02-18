package nars.logic.reason.concept;

import nars.logic.LogicRule;
import nars.logic.entity.TaskLink;
import nars.logic.reason.ConceptFire;
import reactor.event.Event;
import reactor.function.Consumer;

/**
 * when a concept fires a tasklink but before a termlink is selected
 */
abstract public class ConceptFireTask extends LogicRule<ConceptFire> implements Consumer<Event<ConceptFire>> {

    public ConceptFireTask() {
        super(ConceptFire.class, null);
    }

    abstract public boolean apply(ConceptFire f, TaskLink taskLink);

    @Override
    public void accept(Event<ConceptFire> o) {
        ConceptFire f = o.getData();
        if (f!=null && f.getCurrentBeliefLink()==null) {
            boolean result = apply(f, f.getCurrentTaskLink());
            if (!result) {
                o.setData(null);
            }
        }
    }

}

