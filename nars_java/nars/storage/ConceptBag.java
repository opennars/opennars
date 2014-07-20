/*
 * ConceptBag.java
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

package nars.storage;

import java.util.concurrent.atomic.AtomicInteger;
import nars.entity.Concept;
import nars.language.Term;

/**
 * Contains Concepts.
 */
public class ConceptBag extends Bag<Concept> {
    private final AtomicInteger forgettingRate;
    
    /** Constructor
     * @param memory The reference of memory
     */
    public ConceptBag(int levels, int capacity, AtomicInteger forgettingRate) {
        super(levels, capacity);
        this.forgettingRate = forgettingRate;
    }
    
    /**
     * Get the (adjustable) forget rate of ConceptBag
     * @return The forget rate of ConceptBag
     */
    @Override
    protected int forgetRate() {
    	return forgettingRate.get();
    }
    
    public void printAll() {
        for (String k : this.nameTable.keySet()) {
            Term v = nameTable.get(k).getTerm();
            System.out.println("  " + k + " " + v + " (" + v.getClass().getSimpleName() + ")" );
        }
    }

}