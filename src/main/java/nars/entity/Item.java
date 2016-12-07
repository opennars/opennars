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
package nars.entity;

import nars.storage.Memory;

import java.util.Comparator;

/**
 * An item is an object that can be put into a Bag,
 * to participate in the resource competition of the system.
 * <p>
 * It has a key and a budget. Cannot be cloned
 */
public abstract class Item<K> {

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
    
    /** The budget of the Item, consisting of 3 numbers */
    public final BudgetValue budget;

    public Item() { //items that do not need budget
        this.budget = null; //Todo get rid of these console classes such as Reboot completely
    } //instead process them on parsing immediately!!
          
    /**
     * Constructor with initial budget
     * @param key The key value
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
        
        String budgetStr = budget!=null ? budget.toString() : "";
        String n = name().toString();
        return new StringBuilder(budgetStr.length()+n.length()+1).append(budgetStr).append(' ').append(n).toString();
    }

    /**
     * Return a String representation of the Item after simplification
     * @return A simplified String representation of the content
     */
    public String toStringExternal() {                
        final String briefBudget = budget.toStringExternal();
        String n = name().toString();
        return new StringBuilder(briefBudget.length()+n.length()+1).append(briefBudget).append(' ').append(n).toString();
    }
    
    /** similar to toStringExternal but includes budget afterward */
    public String toStringExternal2() {
        final String briefBudget = budget.toStringExternal();
        String n = name().toString();
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

    public static float getPrioritySum(Iterable<? extends Item> c) {
        float totalPriority = 0;
        for (Item i : c)
            totalPriority+=i.getPriority();
        return totalPriority;
    }
    
    /** randomly selects an item from a collection, weighted by priority */
    public static <E extends Item> E selectRandomByPriority(Iterable<E> c) {
        float totalPriority = getPrioritySum(c);
        
        if (totalPriority == 0) return null;
        
        float r = Memory.randomNumber.nextFloat() * totalPriority;
                
        E s = null;
        for (E i : c) {
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
