package nars.rl;

import com.google.common.collect.HashBasedTable;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import vnc.ConceptMap;

/**
 * Maintains a growing/sparse matrix consisting of a mapping of R row label terms (ex: states),
 * C column label terms (ex: actions), and E entry terms (ex: implication of a state to an
 * action) -- according to a specified pattern that matches concepts as they become
 * active and removes them when forgotten.
 */
abstract public class TermMatrix<R extends Term, C extends Term, E extends Term> {

    public final NAR nar;

    /**
     * initializes that mapping which tracks concepts as they appear and disappear, maintaining mapping to the current instance
     */



    protected ConceptMap entries;
    protected ConceptMap.ConceptMapSet<C> cols; //col
    protected ConceptMap.ConceptMapSet<R> rows;  //row

    boolean initialized = true;

    public TermMatrix(NAR nar) {
        super();

        this.nar = nar;

        cols = new ConceptMap.ConceptMapSet(nar) {

            @Override
            public boolean contains(Concept c) {
                return isAction((C)c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);
                C col = (C)c.term;
                onColumnRemove(col);
            }

        };

        rows = new ConceptMap.ConceptMapSet(nar) {

            @Override
            public boolean contains(Concept c) {
                return isState((R)c.term);
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
                return isEntry((E)c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                E i = (E)c.term;
                onEntryRemove(state(i), action(i));
            }

            @Override
            protected void onConceptNew(Concept c) {
                E i = (E)c.term;
                onEntryAdd(state(i), action(i), c);
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

    abstract public R state(E entry);
    abstract public C action(E entry);
    abstract public boolean isState(Term s);
    abstract public boolean isAction(Term a);
    abstract public boolean isEntry(Term c);
}
