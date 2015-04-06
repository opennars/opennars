/*
 * Term.java
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

import nars.Memory;
import nars.Global;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.NALOperator;
import nars.nal.Terms.Termable;
import nars.nal.Named;
import nars.nal.Statement;
import nars.nal.nal7.TemporalRules;
import nars.util.data.sorted.SortedList;

import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;

/**
 * Term is the basic component of Narsese, and the object of processing in NARS.
 * <p>
 * A Term may have an associated Concept containing relations with other Terms.
 * It is not linked in the Term, because a Concept may be forgot while the Term
 * exists. Multiple objects may represent the same Term.
 */
public class Term implements AbstractTerm, Termable, Named<CharSequence> {

    private static final Map<CharSequence,Term> atoms = Global.newHashMap(8192);


    public NALOperator operator() {
        return NALOperator.ATOM;
    }
    
    public boolean isExecutable(final Memory mem) {
        return false;
    }

    public boolean isNormalized() {
        return true;
    }

    public void ensureNormalized(String role) {
        if (hasVar() && !isNormalized()) {
            System.err.println(this + " is not normalized but as " + role + " should have already been");
            System.exit(1);
        }
    }


    public interface TermVisitor {
        public void visit(Term t, Term superterm);
    }
    
    
    
    protected CharSequence name = null;
    
    /**
     * Default constructor that build an internal Term
     */
    protected Term() {
    }

    /**
     * Constructor with a given name
     *
     * @param name A String as the name of the Term
     */
    public Term(final CharSequence name) {
        setName(name);
    }
    
    /** gets the atomic term given a name */
    public final static Term get(final CharSequence name) {
        Term x = atoms.get(name);
        if (x != null) return x;
        x = new Term(name);
        atoms.put(name, x);
        return x;
    }

    public final static Term get(Object o) {
        if (o instanceof Term) return (Term)o;
        if (o instanceof CharSequence) {
            return get((CharSequence) o);
        }
        return null;
    }

    /** gets the atomic term of an integer */
    public final static Term get(final int i) {
        //fast lookup for single digits
        switch (i) {
            case 0: return get("0");
            case 1: return get("1");
            case 2: return get("2");
            case 3: return get("3");
            case 4: return get("4");
            case 5: return get("5");
            case 6: return get("6");
            case 7: return get("7");
            case 8: return get("8");
            case 9: return get("9");
        }
        return get(Integer.toString(i));
    }
    

    /**
     * Reporting the name of the current Term.
     *
     * @return The name of the term as a String
     */
    @Override
    public CharSequence name() {
        return name;
    }

    /**
     * Make a new Term with the same name.
     *
     * @return The new Term
     */
    @Override
    public Term clone() {
        //avoids setName and its intern(); the string will already be intern:
        Term t = new Term();
        t.name = name();
        return t;
    }

    /** attempts to return cloneNormalize result,
     * if it's necessary and possible.
     *  does not modify this term
     * */
    public Term normalized() {
        return this;
    }

    public Term cloneDeep() {
        return clone();
    }

