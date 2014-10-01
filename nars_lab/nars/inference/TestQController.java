package nars.inference;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import nars.core.NAR;
import nars.core.Param;
import nars.core.build.DiscretinuousBagNARBuilder;
import nars.io.TextInput;

/**
 * Dynamic inference controller experiment, using QLearning
 * 
 * 
 * Experiment:
<sseehh_> normally, concept priority drops
<sseehh_> to like 0.03
<sseehh_> average concept priority
<sseehh_> this applies every N cycles
<sseehh_> so its looking at the average over the waiting period
<sseehh_> priority may spike for a few concepts, but this affects little
<sseehh_> if it can raise the avg concept priority, then it has significantly affected inference behavior
 */
public class TestQController {
    
    final static String cpm = "concept.priority.mean";
    final static String td = "task.derived";
    final static String cpv = "concept.priority.variance";
    final static String cph0 = "concept.priority.hist.0";
    final static String cph1 = "concept.priority.hist.1";
    final static String cph2 = "concept.priority.hist.2";
    final static String cph3 = "concept.priority.hist.3";
    final static String nt = "task.novel.total";
    
    
    public static class TestController extends QController {

                
        private double conceptNewMean;
        private double taskDerivedMean;
        
        final int minCycleToForget = 2;
        final int maxCycleToForget = 40;
        
        public TestController(NAR n, int period) {
            super(n, period);
                        
            
            Param p = nar.param();
            
            add(new NControlSensor(p.conceptForgetDurations, 3));
            //add(new NControlSensor(p.beliefCyclesToForget, 2));
            //add(new NControlSensor(p.taskCyclesToForget, 2));
            //add(new NControlSensor(p.termLinkMaxMatched, 2));
            
            add(new EventValueControlSensor(nar, cpm, 0, 1, 7));            
            add(new EventValueControlSensor(nar, cph0, 0, 1, 3));
            add(new EventValueControlSensor(nar, cph1, 0, 1, 3));
            add(new EventValueControlSensor(nar, cph1, 0, 1, 3));
            add(new EventValueControlSensor(nar, cph1, 0, 1, 3));
            add(new EventValueControlSensor(nar, cpv, 5, 4, 0.0001));
            add(new EventValueControlSensor(nar, td, 5, 4, 0.0001) {
                @Override public double get() {
                    return taskDerivedMean = super.get();
                }                
            });            
            add(new EventValueControlSensor(nar, "concept.new", 5, 2, 0.0001) {
                @Override public double get() {
                    return conceptNewMean = super.get();
                }                
            });
            add(new EventValueControlSensor(nar, "task.judgment.process", 5, 8, 0.0001));
            add(new EventValueControlSensor(nar, "task.question.process", 5, 8, 0.0001));
            
            init(3);
            //q.brain.setUseBoltzmann(true);
            //q.brain.setRandActions(0.25);
        }

        @Override
        protected int[] getFeedForwardLayers(int inputSize) {
            //return new int[ (int)Math.ceil(inputSize * 0.5) ];
            //return new int[ (int)Math.ceil(inputSize * 2) ];
            
            //return new int[ ] { 18 }; //fixed # of hidden
            return new int[] { 20 };
        }

        @Override
        protected void act(int action) {
            Param p = nar.param();
            
            
            switch (action) {
                case 0: 
                    p.conceptForgetDurations.set(5);
                    break;
                case 1: 
                    p.conceptForgetDurations.set(10);
                    break;
                case 2:
                    p.conceptForgetDurations.set(15);
                    break;
            }
            
            
//            switch (action) {
//                case 0: p.conceptCyclesToForget.set(14);  break;
//                case 1: p.conceptCyclesToForget.set(10);   break;
//                case 2: p.taskCyclesToForget.set(24);  break;
//                case 3: p.taskCyclesToForget.set(20);   break;
//                case 4: p.beliefCyclesToForget.set(54);  break;
//                case 5: p.beliefCyclesToForget.set(50);   break;
//                case 6: p.termLinkMaxMatched.set(14);  break;
//                case 7: p.termLinkMaxMatched.set(10);   break;
//                case 8: 
//                    //final input: do nothing                    
//                    break;
//            }
        }        
        
