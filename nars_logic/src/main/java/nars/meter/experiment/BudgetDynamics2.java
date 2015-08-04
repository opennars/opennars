package nars.meter.experiment;

import nars.Global;
import nars.NARSeed;
import nars.meter.MaxBeliefConfidence;
import nars.nar.Default;

import java.io.FileNotFoundException;

/**
 * Created by me on 7/14/15.
 */
public class BudgetDynamics2 extends BudgetDynamics {

    public BudgetDynamics2(NARSeed d) {
        super(d);
    }


    public static void main(String[] args) throws FileNotFoundException {

        Global.DEBUG_TERMLINK_SELECTED = true;

        Default d = new Default() {



            /*public DerivationReaction getDerivationReaction() {
                return balancedDerivationBudget;
            }*/



        }.setInternalExperience(null);


        BudgetDynamics b = new BudgetDynamics(d);


        b.believeAndWatchConcept("<a --> b>");
        b.believeAndWatchConcept("<b --> c>");
        b.believeAndWatchConcept("<c --> d>");
        b.askAndWatchConcept("<a --> d>");


        MaxBeliefConfidence mbc;
        b.metrics.add(mbc = new MaxBeliefConfidence(b.nar, "<a --> d>", 1.0f));

        //TextOutput.out(b.nar);
        //NARTrace.out(b.nar);

        b.nar.frame(1550);

        //b.metrics.printCSV(System.out);
        b.metrics.printCSV("/tmp/b.csv");
        //b.metrics.printARFF(new PrintStream(new FileOutputStream("/tmp/b.csv")));

        System.out.println("max conf: " + mbc);

    }



}
