package nars.operate.mental;

import com.google.common.collect.Lists;
import nars.budget.Budget;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.nal.term.Term;
import nars.operate.AbstractOperator;
import nars.Events.TaskDerive;
import nars.Memory;
import nars.NAR;
import nars.Global;
import nars.io.Symbols;
import nars.nal.BudgetFunctions;
import nars.nal.stamp.Stamp;
import nars.nal.nal2.Similarity;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import reactor.jarjar.jsr166e.extra.AtomicDouble;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static nars.nal.term.Compound.termArray;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation extends AbstractOperator {

    private static final float abbreviationProbability = InternalExperience.INTERNAL_EXPERIENCE_PROBABILITY;
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
                    Symbols.JUDGMENT,
                    new TruthValue(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(operation, memory, Tense.Present));
            
            float quality = BudgetFunctions.truthToQuality(sentence.truth);
            
            Budget budget = new Budget(
                    Global.DEFAULT_JUDGMENT_PRIORITY,
                    Global.DEFAULT_JUDGMENT_DURABILITY,
                    quality);

            return Lists.newArrayList( operation.newSubTask(sentence, budget) );

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
                    abbreviate, termArray(task.sentence.term ));

            operation.setTask(task);

            abbreviate.execute(operation, memory);
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
            Operator _abbreviate = memory.operator("^abbreviate");
            if (_abbreviate == null) {
                n.on(abbreviate = new Abbreviate());
            }
        }
        
        
    }

    @Override
    public void onDisabled(NAR n) {

    }

}
