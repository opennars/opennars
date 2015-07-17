package nars.premise;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;

/**
 * Permits any premise to be selected, unfiltered, as decided by the Concept's bag
 */
public class DirectPremiseSelector implements PremiseSelector {

    @Override
    public TermLink nextTermLink(Concept c, TaskLink taskLink) {
        return c.getTermLinks().forgetNext();
    }

}
