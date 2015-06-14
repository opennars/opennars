package nars.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import nars.entity.TermLink;
import nars.storage.Memory;

/**
 * Static utility class for static methods related to Terms
 * @author me
 */
public class Terms {

    public static boolean equalSubTermsInRespectToImageAndProduct(final Term a, final Term b) {
        if (a == null || b == null) {
            return false;
        }
        if (!((a instanceof CompoundTerm) && (b instanceof CompoundTerm))) {
            return a.equals(b);
        }
        if (a instanceof Inheritance && b instanceof Inheritance) {
            return equalSubjectPredicateInRespectToImageAndProduct(a, b);
        }
        if (a instanceof Similarity && b instanceof Similarity) {
            return equalSubjectPredicateInRespectToImageAndProduct(a, b) || equalSubjectPredicateInRespectToImageAndProduct(b, a);
        }
        Term[] A = ((CompoundTerm) a).term;
        Term[] B = ((CompoundTerm) b).term;
        if (A.length != B.length) {
            return false;
        } else {
            for (int i = 0; i < A.length; i++) {
                Term x = A[i];
                Term y = B[i];
                if (!x.equals(y)) {
                    if (x instanceof Inheritance && y instanceof Inheritance) {
                        if (!equalSubjectPredicateInRespectToImageAndProduct(x, y)) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                    if (x instanceof Similarity && y instanceof Similarity) {
                        if (!equalSubjectPredicateInRespectToImageAndProduct(x, y) && !equalSubjectPredicateInRespectToImageAndProduct(y, x)) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                    return false;
                }
            }
            return true;
        }
    }

    public static CompoundTerm ReduceTillLayer2(CompoundTerm itself, Term replacement, Memory memory) {
        if (!(itself != null))
            return null;
        
        Term reduced = reduceComponentOneLayer(itself, replacement, memory);
        if (!(reduced instanceof CompoundTerm))
            return null;
        
        itself = (CompoundTerm)reduced;
        int j = 0;
        for (Term t : itself.term) {
            Term t2 = unwrapNegation(t);
            if (!(t2 instanceof Implication) && !(t2 instanceof Equivalence) && !(t2 instanceof Conjunction) && !(t2 instanceof Disjunction)) {
                j++;
                continue;
            }
            Term ret2 = reduceComponentOneLayer((CompoundTerm) t2, replacement, memory);
            
            //CompoundTerm itselfCompound = itself;
            CompoundTerm replaced = null;
            if (j < itself.term.length  )
                replaced = (CompoundTerm) itself.setComponent(j, ret2, memory);
            
            if (replaced != null) {
                itself = replaced;
            }
            j++;
        }
        return itself;
    }
    /*
    @Deprecated public static Term make(final String op, final ArrayList<Term> arg, final Memory memory) {
    final int length = op.length();
    if (length == 1) {
    final char c = op.charAt(0);
    switch (c) {
    case Symbols.SET_EXT_OPENER:
    return SetExt.make(arg, memory);
    case Symbols.SET_INT_OPENER:
    return SetInt.make(arg, memory);
    case Symbols.INTERSECTION_EXT_OPERATORc:
    return IntersectionExt.make(arg, memory);
    case Symbols.INTERSECTION_INT_OPERATORc:
    return IntersectionInt.make(arg, memory);
    case Symbols.DIFFERENCE_EXT_OPERATORc:
    return DifferenceExt.make(arg, memory);
    case Symbols.DIFFERENCE_INT_OPERATORc:
    return DifferenceInt.make(arg, memory);
    case Symbols.PRODUCT_OPERATORc:
    return Product.make(arg, memory);
    case Symbols.IMAGE_EXT_OPERATORc:
    return ImageExt.make(arg, memory);
    case Symbols.IMAGE_INT_OPERATORc:
    return ImageInt.make(arg, memory);
    }
    }
    else if (length == 2) {
    //since these symbols are the same character repeated, we only need to compare the first character
    final char c1 = op.charAt(0);
    final char c2 = op.charAt(1);
    if (c1 == c2) {
    switch (c1) {
    case Symbols.NEGATION_OPERATORc:
    return Negation.make(arg, memory);
    case Symbols.DISJUNCTION_OPERATORc:
    return Disjunction.make(arg, memory);
    case Symbols.CONJUNCTION_OPERATORc:
    return Conjunction.make(arg, memory);
    }
    } else if (op.equals(Symbols.SEQUENCE_OPERATOR)) {
    return Conjunction.make(arg, TemporalRules.ORDER_FORWARD, memory);
    } else if (op.equals(Symbols.PARALLEL_OPERATOR)) {
    return Conjunction.make(arg, TemporalRules.ORDER_CONCURRENT, memory);
    }
    }
    throw new RuntimeException("Unknown Term operator: " + op);
    }
     */

    /**
     * Try to remove a component from a compound
     *
     * @param t1 The compound
     * @param t2 The component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public static Term reduceComponents(final CompoundTerm t1, final Term t2, final Memory memory) {
        final Term[] list;
        if (t1.getClass() == t2.getClass()) {
            list = t1.cloneTermsExcept(true, ((CompoundTerm) t2).term);
        } else {
            list = t1.cloneTermsExcept(true, t2);
        }
        if (list != null) {
            if (list.length > 1) {
                return memory.term(t1, list);
            }
            if (list.length == 1) {
                if ((t1 instanceof Conjunction) || (t1 instanceof Disjunction) || (t1 instanceof IntersectionExt) || (t1 instanceof IntersectionInt) || (t1 instanceof DifferenceExt) || (t1 instanceof DifferenceInt)) {
                    return list[0];
                }
            }
        }
        return null;
    }

    public static Term reduceComponentOneLayer(CompoundTerm t1, Term t2, Memory memory) {
        Term[] list;
        if (t1.getClass() == t2.getClass()) {
            list = t1.cloneTermsExcept(true, ((CompoundTerm) t2).term);
        } else {
            list = t1.cloneTermsExcept(true, t2);
        }
        if (list != null) {
            if (list.length > 1) {
                return memory.term(t1, list);
            } else if (list.length == 1) {
                return list[0];
            }
        }
        return t1;
    }


    public static Term unwrapNegation(final Term T) {
        if (T != null && T instanceof Negation) {
            return ((CompoundTerm) T).term[0];
        }
        return T;
    }

    public static boolean equalSubjectPredicateInRespectToImageAndProduct(final Term a, final Term b) {
        
        if (a == null || b == null) {
            return false;
        }
        
        if (!(a instanceof Statement) && !(b instanceof Statement)) {
            return false;
        }
        
        if (a.equals(b)) {
            return true;
        }
        
        Statement A = (Statement) a;
        Statement B = (Statement) b;
        
        if (!(A instanceof Similarity && B instanceof Similarity 
                || A instanceof Inheritance && B instanceof Inheritance))
            return false;
            
        Term subjA = A.getSubject();
        Term predA = A.getPredicate();
        Term subjB = B.getSubject();
        Term predB = B.getPredicate();

        Term ta = null, tb = null;
        Term sa = null, sb = null;

        if ((subjA instanceof Product) && (predB instanceof ImageExt)) {
            ta = predA; sa = subjA;
            tb = subjB; sb = predB;
        }
        if ((subjB instanceof Product) && (predA instanceof ImageExt)) {
            ta = subjA; sa = predA;
            tb = predB; sb = subjB;                
        }
        if ((predA instanceof ImageExt) && (predB instanceof ImageExt)) {
            ta = subjA; sa = predA;
            tb = subjB; sb = predB;                
        }
        //DUPLICATE?
        /*if ((predA instanceof ImageExt) && (predB instanceof ImageExt)) {
            ta = subjA; sa = predA;
            tb = subjB; sb = predB;
        }*/
        if ((subjA instanceof ImageInt) && (subjB instanceof ImageInt)) {
            ta = predA; sa = subjA;
            tb = predB; sb = subjB;
        }
        //ANOTHER DUPLICATE?
        /*
        if ((subjA instanceof ImageInt) && (subjB instanceof ImageInt)) {
                Set<Term> componentsA = new HashSet();
                Set<Term> componentsB = new HashSet();
                componentsA.add(predA);
                componentsB.add(predB);
                componentsA.addAll(Arrays.asList(((CompoundTerm) subjA).term));
                componentsB.addAll(Arrays.asList(((CompoundTerm) subjB).term));
                if (componentsA.containsAll(componentsB)) {
                    return true;
                }

        }
        */
        if ((predA instanceof Product) && (subjB instanceof ImageInt)) {
            ta = subjA; sa = predA;
            tb = predB; sb = subjB;                
        }
        if ((predB instanceof Product) && (subjA instanceof ImageInt)) {
            ta = predA; sa = subjA;
            tb = subjB; sb = predB;
        }

        if (ta!=null) {
            Term[] sat = ((CompoundTerm)sa).term;
            Term[] sbt = ((CompoundTerm)sb).term;

            Set<Term> componentsA = new HashSet(1+sat.length);
            Set<Term> componentsB = new HashSet(1+sbt.length);

            componentsA.add(ta);
            Collections.addAll(componentsA, sat);

            componentsB.add(tb);
            Collections.addAll(componentsB, sbt);

            if (componentsA.containsAll(componentsB)) {
                return true;
            }
        }
            
        return false;
    }

    /**
     * Collect TermLink templates into a list, go down one level except in
     * special cases
     * <p>
     *
     * @param componentLinks The list of TermLink templates built so far
     * @param type The type of TermLink to be built
     * @param term The CompoundTerm for which the links are built
     */
    public static ArrayList<TermLink> prepareComponentLinks(final ArrayList<TermLink> componentLinks, final short type, final CompoundTerm t) {
        for (int i = 0; i < t.size(); i++) {
            final Term t1 = t.term[i];
            
            
            if (t1.isConstant()) {
                componentLinks.add(new TermLink(type, t1, i));
            }
            if (((t instanceof Equivalence) || ((t instanceof Implication) && (i == 0))) && ((t1 instanceof Conjunction) || (t1 instanceof Negation))) {
                prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION, (CompoundTerm) t1);
            } else if (t1 instanceof CompoundTerm) {
                final CompoundTerm ct1 = (CompoundTerm)t1;
                final int ct1Size = ct1.size(); //cache because this loop is critical
                for (int j = 0; j < ct1Size; j++) {
                    final Term t2 = ct1.term[j];
                    if (t2.isConstant()) {
                        if ((t1 instanceof Product) || (t1 instanceof ImageExt) || (t1 instanceof ImageInt)) {
                            if (type == TermLink.COMPOUND_CONDITION) {
                                componentLinks.add(new TermLink(TermLink.TRANSFORM, t2, 0, i, j));
                            } else {
                                componentLinks.add(new TermLink(TermLink.TRANSFORM, t2, i, j));
                            }
                        } else {
                            componentLinks.add(new TermLink(type, t2, i, j));
                        }
                    }
                    if ((t2 instanceof Product) || (t2 instanceof ImageExt) || (t2 instanceof ImageInt)) {
                        CompoundTerm ct2 = (CompoundTerm)t2;
                        final int ct2Size = ct2.size();
                        
                        for (int k = 0; k < ct2Size; k++) {
                            final Term t3 = ct2.term[k];
                            
                            if (t3.isConstant()) {
                                if (type == TermLink.COMPOUND_CONDITION) {
                                    componentLinks.add(new TermLink(TermLink.TRANSFORM, t3, 0, i, j, k));
                                } else {
                                    componentLinks.add(new TermLink(TermLink.TRANSFORM, t3, i, j, k));
                                }
                            }
                        }
                    }
                }
            }
        }
        return componentLinks;
    }

   public  static ArrayList<TermLink> prepareComponentLinks(ArrayList<TermLink> componentLinks, CompoundTerm ct) {
        short type = (ct instanceof Statement) ? TermLink.COMPOUND_STATEMENT : TermLink.COMPOUND;   // default
        return prepareComponentLinks(componentLinks, type, ct);
    }

    //TODO move this to a utility method
    public static <T> int indexOf(final T[] array, final T v) {
        /*if (v == null) {
        for (final T e : array)
        if (e == null)
        return true;
        } else {*/
        int i = 0;
        for (final T e : array) {
            if (v.equals(e)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    //TODO move this to a utility method
    public static <T> boolean containsAll(final T[] array, final T v) {
        for (final T e : array) {
            if (!v.equals(e)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array) {
            if (v.equals(e)) {
                return true;
            }
        }
        return false;
    }

    
}
