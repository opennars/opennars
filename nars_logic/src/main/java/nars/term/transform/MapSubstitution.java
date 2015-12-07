package nars.term.transform;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 12/3/15.
 */
public class MapSubstitution implements Substitution {
    public Map<Term, Term> subs;

    /**
     * creates a substitution of one variable; more efficient than supplying a Map
     */
    public MapSubstitution(Term termFrom, Term termTo) {
        this(UnifiedMap.newWithKeysValues(termFrom, termTo));
    }


    public MapSubstitution(final Map<Term, Term> subs) {
        reset(subs);
    }


    public Substitution reset(final Map<Term, Term> subs) {
        this.subs = subs;
        return this;
    }

    @Override
    public boolean isEmpty() {
        return subs.isEmpty();
    }

    /**
     * gets the substitute
     */
    @Override
    final public Term getXY(final Term t) {
        return subs.get(t);
    }


    @Override
    public String toString() {
        return "Substitution{" +
                "subs=" + subs +
                '}';
    }



    @Override
    public void putXY(Term x, Term y) {
        subs.put(x, y);
    }
}
