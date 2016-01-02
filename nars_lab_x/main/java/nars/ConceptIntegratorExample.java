/*
 * Copyright (C) 2014 sue
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.timeline;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.AxisPlot;
import automenta.vivisect.timeline.BarChart;
import automenta.vivisect.timeline.TimelineVis;
import nars.core.Events;
import nars.core.Events.ConceptFire;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.model.Default;
import nars.nal.AbstractObserver;
import nars.nal.entity.Concept;
import nars.util.NARTrace;
import nars.util.TreeMLData;

/**
 *
 */
public class ConceptIntegratorExample extends TimelineExample {
    
    public static class ConceptIntegrator extends AbstractObserver {

        int historySize;
        public final int cycleWindow;

        public final Map<Concept,ConceptActivity> concepts = new HashMap();
        private final NAR nar;
        
        public class ConceptActivity {
            public TreeMLData meanPriority;
            
            private final Concept concept;
            
            float priorityAccumulator = 0;
            float totalPriority = 0;

            
            public ConceptActivity(Concept c) {
                this.concept = c;
                meanPriority = new TreeMLData(c.name().toString() + " ~pri", Video.getColor(c.hashCode(), 0.5f, 0.85f), historySize);
            }

            public ConceptActivity cycle(long n) {
                float p = concept.getPriority();
                priorityAccumulator += p;
                totalPriority += p;
                
                if (n % cycleWindow == (cycleWindow-1)) {
                    float ap = priorityAccumulator / cycleWindow;
                    
                    for (long i = n - cycleWindow + 1; i <= n; i++) {
                        if (i < 0) continue;
                        //TODO linear interpolate
                        meanPriority.add((int)i, ap);
                    }
                    
                    priorityAccumulator = 0;
                }
                
                return this;
            }

            public float getTotalPriority() {
                return totalPriority;
            }
                        
        }
        
        public ConceptIntegrator(NAR n, int cycleWindow, int historySize) {
            super(n, true, Events.CycleEnd.class, Events.ConceptFire.class);
            this.cycleWindow = cycleWindow;
            this.nar = n;
            this.historySize = historySize;
        }

        
        @Override
        public void event(Class event, Object[] arguments) {
            if (event == CycleEnd.class) {
                final long t = nar.time();
                
                for (Concept c : nar.memory.concepts) {
                    
                    ConceptActivity ca = concepts.get(c);
                    if (ca != null)
                        ca.cycle(t);
                    else {
                        concepts.put(c, new ConceptActivity(c).cycle(t));
                    }                        
                }
            }
            else if (event == ConceptFire.class) {
                
            }
        }
        
        private List<AxisPlot> getCharts() {
            List<AxisPlot> l = new ArrayList(concepts.size());
            
            double min = 0, max = 0;
            for (ConceptActivity a : concepts.values()) {
                double[] mm = a.meanPriority.getMinMax();
                if (mm[0] < min) min = mm[0];
                if (mm[1] > max) max = mm[1];
            }
            for (ConceptActivity a : concepts.values()) {
                a.meanPriority.setRange(min, max);
            }

            List<ConceptActivity> cv = new ArrayList(concepts.values());
            
            Collections.sort(cv, new Comparator<ConceptActivity>() {

                @Override public int compare(ConceptActivity b, ConceptActivity a) {
                    return Float.compare(a.getTotalPriority(), b.getTotalPriority());
                }
                
            });            
                                
            for (ConceptActivity a : cv) {
                l.add( new BarChart(a.meanPriority) );
            }
            

            
            return l;
        }
        
        
        
    }
    
    
    public static void main(String[] args) throws Exception {
        int cycles = 500;
                
        NAR nar = new Default().build();
        NARTrace t = new NARTrace(nar);
        nar.addInput("<a --> b>.");
        nar.addInput("<b --> c>.");
        nar.addInput("<(^pick,x) =\\> a>.");
        nar.addInput("<(*, b, c) <-> x>.");
        nar.addInput("a!");

        ConceptIntegrator ci = new ConceptIntegrator(nar, 16, cycles);
        
        nar.run(cycles);

        TimelineVis tc = new TimelineVis(ci.getCharts());


        /*
            new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.charts.get("concept.count"))),

            new StackedPercentageChart(t, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),

            new LineChart(
                    new ConceptBagTimeSeries(nar, nar.memory.concepts, cycles, Mode.ConceptPriorityTotal)            
            ).height(4),

            new LineChart(
                    new ConceptBagTimeSeries(nar, nar.memory.concepts, cycles, Mode.TermLinkPriorityMean),
                    new ConceptBagTimeSeries(nar, nar.memory.concepts, cycles, Mode.TaskLinkPriorityMean)
            
            ).height(4),


            new LineChart(t, "task.novel.add", "task.immediate_processed").height(3),
            new LineChart(t, "task.goal.process", "task.question.process", "task.judgment.process").height(3),
            new LineChart(t, "emotion.busy").height(1),
            new EventChart(t, false, false, true).height(3)
        );
        */
        
        new NWindow("_", new PCanvas(tc)).show(800, 800, true);
    
        

    }
    
}
