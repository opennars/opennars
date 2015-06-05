/*
 * Item.java
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
package nars.nal;

import nars.Memory;
import nars.budget.Budget;

import java.util.Comparator;

/**
 * An item is an object that can be put into a Bag,
 * to participate in the resource competition of the system.
 * <p>
 * It has a key and a budget. Cannot be cloned
 */
public abstract class Item<K> extends Budget implements Itemized<K> {



    public static class ItemPriorityComparator<E extends Item> implements Comparator<E> {

        @Override public int compare(final E a, final E b) {
            float ap = a.getPriority();
            float bp = b.getPriority();

            if ((a == b) || (a.name().equals(b.name())) || (ap==bp))
                return a.hashCode() - b.hashCode();
            else if (ap < bp) return 1;
            else return -1;
        }        
        
    }

    public static final Comparator<Item> priorityComparator = new ItemPriorityComparator<>();


    protected Item( ) {
        this(null);
    }

    /**
     * Constructor with initial budget
     * @param budget The initial budget
     */
    protected Item( final Budget budget) {
        super(budget);
    }

    /** sets the intial budget values */
    protected Item( float p, float d, float q ) {
        super(p, d, q);
    }


    /**
     * Get the current key
     * @return Current key value
     */
    abstract public K name();


    /** called when the item has been permanently discarded.
     *  attempts to completely erase the item and all its contained
     *  data structures.
     *  helps garbage collection as much as possible.  */
    public void delete() {
        
    }

    /**
     * Return a String representation of the Item
     * @return The String representation of the full content
     */
    @Override
    public String toString() {        
        //return budget + " " + key ;
        
        String budgetStr = this !=null ? super.toString() : "";
        String n = name().toString();
        return new StringBuilder(budgetStr.length()+n.length()+1).append(budgetStr).append(' ').append(n).toString();
    }

    /**
     * Return a String representation of the Item after simplification
     * @return A simplified String representation of the content
     */
    public String toStringWithBudget() {
        StringBuilder sb = new StringBuilder();
        appendWithBudget(sb);
        return sb.toString();
    }

    public String toStringSentence() {
        return name().toString();
    }

    public void appendWithBudget(StringBuilder sb) {
        final String briefBudget = super.toStringExternal();
        final String n = name().toString();
        sb.ensureCapacity(briefBudget.length()+n.length()+1);
        sb.append(briefBudget).append(' ').append(n);
    }

    /** similar to toStringExternal but includes budget afterward */
    public String toStringExternal2(boolean includeBudget) {
        final String briefBudget = super.toStringExternal();
        String n = name().toString();
        StringBuilder sb = new StringBuilder( (includeBudget ? briefBudget.length() : 0) +n.length()+1).append(n);

        if (includeBudget)
            sb.append(' ').append(briefBudget);

        return sb.toString();
    }
    
    public String toStringLong() {
    	return toString();
    }

    /*//default:
    @Override
    public int compareTo(final Object o) {
        //return System.identityHashCode(this) - System.identityHashCode(o);
        return hashCode() - o.hashCode();
    }*/
    
   @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof Item) {
            return ((Item)obj).name().equals(name());
        }
        return false;
    }
    
    abstract public static class StringKeyItem extends Item<CharSequence> {
        
        public StringKeyItem(final Budget budget) { super(budget);         }


        @Override
        public boolean equals(final Object obj) {
            if (obj == this) return true;
            if (obj instanceof Item) {
                return ((Item)obj).name().equals(name());
            }
            return false;
        }
    
    }

    public static float getPrioritySum(Iterable<? extends Item> c) {
        float totalPriority = 0;
        for (Item i : c)
            totalPriority+=i.getPriority();
        return totalPriority;
    }
    
    /** randomly selects an item from a collection, weighted by priority */
    public static <E extends Item> E selectRandomByPriority(Memory memory, Iterable<E> c) {
        float totalPriority = getPrioritySum(c);
        
        if (totalPriority == 0) return null;
        
        float r = memory.random.nextFloat() * totalPriority;
                
        E s = null;
        for (E i : c) {
            s = i;
            r -= s.getPriority();
            if (r < 0)
                return s;
        }
        
        return s;
        
    }

    @Override
    public Budget getBudget() {
        return this;
    }
    
    
}
