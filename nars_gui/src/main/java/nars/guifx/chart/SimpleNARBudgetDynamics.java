package nars.guifx.chart;

import nars.Global;
import nars.NAR;
import nars.meter.MemoryBudget;
import nars.nar.Default;
import nars.narsese.NarseseParser;
import nars.task.Task;
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
        int cycles = 256;


        float pri = 0.5f;
        float dur = 0.8f;
        float qua = 0.8f;


        Default d = new Default(); //Equalized(1024, 1, 3);
        //Default d = new NewDefault().setInternalExperience(null);
        //NARSeed d = new ParallelAlann(500, 2);

        //NARSeed d = new DefaultAlann(64);


        //d.getParam().conceptForgetDurations.set(1);
        //d.getParam().termLinkForgetDurations.set(1);
        //d.getParam().taskLinkForgetDurations.set(1);
        //d.getParam().duration.set(10);
        //Solid d = new Solid(1,256, 1, 1, 1, 3);
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.9f;
        Global.TASKLINK_FORGETTING_EXTRA_DEPTH = 0.9f;
        Global.TERMLINK_FORGETTING_EXTRA_DEPTH = 0.9f;
        //d.conceptActivationFactor.set(0.5f);
        //d.setCyclesPerFrame(1);
        //d.duration.set(5);
        //d.level(3);
        //d.getParam().conceptForgetDurations.set(2);
        //d.taskLinkForgetDurations.set(16);
        //d.termLinkForgetDurations.set(16);




        Consumer<NAR> execution = n -> {

            //n.stdout();

            String[] x = abcClosed.clone();
            for (int i = 0; i < x.length; i++) {

                Task t = n.task(x[i]);
                t.getBudget().setPriority(pri);
                t.getBudget().setQuality(qua);
                t.getBudget().setDurability(dur);

                n.input(t);
            }

            n.run(cycles);

            //n.nar.memory.concepts.forEach(System.out::println);
            n.memory.concepts.forEach(c -> c.print(System.out));

        };

        final MemoryBudget mb = new MemoryBudget();

        new NARui(d)

                .then(n -> {
                    n.frame(preCycles);

                })
                .meter( (metrics, nar) -> {
                    metrics.set("# concepts", nar.numConcepts(true, false));
                })
                .meter( (metrics, nar) -> {

                    mb.clear();
                    mb.update(nar.memory);

                    System.out.println(nar.time());

                    double conPriSum = mb.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum);
                    long numConcepts = mb.getLong(MemoryBudget.Budgeted.ActiveConcepts);
                    if (numConcepts == 0) numConcepts = 1;

                    metrics
                        .set("a pri", nar.memory.conceptPriority(term("a"), 0f))
                        .set("b pri", nar.memory.conceptPriority(term("b"), 0f))
                        .set("c pri", nar.memory.conceptPriority(term("c"), 0f))
                        .set("<a-->b> pri", nar.memory.conceptPriority(term("<a-->b>"), 0f))
                        //.set("<b-->a> pri", nar.memory.conceptPriority(term("<b-->a>")))
                        .set("<b-->c> pri", nar.memory.conceptPriority(term("<b-->c>"), 0f))
                        .set("<a<->b> pri", nar.memory.conceptPriority(term("<a<->b>"), 0f))
                        .set("<a<->c> pri", nar.memory.conceptPriority(term("<a<->c>"), 0f))
                        .set("mean(concept pri)", conPriSum / numConcepts) // .getActivePriorityPerConcept(true, false, false)
                        ;
                })
                .meter( (metrics, nar) -> {


                    //double conPriSum = mb.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum);
                    long numConcepts = mb.getLong(MemoryBudget.Budgeted.ActiveConcepts);
                    if (numConcepts == 0) numConcepts = 1;

                    metrics

                            .set("stddev(concept pri)", mb.getDoubleFinite(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev, 0))


                            .set("sum(termlink pri)/cpt",
                                    ((double)mb.get(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum))
                                            /numConcepts)
                            .set("stddev(termlink pri)", mb.getDoubleFinite(MemoryBudget.Budgeted.ActiveTermLinkPriorityStdDev, 0))


                            .set("sum(tasklink pri)/cpt",
                                    ((double)mb.get(MemoryBudget.Budgeted.ActiveTaskLinkPrioritySum))
                                            /numConcepts)
                            .set("stddev(tasklink pri)", mb.getDoubleFinite(MemoryBudget.Budgeted.ActiveTaskLinkPriorityStdDev, 0))
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
