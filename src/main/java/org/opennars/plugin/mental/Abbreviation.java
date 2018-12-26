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
package org.opennars.plugin.mental;

import com.google.common.collect.Lists;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events.TaskDerive;
import org.opennars.language.Similarity;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

import java.util.List;

import static org.opennars.language.CompoundTerm.termArray;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation implements Plugin {
    public EventObserver obs;

    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public PortableDouble priorityFactor = new PortableDouble(1.0);


    public volatile double abbreviationProbability = 0.0001f;
    public volatile int abbreviationComplexityMin = 20;
    public volatile double abbreviationQualityMin = 0.95f;

    public void setAbbreviationProbability(double val) {
        this.abbreviationProbability = val;
    }
    public double getAbbreviationProbability() {
        return abbreviationProbability;
    }
    
    public void setAbbreviationComplexityMin(double val) {
        this.abbreviationComplexityMin = (int) val;
    }
    public double getAbbreviationComplexityMin() {
        return abbreviationComplexityMin;
    }

    public void setAbbreviationQualityMin(double val) {
        this.abbreviationQualityMin = val;
    }
    public double getAbbreviationQualityMin() {
        return abbreviationQualityMin;
    }
    
    public Abbreviation(){}
    public Abbreviation(double abbreviationProbability, int abbreviationComplexityMin, double abbreviationQualityMin) {
        this.abbreviationProbability = abbreviationProbability;
        this.abbreviationComplexityMin = abbreviationComplexityMin;
        this.abbreviationQualityMin = abbreviationQualityMin;
    }

    public boolean canAbbreviate(final Task task) {
        return !(task.sentence.term instanceof Operation) && 
                (task.sentence.term.getComplexity() > abbreviationComplexityMin) &&
                (task.budget.getQuality() > abbreviationQualityMin);
    }
    
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        final Memory memory = n.memory;
        
        Operator _abbreviate = memory.getOperator("^abbreviate");
        if (_abbreviate == null) {
            _abbreviate = memory.addOperator(new Abbreviate());
        }
        final Operator abbreviate = _abbreviate;
        
        if(obs==null) {
            obs= (event, a) -> {
                if (event != TaskDerive.class)
                    return;

                if ((abbreviationProbability < 1.0) && (Memory.randomNumber.nextDouble() >= abbreviationProbability))
                    return;

                final Task task = (Task)a[0];

                //is it complex and also important? then give it a name:
                if (canAbbreviate(task)) {

                    final Operation operation = Operation.make(
                            abbreviate, termArray(task.sentence.term ),
                            false);

                    operation.setTask(task);

                    abbreviate.call(operation, memory, n);
                }

            };
        }
        
        memory.event.set(obs, enabled, TaskDerive.class);
        
        return true;
    }


    /**
     * Operator that give a CompoundTerm an atomic name
     */
    public static class Abbreviate extends Operator {

        public Abbreviate() {
            super("^abbreviate");
        }

        private static Integer currentTermSerial = 1;
        public Term newSerialTerm(final char prefix) {
            synchronized(currentTermSerial) {
                currentTermSerial++;
            }
            return new Term(prefix + String.valueOf(currentTermSerial));
        }


        /**
         * To create a judgment with a given statement
         * @param args Arguments, a Statement followed by an optional tense
         * @param memory The memory in which the operation is executed
         * @return Immediate results as Tasks
         */
        @Override
        protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory, final Timable time) {

            final Term compound = args[0];

            final Term atomic = newSerialTerm(Symbols.TERM_PREFIX);

            final Sentence sentence = new Sentence(
                Similarity.make(compound, atomic),
                Symbols.JUDGMENT_MARK,
                new TruthValue(1, memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, memory.narParameters),  // a naming convension
                new Stamp(time, memory));

            final float quality = BudgetFunctions.truthToQuality(sentence.truth);

            final BudgetValue budget = new BudgetValue(
                memory.narParameters.DEFAULT_JUDGMENT_PRIORITY,
                memory.narParameters.DEFAULT_JUDGMENT_DURABILITY,
                quality, memory.narParameters);

            final Task newTask = new Task(sentence, budget, Task.EnumType.INPUT);
            return Lists.newArrayList(newTask);

        }

    }
}
