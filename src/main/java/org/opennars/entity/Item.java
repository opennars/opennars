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

import org.opennars.storage.Memory;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An item is an object that can be put into a Bag,
 * to participate in the resource competition of the system.
 * <p>
 * It has a key and a budget. Cannot be cloned
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public abstract class Item<K> implements Serializable {

    public static class ItemPriorityComparator<E extends Item> implements Comparator<E> {

        @Override public int compare(final E a, final E b) {
            final float ap = a.getPriority();
            final float bp = b.getPriority();

            if ((a == b) || (a.name().equals(b.name())) || (ap==bp))
                return a.hashCode() - b.hashCode();
            else if (ap < bp) return 1;
            else return -1;
        }        
        
    }
    
    /** The budget of the Item, consisting of 3 numbers */
    public final BudgetValue budget;

    public Item() { // items that do not need budget
        this.budget = null;
    }
          
    /**
     * Constructor with initial budget
     * @param budget The initial budget
     */
    protected Item(final BudgetValue budget) {
        if (budget!=null)
            this.budget = budget.clone(); // clone, not assignment
        else
            this.budget = null;
    }


    /**
     * Get the current key
     * @return Current key value
     */
    abstract public K name();


    /**
     * Get priority value
     * @return Current priority value
     */
     public float getPriority() {
        return budget.getPriority();
    }

    /**
     * Set priority value
     * @param v Set a new priority value
     */
    public void setPriority(final float v) {
        budget.setPriority(v);
    }

    /**
     * Increase priority value
     * @param v The amount of increase
     */
    public void incPriority(final float v) {
        budget.incPriority(v);
    }

    /**
     * Decrease priority value
     * @param v The amount of decrease
     */
    public void decPriority(final float v) {
        budget.decPriority(v);
    }

    /**
     * Get durability value
     * @return Current durability value
     */
    public float getDurability() {
        return budget.getDurability();
    }

    /**
     * Set durability value
     * @param v The new durability value
     */
    public void setDurability(final float v) {
        budget.setDurability(v);
    }

    /**
     * Increase durability value
     * @param v The amount of increase
     */
    public void incDurability(final float v) {
        budget.incDurability(v);
    }

    /**
     * Decrease durability value
     * @param v The amount of decrease
     */
    public void decDurability(final float v) {
        budget.decDurability(v);
    }
    
    

    /** called when the item has been discarded */
    public void end() {
        
    }
    
    /**
     * Get quality value
     * @return The quality value
     */
    public float getQuality() {
        return budget.getQuality();
    }

    /**
     * Set quality value
     * @param v The new quality value
     */
    public void setQuality(final float v) {
        budget.setQuality(v);
    }

    /**
     * Merge with another Item with identical key
     * @param that The Item to be merged
     * @return the resulting Item: this or that
     */
    public Item merge(final Item that) {
        budget.merge(that.budget);
        return this;
    }

    /**
     * Return a String representation of the Item
     * @return The String representation of the full content
     */
    @Override
    public String toString() {        
        //return budget + " " + key ;
        
        final String budgetStr = budget!=null ? budget.toString() : "";
        final String n = name().toString();
        return new StringBuilder(budgetStr.length()+n.length()+1).append(budgetStr).append(' ').append(n).toString();
    }

    /**
     * Return a String representation of the Item after simplification
     * @return A simplified String representation of the content
     */
    public String toStringExternal() {                
        final String briefBudget = budget.toStringExternal();
        final String n = name().toString();
        return new StringBuilder(briefBudget.length()+n.length()+1).append(briefBudget).append(' ').append(n).toString();
    }
    
    /** similar to toStringExternal but includes budget afterward */
    public String toStringExternal2() {
        final String briefBudget = budget.toStringExternal();
        final String n = name().toString();
        return new StringBuilder(briefBudget.length()+n.length()+1).append(n).append(' ').append(briefBudget).toString();
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
        
        public StringKeyItem(final BudgetValue budget) { super(budget);         }

                
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
    
    }

    public static float getPrioritySum(final Iterable<? extends Item> c) {
        float totalPriority = 0;
        for (final Item i : c)
            totalPriority+=i.getPriority();
        return totalPriority;
    }
    
    /** randomly selects an item from a collection, weighted by priority */
    public static <E extends Item> E selectRandomByPriority(final Iterable<E> c) {
        final float totalPriority = getPrioritySum(c);
        
        if (totalPriority == 0) return null;
        
        float r = Memory.randomNumber.nextFloat() * totalPriority;
                
        E s = null;
        for (final E i : c) {
            s = i;
            r -= s.getPriority();
            if (r < 0)
                return s;
        }
        
        return s;
        
    }

    public BudgetValue getBudget() {
        return budget;
    }
    
    
}