    /**
     * Equal terms have identical name, though not necessarily the same
     * reference.
     *
     * @return Whether the two Terms are equal
     * @param that The Term to be compared with the current Term
     */
    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (!(that instanceof Term)) return false;
        final Term t = (Term)that;
        if ((name == null) || (t.name == null)) {
            //check operate first because name() may to avoid potential construction of name()
            if (operator()!=t.operator() || getComplexity() != t.getComplexity() )
                return false;
        }
        return name().equals(t.name());
    }

    /**
     * Produce a hash code for the term
     *
     * @return An integer hash code
     */
    @Override
    public int hashCode() {
        return name().hashCode();
    }

    /**
     * Alias for 'isNormalized'
     * @return A Term is constant by default
     */
    public boolean isConstant() {
        return true;
    }

    public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }   

    public void recurseTerms(final TermVisitor v, Term parent) {
        v.visit(this, parent);
        if (this instanceof Compound) {
            for (Term t : ((Compound)this).term) {
                t.recurseTerms(v, this);
            }
        }
    }
    public void recurseTerms(final TermVisitor v) {
        recurseTerms(v, null);
    }
    
    public void recurseSubtermsContainingVariables(final TermVisitor v) {
        recurseSubtermsContainingVariables(v, null);
    }
    
    public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
        if (!hasVar()) return;
        v.visit(this, parent);
        if (this instanceof Compound) {
            for (Term t : ((Compound)this).term) {
                t.recurseSubtermsContainingVariables(v, this);
            }
        }
    }
    
         
    /**
     * The syntactic complexity, for constant atomic Term, is 1.
     *
     * @return The complexity of the term, an integer
     */
    public short getComplexity() {
        return 1;
    }

    /** only method that should modify Term.name. also caches hashcode 
     * @return whether the name was changed
     */
    protected void setName(final CharSequence newName) {
        this.name = newName;
//        if (this.name!=null) {
//            if (this.name.equals(newName)) {
//                //name is the same
//                return false;
//            }
//        }
//        
//        if (newName == null)
//            return this.name != null;
//        
//        if ((newName.getClass() == String.class) && (newName.length() <= Parameters.INTERNED_TERM_NAME_MAXLEN)) {
//            
//            this.name = ((String)newName).intern();
//        }
//        else {
//            this.name = newName;
//        }
//        return true;
    }
    
    /**
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public int compareTo(final AbstractTerm that) {
        if (that==this) return 0;

        //previously: Orders among terms: variable < atomic < compound
        if (that instanceof Variable) {
            if (getClass()!=Variable.class) return 1;
            return Variable.compare((Variable)this, (Variable)that);
        }
        else if (this instanceof Variable)
            return -1;
        return Texts.compare(name(), that.name());
    }

    
    
    public int containedTemporalRelations() {
        return 0;
    }
    
    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the two have the same content
     */
    public boolean containsTermRecursivelyOrEquals(final Term target) {
        return equals(target);
    }

    /** whether this contains a term in its components. */
    public boolean containsTerm(final Term target) {
        return equals(target);
    }


    /**
     * The same as getName by default, used in display only.
     *
     * @return The name of the term as a String
     */
    @Override
    public final String toString() {
        return name().toString();
    }

    /** Creates a quote-escaped term from a string. Useful for an atomic term that is meant to contain a message as its name */
    public static Term quoted(String t) {
        return Term.get('"' + t + '"');
    }


    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    public boolean hasVar() {
        return false;
    }
    
    public boolean hasVar(final char type) {
        switch (type) {
            case Symbols.VAR_DEPENDENT: return hasVarDep();
            case Symbols.VAR_INDEPENDENT: return hasVarIndep();
            case Symbols.VAR_QUERY: return hasVarQuery();
        }
        throw new RuntimeException("Invalid variable type: " + type);
    }
    
    public boolean hasVarIndep() {
        return false;
    }

    public boolean hasVarDep() {
        return false;
    }

    public boolean hasVarQuery() {
        return false;
    }

    public static TreeSet<Term> toSortedSet(final Term... arg) {
        //use toSortedSetArray where possible
        TreeSet<Term> t = new TreeSet();
        Collections.addAll(t, arg);
        return t;        
    }
    
    public final static Term[] EmptyTermArray = new Term[0];
    
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
                        throw new RuntimeException("invalid order: " + a + " = " + b);
                    }
                }

                if (c < 0) return new Term[] { a, b };
                else if (c > 0) return new Term[] { b, a };
                else if (c == 0) return new Term[] { a }; //equal
                
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

    /** performs a thorough check of the validity of a term (by cloneDeep it) to see if it's valid */
    public static boolean valid(Term content) {
        
        try {
            Term cloned = content.cloneDeep();
            return cloned!=null;
        }
        catch (Throwable e) {
            if (Global.DEBUG && Global.DEBUG_INVALID_SENTENCES) {
                System.err.println("INVALID TERM: " + content);
                e.printStackTrace();
            }
            return false;
        }
        
    }

    public boolean subjectOrPredicateIsIndependentVar() {
        if (!(this instanceof Statement))
            return false;

        Statement cont=(Statement)this;
        if (!cont.hasVarIndep()) return false;

        Term subj = cont.getSubject();
        if ((subj instanceof Variable) && (subj.hasVarIndep()))
            return true;
        Term pred = cont.getPredicate();
        if ((pred instanceof Variable) && (pred.hasVarIndep()))
            return true;

        return false;
    }
    
    @Override public Term getTerm() { return this; }

}
