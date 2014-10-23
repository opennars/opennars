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
import nars.core.build.DefaultNARBuilder;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries.ConceptTimeSeries;
import nars.gui.output.chart.TimeSeries.ConceptTimeSeries.Mode;
import nars.gui.output.chart.TimeSeries.FirstOrderDifferenceTimeSeries;
import nars.gui.output.timeline.Timeline2DCanvas;
import nars.gui.output.timeline.Timeline2DCanvas.BarChart;
import nars.gui.output.timeline.Timeline2DCanvas.EventChart;
import nars.gui.output.timeline.Timeline2DCanvas.LineChart;
import nars.gui.output.timeline.Timeline2DCanvas.StackedPercentageChart;
import nars.io.TextOutput;
import nars.util.NARTrace;

/**
 *
 */
public class ConceptDynamics extends TimelineExample {
    
    public static void main(String[] args) {
        int cycles = 500;
        
        NAR nar = new DefaultNARBuilder().build();
        
        //new TextOutput(nar, System.out);
        
        NARTrace t = new NARTrace(nar);
        nar.addInput("<(&&,<(*,$Z,$Y) --> parent>,<(*,$X,$Z) --> brother>) ==> <(*,$X,$Y) --> uncle>>.");
        nar.addInput("<(*,adam,tom) --> parent>.");
        nar.addInput("<(*,eva,tom) --> parent>.");
        nar.addInput("<(*,tim,adam) --> brother>.");
        nar.addInput("10");
        nar.addInput("<(*,tim,tom) --> uncle>?");
        
        ConceptTimeSeries[] ct1 = new ConceptTimeSeries[] {
            new ConceptTimeSeries(nar, "tim", cycles, Mode.Priority),
            new ConceptTimeSeries(nar, "tom", cycles, Mode.Priority),
            new ConceptTimeSeries(nar, "adam", cycles, Mode.Priority),
            new ConceptTimeSeries(nar, "eva", cycles, Mode.Priority),
        };
        
        ConceptTimeSeries[] ct2 = new ConceptTimeSeries[] {        
            new ConceptTimeSeries(nar, "brother", cycles, Mode.Priority),
            new ConceptTimeSeries(nar, "uncle", cycles, Mode.Priority),
            new ConceptTimeSeries(nar, "parent", cycles, Mode.Priority),
        };
        
        ConceptTimeSeries[] ct3 = new ConceptTimeSeries[] {            
            new ConceptTimeSeries(nar, "<(*,tim,tom) --> uncle>", cycles, Mode.Priority),
        };
        
        //<(*,tim,tom) --> uncle>. %1.00;0.73% {281 ...
        nar.finish(cycles);

        System.out.println( TextOutput.summarize( nar.memory.getConcepts() ) );
        
        new NWindow("_", new Timeline2DCanvas(
            new LineChart(ct1).height(5),
            new LineChart(ct2).height(5),
            new LineChart(ct3).height(5),
                
            //new EventChart(t, true, false, false).height(3),
            new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.charts.get("concept.count"))),
            
            new StackedPercentageChart(t, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
            new LineChart(t, "concept.priority.mean").height(1),

            //new EventChart(t, false, true, false).height(3),
            
            new LineChart(t, "task.solution.best.priority.mean").height(3),
            new BarChart(t, "task.solution.best").height(3),
 
            new LineChart(t, "task.novel.add", "task.immediate_processed").height(1),
            new LineChart(t, "task.goal.process", "task.question.process", "task.judgment.process").height(1),
            new LineChart(t, "emotion.busy").height(1)
            //new EventChart(t, false, false, true).height(3)
        )).show(800, 800, true);
    }
    
}
