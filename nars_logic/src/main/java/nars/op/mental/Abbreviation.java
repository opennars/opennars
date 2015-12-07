package nars.op.mental;

import com.google.common.util.concurrent.AtomicDouble;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.concept.Concept;
import nars.nal.nal2.Similarity;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.util.event.On;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 1-step abbreviation, which calls ^abbreviate directly and not through an added Task.
 * Experimental alternative to Abbreviation plugin.
 */
public class Abbreviation implements Consumer<Task> {

    private static final float abbreviationProbability = InternalExperience.INTERNAL_EXPERIENCE_PROBABILITY;
    public static final Operator abbreviate = Operator.the("abbreviate");

    final float abbreviationConfidence = 0.99f;

    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it:
    public final AtomicInteger abbreviationComplexityMin = new AtomicInteger(24);
    public final AtomicDouble abbreviationQualityMin = new AtomicDouble(0.7f);
    private final NAR nar;
    private final On reg;

    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public AtomicDouble priorityFactor = new AtomicDouble(1.0);


    public Abbreviation(NAR n) {

        reg = n.memory.eventInput.on(this);

        this.nar = n;
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
        return  (t.complexity() > abbreviationComplexityMin.get()) &&
                (task.getQuality() > abbreviationQualityMin.get());
    }




    /**
     * To create a judgment with a given statement
     * @return Immediate results as Tasks
     */
    @Override
    public void accept(Task task) {


        //is it complex and also important? then give it a name:
        if (canAbbreviate(task)) {

            if ((nar.memory.random.nextFloat() < abbreviationProbability))
                return;


            final Compound termAbbreviating = task.getTerm();

            /*Operation compound = Operation.make(
                    Product.make(termArray(termAbbreviating)), abbreviate);*/

            Concept concept = nar.concept(termAbbreviating);

            if (concept!=null && concept.get(Abbreviation.class)==null) {

                Term atomic = newSerialTerm();

                concept.put(Abbreviation.class, atomic);

                Compound c = Task.taskable( Similarity.make(termAbbreviating, atomic) );
                if (c!=null) {

                    Memory m = nar.memory;
                    nar.input(MutableTask.make(
                            c)
                            .judgment().truth(1, abbreviationConfidence)
                            .parent(task).present(m)
                            .budget(Global.DEFAULT_JUDGMENT_PRIORITY,
                                    Global.DEFAULT_JUDGMENT_DURABILITY)
                    );
                }

            }
            else {
                //already abbreviated, remember it
                remind.remind(termAbbreviating, nar);
            }
        }
    }



}
