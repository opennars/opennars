/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import org.opennars.io.Symbols;
import org.opennars.language.Term;

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
 */
public class TermLink extends Item<TermLink> implements TLink<Term>, Serializable {
    
    
    /** At C, point to C; TaskLink only */
    public static final short SELF = 0;
    /** At (&&, A, C), point to C */
    public static final short COMPONENT = 1;
    /** At C, point to (&&, A, C) */
    public static final short COMPOUND = 2;
    /** At <C --> A>, point to C */
    public static final short COMPONENT_STATEMENT = 3;
    /** At C, point to <C --> A> */
    public static final short COMPOUND_STATEMENT = 4;
    /** At <(&&, C, B) ==> A>, point to C */
    public static final short COMPONENT_CONDITION = 5;
    /** At C, point to <(&&, C, B) ==> A> */
    public static final short COMPOUND_CONDITION = 6;
    /** At C, point to <(*, C, B) --> A>; TaskLink only */
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
            //index[0] = 0; //first index is zero, but not necessary to set since index[] was just created
            
            System.arraycopy(indices, 0, index, 1, indices.length);
            /* for (int i = 0; i < indices.length; i++)
                index[i + 1] = (short) indices[i]; */
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
                ? (short)(template.type - 1) //// point to component
                : template.type;
        index = template.index;
        hash = init();
    }

    @Override public TermLink name() { return this; }
    
//    @Override
//    public CharSequence name() {
//        if (key == null)
//            setKey();
//        return key;
//    }

    @Override
    public int hashCode() { return hash;     }

    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (hashCode()!=obj.hashCode()) return false;
        
        if (obj instanceof TermLink) {
            TermLink t = (TermLink)obj;
            
            if (type != t.type) return false;
            if (!Arrays.equals(t.index, index)) return false;
            
            final Term tt = t.target;
            if (target == null) {
                if (tt!=null) return false;
            }
            else if (tt == null) {
                if (target!=null) return false;
            }
            else if (!target.equals(t.target)) return false;
            
            return true;
        }
        return false;
    }

    /**
     * @return  hashcode
     */
    protected int init() {
        //TODO lazy calculate this?
        int h = Objects.hash(target, type, Arrays.hashCode(index));
        return h;
    }
    
    /*protected final void setKey() {
        setKey(null);        
    }*/

    
    /**
     * Set the key of the link
     * @param suffix optional suffix, may be null
     */    
    /*protected final void setKey(final CharSequence suffix) {
        this.key = Texts.yarn(Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD,
                        newKeyPrefix(), 
                        target!=null ? target.name() : null, 
                        suffix);        
    }*/
       
    
    
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
        int estimatedLength = 2+2+1+MAX_INDEX_DIGITS*( (index!=null ? index.length : 0) + 1);
        final StringBuilder prefix = new StringBuilder(estimatedLength);
        prefix.append(at1).append('T').append(type);
        if (index != null) {
            for (int i : index) {
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
