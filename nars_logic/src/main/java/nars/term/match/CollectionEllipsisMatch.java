package nars.term.match;

import nars.Global;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.Subst;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * implementation which stores its series of subterms as a Term[]
 */
public class CollectionEllipsisMatch extends EllipsisMatch {

    public final Collection<Term> term;

    public CollectionEllipsisMatch(Collection<Term> term) {
        this.term = term;
    }
    public CollectionEllipsisMatch(Collection<Term> term, Term except) {
        Collection<Term> tt = this.term = Global.newArrayList(term.size() - 1);
        term.forEach(x -> {
            if (x!=except)
                tt.add(x);
        });
    }
    public CollectionEllipsisMatch(Collection<Term> term, Term except, Term except2) {
        Collection<Term> tt = this.term = Global.newArrayList(term.size() - 2);
        term.forEach(x -> {
            if ((x!=except) && (x!=except2))
                tt.add(x);
        });
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + term;
    }

    @Override
    public boolean applyTo(Subst f, Collection<Term> target, boolean fullMatch) {
        target.addAll(term);
        return true;
    }

    @Override
    public int size() {
        return term.size();
    }

    @Override
    public boolean addContained(Compound Y, Set<Term> target) {

        for (Term e : term) {
            if (!Y.containsTerm(e)) return false;
            target.add(e);
        }
        return true;

    }

    @Override
    public Iterator<Term> iterator() {
        return term.iterator();
    }
}
