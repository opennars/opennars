/*
 * Task.java
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

package com.googlecode.opennars.entity;

import com.googlecode.opennars.language.*;
import com.googlecode.opennars.main.Memory;

/**
 * A task to be processed.
 */
public class Task extends Item {
    private Sentence sentence;
    protected boolean structual = false;        // whether it is based on a structual rule
           
    public Task(Sentence s, BudgetValue b, Memory memory) {
        super(b, memory);
        sentence = s;
        key = sentence.toString();
    }
    
    public Sentence getSentence() {
        return sentence;
    }

    public Term getContent() {
        return sentence.getContent();
    }

    public boolean isStructual() {
        return structual;
    }
    
    public void setStructual() {
        structual = true;
    }

    public void merge(Item that) {
        ((BudgetValue) this).merge(that.getBudget());
        structual = (structual || ((Task) that).isStructual());
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(sentence);
        return s.toString();
    }

    public String toString2() {
        StringBuffer s = new StringBuffer();
        if (sentence instanceof Question)
            s.append(sentence);
        else
            s.append(((Judgement) sentence).toString2());
        return s.toString();
    }
}

