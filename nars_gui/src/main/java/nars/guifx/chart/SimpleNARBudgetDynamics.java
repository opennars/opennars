package nars.guifx.chart;

import nars.Global;
import nars.NARStream;
import nars.nar.Default;
import nars.nar.NewDefault;
import nars.term.Atom;

import java.util.function.Consumer;

/**
 * Created by me on 8/12/15.
 */
public class SimpleNARBudgetDynamics {


    public static void main(String[] args) {


        Consumer<NARStream> execution = n -> {
            n.input("$0.5;0.5;0.05$ <a-->b>.",
                    "$0.5;0.5;0.05$ <b-->c>.").run(1500);
        };

        //Equalized d = new Equalized(1024, 2, 6);
        NewDefault d = new NewDefault();
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.8f;
        d.conceptActivationFactor.set(0.4f);
        d.setCyclesPerFrame(1);
        d.duration.set(5);
        d.conceptForgetDurations.set(2);
        d.taskLinkForgetDurations.set(2);
        d.termLinkForgetDurations.set(2);

        new NARui(d)
                .meter("ConceptPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(true, false, false);
                })
                .meter("A pri", (nar) -> {
                    return nar.memory.conceptPriority(Atom.the("a"));
                })
                .meter("B pri", (nar) -> {
                    return nar.memory.conceptPriority(Atom.the("b"));
                })
                .meter("A-->B pri", (nar) -> {
                    return nar.memory.conceptPriority(nar.term("<a-->b>"));
                })
                .meter("B-->A pri", (nar) -> {
                    return nar.memory.conceptPriority(nar.term("<b-->a>"));
                })
                .meter("A<->B pri", (nar) -> {
                    return nar.memory.conceptPriority(nar.term("<a<->b>"));
                })
                .meter("B-->C pri", (nar) -> {
                    return nar.memory.conceptPriority(nar.term("<b-->c>"));
                })
                .meter("C-->B pri", (nar) -> {
                    return nar.memory.conceptPriority(nar.term("<c-->b>"));
                })
                .meter("TermLinkPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(false, true, false);
                })
                .meter("TaskLinkPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(false, false, true);
                })
                .meter("Concepts", (nar) -> {
                    return nar.memory.numConcepts(true, false);
                })
                .then(execution)
                .view();

    }


}
