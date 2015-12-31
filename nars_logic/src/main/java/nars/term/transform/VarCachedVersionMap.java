package nars.term.transform;

import nars.term.Term;
import nars.term.variable.Variable;
import nars.util.version.VersionMap;
import nars.util.version.Versioned;
import nars.util.version.Versioning;

import java.util.Map;

/**
 * Created by me on 12/17/15.
 */
public final class VarCachedVersionMap extends VersionMap<Term, Term> implements Subst {

    public VarCachedVersionMap(Versioning context) {
        super(context);
    }

    public VarCachedVersionMap(Versioning context, Map<Term, Versioned<Term>> map) {
        super(context, map);
    }

    @Override
    public boolean cache(Term key) {
        //since these should always be normalized variables, they will not exceed a predictable range of entries (ex: $1, $2, .. $n)
        return key instanceof Variable;
    }

    @Override
    public Term getXY(Object t) {
        Versioned<Term> v = map.get(t);
        if (v == null) return null;
        return v.get();
    }

    /** must inspect elements because the entries will be there but possibly null */
    @Override public boolean isEmpty() {
        if (super.isEmpty())
            return true;
        for (Versioned x : map.values()) {
            if (x.get()!=null) return false;
        }
        return true;
    }
}
