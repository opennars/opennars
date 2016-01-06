//package nars.evolve;
//
//import nars.NAR;
//import nars.nar.Default;
//import org.encog.ml.data.MLDataPair;
//
//import java.util.*;
//
///**
// *
// * @author me
// */
//
//
//public class ParameterSearch {
//
//    static Map<Integer, Map<String,Integer>> experiments = new HashMap();
//
//    /** scores based on unit test completion success rate and speed */
//    public static double score(String experiment, NAR nar) throws Exception {
//        NALTestPerformance.reset();
//
//        int tests = 200; //all
//        int additionalCycles = 0;
//        int maxCyclesPerTest = 500;
//
///*[emotion.happy0, task.derived1, task.executed.priority.mean2, cycle.cpu_time.mean3, emotion.busy4, rule.ded_2nd_layer_variable_unification5, concept.priority.mean6, cycle.frequency.hz7, rule.ded_2nd_layer_variable_unification_terms8, task.judgment.process9, concept.count10, concept.questions.mean11, task.add_new.priority.mean12, task.immediate_processed13, io.to_memory.ratio14, rule.fire.tasklink.priority.mean15, task.add_new16, rule.tasktermlinks17, rule.contraposition18, concept.new19, rule.fire.tasklinks20, concept.new.complexity.mean21, rule.analogy22, task.derived.priority.mean23, task.question.process24, cycle.frequency_potential.mean.hz25, task.link_to26, task.executed27, concept.beliefs.mean28, task.goal.process29, rule.tasktermlink.priority.mean30, cycle.ram_use.delta_Kb.sampled31, rule.ded_conjunction_by_question32, memory.noveltasks.total33, rule.belief_revision34, id35, time36, absTime37, successes38, error39]
//*/
//        //int[] ins = new int[] { 1, 8, 9, 22, 10, 15 };
//        int[] ins = new int[] { 38, 35 };
//        int[] outs = new int[] {  36 };
//        //int[] ins = null, outs = null;
//
//        NALTestPerformance.NALControlMLDataSet trainingSet = NALTestPerformance.test(nar, tests, maxCyclesPerTest, additionalCycles, ins, outs, 0);
//        //trainingSet.normalize();
//
//        //System.out.println(trainingSet.allFields);
//
//        int totalTests = 0;
//        Map<Integer, Integer> testTimes = new TreeMap();
//
//        System.out.print(experiment + " ");
//
//        for (int i = 0; i < trainingSet.size(); i++) {
//            MLDataPair g = trainingSet.get(i);
//            int test = (int)g.getInput().getData(1);
//            double success = g.getInput().getData(0);
//            int cycles = (int)g.getIdeal().getData(0);
//
//            if ((success == 1.0) && (testTimes.get(test) == null)) {
//                testTimes.put(test, cycles);
//            }
//
//            totalTests = Math.max(test, totalTests);
//        }
//        for (Integer t : testTimes.keySet()) {
//            if (experiments.get(t) == null)
//                experiments.put(t, new TreeMap());
//            experiments.get(t).put(experiment, testTimes.get(t));
//        }
//
//        trainingSet.toCSV("/tmp/" + experiment + ".csv");
//
//        /*
//
//        long totalTestCycles = 0;
//        for (int i : testTimes.values()) {
//            totalTestCycles += i;
//        }
//        int failedTests = totalTests - testTimes.size();
//        double score = 100.0 / (totalTestCycles + maxCyclesPerTest * failedTests);
//                */
//        //System.out.println("score=" + score + ",  total success cycles: " + totalTestCycles + ", failed tests=" + ((double)failedTests) / ((double)totalTests) );
//        //System.out.println(testTimes);
//
//        return 0; //score;
//    }
//
//    public static void report() {
//        List<String> ep = NALTestPerformance.getExamplePaths();
//        Map<String, Integer> bestCount = new TreeMap();
//        Map<String, Integer> worstCount = new TreeMap();
//
//        System.out.println("\nExperiment Comparison:");
//
//        for (int i : experiments.keySet())  {
//            Map<String, Integer> e = experiments.get(i);
//
//
//            //System.out.printMeaning("  " + ep.get(i) + ": ");
//
//            TreeSet<Integer> cyclesUnique = new TreeSet(e.values());
//            int min = cyclesUnique.first();
//            int max = cyclesUnique.last();
//            if (cyclesUnique.size() == 1) {
//                //System.out.println("tie @ " + cyclesUnique.iterator().next() + " cycles");
//            }
//            else if (cyclesUnique.size() > 2) {
//                List<String> best = new ArrayList(4);
//                List<String> worst = new ArrayList(4);
//                for (Map.Entry<String, Integer> es : e.entrySet()) {
//                    int time = es.getValue();
//                    String exp = es.getKey();
//                    if (time == min)  {
//                        best.add(exp);
//                        try {
//                            bestCount.put(exp, bestCount.get(exp) + 1);
//                        }
//                        catch (NullPointerException nn) {
//                            bestCount.put(exp, 1);
//                        }
//                    }
//                    if (time == max) {
//                        worst.add(exp);
//                        try {
//                            worstCount.put(exp, worstCount.get(exp) + 1);
//                        }
//                        catch (NullPointerException nn) {
//                            worstCount.put(exp, 1);
//                        }
//                    }
//                }
//                //System.out.println("range=" + min + ".." + max + " cycles by winners=" + best );
//            }
//            else {
//                //System.out.println("no result");
//            }
//        }
//
//        System.out.println("Winnings:");
//        for (String k : bestCount.keySet()) {
//            System.out.println("  " + k + " " + bestCount.get(k));
//        }
//        System.out.println();
//        System.out.println();
//
//
//        //this isnt helpful since a test can fail it wont even appear in worst, so for now, don't show
//        //System.out.println("worst: " + worstCount);
//    }
//
//    public static void main(String[] args) throws Exception {
//
////        //p.termLinkRecordLength.set(10);
////        experiments.clear();
////        for (int i = 1; i < 15; i++) {
////            NAR a = new NAR(new Default());
////
////            score("termLinkRecordLength_" + String.format("%03d", i), a);
////        }
////        report();
//
//        //p.beliefCyclesToForget.set(50);
//        experiments.clear();
//        for (int i = 2; i < 100; i+=4) {
//            NAR a = new NAR(new Default());
//
//            (a.param).termLinkForgetDurations.set(i);
//            score("beliefCyclesToForget_" + String.format("%03d", i), a);
//        }
//        report();
//
//        //p.conceptCyclesToForget.set(10);
//        experiments.clear();
//        for (int i = 2; i < 20; i++) {
//            NAR a = new NAR(new Default());
//
//            (a.param).conceptForgetDurations.set(i);
//            score("conceptCyclesToForget_" + String.format("%03d", i), a);
//        }
//        report();
//
//        //p.taskCyclesToForget.set(20);
//        experiments.clear();
//        for (int i = 2; i < 40; i+=2) {
//            NAR a = new NAR(new Default());
//
//            (a.param).taskLinkForgetDurations.set(i);
//            score("taskCyclesToForget_" + String.format("%03d", i), a);
//        }
//        report();
//
//
//        //p.termLinkMaxMatched.set(10);
//        experiments.clear();
//        for (int i = 1; i < 13; i++) {
//            NAR a = new NAR(new Default());
//
//            (a.param).termLinkMaxMatched.set(i);
//            score("termLinkMaxMatched_" + String.format("%03d", i), a);
//        }
//        report();
//
//        //p.termLinkMaxReasoned.set(3);
//        experiments.clear();
//        for (int i = 1; i < 13; i++) {
//            NAR a = new NAR(new Default());
//
//            (a.param).conceptTaskTermProcessPerCycle.set(i);
//            score("termLinkMaxReasoned_" + String.format("%03d", i), a);
//        }
//        report();
//
//
//
////
////
////
////        experiments.clear();
////        for (int i = 0; i < 15; i++) {
////            NAR a = new Default().build();
////
////            //a.param().contrapositionPriority.set(i);
////            //System.out.println("contraposition priority=" + i + " = " + score(a));
////
////            a.param().taskCyclesToForget.set(i);
////            score("taskForgettingRate_" + String.format("%03d", i), a);
////
////            //a.param().maxReasonedTermLink.set(i);
////            //score("maxReasonedTermLink_" + i, a);
////        }
////        report();
////
////
////
////
////        experiments.clear();
////        for (int i = 0; i < 100; i+=5) {
////            NAR a = new Default().build();
////
////            //a.param().contrapositionPriority.set(i);
////            //System.out.println("contraposition priority=" + i + " = " + score(a));
////
////            a.param().beliefCyclesToForget.set(i);
////            score("beliefForgettingRate_" + String.format("%03d", i), a);
////
////            //a.param().maxReasonedTermLink.set(i);
////            //score("maxReasonedTermLink_" + i, a);
////        }
////        report();
//
//    }
// }
