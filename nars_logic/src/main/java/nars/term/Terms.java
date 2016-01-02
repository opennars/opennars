package nars.term;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.Global;
import nars.Op;
import nars.term.compound.Compound;
import nars.util.Texts;
import nars.util.data.sorted.SortedList;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import static nars.Op.*;

/**
 * Static utility class for static methods related to Terms
 * @author me
 */
public class Terms {
    public static final Term[] Empty = new Term[0];
    public static final IntFunction<Term[]> TermArrayBuilder = Term[]::new;

    public static boolean equalSubTermsInRespectToImageAndProduct(Termed a, Termed b) {

        if (a == null || b == null) {
            return false;
        }

        //if one is not a compound, then return their equality
        if (!((a instanceof Compound) && (b instanceof Compound))) {
            return a.equals(b);
        }

        Op o = a.op();
        boolean equalOps = o == b.op();

        if (equalOps) {
            switch (o) {
                case INHERIT:
                    return equalSubjectPredicateInRespectToImageAndProduct((Compound)a, (Compound)b);

                case SIMILAR:
                    return equalSubjectPredicateInRespectToImageAndProduct((Compound)a,(Compound)b)
                            || equalSubjectPredicateInRespectToImageAndProduct((Compound)b, (Compound)a);
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


    public static boolean equalSubjectPredicateInRespectToImageAndProduct(Compound a, Compound b) {
        return equalSubjectPredicateInRespectToImageAndProduct(a, b, true);
    }

    static boolean equalSubjectPredicateInRespectToImageAndProduct(Termed A, Termed B, boolean requireEqualImageRelation) {


        if (A.equals(B)) {
            return true;
        }

        Term subjA = Statement.subj(A);
        Term predA = Statement.pred(A);
        Term subjB = Statement.subj(B);
        Term predB = Statement.pred(B);

        Term ta = null, tb = null; //the compound term to put itself in the comparison set
        Term sa = null, sb = null; //the compound term to put its components in the comparison set

        if ((subjA.op(PRODUCT)) && (predB.op(IMAGE_EXT))) {
            ta = predA;
            sa = subjA;
            tb = subjB;
            sb = predB;
        }
        if ((subjB.op(PRODUCT)) && (predA.op(IMAGE_EXT))) {
            ta = subjA;
            sa = predA;
            tb = predB;
            sb = subjB;
        }
        if ((predA.op(IMAGE_EXT)) && (predB.op(IMAGE_EXT))) {
            ta = subjA;
            sa = predA;
            tb = subjB;
            sb = predB;
        }

        if ((subjA.op(IMAGE_INT)) && (subjB.op(IMAGE_INT))) {
            ta = predA;
            sa = subjA;
            tb = predB;
            sb = subjB;
        }

        if ((predA.op(PRODUCT)) && (subjB.op(IMAGE_INT))) {
            ta = subjA;
            sa = predA;
            tb = predB;
            sb = subjB;
        }
        if ((predB.op(PRODUCT)) && (subjA.op(IMAGE_INT))) {
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
            //if (sa.op().isImage() && sb.op().isImage()) {
                if (((Compound)sa).relation() != ((Compound)sb).relation()) {
                    return false;
                }
            //}
        }

        return containsAll((Compound) sa, ta, (Compound) sb, tb);

    }

    private static boolean containsAll(TermContainer sat, Term ta, TermContainer sbt, Term tb) {
        //set for fast containment check
        Set<Term> componentsA = sat.toSet();
        componentsA.add(ta);

        //test A contains B
        if (!componentsA.contains(tb))
            return false;

        Term[] sbtt = sbt.terms();
        for (Term x : sbtt) {
            if (!componentsA.contains(x))
                return false;
        }

        return true;
    }


    /** brute-force equality test */
    public static boolean contains(Term[] container, Term v) {
        for (Term e : container)
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

    public static Term[] toSortedSetArray(Term... arg) {
        switch (arg.length) {

            case 0:
                return Terms.Empty;

            case 1:
                return arg; //new Term[] { arg[0] };
            case 2:
                Term a = arg[0];
                Term b = arg[1];
                int c = a.compareTo(b);

//                if (Global.DEBUG) {
//                    //verify consistency of compareTo() and equals()
//                    boolean equal = a.equals(b);
//                    if ((equal && (c!=0)) || (!equal && (c==0))) {
//                        throw new RuntimeException("invalid order (" + c + "): " + a + " = " + b);
//                    }
//                }

                if (c < 0) return arg; //same as input //new Term[]{a, b};
                else if (c > 0) return new Term[]{b, a};
                else /*if (c == 0)*/ return new Term[]{a}; //equal

                //TODO fast sorted array for arg.length == 3

            default:
                //terms > 2:
                return new SortedList<>(arg).toArray(TermArrayBuilder);
        }
    }

//    public static <T extends Term> T[] toSortedSetArray(Collection<T> c) {
//        TreeSet<T> t = new TreeSet<>(c);
//        return t.toArray((T[]) new Term[t.size()]);
//    }

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

    /**
     * for printing complex terms as a recursive tree
     */
    public static void printRecursive(Term x, Consumer<String> c) {
        printRecursive(x, 0, c);
    }

    public static void printRecursive(Term x, int level, Consumer<String> c) {
        //indent
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < level; i++)
            line.append("  ");

        line.append(x);

        if (x instanceof Compound) {
            for (Term z : ((Compound<?>) x))
                printRecursive(z, level + 1, c);
        }

        c.accept(line.toString());
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
    public static Term[] toArray(Term... t) {
        return t;
    }

    public static List<Term> toList(Term[] t) {
        return Arrays.asList(t);
    }

    /** makes a set from the array of terms */
    public static Set<Term> toSet(Term[] t) {
        if (t.length == 1)
            return Collections.singleton(t[0]);
        Set<Term> l = Global.newHashSet(t.length);
        Collections.addAll(l, t);
        return l;
    }

    @SafeVarargs
    public static <T> Set<T> toSortedSet(T... t) {

        int l = t.length;
        if (l == 1)
            return Collections.singleton(t[0]);

        TreeSet<T> s = new TreeSet();
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
                if ((a.op() == PRODUCT) || (a.op() == Op.INHERIT)) {
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
        int[] max = {0};
        term.recurseTerms((t, p) -> {
            int m = t.op().minLevel;
            if (m > max[0])
                max[0] = m;
        });
        return max[0];
    }

    public static Term[] concat(Term[] a, Term... b) {

        if (a == null) {
            return null;
        }

        if (a.length == 0) return b;
        if (b.length == 0) return a;

        int L = a.length + b.length;

        //TODO apply preventUnnecessaryDeepCopy to more cases

        Term[] arr = new Term[L];

        int l = a.length;
        System.arraycopy(a, 0, arr, 0, l);
        System.arraycopy(b, 0, arr, l, b.length);

        return arr;
    }

    public static <T extends Term> Term[] filter(T[] input, IntObjectPredicate<T> filter) {

        int s = input.length;

        List<Term> l = Global.newArrayList(s);

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

    public static Term[] toArray(Collection<Term> l) {
        int s = l.size();
        if (s == 0)
            return Terms.Empty;
        return l.toArray(new Term[s]);
    }

    public static Term[] cloneTermsReplacing(Term[] term, Term from, Term to) {
        Term[] y = new Term[term.length];
        int i = 0;
        for (Term x : term) {
            if (x.equals(from))
                x = to;
            y[i++] = x;
        }
        return y;
    }

    /** a heuristic for measuring the difference between terms
     *  in range of 0..100%, 0 meaning equal
     * */
    public static float termDistance(Term a, Term b, float ifLessThan) {
        if (a.equals(b)) return 0;
        //TODO handle TermMetadata terms

        float dist = 0;
        if (a.op()!=b.op()) {
            //50% for equal term
            dist += 0.25f;
            if (dist >= ifLessThan) return dist;
        }

        if (a.size()!=b.size()) {
            dist += 0.25f;
            if (dist >= ifLessThan) return dist;
        }

        if (a.structure()!=b.structure()) {
            dist += 0.25f;
            if (dist >= ifLessThan) return dist;
        }

        //HACK use toString for now
        dist += Texts.levenshteinDistancePercent(
                a.toString(false),
                b.toString(false)) * 0.25f;

        return dist;
    }
}
