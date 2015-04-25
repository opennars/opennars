/*
 * TermLink.java
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
package nars.nal.tlink;

import nars.budget.Budget;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.term.Termed;
import nars.nal.Item;
import nars.nal.term.Term;

/**
 * A tlink between a compound term and a component term
 * <p>
 * A TermLink links the current Term to a target Term, which is 
 * either a component of, or compound made from, the current term.
 * <p>
 * Neither of the two terms contain variable shared with other terms.
 * <p>
 * The index value(s) indicates the location of the component in the compound.
 * <p>
 * This class is mainly used in logic.RuleTable to dispatch premises to logic rules
 */
public class TermLink extends Item<TermLinkKey> implements TLink<Term>, Termed, TermLinkKey {



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
    
    
    /** The linked Term */
    public final Term target;
    
    /** The type of tlink, one of the above */
    public final short type;
    
    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;

    private final String prefix;

    transient final int hash;
    private final Term source;


    /**
     * Constructor to make actual TermLink from a template
     * <p>
     * called in Concept.buildTermLinks only
     * @param
     * @param incoming or outgoing
     * @param template TermLink template previously prepared
     * @param v Budget value of the tlink
     */
    public TermLink(boolean incoming, Term host, TermLinkTemplate template, String name, Budget v) {
        super(v);

        host.ensureNormalized("Host term in TermLink");
        template.target.ensureNormalized("Template target in " + template);

        if (incoming) {
            this.source = template.target;
            this.target = host;
            type = template.type;
        }
        else {
            this.source = host;
            this.target = template.target;
            type = (short)(template.type - 1); //// point to component
        }

        index = template.index;

        this.prefix = name;

        this.hash = termLinkHashCode();
    }



    public boolean toSelfOrTransform() {
        return ((type == SELF) || (type == TRANSFORM));
    }
    public boolean toSuperTerm() {
        if (toSelfOrTransform()) return false;
        return type % 2 == 1;
    }
    public boolean toSubTerm() {
        if (toSelfOrTransform()) return false;
        return type % 2 == 0;
    }

    @Override
    public TermLink name() {
        return this;
    }

    @Override
    public int hashCode() {
        return hash;
    }


    @Override
    public boolean equals(final Object obj) {
        return termLinkEquals(obj);
    }

    
    @Override
    public String toString() {
        //return new StringBuilder().append(newKeyPrefix()).append(target!=null ? target.name() : "").toString();
        return getPrefix() + Symbols.TLinkSeparator + getTarget();
    }

    public String getPrefix() { return prefix; }

    /** the original prefix code, verbose. see TermLinkTemplate.prefix(..) */
    @Deprecated protected CharSequence newPrefix() {

        final String at1, at2;
        if ((type % 2) == 1) {  // to component
            at1 = Symbols.TO_COMPONENT_1;
            at2 = Symbols.TO_COMPONENT_2;
        } else {                // to compound
            at1 = Symbols.TO_COMPOUND_1;
            at2 = Symbols.TO_COMPOUND_2;
        }

        final int MAX_INDEX_DIGITS = 2;

        final CharSequence targetName = target.name();

        final int estimatedLength = 2+2+1+MAX_INDEX_DIGITS*( (index!=null ? index.length : 0) + 1);// + targetName.length();

        final StringBuilder n = new StringBuilder(estimatedLength);
        n.append(at1).append('T').append(type);
        if (index != null) {
            for (final short i : index) {
                //prefix.append('-').append( Integer.toString(i + 1, 16 /** hexadecimal */)  );

                n.append('-');

                final char ii = (char)(i + 1);
                if (ii < 10)
                    n.append((char)(ii+'0') );
                else
                    n.append( Texts.n2(ii) );
            }

        }
        return n.append(at2);//.append(targetName);
    }

    /**
     * Get one index by level
     * @param i The index level
     * @return The index value
     */
    @Override
    public final short getIndex(final int i) {
        if ((i < 0) || ( i >= index.length))
            throw new RuntimeException(this + " index fault: " + i);
        //if (/*(index != null) &&*/ (i < index.length)) {
            return index[i];
        //} else {
            //return -1;
        //}
    }

    @Override public void end() {
        
    }

    @Override
    public Term getTarget() {
        return getTerm();
    }

    @Override
    public Term getSource() {
        return source;
    }


    @Override
    public Term getTerm() {
        target.ensureNormalized("Template target");
        return target;
    }

}
