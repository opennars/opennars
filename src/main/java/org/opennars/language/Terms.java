/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import org.opennars.entity.Sentence;
import org.opennars.entity.TermLink;
import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols;
import org.opennars.storage.Memory;

import java.util.*;

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
        final Term[] A = ((CompoundTerm) a).term;
        final Term[] B = ((CompoundTerm) b).term;
        if (A.length != B.length || !(a.operator().equals(b.operator()))) {
            return false;
        } else {
            for (int i = 0; i < A.length; i++) {
                final Term x = A[i];
                final Term y = B[i];
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

    public static Term reduceUntilLayer2(final CompoundTerm _itself, final Term replacement, final Memory memory) {
        if (_itself == null)
            return null;
        
        final Term reduced = reduceComponentOneLayer(_itself, replacement, memory);
        if (!(reduced instanceof CompoundTerm))
            return null;
        
        CompoundTerm itself = (CompoundTerm)reduced;
        int j = 0;
        for (final Term t : itself.term) {
            final Term t2 = unwrapNegation(t);
            if (!(t2 instanceof Implication) && !(t2 instanceof Equivalence) && !(t2 instanceof Conjunction) && !(t2 instanceof Disjunction)) {
                j++;
                continue;
            }
            final Term ret2 = reduceComponentOneLayer((CompoundTerm) t2, replacement, memory);
            
            //CompoundTerm itselfCompound = itself;
            Term replaced = null;
            if (j < itself.term.length  )
                replaced = itself.setComponent(j, ret2, memory);
            
            if (replaced != null) {
                if (replaced instanceof CompoundTerm)
                    itself = (CompoundTerm)replaced;
                else
                    return replaced;
            }
            j++;
        }
        return itself;
    }

    /* static methods making new compounds, which may return null */
    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @return A compound term or null
     */
    public static Term term(final CompoundTerm compound, final Term[] components) {
        if (compound instanceof ImageExt) {
            return new ImageExt(components, ((Image) compound).relationIndex);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((Image) compound).relationIndex);
        } else {
            return term(compound.operator(), components);
        }
    }

    public static Term term(final CompoundTerm compound, final Collection<Term> components) {
        final Term[] c = components.toArray(new Term[0]);
        return term(compound, c);
    }
    

    /**
     * Try to make a compound term from an operator and a list of term
     * <p>
     * Called from StringParser
     *
     * @param copula Term operator
     * @param componentList Component list
     * @return A term or null
     */
    public static Term term(final Symbols.NativeOperator copula, final Term[] componentList) {
        
        switch (copula) {
            
            case SET_EXT_OPENER:
                return SetExt.make(componentList);
            case SET_INT_OPENER:
                return SetInt.make(componentList);
            case INTERSECTION_EXT:
                return IntersectionExt.make(componentList);
            case INTERSECTION_INT:
                return IntersectionInt.make(componentList);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(componentList);
            case DIFFERENCE_INT:
                return DifferenceInt.make(componentList);
            case INHERITANCE:
                return Inheritance.make(componentList[0], componentList[1]);
            case PRODUCT:
                return new Product(componentList);
            case IMAGE_EXT:
                return ImageExt.make(componentList);
            case IMAGE_INT:
                return ImageInt.make(componentList);
            case NEGATION:
                return Negation.make(componentList);
            case DISJUNCTION:
                return Disjunction.make(componentList);
            case CONJUNCTION:
                return Conjunction.make(componentList);
            case SEQUENCE:
                return Conjunction.make(componentList, TemporalRules.ORDER_FORWARD);
            case SPATIAL:
                return Conjunction.make(componentList, TemporalRules.ORDER_FORWARD, true);
            case PARALLEL:
                return Conjunction.make(componentList, TemporalRules.ORDER_CONCURRENT);
            case IMPLICATION:
                return Implication.make(componentList[0], componentList[1]);
            case IMPLICATION_AFTER:
                return Implication.make(componentList[0], componentList[1], TemporalRules.ORDER_FORWARD);
            case IMPLICATION_BEFORE:
                return Implication.make(componentList[0], componentList[1], TemporalRules.ORDER_BACKWARD);
            case IMPLICATION_WHEN:
                return Implication.make(componentList[0], componentList[1], TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE:
                return Equivalence.make(componentList[0], componentList[1]);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(componentList[0], componentList[1], TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(componentList[0], componentList[1], TemporalRules.ORDER_FORWARD);
            default:
                throw new IllegalStateException("Unknown Term operator: " + copula + " (" + copula.name() + ")");
        }
    }

    /**
     * Try to remove a component from a compound
     *
     * @param compound The compound
     * @param component The component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public static Term reduceComponents(final CompoundTerm compound, final Term component, final Memory memory) {
        final Term[] list;
        if (compound.getClass() == component.getClass()) {
            list = compound.cloneTermsExcept(true, ((CompoundTerm) component).term);
        } else {
            list = compound.cloneTermsExcept(true, new Term[] { component });
        }
        if (list != null) {
            if (list.length > 1) {
                return term(compound, list);
            }
            if (list.length == 1) {
                if ((compound instanceof Conjunction) || (compound instanceof Disjunction) || (compound instanceof IntersectionExt) || (compound instanceof IntersectionInt) || (compound instanceof DifferenceExt) || (compound instanceof DifferenceInt)) {
                    return list[0];
                }
            }
        }
        return null;
    }

    public static Term reduceComponentOneLayer(final CompoundTerm compound, final Term component, final Memory memory) {
        final Term[] list;
        if (compound.getClass() == component.getClass()) {
            list = compound.cloneTermsExcept(true, ((CompoundTerm) component).term);
        } else {
            list = compound.cloneTermsExcept(true, new Term[] { component });
        }
        if (list != null) {
            if (list.length > 1) {
                return term(compound, list);
            } else if (list.length == 1) {
                return list[0];
            }
        }
        return compound;
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
        
        final Statement A = (Statement) a;
        final Statement B = (Statement) b;
        
        if (!(A instanceof Similarity && B instanceof Similarity 
                || A instanceof Inheritance && B instanceof Inheritance))
            return false;
            
        final Term subjA = A.getSubject();
        final Term predA = A.getPredicate();
        final Term subjB = B.getSubject();
        final Term predB = B.getPredicate();

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
        if ((subjA instanceof ImageInt) && (subjB instanceof ImageInt)) {
            ta = predA; sa = subjA;
            tb = predB; sb = subjB;
        }
        if ((predA instanceof Product) && (subjB instanceof ImageInt)) {
            ta = subjA; sa = predA;
            tb = predB; sb = subjB;                
        }
        if ((predB instanceof Product) && (subjA instanceof ImageInt)) {
            ta = predA; sa = subjA;
            tb = subjB; sb = predB;
        }

        if (ta==null) {
            return false;
        }

        final Term[] sat = ((CompoundTerm)sa).term;
        final Term[] sbt = ((CompoundTerm)sb).term;

        if(sa instanceof Image && sb instanceof Image) {
            final Image im1=(Image) sa;
            final Image im2=(Image) sb;
            if(im1.relationIndex != im2.relationIndex) {
                return false;
            }
        }

        final Set<Term> componentsA = new HashSet(1+sat.length);
        final Set<Term> componentsB = new HashSet(1+sbt.length);

        componentsA.add(ta);
        Collections.addAll(componentsA, sat);

        componentsB.add(tb);
        Collections.addAll(componentsB, sbt);

        for(final Term sA : componentsA) {
            boolean had=false;
            for(final Term sB : componentsB) {
                if(sA instanceof Variable && sB instanceof Variable) {
                    if(sA.name.equals(sB.name)) {
                        had=true;
        }
                }
                else
                if(sA.equals(sB)) {
                    had=true;
                }
            }
            if(!had) {
                return false;
            }
        }
            
        return true;
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
    public static List<TermLink> prepareComponentLinks(final List<TermLink> componentLinks, final short type, final CompoundTerm term) {
        
        final boolean tEquivalence = (term instanceof Equivalence);
        final boolean tImplication = (term instanceof Implication);
        
        for (int i = 0; i < term.size(); i++) {
            Term t1 = term.term[i];
            t1=new Sentence(
                t1,
                Symbols.TERM_NORMALIZING_WORKAROUND_MARK,
                null,
                null).term;
            
            
            if (!(t1 instanceof Variable)) {
                componentLinks.add(new TermLink(type, t1, i));
            }
            if ((tEquivalence || (tImplication && (i == 0))) && ((t1 instanceof Conjunction) || (t1 instanceof Negation))) {
                prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION, (CompoundTerm) t1);
            } else if (t1 instanceof CompoundTerm) {
                final CompoundTerm ct1 = (CompoundTerm)t1;
                final int ct1Size = ct1.size(); //cache because this loop is critical
                final boolean t1ProductOrImage = (t1 instanceof Product) || (t1 instanceof ImageExt) || (t1 instanceof ImageInt);
                
                for (int j = 0; j < ct1Size; j++) {
                    Term t2 = ct1.term[j];

                    t2 = new Sentence(
                        t2,
                        Symbols.TERM_NORMALIZING_WORKAROUND_MARK,
                        null,
                        null).term;

                    if (!t2.hasVar()) {
                        if (t1ProductOrImage) {
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
                        final CompoundTerm ct2 = (CompoundTerm)t2;
                        final int ct2Size = ct2.size();
                        
                        for (int k = 0; k < ct2Size; k++) {
                            Term t3 = ct2.term[k];

                            t3 = new Sentence(
                                t3,
                                Symbols.TERM_NORMALIZING_WORKAROUND_MARK,
                                null,
                                null).term;

                            if (!t3.hasVar()) {
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

   public  static List<TermLink> prepareComponentLinks(final List<TermLink> componentLinks, final CompoundTerm ct) {
        final short type = (ct instanceof Statement) ? TermLink.COMPOUND_STATEMENT : TermLink.COMPOUND;   // default
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

    /** compres a set of terms (assumed to be unique) with another set to find if their
     * contents match. they can be in different order and still match.  this is useful for
     * comparing whether compound terms in which order doesn't matter (ex: conjunction)
     * are equivalent.
     */ 
    public static <T> boolean containsAll(final T[] a, final T[] b) {
        for (final T ax : a) {
            if (!contains(b, ax))
                return false;
        }
        return true;
    }
    
    /** a contains any of b  NOT TESTED YET */
    public static boolean containsAny(final Term[] a, final Collection<Term> b) {
        for (final Term bx : b) {
            if (contains(a, bx))
                return true;
        }
        for (final Term ax : a) {
            if (ax instanceof CompoundTerm)
                if (containsAny(((CompoundTerm)ax).term, b))
                    return true;
        }
        
        return false;
    }

    public static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array) {
            if (v.equals(e)) {
                return true;
            }
        }
        return false;
    }

    static boolean equals(final Term[] a, final Term[] b) {
        if (a.length!=b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i]))
                return false;            
        }
        return true;
    }

    public static void verifyNonNull(final Collection t) {
        for (final Object o : t)
            if (o == null)
                throw new IllegalStateException("Element null in: " + t);
    }

    static void verifyNonNull(final Term... t) {
        for (final Object o : t)
            if (o == null)
                throw new IllegalStateException("Element null in: " + Arrays.toString(t));
    }    
    
    public static Term[] verifySortedAndUnique(final Term[] arg, final boolean allowSingleton) {
        if (arg.length == 0) {
            throw new IllegalStateException("Needs >0 components");
        }
        if (!allowSingleton && (arg.length == 1)) {
            throw new IllegalStateException("Needs >1 components: " + Arrays.toString(arg));
        }
        final Term[] s = Term.toSortedSetArray(arg);
        if (arg.length!=s.length) {
            throw new IllegalStateException("Contains duplicates: " + Arrays.toString(arg));
        }
        int j = 0;
        for (final Term t : s) {
            if (!t.equals(arg[j++]))
                throw new IllegalStateException("Un-ordered: " + Arrays.toString(arg) + " , correct order=" + Arrays.toString(s));
        }        
        return s;
    }    
}
