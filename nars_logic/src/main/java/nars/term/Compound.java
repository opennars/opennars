/*
 * CompoundTerm.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.term;

import com.google.common.collect.Iterators;
import nars.Global;
import nars.Op;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal4.Product;
import nars.nal.nal7.TemporalRules;
import nars.term.transform.*;
import nars.util.data.sexpression.IPair;
import nars.util.data.sexpression.Pair;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.copyOf;
import static nars.Symbols.*;

/**
 * a compound term
 */
public abstract class Compound<T extends Term> implements Term, Iterable<T>, IPair {


    /**
     * list of (direct) term
     */
    public final T[] term;

    /**
     * syntactic complexity of the compound, the sum of those of its term
     * plus 1
     * TODO make final again
     */

    /**
     * bitvector of subterm types, indexed by NALOperator's .ordinal() and OR'd into by each subterm
     */
    protected transient int structureHash;
    transient int contentHash;


    /**
     * Whether contains a variable
     */
    transient private byte hasVarQueries, hasVarIndeps, hasVarDeps;
    transient private int varTotal, volume, complexity;

    transient private int containedTemporalRelations = -1;


    /**
     * subclasses should be sure to call init() in their constructors; it is not done here
     * to allow subclass constructors to set data before calling init()
     */
    public Compound(final T... components) {
        super();


        this.complexity = -1;
        this.term = components;
    }

    public boolean impossibleSubTermVolume(final int otherTermVolume) {
        return otherTermVolume >
                volume()
                        - 1 /* for the compound itself */
                        - (length() - 1) /* each subterm has a volume >= 1, so if there are more than 1, each reduces the potential space of the insertable */
                ;
    }

    public boolean impossibleToMatch(final int possibleSubtermStructure) {
        final int existingStructure = structureHash;

        //if the OR produces a different result compared to subterms,
        // it means there is some component of the other term which is not found
        return ((possibleSubtermStructure | existingStructure) != existingStructure);
    }


    final private static int PRIME1 = 31;
    final private static int PRIME2 = 92821;

    /**
     * call this after changing Term[] contents: recalculates variables and complexity
     */
    protected void init(final Term... term) {

        int deps = 0, indeps = 0, queries = 0;
        int compl = 1, vol = 1;


        int subt = getStructureBase();


        int contentHash = (PRIME2 * subt) + structure2();
        ;

        int p = 0;
        for (final Term t : term) {

            if (t == this)
                throw new RuntimeException("term can not contain itself");

            /*if (t == null)
                throw new RuntimeException("null subterm");*/

            //first to trigger subterm update if necessary
            contentHash = (PRIME1 * contentHash) + (t.hashCode() + p);

            compl += t.complexity();
            vol += t.volume();
            deps += t.varDep();
            indeps += t.varIndep();
            queries += t.varQuery();
            subt |= t.structure();

            p++;
        }

        this.structureHash = subt;

        if (contentHash == 0) contentHash = 1; //nonzero to indicate hash calculated
        this.contentHash = contentHash;

        this.hasVarDeps = (byte) deps;
        this.hasVarIndeps = (byte) indeps;
        this.hasVarQueries = (byte) queries;
        this.varTotal = (short) (deps + indeps + queries);
        this.complexity = (short) compl;

        if ((this.volume = (short) vol) > Global.COMPOUND_VOLUME_MAX) {
            throw new RuntimeException("volume limit exceeded for new Compound[" + op() + "] " + Arrays.toString(term));
        }

    }


    protected int getStructureBase() {
        final int opOrdinal = op().ordinal();
        if (opOrdinal < 31)
            return 1 << opOrdinal;
        return 0;
    }

    public final void rehash() {
        //this may not be necessary
        for (final Term t : term) {
            t.rehash();
        }

        init(term);
    }


    /**
     * allows implementations to include a 32 bit identifier within the struturehash
     * which will be used to exclude inequalities. by default, it returns 0 which
     * will not change the default structureHash structure
     *
     * @return
     */
    public int structure2() {
        return 0;
    }


    @Override
    public final int structure() {
        return structureHash;
    }


    @Override
    public byte[] bytes() {

        final int numArgs = term.length;

        byte[] opBytes = op().bytes;

        int len = opBytes.length + 1;

        for (final Term t : term) {
            len += t.bytes().length + 1;
        }

        ByteBuf b = ByteBuf.create(len);

        b.add(opBytes);

        for (int i = 0; i < numArgs; i++) {
            Term t = term[i];

            if (i != 0) {
                b.add(((byte) ARGUMENT_SEPARATOR));
            }

            b.add(t.bytes());

        }

        b.add((byte) COMPOUND_TERM_CLOSER);

        return b.toBytes();

    }

