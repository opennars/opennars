/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;


import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Long class represents the long prolog data type
 *
 *
 *
 */
@SuppressWarnings("serial")
public class Long extends PNum {
    
   private final long value;
    
    public Long(long v) {
        value = v;
    }
    
    /**
     *  Returns the value of the Integer as int
     *
     */
    final public int intValue() {
        return (int) value;
    }
    
    /**
     *  Returns the value of the Integer as float
     *
     */
    final public float floatValue() {
        return (float) value;
    }
    
    /**
     *  Returns the value of the Integer as double
     *
     */
    final public double doubleValue() {
        return (double) value;
    }
    
    /**
     *  Returns the value of the Integer as long
     *
     */
    final public long longValue() {
        return value;
    }
    
    
    /** is this term a prolog integer term? */
    final public boolean isInteger() {
        return true;
    }
    
    /** is this term a prolog real term? */
    final public boolean isReal() {
        return false;
    }
    
    /** is an int Integer number? 
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isTypeInt() {
        return false;
    }

    /** is an int Integer number?
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isInt() {
        return false;
    }
    
    /** is a float Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isTypeFloat() {
        return false;
    }

    /** is a float Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isFloat() {
        return false;
    }
    
    /** is a double Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isTypeDouble() {
        return false;
    }

    /** is a double Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isDouble() {
        return false;
    }
    
    /** is a long Integer number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isTypeLong() {
        return true;
    }

    /** is a long Integer number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isLong() {
        return true;
    }
    
    /**
     * Returns true if this integer term is grater that the term provided.
     * For number term argument, the int value is considered.
     */
    public boolean isGreater(Term t) {
        t = t.getTerm();
        if (t instanceof PNum) {
            return value > ( (PNum) t ).longValue();
        } else if (t instanceof Struct) {
            return false;
        } else return t instanceof Var;
    }
    
    public boolean isGreaterRelink(Term t, ArrayList<String> vorder) {
        t = t.getTerm();
        if (t instanceof PNum) {
            return value > ( (PNum) t ).longValue();
        } else if (t instanceof Struct) {
            return false;
        } else return t instanceof Var;
    }
    
    /**
     * Returns true if this integer term is equal that the term provided.
     * For number term argument, the int value is considered.
     */
    public boolean isEqual(Term t) {
        t = t.getTerm();
        if (t instanceof PNum) {
            return value == ( (PNum) t ).longValue();
        } else {
            return false;
        }
    }
    
    /**
     * Tries to unify a term with the provided term argument.
     * This service is to be used in demonstration context.
     */
    public boolean unify(List<Var> vl1, List<Var> vl2, Term t) {
        t = t.getTerm();
        if (t instanceof Var) {
            return ((Var)t).unify(vl1, vl2, this);
        } else if (t instanceof PNum && ((PNum) t).isInteger()) {
            return value == ((PNum) t).longValue();
        } else {
            return false;
        }
    }

    @Override
    public PTerm clone() {
        return null;
    }

    public String toString() {
        return java.lang.Long.toString(value);
    }


    @Override
    public int hashCode() {
        return (int) value;
    }

}