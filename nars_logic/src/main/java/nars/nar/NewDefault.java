package nars.nar;

import nars.NAR;
import nars.concept.ConceptBuilder;
import nars.nal.LogicPolicy;
import nars.nal.LogicStage;
import nars.nal.Deriver;
import nars.nal.nal8.OpReaction;
import nars.op.app.STMEventInference;
import nars.op.mental.Abbreviation;
import nars.op.mental.Counting;
import nars.op.mental.FullInternalExperience;
import nars.op.mental.InternalExperience;
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

    //
    public Deriver getDeriver() {
        return Deriver.defaults;
    }

    @Override
    public LogicPolicy getLogicPolicy() {
        return nalex(getDeriver());
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

        n.the(Deriver.class, getDeriver());

        n.setCyclesPerFrame(cyclesPerFrame);

        if (maxNALLevel >= 7) {
            //n.on(PerceptionAccel.class);
            n.on(STMEventInference.class);


            if (maxNALLevel >= 8) {

                for (OpReaction o : defaultOperators)
                    n.on(o);
                for (OpReaction o : exampleOperators)
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
