/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Events;
import nars.Param;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nal.Premise;
import nars.term.Term;

import java.util.Random;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
public class ConceptProcess extends NAL implements Premise {

    protected final TaskLink currentTaskLink;
    protected final Concept currentConcept;

    protected TermLink currentTermLink;


    //essentially a cache for a concept lookup
    private transient Concept currentTermLinkConcept;
    final TermLinkNoveltyFilter termLinkNovel = new TermLinkNoveltyFilter();


    private final int termLinksToFire;
    private long now;


    public ConceptProcess(Concept concept, TaskLink taskLink) {
        this(concept, taskLink, concept.getMemory().param.termLinkMaxReasoned.get());
    }

    public ConceptProcess(Concept concept, TaskLink taskLink, int termLinkCount) {
        super(concept.getMemory(), taskLink.getTask());


        this.currentTaskLink = taskLink;
        this.currentConcept = concept;
        this.termLinksToFire = termLinkCount;


    }



    /**
     * @return the currentConcept
     */
    @Override
    public Concept getCurrentConcept() {
        return currentConcept;
    }


    protected void beforeFinish() {

    }

    @Override
    protected void onFinished() {
        beforeFinish();


        emit(Events.ConceptProcessed.class, this);
        memory.logic.TASKLINK_FIRE.hit();

    }


    protected void processTask() {

        setCurrentTerm(currentConcept.getTerm());
        setCurrentTermLink(null);
        reasoner.fire(this);
    }

    protected void processTerms() {

        //TODO early termination condition of this loop when (# of termlinks) - (# of non-novel) <= 0
        int numTermLinks = getCurrentConcept().getTermLinks().size();
        if (numTermLinks == 0)
            return;

        final float noveltyHorizon = memory.param.noveltyHorizon.floatValue();

        int termLinkSelectionAttempts = termLinksToFire;

        currentConcept.updateTermLinks();



        /** use the time since last cycle as a sort of estimate for how to divide this cycle into subcycles;
         * this isnt necessary for default mode but realtime mode and others may have
         * irregular or unpredictable clocks.
         */
        float cyclesSincePrevious = memory.timeSinceLastCycle();

        float subCycle = (cyclesSincePrevious) / (termLinkSelectionAttempts);

        int termLinksSelected = 0;
        float n = now;

        while (termLinkSelectionAttempts-- > 0) {


           final TermLink bLink = nextTermLink(noveltyHorizon, termLinksToFire);

            if (bLink!=null) {
                processTerm(bLink);
                termLinksSelected++;
                n += subCycle;
            }

            //emit(Events.TermLinkSelected.class, bLink, this);

        }


        /*if (termLinksSelected == 0) {
            System.err.println(now + ": " + currentConcept + ": " + termLinksSelected + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
                    //+ currentConcept.getTermLinks().values()
            );
            //currentConcept.taskLinks.printAll(System.out);
        }*/
    }

    final static float minNovelty = Param.NOVELTY_FLOOR;


    public final class TermLinkNoveltyFilter  {

        private float noveltyHorizon;
        private int numTermLinks; //total # of tasklinks in the bag
        private int termsLinkBeingFired;

        private float noveltyDuration;

        /** now is float because it is calculated as the fraction of current time + 1/(termlinks matched), thus including the subcycle */
        public void set(float noveltyHorizon, int numTermLinksInBag, int termsLinkBeingFired) {
            this.noveltyHorizon = noveltyHorizon;
            this.numTermLinks = numTermLinksInBag;
            this.termsLinkBeingFired = termsLinkBeingFired;

            /** proportional to an amount of cycles it should take a fired termlink
             * to be considered novel.
             * there needs to be at least 2 termlinks to use the novelty filter.
             * if there is one termlink, there is nothing to prioritize it against.
             * */
            this.noveltyDuration = (noveltyHorizon *
                    Math.max(0, numTermLinksInBag-1));
        }