        @Override
        public double reward() {
            //maximize concept priority
            //return conceptPriority;
            // + conceptNewMean;
            
            return conceptNewMean + taskDerivedMean + 1* nar.memory.logic.d("task.solution.best");            
        }


    }

    public static void input(String example, NAR... n) {
        for (NAR x : n)
            x.addInput(new TextInput(getExample(example)));             
    }
    
    public static NAR newNAR() {
        //return new DefaultNARBuilder().build();        
        return new DiscretinuousBagNARBuilder().setConceptBagSize(8192).build();        
    }
    
    public static void main(String[] arg) {
          
        int controlPeriod = 2;
        
        NAR n = newNAR(); 
        TestController qn = new TestController(n, controlPeriod);
        qn.setActive(false);
        
        //m has controller deactivated
        NAR m = newNAR();
        TestController qm = new TestController(m, controlPeriod);
        qm.setActive(false);

        //random policy
        NAR r = newNAR();
        TestController qr = new TestController(r, controlPeriod) {

            @Override
            protected void act(int ignored) {
                int action = (int)(Math.random() * getNumActions());
                super.act(action); 
            }
            
        };
        qr.setActive(false);
        
        double mm = 0, nn = 0, rr = 0;
        int displayCycles = 1000;
        double[] nAction = new double[qn.getNumActions()];
        long startupPeriod = 2000;
        int resetPeriod = 50000;
        double avgCycleToForget = 0;
        int time = 0;
        while (true ) {
            

            if (time % resetPeriod == 0) {
                System.out.println("RESET");
                n.reset();
                m.reset();
                r.reset();
                
                input("nal/Examples/Example-MultiStep-edited.txt", n, m, r);
                //input("nal/test/nars_multistep_1.nal", n, m, r);
                //input("nal/test/nars_multistep_2.nal", n, m, r);                
            }
            
            if (time > startupPeriod) {
                qr.setActive(true);
                qn.setActive(true);
                double[] oqn = qn.getOutput();
                if (oqn!=null) {
                    for (int i = 0; i < nAction.length; i++)
                        nAction[i] += oqn[i] / displayCycles;
                }                
             
                
            }
            
            n.step(1);            
            m.step(1);
            r.step(1);
            
            avgCycleToForget += ((double)n.param().conceptForgetDurations.get()) / displayCycles;
            mm += qm.reward();
            nn += qn.reward();
            rr += qr.reward();
            
                        
            if (time % displayCycles == 0) {
                System.out.print(
                        //((nn-mm)/((nn+mm)/2.0)*100.0) + " , " + 
                                time + ", " +
                                df.format(mm) + " , " + df.format(nn) + " , " + df.format(rr) + " , ");
                          
                //System.out.println();
                System.out.print(avgCycleToForget + ", ");
                printCSVLine(System.out, nAction);
                
                mm = nn = rr = avgCycleToForget = 0;
                Arrays.fill(nAction, 0);
            }
            time++;
        }
                
    }

    
    
    
    
    
    protected final static DecimalFormat df = new DecimalFormat("#.###");
    
    public static void printCSVLine(PrintStream out, List<String> o) {
        StringJoiner line = new StringJoiner(",", "", "");
        int n = 0;
        for (String x : o) {
            line.add(x + "_" + (n++));
        }
        out.println(line.toString());
    }

    public static void printCSVLine(PrintStream out, double[] a) {
        StringJoiner line = new StringJoiner(",", "", "");        
        for (double x : a)
            line.add(df.format(x));
        out.println(line.toString());
    }
        
    
    protected static Map<String, String> exCache = new HashMap(); //path -> script data
    
    /** duplicated from NALTest.java  -- TODO use a comon copy of this method */
    public static String getExample(String path) {
        try {
            String existing = exCache.get(path);
            if (existing!=null)
                return existing;
            
            StringBuilder  sb  = new StringBuilder();
            String line;
            File fp = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(fp));
            while ((line = br.readLine())!=null) {
                sb.append(line).append("\n");
            }
            existing = sb.toString();
            exCache.put(path, existing);
            return existing;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
    
}
