package nars.operator.mental;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import nars.core.AbstractPlugin;
import nars.core.Events.TaskDerive;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.logic.BudgetFunctions;
import nars.logic.entity.*;
import nars.logic.nal2.Similarity;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static nars.logic.entity.CompoundTerm.termArray;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation extends AbstractPlugin {

    private static final double abbreviationProbability = InternalExperience.INTERNAL_EXPERIENCE_PROBABILITY;
    private Operator abbreviate = null;
    private Memory memory;

    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it:
    public final AtomicInteger abbreviationComplexityMin = new AtomicInteger(20);
    public final AtomicDouble abbreviationQualityMin = new AtomicDouble(0.95f);

    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public AtomicDouble priorityFactor = new AtomicDouble(1.0);



    /**
    * Operator that give a CompoundTerm an atomic name
    */
    public static class Abbreviate extends Operator implements Mental {

        public Abbreviate() {
            super("^abbreviate");
        }

        private static final AtomicInteger currentTermSerial = new AtomicInteger(1);

        public Term newSerialTerm() {
            return new Term(Symbols.TERM_PREFIX + String.valueOf(currentTermSerial.incrementAndGet()));
        }


        /**
         * To create a judgment with a given statement
         * @param args Arguments, a Statement followed by an optional tense
         * @param memory The memory in which the operation is executed
         * @return Immediate results as Tasks
         */
        @Override
        protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
            
            Term compound = args[0];
            
            Term atomic = newSerialTerm();
                        
            Sentence sentence = new Sentence(
                    Similarity.make(compound, atomic), 
                    Symbols.JUDGMENT_MARK, 
                    new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(memory));
            
            float quality = BudgetFunctions.truthToQuality(sentence.truth);
            
            BudgetValue budget = new BudgetValue(
                    Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                    Parameters.DEFAULT_JUDGMENT_DURABILITY, 
                    quality);

            return Lists.newArrayList( new Task(sentence, budget) );        

        }

    }
    

    public boolean canAbbreviate(Task task) {
        return !(task.sentence.term instanceof Operation) && 
                (task.sentence.term.getComplexity() > abbreviationComplexityMin.get()) &&
                (task.budget.getQuality() > abbreviationQualityMin.get());
    }




    @Override
    public void event(Class event, Object[] a) {
        if (event != TaskDerive.class)
            return;

        if ((Memory.randomNumber.nextDouble() < abbreviationProbability))
            return;

        Task task = (Task)a[0];

        //is it complex and also important? then give it a name:
        if (canAbbreviate(task)) {

            Operation operation = Operation.make(
                    abbreviate, termArray(task.sentence.term ),
                    false);

            operation.setTask(task);

            abbreviate.call(operation, memory);
        }
    }

    @Override
    public Class[] getEvents() {
        return new Class[] { TaskDerive.class };
    }

    @Override
    public void onEnabled(NAR n) {
        memory = n.memory;

        if (abbreviate == null) {
            Operator _abbreviate = memory.getOperator("^abbreviate");
            if (_abbreviate == null) {
                abbreviate = memory.addOperator(new Abbreviate());
            }
        }
        
        
    }

    @Override
    public void onDisabled(NAR n) {

    }

}
