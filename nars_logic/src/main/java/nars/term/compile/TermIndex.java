package nars.term.compile;

import nars.term.Term;
import nars.term.Termed;
import nars.term.transform.CompoundTransform;

import java.util.function.Consumer;

/**
 *
 */
public interface TermIndex {

    public Termed get(Term t);

    public void forEachTerm(Consumer<Termed> c);

    CompoundTransform getCompoundTransformer();

}
