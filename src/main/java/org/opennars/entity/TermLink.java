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
package org.opennars.entity;

import org.opennars.io.Symbols;
import org.opennars.language.Term;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A link between a compound term and a component term
 * <p>
 * A TermLink links the current Term to a target Term, which is 
 * either a component of, or compound made from, the current term.
 * <p>
 * Neither of the two terms contain variable shared with other terms.
 * <p>
 * The index value(s) indicates the location of the component in the compound.
 * <p>
 * This class is mainly used in inference.RuleTable to dispatch premises to inference rules
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class TermLink extends Item<TermLink> implements TLink<Term>, Serializable {
    
    
    /** At C, point to C; TaskLink only */
    public static final short SELF = 0;
    /** At (&amp;&amp;, A, C), point to C */
    public static final short COMPONENT = 1;
    /** At C, point to (&amp;&amp;, A, C) */
    public static final short COMPOUND = 2;
    /** At &lt;C --&gt; A&gt;, point to C */
    public static final short COMPONENT_STATEMENT = 3;
    /** At C, point to &lt;C --&gt; A&gt; */
    public static final short COMPOUND_STATEMENT = 4;
    /** At &lt;(&amp;&amp;, C, B) ==&gt; A&gt;, point to C */
    public static final short COMPONENT_CONDITION = 5;
    /** At C, point to &lt;(&amp;&amp;, C, B) ==&gt; A&gt; */
    public static final short COMPOUND_CONDITION = 6;
    /** At C, point to &lt;(*, C, B) --&gt; A&gt;; TaskLink only */
    public static final short TRANSFORM = 8;
    /** At C, point to B, potentially without common subterm term */
    public static final short TEMPORAL = 9;
    
    
    /** The linked Term */
    public final Term target;
    
    /** The type of link, one of the above */    
    public final short type;
    
    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;

    protected final int hash;
   

            
    /**
     * Constructor for TermLink template
     * <p>
     * called in CompoundTerm.prepareComponentLinks only
     * @param target Target Term
     * @param type Link type
     * @param indices Component indices in compound, may be 1 to 4
     */
    public TermLink(final Term target, final short type, final short... indices) {
        super(null);
        this.target = target;
        this.type = type;
        assert (type % 2 == 0); // template types all point to compound, though the target is component
        if (type == TermLink.COMPOUND_CONDITION) {  // the first index is 0 by default
            
            index = new short[indices.length + 1];
            index[0] = 0;
            
            System.arraycopy(indices, 0, index, 1, indices.length);
        } else {
            index = indices;            
        }
        hash = init();
    }



    /**
     * Constructor to make actual TermLink from a template
     * <p>
     * called in Concept.buildTermLinks only
     * @param t Target Term
     * @param template TermLink template previously prepared
     * @param v Budget value of the link
     */
    public TermLink(final Term t, final TermLink template, final BudgetValue v) {
        super(v);
        target = t;
        type = (template.target.equals(t)) 
                ? (short)(template.type - 1) // point to component
                : template.type;
        index = template.index;
        hash = init();
    }

    @Override public TermLink name() { return this; }
    
    @Override
    public int hashCode() { return hash;     }

    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (hashCode()!=obj.hashCode()) return false;
        
        if (obj instanceof TermLink) {
            final TermLink t = (TermLink)obj;
            
            if (type != t.type) return false;
            if (!Arrays.equals(t.index, index)) return false;
            
            final Term tt = t.target;
            if (target == null) {
                return tt == null;
            }
            else if (tt == null) {
                return target == null;
            }
            else return target.equals(t.target);

        }
        return false;
    }

    /**
     * @return  hashcode
     */
    protected int init() {
        //TODO lazy calculate this?
        final int h = Objects.hash(target, type, Arrays.hashCode(index));
        return h;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append(newKeyPrefix()).append(target!=null ? target.name() : "").toString();
    }

    public CharSequence newKeyPrefix() {
        final String at1, at2;
        if ((type % 2) == 1) {  // to component
            at1 = Symbols.TO_COMPONENT_1;
            at2 = Symbols.TO_COMPONENT_2;
        } else {                // to compound
            at1 = Symbols.TO_COMPOUND_1;
            at2 = Symbols.TO_COMPOUND_2;
        }
        final int MAX_INDEX_DIGITS = 2;
        final int estimatedLength = 2+2+1+MAX_INDEX_DIGITS*( (index!=null ? index.length : 0) + 1);
        final StringBuilder prefix = new StringBuilder(estimatedLength);
        prefix.append(at1).append('T').append(type);
        if (index != null) {
            for (final int i : index) {
                prefix.append('-').append( Integer.toString(i + 1, 16 /** hexadecimal */)  );
            }
        }
        prefix.append(at2);
        return prefix;
    }
    
    /**
     * Get one index by level
     * @param i The index level
     * @return The index value
     */
    public final short getIndex(final int i) {
        if ((index != null) && (i < index.length)) {
            return index[i];
        } else {
            return -1;
        }
    }

    public TermLink(final short type, final Term target, final int i0) {
        this(target, type, (short)i0);
    }
    
    public TermLink(final short type, final Term target, final int i0, final int i1) {
        this(target, type, (short)i0, (short)i1);
    }
    
    public TermLink(final short type, final Term target, final int i0, final int i1, final int i2) {
        this(target, type, (short)i0, (short)i1, (short)i2);
    }

    public TermLink(final short type, final Term target, final int i0, final int i1, final int i2, final int i3) {
        this(target, type, (short)i0, (short)i1, (short)i2, (short)i3);
    }

    @Override public void end() {
        
    }

    @Override
    public Term getTarget() {
        return target;
    }

    public Term getTerm() {
        return getTarget();
    }

    
}
