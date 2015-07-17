package nars.premise;

import nars.Memory;
import nars.Param;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;

import java.util.Random;

/**
 * Uses a Record short-term-memory list of recently fired termlinks
 * for each tasklink
 */
public class NoveltyRecordPremiseSelector implements PremiseSelector {

    final static float minNovelty = Param.NOVELTY_FLOOR;

    private float noveltyHorizon;
    private int numTermLinks; //total # of tasklinks in the bag

    private float noveltyDuration;

    final Memory memory;

    public NoveltyRecordPremiseSelector(Memory memory) {
        this.memory = memory;
    }


    /**
     * now is float because it is calculated as the fraction of current time + 1/(termlinks matched), thus including the subcycle
     */
    public void set(final float noveltyHorizon, final int numTermLinksInBag) {
        this.noveltyHorizon = noveltyHorizon;
        this.numTermLinks = numTermLinksInBag;


        /** proportional to an amount of cycles it should take a fired termlink
         * to be considered novel.
         * there needs to be at least 2 termlinks to use the novelty filter.
         * if there is one termlink, there is nothing to prioritize it against.
         * */
        this.noveltyDuration = (noveltyHorizon *
                Math.max(0, numTermLinksInBag - 1));
    }


    public boolean test(TaskLink taskLink, TermLink termLink, long now, Random rng) {
        if (noveltyDuration == 0) {
            //this will happen in the case of one termlink,
            //in which case there is no other option so duration
            //will be zero
            return true;
        }

        if (!taskLink.valid(termLink))
            return false;


        TaskLink.Recording r = taskLink.get(termLink);
        if (r == null) {
            taskLink.put(termLink, now);
            return true;
        } else {
            boolean result;

            //determine age (non-novelty) factor
            float lft = taskLink.getLastFireTime();
            if (lft == -1) {
                //this is its first fire
                result = true;
            } else {

                float timeSinceLastFire = lft - r.getTime();
                float factor = noveltyFactor(timeSinceLastFire, minNovelty, noveltyDuration);

                if (factor <= 0) {
                    result = false;
                } else if (factor >= 1f) {
                    result = true;
                } else {
                    float f = rng.nextFloat();
                    result = (f < factor);
                }
            }


            if (result) {
                taskLink.put(r, now);
                return true;
            } else {
                return false;
            }

        }

    }

    public static float noveltyFactor(final float timeSinceLastFire, final float minNovelty, final float noveltyDuration) {


        if (timeSinceLastFire <= 0)
            return minNovelty;

        float n = Math.max(0,
                Math.min(1f,
                        timeSinceLastFire /
                                noveltyDuration));


        n = (minNovelty) + (n * (1.0f - minNovelty));

        return n;

    }


    /**
     * Replace default to prevent repeated logic, by checking TaskLink
     *
     * @param taskLink The selected TaskLink
     * @param time     The current time
     * @return The selected TermLink
     */
    public TermLink nextTermLink(Concept c, TaskLink taskLink) {

        final float noveltyHorizon = memory.param.noveltyHorizon.floatValue();

        final int links = c.getTermLinks().size();
        if (links == 0) return null;

        int toMatch = memory.param.termLinkMaxMatched.get();

        //optimization case: if there is only one termlink, we will never get anything different from calling repeatedly
        if (links == 1) toMatch = 1;

        Bag<TermLinkKey, TermLink> tl = c.getTermLinks();

        set(noveltyHorizon, tl.size());

        final long now = memory.time();

        Random rng = memory.random;

        for (int i = 0; (i < toMatch); i++) {

            final TermLink termLink = tl.forgetNext();

            if (termLink != null) {
                if (test(taskLink, termLink, now, rng)) {
                    return termLink;
                }
            } else {
                break;
            }

        }

        return null;
    }

}
