package nars.event;


import nars.NAR;
import nars.concept.Concept;
import nars.util.event.DefaultTopic;
import nars.util.event.OnTopics;

/** watches for concept lifecycle (creation and forget) events */
abstract public class ConceptReaction extends OnTopics {

    public final NAR nar;

    private final DefaultTopic.On onConceptActive;
    private final DefaultTopic.On onConceptForget;


    public ConceptReaction(NAR n) {


        this.onConceptActive = n.mem().eventConceptActivated.on(c -> {
            onConceptActive(c);
        });
        this.onConceptForget = n.mem().eventConceptForget.on(c -> {
            onConceptForget(c);
        });

        this.nar = n;
        nar.taskLater(this::init);

    }

    protected void init() {

        //add existing events
        nar.forEachConcept(ConceptReaction.this::onConceptActive);
    }

//    @Override
//    public void event(final Class event, final Object[] args) {
////        if (event == Events.ConceptDelete.class) {
////            Concept c = (Concept)args[0];
////            onConceptDelete(c);
////        }
//        if (event == Events.ResetStart.class) {
//            nar.forEachConcept(ConceptReaction.this::onConceptForget);
//        }
//    }

    /** called for concepts newly created or remembered (from subconcepts) */
    abstract public void onConceptActive(Concept c);

    /** called if concept leaves main memory (either removed entirely, or moved to subconcepts) */
    abstract public void onConceptForget(Concept c);



//    /** called before a concept instance dies permanently */
//    public void onConceptDelete(Concept c) {
//
//    }

}