    @Override
    public void append(final Appendable p, final boolean pretty) throws IOException {

        boolean opener = appendTermOpener();
        if (opener)
            p.append(COMPOUND_TERM_OPENER);


        final boolean appendedOperator = appendOperator(p);


        int nterms = term.length;
        for (int i = 0; i < nterms; i++) {
            if ((i != 0) || (i == 0 && nterms > 1 && appendedOperator)) {
                p.append(ARGUMENT_SEPARATOR);
                if (pretty)
                    p.append(' ');
            }

            term[i].append(p, pretty);
        }


        appendCloser(p);

    }

    @Override
    public String toString() {
        return toString(true);
    }

    public static void appendSeparator(final Appendable p, final boolean pretty) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');
    }

    public boolean appendOperator(Appendable p) throws IOException {
        p.append(op().str);
        return true;
    }

    public void appendCloser(Appendable p) throws IOException {
        p.append(COMPOUND_TERM_CLOSER);
    }

    public boolean appendTermOpener() {
        return true;
    }


    /**
     * build a component list from terms
     *
     * @return the component list
     */
    public static Term[] termArray(final Term... t) {
        return t;
    }


    public static List<Term> termList(final Term... t) {
        return Arrays.asList((Term[]) t);
    }


    public static void writeCompound1(final Op op, final Term singleTerm, Appendable writer, boolean pretty) throws IOException {
        writer.append(COMPOUND_TERM_OPENER);
        writer.append(op.str);
        writer.append(ARGUMENT_SEPARATOR);
        singleTerm.append(writer, pretty);
        writer.append(COMPOUND_TERM_CLOSER);
    }

    public static byte[] newCompound1Key(final Op op, final Term singleTerm) {

        final byte[] opBytes = op.bytes;

        if (opBytes.length > 1)
            throw new RuntimeException("Compound1 operators must have a 1 char representation; invalid: " + op);

        final byte[] termBytes = singleTerm.bytes();

        return ByteBuf.create(opBytes.length + termBytes.length)
                //.add((byte)COMPOUND_TERM_OPENER.ch)
                .add(opBytes)
                        //.add((byte) ARGUMENT_SEPARATOR)
                .add(termBytes)
                        //.add((byte) COMPOUND_TERM_CLOSER.ch)
                .toBytes();
    }


    /**
     * Shallow clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(final Term[] original, final Term... additional) {
        if (original == null) {
            return null;
        }

        int L = original.length + additional.length;
        if (L == 0)
            return original;

        //TODO apply preventUnnecessaryDeepCopy to more cases

        final Term[] arr = new Term[L];

        final int l = original.length;
        System.arraycopy(original, 0, arr, 0, l);
        System.arraycopy(additional, 0, arr, l, additional.length);

//        int j = 0;
//        Term[] srcArray = original;
//        for (int i = 0; i < L; i++) {
//            if (i == original.length) {
//                srcArray = additional;
//                j = 0;
//            }
//
//            arr[i] = srcArray[j++];
//        }

        return arr;

    }

    /**
     * from: http://stackoverflow.com/a/19333201
     */
    public static <T> void shuffle(final T[] array, final Random random) {
        int count = array.length;
        for (int i = count; i > 1; i--) {
            int a = i - 1;
            int b = random.nextInt(i);
            final T t = array[b];
            array[b] = array[a];
            array[a] = t;
        }
    }


