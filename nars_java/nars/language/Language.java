package nars.language;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nars.entity.TermLink;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.*;
import nars.storage.Memory;


public class Language {

    public static boolean EqualSubTermsInRespectToImageAndProduct(final Term a, final Term b) {
        if (a == null || b == null) {
            return false;
        }
        if (!((a instanceof CompoundTerm) && (b instanceof CompoundTerm))) {
            return a.equals(b);
        }
        if (a instanceof Inheritance && b instanceof Inheritance) {
            return EqualSubjectPredicateInRespectToImageAndProduct(a, b);
        }
        if (a instanceof Similarity && b instanceof Similarity) {
            return EqualSubjectPredicateInRespectToImageAndProduct(a, b) || EqualSubjectPredicateInRespectToImageAndProduct(b, a);
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
                        if (!EqualSubjectPredicateInRespectToImageAndProduct(x, y)) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                    if (x instanceof Similarity && y instanceof Similarity) {
                        if (!EqualSubjectPredicateInRespectToImageAndProduct(x, y) && !EqualSubjectPredicateInRespectToImageAndProduct(y, x)) {
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
        if (!(itself instanceof CompoundTerm)) {
            return null;
        }
        itself = (CompoundTerm) reduceComponentOneLayer(itself, replacement, memory);
        int j = 0;
        for (Term t : itself.term) {
            Term t2 = unwrapNegation(t);
            if (!(t2 instanceof Implication) && !(t2 instanceof Equivalence) && !(t2 instanceof Conjunction) && !(t2 instanceof Disjunction)) {
                j++;
                continue;
            }
            Term ret2 = reduceComponentOneLayer((CompoundTerm) t2, replacement, memory);
            CompoundTerm replaced = (CompoundTerm) CompoundTerm.setComponent((CompoundTerm) itself, j, ret2, memory);
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
            case EQUIVALENCE:
                return Equivalence.make(a[0], a[1], memory);
        }
        throw new RuntimeException("Unknown Term operator: " + op + " (" + op.name() + ")");
    }

    //3 helper functions for dedSecondLayerVariableUnification:
    public static Term unwrapNegation(Term T) {
        if (T != null && T instanceof Negation) {
            return ((CompoundTerm) T).term[0];
        }
        return T;
    }

    public static boolean EqualSubjectPredicateInRespectToImageAndProduct(final Term a, final Term b) {
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
        if (A instanceof Similarity && B instanceof Similarity || A instanceof Inheritance && B instanceof Inheritance) {
            Term subjA = A.getSubject();
            Term predA = A.getPredicate();
            Term subjB = B.getSubject();
            Term predB = B.getPredicate();
            if (subjA instanceof Product) {
                if (predB instanceof ImageExt) {
                    Set<Term> componentsA = new HashSet();
                    componentsA.add(predA);
                    componentsA.addAll(Arrays.asList(((CompoundTerm) subjA).term));
                    Set<Term> componentsB = new HashSet();
                    componentsB.add(subjB);
                    componentsB.addAll(Arrays.asList(((CompoundTerm) predB).term));
                    if (componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            if (subjB instanceof Product) {
                if (predA instanceof ImageExt) {
                    Set<Term> componentsA = new HashSet();
                    componentsA.add(subjA);
                    componentsA.addAll(Arrays.asList(((CompoundTerm) predA).term));
                    Set<Term> componentsB = new HashSet();
                    componentsB.add(predB);
                    componentsB.addAll(Arrays.asList(((CompoundTerm) subjB).term));
                    if (componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            if (predA instanceof ImageExt) {
                if (predB instanceof ImageExt) {
                    Set<Term> componentsA = new HashSet();
                    Set<Term> componentsB = new HashSet();
                    componentsA.add(subjA);
                    componentsB.add(subjB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm) predA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm) predB).term));
                    if (componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            if (predA instanceof ImageExt) {
                if (predB instanceof ImageExt) {
                    Set<Term> componentsA = new HashSet();
                    Set<Term> componentsB = new HashSet();
                    componentsA.add(subjA);
                    componentsB.add(subjB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm) predA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm) predB).term));
                    if (componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            if (subjA instanceof ImageInt) {
                if (subjB instanceof ImageInt) {
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
            }
            if (subjA instanceof ImageInt) {
                if (subjB instanceof ImageInt) {
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
            }
            if (predA instanceof Product) {
                if (subjB instanceof ImageInt) {
                    Set<Term> componentsA = new HashSet();
                    Set<Term> componentsB = new HashSet();
                    componentsA.add(subjA);
                    componentsB.add(predB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm) predA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm) subjB).term));
                    if (componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            if (predB instanceof Product) {
                if (subjA instanceof ImageInt) {
                    Set<Term> componentsA = new HashSet();
                    Set<Term> componentsB = new HashSet();
                    componentsA.add(predA);
                    componentsB.add(subjB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm) subjA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm) predB).term));
                    if (componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
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

    /**
     * Check whether a string represent a name of a term that contains a
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a variable
     */
    public static boolean containVar(final String n) {
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            char c = n.charAt(i);
            if ((c == Symbols.VAR_INDEPENDENT) || (c == Symbols.VAR_DEPENDENT) || (c == Symbols.VAR_QUERY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a string represent a name of a term that contains a
     * dependent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a dependent variable
     */
    public static boolean containVarDep(final String n) {
        return n.indexOf(Symbols.VAR_DEPENDENT) >= 0;
    }

    /**
     * Check whether a string represent a name of a term that contains an
     * independent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains an independent variable
     */
    public static boolean containVarIndep(final String n) {
        return n.indexOf(Symbols.VAR_INDEPENDENT) >= 0;
    }

    /**
     * Check whether a string represent a name of a term that contains a query
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a query variable
     */
    public static boolean containVarQuery(final String n) {
        return n.indexOf(Symbols.VAR_QUERY) >= 0;
    }

    public static boolean containDepOrIndepVar(final String n) {
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            char c = n.charAt(i);
            if ((c == Symbols.VAR_INDEPENDENT) || (c == Symbols.VAR_DEPENDENT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether the unification is possible
     */
    public static boolean unify(final char type, final Term t1, final Term t2) {
        return unify(type, t1, t2, t1, t2);
    }

    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t1 The first term to be unified
     * @param t2 The second term to be unified
     * @param compound1 The compound containing the first term
     * @param compound2 The compound containing the second term
     * @return Whether the unification is possible
     */
    public static boolean unify(final char type, final Term t1, final Term t2, final Term compound1, final Term compound2) {
        final HashMap<Term, Term> map1 = new HashMap<>();
        final HashMap<Term, Term> map2 = new HashMap<>();
        final boolean hasSubs = findSubstitute(type, t1, t2, map1, map2);
        if (hasSubs) {
            if (!map1.isEmpty()) {
                ((CompoundTerm) compound1).applySubstitute(map1);
                compound1.renameVariables();
            }
            if (!map2.isEmpty()) {
                ((CompoundTerm) compound2).applySubstitute(map2);
                compound2.renameVariables();
            }
        }
        return hasSubs;
    }

    /**
     * To recursively find a substitution that can unify two Terms without
     * changing them
     *
     * @param type The type of Variable to be substituted
     * @param term1 The first Term to be unified
     * @param term2 The second Term to be unified
     * @param map1 The substitution for term1 formed so far
     * @param map2 The substitution for term2 formed so far
     * @return Whether there is a substitution that unifies the two Terms
     */
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final HashMap<Term, Term> map1, final HashMap<Term, Term> map2) {
        Term t;
        if ((term1 instanceof Variable) && (((Variable) term1).getType() == type)) {
            final Variable var1 = (Variable) term1;
            t = map1.get(var1);
            if (t != null) {
                return findSubstitute(type, t, term2, map1, map2);
            } else {
                if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
                    Variable CommonVar = makeCommonVariable(term1, term2);
                    map1.put(var1, CommonVar);
                    map2.put(term2, CommonVar);
                } else {
                    map1.put(var1, term2);
                    if (isCommonVariable(var1)) {
                        map2.put(var1, term2);
                    }
                }
                return true;
            }
        } else if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
            final Variable var2 = (Variable) term2;
            t = map2.get(var2);
            if (t != null) {
                return findSubstitute(type, term1, t, map1, map2);
            } else {
                map2.put(var2, term1);
                if (isCommonVariable(var2)) {
                    map1.put(var2, term1);
                }
                return true;
            }
        } else if ((term1 instanceof CompoundTerm) && term1.getClass().equals(term2.getClass())) {
            final CompoundTerm cTerm1 = (CompoundTerm) term1;
            final CompoundTerm cTerm2 = (CompoundTerm) term2;
            if (cTerm1.size() != (cTerm2).size()) {
                return false;
            }
            if ((cTerm1 instanceof ImageExt) && (((ImageExt) cTerm1).relationIndex != ((ImageExt) cTerm2).relationIndex) || (cTerm1 instanceof ImageInt) && (((ImageInt) cTerm1).relationIndex != ((ImageInt) cTerm2).relationIndex)) {
                return false;
            }
            Term[] list = cTerm1.cloneTerms();
            if (cTerm1.isCommutative()) {
                CompoundTerm.shuffle(list, Memory.randomNumber);
            }
            for (int i = 0; i < cTerm1.size(); i++) {
                Term t1 = list[i];
                Term t2 = cTerm2.term[i];
                if (!findSubstitute(type, t1, t2, map1, map2)) {
                    return false;
                }
            }
            return true;
        }
        return term1.equals(term2);
    }

    private static Variable makeCommonVariable(final Term v1, final Term v2) {
        return new Variable(v1.getName() + v2.getName() + '$');
    }

    private static boolean isCommonVariable(final Variable v) {
        String s = v.getName();
        return s.charAt(s.length() - 1) == '$';
    }

    /**
     * Check if two terms can be unified
     *
     * @param type The type of variable that can be substituted
     * @param term1 The first term to be unified
     * @param term2 The second term to be unified
     * @return Whether there is a substitution
     */
    public static boolean hasSubstitute(final char type, final Term term1, final Term term2) {
        return findSubstitute(type, term1, term2, new HashMap<Term, Term>(), new HashMap<Term, Term>());
    }
    
}
