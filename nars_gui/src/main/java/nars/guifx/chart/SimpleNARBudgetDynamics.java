//package nars.guifx.chart;
//
//import javafx.beans.property.ObjectProperty;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.beans.property.StringProperty;
//import javafx.scene.Scene;
//import javafx.scene.layout.BorderPane;
//import nars.NAR;
//import nars.Narsese;
//import nars.guifx.NARfx;
//import nars.guifx.util.POJOPane;
//import nars.nar.Default;
//import nars.task.Task;
//import nars.term.Term;
//import nars.util.meter.MemoryBudget;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//
//import static javafx.application.Platform.runLater;
//import static nars.guifx.NARfx.scrolled;
//
///**
// * Created by me on 8/12/15.
// */
//public class SimpleNARBudgetDynamics {
//
//    static String[] abFwdClosed =    { "<a-->b>." };
//    static String[] abBidiClosed =   { "<a-->b>.", "<b-->a>." };
//    static String[] abcClosed =      { "<a-->b>.", "<b-->c>." };
//
//    static final Map<String,Term> terms = new HashMap();
//
//    static Narsese np = Narsese.the();
//
//    public static Term term(String s) {
//        Term x = terms.get(s);
//        if (x == null) {
//            terms.put(s, x = np.term(s));
//        }
//        return x;
//    }
//
//    static final BorderPane x = new BorderPane();
//    static final MemoryBudget mb = new MemoryBudget(); //one thread at a time
//
//    @SuppressWarnings("HardcodedFileSeparator")
//    static final BiConsumer<NAR,Consumer<NAR>> update = (d, execution) -> new NARui(d)
//
////            .then(n -> {
////                //n.frame(preCycles);
////            })
//            .meter( (metrics, nar) -> metrics.set("# concepts", nar.index().size()))
//            .meter( (metrics, nar) -> {
//
//                mb.clear();
//                mb.update(nar);
//
//                System.out.println(nar.time());
//
//                double conPriSum = mb.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum);
//                long numConcepts = mb.getLong(MemoryBudget.Budgeted.ActiveConcepts);
//                if (numConcepts == 0) numConcepts = 1;
//
//                metrics
//                        .set("a pri", nar.memory.conceptPriority(term("a"), 0.0f))
//                        .set("b pri", nar.memory.conceptPriority(term("b"), 0.0f))
//                        .set("c pri", nar.memory.conceptPriority(term("c"), 0.0f))
//                        .set("<a-->b> pri", nar.memory.conceptPriority(term("<a-->b>"), 0.0f))
//                        //.set("<b-->a> pri", nar.memory.conceptPriority(term("<b-->a>")))
//                        .set("<b-->c> pri", nar.memory.conceptPriority(term("<b-->c>"), 0.0f))
//                        .set("<a<->b> pri", nar.memory.conceptPriority(term("<a<->b>"), 0.0f))
//                        .set("<a<->c> pri", nar.memory.conceptPriority(term("<a<->c>"), 0.0f))
//                        .set("mean(concept pri)", conPriSum / numConcepts) // .getActivePriorityPerConcept(true, false, false)
//                ;
//            })
//            .meter( (metrics, nar) -> {
//
//
//                //double conPriSum = mb.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum);
//                long numConcepts = mb.getLong(MemoryBudget.Budgeted.ActiveConcepts);
//                if (numConcepts == 0) numConcepts = 1;
//
//                metrics
//
//                        .set("stddev(concept pri)", mb.getDoubleFinite(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev, 0))
//
//
//                        .set("sum(termlink pri)/cpt",
//                                ((double)mb.get(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum))
//                                        /numConcepts)
//                        .set("stddev(termlink pri)", mb.getDoubleFinite(MemoryBudget.Budgeted.ActiveTermLinkPriorityStdDev, 0))
//
//
//                        .set("sum(tasklink pri)/cpt",
//                                ((double)mb.get(MemoryBudget.Budgeted.ActiveTaskLinkPrioritySum))
//                                        /numConcepts)
//                        .set("stddev(tasklink pri)", mb.getDoubleFinite(MemoryBudget.Budgeted.ActiveTaskLinkPriorityStdDev, 0))
//                ;
//            })
//                /*.meter("ConceptPriorityMean", (nar) -> {
//                    return nar.memory.getActivePriorityPerConcept(true, false, false);
//                })*///                .meter("A pri", (nar) -> {
////                    return nar.memory.conceptPriority(Atom.the("a"));
////                })
////                /*.meter("TermLinkPriorityMean", (nar) -> {
////                    return nar.memory.getActivePriorityPerConcept(false, true, false);
////                })
////                .meter("TaskLinkPriorityMean", (nar) -> {
////                    return nar.memory.getActivePriorityPerConcept(false, false, true);
////                })*/
////                .meter("Concepts", (nar) -> {
////                    return nar.memory.numConcepts(true, false);
////                })
//
//            .then(execution)
//            .viewAll((c) -> {
//                runLater(() -> {
//                            x.setCenter(scrolled(c));
//                            x.layout();
//                        });
//                System.out.println("done");
//            });
//
//    public static final class Variables {
//        public final StringProperty input = new SimpleStringProperty("");
//        public final ObjectProperty<NAR> nar = new SimpleObjectProperty(
//                new Default(1000, 1, 2, 2)
//        );
//    }
//
//    public static void main(String[] args) {
//
//
//        int cycles = 1256;
//
//
//        float pri = 0.5f;
//        float dur = 0.5f;
//        float qua = 0.5f;
//
//
//
//
//        //Solid d = new Solid(1,256, 1, 1, 1, 3);
//        //Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 1.0f;
//        //Global.TASKLINK_FORGETTING_EXTRA_DEPTH = 1.0f;
//        //Global.TERMLINK_FORGETTING_EXTRA_DEPTH = 1.0f;
//        //d.conceptActivationFactor.set(0.5f);
//        //d.setCyclesPerFrame(1);
//        //d.duration.set(5);
//        //d.level(3);
//        //d.getParam().conceptForgetDurations.set(2);
//        //d.taskLinkForgetDurations.set(16);
//        //d.termLinkForgetDurations.set(16);
//
//
//
//
//        Consumer<NAR> execution = n -> {
//
//            //n.stdout();
//
//            String[] x =
//                    //abFwdClosed;
//                    //abBidiClosed;
//                    abcClosed;
//            for (String aX : x) {
//
//                Task t = n.task(aX);
//                t.getBudget().setPriority(pri);
//                t.getBudget().setQuality(qua);
//                t.getBudget().setDurability(dur);
//
//                n.input(t);
//            }
//
//            n.frame(cycles);
//
//            n.forEachConcept(System.out::println);
//
//        };
//
//
//
//
//
//        Variables vars = new Variables();
//
//        NARfx.run((a,b) -> {
//
//
//            x.setLeft(new POJOPane<>(vars));
//
//
//
//            NAR d = vars.nar.get(); //new Default2(512, 1, 1, 1); //Equalized(1024, 1, 3);
//            //Default d = new NewDefault().setInternalExperience(null);
//            //d.memory.conceptForgetDurations.set(8);
//            //d.getParam().conceptForgetDurations.set(1);
//            //d.getParam().termLinkForgetDurations.set(1);
//            //d.getParam().taskLinkForgetDurations.set(1);
//            //d.getParam().duration.set(10);
//            d.trace();
//
//            update.accept(d, execution);
//
//
//
//            b.setScene(new Scene(x, 800, 600));
//            b.show();
//        });
//
//
//
//
//    }
//
//
// }