//    @Deprecated public static <T> void shuffleOLD(final T[] ar, final Random rnd) {
//        if (ar.length < 2) return;
//
//
//        for (int i = ar.length - 1; i > 0; i--) {
//            final int index = rnd.nextInt(i + 1);
//            final T a = ar[index];
//            ar[index] = ar[i];
//            ar[i] = a;
//        }
//    }

    public T term(int i) {
        return term[i];
    }

    public Term term(int i, boolean unwrapLen1SetExt, boolean unwrapLen1SetInt, boolean unwrapLen1Product) {
        Term x = term[i];
        return Compound.unwrap(x, unwrapLen1SetExt, unwrapLen1SetInt, unwrapLen1Product);
    }

    public static Term unwrap(Term x, boolean unwrapLen1SetExt, boolean unwrapLen1SetInt, boolean unwrapLen1Product) {
        if (x instanceof Compound) {
            Compound c = (Compound) x;
            if (c.length() == 1) {
                if ((unwrapLen1SetInt && (c instanceof SetInt)) ||
                        (unwrapLen1SetExt && (c instanceof SetExt)) ||
                        (unwrapLen1Product && (c instanceof Product))
                        ) {
                    return c.term(0);
                }
            }
        }

        return x;
    }

    @Override
    public StringBuilder toStringBuilder(boolean pretty) {
        StringBuilder sb = new StringBuilder();
        try {
            append(sb, pretty);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    /**
     * Abstract method to get the operate of the compound
     */
    @Override
    public abstract Op op();

    /**
     * Abstract clone method
     *
     * @return A clone of the compound term
     */
    @Override
    public abstract Term clone();

//    @Override
//    public int compareTo(final Object that) {
//        if (that == this) return 0;
//
//        // variables have earlier sorting order than non-variables
//        if (!(that instanceof Compound)) return 1;
//
//        final Compound c = (Compound) that;
//
//        int opdiff = compareClass(this, c);
//        if (opdiff != 0) return opdiff;
//
//        return compare(c);
//    }

//    public static int compareClass(final Object b, final Object c) {
//        Class c1 = b.getClass();
//        Class c2 = c.getClass();
//        int h = Integer.compare(c1.hashCode(), c2.hashCode());
//        if (h != 0) return h;
//        return c1.getName().compareTo(c2.getName());
//    }

//    /**
//     * compares only the contents of the subterms; assume that the other term is of the same operator type
//     */
//    public int compareSubterms(final Compound otherCompoundOfEqualType) {
//        return Terms.compareSubterms(term, otherCompoundOfEqualType.term);
//    }


//    final static int maxSubTermsForNameCompare = 2; //tunable
//
//    protected int compare(final Compound otherCompoundOfEqualType) {
//
//        int l = length();
//
//        if ((l != otherCompoundOfEqualType.length()) || (l < maxSubTermsForNameCompare))
//            return compareSubterms(otherCompoundOfEqualType);
//
//        return compareName(otherCompoundOfEqualType);
//    }
//
//
//    public int compareName(final Compound c) {
//        return super.compareTo(c);
//    }


    @Override
    final public int hashCode() {
        return contentHash;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Compound)) return false;
        Compound c = (Compound) that;
        if (contentHash != c.contentHash ||
                structureHash != c.structureHash ||
                volume() != c.volume())
            return false;

        final int s = this.length();
        for (int i = 0; i < s; i++) {
            Term a = term(i);
            Term b = c.term(i);
            if (!a.equals(b)) return false;
        }

        return true;
    }


    @Override
    public int compareTo(final Object o) {
        if (this == o) return 0;
        if (!(o instanceof Compound))
            return 1;

        int diff;
        if ((diff = Integer.compare(o.hashCode(), hashCode())) != 0)
            return diff;

        final Compound c = (Compound) o;

        final int s = this.length();
        if ((diff = Integer.compare(s, c.length())) != 0)
            return diff;

        if ((diff = Integer.compare(structureHash, c.structureHash)) != 0)
            return diff;

        for (int i = 0; i < s; i++) {
            final Term a = term(i);
            final Term b = c.term(i);
            final int d = a.compareTo(b);
            if (d != 0) return d;
        }

        return 0;
    }

    public final void recurseTerms(final TermVisitor v, final Term parent) {
        v.visit(this, parent);
        //if (this instanceof Compound) {
        for (final Term t : ((Compound) this).term) {
            t.recurseTerms(v, this);
        }
        //}
    }

