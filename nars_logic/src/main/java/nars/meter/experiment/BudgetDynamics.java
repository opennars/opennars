package nars.meter.experiment;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.NARSeed;
import nars.meter.MemoryBudgetState;
import nars.meter.NARMetrics;
import nars.meter.NARTrace;
import nars.nal.Premise;
import nars.nar.Default;
import nars.process.DerivationReaction;
import nars.task.Task;
import nars.util.meter.TemporalMetrics;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by me on 7/12/15.
 */
public class BudgetDynamics {

    private final TemporalMetrics<Object> metrics;
    private final NAR nar;
    private final NARMetrics nm;

    public BudgetDynamics(NARSeed d) {

        this.nar  = new NAR(d);

        this.nm = new NARMetrics(nar, 1000);

        MemoryBudgetState.on("Memory", nm);

        this.metrics = nm.metrics;

    }



    public void input(String s) {
        nar.input(s + ".");
    }
    public void watchConcept(String s) {
        MemoryBudgetState.onConcept(nm, nar.term(s));
    }

    public void inputAndWatchConcept(String s) {
        input(s);
        watchConcept(s);
    }


    public static void main(String[] args) throws FileNotFoundException {

        Global.DEBUG_TERMLINK_SELECTED = true;

        Default d = new Default() {

            @Override
            protected void initDerivationFilters() {
                //super.initDerivationFilters();
            }

            public DerivationReaction getDerivationReaction() {
                return balancedDerivationBudget;
            }

            public final DerivationReaction balancedDerivationBudget = new DerivationReaction() {

                @Override
                public void onDerivation(Premise p, Iterable<Task> derived, Memory m) {


                    float totalDerivedPriority = 0;
                    for (Task t : derived)
                        totalDerivedPriority += t.getPriority();
                    System.err.println("@" + m.time() + ": " + this + " requesting " + totalDerivedPriority + " for " + derived);
                    System.err.println("  from " + p.getConcept() + " " + p.getConcept().getBudget().toBudgetString());
                    float refund = 0;
                    for (Task t : derived) {
                        final boolean added = m.taskAdd(t);
                        if (!added)
                            refund += t.getPriority();
                    }


                }

            };


        }.setInternalExperience(null);
        //d.noveltyHorizon.set(1);

        BudgetDynamics b = new BudgetDynamics(d);

        b.inputAndWatchConcept("<a --> b>");
        b.inputAndWatchConcept("<b --> c>");
        b.watchConcept("<a --> c>");

        //TextOutput.out(b.nar);
        NARTrace.out(b.nar);

        b.nar.frame(200);

        b.metrics.printCSV(
                //System.out
                "/tmp/b.csv"
        );

    }


}
