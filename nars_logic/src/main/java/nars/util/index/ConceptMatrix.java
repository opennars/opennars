//package nars.util.index;
//
//import nars.NAR;
//import nars.budget.Budget;
//import nars.concept.Concept;
//import nars.event.ConceptReaction;
//import nars.term.Term;
//
///**
// * Maintains a growing/sparse matrix consisting of a mapping of R row label concepts (ex: states),
// * C column label concepts (ex: actions), and E entry concepts (ex: implication of a state to an
// * action) -- according to a specified pattern that matches concepts as they become
// * active and removes them when forgotten.
// */
//abstract public class ConceptMatrix<R extends Term, C extends Term, E extends Term, V extends ConceptMatrixEntry> {
//
//    private final ConceptReaction entries;
//
//
//    public final NAR nar;
//
//
//    /**
//     * q-value matrix:  q[state][action]
//     */
//    //public final HashBasedTable<R,C, V> table = HashBasedTable.create();
//
//    public final ConceptSetTermsOnly<C> cols;
//    public final ConceptSetTermsOnly<R> rows;
//
//
//    boolean uninitialized = true;
//
//
//
//    public ConceptMatrix(NAR nar) {
//        super();
//
//        this.nar = nar;
//
//        cols = new ConceptSetTermsOnly(nar) {
//
//            @Override
//            protected void onFrame() {
//                super.onFrame();
//                if (uninitialized) {
//                    ConceptMatrix.this.init();
//                    uninitialized = false;
//                }
//
//            }
//
//            @Override
//            public boolean contains(Concept c) {
//                return isCol(c);
//            }
//
//
//        };
//
//        rows = new ConceptSetTermsOnly(nar) {
//
//            @Override
//            public boolean contains(Concept c) {
//                return isRow(c);
//            }
//
//        };
//
//        entries = new ConceptReaction(nar, true) {
//
//
//            @Override
//            public void onConceptActive(Concept c) {
//                if (isEntry(c.getTerm())) {
//                    E i = (E) c.getTerm();
//
//                    R rr = getRow(i);
//                    C cc = getCol(i);
//
//                    V v = getEntry(c, rr, cc);
//
//                    //onEntryAdd(rr, cc, v);
//                }
//            }
//
//            @Override
//            public void onConceptForget(Concept c) {
//            }
//
//        };
//
//
//    }
//
//    public void deleteEntry(V m) {
//        cols.exclude(getCol(m));
//    }
//
//
//    protected void init() {
//
//    }
//
//
//    /**
//     * get existing entry or attempt to create one. if fails, returns null
//     *
//     * @return
//     */
//    protected V getEntry(Concept c, R row, C col) {
//        V v = c.get(this);
//        if (v != null) {
//            return v;
//        }
//
//        v = newEntry(c, row, col);
//        c.put(this, v);
//        return v;
//    }
//
//    public V getEntry(R row, C col) {
//        return getEntry(row, col, 0, 0, 0);
//    }
//    public V getEntry(R row, C col, float conceptualizePriority, float conceptualizeDuration, float conceptualizeQuality) {
//        E qt = qterm(row, col);
//        if (qt != null) {
//            Concept c;
//            if (conceptualizePriority == 0) {
//                c = nar.concept(qt);
//            }
//            else {
//                c = nar.conceptualize(
//                        qt, new Budget(conceptualizePriority, conceptualizeDuration, conceptualizeQuality)
//                );
//            }
//            if (c != null) {
//                return getEntry(c, row, col);
//            }
//        }
//
//        return null;
//    }
//
//    abstract public E qterm(R r, C c);
//
//
//    //v = new ConceptMatrixEntry<>(c, this);
//    abstract public V newEntry(Concept c, R row, C col);
//
//    abstract public R getRow(E entry);
//    abstract public C getCol(E entry);
//
//    public C getCol(V v) {
//        return getCol((E) v.concept.getTerm());
//    }
//    public C getCol(Concept c) {
//        return getCol((E) c.getTerm());
//    }
//
//    abstract public boolean isRow(Term s);
//
//    abstract public boolean isCol(Term a);
//
//    public boolean isCol(Concept a) {
//        if (isCol(a.getTerm())) {
//            return cols.include(a);
//        }
//        return false;
//    }
//    public boolean isRow(Concept a) {
//        if (isRow(a.getTerm())) {
//            return rows.include(a);
//        }
//        return false;
//    }
//
//    abstract public boolean isEntry(Term c);
//
//    public void off() {
//        cols.off();
//        rows.off();
//        entries.off();
//    }
//
//}