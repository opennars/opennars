/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.language;

import com.google.common.collect.Iterators;
import org.opennars.entity.TermLink;
import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;
import org.opennars.storage.Memory;

import java.nio.CharBuffer;
import java.util.*;

import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;


/**
 * Compound term as defined in the NARS-theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public abstract class CompoundTerm extends Term implements Iterable<Term> {
    
    /**
     * list of (direct) term
     *
     */
    // TODO make final again
    public final Term[] term;
    
    /**
     * syntactic complexity of the compound, the sum of those of its term plus 1
     */
    // TODO make final again
    public short complexity;
    
    
    /** Whether contains a variable */
    private boolean hasVariables, hasVarQueries, hasVarIndeps, hasVarDeps, hasIntervals;
    
    int containedTemporalRelations = -1;
    int hash;
    private boolean normalized;
    

    /**
     * method to get the operator of the compound
     */
    @Override public abstract NativeOperator operator();

    /**
     * clone method
     *
     * @return A clone of the compound term
     */
    @Override public abstract CompoundTerm clone();

    
    /** subclasses should be sure to call init() in their constructors;
     * it is not done here to allow subclass constructors to set data before calling init() */
    public CompoundTerm(final Term[] components) {
        super();
        this.term = components;
    }
    
    public static class ConvRectangle
    {
        public String index_variable = null;
        public int[] term_indices = null; //size X, size Y, pos X, pos Y, min size X, min size Y
        public ConvRectangle(){} //the latter two for being able to assing a relative index for size too
    }
    public static ConvRectangle UpdateConvRectangle(final Term[] term) {
        String index_last_var = null;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0, 
                minsX = Integer.MAX_VALUE, minsY = Integer.MAX_VALUE;
        boolean hasTermIndices = false;
        boolean calculateTermIndices = true;
        for (final Term t : term) {
            if(t != null && t.term_indices != null) {
                if(!calculateTermIndices || 
                        (t.index_variable != null && index_last_var != null &&
                        (!t.index_variable.equals(index_last_var)))) {
                    calculateTermIndices = false;
                    hasTermIndices = false;
                    continue; //different "channels", don't calculate term indices
                }
                hasTermIndices = true;
                final int size_X = t.term_indices[0];
                if(size_X < minsX)
                    minsX = size_X;
                final int size_Y = t.term_indices[1];
                if(size_Y < minsY)
                    minsY = size_Y;
                final int pos_X = t.term_indices[2];
                final int pos_Y = t.term_indices[3];
                if(pos_X < minX)
                    minX = pos_X;
                if(pos_Y < minY)
                    minY = pos_Y;
                if(pos_X+size_X > maxX)
                    maxX = pos_X+size_X;
                if(pos_Y+size_Y > maxY)
                    maxY = pos_Y+size_Y;
                
                index_last_var = t.index_variable;
            }
        }
        final ConvRectangle rect = new ConvRectangle();// = new ConvRectangle();
        if(hasTermIndices) {
            rect.term_indices = new int[6];
            rect.term_indices[0] = maxX-minX;
            rect.term_indices[1] = maxY-minY;
            rect.term_indices[2] = minX;
            rect.term_indices[3] = minY;
            rect.term_indices[4] = minsX;
            rect.term_indices[5] = minsY;
            rect.index_variable = index_last_var;
        }
        return rect;
    }
    
    /** call this after changing Term[] contents */
    protected void init(final Term[] term) {

        this.complexity = 1;
        this.hasVariables = this.hasVarDeps = this.hasVarIndeps = this.hasVarQueries = false;
        
        if(this.term_indices == null) {
            final ConvRectangle rect = UpdateConvRectangle(term);
            this.index_variable = rect.index_variable;
            this.term_indices = rect.term_indices;
        }
        
        for (final Term t : term) {

            this.complexity += t.getComplexity();
            hasVariables |= t.hasVar();
            hasVarDeps |= t.hasVarDep();
            hasVarIndeps |= t.hasVarIndep();
            hasVarQueries |= t.hasVarQuery();
            hasIntervals |= t.hasInterval();
        }
        
        invalidateName();        
        
        if (!hasVar())
            setNormalized(true);
    }

    
    public void invalidateName() {        
        this.setName(null); //invalidate name so it will be (re-)created lazily
        for (final Term t : term) {
            if (t.hasVar())
                if (t instanceof CompoundTerm)
                    ((CompoundTerm)t).invalidateName();
        }     
        setNormalized(false);
    }

    /** Must be Term return type because the type of Term may change with different arguments */
    abstract public Term clone(final Term[] replaced);
    
    @Override
    public CompoundTerm cloneDeep() {
        final Term c = clone(cloneTermsDeep());
        if (c == null)
            return null;
        if (MiscFlags.DEBUG && c.getClass()!=getClass()) //debug relevant, while it is natural due to interval 
                                                          //simplification to reduce to other term type,
                                                          //other cases should not appear
            System.out.println("cloneDeep resulted in different class: " + c + " from " + this);
        if (isNormalized())
            ((CompoundTerm)c).setNormalized(true);
        if(!(c instanceof CompoundTerm)) {
            return null;
        }
        return (CompoundTerm)c;
    }
    
    public static void transformIndependentVariableToDependent(final CompoundTerm T) { //a special instance of transformVariableTermsDeep in 1.7
        final Term[] term=T.term;
        for (int i = 0; i < term.length; i++) {
            final Term t = term[i];
            if (t.hasVar()) {
                if (t instanceof CompoundTerm) {
                    transformIndependentVariableToDependent((CompoundTerm) t);
                } else if (t instanceof Variable && ((Variable)t).isIndependentVariable()) {  /* it's a variable */
                    term[i] = new Variable(""+Symbols.VAR_DEPENDENT+t.name().subSequence(1, t.name().length())); // vars.get(t.toString());
                    assert term[i] != null;
                }
            }
        }
    }
    
    static final Interval conceptival = new Interval(1);
    private static void ReplaceIntervals(final CompoundTerm comp) {
        comp.invalidateName();
        for(int i=0; i<comp.term.length; i++) {
            final Term t = comp.term[i];
            if(t instanceof Interval) {
                assert conceptival != null;
                comp.term[i] = conceptival;
                comp.invalidateName();
            }
            else
            if(t instanceof CompoundTerm) {
                ReplaceIntervals((CompoundTerm) t);
            }
        }
    }

    public static Term replaceIntervals(Term T) {
        if(T instanceof CompoundTerm) {
            T=T.cloneDeep(); //we will operate on a copy
            if(T == null) {
                return null; //not a valid concept term
            }
            ReplaceIntervals((CompoundTerm) T);
        }
        return T;
    }
    
    private static void ExtractIntervals(final Memory mem, final List<Long> ivals, final CompoundTerm comp) {
        for(int i=0; i<comp.term.length; i++) {
            final Term t = comp.term[i];
            if(t instanceof Interval) {
                ivals.add(((Interval) t).time);
            }
            else
            if(t instanceof CompoundTerm) {
                ExtractIntervals(mem, ivals, (CompoundTerm) t);
            }
        }
    }

    public static List<Long> extractIntervals(final Memory mem, final Term T) {
        final List<Long> ret = new ArrayList<>();
        if(T instanceof CompoundTerm) {
            ExtractIntervals(mem, ret, (CompoundTerm) T);
        }
        return ret;
    }

    public static class UnableToCloneException extends RuntimeException {

        public UnableToCloneException(final String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            if (MiscFlags.DEBUG) {
                return super.fillInStackTrace();
            }
            else {
                //avoid recording stack trace for efficiency reasons
                return this;
            }
        }
        
        
    }
    
    public CompoundTerm cloneDeepVariables() {                
        final Term c = clone( cloneVariableTermsDeep() );
        
        if (c == null)
            return null;
        
        if (MiscFlags.DEBUG && c.getClass()!=getClass())
            System.out.println("cloneDeepVariables resulted in different class: " + c + " from " + this);                
        
        final CompoundTerm cc = (CompoundTerm)c;
        cc.setNormalized(isNormalized());
        return cc;
    }

    @Override
    public int containedTemporalRelations() {
        if (containedTemporalRelations == -1) {
            
            containedTemporalRelations = 0;
            
            if ((this instanceof Equivalence) || (this instanceof Implication)) {
                final int temporalOrder = this.getTemporalOrder();
                switch (temporalOrder) {
                    case TemporalRules.ORDER_FORWARD:
                    case TemporalRules.ORDER_CONCURRENT:
                    case TemporalRules.ORDER_BACKWARD:
                        containedTemporalRelations = 1;
                }                
            }
            
            for (final Term t : term)
                containedTemporalRelations += t.containedTemporalRelations();
        }
        return this.containedTemporalRelations;
    }
    
    


    /**
     * build a component list from terms
     * @return the component list
     */
    public static Term[] termArray(final Term... t) {
        return t;
    }
    public static List<Term> termList(final Term... t) {
        return Arrays.asList((Term[])t);
    }
    
    

    /* ----- utilities for oldName ----- */
    /**
     * default method to make the oldName of the current term from existing
     * fields.  needs overridden in certain subclasses
     *
     * @return the oldName of the term
     */
    protected CharSequence makeName() {
        return makeCompoundName(operator(), term);
    }

    @Override
    public CharSequence name() {
        if (this.nameInternal() == null) {
            this.setName(makeName());
        }
        return this.nameInternal();
    }
    
    
    
    /**
     * default method to make the oldName of a compound term from given fields
     *
     * @param op the term operator
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static CharSequence makeCompoundName(final NativeOperator op, final Term... arg) {
        int size = 1 + 1;
        
        final String opString = op.toString();
        size += opString.length();
        for (final Term t : arg) 
            size += 1 + t.name().length();
        
        final CharBuffer n = CharBuffer.allocate(size)
            .append(COMPOUND_TERM_OPENER.ch).append(opString);
            
        for (final Term t : arg) {            
            n.append(Symbols.ARGUMENT_SEPARATOR).append(t.name());
        }
        
        n.append(COMPOUND_TERM_CLOSER.ch);
                        
        return n.compact().toString();
    }
    

 

    /* ----- utilities for other fields ----- */
    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */    
    @Override public short getComplexity() {
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
    final public int size() {
        return term.length;
    }


    /** Gives a set of all contained term, recursively */
    public Set<Term> getContainedTerms() {
        final Set<Term> s = new HashSet(getComplexity());
        for (final Term t : term) {
            s.add(t);
            if (t instanceof CompoundTerm)
                s.addAll( ((CompoundTerm)t).getContainedTerms() );
        }
        return s;
    }

    /**
     * Clone the component list
     *
     * @return The cloned component list
     */
    public Term[] cloneTerms(final Term... additional) {
        return cloneTermsAppend(term, additional);
    }
    

    /**
     * Cloned array of Terms, except for one or more Terms.
     * @param toRemove
     * @return the cloned array with the missing terms removed, OR null if no terms were actually removed when requireModification=true
     */
    public Term[] cloneTermsExcept(final boolean requireModification, final Term[] toRemove) {
        //TODO if deep, this wastes created clones that are then removed.  correct this inefficiency?
        
        final List<Term> l = asTermList();
        boolean removed = false;
                
        for (final Term t : toRemove) {
            if (l.remove(t))
                removed = true;
        }
        if ((!removed) && (requireModification))
            return null;
                
        return l.toArray(new Term[0]);
    }
    

    
    /**
     * Deep clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(final Term[] original, final Term[] additional) {
        if (original == null) {
            return null;
        }        

        final int L = original.length + additional.length;
        if (L == 0)
            return original;
        
        //TODO apply preventUnnecessaryDeepCopy to more cases
        
        final Term[] arr = new Term[L];
        
        int i;
        int j = 0;
        Term[] srcArray = original;
        for (i = 0; i < L; i++) {            
            if (i == original.length) {
                srcArray = additional;
                j = 0;
            }
            
            arr[i] = srcArray[j++];
        }

        return arr;
        
    }

    public List<Term> asTermList() {        
        final List l = new ArrayList(term.length);
        addTermsTo(l);
        return l;
    }

    /** forced deep clone of terms */
    public Term[] cloneTermsDeep() {
        final Term[] l = new Term[term.length];
        for (int i = 0; i < l.length; i++) {
            l[i] = term[i].cloneDeep();
            if(l[i] == null) {
                return null;
            }
        }
        return l;        
    }    
    public Term[] cloneVariableTermsDeep() {
        final Term[] l = new Term[term.length];
        for (int i = 0; i < l.length; i++)  {     
            Term t = term[i];
            if (t.hasVar()) {
                if (t instanceof CompoundTerm) {
                    t = ((CompoundTerm)t).cloneDeepVariables();
                }
                else  /* it's a variable */
                    t = t.clone();
            }
            l[i] = t;            
        }
        return l;
    }
    
    /** forced deep clone of terms */
    public List<Term> cloneTermsListDeep() {
        final List<Term> l = new ArrayList(term.length);
        for (final Term t : term)
            l.add(t.clone());
        return l;        
    }
    
    static void shuffle(final Term[] ar,final Random randomNumber)
    {
        if (ar.length < 2)  {
            return;
        }

      for (int i = ar.length - 1; i > 0; i--)
      {
        final int index = randomNumber.nextInt(i + 1);
        // Simple swap
        final Term a = ar[index];
        ar[index] = ar[i];
        ar[i] = a;
      }
    }
    
    

    /**
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&amp;&amp;,&lt;a --&gt; b&gt;,&lt;b --&gt; c&gt;) also contains &lt;a --&gt; #1&gt;
     *
     * @param t The component to be checked
     * @return Whether the component is in the compound
     */
    /*
     * extra comment because it is a Implementation detail - question:
     *
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&amp;&amp;,&lt;a --&gt; b&gt;,&lt;b --&gt; c&gt;) also contains &lt;a --&gt; #1&gt;
     *  ^^^ is this right? if so then try containsVariablesAsWildcard
     */
    @Override
    public boolean containsTerm(final Term t) {        
        return Terms.contains(term, t);
        //return Terms.containsVariablesAsWildcard(term, t);
    }

    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean containsTermRecursively(final Term target) { 
        if (super.containsTermRecursively(target))
            return true;
        for (final Term term : term) {            
            if (term.containsTermRecursively(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursively count how often the terms are contained
     *
     * @param map The count map that will be created to count how often each term occurs
     * @return The counts of the terms
     */
    @Override
    public Map<Term, Integer> countTermRecursively(Map<Term,Integer> map) { 
        if(map == null) {
            map = new HashMap<Term, Integer>();
        }
        map.put(this, map.getOrDefault(this, 0) + 1);
        for (final Term term : term) {            
            term.countTermRecursively(map);
        }
        return map;
    }
    
    /**
     * Add all the components of term t into components recursively
     * 
     * @param t The term
     * @param components The components
     * @return 
     */
    public static Set<Term> addComponentsRecursively(Term t, Set<Term> components) {
        if(components == null) {
            components = new HashSet<Term>();
        }
        components.add(t);
        if(t instanceof CompoundTerm) {
            CompoundTerm cTerm = (CompoundTerm) t;
            for(Term component : cTerm) {
                addComponentsRecursively(component, components);
            }
        }
        return components;
    }

    /**
     * Check whether the compound contains all term of another term, or
 that term as a whole
     *
     * @param t The other term
     * @return Whether the term are all in the compound
     */
    public boolean containsAllTermsOf(final Term t) {
        if (getClass() == t.getClass()) { //(t instanceof CompoundTerm) {
            return Terms.containsAll(term, ((CompoundTerm) t).term );
        } else {
            return Terms.contains(term, t);
        }
    }

    /**
     * Try to replace a component in a compound at a given index by another one
     *
     * @param index The location of replacement
     * @param t The new component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public Term setComponent(final int index, final Term t, final Memory memory) {
        final List<Term> list = asTermList();//Deep();
        list.remove(index);
        if (t != null) {
            if (getClass() != t.getClass()) {
                list.add(index, t);
            } else {
                //final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                final Term[] tt = ((CompoundTerm)t).term;
                for (int i = 0; i < tt.length; i++) {
                    list.add(index + i, tt[i]);
                }
            }
        }
        if(this.isCommutative()) {
            Term[] ret = list.toArray(new Term[0]);
            return Terms.term(this, ret);
        }
        return Terms.term(this, list);
    }

    /* ----- variable-related utilities ----- */
    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    @Override
    public boolean hasVar() {
        return hasVariables;    
    }

    @Override
    public boolean hasVarDep() {
        return hasVarDeps;
    }

    @Override
    public boolean hasVarIndep() {
        return hasVarIndeps;
    }

    @Override
    public boolean hasVarQuery() {
        return hasVarQueries;
    }
    
    @Override
    public boolean hasInterval() {
        return hasIntervals;
    }
    
    /**
     * Recursively apply a substitute to the current CompoundTerm
     * May return null if the term can not be created
     * @param subs
     */
    public Term applySubstitute(final Map<Term, Term> subs) {   
        if ((subs == null) || (subs.isEmpty())) {            
            return this;//.clone();
        }
                
        final Term[] tt = new Term[term.length];
        boolean modified = false;
        
        for (int i = 0; i < tt.length; i++) {
            final Term t1 = tt[i] = term[i];
            
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
            } else if (t1 instanceof CompoundTerm) {
                final Term ss = ((CompoundTerm) t1).applySubstitute(subs);
                if (ss!=null) {
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

    /** returns result of applySubstitute, if and only if it's a CompoundTerm. 
     * otherwise it is null */
    public CompoundTerm applySubstituteToCompound(final Map<Term, Term> substitute) {
        final Term t = applySubstitute(substitute);
        if (t instanceof CompoundTerm)
            return ((CompoundTerm)t);
        return null;
    }

    
    /* ----- link CompoundTerm and its term ----- */
    /**
     * Build TermLink templates to constant term and subcomponents
     * <p>
     * The compound type determines the link type; the component type determines
     * whether to build the link.
     *
     * @return A list of TermLink templates
     */
    public List<TermLink> prepareComponentLinks() {
        //complexity seems like an upper bound for the resulting number of componentLinks. 
        //so use it as an initial size for the array list
        final List<TermLink> componentLinks = new ArrayList<>( getComplexity() );
        return Terms.prepareComponentLinks(componentLinks, this);
    }

    final public void addTermsTo(final Collection<Term> c) {
        Collections.addAll(c, term);
    }



    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public int compareTo(final AbstractTerm that) {
        if (that==this) { 
            return 0;
        }
        return super.compareTo(that);
    }
    
    @Override
    public boolean equals(final Object that) {
        if (that==this) return true;                
        if (!(that instanceof Term))
            return false;
        return name().equals(((Term)that).name());
    }   

    public void setNormalized(final boolean b) {
        this.normalized = b;
    }

    public boolean isNormalized() {
        return normalized;
    }
    
    public Term[] cloneTermsReplacing(final Term from, final Term to) {
        final Term[] y = new Term[term.length];
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

    
}
