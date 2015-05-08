package nars.event;


import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.ConceptProcess;

import java.util.function.Consumer;

/** watches for concept lifecycle (creation and forget) events */
abstract public class ConceptReaction extends NARReaction {

    public final Memory memory;

    public ConceptReaction(NAR n) {
        this(n.memory);
    }

    public ConceptReaction(Memory m) {
        super(m.event, true, Events.ConceptForget.class, Events.ResetStart.class, Events.ConceptActive.class, Events.ConceptDelete.class);

        this.memory = m;
        memory.taskLater(this::init);

    }

    protected void init() {

        //add existing events
        memory.concepts.forEach(new Consumer<Concept>() {
            @Override
            public void accept(Concept concept) {
                onConceptActive(concept);
            }
        });
    }

    @Override
    public void event(final Class event, final Object[] args) {
        if (event == Events.ConceptActive.class) {
            Concept c = (Concept)args[0];
            onConceptActive(c);
        }
        else if (event == Events.ConceptForget.class) {
            Concept c = (Concept)args[0];
            onConceptForget(c);
        }
        else if (event == Events.ConceptDelete.class) {
            Concept c = (Concept)args[0];
            onConceptDelete(c);
        }
        else if (event == Events.ResetStart.class) {
            memory.concepts.forEach(new Consumer<Concept>() {
                @Override
                public void accept(Concept concept) {
                    onConceptForget(concept);
                }
            });
        }
    }

    /** called for concepts newly created or remembered (from subconcepts) */
    abstract public void onConceptActive(Concept c);

    /** called if concept leaves main memory (either removed entirely, or moved to subconcepts) */
    abstract public void onConceptForget(Concept c);

    /** called before a concept instance dies permanently */
    public void onConceptDelete(Concept c) {

    }

}
