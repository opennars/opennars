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
import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLongs;
import nars.Global;
import nars.Memory;
import nars.Op;
import nars.nal.nal7.TemporalRules;
import nars.term.transform.*;
import nars.util.data.id.DynamicUTF8Identifier;
import nars.util.data.id.UTF8Identifier;
import nars.util.data.sexpression.IPair;
import nars.util.data.sexpression.Pair;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static java.util.Arrays.copyOf;
import static nars.Op.COMPOUND_TERM_CLOSER;
import static nars.Op.COMPOUND_TERM_OPENER;
import static nars.Symbols.ARGUMENT_SEPARATOR;

/**
 * a compound term
 */
public abstract class Compound extends DynamicUTF8Identifier implements Term, Collection<Term>, IPair {


    /**
     * list of (direct) term
     */
    public final Term[] term;

    /**
     * syntactic complexity of the compound, the sum of those of its term
     * plus 1
     * TODO make final again
     */

    /** bitvector of subterm types, indexed by NALOperator's .ordinal() and OR'd into by each subterm */
    long structureHash;
    int contentHash;


    /**
     * Whether contains a variable
     */
    transient private byte hasVarQueries, hasVarIndeps, hasVarDeps;
    transient private short varTotal, mass, complexity;

    transient private int containedTemporalRelations = -1;
    private boolean normalized;


    /**
     * subclasses should be sure to call init() in their constructors; it is not done here
     * to allow subclass constructors to set data before calling init()
     */
    public Compound(final Term... components) {
        super();

        this.complexity = -1;
        this.term = components;
    }




    /**
     * call this after changing Term[] contents: recalculates variables and complexity
     */
    protected void init(final Term[] term) {

        int deps = 0, indeps = 0, queries = 0;
        int compl = 1;

        final int opOrdinal = operator().ordinal();
        int subt = (1 << opOrdinal);

        final int PRIME1 = 31;
        final int PRIME2 = 92821;

        int asc = additionalStructureCode();
        long contentHash = opOrdinal + PRIME1 * asc;


        for (final Term t : term) {
            /*if (t == null)
                throw new RuntimeException("null subterm");*/
            compl += t.getComplexity();
            deps += t.varDep();
            indeps += t.varIndep();
            queries += t.varQuery();
            subt |= t.structuralHash();
            contentHash = (contentHash + t.hashCode()) * PRIME2;
        }

        this.structureHash = subt | (((long)asc) << 32);
        this.contentHash = (int)contentHash;

        this.hasVarDeps = (byte) deps;
        this.hasVarIndeps = (byte) indeps;
        this.hasVarQueries = (byte) queries;
        this.varTotal = (short)(deps + indeps + queries);
        this.complexity = (short) compl;

        if ((this.mass = (short)(varTotal + complexity)) > Global.COMPOUND_MASS_LIMIT) {
            throw new RuntimeException("mass limit exceeded for new Compound term: " + operator() + " " + Arrays.toString(term));
        }

        invalidate();
    }

    /** allows implementations to include a 32 bit identifier within the struturehash
     * which will be used to exclude inequalities. by default, it returns 0 which
     * will not change the default structureHash structure
     * @return
     */
    public int additionalStructureCode() { return 0; }

    @Override
    public boolean requiresNormalizing() {
        return true;
    }

    @Override
    public long structuralHash() {
        return structureHash;
    }


    @Override
    public byte[] init() {

        final int numArgs = term.length;

        byte[] opBytes = operator().bytes;

        int len = opBytes.length;

        for (final Term t : term) {
            len += t.bytes().length + 1;
        }

        ByteBuf b = ByteBuf.create(len);

        b.add(opBytes);

        for (int i = 0; i < numArgs; i++) {
            Term t = term[i];

            if (i!=0) {
                b.add(((byte) ARGUMENT_SEPARATOR));
            }

            b.add(t.bytes());

        }

        b.add((byte)COMPOUND_TERM_CLOSER.byt);

        return b.toBytes();

    }

    @Override
    public void append(final Writer p, final boolean pretty) throws IOException {

        boolean opener = appendTermOpener();
        if (opener)
            p.append(COMPOUND_TERM_OPENER.ch);


        final boolean appendedOperator = appendOperator(p);


        int nterms = term.length;
        for (int i = 0; i < nterms; i++) {
            if ((i!=0) || (i == 0 && nterms > 1 && appendedOperator)) {
                p.append(ARGUMENT_SEPARATOR);
                if (pretty)
                    p.append(' ');
            }

            term[i].append(p, pretty);
        }


        appendCloser(p);

    }

