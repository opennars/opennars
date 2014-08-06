package nars.language;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nars.entity.TermLink;
import nars.inference.TemporalRules;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.*;
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
        if (!(itself instanceof CompoundTerm))
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
            
            CompoundTerm itselfCompound = (CompoundTerm)itself;
            CompoundTerm replaced = null;
            if (j < itself.term.length  )
                replaced = (CompoundTerm) CompoundTerm.setComponent(
                    (CompoundTerm) itself, j, ret2, memory);
            
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
                return make(t1, list, memory);
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
                return make(t1, list, memory);
            } else if (list.length == 1) {
                if (t1 instanceof CompoundTerm) {
                    return list[0];
                }
            }
        }
        return t1;
    }

    /* static methods making new compounds, which may return null */
    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @param memory Reference to the memory
     * @return A compound term or null
     */
    public static Term make(final CompoundTerm compound, final Term[] components, final Memory memory) {
        if (compound instanceof ImageExt) {
            return ImageExt.make(components, ((ImageExt) compound).relationIndex, memory);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((ImageInt) compound).relationIndex, memory);
        } else {
            return make(compound.operator(), components, memory);
        }
    }

    public static Term make(final CompoundTerm compound, Collection<Term> components, final Memory memory) {
        Term[] c = components.toArray(new Term[components.size()]);
        return make(compound, c, memory);
    }

    /**
     * Try to make a compound term from an operator and a list of term
     * <p>
     * Called from StringParser
     *
     * @param op Term operator
     * @param arg Component list
     * @param memory Reference to the memory
     * @return A compound term or null
     */
    public static Term make(final NativeOperator op, final Term[] a, final Memory memory) {
        switch (op) {
            case SET_EXT_OPENER:
                return SetExt.make(CompoundTerm.termList(a), memory);
            case SET_INT_OPENER:
                return SetInt.make(CompoundTerm.termList(a), memory);
            
            case INTERSECTION_EXT:
                return IntersectionExt.make(CompoundTerm.termList(a), memory);
            case INTERSECTION_INT:
                return IntersectionInt.make(CompoundTerm.termList(a), memory);
            
            case DIFFERENCE_EXT:
                return DifferenceExt.make(a, memory);
            case DIFFERENCE_INT:
                return DifferenceInt.make(a, memory);
            
            case INHERITANCE:
                return Inheritance.make(a[0], a[1], memory);
            
            case PRODUCT:
                return Product.make(a, memory);
            
            case IMAGE_EXT:
                return ImageExt.make(a, memory);
            case IMAGE_INT:
                return ImageInt.make(a, memory);
            
            case NEGATION:
                return Negation.make(a, memory);
            
            case DISJUNCTION:
                return Disjunction.make(CompoundTerm.termList(a), memory);
            case CONJUNCTION:
                return Conjunction.make(a, memory);
            
            case SEQUENCE:
                return Conjunction.make(a, TemporalRules.ORDER_FORWARD, memory);
            case PARALLEL:
                return Conjunction.make(a, TemporalRules.ORDER_CONCURRENT, memory);
            
            case IMPLICATION:
                return Implication.make(a[0], a[1], memory);
            case IMPLICATION_AFTER:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_FORWARD, memory);
            case IMPLICATION_BEFORE:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_BACKWARD, memory);
            case IMPLICATION_WHEN:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT, memory);

            case EQUIVALENCE:
                return Equivalence.make(a[0], a[1], memory);            
            case EQUIVALENCE_WHEN:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT, memory);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_FORWARD, memory);
            
        }
        throw new RuntimeException("Unknown Term operator: " + op + " (" + op.name() + ")");
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
            for (final Term x : sat) 
                componentsA.add(x);

            componentsB.add(tb);
            for (final Term x : sbt) 
                componentsB.add(x);

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
    public static List<TermLink> prepareComponentLinks(final List<TermLink> componentLinks, final short type, final CompoundTerm t) {
        for (int i = 0; i < t.size(); i++) {
            final Term t1 = t.term[i];
            if (t1.isConstant()) {
                componentLinks.add(new TermLink(type, t1, i));
            }
            if (((t instanceof Equivalence) || ((t instanceof Implication) && (i == 0))) && ((t1 instanceof Conjunction) || (t1 instanceof Negation))) {
                prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION, (CompoundTerm) t1);
            } else if (t1 instanceof CompoundTerm) {
                for (int j = 0; j < ((CompoundTerm) t1).size(); j++) {
                    final Term t2 = ((CompoundTerm) t1).term[j];
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
                        for (int k = 0; k < ((CompoundTerm) t2).size(); k++) {
                            final Term t3 = ((CompoundTerm) t2).term[k];
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

   public  static List<TermLink> prepareComponentLinks(List<TermLink> componentLinks, CompoundTerm ct) {
        short type = (ct instanceof Statement) ? TermLink.COMPOUND_STATEMENT : TermLink.COMPOUND;   // default
        return prepareComponentLinks(componentLinks, type, ct);
    }

    
}
