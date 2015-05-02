package nars.util.index;

import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

/**
 * Maintains a growing/sparse matrix consisting of a mapping of R row label concepts (ex: states),
 * C column label concepts (ex: actions), and E entry concepts (ex: implication of a state to an
 * action) -- according to a specified pattern that matches concepts as they become
 * active and removes them when forgotten.
 */
abstract public class ConceptMatrix<R extends Term, C extends Term, E extends Term> {

    public final NAR nar;

    /**
     * initializes that mapping which tracks concepts as they appear and disappear, maintaining mapping to the current instance
     */

    protected final ConceptMap entries;
    protected final SetConceptMap<C> cols;
    protected final SetConceptMap<R> rows;

    boolean initialized = true;

    public ConceptMatrix(NAR nar) {
        super();

        this.nar = nar;

        cols = new SetConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isCol((C) c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);
                C col = (C)c.term;
                onColumnRemove(col);
            }

        };

        rows = new SetConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isRow((R) c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);

                R r = (R)c.term;
                onRowRemove(r);
            }

        };

        entries = new ConceptMap(nar) {
            @Override
            protected void onCycle() {
                super.onCycle();
                if (initialized) {

                    init();

                    initialized = false;
                }
            }

            @Override
            public boolean contains(Concept c) {
                Term x = c.term;
                return isEntry(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                E i = (E)c.term;
                onEntryRemove(getRow(i), getCol(i));
            }

            @Override
            protected void onConceptNew(Concept c) {
                E i = (E)c.term;
                onEntryAdd(getRow(i), getCol(i), c);
            }

        };

    }

    protected void onRowRemove(R r) {

    }
    protected void onColumnRemove(C c) {

    }

    protected void onEntryAdd(R r, C c, Concept entry) {

    }

    protected void onEntryRemove(R r, C c) {

    }

    protected void init() {

    }

    abstract public R getRow(E entry);
    abstract public C getCol(E entry);
    abstract public boolean isRow(Term s);
    abstract public boolean isCol(Term a);
    abstract public boolean isEntry(Term c);

    public void off() {
        cols.off();
        rows.off();
        entries.off();
    }
}
