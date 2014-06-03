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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.storage;

import java.util.HashMap;

import com.googlecode.opennars.entity.Concept;
import com.googlecode.opennars.main.Parameters;

/**
 * Contains Concepts.
 */
public class ConceptBag extends Bag<Concept> {
    
    protected int capacity() {
        return Parameters.CONCEPT_BAG_SIZE;
    }
    
    // this is for active concept only
    protected int forgetRate() {
        return Parameters.CONCEPT_DEFAULT_FORGETTING_CYCLE;
    }

	public HashMap<String, Concept> getNameTable() {
		return this.nameTable;
	}
}