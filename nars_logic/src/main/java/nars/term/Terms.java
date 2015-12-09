package nars.term;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.Global;
import nars.Op;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Image;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.ImageInt;
import nars.nal.nal4.Product;
import nars.term.compound.Compound;
import nars.util.data.sorted.SortedList;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Static utility class for static methods related to Terms
 * @author me
 */
public class Terms {
    public final static Term[] Empty = new Term[0];
    public static final IntFunction<Term[]> TermArrayBuilder = Term[]::new;

    public static boolean equalSubTermsInRespectToImageAndProduct(final Term a, final Term b) {

        if (a == null || b == null) {
            return false;
        }

        //if one is not a compound, then return their equality
        if (!((a instanceof Compound) && (b instanceof Compound))) {
            return a.equals(b);
        }

        boolean equalOps = a.op() == b.op();

        if (equalOps) {
            if (a instanceof Inheritance) {
                return equalSubjectPredicateInRespectToImageAndProduct(a, b);
            } else if (a instanceof Similarity) {
                return equalSubjectPredicateInRespectToImageAndProduct(a, b)
                        || equalSubjectPredicateInRespectToImageAndProduct(b, a);
            }
        }

        Compound A = ((Compound) a);
        Compound B = ((Compound) b);
        int aLen = A.size();
        if (aLen != B.size())
            return false;

        //match all subterms
        for (int i = 0; i < aLen; i++) {
            if (!equalSubTermsInRespectToImageAndProduct(A.term(i), B.term(i)))
                return false;

        }
        return true;
    }


    public static boolean equalSubjectPredicateInRespectToImageAndProduct(final Term a, final Term b) {
        return equalSubjectPredicateInRespectToImageAndProduct((Statement) a, (Statement) b, true);
    }

    static boolean equalSubjectPredicateInRespectToImageAndProduct(final Statement A, final Statement B, boolean requireEqualImageRelation) {


        if (A.equals(B)) {
            return true;
        }

        Term subjA = A.getSubject();
        Term predA = A.getPredicate();
        Term subjB = B.getSubject();
        Term predB = B.getPredicate();

        Term ta = null, tb = null; //the compound term to put itself in the comparison set
        Term sa = null, sb = null; //the compound term to put its components in the comparison set

        if ((subjA instanceof Product) && (predB instanceof ImageExt)) {
            ta = predA;
            sa = subjA;
            tb = subjB;
            sb = predB;
        }
        if ((subjB instanceof Product) && (predA instanceof ImageExt)) {
            ta = subjA;
            sa = predA;
            tb = predB;
            sb = subjB;
        }
        if ((predA instanceof ImageExt) && (predB instanceof ImageExt)) {
            ta = subjA;
            sa = predA;
            tb = subjB;
            sb = predB;
        }

        if ((subjA instanceof ImageInt) && (subjB instanceof ImageInt)) {
            ta = predA;
            sa = subjA;
            tb = predB;
            sb = subjB;
        }

        if ((predA instanceof Product) && (subjB instanceof ImageInt)) {
            ta = subjA;
            sa = predA;
            tb = predB;
            sb = subjB;
        }
        if ((predB instanceof Product) && (subjA instanceof ImageInt)) {
            ta = predA;
            sa = subjA;
            tb = subjB;
            sb = predB;
        }

        if (ta == null)
            return false;


        //original code did not check relation index equality
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/language/CompoundTerm.java
        if (requireEqualImageRelation) {
            if (sa instanceof Image && sb instanceof Image) {
                if (((Image) sa).relationIndex != ((Image) sb).relationIndex) {
                    return false;
                }
            }
        }

        return containsAll((Compound) sa, ta, (Compound) sb, tb);

    }

    private static boolean containsAll(Compound sat, Term ta, Compound sbt, Term tb) {
        //temporary set for fast containment check
        Set<Term> componentsA = Global.newHashSet(sat.size() + 1);
        componentsA.add(ta);
        sat.addAllTo(componentsA);

        //test A contains B
        if (!componentsA.contains(tb))
            return false;
        int l = sbt.size();
        for (int i = 0; i < l; i++) {
            Term bComponent = sbt.term(i);
            if (!componentsA.contains(bComponent))
                return false;
        }

        return true;
    }


    public static boolean contains(final Term[] container, final Term v) {
        for (final Term e : container)
            if (v.equals(e))
                return true;
        return false;
    }


    public static Term[] reverse(Term[] arg) {
        int l = arg.length;
        Term[] r = new Term[l];
        for (int i = 0; i < l; i++) {
            r[i] = arg[l - i - 1];
        }
        return r;
    }

