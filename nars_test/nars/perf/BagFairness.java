package nars.perf;

import java.awt.Color;
import java.util.UUID;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.core.control.SequentialMemoryCycle;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries;
import nars.io.Texts;
import nars.language.Term;
import nars.timeline.Timeline2DCanvas;
import nars.timeline.Timeline2DCanvas.Chart;

/**
 *
 * @author me
 */


public class BagFairness {

    final int bins = 10;
    TimeSeries bin[] = new TimeSeries[bins];
    long total = 0;
            
    public BagFairness(NAR n, int iterations, double insertProb, double removeProb) {

        final float maxConcepts = 1000;
        
        for (int b = bins-1; b >= 0; b--) {
            double percentStart = ((double)b)/bins;
            double percentEnd = ((double)(b+1))/bins;            
            //System.out.println(  + ":\t\t" + amount);
            
            bin[b] = new TimeSeries(percentStart + ".." + percentEnd, Color.getHSBColor(0.2f + 0.5f * (float)percentStart, 0.8f, 0.8f), iterations-1).setRange(0, maxConcepts);
        }
        
        n.event().on(Events.ConceptFire.class, new Observer() {

            @Override
            public void event(Class event, Object[] arguments) {
            }
            
        });
        
        while (n.getTime() < iterations) {
            double p = Math.random();
            if (p < insertProb) {
                float priority = (float)Math.random(); //uniform distribution                
                n.addInput("$" + Texts.n2(priority) + "$ " + new Term(UUID.randomUUID().toString()) + ".");
            }
            p = Math.random();
            if (p < removeProb) {
                //NullItem removed = bag.takeOut();
                //n.memory.
                /*if (removed!=null) {
                    removalPriority(removed.getPriority());
                } */               
            }
            
            double[] d = ((SequentialMemoryCycle)n.memory.conceptProcessor).concepts.getPriorityDistribution(bins);

            int concepts = ((SequentialMemoryCycle)n.memory.conceptProcessor).concepts.size();
            for (int b = 0; b < bins; b++) {
                
                bin[b].push(n.getTime(), (float)d[b] * concepts);
            }
            //((SequentialMemoryCycle)n.memory.conceptProcessor).processConcept();
            n.step(1);
        }
        
        Chart[] charts = new Chart[bins+2];
        int b;
        for (b = 0; b < bins; b++) {
            charts[b] = new Timeline2DCanvas.LineChart(bin[b]);                 
        }        
        charts[b++] = new Timeline2DCanvas.StackedPercentageChart(bin).height(8f);
        charts[b++] = new Timeline2DCanvas.LineChart(bin).height(8f);
        new NWindow("_", new Timeline2DCanvas(charts)).show(800, 800, true);

        //printResults(insertProb, removeProb);
        
    }

    /*protected void removalPriority(float p) {
        int b = (int)Math.floor(p * bins);
        bin[b]++;
        total++;
    }*/
    
//    protected void printResults(double insertProb, double removeProb) {
//        System.out.print(insertProb + ", " + removeProb + ",    ");
//        for (int i = bins-1; i >=0; i--) {
//            double percentStart = ((double)i)/bins;
//            double percentEnd = ((double)(i+1))/bins;
//            double amount = ((double)bin[i]) / ((double)total);
//            //System.out.println( percentStart + ".." + percentEnd + ":\t\t" + amount);
//            System.out.print(amount + ", ");
//        }
//        System.out.println();
//    }
//    
    public static void main(String[] args) {
        NAR n = new DefaultNARBuilder().build();
        
        //AbstractBag<NullItem, CharSequence> b = new DefaultBag<NullItem,CharSequence>(100,1000, n.param().conceptForgetDurations);
        //AbstractBag<NullItem, CharSequence> b = new ContinuousBag<NullItem,CharSequence>(1000, n.param().conceptForgetDurations,true);
        for (double rProb = 0.5; rProb <= 1.0; rProb += 10.10) {
            new BagFairness(n,
                    1500, 1.0, rProb
            );
        }
    }
}
