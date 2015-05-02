package nars.util.index;

import com.google.common.collect.HashBasedTable;
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


    //TODO combine entries and table into the same ConceptMap subclass

    /** active entries */
    public final ConceptMap entries;

    /** q-value matrix:  q[state][action] */
    public final HashBasedTable<R,C,Concept> table = HashBasedTable.create();

    protected final SetConceptMap<C> cols;
    protected final SetConceptMap<R> rows;

    boolean initialized = true;

    public ConceptMatrix(NAR nar) {
        super();

        this.nar = nar;

        cols = new SetConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isCol(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);
                C col = (C)c.term;

                table.columnMap().remove(col);

                onColumnRemove(col);
            }

        };

        rows = new SetConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isRow(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);

                R r = (R)c.term;

                table.rowMap().remove(r);

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

                R rr = getRow(i);
                C cc = getCol(i);

                table.remove(rr, cc);

                onEntryRemove(rr, cc);
            }

            @Override
            protected void onConceptNew(Concept c) {
                E i = (E)c.term;

                R rr = getRow(i);
                C cc = getCol(i);

                table.put(rr, cc, c);

                onEntryAdd(rr, cc, c);
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

    protected Concept getEntry(R row, C col) {
        return table.get(row, col);
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
