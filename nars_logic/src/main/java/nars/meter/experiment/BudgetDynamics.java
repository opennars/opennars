package nars.meter.experiment;

import nars.Memory;
import nars.NAR;
import nars.NARSeed;
import nars.concept.Concept;
import nars.meter.MemoryBudgetState;
import nars.meter.NARMetrics;
import nars.meter.NARTrace;
import nars.nar.Default;
import nars.premise.Premise;
import nars.process.DerivationReaction;
import nars.task.Task;
import nars.util.meter.TemporalMetrics;

import java.io.FileNotFoundException;

/**
 * Created by me on 7/12/15.
 */
public class BudgetDynamics {

    public final TemporalMetrics<Object> metrics;
    public final NAR nar;
    private final NARMetrics nm;

    public BudgetDynamics(NARSeed d) {

        this.nar  = new NAR(d);

        this.nm = new NARMetrics(nar, 1000);

        MemoryBudgetState.on("Memory", nm);

        this.metrics = nm.metrics;

    }



    public void input(String s) {
        nar.input(s);
    }
    public void watchConcept(String s) {
        MemoryBudgetState.onConcept(nm, nar.term(s));
    }

    public void believeAndWatchConcept(String s) {
        nar.believe(s);
        watchConcept(s);
    }
    public void askAndWatchConcept(String s) {
        nar.ask(s);
        watchConcept(s);
    }


    public static void main(String[] args) throws FileNotFoundException {



        Default d = new Default() {

            public DerivationReaction getDerivationReaction() {
                return balancedDerivationBudget;
            }

            public final DerivationReaction balancedDerivationBudget = new DerivationReaction() {

                @Override
                public void onDerivation(Premise p, Iterable<Task> derived, Memory m) {


                    Concept parentConcept = p.getConcept();
                    float parentConceptPriority = parentConcept.getPriority();

                    float totalCost = 0;
                    for (final Task t : derived) {
                        t.getBudget().mulPriority( parentConceptPriority );

                        //TODO threshold detect

                        totalCost += t.getPriority();
                    }

                    float refund = 0;
                    for (Task t : derived) {
                        final boolean added = m.input(t);
                        if (!added)
                            refund += t.getPriority();
                    }

                    totalCost -= refund;

                    System.err.println("@" + m.time() + ": " + this + " requesting " + totalCost + " for " + derived);
                    System.err.println("  from " + parentConcept + " " + parentConcept.getBudget().toBudgetString());

                    if (totalCost > 0) {
                        float newPriority;
                        if (totalCost > parentConceptPriority)
                            //this will produce a measurable deficit of: totalCost - parentConceptPriority
                            newPriority = 0;
                        else {
                            newPriority = parentConceptPriority - totalCost;

                        }

                        m.getControl().reprioritize(parentConcept.getTerm(), newPriority);

                    }



                }

            };


        }.setInternalExperience(null);
        //d.noveltyHorizon.set(1);
        d.conceptForgetDurations.set(5);

        BudgetDynamics b = new BudgetDynamics(d);

        b.watchConcept("a");
        b.watchConcept("b");
        b.watchConcept("c");
        b.believeAndWatchConcept("<a --> b>");
        b.believeAndWatchConcept("<b --> c>");
        b.watchConcept("<a --> c>");
        b.watchConcept("(&, a, b)");
        b.watchConcept("(|, b, c)");

        //TextOutput.out(b.nar);
        NARTrace.out(b.nar);

        b.nar.frame(150);

        //b.metrics.printCSV(System.out);
        b.metrics.printCSV("/tmp/b.csv");

    }


}
