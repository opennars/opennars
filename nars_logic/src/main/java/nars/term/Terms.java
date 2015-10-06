package nars.term;

import nars.Global;
import nars.Op;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Instance;
import nars.nal.nal2.InstanceProperty;
import nars.nal.nal2.Property;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.*;
import nars.nal.nal4.Image;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.ImageInt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Disjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Temporal;
import nars.util.data.sorted.SortedList;

import java.util.*;

/**
 * Static utility class for static methods related to Terms
 * @author me
 */
public class Terms {

    public final static Term[] EmptyTermArray = new Term[0];


//    /** use this instead of .getClass() == .getClass() comparisons, to allow for different implementations of the same essential type */
//    public static final boolean equalType(final Term a, final Term b, final boolean operator, final boolean temporalOrder) {
//        if (operator) {
//            if (!equalType(a, b)) return false;
//        }
//        if (temporalOrder) {
//            if (!TemporalRules.matchingOrder(a.getTemporalOrder(), b.getTemporalOrder())) {
//                return false;
//            }
//        }
//        return true;
//    }
//

    public static boolean equalSubTermsInRespectToImageAndProduct(final Term a, final Term b) {
        if (a == null || b == null) {
            return false;
        }
        if (!((a instanceof Compound) && (b instanceof Compound))) {
            return a.equals(b);
        }
        if (a instanceof Inheritance && b instanceof Inheritance) {
            return equalSubjectPredicateInRespectToImageAndProduct(a, b);
        }
        if (a instanceof Similarity && b instanceof Similarity) {
            return equalSubjectPredicateInRespectToImageAndProduct(a, b) || equalSubjectPredicateInRespectToImageAndProduct(b, a);
        }
        Term[] A = ((Compound) a).term;
        Term[] B = ((Compound) b).term;
        if (A.length != B.length)
            return false;

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

//    public static Term reduceUntilLayer2(final Compound _itself, Term replacement, Memory memory) {
//        if (_itself == null)
//            return null;
//
//        Term reduced = reduceComponentOneLayer(_itself, replacement, memory);
//        if (!(reduced instanceof Compound))
//            return null;
//
//        Compound itself = (Compound)reduced;
//        int j = 0;
//        for (Term t : itself.term) {
//            Term t2 = unwrapNegation(t);
//            if (!(t2 instanceof Implication) && !(t2 instanceof Equivalence) && !(t2 instanceof Junction)) {
//                j++;
//                continue;
//            }
//            Term ret2 = reduceComponentOneLayer((Compound) t2, replacement, memory);
//
//            //CompoundTerm itselfCompound = itself;
//            Term replaced = null;
//            if (j < itself.term.length  )
//                replaced = itself.cloneReplacingSubterm(j, ret2);
//
//            if (replaced != null) {
//                if (replaced instanceof Compound)
//                    itself = (Compound)replaced;
//                else
//                    return replaced;
//            }
//            j++;
//        }
//        return itself;
//    }
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
    throw new RuntimeException("Unknown Term operate: " + op);
    }
     */

//    /**
//     * Try to remove a component from a compound
//     *
//     * @param t1 The compound
//     * @param t2 The component
//     * @param memory Reference to the memory
//     * @return The new compound
//     */
//    public static Term reduceComponents(final Compound t1, final Term t2, final Memory memory) {
//
//        final Term[] list;
//
//        if ((t1.op() == t2.op()))  {
//            list = t1.cloneTermsExcept(true, ((Compound) t2).term);
//        } else {
//            list = t1.cloneTermsExcept(true, t2);
//        }
//
//        if (list == null)
//            return null;
//
//        if (list.length == 1)  {
//            if ((t1 instanceof Junction) || (t1 instanceof Intersect) || (t1 instanceof Difference)) {
//                return list[0];
//            }
//
//        }
//        //else  /*if (list.length > 1)*/ {
//            return Memory.term(t1, list);
//        //}
//
//    }

//    public static Term reduceComponentOneLayer(Compound t1, Term t2, Memory memory) {
//        Term[] list;
//        if ((t1.op() == t2.op())) {
//            list = t1.cloneTermsExcept(true, ((Compound) t2).term);
//        } else {
//            list = t1.cloneTermsExcept(true, t2);
//        }
//        if (list != null) {
//            if (list.length > 1) {
//                return term(t1, list);
//            } else if (list.length == 1) {
//                return list[0];
//            }
//        }
//        return t1;
//    }
//
//
//    public static Term unwrapNegation(final Term T) {
//        if (T != null && T instanceof Negation) {
//            return ((Compound) T).term[0];
//        }
//        return T;
//    }

