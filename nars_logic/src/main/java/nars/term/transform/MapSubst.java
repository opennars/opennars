package nars.term.transform;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 12/3/15.
 */
public class MapSubst implements Subst {

    public final Map<Term, Term> xy;

    /**
     * creates a substitution of one variable; more efficient than supplying a Map
     */
    public MapSubst(Term termFrom, Term termTo) {
        this(UnifiedMap.newWithKeysValues(termFrom, termTo));
    }


    public MapSubst(Map<Term, Term> xy) {
        this.xy = xy;
    }

    @Override
    public void clear() {
        xy.clear();
    }



    @Override
    public boolean isEmpty() {
        return xy.isEmpty();
    }

    /**
     * gets the substitute
     * @param t
     */
    @Override
    public final Term getXY(Object t) {
        return xy.get(t);
    }


    @Override
    public String toString() {
        return "Substitution{" +
                "subs=" + xy +
                '}';
    }


}
