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
package nars.nal.term;

import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.NALOperator;
import nars.nal.Terms;
import nars.nal.nal7.TemporalRules;
import nars.util.data.FastPutsArrayMap;
import nars.util.utf8.ByteBuf;
import nars.util.utf8.Utf8;
import nars.util.data.sexpression.IPair;
import nars.util.data.sexpression.Pair;

import java.util.*;

import static java.util.Comparator.naturalOrder;
import static nars.nal.NALOperator.COMPOUND_TERM_CLOSER;
import static nars.nal.NALOperator.COMPOUND_TERM_OPENER;

/** a compound term */
public abstract class Compound implements Term, Iterable<Term>, IPair {



    /**
     * list of (direct) term
     */
    public final Term[] term;

    /**
     * syntactic complexity of the compound, the sum of those of its term
     * plus 1
     * TODO make final again
     */
    transient public short complexity;


    /**
     * Whether contains a variable
     */
    transient private boolean hasVariables, hasVarQueries, hasVarIndeps, hasVarDeps;

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
    protected static CharSequence makeCompoundName(final NALOperator op, final Term singleTerm) {
        int size = 2; // beginning and end parens
        String opString = op.toString();
        size += opString.length();
        final CharSequence tString = singleTerm.toString();
        size += tString.length();
        return new StringBuilder(size).append(COMPOUND_TERM_OPENER.ch).append(opString).append(Symbols.ARGUMENT_SEPARATOR).append(tString).append(COMPOUND_TERM_CLOSER.ch).toString();
    }
    protected static byte[] makeCompound1Key(final NALOperator op, final Term singleTerm) {

        final byte[] opString = op.toBytes();

        final byte[] tString = singleTerm.name();

        return ByteBuf.create(1 + opString.length + 1 + tString.length + 1)
                .add((byte)COMPOUND_TERM_OPENER.ch)
                .add(opString)
                .add((byte) Symbols.ARGUMENT_SEPARATOR)
                .add(tString)
                .add((byte) COMPOUND_TERM_CLOSER.ch)
                .toBytes();
    }

    protected static byte[] makeCompoundNKey(final NALOperator op, final Term... arg) {

        ByteBuf b = ByteBuf.create(64)
                .add((byte)COMPOUND_TERM_OPENER.ch)
                .add(op.toBytes());

        for  (final Term t : arg) {
            b.add((byte)Symbols.ARGUMENT_SEPARATOR).add(t.name());
        }

        return b.add((byte) COMPOUND_TERM_CLOSER.ch).toBytes();

    }

