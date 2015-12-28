//package nars.term.match;
//
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Iterators;
//import nars.term.Term;
//import nars.term.compound.Compound;
//import nars.term.transform.Subst;
//
//import java.util.*;
//
///**
// * implementation which stores its series of subterms as a Term[]
// */
//public class ArrayEllipsisMatch<T extends Term> extends EllipsisMatch {
//
//    public final Term[] term;
//
//    public ArrayEllipsisMatch(Compound y, int from, int to) {
//        this(Subst.collect(y, from, to));
//    }
//
//    public ArrayEllipsisMatch(Term[] term) {
//        this.term = term;
//    }
//
//    @Override
//    public void apply(Collection<Term> sub) {
//        Collections.addAll(sub, term);
//    }
//
//    @Override
//    public String toString() {
//        return getClass().getSimpleName()+ ':' +Arrays.toString(term);
//    }
//
//
//    @Override
//    public int size() {
//        return term.length;
//    }
//
//    @Override
//    public boolean addContained(Compound Y, Set<Term> target) {
//        for (Term e : term) {
//            if (!Y.containsTerm(e)) return false;
//            target.add(e);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        return (obj instanceof ArrayEllipsisMatch) &&
//                Arrays.equals(((ArrayEllipsisMatch)obj).term
//        //return Iterables.elementsEqual(this, ((Iterable)obj));
//    }
//
//    @Override
//    public Iterator<Term> iterator() {
//        throw new RuntimeException("inefficient");
//        //return Iterators.forArray(term);
//    }
//}
