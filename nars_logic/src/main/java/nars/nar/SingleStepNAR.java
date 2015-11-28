package nars.nar;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import nars.Global;
import nars.nal.Deriver;
import nars.nal.SimpleDeriver;
import nars.nal.TaskRule;
import nars.task.flow.FIFOTaskPerception;
import nars.time.FrameClock;
import nars.util.db.InfiniPeer;
import nars.util.meter.DerivationGraph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SingleStepNAR extends Default {

    static DerivationGraph derivations = new DerivationGraph(false, false);

    static Multimap<TaskRule, DerivationGraph.PremiseKey> ruleDerivations =
            Multimaps.newMultimap(Global.newHashMap(1024),
                    () -> Global.newHashSet(4));

//    static Multimap<DerivationGraph.PremiseKey, TaskRule> derivationRules =
//            Multimaps.newMultimap(new FasterHashMap(1024),
//                    () -> new HashSet());

    /*static DirectedMultigraph rulegraph = new DirectedMultigraph((a, b)->{
        return a + ":" + b;
    });*/


//    static {
//        if (Global.DEBUG_DERIVATION_GRAPH) {
//            Runtime.getRuntime().addShutdownHook(new Thread(new RuleReport()));
//        }
//    }

    @Override
    protected SimpleDeriver getDeriver() {
//        if (Global.DEBUG_DERIVATION_GRAPH) {
//
//
//            return new SimpleDeriver(SimpleDeriver.standard) {
//
//                @Override
//                public Stream<Task> forEachRule(RuleMatch match) {
//
//                    //record an empty derivation, in case nothing is returned in the stream
//                    //allowing us to see what is mising
//                    derivations.add(match.premise /* none */);
//
//                    ;
//                    Stream<Task> s = super.forEachRule(match).peek(t -> {
//                        DerivationGraph.DerivationPattern dd =
//                                derivations.add(match.premise, t);
//                        ruleDerivations.put(match.rule, dd.key);
//                        //derivationRules.put(dd.key, match.rule);
//                    });
//
//                    return s;
//                }
//            };
//        }
//        else {
            return new SimpleDeriver();
        //}
    }


    public SingleStepNAR() {
        super(128, 2, 2, 3, new FrameClock());


        Global.DEBUG = true;

//        memory.eventConceptProcess.on((p) -> {
//           derivations.add(p)
//        });

    }

    @Override
    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                task -> task.isInput() /* allow only input tasks*/,
                task -> process(task)
        );
        return input;
    }

    private static class RuleReport implements Runnable {
        @Override
        public void run() {

            try {
                {
                    PrintStream out = newReportStream("derivations.aggregate.txt");

                    derivations.premiseResult.forEach((k, v) -> {

                        out.println(k);// + " " v.actual.size() +

                        for (DerivationGraph.TaskResult t : v.actual) {
                            out.println('\t' + t.key);
                        }
                        out.println();

                    });

                    out.println();
                    out.println("Rule -> Derivations:");
                    for (TaskRule t : ruleDerivations.keySet()) {
                        Collection<DerivationGraph.PremiseKey> c = ruleDerivations.get(t);
                        out.println(c.size() + "\t" + t);
                        c.forEach(x -> {
                            out.print(' ');
                            out.print(x + " ");
                        });
                    }
                    out.close();
                }
                {

                    PrintStream out = newReportStream("derivations.unused.txt");

                    TreeSet<TaskRule> used = new TreeSet();
                    TreeSet<TaskRule> unused = new TreeSet();
                    for (TaskRule t : Deriver.standard) {
                        Collection<DerivationGraph.PremiseKey> x = ruleDerivations.get(t);
                        if (x == null || x.isEmpty()) {
                            unused.add(t);
                        } else {
                            used.add(t);
                        }
                    }


                    //organize by assigned NAL level
                    Set<TaskRule>[] unusedCounts = new Set[9];
                    Set<TaskRule>[] all = new Set[9];
                    for (int i = 0; i < 9; i++) {
                        unusedCounts[i] = new TreeSet();
                        all[i] = new TreeSet();
                    }

                    used.forEach(p -> all[p.nal()].add(p));
                    unused.forEach(p -> all[p.nal()].add(p));

                    unused.forEach(p -> unusedCounts[p.nal()].add(p));

                    for (int i = 0; i < 9; i++) {
                        int us = all[i].size();
                        int un = unusedCounts[i].size();
                        if (us == 0) {
                            out.println("\t NAL" + i + " has 0 specific rules\n");
                            continue;
                        }
                        float p = (1f-(((float) un) / (us))) * 100.0f;
                        out.println("\t NAL" + i + " Tested " + (us - un) + '/' + (us) + " (" +
                                Math.ceil((int) p) + "% tested, " + un + " not tested): ");
                        final int finalI = i;
                        all[i].forEach(r -> {
                            boolean tested = !unusedCounts[finalI].contains(r);

                            out.print("\t\t");
                            out.print(tested ? "YES\t" : " NO\t");
                            out.print(r);

//                            ///----------------AUTOTEST
//                            try {
//                                RuleTest rt = RuleTest.from(r);
//                                rt.run(false);
//                                TestNAR.Report report = rt.getReport();
//                                out.println(report);
//                            }
//                            catch (Exception e) {
//                                out.println(e);
//                            }
//                            //----------

                            out.println();
                        });
                        out.println();
                    }

                    out.println();
                    out.println("Unused rules:");
                    unused.forEach(r -> out.println("\t" + r));
                    out.println("Total Unused: " + unused.size());
                    out.println();


                    out.close();
                }
                {

                    PrintStream out = newReportStream("derivations.gml");
                    derivations.print(new OutputStreamWriter(out));
                    out.close();
                }


                } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        public static PrintStream newReportStream(String file) throws FileNotFoundException {
            String path = InfiniPeer.getTempDir() + '/' + file;

            System.out.println("Saving results to: " + path);

            return new PrintStream(new FileOutputStream(path));
        }
    }
}
