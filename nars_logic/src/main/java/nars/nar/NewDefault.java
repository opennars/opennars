package nars.nar;

import nars.NAR;
import nars.concept.ConceptBuilder;
import nars.nal.LogicPolicy;
import nars.nal.LogicStage;
import nars.nal.NALExecuter;
import nars.nal.nal8.Operator;
import nars.op.app.STMInduction;
import nars.op.mental.*;
import nars.process.concept.ConceptFireTaskTerm;
import nars.process.concept.FilterEqualSubtermsInRespectToImageAndProduct;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;

import static nars.op.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.op.mental.InternalExperience.InternalExperienceMode.Minimal;

/**
 * Temporary class which uses the new rule engine for ruletables
 */
public class NewDefault extends Default {

    @Override
    public LogicPolicy getLogicPolicy() {
        return nalex(NALExecuter.defaults);
    }

    public static LogicPolicy nalex(ConceptFireTaskTerm ruletable) {

        return new LogicPolicy(

                new LogicStage /* <ConceptProcess> */ [] {
                        new FilterEqualSubtermsInRespectToImageAndProduct(),
                        ruletable
                        //---------------------------------------------
                } ,

                new DerivationFilter[] {
                        new FilterBelowConfidence(),
                        new FilterDuplicateExistingBelief(),
                }

        );
    }

    /** initialization after NAR is constructed */
    @Override public void init(NAR n) {

        n.setCyclesPerFrame(cyclesPerFrame);

        if (maxNALLevel >= 7) {
            //n.on(PerceptionAccel.class);
            //n.on(STMInduction.class);


            if (maxNALLevel >= 8) {

                for (Operator o : defaultOperators)
                    n.on(o);
                for (Operator o : exampleOperators)
                    n.on(o);

                for (ConceptBuilder c : defaultConceptBuilders) {
                    n.on(c);
                }

                //n.on(Anticipate.class);      // expect an event

                if (internalExperience == Minimal) {
                    new InternalExperience(n);
                    new Abbreviation(n);
                } else if (internalExperience == Full) {
                    new FullInternalExperience(n);
                    n.on(new Counting());
                }
            }
        }

        //n.on(new RuntimeNARSettings());

    }
}
