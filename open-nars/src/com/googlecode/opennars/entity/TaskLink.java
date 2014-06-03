/*
 * TaskLink.java
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

import java.util.*;

import com.googlecode.opennars.language.Term;
import com.googlecode.opennars.main.*;

/**
 * Reference to a Task.
 * <p>
 * The reason to separate a Task and a TaskLink is that a Task can be linked from multiple Concepts, with different BudgetValue.
 */
public class TaskLink extends TermLink {
    private static final int RECORD_LENGTH = Parameters.TASK_INFERENCE_RECORD_LENGTH;
    private Task targetTask;        // now "target" means a term !!!
    private ArrayList<String> record; // remember the CompositionLinks that has been used recently
    
    public TaskLink(Task t, TermLink template, BudgetValue v, Memory memory) {
        super(v, memory);
        if (template == null) {
            type = TermLink.SELF;
            index = null;
        } else {
            type = template.getType();
            index = template.getIndices();            
        }
        targetTask = t;
        record = new ArrayList<String>(Parameters.TASK_INFERENCE_RECORD_LENGTH);
        setKey();
        key += t.getKey();
    }
    
    public Task getTargetTask() {
        return targetTask;
    }
    
    public ArrayList<String> getRecord() {
        return record;
    }
    
    public void merge(Item that) {
        ((BudgetValue) this).merge(that.getBudget());
        ArrayList<String> v = ((TaskLink) that).getRecord();
        for (int i = 0; i < v.size(); i++)
            if (record.size() <= RECORD_LENGTH)
                record.add(v.get(i));
    }

    // To check whether a TaskLink can use a TermLink
    // return false if they intereacted recently
    // called in CompositionBag only
    // move into Task ?
    public boolean novel(TermLink bLink) {
        Term bTerm = bLink.getTarget();
        if (bTerm.equals(targetTask.getSentence().getContent()))
            return false;
        String key = bLink.getKey();
        for (int i = 0; i < record.size(); i++)
            if (key.equals((String) record.get(i)))
                return false;
        record.add(key);       // add knowledge reference to record
//        if (record.size() > RECORD_LENGTH)         // keep a constant length --- allow repeatation
//            record.remove(0);
        return true;
    }
}

