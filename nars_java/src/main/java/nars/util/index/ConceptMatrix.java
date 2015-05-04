package nars.util.index;

import com.google.common.collect.HashBasedTable;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.Global;
import nars.NAR;
import nars.event.ConceptReaction;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Maintains a growing/sparse matrix consisting of a mapping of R row label concepts (ex: states),
 * C column label concepts (ex: actions), and E entry concepts (ex: implication of a state to an
 * action) -- according to a specified pattern that matches concepts as they become
 * active and removes them when forgotten.
 */
abstract public class ConceptMatrix<R extends Term, C extends Term, E extends Term, V extends ConceptMatrix.EntryValue> {

    private final ConceptReaction entries;

    /** base type for cell entries in the matrix. subclass to add additional information per cell */
    public static class EntryValue {

        @Nullable
        private Concept concept;

        public EntryValue(Concept c) {
            this.concept = c;
        }

        public Concept getConcept() {
            return concept;
        }

        public void setConcept(Concept concept) {
            this.concept = concept;
        }
    }

    public final NAR nar;



    /** active entries */
    //public final ConceptMap entries;

    /** q-value matrix:  q[state][action] */
    public final HashBasedTable<R,C, V> table = HashBasedTable.create();

    public final SetConceptMap<C> cols;
    public final SetConceptMap<R> rows;

    //TODO merge into a MapConceptMap<C,ConceptStatistics> instance
    final ObjectIntHashMap<R> rowConcepts = new ObjectIntHashMap<>();
    final ObjectIntHashMap<C> colConcepts = new ObjectIntHashMap<>();

    boolean uninitialized = true;


    protected static void ensureValidCount(final int x) {
        if (x < 0) throw new RuntimeException("concept count underflow");
    }

    protected int addColCount(C col, int d) {
        int x = colConcepts.addToValue(col, d);
        ensureValidCount(x);

        if (Global.DEBUG) {
            if (x > table.rowKeySet().size()) {
                throw new RuntimeException("concept count overflow");
            }
        }

        //remove the col if no entries referencing concepts remain
        //even if the index label concept is removed, there may still be entries referring to it
        if (x == 0) {
            //no concepts remain in this column, remove it
            table.columnMap().remove(col);
            colConcepts.remove(col);
            onColumnRemove(col);
        }

        return x;
    }
    protected int addRowCount(R row, int d) {
        int x = rowConcepts.addToValue(row, d);
        ensureValidCount(x);

        if (Global.DEBUG) {
            if (x > table.columnKeySet().size()) {
                throw new RuntimeException("concept count overflow");
            }
        }

        //remove the row if no entries referencing concepts remain
        //even if the index label concept is removed, there may still be entries referring to it
        if (x == 0) {
            //no concepts remain in this column, remove it
            table.rowMap().remove(row);
            rowConcepts.remove(row);
            onRowRemove(row);
        }

        return x;
    }

    protected void addCount(R r, C c, int d) {
        addRowCount(r, d);
        addColCount(c, d);
    }

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
                    E i = (E)c.term;

                    R rr = getRow(i);
                    C cc = getCol(i);

                    V v = getEntry(rr, cc);
                    if (v == null) {
                        v = newEntry(c);
                        table.put(rr, cc, v);
                        addCount(rr, cc, 1);
                    }
                    else {
                        if (v.getConcept() == null) {
                            addCount(rr, cc, 1);
                        }

                        v.setConcept(c);
                    }

                    onEntryAdd(rr, cc, v);
                }
            }

            @Override
            public void onConceptForget(Concept c) {
                if (isEntry(c.term)) {
                    E i = (E)c.term;
                    R rr = getRow(i);
                    C cc = getCol(i);

                    V v = getEntry(rr, cc);
                    if ((v != null) && (v.getConcept()!=null)) {
                        v.setConcept(null);
                        addCount(rr, cc, -1);
                    }
                }
            }

            @Override
            public void onConceptProcessed(Concept c) {

            }
        };


    }

    public static boolean hasConcepts(final Collection<? extends EntryValue> values) {
        for (EntryValue v : values) {
            if (v.getConcept()!=null) return true;
        }
        return false;
    }

    protected void onRowRemove(R r) {

    }
    protected void onColumnRemove(C c) {

    }

    protected void onEntryAdd(R r, C c, V v) {

    }

    protected void onEntryRemove(R r, C c) {

    }

    protected void init() {

    }

    protected V getEntry(R row, C col) {
        return table.get(row, col);
    }

    abstract public V newEntry(Concept concept);

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
