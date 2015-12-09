package nars.term.transform;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 12/3/15.
 */
public class MapSubst implements Subst {

    public Map<Term, Term> subs;

    /**
     * creates a substitution of one variable; more efficient than supplying a Map
     */
    public MapSubst(Term termFrom, Term termTo) {
        this(UnifiedMap.newWithKeysValues(termFrom, termTo));
    }


    public MapSubst(Map<Term, Term> subs) {
        this.subs = subs;
    }

    @Override
    public void clear() {
        subs.clear();
    }



    @Override
    public boolean isEmpty() {
        return subs.isEmpty();
    }

    /**
     * gets the substitute
     */
    @Override
    public final Term getXY(Term t) {
        return subs.get(t);
    }


    @Override
    public String toString() {
        return "Substitution{" +
                "subs=" + subs +
                '}';
    }


}
