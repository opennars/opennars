package nars.util.index;

import nars.NAR;
import nars.event.ConceptReaction;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maintains a growing/sparse matrix consisting of a mapping of R row label concepts (ex: states),
 * C column label concepts (ex: actions), and E entry concepts (ex: implication of a state to an
 * action) -- according to a specified pattern that matches concepts as they become
 * active and removes them when forgotten.
 */
abstract public class ConceptMatrix<R extends Term, C extends Term, E extends Term, V extends ConceptMatrixEntry> {

    private final ConceptReaction entries;


    public final NAR nar;


    /**
     * q-value matrix:  q[state][action]
     */
    //public final HashBasedTable<R,C, V> table = HashBasedTable.create();

    public final SetConceptMap<C> cols;
    public final SetConceptMap<R> rows;


    boolean uninitialized = true;



    public ConceptMatrix(NAR nar) {
        super();

        this.nar = nar;

        cols = new SetConceptMap(nar) {

            @Override
            protected void onFrame() {
                super.onFrame();
                if (uninitialized) {
                    ConceptMatrix.this.init();
                    uninitialized = false;
                }

            }

            @Override
            public boolean contains(Concept c) {
                return isCol(c.term);
            }


        };

        rows = new SetConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isRow(c.term);
            }

        };

        entries = new ConceptReaction(nar) {


            @Override
            public void onConceptRemember(Concept c) {
                if (isEntry(c.term)) {
                    E i = (E) c.term;

                    R rr = getRow(i);
                    C cc = getCol(i);

                    V v = getEntry(c, rr, cc);

                    //onEntryAdd(rr, cc, v);
                }
            }

            @Override
            public void onConceptForget(Concept c) {
            }

            @Override
            public void onConceptDelete(Concept c) {


            }

            @Override
            public void onConceptProcessed(Concept c) {

            }
        };


    }

    public void deleteEntry(V m) {
        cols.exclude(getCol(m));
    }


    protected void init() {

    }


    /**
     * get existing entry or attempt to create one. if fails, returns null
     *
     * @return
     */
    protected V getEntry(Concept c, R row, C col) {
        V v = c.get(this);
        if (v != null) {
            return v;
        }

        v = newEntry(c, row, col);
        c.put(this, v);
        return v;
    }

    public V getEntry(R row, C col) {
        E qt = qterm(row, col);
        if (qt != null) {
            Concept c = nar.concept(qt);
            if (c != null) {
                return getEntry(c, row, col);
            }
        }

        return null;
    }

    abstract public E qterm(R r, C c);


    //v = new ConceptMatrixEntry<>(c, this);
    abstract public V newEntry(Concept c, R row, C col);

    abstract public R getRow(E entry);
    abstract public C getCol(E entry);

    public C getCol(V v) {
        return getCol((E) v.concept.term);
    }
    public C getCol(Concept c) {
        return getCol((E)c.term);
    }

    abstract public boolean isRow(Term s);

    abstract public boolean isCol(Term a);

    abstract public boolean isEntry(Term c);

    public void off() {
        cols.off();
        rows.off();
        entries.off();
    }

}