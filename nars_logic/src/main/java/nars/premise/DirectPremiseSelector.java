package nars.premise;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Permits any premise to be selected, unfiltered, as decided by the Concept's bag
 * Includes parameter for how many re-attempts in case no termlink was
 * provided by the bag, or if the provided termlink was invalid.
 */
public class DirectPremiseSelector implements PremiseSelector {

    public final AtomicInteger maxSelectionAttempts;

    public DirectPremiseSelector(AtomicInteger maxSelectionAttempts) {
        this.maxSelectionAttempts = maxSelectionAttempts;
    }

    @Override
    public @Nullable TermLink nextTermLink(final Concept c, final TaskLink taskLink) {

        final int attempting = getMaxAttempts(c);
        int r = attempting;

        TermLink result = null;
        while (r > 0) {

            r--;

            result = c.getTermLinks().forgetNext();

            if (validTermLinkTarget(c, taskLink, result))
                break;

        }

        onSelect(c, result != null, attempting - r);

        return result;
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
