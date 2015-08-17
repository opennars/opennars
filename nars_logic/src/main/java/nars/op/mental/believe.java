/*
 * Believe.java
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
package nars.op.mental;

import com.google.common.collect.Lists;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.ArrayList;

/**
 * Operator that creates a judgment with a given statement
 * Causes the system to belief things it has no evidence for
 */
public class believe extends SynchOperator implements Mental {

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     *
     */
    @Override
    public ArrayList<Task> apply(Operation op) {

        //TODO convert to TaskSeed

        Compound content = Sentence.termOrException(op.arg(0));

        Truth truth;
        return Lists.newArrayList( op.newSubTask(op.getMemory(),
                content, Symbols.JUDGMENT, truth = new DefaultTruth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE), memory.time(),
                new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, truth)) );


    }

//    @Override
//    public boolean isExecutable(Memory mem) {
//        return false;
//    }
}