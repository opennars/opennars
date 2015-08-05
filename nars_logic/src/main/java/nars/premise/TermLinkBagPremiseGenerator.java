package nars.premise;

import nars.bag.tx.ParametricBagForgetting;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Permits any premise to be selected, unfiltered, as decided by the Concept's bag
 * Includes parameter for how many re-attempts in case no termlink was
 * provided by the bag, or if the provided termlink was invalid.
 *
 * Not thread safe
 */
public class TermLinkBagPremiseGenerator extends ParametricBagForgetting<TermLinkKey,TermLink> implements PremiseGenerator, Function<TermLink, ParametricBagForgetting.ForgetAction> {

    public final AtomicInteger maxSelectionAttempts;
    private Concept currentConcept;
    private TaskLink currentTaskLink;


    public TermLinkBagPremiseGenerator(AtomicInteger maxSelectionAttempts) {
        super();
        this.maxSelectionAttempts = maxSelectionAttempts;
        setModel(this);

    }

    @Override
    public ForgetAction apply(TermLink termLink) {

        if (validTermLinkTarget(currentConcept, currentTaskLink, termLink)) {
            return ParametricBagForgetting.ForgetAction.SelectAndForget;
        }
        else {
            return ParametricBagForgetting.ForgetAction.Ignore;
        }

    }

    @Override
    public @Nullable TermLink nextTermLink(final Concept c, final TaskLink taskLink) {

        final int attempting = getMaxAttempts(c);
        if (attempting == 0) return null;

        int r = attempting;

        this.currentConcept = c;
        this.currentTaskLink = taskLink;
        set(c.getMemory().param.termLinkForgetDurations.floatValue(), c.getMemory().time());

        while (r > 0) {

            r--;

            c.getTermLinks().update(this);
            if (selected != null)
                break;

        }

        onSelect(c, selected != null, attempting - r);

        return selected;
    }

    /** for statistics and tuning purposes in subclasses */
    protected void onSelect(final Concept c, final boolean foundSomething, final int attempts) {

        /*
        int s = c.getTermLinks().size();
        System.out.println(c + " termlinks avail=" + s +
                    " found=" + foundSomething + " attempts=" + attempts);
        */

    }

    protected int getMaxAttempts(final Concept c) {
        int termlinks = c.getTermLinks().size();

        if (termlinks == 0)
            return 0;

        if (termlinks == 1)
            return 1;

        return maxSelectionAttempts.get();
    }

}
