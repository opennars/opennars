package nars.util.index;

import com.google.common.collect.HashBasedTable;
import nars.Global;
import nars.NAR;
import nars.event.ConceptReaction;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Maintains a growing/sparse matrix consisting of a mapping of R row label concepts (ex: states),
 * C column label concepts (ex: actions), and E entry concepts (ex: implication of a state to an
 * action) -- according to a specified pattern that matches concepts as they become
 * active and removes them when forgotten.
 */
abstract public class ConceptMatrix<R extends Term, C extends Term, E extends Term, V extends ConceptMatrix.EntryValue> {

    private final ConceptReaction entries;
    private boolean checkForEmptyColumns = true;
    private boolean checkForEmptyRows = true;

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

        /** returns true if the nullity changed */
        public boolean setConcept(Concept concept) {
            boolean b = false;
            if (this.concept == null && concept!=null) b = true;
            if (this.concept != null && concept==null) b = true;
            this.concept = concept;
            return b;
        }
    }

    public final NAR nar;


    /** q-value matrix:  q[state][action] */
    public final HashBasedTable<R,C, V> table = HashBasedTable.create();

    public final SetConceptMap<C> cols;
    public final SetConceptMap<R> rows;


    boolean uninitialized = true;


    protected Set<R> rowsMaybeEmpty = Global.newHashSet(1);
    protected Set<C> colsMaybeEmpty = Global.newHashSet(1);

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

                if (checkForEmptyRows) {
                    for (R r : rowsMaybeEmpty)
                        updateRow(r);
                    rowsMaybeEmpty.clear();
                }

                if (checkForEmptyColumns) {
                    for (C c : colsMaybeEmpty)
                        updateCol(c);
                    colsMaybeEmpty.clear();
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
                    }
                    else {
                        v.setConcept(c);
                    }

                    if (checkForEmptyRows)
                        rowsMaybeEmpty.remove(rr);

                    if (checkForEmptyColumns)
                        colsMaybeEmpty.remove(cc);

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
                    if ((v != null) && (v.setConcept(null))) {
                        if (checkForEmptyRows)
                            rowsMaybeEmpty.add(rr);

                        if (checkForEmptyColumns)
                            colsMaybeEmpty.add(cc);
                    }
                }
            }

            @Override
            public void onConceptProcessed(Concept c) {

            }
        };


    }

    protected void updateRow(R r) {
        if (!hasConcepts(table.row(r).values())) {
            table.rowMap().remove(r);
            onRowRemove(r);
        }
    }
    protected void updateCol(C col) {
        if (!hasConcepts(table.column(col).values())) {
            table.columnMap().remove(col);
            onColumnRemove(col);
        }
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

    public void setCheckForEmptyColumns(boolean checkForEmptyColumns) {
        this.checkForEmptyColumns = checkForEmptyColumns;
    }

    public void setCheckForEmptyRows(boolean checkForEmptyRows) {
        this.checkForEmptyRows = checkForEmptyRows;
    }
}