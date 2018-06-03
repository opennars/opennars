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
import org.opennars.inference.BudgetFunctions;
import org.opennars.io.Symbols;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.util.List;

/**
 * Operator that creates a judgment with a given statement
 * Causes the system to belief things it has no evidence for
 */
public class Believe extends Operator {

    public Believe() {
        super("^believe");
    }

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
+    * @return Immediate results as Tasks
     */
    @Override
    protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory) {

        final Term content = args[1];
        
        final TruthValue truth = new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
        final Sentence sentence = new Sentence(
            content,
            Symbols.JUDGMENT_MARK,
            truth,
            new Stamp(memory));

        final float quality = BudgetFunctions.truthToQuality(truth);
        final BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, quality);

        Task.MakeInfo newTaskMakeInfo = new Task.MakeInfo();
        newTaskMakeInfo.sentence = sentence;
        newTaskMakeInfo.budget = budget;
        newTaskMakeInfo.isInput = true;
        final Task newTask = Task.make(newTaskMakeInfo);

        return Lists.newArrayList(newTask);

    }
}