//    public final void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
//        if (hasVar()) {
//            v.visit(this, parent);
//            //if (this instanceof Compound) {
//            for (Term t : term) {
//                t.recurseSubtermsContainingVariables(v, this);
//            }
//            //}
//        }
//    }

    /**
     * extracts a subterm provided by the address tuple
     * returns null if specified subterm does not exist
     */
    public <X extends Term> X subterm(final int... address) {
        Term ptr = this;
        for (final int i : address) {
            if (ptr instanceof Compound) {
                ptr = ((Compound) ptr).term[i];
            }
        }
        return (X) ptr;
    }


    /* UNTESTED
    public Compound clone(VariableTransform t) {
        if (!hasVar())
            throw new RuntimeException("this VariableTransform clone should not have been necessary");

        Compound result = cloneVariablesDeep();
        if (result == null)
            throw new RuntimeException("unable to clone: " + this);

        result.transformVariableTermsDeep(t);

        result.invalidate();

        return result;
    } */


    @Override
    final public <T extends Term> T normalized() {
        return normalized(false);
    }

    /**
     * careful: this will modify the term and should not be used unless the instance is new and unreferenced.
     */
    public <T extends Term> T normalizeDestructively() {
        return normalized(true);
    }

    /**
     * Normalizes if contain variables which need to be finalized for use in a Sentence
     * May return null if the resulting compound term is invalid
     */
    protected <T extends Term> T normalized(final boolean destructive) {

        if (isNormalized()) {
            return (T) this;
        } else {
            final Compound result = new VariableNormalization(this, destructive).getResult();
            if (result == null)
                return null;

            return (T) result;
        }

    }

    @Override
    public int volume() {
        return volume;
    }

    /**
     * Must be Term return type because the type of Term may change with different arguments
     */
    abstract public Term clone(final Term[] replaced);

    public Compound cloneDeep() {
        Term c = clone(cloneTermsDeep());
        if (c == null) return null;

//        if (c.operator() != operator()) {
//            throw new RuntimeException("cloneDeep resulted in different class: " + c + '(' + c.getClass() + ") from " + this + " (" + getClass() + ')');
//        }


        return ((Compound) c);
    }


    public static Compound<?> transformIndependentToDependentVariables(final Compound c) {
        if (!c.hasVarIndep())
            return c;

        return (Compound) c.cloneTransforming(new TransformIndependentToDependentVariables());
    }

    public <X extends Compound> X cloneTransforming(final CompoundTransform t) {
        if (t.testSuperTerm(this)) {
            Term[] cls = cloneTermsTransforming(t, 0);
            if (cls == null) return null;
            return (X) clone(cls);
        }
        return (X) this;
    }


    /**
     * override in subclasses to avoid unnecessary reinit
     */
    /*public CompoundTerm _clone(final Term[] replaced) {
        if (Terms.equals(term, replaced)) {
            return this;
        }
        return clone(replaced);
    }*/
    @Override
    public int containedTemporalRelations() {
        if (containedTemporalRelations == -1) {

            /*if ((this instanceof Equivalence) || (this instanceof Implication))*/
            {
                int temporalOrder = this.getTemporalOrder();
                switch (temporalOrder) {
                    case TemporalRules.ORDER_FORWARD:
                    case TemporalRules.ORDER_CONCURRENT:
                    case TemporalRules.ORDER_BACKWARD:
                        containedTemporalRelations = 1;
                        break;
                    default:
                        containedTemporalRelations = 0;
                        break;
                }
            }

            for (final Term t : term)
                containedTemporalRelations += t.containedTemporalRelations();
        }
        return this.containedTemporalRelations;
    }

