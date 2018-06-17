/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            new Stamp(time, memory));
        
        final BudgetValue budget = new BudgetValue(memory.narParameters.DEFAULT_JUDGMENT_PRIORITY, memory.narParameters.DEFAULT_JUDGMENT_DURABILITY, truth, memory.narParameters);

        final Task newTask = new Task(sentence, budget, Task.EnumType.INPUT);
        return Lists.newArrayList(newTask);
    }
}
