package nars.util.index;

import com.google.common.collect.HashBasedTable;
import nars.NAR;
import nars.event.ConceptReaction;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Maintains a growing/sparse matrix consisting of a mapping of R row label concepts (ex: states),
 * C column label concepts (ex: actions), and E entry concepts (ex: implication of a state to an
 * action) -- according to a specified pattern that matches concepts as they become
 * active and removes them when forgotten.
 */
abstract public class ConceptMatrix<R extends Term, C extends Term, E extends Term, V extends ConceptMatrix.EntryValue> {

    private final ConceptReaction entries;

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


    //TODO combine entries and table into the same ConceptMap subclass

    /** active entries */
    //public final ConceptMap entries;

    /** q-value matrix:  q[state][action] */
    public final HashBasedTable<R,C, V> table = HashBasedTable.create();

    public final SetConceptMap<C> cols;
    public final SetConceptMap<R> rows;

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

                //remove the col if no entries referencing concepts remain
                //even if the index label concept is removed, there may still be entries referring to it
                Map<C, Map<R, V>> cm = table.columnMap();
                if (cm!=null) {
                    Map<R, V> mc = cm.get(col);
                    if (mc!=null && !hasConcepts(mc.values())) {
                        cm.remove(col);
                        onColumnRemove(col);
                    }
                }

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

                //remove the row if no entries referencing concepts remain
                //even if the index label concept is removed, there may still be entries referring to it
                Map<R, Map<C, V>> rm = table.rowMap();
                if (rm!=null) {
                    Map<C, V> mr = rm.get(r);
                    if (mr!=null && !hasConcepts(mr.values())) {
                        rm.remove(r);
                        onRowRemove(r);
                    }
                }
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
                    else
                        v.setConcept(c);

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
                    if (v != null) v.setConcept(null);
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