//    @Override
//    public boolean equals(final Object that) {
//        if (!(that instanceof CompoundTerm))
//            return false;
//        
//        final CompoundTerm t = (CompoundTerm)that;
//        return name().equals(t.name());
//        
//        /*if (hashCode() != t.hashCode())
//            return false;
//        
//        if (operate() != t.operate())
//            return false;
//        
//        if (size() != t.size())
//            return false;
//        
//        for (int i = 0; i < term.size(); i++) {
//            final Term c = term.get(i);
//            if (!c.equals(t.componentAt(i)))
//                return false;
//        }
//        
//        return true;*/
//        
//    }


 

    /* ----- utilities for other fields ----- */

    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */
    @Override
    public int complexity() {
        return complexity;
    }


    /**
     * Check if the order of the term matters
     * <p>
     * commutative CompoundTerms: Sets, Intersections Commutative Statements:
     * Similarity, Equivalence (except the one with a temporal order)
     * Commutative CompoundStatements: Disjunction, Conjunction (except the one
     * with a temporal order)
     *
     * @return The default value is false
     */
    public boolean isCommutative() {
        return false;
    }

    /* ----- extend Collection methods to component list ----- */

    /**
     * get the number of term
     *
     * @return the size of the component list
     */
    @Override
    public int length() {
        return term.length;
    }


    /**
     * Gives a set of all (unique) contained term, recursively
     */
    public Set<Term> getContainedTerms() {
        Set<Term> s = Global.newHashSet(complexity());
        for (Term t : term) {
            s.add(t);
            if (t instanceof Compound)
                s.addAll(((Compound) t).getContainedTerms());
        }
        return s;
    }

    /**
     * (shallow) Clone the component list
     */
    public Term[] cloneTerms(final Term... additional) {

        return cloneTermsAppend(term, additional);
    }

    /**
     * (shallow) Clone the component list
     */
    public Term[] cloneTerms() {
        return copyOf(term, term.length);
    }


    /**
     * Cloned array of Terms, except for one or more Terms.
     *
     * @param toRemove
     * @return the cloned array with the missing terms removed,
     * OR null if no terms were actually removed when requireModification=true
     */
    public Term[] cloneTermsExcept(final boolean requireModification, final Term... toRemove) {

        final int toRemoveLen = toRemove.length;
        if (toRemoveLen == 0)
            throw new RuntimeException("no removals specified");
        else if (toRemoveLen == 1) {
            //use the 1-term optimized version of this method
            return cloneTermsExcept(requireModification, toRemove[0]);
        }

        final int n = length();
        final Term[] l = new Term[n];

        final Set<Term> toRemoveSet = Compound.termSet(toRemove);


        int remain = 0;
        for (int i = 0; i < n; i++) {
            final Term x = term(i);
            if (!toRemoveSet.contains(x))
                l[remain++] = x;
        }

        return resultOfCloneTermsExcept(requireModification, l, remain);
    }

    /**
     * Cloned array of Terms, except for a specific Term.
     *
     * @param toRemove
     * @return the cloned array with the missing terms removed,
     * OR null if no terms were actually removed when requireModification=true
     */
    public Term[] cloneTermsExcept(final boolean requireModification, final Term toRemove) {

        final int n = length();
        final Term[] l = new Term[n];


        int remain = 0;
        for (int i = 0; i < n; i++) {
            final Term x = term(i);
            if (!toRemove.equals(x))
                l[remain++] = x;
        }

        return resultOfCloneTermsExcept(requireModification, l, remain);
    }

    private static Term[] resultOfCloneTermsExcept(boolean requireModification, Term[] l, int remain) {
        final boolean removed = (remain != l.length);


        if (!removed) {
            //no removals
            if (requireModification)
                return null;
            return l;
        } else {
            //trim the array
            return Arrays.copyOf(l, remain);
        }
    }


    /**
     * creates a new ArrayList for terms
     */
    public List<Term> asTermList() {
        List<Term> l = Global.newArrayList(length());
        addTermsTo(l);
        return l;
    }

    public static Set<Term> termSet(Term... t) {
        Set<Term> l = Global.newHashSet(t.length);
        for (Term x : t) l.add(x);
        return l;
    }

    /**
     * forced deep clone of terms
     */
    public Term[] cloneTermsDeep() {
        Term[] l = new Term[length()];
        for (int i = 0; i < l.length; i++)
            l[i] = term[i].cloneDeep();
        return l;
    }


    /**
     * clones all non-constant sub-compound terms, excluding the variables themselves which are not cloned. they will be replaced in a subsequent transform step
     */
    public Compound cloneVariablesDeep() {
        return (Compound) clone(cloneTermsDeepIfContainingVariables());
    }

    public Term[] cloneTermsDeepIfContainingVariables() {
        Term[] l = new Term[length()];
        for (int i = 0; i < l.length; i++) {
            Term t = term[i];

            if ((!(t instanceof Variable)) && (t.hasVar())) {
                t = t.cloneDeep();
            }

            //else it is an atomic term or a compoundterm with no variables, so use as-is:
            l[i] = t;
        }
        return l;
    }

    public <T extends Compound> boolean transform(CompoundTransform trans) {
        return transform(trans, 0);
    }

    @Override
    public boolean levelValid(final int nal) {
        if (!op().levelValid(nal))
            return false;

        //TODO use structural hash
        for (Term sub : term) {
            if (!sub.levelValid(nal))
                return false;
        }
        return true;
    }

    /**
     * transforms destructively, may need to use on a new clone
     * @return if changed
     */
    protected <I extends Compound> boolean transform(CompoundTransform<I, T> trans, int depth) {
        final int len = length();

        boolean changed = false;

        I thiss = null;
        for (int i = 0; i < len; i++) {
            Term t = term[i];

            if (trans.test(t)) {
                if (thiss == null) thiss = (I) this;
                T s = trans.apply(thiss, (T) t, depth + 1);
                if (!s.equals(t)) {
                    term[i] = s;
                    changed = true;
                }
            } else if (t instanceof Compound) {
                //recurse
                changed |= ((Compound) t).transform(trans);
            }

        }

        if (changed) {
            rehash();
        }

        return changed;
    }

    /**
     * forced deep clone of terms
     */
    public ArrayList<Term> cloneTermsListDeep() {
        ArrayList<Term> l = new ArrayList(length());
        for (final Term t : term)
            l.add(t.clone());
        return l;
    }

    /**
     * Check the subterms (first level only) for a target term
     *
     * @param t The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean containsTerm(final Term t) {
        if (impossibleSubterm(t))
            return false;
        return Terms.contains(term, t);
    }


    
    /*static void shuffle(final Term[] list, final Random randomNumber) {
        if (list.length < 2)  {
            return;
        }
        
        
        int n = list.length;
        for (int i = 0; i < n; i++) {
            // between i and n-1
            int r = i + (randomNumber.nextInt() % (n-i));
            Term tmp = list[i];    // swap
            list[i] = list[r];
            list[r] = tmp;
        }
    }*/
    
