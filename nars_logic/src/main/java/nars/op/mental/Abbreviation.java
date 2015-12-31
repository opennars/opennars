package nars.op.mental;

import com.google.common.util.concurrent.AtomicDouble;
import nars.*;
import nars.concept.Concept;
import nars.nal.nal8.Operator;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;

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

    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public AtomicDouble priorityFactor = new AtomicDouble(1.0);


    public Abbreviation(NAR n) {

        n.memory.eventInput.on(this);
        nar = n;

    }

    private static final AtomicInteger currentTermSerial = new AtomicInteger(1);

    public static Term newSerialTerm() {
        return Atom.the(Symbols.TERM_PREFIX + Integer.toHexString(currentTermSerial.incrementAndGet()));
    }



    public boolean canAbbreviate(Task task) {
        Term t = task.term();

        if (Op.isOperation(t)) return false;
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
            if ((nar.memory.random.nextFloat() >= abbreviationProbability)) {


                Compound termAbbreviating = task.term();

            /*Operation compound = Operation.make(
                    Product.make(termArray(termAbbreviating)), abbreviate);*/

                Concept concept = nar.concept(termAbbreviating);

                if (concept != null && concept.get(Abbreviation.class) == null) {

                    Term atomic = newSerialTerm();

                    concept.put(Abbreviation.class, atomic);

                    Compound c = (Compound) $.sim(termAbbreviating, atomic);
                    if (c != null) {

                        Memory m = nar.memory;
                        nar.input(
                                new MutableTask(c)
                                        .judgment().truth(1, abbreviationConfidence)
                                        .parent(task).present(m)
                        );
                    }
                }
            }
        }
    }



}