    public static boolean equalSubjectPredicateInRespectToImageAndProduct(final Term a, final Term b) {
        return equalSubjectPredicateInRespectToImageAndProduct(a, b, true);
    }

    public static boolean equalSubjectPredicateInRespectToImageAndProduct(final Term a, final Term b, boolean requireEqualImageRelation) {
        
        if (a == null || b == null) {
            return false;
        }
        
        if (!(a instanceof Statement) || !(b instanceof Statement)) {
            return false;
        }
        
        if (a.equals(b)) {
            return true;
        }
        
        Statement A = (Statement) a;
        Statement B = (Statement) b;
        
        /*
        //REMOVED this prevents the non-statement cases further down
            "if ((predA instanceof Product) && (subjB instanceof ImageInt))"
            "if ((predB instanceof Product) && (subjA instanceof ImageInt))"
                --both will never occur since this test prevents them
        if (!((A instanceof Similarity && B instanceof Similarity)
                || (A instanceof Inheritance && B instanceof Inheritance)))
            return false;*/
            
        Term subjA = A.getSubject();
        Term predA = A.getPredicate();
        Term subjB = B.getSubject();
        Term predB = B.getPredicate();

        Term ta = null, tb = null; //the compound term to put itself in the comparison set
        Term sa = null, sb = null; //the compound term to put its components in the comparison set

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

        if (ta==null)
            return false;

        Term[] sat = ((Compound)sa).term;
        Term[] sbt = ((Compound)sb).term;

        //original code did not check relation index equality
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/language/CompoundTerm.java
        if (requireEqualImageRelation) {
            if (sa instanceof Image && sb instanceof Image) {
                if(((Image) sa).relationIndex != ((Image) sb).relationIndex) {
                    return false;
                }
            }
        }

        return containsAll(sat, ta, sbt, tb);

        /*
        for(Term sA : componentsA) {
            boolean had=false;
            for(Term sB : componentsB) {
                if(sA instanceof Variable && sB instanceof Variable) {
                    if(sA.name().equals(sB.name())) {
                        had=true;
                    }
                }
                else if(sA.equals(sB)) {
                    had=true;
                }
            }
            if(!had) {
                return false;
            }
        }
        */
    }

    private static boolean containsAll(Term[] sat, Term ta, Term[] sbt, Term tb) {
        //temporary set for fast containment check
        Set<Term> componentsA = Global.newHashSet(sat.length + 1);
        componentsA.add(ta);
        Collections.addAll(componentsA, sat);

        //test A contains B
        if (!componentsA.contains(tb))
            return false;
        for (Term bComponent : sbt)
            if (!componentsA.contains(bComponent))
                return false;

        return true;
    }



