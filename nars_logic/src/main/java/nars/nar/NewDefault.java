package nars.nar;

import nars.Param;
import nars.nal.*;
import nars.process.concept.FilterEqualSubtermsAndSetPremiseBelief;

/**
 * Temporary class which uses the new rule engine for ruletables
 *
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model *
 */
public class NewDefault extends Default {

    public NewDefault() {
        this(1,1,3);
    }

    public NewDefault(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }

    @Override
    public Param newParam() {
        Param p = super.newParam();
        //deprecated: all reasoning components should be added to the DI index automatically
        the(Deriver.class, der);
        return p;
    }

    public static final Deriver der;
    static {
        Deriver r;

        try {
            r = new SimpleDeriver(DerivationRules.standard);
        } catch (Exception e) {
            r = null;
            e.printStackTrace();
            System.exit(1);
        }

        der = r;
    }


    @Override
    public PremiseProcessor getPremiseProcessor(Param p) {

        return new PremiseProcessor(

                new LogicStage[] {
                        new FilterEqualSubtermsAndSetPremiseBelief(),
                        //new QueryVariableExhaustiveResults(),
                        p.the(Deriver.class)
                        //---------------------------------------------
                } ,

                getDerivationFilters()

        );
    }



}
