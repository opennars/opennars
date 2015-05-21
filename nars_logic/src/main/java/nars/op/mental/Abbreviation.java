package nars.op.mental;

import com.google.common.util.concurrent.AtomicDouble;
import nars.Events.TaskDerive;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.event.NARReaction;
import nars.nal.DefaultTruth;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.stamp.Stamp;
import nars.nal.term.Atom;
import nars.nal.term.Term;

import java.util.concurrent.atomic.AtomicInteger;

import static nars.nal.term.Compound.termArray;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation extends NARReaction {

    private static final float abbreviationProbability = InternalExperience.INTERNAL_EXPERIENCE_PROBABILITY;
    public static final Term abbreviate = Atom.the("abbreviate");
    public final Memory memory;

    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it:
    public final AtomicInteger abbreviationComplexityMin = new AtomicInteger(16);
    public final AtomicDouble abbreviationQualityMin = new AtomicDouble(0.75f);

    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public AtomicDouble priorityFactor = new AtomicDouble(1.0);


    public Abbreviation(NAR n) {
        super(n, TaskDerive.class );

        memory = n.memory;

    }

    private static final AtomicInteger currentTermSerial = new AtomicInteger(1);

    public Term newSerialTerm() {
        return Atom.the(Symbols.TERM_PREFIX + Integer.toHexString(currentTermSerial.incrementAndGet()));
    }



    public boolean canAbbreviate(final Task task) {
        final Term t = task.getTerm();

        if (t instanceof Operation) return false;
        if (t instanceof Similarity) {
            Similarity s = (Similarity)t;
            if (Operation.isA(s.getSubject(), abbreviate)) return false;
            if (Operation.isA(s.getPredicate(), abbreviate)) return false;
        }
        return  (t.getComplexity() > abbreviationComplexityMin.get()) &&
                (task.getQuality() > abbreviationQualityMin.get());
    }



    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory
     * @return Immediate results as Tasks
     */
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
                    abbreviate, Product.make(termArray(task.sentence.term)));

            operation.setTask(task);

            Term compound = operation;

            Term atomic = newSerialTerm();

            Sentence sentence = new Sentence(
                    Similarity.make(compound, atomic),
                    Symbols.JUDGMENT,
                    new DefaultTruth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(operation, memory, Tense.Present));

            Budget budget = new Budget(
                    Global.DEFAULT_JUDGMENT_PRIORITY,
                    Global.DEFAULT_JUDGMENT_DURABILITY,
                    BudgetFunctions.truthToQuality(sentence.truth));

            DirectProcess.run( memory, memory.task(sentence, budget, task ));

            //memory.execute(operation);
            //abbreviate.execute(operation, memory);
        }
    }



}