    //TODO move this to a utility method
    public static <T> int indexOf(final T[] array, final T v) {
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
    public static <T> boolean containsAll(final T[] container, final T[] content) {
        for (final T x : content) {
            if (!contains(container, x))
                return false;
        }
        return true;
    }

//    public static <T extends Term> boolean containsAll(final Compound container, final T[] content) {
//        for (final T x : content) {
//            if (!container.containsTerm(x))
//                return false;
//        }
//        return true;
//    }
    
    /** a contains any of b  NOT TESTED YET */
    public static boolean containsAny(Compound<?> a, final Collection<Term> b) {
        for (final Term bx : b) {
            if (a.containsTerm(bx))
                return true;
        }
        for (final Term ax : a) {
            if (ax instanceof Compound)
                if (containsAny(((Compound)ax), b))
                    return true;
        }
        
        return false;
    }

    public static <T> boolean contains(final T[] container, final T v) {
        for (final T e : container) {
            if (v.equals(e)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(final Term[] container, final Term v) {
        for (final Term e : container)
            if (v.equals(e))
                return true;
        return false;
    }

    public static boolean equals(final Term[] a, final Term[] b) {
        if (a.length!=b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i]))
                return false;            
        }
        return true;
    }

    public static void verifyNonNull(Term... t) {
        for (Object o : t)
            if (o == null)
                throw new RuntimeException("Element null in: " + Arrays.toString(t));
    }    
    
    public static Term[] verifySortedAndUnique(final Term[] arg, boolean allowSingleton) {        
        if (arg.length == 0) {
            throw new RuntimeException("Needs >0 components");
        }
        if (!allowSingleton && (arg.length == 1)) {
            throw new RuntimeException("Needs >1 components: " + Arrays.toString(arg));
        }
        Term[] s = Terms.toSortedSetArray(arg);
        if (arg.length!=s.length) {
            throw new RuntimeException("Contains duplicates: " + Arrays.toString(arg));
        }
        int j = 0;
        for (Term t : s) {
            if (!t.equals(arg[j++]))
                throw new RuntimeException("Un-ordered: " + Arrays.toString(arg) + " , correct order=" + Arrays.toString(s));
        }        
        return s;
    }

//    /**
//     * comparison that will match constant terms, allowing variables to match regardless
//     * ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
//     */
//    public static boolean containsVariablesAsWildcard(final Term[] term, final Term b) {
//        Compound bCompound = (b instanceof Compound) ? ((Compound)b) : null;
//        for (Term a : term) {
//            if (a.equals(b)) return true;
//
//            if ((a instanceof Compound) && (bCompound!=null))  {
//                if (((Compound)a).equalsVariablesAsWildcards(bCompound))
//                        return true;
//            }
//        }
//        return false;
//    }

    
//    /** true if any of the terms contains a variable */
//    public static boolean containsVariables(Term... args) {
//        for (Term t : args) {
//            if (t.hasVar())
//                return true;
//        }
//        return false;
//    }

//    public static final Predicate<? super TaskRule> levelValid(int nalLevel) {
//        return (t) -> Terms.levelValid(t, nalLevel);
//    }


    //    public static boolean levelValid(Sentence sentence, int nal) {
//        if (nal >= 8) return true;
//
//        Term t = sentence.getTerm();
//        if (!sentence.isEternal() && (nal < 7)) return false;
//        return levelValid(t, nal);
//    }

//    /**
//     * Make a Statement from given components, called by the rules
//     * @return The Statement built
//     * @param subj The first component
//     * @param pred The second component
//     * @param statement A sample statement providing the class type
//     */
//    public static Statement makeStatement(final Statement statement, final Term subj, final Term pred) {
//        if (statement instanceof Inheritance) {
//            return Inheritance.make(subj, pred);
//        }
//        if (statement instanceof Similarity) {
//            return Similarity.make(subj, pred);
//        }
//        if (statement instanceof Implication) {
//            return Implication.make(subj, pred, statement.getTemporalOrder());
//        }
//        if (statement instanceof Equivalence) {
//            return Equivalence.make(subj, pred, statement.getTemporalOrder());
//        }
//        return null;
//    }

//    /**
//     * Make a symmetric Statement from given term and temporal
// information, called by the rules
//     *
//     * @param statement A sample asymmetric statement providing the class type
//     * @param subj The first component
//     * @param pred The second component
//     * @param order The temporal order
//     * @return The Statement built
//     */
//    final public static Statement makeSymStatement(final Statement statement, final Term subj, final Term pred, final int order) {
//        if (statement instanceof Inheritance) {
//            return Similarity.make(subj, pred);
//        }
//        if (statement instanceof Implication) {
//            return Equivalence.make(subj, pred, order);
//        }
//        return null;
//    }

//    public static Compound<?> compoundOrNull(Term t) {
//        if (t instanceof Compound) return (Compound)t;
//        return null;
//    }


    public static Term[] reverse(Term[] arg) {
        int l = arg.length;
        Term[] r = new Term[l];
        for (int i = 0; i < l; i++) {
            r[i] = arg[l - i - 1];
        }
        return r;
    }


//    public static TreeSet<Term> toSortedSet(final Term... arg) {
//        //use toSortedSetArray where possible
//        TreeSet<Term> t = new TreeSet();
//        Collections.addAll(t, arg);
//        return t;
//    }

    public static Term[] toSortedSetArray(final Collection<Term> a, final Collection<Term> b) {
        return toSortedSetArray(a, b);
    }

    public static Term[] toSortedSetArray(final Term... arg) {
        switch (arg.length) {
            case 0: return EmptyTermArray;
            case 1: return new Term[] { arg[0] };
            case 2:
                Term a = arg[0];
                Term b = arg[1];
                int c = a.compareTo(b);

                if (Global.DEBUG) {
                    //verify consistency of compareTo() and equals()
                    boolean equal = a.equals(b);
                    if ((equal && (c!=0)) || (!equal && (c==0))) {
                        throw new RuntimeException("invalid order (" + c + "): " + a + " = " + b);
                    }
                }

                if (c < 0) return new Term[] { a, b };
                else if (c > 0) return new Term[] { b, a };
                else /*if (c == 0)*/ return new Term[] { a }; //equal

        }

        //TODO fast sorted array for arg.length == 3

        //terms > 2:

        SortedList<Term> s = new SortedList(arg.length);
        s.setAllowDuplicate(false);

        Collections.addAll(s, arg);

        return s.toArray(new Term[s.size()] );

        /*
        TreeSet<Term> s = toSortedSet(arg);
        //toArray didnt seem to work, but it might. in the meantime:
        Term[] n = new Term[s.size()];
        int j = 0;
        for (Term x : s) {
            n[j++] = x;
        }
        return n;
        */
    }

    public static <T extends Term> T[] toSortedSetArray(final Collection<T>... c) {
        TreeSet<T> t = new TreeSet<>();
        for (Collection<T> it : c) {
            t.addAll(it);
        }
        return t.toArray((T[]) new Term[t.size()]);
    }


    /** tests if any subterms are in common, effectively a bidirectional Terms.contains(Term[], Term) */
    public static boolean shareAnyTerms(Term[] a, Term[] b) {

        //TODO special 2 x 2 case: tests= {a1, b1}, {a1, b2}, {a2,b1}, {a2,b2}
        //TODO special 2 x 2 case: tests= {a1, b1}, {a1, b2}, {a2,b1}, {a2,b2}


        for (final Term x : b)
            if (Terms.contains(a, x)) return true;
        for (final Term x : a)
            if (Terms.contains(b, x)) return true;
        return false;
    }

    /** tests if any subterms are in common, effectively abidirectional Terms.contains(Term[], Term) */
    public static boolean shareAnySubTerms(Compound a, Compound b) {
        return shareAnyTerms(a.term, b.term);
    }


    public static int compareSubterms(final Term[] a, final Term[] b) {
        int al = a.length;
        int l = Integer.compare(al, b.length);
        if (l!=0) return l;

        for (int i = 0; i < al; i++) {
            Term x = a[i];
            Term y = b[i];
            int ca = x.compareTo(y);
            if (ca != 0) return ca;
        }

        return 0;
    }
    public static boolean equalSubterms(final Term[] a, final Term[] b) {
        int al = a.length;
        int bl = b.length;
        if (bl!=al) return false;

        for (int i = 0; i < al; i++) {
            if (!a[i].equals(b[i])) return false;
        }

        return true;
    }

    /** for printing complex terms as a recursive tree */
    public static void printRecursive(Term x) {
        printRecursive(x, 0);
    }
    public static void printRecursive(Term x, int level) {
        //indent
        for (int i = 0; i < level; i++)
            System.out.print("  ");

        System.out.print(x);
        System.out.print(" (");
        System.out.print(x.op() + "[" + x.getClass().getSimpleName() + "] ");
        System.out.print("c" + x.complexity() + ",v" + x.volume() + " ");
        System.out.print(Integer.toBinaryString(x.structure()) + ")");
        System.out.println();

        if (x instanceof Compound) {
            for (Term z : ((Compound<?>)x))
                printRecursive(z, level+1);
        }
    }

    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @return A compound term or null
     */
    public static Term term(final Compound compound, final Term[] components) {
        if (compound instanceof ImageExt) {
            return new ImageExt(components, ((Image) compound).relationIndex);
        } else if (compound instanceof ImageInt) {
            return new ImageInt(components, ((Image) compound).relationIndex);
        } else {
            return term(compound.op(), components);
        }
    }

    public static Term term(final Compound compound, Collection<Term> components) {
        Term[] c = components.toArray(new Term[components.size()]);
        return term(compound, c);
    }

    private static boolean ensureTermLength(int num, Term[] a) {
        return (a.length==num);
        /*if (a.length!=num)
            throw new CompoundTerm.InvalidTermConstruction("Expected " + num + " args to create Term from " + Arrays.toString(a));*/
    }

    /**
     * Try to make a compound term from an operate and a list of term
     * <p>
     * Called from StringParser
     *
     * @param op Term operate
     * @return A term or null
     */
    public static Term term(final Op op, final Term... a) {

        switch (op) {


            case SET_EXT_OPENER:
                return SetExt.make(a);
            case SET_INT_OPENER:
                return SetInt.make(a);
            case INTERSECTION_EXT:
                return IntersectionExt.make(a);
            case INTERSECTION_INT:
                return IntersectionInt.make(a);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(a);
            case DIFFERENCE_INT:
                return DifferenceInt.make(a);
            case PRODUCT:
                return Product.make(a);
            case IMAGE_EXT:
                return Image.makeExt(a);
            case IMAGE_INT:
                return Image.makeInt(a);
            case NEGATION:
                return Negation.make(a);
            case DISJUNCTION:
                return Disjunction.make(a);
            case CONJUNCTION:
                return Conjunction.make(a);
            case SEQUENCE:
                return Conjunction.make(a, Temporal.ORDER_FORWARD);
            case PARALLEL:
                return Conjunction.make(a, Temporal.ORDER_CONCURRENT);

            //STATEMENTS --------------------------
            case PROPERTY:
                if (ensureTermLength(2, a)) return Property.make(a[0], a[1]); break;
            case INSTANCE:
                if (ensureTermLength(2, a)) return Instance.make(a[0], a[1]); break;
            case INSTANCE_PROPERTY:
                if (ensureTermLength(2, a)) return InstanceProperty.make(a[0], a[1]); break;

            case INHERITANCE:
                if (ensureTermLength(2, a)) return Inheritance.makeTerm(a[0], a[1]); break;

            case SIMILARITY:
                if (ensureTermLength(2, a)) return Similarity.makeTerm(a[0], a[1]); break;

            case IMPLICATION:
                if (ensureTermLength(2, a)) return Implication.makeTerm(a[0], a[1]); break;
            case IMPLICATION_AFTER:
                if (ensureTermLength(2, a)) return Implication.make(a[0], a[1], Temporal.ORDER_FORWARD); break;
            case IMPLICATION_BEFORE:
                if (ensureTermLength(2, a)) return Implication.make(a[0], a[1], Temporal.ORDER_BACKWARD); break;
            case IMPLICATION_WHEN:
                if (ensureTermLength(2, a)) return Implication.make(a[0], a[1], Temporal.ORDER_CONCURRENT); break;

            case EQUIVALENCE:
                if (ensureTermLength(2, a)) return Equivalence.makeTerm(a[0], a[1]); break;
            case EQUIVALENCE_WHEN:
                if (ensureTermLength(2, a)) return Equivalence.make(a[0], a[1], Temporal.ORDER_CONCURRENT); break;
            case EQUIVALENCE_AFTER:
                if (ensureTermLength(2, a)) return Equivalence.make(a[0], a[1], Temporal.ORDER_FORWARD); break;

            default:
                throw new RuntimeException("Unknown op: " + op + " (" + op.name() + ')');
        }

        return null;
    }

    /**
     * build a component list from terms
     *
     * @return the component list
     */
    public static Term[] toArray(final Term... t) {
        return t;
    }

    public static List<Term> toList(final Term... t) {
        return Arrays.asList((Term[]) t);
    }

    public static Set<Term> toSet(Term... t) {
        if (t.length == 1)
            return Collections.singleton(t[0]);
        Set<Term> l = Global.newHashSet(t.length);
        Collections.addAll(l, t);
        return l;
    }

    public static <T> Set<T> toSortedSet(T... t) {

        final int l = t.length;
        if (l ==1)
            return Collections.singleton(t[0]);

        final TreeSet<T> s = new TreeSet();
        Collections.addAll(s, t);
        return s;
    }

    /**
     * interprets subterms of a compound term to a set of
     * key,value pairs (Map entries).
     * ie, it translates this SetExt tp a Map<Term,Term> in the
     * following pattern:
     *
     *      { (a,b) }  becomes Map a=b
     *      [ (a,b), b:c ] bcomes Map a=b, b=c
     *      { (a,b), (b,c), d } bcomes Map a=b, b=c, d=null
     *
     * @return a potentially incomplete map representation of this compound
     */
    public static Map<Term,Term> toKeyValueMap(Subterms<?> t) {

        Map<Term,Term> result = Global.newHashMap();

        t.forEach( a -> {
            if (a.length() == 2) {
                if ((a.op() == Op.PRODUCT) || (a.op() == Op.INHERITANCE)) {
                    Compound ii = (Compound)a;
                    result.put(ii.term(0), ii.term(1));
                }
            }
            else if (a.length() == 1) {
                result.put(a, null);
            }
        });

        return result;
    }

    public static int maxLevel(Term term) {
        final int[] max = {0};
        term.recurseTerms((t,p) -> {
            int m = t.op().minLevel;
            if (m > max[0])
                max[0] = m;
        });
        return max[0];
    }
}
