package nars.premise;

import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.tuple.Tuples;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Sentence;
import nars.term.Term;

/**
 * Model that a Concept uses to decide a Termlink to fire with a given Tasklink
 */
public interface PremiseGenerator {

    public void setConcept(Concept c);

    abstract public boolean validTermLinkTarget(final TermLink term, final TaskLink task);

    public static Pair<Term, Sentence> pair(TaskLink taskLink, TermLink t) {
        return Tuples.pair(t.getTerm(), taskLink.getTask());
    }

    public TermLink nextTermLink(Concept c, TaskLink taskLink);
}
