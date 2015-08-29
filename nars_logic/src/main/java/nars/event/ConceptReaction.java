package nars.event;


import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.util.event.Observed;

import java.util.function.Consumer;

/** watches for concept lifecycle (creation and forget) events */
abstract public class ConceptReaction extends NARReaction {

    public final Memory memory;

    private final Observed.DefaultObserved.DefaultObservableRegistration onConceptActive;
    private final Observed.DefaultObserved.DefaultObservableRegistration onConceptForget;


    public ConceptReaction(NAR n) {
        this(n.memory);
    }

    public ConceptReaction(Memory m) {
        this(m, true);
    }

    public ConceptReaction(Memory m, boolean active, Class... additionalEvents) {
        super(m.event, true, additionalEvents);


        this.onConceptActive = m.eventConceptActive.on(c -> {
            onConceptActive(c);
        });
        this.onConceptForget = m.eventConceptForget.on(c -> {
            onConceptForget(c);
        });

        this.memory = m;
        memory.taskLater(this::init);

    }

    protected void init() {

        //add existing events
        memory.getControl().forEach(new Consumer<Concept>() {
            @Override
            public void accept(Concept concept) {
                onConceptActive(concept);
            }
        });
    }

    @Override
    public void event(final Class event, final Object[] args) {
//        if (event == Events.ConceptDelete.class) {
//            Concept c = (Concept)args[0];
//            onConceptDelete(c);
//        }
        if (event == Events.ResetStart.class) {
            memory.getControl().forEach(new Consumer<Concept>() {
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
