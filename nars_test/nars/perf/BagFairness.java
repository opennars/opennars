package nars.perf;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.core.control.DefaultAttention;
import nars.entity.Concept;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries;
import nars.gui.output.timeline.LineChart;
import nars.io.Input;
import nars.io.Texts;
import nars.language.Term;
import nars.storage.Bag;
import nars.gui.output.timeline.Chart;
import nars.gui.output.timeline.MultiTimeline;
import nars.gui.output.timeline.StackedPercentageChart;
import nars.storage.DelayBag;
import nars.storage.FairDelayBag;

/**
 *
 * @author me
 */


public class BagFairness {

    final int bins = 10;
    TimeSeries fired[] = new TimeSeries[bins];
    TimeSeries[] held = new TimeSeries[bins];
    float fireCount[] = new float[bins];
    long total = 0;
    
    private final ArrayList<Chart> charts;
            
    float nextConceptPriority;
    
    public BagFairness(NAR n, Input input, int maxConcepts, int iterations) {

        for (int b = 0; b < bins; b++) {
            double percentStart = ((double)b)/bins;
            double percentEnd = ((double)(b+1))/bins;            
            if (percentEnd > 1.0) percentEnd = 1.0;
            
            held[b] = new TimeSeries("Concept: " + Texts.n2(percentStart) + ".." + Texts.n2(percentEnd), Color.getHSBColor(0.2f + 0.7f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, 1.0f);
            
            fired[b] = new TimeSeries("Fired: " + Texts.n2(percentStart) + ".." + Texts.n2(percentEnd), Color.getHSBColor(0.2f + 0.7f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, maxConcepts);
        }

        
        n.event().on(Events.ConceptFire.class, new Observer() {

            @Override
            public void event(Class event, Object[] arguments) {
                if (nextConceptPriority!=-1)
                    throw new RuntimeException("Only supports 1 concept per cycle");
                
                nextConceptPriority = ((Concept)arguments[0]).getPriority();
            }
            
        });
        
        n.addInput(input);
        
        double[] d = new double[bins];
        
        while (n.time() < iterations) {

            nextConceptPriority = -1;
                                
            
            ///((SequentialMemoryCycle)n.memory.conceptProcessor).processConcept();
            n.step(1);
            n.memory.addSimulationTime(1);
            
            if (nextConceptPriority!=-1) {
                float p = nextConceptPriority;
                int b = Bag.bin(p, bins-1);
                
                fireCount[b]++;                
            }

            
            int concepts = ((DefaultAttention)n.memory.concepts).concepts.size();
            
            ((DefaultAttention)n.memory.concepts).concepts.getPriorityDistribution(d);
            for (int b = 0; b < bins; b++) {
                
                held[b].push(n.time(), (float)d[b]);
                fired[b].push(n.time(), fireCount[b]);
            }
            
            
        }
        
        charts = new ArrayList();

        /*for (b = 0; b < bins; b++) {
            charts[b] = new Timeline2DCanvas.LineChart(held[b]);                 
        } 8*/       
        
        charts.add(new LineChart(held).height(8f));

        charts.add(new StackedPercentageChart(held).height(8f));

        charts.add(new StackedPercentageChart(fired).height(8f));
        
        
        
        //new NWindow("_", new Timeline2DCanvas(charts)).show(800, 800, true);

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
        private final double inputProb;
        private final double minPriority;
        private final double maxPriority;
        private final int numTerms;
        private final double inheritanceProb;
        private final double similarityProb;
        private final double productProb;

        public RandomTermInput(int numTerms, double inputProb, double inheritanceProb, double similarityProb, double productProb, double minPriority, double maxPriority) {
            this.numTerms = numTerms;
            this.inputProb = inputProb;
            this.inheritanceProb = inheritanceProb;
            this.similarityProb = similarityProb;
            this.productProb = productProb;
            this.minPriority = minPriority;
            this.maxPriority = maxPriority;
            
        }

        @Override public String next() throws IOException {
            double p = Math.random();
            if (p < inputProb) {
                
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
        
        int maxConcepts = 200;
        float inputRate = 0.2f;

        new NWindow("_", new MultiTimeline(2, 2, 1) {

            @Override public Chart[] getCharts(int experiment) {
                
                final NAR n = new Default() {

                    @Override public Bag<Concept, Term> newConceptBag() {                        
                        if (experiment == 0) {
                            return new DelayBag(param.conceptForgetDurations, getConceptBagSize());
                            //return new LevelBag(getConceptBagSize(), 100, 0);
                            //return new AdaptiveContinuousBag(getConceptBagSize());
                        }
                        else { //if (experiment == 1) {
                            //return new CurveBag(getConceptBagSize(), true);
                            //return new LevelBag(getConceptBagSize(), 100);
                            return new FairDelayBag(param.conceptForgetDurations, getConceptBagSize());
                        }
                    }

                }.simulationTime().setConceptBagSize(maxConcepts).build();

                
                ArrayList<Chart> ch = new BagFairness(n, 
                        new RandomTermInput(8, inputRate, 0.01, 0.5, 0.5, 0.1f, 1.0), 
                        maxConcepts, /* concepts */
                        1500 /* iterations */).charts;
                return ch.toArray(new Chart[ch.size()]);
            }            
        }).show(1200, 900, true);
        
        
    }
}
