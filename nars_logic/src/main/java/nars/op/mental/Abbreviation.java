package nars.op.mental;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import nars.Events.TaskDerive;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.concept.Concept;
import nars.event.NARReaction;
import nars.nal.nal2.Similarity;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation extends NARReaction {

    private static final float abbreviationProbability = InternalExperience.INTERNAL_EXPERIENCE_PROBABILITY;
    public static final Operator abbreviate = Operator.the("abbreviate");
    public final Memory memory;

    final float abbreviationConfidence = 0.99f;

    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it:
    public final AtomicInteger abbreviationComplexityMin = new AtomicInteger(24);
    public final AtomicDouble abbreviationQualityMin = new AtomicDouble(0.7f);

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
        /*if (t instanceof Similarity) {
            Similarity s = (Similarity)t;
            if (Operation.isA(s.getSubject(), abbreviate)) return false;
            if (Operation.isA(s.getPredicate(), abbreviate)) return false;
        }*/
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

        if ((memory.random.nextFloat() < abbreviationProbability))
            return;

        Task task = (Task)a[0];

        //is it complex and also important? then give it a name:
        if (canAbbreviate(task)) {

            final Compound termAbbreviating = task.getTerm();

            /*Operation compound = Operation.make(
                    Product.make(termArray(termAbbreviating)), abbreviate);*/

            Concept concept = memory.concept(termAbbreviating);

            if (concept!=null && concept.get(Abbreviation.class)==null) {

                Term atomic = newSerialTerm();

                concept.put(Abbreviation.class, atomic);

                memory.add( memory.newTask(Similarity.make(termAbbreviating, atomic))
                        .judgment().truth(1, abbreviationConfidence)
                        .parent(task).occurrNow()
                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY,
                                Global.DEFAULT_JUDGMENT_DURABILITY).get()
                );

            }
            else {
                //already abbreviated, remember it
                remind.remind(termAbbreviating, memory);
            }
        }
    }



}
