package nars.premise;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;

/**
 * Model that a Concept uses to decide a Termlink to fire with a given Tasklink
 */
public interface PremiseSelector {
    public TermLink nextTermLink(Concept c, TaskLink taskLink);
}
