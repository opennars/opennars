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

import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;
import nars.term.compound.Compound;
import nars.truth.Truth;

import java.util.ArrayList;

/**
 * Operator that creates a judgment with a given statement
 * Causes the system to belief things it has no evidence for
 */
public class believe extends SyncOperator implements Mental {

    /**
     * To create a judgment with a given statement
     */
    @Override
    public ArrayList<Task> apply(Task<Operation> op) {

        //TODO convert to TaskSeed

        Compound content = (Compound) op.getTerm().arg(0);
        if (content == null)
            return null;

        Truth truth;

        throw new RuntimeException("unimpl TODO");
//        //TODO clean this up, it's non-standard
//        return Lists.newArrayList(op.getTerm().newSubTask(
//                op,
//                nar.memory,
//                content, Symbols.JUDGMENT, truth = new DefaultTruth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),
//                nar.time(),
//                new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, truth)));
    }

//    @Override
//    public boolean isExecutable(Memory mem) {
//        return false;
//    }
}