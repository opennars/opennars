package nars.guifx.chart;

import nars.Global;
import nars.NARSeed;
import nars.NARStream;
import nars.nar.experimental.DefaultAlann;
import nars.narsese.NarseseParser;
import nars.term.Term;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by me on 8/12/15.
 */
public class SimpleNARBudgetDynamics {

    static String[] abClosed = new String[] { "<a-->b>.", "<b-->a>." };
    static String[] abcClosed = new String[] { "<a-->b>.", "<b-->c>." };

    static final Map<String,Term> terms = new HashMap();

    static NarseseParser np = NarseseParser.the();

    public static Term term(String s) {
        Term x = terms.get(s);
        if (x == null) {
            terms.put(s, x = np.term(s));
        }
        return x;
    }

    public static void main(String[] args) {


        int preCycles = 0;
        int cycles = 80;

        float pri = 0.1f;

        String[] x = abcClosed.clone();
        for (int i = 0; i < x.length; i++) {
            x[i] = "$" + pri + ";" + pri + ";" + pri + "$ " + x[i];
        }




        //Default d = new Default(1024, 1, 3).setInternalExperience(null);
        //Default d = new Equalized(1024, 5, 7).setInternalExperience(null);
        //Default d = new NewDefault().setInternalExperience(null);
        //NARSeed d = new ParallelAlann(500, 2);
        NARSeed d = new DefaultAlann(200);
        //Solid d = new Solid(1,256, 1, 1, 1, 3);
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.9f;
        //d.conceptActivationFactor.set(0.5f);
        //d.setCyclesPerFrame(1);
        //d.duration.set(5);
        //d.level(3);
        //d.conceptForgetDurations.set(2);
        //d.taskLinkForgetDurations.set(1);
        //d.termLinkForgetDurations.set(2);




        Consumer<NARStream> execution = n -> {
            n.input(x).run(cycles);

            n.nar.memory.concepts.forEach(System.out::println);

        };

        new NARui(d)

                .then(n -> { n.nar.frame(preCycles); })
                .meter( (metrics, nar) -> {
                    metrics.set("# concepts", nar.memory.numConcepts(true, false));
                })
                .meter( (metrics, nar) -> {  metrics
                        .set("A pri", nar.memory.conceptPriority(term("a")))
                        .set("B pri", nar.memory.conceptPriority(term("b")))
                        .set("<a-->b> pri", nar.memory.conceptPriority(term("<a-->b>")))
                        .set("<b-->a> pri", nar.memory.conceptPriority(term("<b-->a>")))
                        .set("<b-->a> pri", nar.memory.conceptPriority(term("<b-->c>")))
                        .set("<a<->b> pri", nar.memory.conceptPriority(term("<a<->b>")))
                        .set("<a<->c> pri", nar.memory.conceptPriority(term("<a<->c>")))
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
