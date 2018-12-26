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

import org.opennars.io.Texts;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.opennars.io.Symbols.*;

/**
 * A variable term, which does not correspond to a concept
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Variable extends Term {
    /** caches the type character for faster lookup than charAt(0) */
    private char type = 0;
    
    private Term scope;

    private int hash;
    

    public Variable(final CharSequence name) {
        this(name, null);        
    }
    
    /**
     * Constructor, from a given variable name
     *
     * @param name A String read from input
     */
    protected Variable(final CharSequence name, final Term scope) {
        super();        
        setScope(scope, name);
    }

    public Variable setScope(final Term scope, final CharSequence n) {
        this.setName(n);
        this.type = n.charAt(0);
        this.scope = scope != null ? scope : this;
        this.hash = 0; //calculate lazily
        if (!validVariableType(type)) 
            throw new IllegalStateException("Invalid variable type: " + n);
        return this;
    }
    
    /**
     * Clone a Variable
     *
     * @return The cloned Variable
     */
    @Override
    public Variable clone() {
        final Variable v = new Variable(name(), scope);
        if (scope == this)
            v.scope = v;
        return v;
    }

    /**
     * Get the type of the variable
     *
     * @return The variable type
     */
    public char getType() {
        return type;
    }
    

    /**
     * A variable is not constant
     *
     * @return false
     */
    @Override
    public boolean isConstant() {
        return false;
    }

    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public short getComplexity() {
        return 0;
    }


    @Override public boolean hasVar() {
        return true;
    }
    @Override public boolean hasVarIndep() {
        return isIndependentVariable();
    }
    @Override public boolean hasVarDep() {
        return isDependentVariable();
    }
    @Override public boolean hasVarQuery() {
        return isQueryVariable();
    }
    

    @Override public boolean equals(final Object that) {
        if (that == this) {
            return true;
        }
        if (!(that instanceof Variable)) { 
            return false;
        }
        if(!super.equals(that)) {
            return false;
        }
        final Variable v = (Variable)that;
        if (!name().equals(v.name())) {
            return false;
        }
        if ((getScope() == this && v.getScope() != v) ||
            (getScope() != this && v.getScope() == v)) {
            return false;
        }
        return (v.getScope().name().equals(getScope().name()));
    }
    
    public boolean equalsTerm(final Object that) {
        //TODO factor these comparisons into 2 nested if's
        final Variable v = (Variable)that;
        if ((v.scope == v) && (scope == this))
            //both are unscoped, so compare by name only
            return name().equals(v.name());
        else if ((v.scope!=v) && (scope==this))
            return false;
        else if ((v.scope==v) && (scope!=this))
            return false;
        else {
            if (!name().equals(v.name()))
                return false;

            if (scope == v.scope) return true;

            if (scope.hashCode()!=v.scope.hashCode())
                return false;

            //WARNING infinnite loop can happen if the two scopes start equaling echother
            //we need a special equals comparison which ignores variable scope when recursively
            //called from this
            //until then, we'll use the name for comparison because it wont 
            //invoke infinite recursion

            return scope.name().equals(v.scope.name());       
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            if (scope!=this)
                this.hash = 31 * name().hashCode() + scope.hashCode();
            else
                this.hash = name().hashCode();
        }
        return hash;
    }

    @Override
    public int compareTo(final AbstractTerm that) {
        if(this == that) {
            return 0;
        }
        final int superCmp = super.compareTo(that);
        if(superCmp != 0) {
            return superCmp;
        }
        if(!(that instanceof Variable)) {
            return 0;
        }

        final Variable thatVar = (Variable) that;
        final int nameCmp = String.valueOf(this.name()).compareTo(String.valueOf(thatVar.name()));
        if( nameCmp != 0 ) {
            return nameCmp;
        }
        if(this.getScope() == this && thatVar.getScope() != thatVar) {
            return 1;
        }
        if(this.getScope() != this && thatVar.getScope() == thatVar) {
            return -1;
        }
        return String.valueOf(this.getScope().name()).compareTo(String.valueOf(thatVar.getScope().name()));
    }

    /*
     * variable terms are listed first alphabetically
     *
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    /*@Override
    public final int compareTo(final AbstractTerm that) {
        return (that instanceof Variable) ? ((Comparable)name()).compareTo(that.name()) : -1;
    }*/

    boolean isQueryVariable() { return getType() == VAR_QUERY;    }
    boolean isDependentVariable() { return getType() == VAR_DEPENDENT;    }
    boolean isIndependentVariable() { return getType() == VAR_INDEPENDENT;    }

    boolean isCommon() {     
        final CharSequence n = name();
        final int l = n.length();
        return n.charAt(l - 1) == '$';
    }

    public Term getScope() {
        return scope;
    }
    
    //ported back from 1.7, sehs addition
    public static int compare(final Variable a, final Variable b) {
        //int i = a.name().compareTo(b.name());
        final int i=Texts.compareTo(a.name(), b.name());
        if (i == 0) {
            final boolean ascoped = a.scope!=a;
            final boolean bscoped = b.scope!=b;
            if (!ascoped && !bscoped) {
                //if the two variables are each without scope, they are not equal.
                //so use their identityHashCode to determine a stable ordering
                final int as = System.identityHashCode(a.scope);
                final int bs = System.identityHashCode(b.scope);
                return Integer.compare(as, bs);
            }
            else if (ascoped && !bscoped) {
                return -1;
            }
            else if (bscoped && !ascoped) {
             return 1;
            }
            else {
                return Texts.compareTo(a.getScope().name(), b.getScope().name());
               // return Texts.compare(a.getScope().name(), b.getScope().name());
           }
        }
        return i;
    }


    public static boolean validVariableType(final char c) {
        return (c == VAR_QUERY) || (c == VAR_DEPENDENT) || (c == VAR_INDEPENDENT);
    }
    
    private static final int MAX_CACHED_VARNAME_INDEXES = 64;
    private static final CharSequence[] vn1 = new CharSequence[MAX_CACHED_VARNAME_INDEXES];
    private static final CharSequence[] vn2 = new CharSequence[MAX_CACHED_VARNAME_INDEXES];
    private static final CharSequence[] vn3 = new CharSequence[MAX_CACHED_VARNAME_INDEXES];
    
    
    public static CharSequence getName(final char type, final int index) {
        if (index > MAX_CACHED_VARNAME_INDEXES)
            return newName(type, index);
        
        
        final CharSequence[] cache;
        switch (type) {
            case VAR_INDEPENDENT: cache = vn1; break;
            case VAR_DEPENDENT: cache = vn2; break;
            case VAR_QUERY: cache = vn3; break;
            default:
                throw new IllegalStateException("Invalid variable type");
        }
        
        CharSequence c = cache[index];
        if (c == null) {
            c = newName(type, index);
            cache[index] = c;
        }
            
        return c;
    }
    
    protected static CharSequence newName(final char type, int index) {
        
        final int digits = (index >= 256 ? 3 : ((index >= 16) ? 2 : 1));
        final CharBuffer cb  = CharBuffer.allocate(1 + digits).append(type);
        do {
            cb.append(  Character.forDigit(index % 16, 16) ); index /= 16;
        } while (index != 0);
        return cb.compact().toString();
    }
    
    @Override
    public Map<Term, Integer> countTermRecursively(Map<Term,Integer> map) { 
        if(map == null) {
            map = new HashMap<Term, Integer>();
        }
        return map; //don't count vars
    }
}
