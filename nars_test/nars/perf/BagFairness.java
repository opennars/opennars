package nars.perf;

import automenta.vivisect.TreeMLData;
import java.awt.Color;
import java.util.ArrayList;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.NAR;
import nars.config.Parameters;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.timeline.LineChart;
import nars.util.Texts;
import nars.storage.Bag;
import automenta.vivisect.timeline.Chart;
import automenta.vivisect.timeline.MultiTimeline;
import automenta.vivisect.timeline.StackedPercentageChart;
import nars.control.DerivationContext;

/**
 **
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
    
    public BagFairness(NAR n, String file, int maxConcepts, int iterationRecordBegin, int iterations) {

        for (int b = 0; b < bins; b++) {
            double percentStart = ((double)b)/bins;
            double percentEnd = ((double)(b+1))/bins;            
            if (percentEnd > 1.0) percentEnd = 1.0;
            
            held[b] = new TreeMLData("Concept: " + Texts.n2(percentStart) + ".." + Texts.n2(percentEnd), Color.getHSBColor(0.2f + 0.7f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, 1.0f);
            
            fired[b] = new TreeMLData("Fired: " + Texts.n2(percentStart) + ".." + Texts.n2(percentEnd), Color.getHSBColor(0.2f + 0.7f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, maxConcepts);
        }

        //TODO use ConceptFire event observer impl
        n.memory.event.on(Events.ConceptFire.class, new EventObserver() {

            @Override
            public void event(Class event, Object[] arguments) {
                if (nextConceptPriority!=-1)
                    throw new RuntimeException("Only supports 1 concept per cycle");
                
                nextConceptPriority = ((DerivationContext)arguments[0]).getCurrentConcept().getPriority();
            }
            
        });
        
        n.addInputFile(file);
        
        double[] d = new double[bins];
        
        while (n.time() < iterations) {

            nextConceptPriority = -1;
                             
            
            
            //((SequentialMemoryCycle)n.memory.conceptProcessor).processConcept();
            n.memory.concepts.peekNext();
            n.cycles(1);
            
            //if (n.memory.param.getTiming() == Timing.Simulation)
            //    n.memory.addSimulationTime(1);
            
            if (n.time() > iterationRecordBegin) {

                if (nextConceptPriority!=-1) {
                    float p = nextConceptPriority;
                    int b = Bag.bin(p, bins-1);

                    fireCount[b]++;                
                }


                int concepts = n.memory.concepts.size();

                n.memory.concepts.getPriorityDistribution(d);
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
                
                final NAR n = new NAR();
                
                ArrayList<Chart> ch = new BagFairness(n, 
                        "nal/Examples/Example-MultiStep-edited.txt", 
                        maxConcepts, /* concepts */
                        numIterations-displayedIterations, numIterations /* iterations */).charts;
                return ch.toArray(new Chart[ch.size()]);
            }            
        }).show(1200, 900, true);
        
        
    }
}
