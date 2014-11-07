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
package nars.timeline.example;

import nars.core.NAR;
import nars.core.build.Default;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries.ConceptBagTimeSeries;
import nars.gui.output.chart.TimeSeries.ConceptBagTimeSeries.Mode;
import nars.gui.output.chart.TimeSeries.FirstOrderDifferenceTimeSeries;
import nars.gui.output.timeline.BarChart;
import nars.gui.output.timeline.Chart;
import nars.gui.output.timeline.EventChart;
import nars.gui.output.timeline.LineChart;
import nars.gui.output.timeline.StackedPercentageChart;
import nars.gui.output.timeline.Timeline2DCanvas;
import nars.util.NARTrace;

/**
 *
 */
public class BudgetExpense extends TimelineExample {
    
    
    public static void main(String[] args) throws Exception {
        int cycles = 1000;
                
        NAR nar = new Default().build();
        NARTrace t = new NARTrace(nar);
        nar.addInput("<a --> b>.");
        nar.addInput("<b --> c>.");
        nar.addInput("<(^pick,x) =\\> a>.");
        nar.addInput("<(*, b, c) <-> x>.");
        nar.addInput("a!");

        
        Timeline2DCanvas tc = new Timeline2DCanvas(

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
                
        nar.finish(cycles);
        
        new NWindow("_", tc).show(800, 800, true);
    
        

    }
    
}