    public static Term[] toSortedSetArray(final Term... arg) {
        switch (arg.length) {

            case 0:
                throw new RuntimeException("empty"); //return EmptyTermArray;

            case 1:
                return arg; //new Term[] { arg[0] };
            case 2:
                final Term a = arg[0];
                final Term b = arg[1];
                final int c = a.compareTo(b);

//                if (Global.DEBUG) {
//                    //verify consistency of compareTo() and equals()
//                    boolean equal = a.equals(b);
//                    if ((equal && (c!=0)) || (!equal && (c==0))) {
//                        throw new RuntimeException("invalid order (" + c + "): " + a + " = " + b);
//                    }
//                }

                if (c < 0) return new Term[]{a, b};
                else if (c > 0) return new Term[]{b, a};
                else /*if (c == 0)*/ return new Term[]{a}; //equal

                //TODO fast sorted array for arg.length == 3

            default:
                //terms > 2:
                return new SortedList<>(arg).toArray(TermArrayBuilder);
        }
    }

    public static <T extends Term> T[] toSortedSetArray(final Collection<T> c) {
        TreeSet<T> t = new TreeSet<>(c);
        return t.toArray((T[]) new Term[t.size()]);
    }

    /**
     * for printing complex terms as a recursive tree
     */
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
        System.out.print("c" + x.complexity() + ",v" + x.volume() + ' ');
        System.out.print(Integer.toBinaryString(x.structure()) + ')');
        System.out.println();

        if (x instanceof Compound) {
            for (Term z : ((Compound<?>) x))
                printRecursive(z, level + 1);
        }
    }

    private static boolean ensureTermLength(int num, Term[] a) {
        return (a.length == num);
        /*if (a.length!=num)
            throw new CompoundTerm.InvalidTermConstruction("Expected " + num + " args to create Term from " + Arrays.toString(a));*/
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
        if (l == 1)
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
     * <p/>
     * { (a,b) }  becomes Map a=b
     * [ (a,b), b:c ] bcomes Map a=b, b=c
     * { (a,b), (b,c), d } bcomes Map a=b, b=c, d=null
     *
     * @return a potentially incomplete map representation of this compound
     */
    public static Map<Term, Term> toKeyValueMap(Compound<?> t) {

        Map<Term, Term> result = Global.newHashMap();

        t.forEach(a -> {
            if (a.size() == 2) {
                if ((a.op() == Op.PRODUCT) || (a.op() == Op.INHERITANCE)) {
                    Compound ii = (Compound) a;
                    result.put(ii.term(0), ii.term(1));
                }
            } else if (a.size() == 1) {
                result.put(a, null);
            }
        });

        return result;
    }

    public static int maxLevel(Term term) {
        final int[] max = {0};
        term.recurseTerms((t, p) -> {
            int m = t.op().minLevel;
            if (m > max[0])
                max[0] = m;
        });
        return max[0];
    }

    public static Term[] concat(Term[] a, Term[] b) {

        if (a == null) {
            return null;
        }

        if (a.length == 0) return b;
        if (b.length == 0) return a;

        int L = a.length + b.length;

        //TODO apply preventUnnecessaryDeepCopy to more cases

        final Term[] arr = new Term[L];

        final int l = a.length;
        System.arraycopy(a, 0, arr, 0, l);
        System.arraycopy(b, 0, arr, l, b.length);

        return arr;
    }

    public static <T> Term[] filter(T[] input, IntObjectPredicate<T> filter) {

        int s = input.length;

        List<T> l = Global.newArrayList(s);

        for (int i = 0; i < s; i++) {
            T t = input[i];
            if (filter.accept(i, t))
                l.add(t);
        }
        if (l.isEmpty()) return Terms.Empty;
        return l.toArray(new Term[l.size()]);
    }

    public static Term[] filter(Term[] input, IntPredicate filter) {
        return filter(input, (i, t) -> filter.test(i));
    }

    public static Term[] filter(Term[] input, Predicate<Term> filter) {
        return filter(input, (i, t) -> filter.test(t));
    }

    public static Term[] toArray(List<Term> l) {
        return l.toArray(new Term[l.size()]);
    }

    public static Term[] cloneTermsReplacing(final Term[] term, final Term from, final Term to) {
        Term[] y = new Term[term.length];
        int i = 0;
        for (Term x : term) {
            if (x.equals(from))
                x = to;
            y[i++] = x;
        }
        return y;
    }
}
