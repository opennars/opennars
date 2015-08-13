package nars.guifx.chart;

import nars.Global;
import nars.NARStream;
import nars.nar.Default;
import nars.term.Atom;

import java.util.function.Consumer;

/**
 * Created by me on 8/12/15.
 */
public class SimpleNARBudgetDynamics {

    static String[] abClosed = new String[] { "<a-->b>.", "<b-->a>." };
    static String[] abcClosed = new String[] { "<a-->b>.", "<b-->c>." };

    public static void main(String[] args) {


        int cycles = 200;

        Consumer<NARStream> execution = n -> {
            n.input(abcClosed).run(cycles);
        };


        Default d = new Default(1024, 1, 3);
        //NewDefault d = new NewDefault();
        //Solid d = new Solid(1,256, 1, 1, 1, 3);
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.8f;
        //d.conceptActivationFactor.set(0.3f);
        d.setCyclesPerFrame(1);
        d.duration.set(5);
        d.conceptForgetDurations.set(3);
        //d.taskLinkForgetDurations.set(2);
        //d.termLinkForgetDurations.set(2);

        new NARui(d)
                .meter( (metrics, nar) -> {
                        metrics.set("A pri", nar.memory.numConcepts(true, false));
                })
                .meter( (metrics, nar) -> {  metrics
                        .set("A pri", nar.memory.conceptPriority(Atom.the("a")))
                        .set("B pri", nar.memory.conceptPriority(Atom.the("b")))
                        .set("<a-->b> pri", nar.memory.conceptPriority(nar.term("<a-->b>")))
                        .set("<b-->a> pri", nar.memory.conceptPriority(nar.term("<b-->a>")))
                        .set("<a<->b> pri", nar.memory.conceptPriority(nar.term("<a<->b>")))
                        ;
                })
                .meter( (metrics, nar) -> {  metrics
                            .set("sum(termlink pri)", nar.memory.getActivePriorityPerConcept(false, true, false))
                            .set("sum(tasklink pri)", nar.memory.getActivePriorityPerConcept(false, false, true))
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