    public static void appendSeparator(final Writer p, final boolean pretty) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');
    }

    public boolean appendOperator(Writer p) throws IOException {
        p.append(operator().str);
        return true;
    }

    public void appendCloser(Writer p) throws IOException {
        p.append(COMPOUND_TERM_CLOSER.ch);
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

    /**
     * single term version of makeCompoundName without iteration for efficiency
     */
    @Deprecated
    protected static CharSequence makeCompoundName(final Op op, final Term singleTerm) {
        int size = 2; // beginning and end parens
        String opString = op.toString();
        size += opString.length();
        final CharSequence tString = singleTerm.toString();
        size += tString.length();
        return new StringBuilder(size).append(COMPOUND_TERM_OPENER.ch).append(opString).append(ARGUMENT_SEPARATOR).append(tString).append(COMPOUND_TERM_CLOSER.ch).toString();
    }

    public static void writeCompound1(final Op op, final Term singleTerm, Writer writer, boolean pretty) throws IOException {

        writer.append(COMPOUND_TERM_OPENER.ch);
        writer.append(op.str);
        writer.append(ARGUMENT_SEPARATOR);
        singleTerm.append(writer, pretty);
        writer.append(COMPOUND_TERM_CLOSER.ch);
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

        int j = 0;
        Term[] srcArray = original;
        for (int i = 0; i < L; i++) {
            if (i == original.length) {
                srcArray = additional;
                j = 0;
            }

            arr[i] = srcArray[j++];
        }

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

    public Term term(int i) {
        return term[i];
    }

    /**
     * Abstract method to get the operate of the compound
     */
    @Override
    public abstract Op operator();

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

    public static int compareClass(final Object b, final Object c) {
        Class c1 = b.getClass();
        Class c2 = c.getClass();
        int h = Integer.compare(c1.hashCode(), c2.hashCode());
        if (h != 0) return h;
        return c1.getName().compareTo(c2.getName());
    }

    /**
     * this will be called if the c is of the same class as 'this'.
     * the implementation can decide whether to compare by name() or by
     * subterm content
     */
    abstract protected int compare(Compound otherCompoundOfEqualType);

    public int compareName(final Compound c) {
        return super.compareTo(c);
    }

    /**
     * compares only the contents of the subterms; assume that the other term is of the same operator type
     */
    abstract public int compareSubterms(final Compound otherCompoundOfEqualType);

    @Override
    final public int hashCode() {
        return contentHash;
    }

    @Override
    final public boolean equals(final Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Compound)) return false;
        Compound c = (Compound)that;
        if (contentHash != c.contentHash ||
                structureHash != c.structureHash ||
                    length()!=c.length())
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
        if (!(o instanceof Compound)) return 1;


        int diff;
        if ((diff = UnsignedInts.compare(o.hashCode(), hashCode()))!=0)
            return diff;

        final Compound c = (Compound)o;
        if ((diff = UnsignedLongs.compare(c.structureHash, structureHash))!=0)
            return diff;

        final int s = this.length();
        if ((diff = c.length() - s)!=0)
            return diff;

        for (int i = 0; i < s; i++) {
            Term a = term(i);
            Term b = c.term(i);
            diff = a.compareTo(b);
            if (diff!=0) return diff;
        }

        return 0;
    }

    public void recurseTerms(final TermVisitor v, Term parent) {
        v.visit(this, parent);
        if (this instanceof Compound) {
            for (Term t : ((Compound) this).term) {
                t.recurseTerms(v, this);
            }
        }
    }

    public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
        if (hasVar()) {
            v.visit(this, parent);
            //if (this instanceof Compound) {
            for (Term t : term) {
                t.recurseSubtermsContainingVariables(v, this);
            }
            //}
        }
    }

    /**
     * extracts a subterm provided by the index tuple
     * returns null if specified subterm does not exist
     */
    public <X extends Term> X subterm(int... index) {
        Term ptr = this;
        for (int i : index) {
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




    protected boolean requiresNormalization() {
        return (hasVar() && !isNormalized());
    }

    @Override
    public <T extends Term> T normalized() {
        return normalized(false);
    }

    /** careful: this will modify the term and should not be used unless the instance is new and unreferenced. */
    public <T extends Term> T normalizeDestructively() {
        return normalized(true);
    }

    /**
     * Normalizes if contain variables which need to be finalized for use in a Sentence
     * May return null if the resulting compound term is invalid
     */
    protected <T extends Term> T normalized(boolean destructive) {

        if (!requiresNormalization()) return (T) this;

        VariableNormalization vn = new VariableNormalization(this, destructive);
        Compound result = vn.getResult();
        if (result == null) return null;

        if (vn.hasRenamed()) {
            result.invalidate();
        }

        result.setNormalized(); //dont set subterms normalized, in case they are used as pieces for something else they may not actually be normalized unto themselves (ex: <#3 --> x> is not normalized if it were its own term)

        return (T) result;
    }

    @Override
    public int getMass() {
        return mass;
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


    public static Compound transformIndependentToDependentVariables(Compound c) {

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


    @Override
    public UTF8Identifier name() {
        return this;
    }



 

    /* ----- utilities for other fields ----- */

    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */
    @Override
    public int getComplexity() {
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


    @Override
    public boolean isConstant() {
        return isNormalized();
    }


    /**
     * Gives a set of all (unique) contained term, recursively
     */
    public Set<Term> getContainedTerms() {
        Set<Term> s = Global.newHashSet(getComplexity());
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
     * @return the cloned array with the missing terms removed, OR null if no terms were actually removed when requireModification=true
     */
    public Term[] cloneTermsExcept(final boolean requireModification, final Term... toRemove) {
        //TODO if deep, this wastes created clones that are then removed.  correct this inefficiency?

        List<Term> l = asTermList();
        boolean removed = false;

        for (final Term t : toRemove) {
            if (l.remove(t))
                removed = true;
        }
        if ((!removed) && (requireModification))
            return null;


        return l.toArray(new Term[l.size()]);
    }

    /**
     * creates a new ArrayList for terms
     */
    public List<Term> asTermList() {
        List<Term> l = new ArrayList(length());
        addTermsTo(l);
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

    public <T extends Compound> T transform(CompoundTransform trans) {
        transform(trans, 0);
        return (T)this;
    }

    protected <I extends Compound, T extends Term> void transform(CompoundTransform<I, T> trans, int depth) {
        final int len = length();

        I thiss = null;
        for (int i = 0; i < len; i++) {
            Term t = term[i];

            if (t.hasVar()) {
                if (t instanceof Compound) {
                    ((Compound) t).transform(trans);
                } else if (trans.test(t)) {

                    if (thiss == null) thiss = (I) this;
                    term[i] = trans.apply(thiss, (T) t, depth + 1);

                }
            }
        }
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

    public boolean impossibleSubterm(final Term target) {
        return ((impossibleSubStructure(target.subtermStructure())) ||
        (impossibleSubTermMass(target.getMass())));
    }
    public boolean impossibleSubTermOrEquality(final Term target) {
        return ((impossibleSubStructure(target.subtermStructure())) ||
                (impossibleSubTermOrEqualityMass(target.getMass())));
    }

//    /** tests if another term is possibly a subterm of this, given
//     *  its mass and this term's mass.
//     *  (if the target is larger than the maximum combined subterms size,
//     *  it would not be contained by this) */
//    public boolean impossibleSubTermByMass(final Compound possibleComponent) {
//        if (impossibleSubtermByType(possibleComponent)) return true;
//        if (impossibleSubTermByMass(possibleComponent.getMass())) return true;
//        return false;
//    }

    public boolean impossibleSubStructure(final Term c) {
        return impossibleSubStructure(c.subtermStructure());
    }
    public boolean impossibleSubStructure(final int possibleSubtermStructure) {
        final int existingStructure = subtermStructure();

        //if the OR produces a different result compared to subterms,
        // it means there is some component of the other term which is not found
        return ((possibleSubtermStructure | existingStructure) != existingStructure);
    }

    public boolean impossibleSubTermMass(final int otherTermsMass) {
        return otherTermsMass >
                getMass()
                        - 1 /* for the compound itself */
                        - (length() - 1) /* each subterm has a mass >= 1, so if there are more than 1, each reduces the potential space of the insertable */
                ;
    }

    /** if it's larger than this term it can not be equal to this.
     * if it's larger than some number less than that, it can't be a subterm.
     */
    public boolean impossibleSubTermOrEqualityMass(int otherTermsMass) {
        return otherTermsMass > getMass();
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
    public boolean containsTermRecursivelyOrEquals(final Term target) {
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
        if (Terms.equalType(this, t)) {
            return Terms.containsAll(term, ((Compound) t).term);
        } else {
            return this.containsTerm(t);
        }
    }

    /**
     * Try to replace a component in a compound at a given index by another one
     *
     * @param index   The location of replacement
     * @param subterm The new component
     * @return The new compound
     */
    public Term cloneReplacingSubterm(final int index, final Term subterm) {

        final boolean e = (subterm != null) && Terms.equalType(this, subterm);

        //if the subterm is alredy equivalent, just return this instance because it will be equivalent
        if (subterm != null && (e) && (term[index].equals(subterm)))
            return this;

        List<Term> list = asTermList();//Deep();

        list.remove(index);

        if (subterm != null) {
            if (!e) {
                list.add(index, subterm);
            } else {
                //splice in subterm's subterms at index
                list.addAll(index, ((Compound) subterm));

                /*Term[] tt = ((Compound) subterm).term;
                for (int i = 0; i < tt.length; i++) {
                    list.add(index + i, tt[i]);
                }*/
            }
        }

        return Memory.term(this, list);
    }


    /**
     * Check whether the compound contains all term of another term, or
     that term as a whole
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
    public int getTotalVariables() {
        return varTotal;
    }

    @Override
    public boolean hasVar() {
        return varTotal > 0;
    }

    /**
     * NOT TESTED YET
     */
    public boolean containsAnyTermsOf(final Collection<Term> c) {
        return Terms.containsAny(this, c);
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


        Substitution S = new Substitution(subs);

        return applySubstitute(S);
    }


    public Term applySubstitute(final Substitution S) {

        if (S.impossible(this))
            return this;


        Term[] in = this.term;
        Term[] out = in;

        final int subterms = in.length;

        for (int i = 0; i < subterms; i++) {
            Term t1 = in[i];

            int m = t1.getMass();
            if (m < S.minMassOfMatch) {
                //too small to be any of the keys or hold them in a subterm
                continue;
            }

            Term t2;
            if ((t2 = S.get(t1))!=null) {


                //prevents infinite recursion
                if (!t2.containsTerm(t1)) {
                    if (out == in) out = copyOf(in, subterms);
                    out[i] = t2; //t2.clone();
                }

            } else if (t1 instanceof Compound) {

                //additional constraint here?

                Term ss = ((Compound) t1).applySubstitute(S);
                if ((ss != null) && (!ss.equals(in[i]))) {
                    //modification
                    if (out == in) out = copyOf(in, subterms);
                    out[i] = ss;
                }
            }
        }

        if (out == in) //nothing changed
            return this;

        return this.clone(out);
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
    public boolean isNormalized() {
        return normalized;
    }


    protected void setNormalized() {
        this.normalized = true;
    }


    /**
     * compare subterms where any variables matched are not compared
     */
    public boolean equalsVariablesAsWildcards(final Compound c) {
        if (!Terms.equalType(this, c)) return false;
        if (length() != c.length()) return false;
        for (int i = 0; i < length(); i++) {
            Term a = term[i];
            Term b = c.term[i];
            if ((a instanceof Variable) /*&& (a.hasVarDep())*/ ||
                    ((b instanceof Variable) /*&& (b.hasVarDep())*/))
                continue;
            if (!a.equals(b)) return false;
        }
        return true;
    }

    public Term[] cloneTermsReplacing(final Term from, final Term to) {
        Term[] y = new Term[length()];
        int i = 0;
        for (Term x : term) {
            if (x.equals(from))
                x = to;
            y[i++] = x;
        }
        return y;
    }


    protected <I extends Compound, T extends Term> Term[] cloneTermsTransforming(final CompoundTransform<I, T> trans, final int level) {
        Term[] y = new Term[length()];
        int i = 0;
        boolean mod = false;
        for (Term x : this.term) {
            if (trans.test(x)) {
                x = trans.apply((I) this, (T) x, level);
            }
            else if (x instanceof Compound) {
                //recurse
                Compound cx = (Compound)x;
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

    @Override
    public int size() {
        return length();
    }

    @Override
    public boolean isEmpty() {
        return length() != 0;
    }

    /**
     * first level only, not recursive
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof Term)
            return containsTerm((Term) o);
        return false;
    }

    @Override
    public Iterator<Term> iterator() {
        return Iterators.forArray(term);
    }

    @Override
    public Object[] toArray() {
        return term;
    }

    private boolean unsupportedCollectionMethod() {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        unsupportedCollectionMethod();
        return null;
    }

    @Override
    public boolean add(Term term) {
        return unsupportedCollectionMethod();
    }

    @Override
    public boolean remove(Object o) {
        return unsupportedCollectionMethod();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return unsupportedCollectionMethod();
    }

    @Override
    public boolean addAll(Collection<? extends Term> c) {
        return unsupportedCollectionMethod();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return unsupportedCollectionMethod();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return unsupportedCollectionMethod();
    }

    @Override
    public void clear() {
        unsupportedCollectionMethod();
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


    public static class InvalidTermConstruction extends RuntimeException {
        public InvalidTermConstruction(String reason) {
            super(reason);
        }
    }


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
