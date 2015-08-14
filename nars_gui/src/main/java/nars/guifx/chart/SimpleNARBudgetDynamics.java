package nars.guifx.chart;

import nars.Global;
import nars.NARStream;
import nars.nar.Default;
import nars.nar.experimental.Equalized;
import nars.term.Atom;

import java.util.function.Consumer;

/**
 * Created by me on 8/12/15.
 */
public class SimpleNARBudgetDynamics {

    static String[] abClosed = new String[] { "<a-->b>.", "<b-->a>." };
    static String[] abcClosed = new String[] { "<a-->b>.", "<b-->c>." };

    public static void main(String[] args) {


        int cycles = 32;

        Consumer<NARStream> execution = n -> {
            n.stdout().input(abClosed).run(cycles);
        };


        //Default d = new Default(1024, 5, 3).setInternalExperience(null);
        Default d = new Equalized(1024, 40, 7).setInternalExperience(null);
        //NewDefault d = new NewDefault();
        //Solid d = new Solid(1,256, 1, 1, 1, 3);
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.8f;
        //d.conceptActivationFactor.set(0.5f);
        d.setCyclesPerFrame(1);
        d.duration.set(5);
        d.conceptForgetDurations.set(2);
        //d.taskLinkForgetDurations.set(2);
        //d.termLinkForgetDurations.set(2);

        new NARui(d)

                .meter( (metrics, nar) -> {
                    metrics.set("# concepts", nar.memory.numConcepts(true, false));
                })
                .meter( (metrics, nar) -> {  metrics
                        .set("A pri", nar.memory.conceptPriority(Atom.the("a")))
                        .set("B pri", nar.memory.conceptPriority(Atom.the("b")))
                        .set("<a-->b> pri", nar.memory.conceptPriority(nar.term("<a-->b>")))
                        .set("<b-->a> pri", nar.memory.conceptPriority(nar.term("<b-->a>")))
                        .set("<b-->a> pri", nar.memory.conceptPriority(nar.term("<b-->c>")))
                        .set("<a<->b> pri", nar.memory.conceptPriority(nar.term("<a<->b>")))
                        .set("<a<->c> pri", nar.memory.conceptPriority(nar.term("<a<->c>")))
                        ;
                })
                .meter( (metrics, nar) -> {  metrics
                            .set("mean(concept pri)", nar.memory.getActivePriorityPerConcept(true, false, false))
                            .set("sum(termlink pri)/cpt", nar.memory.getActivePriorityPerConcept(false, true, false))
                            .set("sum(tasklink pri)/cpt", nar.memory.getActivePriorityPerConcept(false, false, true))
                        ;
                })
                /*.meter("ConceptPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(true, false, false);
                })*///                .meter("A pri", (nar) -> {
//                    return nar.memory.conceptPriority(Atom.the("a"));
//                })
//                /*.meter("TermLinkPriorityMean", (nar) -> {
//                    return nar.memory.getActivePriorityPerConcept(false, true, false);
//                })
//                .meter("TaskLinkPriorityMean", (nar) -> {
//                    return nar.memory.getActivePriorityPerConcept(false, false, true);
//                })*/
//                .meter("Concepts", (nar) -> {
//                    return nar.memory.numConcepts(true, false);
//                })

                .then(execution)
                .viewAll();

    }


}
