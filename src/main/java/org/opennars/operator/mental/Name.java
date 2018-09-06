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
package org.opennars.operator.mental;

import com.google.common.collect.Lists;
import org.opennars.entity.*;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.language.Similarity;
import org.opennars.language.Term;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.util.List;

/**
 * Operator that give a CompoundTerm a new name
 */
public class Name extends Operator {

    public Name() {
        super("^name");
    }

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory, final Timable time) {
        final Term compound = args[1];
        final Term atomic = args[2];
        final Similarity content = Similarity.make(compound, atomic);
        
        final TruthValue truth = new TruthValue(1, 0.9999f, memory.narParameters);  // a naming convension
        final Sentence sentence = new Sentence(
            content,
            Symbols.JUDGMENT_MARK,
            truth,
            new Stamp(time, memory),
            memory.narParameters);
        
        final BudgetValue budget = new BudgetValue(memory.narParameters.DEFAULT_JUDGMENT_PRIORITY, memory.narParameters.DEFAULT_JUDGMENT_DURABILITY, truth, memory.narParameters);

        final Task newTask = new Task(sentence, budget, Task.EnumType.INPUT);
        return Lists.newArrayList(newTask);
    }
}
