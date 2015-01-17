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
package nars.logic.entity;

import nars.core.Parameters;
import nars.io.Symbols;
import nars.logic.Terms.Termable;
import nars.util.bag.BagSelector;

import java.util.List;

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
 * This class is mainly used in logic.RuleTable to dispatch premises to logic rules
 */
public class TermLink extends Item<String> implements TLink<Term>, Termable {
    
    
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
    
    /** The type of link, one of the above */    
    public final short type;
    
    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;

    private final String name;


    /**
     * Constructor to make actual TermLink from a template
     * <p>
     * called in Concept.buildTermLinks only
     * @param
     * @param incoming or outgoing
     * @param template TermLink template previously prepared
     * @param v Budget value of the link
     */
    public TermLink(boolean incoming, Term host, TermLinkTemplate template, String name, BudgetValue v) {
        super(v);

        if (incoming) {
            this.target = host;
            type = template.type;
        }
        else {
            this.target = template.target;
            type = (short)(template.type - 1); //// point to component
        }
        index = template.index;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;

        if (obj instanceof TermLink) {

            TermLink t = (TermLink)obj;
            return t.name().equals(name());
//
//            short[] ti = t.index;
//            final int il = index.length;
//
//            //compare index length
//            if (il!=ti.length) return false;
//
//            //compare type
//            if (type != t.type) return false;
//
//            //compare array content
//            for (int i=0; i<il; i++)
//                if (index[i] != ti[i])
//                    return false;
//
//            //compare target nullity
//            final Term tt = t.target;
//            if (target == null) {
//                if (tt!=null) return false;
//            }
//            else if (tt == null)
//                return false;
//
//            //compare target
//            return (target.equals(t.target));
        }
        return false;
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
        int estimatedLength = 2+2+1+MAX_INDEX_DIGITS*( (index!=null ? index.length : 0) + 1);
        final StringBuilder prefix = new StringBuilder(estimatedLength);
        prefix.append(at1).append('T').append(type);
        if (index != null) {
            for (short i : index) {
                //prefix.append('-').append( Integer.toString(i + 1, 16 /** hexadecimal */)  );
                prefix.append('-').append( Integer.toString(i)  );
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
        return target;
    }


    @Override
    public Term getTerm() {
        return getTarget();
    }



    public static class TermLinkBuilder implements BagSelector<String,TermLink> {

        public final Concept concept;

        List<TermLinkTemplate> template;
        int nonTransforms;

        private final CompoundTerm host;

        private Term from = null;
        private Term to = null;
        //private Term other = null;

        private TermLinkTemplate currentTemplate;
        private boolean incoming;
        private BudgetValue budget = new BudgetValue(0,0,0);

        public TermLinkBuilder(Concept c) {
            this.concept = c;

            host = (CompoundTerm)c.getTerm();

            int complexity = host.getComplexity();

            template = Parameters.newArrayList(complexity + 1);
            nonTransforms = 0;

            host.prepareComponentLinks(this);
        }

        /** count how many termlinks are non-transform */
        public List<TermLinkTemplate> templates() {
            return template;
        }


        public void addTemplate(TermLinkTemplate tl) {
            template.add(tl);

            tl.setConcept(host);

            if (tl.type!= TRANSFORM)
                nonTransforms++;
        }


        @Override public BudgetValue getBudget() {
            return budget;
        }

        public String name(Term from) {
            return currentTemplate.name( from!= concept.term );
        }

        /** configures this selector's current budget for the next bag operation */
        public BudgetValue set(float subBudget, float durability, float quality) {
            budget.setPriority(subBudget);
            budget.setDurability(durability);
            budget.setQuality(quality);
            return budget;
        }

        /** configures this selector's current bag key for the next bag operation */
        public TermLinkBuilder set(TermLinkTemplate temp, Term source, Term target) {
            if ((this.from == source) && (this.to == target)) return this;
            this.currentTemplate = temp;
            this.incoming = !source.equals(concept.term);
            //this.other = incoming ? source : target;
            this.from = source;
            this.to = target;
            return this;
        }

        /**
         *
         * @return the amount of remaining budget priority
         */
        /*double invest(Bag<TermLink,String> bag) {
            //TODO move code from Concept here
            //iterate all items, both forward and backward
            return 0;
        }*/

        @Override public String name() {
            return name(this.from);
        }


        @Override
        public TermLink newInstance() {
            return new TermLink(incoming, concept.getTerm(), currentTemplate, name(), getBudget());
        }

        public int size() {
            return template.size();
        }

        public void clear() {
            template.clear();
            nonTransforms = 0;
        }

        /** count of how many templates are non-transforms */
        public int getNonTransforms() {
            return nonTransforms;
        }
    }
}
