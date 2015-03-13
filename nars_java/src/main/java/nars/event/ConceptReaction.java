package nars.event;


import nars.core.Events;
import nars.core.NAR;
import nars.logic.entity.Concept;
import nars.logic.reason.ConceptProcess;

import java.util.function.Consumer;

/** watches for concept lifecycle (creation and forget) events */
abstract public class ConceptReaction extends AbstractReaction {

    private final NAR nar;

    public ConceptReaction(NAR n) {
        super(n, Events.ConceptNew.class, Events.ConceptForget.class, Events.ConceptFired.class, Events.ResetStart.class);

        this.nar = n;
        //add existing events
        n.memory.taskLater(new Runnable() {
            @Override
            public void run() {
                n.memory.concepts.forEach(new Consumer<Concept>() {
                    @Override
                    public void accept(Concept concept) {
                        onNewConcept(concept);
                    }
                });
            }
        });
    }

    @Override
    public void event(final Class event, final Object[] args) {
        if (event == Events.ConceptNew.class) {
            Concept c = (Concept)args[0];
            onNewConcept(c);
        }
        else if (event == Events.ConceptForget.class) {
            Concept c = (Concept)args[0];
            onForgetConcept(c);
        }
        else if (event == Events.ConceptFired.class) {
            Concept c = ((ConceptProcess)args[0]).getCurrentConcept();
            onFiredConcept(c);
        }
        else if (event == Events.ResetStart.class) {
            nar.memory.concepts.forEach(new Consumer<Concept>() {
                @Override
                public void accept(Concept concept) {
                    onForgetConcept(concept);
                }
            });
        }
    }

    abstract public void onNewConcept(Concept c);
    abstract public void onForgetConcept(Concept c);
    abstract public void onFiredConcept(Concept c);
}
