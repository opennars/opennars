package nars.event;


import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.ConceptProcess;

import java.util.function.Consumer;

/** watches for concept lifecycle (creation and forget) events */
abstract public class ConceptReaction extends AbstractReaction {

    private final Memory memory;

    public ConceptReaction(NAR n) {
        this(n.memory);
    }

    public ConceptReaction(Memory m) {
        super(m.event, true, Events.ConceptNew.class, Events.ConceptForget.class, Events.ConceptFired.class, Events.ResetStart.class);

        this.memory = m;
        //add existing events
        memory.taskLater(new Runnable() {
            @Override
            public void run() {
                memory.concepts.forEach(new Consumer<Concept>() {
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
            memory.concepts.forEach(new Consumer<Concept>() {
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