        public boolean test(TermLink termLink, Random rng) {
            if (noveltyDuration == 0) {
                //this will happen in the case of one termlink,
                //in which case there is no other option so duration
                //will be zero
                return true;
            }

            final TaskLink taskLink = currentTaskLink;

            if (!taskLink.valid(termLink))
                return false;




            TaskLink.Recording r = taskLink.get(termLink);
            if (r == null) {
                taskLink.put(termLink, now);
                return true;
            }
            else {
                boolean result;

                //determine age (non-novelty) factor
                float lft = taskLink.getLastFireTime();
                if (lft == -1) {
                    //this is its first fire
                    result = true;
                }
                else {

                    float timeSinceLastFire = lft - r.getTime();
                    float factor = noveltyFactor(timeSinceLastFire);

                    if (factor <= 0) {
                        result = false;
                    }
                    else if (factor >= 1f) {
                        result = true;
                    } else {
                        float f = rng.nextFloat();
                        result = (f < factor);
                    }
                }


                if (result) {
                    taskLink.put(r, now);
                    return true;
                }
                else {
                    return false;
                }

            }

        }

        private float noveltyFactor(final float timeSinceLastFire) {


            if (timeSinceLastFire <= 0)
                return minNovelty;

            float n = Math.max(0,
                    Math.min(1f,
                            timeSinceLastFire /
                                    noveltyDuration) ) ;


            n = (minNovelty) + (n * (1.0f - minNovelty));

            return n;

        }

    }


    /**
     * Replace default to prevent repeated logic, by checking TaskLink
     *
     * @param taskLink The selected TaskLink
     * @param time The current time
     * @return The selected TermLink
     */
    TermLink nextTermLink(float noveltyHorizon, int termLinksBeingFired) {

        final int links = currentConcept.getTermLinks().size();
        if (links == 0) return null;

        int toMatch = memory.param.termLinkMaxMatched.get();

        //optimization case: if there is only one termlink, we will never get anything different from calling repeatedly
        if (links == 1) toMatch = 1;

        Bag<TermLinkKey, TermLink> tl = currentConcept.getTermLinks();

        termLinkNovel.set(noveltyHorizon, tl.size(), termLinksBeingFired);


        Random rng = memory.random;
        for (int i = 0; (i < toMatch); i++) {

            final TermLink termLink = tl.forgetNext();

            if (termLink != null) {
                if (termLinkNovel.test(termLink, rng)) {
                    return termLink;
                }
            }
            else {
                break;
            }

        }

        return null;
    }


    /**
     * Entry point of the logic engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     */
    protected void processTerm(TermLink bLink) {
        setCurrentTermLink(bLink);

        reasoner.fire(this);

        emit(Events.BeliefReason.class, getCurrentBelief(), getCurrentTask(), this);
    }

    @Override
    public void run() {
        if (!currentConcept.isActive()) return;

        super.run();
    }

    @Override
    protected void process() {

        final long now = this.now = memory.time();

        currentConcept.setUsed(now);
        currentTaskLink.setUsed(now);

        currentConcept.getTermLinks().setForgetNext(memory.param.termLinkForgetDurations, memory);

        processTask();

        if (currentTaskLink.type != TermLink.TRANSFORM) {
            processTerms();
        }

        currentTaskLink.setFired(now);
    }





    /**
     * @return the currentBeliefLink
     */
    @Override
    public TermLink getCurrentTermLink() {
        return currentTermLink;
    }

    /**
     * @param currentTermLink the currentBeliefLink to set
     */
    public void setCurrentTermLink(TermLink currentTermLink) {

        if (currentTermLink == null) {
            this.currentTermLink = null;
            this.currentTermLinkConcept = null;
        }
        else {
            this.currentTermLink = currentTermLink;
            this.currentTermLinkConcept = null; //this will be fetched if requested, and cached until the termlink changes
            currentTermLink.setUsed(memory.time());
        }
    }



    /**
     * @return the currentTaskLink
     */
    @Override
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }





    /** the current belief concept */
    public Concept getCurrentTermLinkConcept() {
        if (currentTermLinkConcept == null && getCurrentTermLink()!=null) {
            currentTermLinkConcept = memory.concept( getCurrentTermLink().getTarget() );
        }
        return currentTermLinkConcept;
    }

    public float conceptPriority(Term target) {
        //first check for any cached Concept
        if (target == getCurrentTermLink().target) {
            Concept c = getCurrentTermLinkConcept();

            if (c == null) return 0; //if the concept does not exist, use priority = 0

            return c.getPriority();
        }
        return super.conceptPriority(target);
    }


    @Override
    public String toString() {
        return new StringBuilder()
        .append("ConceptProcess[").append(currentConcept.toString()).append(':').append(currentTaskLink).append(',').append(currentTermLink).append(']')
        .toString();
    }


}
