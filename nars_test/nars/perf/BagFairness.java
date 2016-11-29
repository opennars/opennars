package nars.perf;

import automenta.vivisect.TreeMLData;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.core.control.DefaultAttention;
import nars.entity.Concept;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.timeline.LineChart;
import nars.io.Input;
import nars.io.Texts;
import nars.language.Term;
import nars.storage.Bag;
import automenta.vivisect.timeline.Chart;
import automenta.vivisect.timeline.MultiTimeline;
import automenta.vivisect.timeline.StackedPercentageChart;
import nars.storage.Memory.Timing;
import nars.core.control.FireConcept;
import nars.storage.LevelBag;

/**
 *
 * @author me
 */


public class BagFairness {

    final int bins = 10;
    TreeMLData fired[] = new TreeMLData[bins];
    TreeMLData[] held = new TreeMLData[bins];
    float fireCount[] = new float[bins];
    long total = 0;
    
    private final ArrayList<Chart> charts;
            
    float nextConceptPriority;
    
    public BagFairness(NAR n, Input input, int maxConcepts, int iterationRecordBegin, int iterations) {

        for (int b = 0; b < bins; b++) {
            double percentStart = ((double)b)/bins;
            double percentEnd = ((double)(b+1))/bins;            
            if (percentEnd > 1.0) percentEnd = 1.0;
            
            held[b] = new TreeMLData("Concept: " + Texts.n2(percentStart) + ".." + Texts.n2(percentEnd), Color.getHSBColor(0.2f + 0.7f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, 1.0f);
            
            fired[b] = new TreeMLData("Fired: " + Texts.n2(percentStart) + ".." + Texts.n2(percentEnd), Color.getHSBColor(0.2f + 0.7f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, maxConcepts);
        }

        //TODO use ConceptFire event observer impl
        n.event().on(Events.ConceptFire.class, new EventObserver() {

            @Override
            public void event(Class event, Object[] arguments) {
                if (nextConceptPriority!=-1)
                    throw new RuntimeException("Only supports 1 concept per cycle");
                
                nextConceptPriority = ((FireConcept)arguments[0]).getCurrentConcept().getPriority();
            }
            
        });
        
        n.addInput(input);
        
        double[] d = new double[bins];
        
        while (n.time() < iterations) {

            nextConceptPriority = -1;
                             
            
            
            //((SequentialMemoryCycle)n.memory.conceptProcessor).processConcept();
            n.memory.concepts.sampleNextConcept();
            n.step(1);
            
            if (n.memory.param.getTiming() == Timing.Simulation)
                n.memory.addSimulationTime(1);
            
            if (n.time() > iterationRecordBegin) {

                if (nextConceptPriority!=-1) {
                    float p = nextConceptPriority;
                    int b = Bag.bin(p, bins-1);

                    fireCount[b]++;                
                }


                int concepts = ((DefaultAttention)n.memory.concepts).concepts.size();

                ((DefaultAttention)n.memory.concepts).concepts.getPriorityDistribution(d);
                for (int b = 0; b < bins; b++) {

                    held[b].add((int)n.time(), (float)d[b]);
                    fired[b].add((int)n.time(), fireCount[b]);
                }
            }
            
            
        }
        
        charts = new ArrayList();

        /*for (b = 0; b < bins; b++) {
            charts[b] = new TimelineVis.LineChart(held[b]);                 
        } 8*/       
        
        charts.add(new LineChart(held).height(8f));

        charts.add(new StackedPercentageChart(held).height(8f));

        charts.add(new StackedPercentageChart(fired).height(8f));
        
        
        
        //new NWindow("_", new TimelineVis(charts)).show(800, 800, true);

        //printResults(insertProb, removeProb);
        
    }

    /*protected void removalPriority(float p) {
        int b = (int)Math.floor(p * bins);
        held[b]++;
        total++;
    }*/
    
//    protected void printResults(double insertProb, double removeProb) {
//        System.out.print(insertProb + ", " + removeProb + ",    ");
//        for (int i = bins-1; i >=0; i--) {
//            double percentStart = ((double)i)/bins;
//            double percentEnd = ((double)(i+1))/bins;
//            double amount = ((double)held[i]) / ((double)total);
//            //System.out.println( percentStart + ".." + percentEnd + ":\t\t" + amount);
//            System.out.print(amount + ", ");
//        }
//        System.out.println();
//    }
//    
    
    public static class RandomTermInput implements Input<String> {
        
        private final double minPriority;
        private final double maxPriority;
        private final int numTerms;
        private final double inheritanceProb;
        private final double similarityProb;
        private final double productProb;
        private final int numInputs;
        private int inputs;

        public RandomTermInput(int numTerms, int numInputs, double inheritanceProb, double similarityProb, double productProb, double minPriority, double maxPriority) {
            this.numTerms = numTerms;
            this.numInputs = numInputs;
            this.inheritanceProb = inheritanceProb;
            this.similarityProb = similarityProb;
            this.productProb = productProb;
            this.minPriority = minPriority;
            this.maxPriority = maxPriority;
            this.inputs = 0;
            
        }

        @Override public String next() throws IOException {
            
            if (inputs < numInputs) {
                
                //uniform distribution
                double pr = Math.random() * (maxPriority-minPriority) + minPriority;
                float priority = (float)pr;                               
                
                double tp = inheritanceProb + similarityProb + productProb;                
                double s = Math.random() * tp;
                s -= inheritanceProb; if (s < 0) {
                    return "$" + Texts.n2(priority) + "$ <" + randomTerm() + " --> " + randomTerm() + ">.";
                }
                s -= similarityProb; if (s < 0) {
                    return "$" + Texts.n2(priority) + "$ <" + randomTerm() + " <-> " + randomTerm() + ">.";
                }
                s -= productProb; if (s < 0) {
                    return "$" + Texts.n2(priority) + "$ <(*," + randomTerm() + "," + randomTerm() + ") --> " + randomTerm() + ">.";
                }
                
                inputs++;
                
            }
            return null;
        }

        private Term randomTerm() {
            int t = (int)(Math.random() * numTerms);
            return new Term("t" + t);
        }

        @Override public boolean finished(boolean stop) { return false; }
    }
    
    public static void main(String[] args) {
        Parameters.DEBUG = true;
        
        int inputs = 100;
        int maxConcepts = 1000;
        float inputRate = 0.2f;
        int displayedIterations = 600;
        int numIterations = 600;
        float minPri = 0.1f;
        float maxPri = 1.0f;

        new NWindow("_", new MultiTimeline(1, 1, 1) {

            @Override public Chart[] getCharts(int experiment) {
                
                final NAR n = new NAR(new Default() {

                    @Override public Bag<Concept, Term> newConceptBag() {                        
                        /*if (experiment == 0)*/ {
                            //return new DelayBag(param.conceptForgetDurations, getConceptBagSize());
                            //return new LevelBag2(getConceptBagSize(), 20);
                            //return new AdaptiveContinuousBag(getConceptBagSize());
                            return new LevelBag(getConceptBagSize(), 20);
                        }
                        /*else { //if (experiment == 1) {
                            
                            //return new LevelBag(getConceptBagSize(), 100);
                            return new FairDelayBag(param.conceptForgetDurations, getConceptBagSize());
                        }*/
                    }

                }.setConceptBagSize(maxConcepts));

                
                ArrayList<Chart> ch = new BagFairness(n, 
                        new RandomTermInput(8, inputs, 0.01, 0.5, 0.5, minPri, maxPri), 
                        maxConcepts, /* concepts */
                        numIterations-displayedIterations, numIterations /* iterations */).charts;
                return ch.toArray(new Chart[ch.size()]);
            }            
        }).show(1200, 900, true);
        
        
    }
}
