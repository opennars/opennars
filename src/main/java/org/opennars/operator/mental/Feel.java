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
import org.opennars.language.Inheritance;
import org.opennars.language.SetInt;
import org.opennars.language.Tense;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.util.List;

/**
 * Feeling common operations
 */
public abstract class Feel extends Operator {
    private final Term feelingTerm;

    public Feel(final String name) {
        super(name);
        
        // remove the "^feel" prefix from name
        this.feelingTerm = Term.get(((String)name()).substring(5).toLowerCase());
    }

    final static Term selfSubject = Term.SELF;
    
    /**
     * To get the current value of an internal sensor
     *
     * @param value The value to be checked, in [0, 1]
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    protected List<Task> feeling(final float value, final Memory memory) {
        final Stamp stamp = new Stamp(memory, Tense.Present);
        final TruthValue truth = new TruthValue(value, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                
        final Term predicate = new SetInt(feelingTerm);
        
        final Term content = Inheritance.make(selfSubject, predicate);
        final Sentence sentence = new Sentence(
            content,
            Symbols.JUDGMENT_MARK,
            truth,
            stamp);

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
