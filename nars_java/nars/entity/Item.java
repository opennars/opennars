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

/**
 * An item is an object that can be put into a Bag,
 * to participate in the resource competition of the system.
 * <p>
 * It has a key and a budget. Cannot be cloned
 */
public abstract class Item implements Comparable {

    
    /** The budget of the Item, consisting of 3 numbers */
    public final BudgetValue budget;

    /**
     * The default constructor
     */
    protected Item() {
        this(new BudgetValue());
    }

    /**
     * Constructor with initial budget
     * @param key The key value
     * @param budget The initial budget
     */
    protected Item(final BudgetValue budget) {
        if (budget!=null)
            this.budget = new BudgetValue(budget);  // clone, not assignment
        else
            this.budget = null;
    }


    /**
     * Get the current key
     * @return Current key value
     */
    abstract public CharSequence getKey();


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

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof Item) {
            return ((Item)obj).getKey().equals(getKey());
        }
        return false;
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
     */
    public void merge(final Item that) {
        budget.merge(budget);
    }

    /**
     * Return a String representation of the Item
     * @return The String representation of the full content
     */
    @Override
    public String toString() {        
        //return budget + " " + key ;
        
        String budgetStr = budget!=null ? budget.toString() : "";
        return new StringBuilder(budgetStr.length()+getKey().length()+1).append(budgetStr).append(' ').append(getKey()).toString();
    }

    /**
     * Return a String representation of the Item after simplification
     * @return A simplified String representation of the content
     */
    public String toStringExternal() {        
        //return budget.toStringBrief() + " " + key ;
        final String briefBudget = budget.toStringExternal();
        return new StringBuilder(briefBudget.length()+getKey().length()+1).append(briefBudget).append(' ').append(getKey()).toString();
    }
    
    public String toStringLong() {
    	return toString();
    }

    //default:
    @Override
    public int compareTo(Object o) {
        //return System.identityHashCode(this) - System.identityHashCode(o);
        return hashCode() - o.hashCode();
    }
    
}