/*        public static void shuffle(final Term[] ar,final Random rnd)
        {
            if (ar.length < 2)
                return;



          for (int i = ar.length - 1; i > 0; i--)
          {
            int index = randomNumber.nextInt(i + 1);
            // Simple swap
            Term a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
          }

        }*/

    /**
     * Recursively check if a compound contains a term
     * This method DOES check the equality of this term itself.
     * Although that is how Term.containsTerm operates
     *
     * @param target The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean equalsOrContainsTermRecursively(final Term target) {
        if (this.equals(target)) return true;
        return containsTermRecursively(target);
    }

    /**
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
     *  ^^^ is this right? if so then try containsVariablesAsWildcard
     *
     * @param t The component to be checked
     * @return Whether the component is in the compound
     */
    //return Terms.containsVariablesAsWildcard(term, t);
    //^^ ???

    /**
     * searches for a subterm
     * TODO parameter for max (int) level to scan down
     */
    public boolean containsTermRecursively(final Term target) {
        if (impossibleSubterm(target)) return false;

        for (final Term x : term) {
            if (impossibleSubTermOrEquality(target))
                continue;
            if (x.equals(target)) return true;
            if (x instanceof Compound) {
                if (((Compound) x).containsTermRecursively(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * true if equal operate and all terms contained
     */
    public boolean containsAllTermsOf(final Term t) {
        if ((op() == t.op())) {
            return Terms.containsAll(term, ((Compound) t).term);
        } else {
            return this.containsTerm(t);
        }
    }

//    /**
//     * Try to replace a component in a compound at a given index by another one
//     *
//     * @param index   The location of replacement
//     * @param subterm The new component
//     * @return The new compound
//     */
//    public Term cloneReplacingSubterm(final int index, final Term subterm) {
//
//        final boolean e = (subterm != null) && (op() == subterm.op());
//
//        //if the subterm is alredy equivalent, just return this instance because it will be equivalent
//        if (subterm != null && (e) && (term[index].equals(subterm)))
//            return this;
//
//        List<Term> list = asTermList();//Deep();
//
//        list.remove(index);
//
//        if (subterm != null) {
//            if (!e) {
//                list.add(index, subterm);
//            } else {
//                //splice in subterm's subterms at index
//                for (final Term t : term) {
//                    list.add(t);
//                }
//
//                /*Term[] tt = ((Compound) subterm).term;
//                for (int i = 0; i < tt.length; i++) {
//                    list.add(index + i, tt[i]);
//                }*/
//            }
//        }
//
//        return Memory.term(this, list);
//    }


    /**
     * Check whether the compound contains all term of another term, or
     * that term as a whole
     *
     * @param t The other term
     * @return Whether the term are all in the compound
     */
//    public boolean containsAllTermsOf_(final Term t) {
//        if (t instanceof CompoundTerm) {
//        //if (operate() == t.operate()) {
//            //TODO make unit test for containsAll
//            return Terms.containsAll(term, ((CompoundTerm) t).term );
//        } else {
//            return Terms.contains(term, t);
//        }
//    }


//    /**
//     * Try to add a component into a compound
//     *
//     * @param t1 The compound
//     * @param t2 The component
//     * @param memory Reference to the memory
//     * @return The new compound
//     */
//    public static Term addComponents(final CompoundTerm t1, final Term t2, final Memory memory) {
//        if (t2 == null)
//            return t1;
//        
//        boolean success;
//        Term[] terms;
//        if (t2 instanceof CompoundTerm) {
//            terms = t1.cloneTerms(((CompoundTerm) t2).term);
//        } else {
//            terms = t1.cloneTerms(t2);
//        }
//        return Memory.make(t1, terms, memory);
//    }
    @Override
    public int varDep() {
        return hasVarDeps;
    }

    /* ----- variable-related utilities ----- */

    @Override
    public int varIndep() {
        return hasVarIndeps;
    }

    @Override
    public int varQuery() {
        return hasVarQueries;
    }

    @Override
    public int vars() {
        return varTotal;
    }

    @Override
    public boolean hasVar() {
        return varTotal > 0;
    }

    public final boolean hasVar(final Op type) {

        switch (type) {
            case VAR_DEPENDENT:
                return hasVarDep();
            case VAR_INDEPENDENT:
                return hasVarIndep();
            case VAR_QUERY:
                return hasVarQuery();
            case VAR_PATTERN:
                /* if this is the case, its always the case because
                   then its the meta-matcher which asks
                   who only operators with PATTERN variables
                 */
                return true;
        }
        throw new RuntimeException("Invalid variable type: " + type);
    }


    /**
     * Recursively apply a substitute to the current CompoundTerm
     * May return null if the term can not be created
     *
     * @param subs
     */
    public Term applySubstitute(final Map<Term, Term> subs) {

        //TODO calculate superterm capacity limits vs. subs min/max

        if ((subs == null) || (subs.isEmpty())) {
            return this;
        }

        return new Substitution(subs).apply(this);
    }


    /**
     * returns result of applySubstitute, if and only if it's a CompoundTerm.
     * otherwise it is null
     */
    public Compound applySubstituteToCompound(Map<Term, Term> substitute) {
        Term t = applySubstitute(substitute);
        if (t instanceof Compound)
            return ((Compound) t);
        return null;
    }

    final public void addTermsTo(final Collection<Term> c) {
        Collections.addAll(c, term);
    }

    @Override
    public final boolean isNormalized() {
        return !hasVar();
    }


//    /**
//     * compare subterms where any variables matched are not compared
//     */
//    public boolean equalsVariablesAsWildcards(final Compound c) {
//        if (!(op() == c.op())) return false;
//        if (length() != c.length()) return false;
//        for (int i = 0; i < length(); i++) {
//            Term a = term[i];
//            Term b = c.term[i];
//            if ((a instanceof Variable) /*&& (a.hasVarDep())*/ ||
//                    ((b instanceof Variable) /*&& (b.hasVarDep())*/))
//                continue;
//            if (!a.equals(b)) return false;
//        }
//        return true;
//    }

//    public Term[] cloneTermsReplacing(final Term from, final Term to) {
//        Term[] y = new Term[length()];
//        int i = 0;
//        for (Term x : term) {
//            if (x.equals(from))
//                x = to;
//            y[i++] = x;
//        }
//        return y;
//    }

    public Term[] cloneTermsReplacing(int index, final Term replaced) {
        Term[] y = cloneTerms();
        y[index] = replaced;
        return y;
    }


    protected <I extends Compound, T extends Term> Term[] cloneTermsTransforming(final CompoundTransform<I, T> trans, final int level) {
        final Term[] y = new Term[length()];
        int i = 0;
        for (Term x : this.term) {
            if (trans.test(x)) {
                x = trans.apply((I) this, (T) x, level);
            } else if (x instanceof Compound) {
                //recurse
                Compound cx = (Compound) x;
                if (trans.testSuperTerm(cx)) {
                    Term[] cls = cx.cloneTermsTransforming(trans, level + 1);
                    if (cls == null) return null;
                    x = cx.clone(cls);
                }
            }
            if (x == null) return null;
            y[i++] = x;
        }
        return y;
    }

    public final int size() {
        return length();
    }

    public boolean isEmpty() {
        return length() != 0;
    }

    /**
     * first level only, not recursive
     */
    public boolean contains(Object o) {
        if (o instanceof Term)
            return containsTerm((Term) o);
        return false;
    }

    @Override
    public final Iterator<T> iterator() {
        return Iterators.forArray(term);
    }

    @Override
    public final void forEach(final Consumer<? super T> action) {
        for (T t : this.term)
            action.accept(t);
    }

    @Override
    public String toStringCompact() {
        return toString(false);
    }

    @Override
    public String toString(boolean pretty) {
        return toStringBuilder(pretty).toString();
    }

    @Override
    public Object first() {
        return term[0];
    }

    /**
     * cdr or 'rest' function for s-expression interface when arity > 1
     */
    @Override
    public Object rest() {
        final int len = length();
        if (len == 1) throw new RuntimeException("Pair fault");
        if (len == 2) return term[1];
        if (len == 3) return new Pair(term[1], term[2]);
        if (len == 4) return new Pair(term[1], new Pair(term[2], term[3]));

        //this may need tested better:
        Pair p = null;
        for (int i = len - 2; i >= 0; i--) {
            if (p == null)
                p = new Pair(term[i], term[i + 1]);
            else
                p = new Pair(term[i], p);
        }
        return p;
    }

    @Override
    public Object setFirst(Object first) {
        throw new RuntimeException(this + " not modifiable");
    }

    @Override
    public Object setRest(Object rest) {
        throw new RuntimeException(this + " not modifiable");
    }

//    public int countOccurrences(final Term t) {
//        final AtomicInteger o = new AtomicInteger(0);
//
//        if (equals(t)) return 1;
//
//        recurseTerms((n, p) -> {
//            if (n.equals(t))
//                o.incrementAndGet();
//        });
//
//        return o.get();
//    }

    /** unordered set */
    public Set<T> asTermSet() {

        final int l = length();
        if (l ==1)
            return Collections.singleton(term[0]);

        final HashSet<T> s = new HashSet(l);
        Collections.addAll(s, term);
        return s;
    }

    public Set<T> asTermSortedSet() {
        final int l = length();
        if (l ==1)
            return Collections.singleton(term[0]);

        final TreeSet<T> s = new TreeSet();
        Collections.addAll(s, term);
        return s;
    }


    public static class InvalidTermConstruction extends RuntimeException {
        public InvalidTermConstruction(String reason) {
            super(reason);
        }
    }


//    /**
//     * single term version of makeCompoundName without iteration for efficiency
//     */
//    @Deprecated
//    protected static CharSequence makeCompoundName(final Op op, final Term singleTerm) {
//        int size = 2; // beginning and end parens
//        String opString = op.toString();
//        size += opString.length();
//        final CharSequence tString = singleTerm.toString();
//        size += tString.length();
//        return new StringBuilder(size).append(COMPOUND_TERM_OPENER).append(opString).append(ARGUMENT_SEPARATOR).append(tString).append(COMPOUND_TERM_CLOSER).toString();
//    }

    //    @Deprecated public static class UnableToCloneException extends RuntimeException {
//
//        public UnableToCloneException(String message) {
//            super(message);
//        }
//
//        @Override
//        public synchronized Throwable fillInStackTrace() {
//            /*if (Parameters.DEBUG) {
//                return super.fillInStackTrace();
//            } else {*/
//                //avoid recording stack trace for efficiency reasons
//                return this;
//            //}
//        }
//
//
//    }


}


//    /** performs a deep comparison of the term structure which should have the same result as normal equals(), but slower */
//    @Deprecated public boolean equalsByTerm(final Object that) {
//        if (!(that instanceof CompoundTerm)) return false;
//
//        final CompoundTerm t = (CompoundTerm)that;
//
//        if (operate() != t.operate())
//            return false;
//
//        if (getComplexity()!= t.getComplexity())
//            return false;
//
//        if (getTemporalOrder()!=t.getTemporalOrder())
//            return false;
//
//        if (!equals2(t))
//            return false;
//
//        if (term.length!=t.term.length)
//            return false;
//
//        for (int i = 0; i < term.length; i++) {
//            if (!term[i].equals(t.term[i]))
//                return false;
//        }
//
//        return true;
//    }
//
//
//
//
//    /** additional equality checks, in subclasses, only called by equalsByTerm */
//    @Deprecated public boolean equals2(final CompoundTerm other) {
//        return true;
//    }

//    /** may be overridden in subclass to include other details */
//    protected int calcHash() {
//        //return Objects.hash(operate(), Arrays.hashCode(term), getTemporalOrder());
//        return name().hashCode();
//    }

//
//    /**
//     * Orders among terms: variable < atomic < compound
//     *
//     * @param that The Term to be compared with the current Term
//\     * @return The order of the two terms
//     */
//    @Override
//    public int compareTo(final AbstractTerm that) {
//        if (this == that) return 0;
//
//        if (that instanceof CompoundTerm) {
//            final CompoundTerm t = (CompoundTerm) that;
//            if (size() == t.size()) {
//                int opDiff = this.operate().ordinal() - t.operate().ordinal(); //should be faster faster than Enum.compareTo
//                if (opDiff != 0) {
//                    return opDiff;
//                }
//
//                int tDiff = this.getTemporalOrder() - t.getTemporalOrder(); //should be faster faster than Enum.compareTo
//                if (tDiff != 0) {
//                    return tDiff;
//                }
//
//                for (int i = 0; i < term.length; i++) {
//                    final int diff = term[i].compareTo(t.term[i]);
//                    if (diff != 0) {
//                        return diff;
//                    }
//                }
//
//                return 0;
//            } else {
//                return size() - t.size();
//            }
//        } else {
//            return 1;
//        }
//    }



    /*
    @Override
    public boolean equals(final Object that) {
        return (that instanceof Term) && (compareTo((Term) that) == 0);
    }
    */


//
//
//
//
//    /**
//     * Orders among terms: variable < atomic < compound
//     *
//     * @param that The Term to be compared with the current Term
//\     * @return The order of the two terms
//     */
//    @Override
//    public int compareTo(final Term that) {
//        /*if (!(that instanceof CompoundTerm)) {
//            return getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
//        }
//        */
//        return -name.compareTo(that.name());
//            /*
//            if (size() == t.size()) {
//                int opDiff = this.operate().ordinal() - t.operate().ordinal(); //should be faster faster than Enum.compareTo
//                if (opDiff != 0) {
//                    return opDiff;
//                }
//
//                for (int i = 0; i < term.length; i++) {
//                    final int diff = term[i].compareTo(t.term[i]);
//                    if (diff != 0) {
//                        return diff;
//                    }
//                }
//
//                return 0;
//            } else {
//                return size() - t.size();
//            }
//        } else {
//            return 1;
//            */
//    }
