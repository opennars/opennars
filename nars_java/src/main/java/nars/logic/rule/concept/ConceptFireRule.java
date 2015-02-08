package nars.logic.rule.concept;

import nars.logic.FireConcept;
import nars.logic.LogicRule;
import nars.logic.Terms;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.rule.TaskFireTerm;
import reactor.event.Event;


abstract public class ConceptFireRule extends LogicRule<FireConcept> implements TaskFireTerm {

    public ConceptFireRule() {
        super(FireConcept.class, null);
        setAction(this);
    }

    @Override
    public void accept(Event<FireConcept> o) {
        FireConcept f = o.getData();
        if (f==null) return;

        boolean result = apply(f, f.getCurrentTaskLink(), f.getCurrentBeliefLink());
        if (!result) {
            o.recycle();
        }
    }

}

