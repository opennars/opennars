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
import org.opennars.io.Symbols;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.util.List;

/**
 * Operator that creates a goal with a given statement
 */
public class Want extends Operator {

    public Want() {
        super("^want");
    }

    /**
     * To create a goal with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory) {

        final Term content = args[1];
        
        final TruthValue truth = new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
        final Sentence sentence = new Sentence(
            content,
            Symbols.GOAL_MARK,
            truth,
            new Stamp(memory));
        
        final BudgetValue budget = new BudgetValue(Parameters.DEFAULT_GOAL_PRIORITY, Parameters.DEFAULT_GOAL_DURABILITY, truth);

        Task.MakeInfo newTaskMakeInfo = new Task.MakeInfo();
        newTaskMakeInfo.sentence = sentence;
        newTaskMakeInfo.budget = budget;
        newTaskMakeInfo.isInput = true;
        final Task newTask = Task.make(newTaskMakeInfo);
        return Lists.newArrayList(newTask);
    }
    
}
