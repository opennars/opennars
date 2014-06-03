/*
 * Tense.java
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

package com.googlecode.opennars.language;

import java.util.*;

import com.googlecode.opennars.main.Memory;

/**
 * Term with temporal relation with "now"
 */
public abstract class Tense extends CompoundTerm {
    
    protected Tense(String n, ArrayList<Term> arg) {
        super(n, arg);
    }

    protected Tense(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        super(n, cs, open, closed, i);
    }

    public static Term make(Term content, CompoundTerm.TemporalOrder order, Memory memory) {
        switch (order) {
            case AFTER:
                return TenseFuture.make(content, memory);
            case WHEN:
                return TensePresent.make(content, memory);
            case BEFORE:
                return TensePast.make(content, memory);
            default:
                return content;
        }
    }
}