    /**
     * default method to make the oldName of a compound term from given fields
     *
     * @param op  the term operate
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static CharSequence makeCompoundName(final NALOperator op, final Term... arg) {
        throw new RuntimeException("Not necessary, utf8 keys should be used instead");
//
//        int size = 1 + 1;
//
//        String opString = op.toString();
//        size += opString.length();
//        /*for (final Term t : arg)
//            size += 1 + t.name().length();*/
//
//
//        final StringBuilder n = new StringBuilder(size)
//                .append(COMPOUND_TERM_OPENER.ch).append(opString);
//
//        for (final Term t : arg) {
//            n.append(Symbols.ARGUMENT_SEPARATOR).append(t.toString());
//        }
//
//        n.append(COMPOUND_TERM_CLOSER.ch);
//
//        return n.toString();
    }

    /**
     * Shallow clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(final Term[] original, final Term[] additional) {
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

    public static <T> void shuffle(T[] ar, Random rnd) {
        if (ar.length < 2) return;
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            T a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public Term term(int i ) { return term[i]; }

    /**
     * Abstract method to get the operate of the compound
     */
    @Override
    public abstract NALOperator operator();

    /**
     * Abstract clone method
     *
     * @return A clone of the compound term
     */
    @Override
    public abstract Term clone();

    abstract public int hashCode();

    @Override
    public int compareTo(final Term that) {
        if (that==this) return 0;

        // variables have earlier sorting order than non-variables
        if (!(that instanceof Compound)) return 1;

        final Compound c = (Compound)that;


        int opdiff = getClass().getName().compareTo(c.getClass().getName());
        if (opdiff == 0) {
            //return compareSubterms(c);

            int sd = compareSubterms(c);
            if (sd == 0) {
                share(c);
            }
            return sd;
        }
        return opdiff;
    }

    /** copy subterms so that reference check will be sufficient to determine equality
     * assumes that 'equivalent' has already been determined to be equal.
     * */
    protected void share(Compound equivalent) {
        if (!hasVar()) {
            //System.arraycopy(term, 0, equivalent.term, 0, term.length);
        }
    }

    /** compares only the contents of the subterms; assume that the other term is of the same operator type */
    abstract public int compareSubterms(final Compound otherCompoundOfEqualType);

    @Override
    abstract public boolean equals(final Object that);

    public void recurseTerms(final TermVisitor v, Term parent) {
        v.visit(this, parent);
        if (this instanceof Compound) {
            for (Term t : ((Compound)this).term) {
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

    /** extracts a subterm provided by the index tuple
     *  returns null if specified subterm does not exist
     * */
    public <X extends Term> X subterm(int... index) {
        Term ptr = this;
        for (int i : index) {
            if (ptr instanceof Compound) {
                ptr = ((Compound)ptr).term[i];
            }
        }
        return (X) ptr;
    }

    public interface VariableTransform  {
        public Variable apply(Compound containingCompound, Variable v, int depth);
    }



    public static class VariableNormalization implements VariableTransform {

        /** overridden keyEquals necessary to implement alternate variable hash/equality test for use in normalization's variable transform hashmap */
        static final class VariableMap extends FastPutsArrayMap<Variable,Variable> {

            final static Comparator<Entry<Variable,Variable>> comp = new Comparator<Entry<Variable,Variable>>() {
                @Override public int compare(Entry<Variable,Variable> c1, Entry<Variable,Variable> c2) {
                    return c1.getKey().compareTo(c2.getKey());
                }
            };

            public VariableMap(int initialCapacity) {
                super(initialCapacity);
            }

            @Override
            public boolean keyEquals(final Variable a, final Object ob) {
                if (a == ob) return true;
                Variable b = ((Variable)ob);
                if (!b.isScoped() || !a.isScoped())
                    return false;
                return Utf8.equals2(b.name(), a.name());
            }

            @Override
            public Variable put(Variable key, Variable value) {
                Variable removed = super.put(key, value);
                /*if (size() > 1)
                    Collections.sort(entries, comp);*/
                return removed;
            }
        }

        VariableMap rename = null;

        final Compound result;
        boolean renamed = false;

        public VariableNormalization(Compound target) {

            this.result = target.cloneVariablesDeep();
            if (this.result!=null)
                this.result.transformVariableTermsDeep(this);


            if (rename!=null)
                rename.clear(); //assist GC
        }

        @Override
        public Variable apply(final Compound ct, final Variable v, int depth) {
            Variable vname = v;
//            if (!v.hasVarIndep() && v.isScoped()) //already scoped; ensure uniqueness?
//                vname = vname.toString() + v.getScope().name();


            if (rename==null) rename = new VariableMap(2); //lazy allocate

            Variable vv = rename.get(vname);

            if (vv == null) {
                //type + id
                vv = new Variable(
                    Variable.getName(v.getType(), rename.size() + 1),
                    true
                );
                rename.put(vname, vv);
                renamed = !Utf8.equals2(vv.name(), v.name());
            }

            return vv;
        }

        public boolean hasRenamed() {
            return renamed;
        }

        public Compound getResult() {
            return result;
        }
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

    /**
     * Normalizes if contain variables which need to be finalized for use in a Sentence
     * May return null if the resulting compound term is invalid
     */
    @Override
    public <T extends Term> T normalized() {
        if (!hasVar()) return (T) this;
        if (isNormalized()) return (T) this;


        VariableNormalization vn = new VariableNormalization(this);
        Compound result = vn.getResult();
        if (result == null) return null;

        if (vn.hasRenamed()) {
            result.invalidate();
        }

        result.setNormalized(); //dont set subterms normalized, in case they are used as pieces for something else they may not actually be normalized unto themselves (ex: <#3 --> x> is not normalized if it were its own term)


//        if (!valid(result)) {
////                UnableToCloneException ntc = new UnableToCloneException("Invalid term discovered after normalization: " + result + " ; prior to normalization: " + this);
////                ntc.printStackTrace();
////                throw ntc;
//            return null;
//        }


        return (T)result;

    }



    /**
     * call this after changing Term[] contents: recalculates variables and complexity
     */
    protected void init(Term[] term) {

        this.complexity = 1;
        this.hasVariables = this.hasVarDeps = this.hasVarIndeps = this.hasVarQueries = false;
        for (final Term t : term) {
            this.complexity += t.getComplexity();
            hasVariables |= t.hasVar();
            hasVarDeps |= t.hasVarDep();
            hasVarIndeps |= t.hasVarIndep();
            hasVarQueries |= t.hasVarQuery();
        }

        invalidate();
    }

    public void invalidate() {

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

    /**
     * default method to make the oldName of the current term from existing
     * fields.  needs overridden in certain subclasses
     *
     * @return the oldName of the term
     */
    @Deprecated protected CharSequence makeName() {
        return makeCompoundName(operator(), term);
    }

    protected byte[] makeKey() {
        return makeCompoundNKey(operator(), term);
    }




    abstract public byte[] name();

 

    /* ----- utilities for other fields ----- */

    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */
    @Override
    public short getComplexity() {
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
        return Arrays.copyOf(term, term.length);
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


    /** clones all non-constant sub-compound terms, excluding the variables themselves which are not cloned. they will be replaced in a subsequent transform step */
    protected Compound cloneVariablesDeep() {
        return (Compound) clone(cloneVariableTermsDeep());
    }

    public Term[] cloneVariableTermsDeep() {
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

    protected void transformVariableTermsDeep(VariableTransform variableTransform) {
        transformVariableTermsDeep(variableTransform, 0);
    }

    protected void transformVariableTermsDeep(VariableTransform variableTransform, int depth) {
        final int len = length();
        for (int i = 0; i < len; i++) {
            Term t = term[i];

            if (t.hasVar()) {
                if (t instanceof Compound) {
                    ((Compound)t).transformVariableTermsDeep(variableTransform);
                } else if (t instanceof Variable) {  /* it's a variable */
                    term[i] = variableTransform.apply(this, (Variable)t, depth+1);
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
        for (Term x : term) {
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
            return Terms.contains(term, t);
        }
    }

    /**
     * Try to replace a component in a compound at a given index by another one
     *
     * @param index The location of replacement
     * @param t     The new component
     * @return The new compound
     */
    public Term setComponent(final int index, final Term t) {



        final boolean e = (t!=null) && Terms.equalType(this, t, true, true);

        //if the subterm is alredy equivalent, just return this instance because it will be equivalent
        if (t != null && (e) && (term[index].equals(t)))
            return this;

        List<Term> list = asTermList();//Deep();

        list.remove(index);

        if (t != null) {
            if (!e) {
                list.add(index, t);
            } else {
                //final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                Term[] tt = ((Compound) t).term;
                for (int i = 0; i < tt.length; i++) {
                    list.add(index + i, tt[i]);
                }
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

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    @Override
    public boolean hasVar() {
        return hasVariables;
    }

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
    public boolean hasVarDep() {
        return hasVarDeps;
    }

    /* ----- variable-related utilities ----- */

    @Override
    public boolean hasVarIndep() {
        return hasVarIndeps;
    }

    @Override
    public boolean hasVarQuery() {
        return hasVarQueries;
    }

    /**
     * NOT TESTED YET
     */
    public boolean containsAnyTermsOf(final Collection<Term> c) {
        return Terms.containsAny(term, c);
    }

    /**
     * Recursively apply a substitute to the current CompoundTerm
     * May return null if the term can not be created
     *
     * @param subs
     */
    public Term applySubstitute(final Map<Term, Term> subs) {
        if ((subs == null) || (subs.isEmpty())) {
            return this;//.clone();
        }

        Term[] tt = new Term[length()];
        boolean modified = false;

        for (int i = 0; i < tt.length; i++) {
            Term t1 = tt[i] = term[i];

            if (subs.containsKey(t1)) {
                Term t2 = subs.get(t1);
                while (subs.containsKey(t2)) {
                    t2 = subs.get(t2);
                }
                //prevents infinite recursion
                if (!t2.containsTerm(t1)) {
                    tt[i] = t2; //t2.clone();
                    modified = true;
                }
            } else if (t1 instanceof Compound) {
                Term ss = ((Compound) t1).applySubstitute(subs);
                if (ss != null) {
                    tt[i] = ss;
                    if (!tt[i].equals(term[i]))
                        modified = true;
                }
            }
        }
        if (!modified)
            return this;

        if (this.isCommutative()) {
            Arrays.sort(tt);
        }

        return this.clone(tt);
    }


//    /** caches a static copy of commonly uesd index variables of each variable type */
//    public static final int maxCachedVariableIndex = 32;
//    public static final Variable[][] varCache = (Variable[][]) Array.newInstance(Variable.class, 3, maxCachedVariableIndex);
//    
//    public static Variable getIndexVariable(final char type, final int i) {
//        int typeI;
//        switch (type) {
//            case '#': typeI = 0; break;
//            case '$': typeI = 1; break;
//            case '?': typeI = 2; break;
//            default: throw new RuntimeException("Invalid variable type: " + type + ", index " + i);
//        }
//        
//        if (i < maxCachedVariableIndex) {
//            Variable existing = varCache[typeI][i];
//            if (existing == null)
//                existing = varCache[typeI][i] = new Variable(type + String.valueOf(i));
//            return existing;
//        }
//        else
//            return new Variable(type + String.valueOf(i));
//    }


//    /**
//     * Recursively rename the variables in the compound
//     *
//     * @param map The substitution established so far
//     * @return an array of terms, normalized; may return the original Term[] array if nothing changed,
//     * otherwise a clone of the array will be returned
//     */
//    public static Term[] normalizeVariableNames(String prefix, final Term[] s, final HashMap<Variable, Variable> map) {
//        
//        boolean renamed = false;
//        Term[] t = s.clone();
//        char c = 'a';
//        for (int i = 0; i < t.length; i++) {
//            final Term term = t[i];
//            
//
//            if (term instanceof Variable) {
//
//                Variable termV = (Variable)term;                
//                Variable var;
//
//                var = map.get(termV);
//                if (var == null) {
//                    //var = getIndexVariable(termV.getType(), map.size() + 1);
//                    var = new Variable(termV.getType() + /*prefix + */String.valueOf(map.size() + 1));
//                }
//                
//                if (!termV.equals(var)) {
//                    t[i] = var;
//                    renamed = true;
//                }
//
//                map.put(termV, var);
//
//            } else if (term instanceof CompoundTerm) {
//                CompoundTerm ct = (CompoundTerm)term;
//                if (ct.containVar()) {
//                    Term[] d = normalizeVariableNames(prefix + Character.toString(c),  ct.term, map);
//                    if (d!=ct.term) {                        
//                        t[i] = ct.clone(d, true);
//                        renamed = true;
//                    }
//                }
//            }        
//            c++;
//        }
//            
//        if (renamed) {            
//            return t;
//        }
//        else 
//            return s;
//    }

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

//    /** recursively sets all subterms normalization */
//    protected void setNormalizedSubTerms(boolean b) {
//        setNormalized(b);
//
//        //recursively set subterms as normalized
//        for (final Term t : term)
//            if (t instanceof CompoundTerm)
//                ((CompoundTerm)t).setNormalizedSubTerms(b);
//    }

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

    @Override
    public Iterator<Term> iterator() {
        return Iterators.forArray(term);
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
        for (int i = len  - 2; i >= 0; i--) {
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

    @Override
    public String toString() {
        return Utf8.fromUtf8(name());
    }

    abstract public byte[] nameCached();

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
