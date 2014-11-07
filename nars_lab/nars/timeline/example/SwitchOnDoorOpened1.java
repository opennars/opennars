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

import java.io.File;
import java.io.IOException;
import nars.core.NAR;
import nars.core.build.Default;
import nars.grid2d.TestChamber;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries.FirstOrderDifferenceTimeSeries;
import nars.io.TextInput;
import nars.gui.output.timeline.Timeline2DCanvas;
import nars.gui.output.timeline.BarChart;
import nars.gui.output.timeline.EventChart;
import nars.gui.output.timeline.LineChart;
import nars.gui.output.timeline.StackedPercentageChart;
import nars.io.TextOutput;
import nars.util.NARTrace;

/**
 *
 */
public class SwitchOnDoorOpened1 extends TimelineExample {
    
    public static void main(String[] args) throws Exception {
        int cycles = 1000;
        int inputDelay = 5;
        
        NAR nar = new Default().build();
        new TestChamber(nar, false);
        
        NARTrace t = new NARTrace(nar);
        
        new TextOutput(nar, System.out);
        new NARSwing(nar);
        
        TextInput i = new TextInput(new File("nal/TestChamber/TestChamberIndependentExperience/switch_on_door_opened.nal")) {
            int c = 0;
            @Override public Object next() throws IOException {
                if (c++ % 2 == 0)
                    return super.next();
                else
                    return inputDelay+ "\n";
            }
        };
        
        nar.addInput(i);
        
        nar.step(cycles);        

        
        new NWindow("_", new Timeline2DCanvas(
                
            new BarChart(t, "task.executed").height(2),
            new StackedPercentageChart(t, "plan.graph.in.other.count", "plan.graph.in.operation.count", "plan.graph.in.interval.count").height(3),
            new LineChart(t, "plan.graph.in.delay_magnitude.mean").height(1),
            new LineChart(t, "plan.graph.edge.count", "plan.graph.vertex.count").height(2),                
            new StackedPercentageChart(t, "plan.task.executable", "plan.task.planned").height(2),
            
                
            new EventChart(t, true, false, false).height(3),
            new EventChart(t, false, true, false).height(3),
            new EventChart(t, false, false, true).height(3),

            new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.charts.get("concept.count"))),
            
            new StackedPercentageChart(t, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
            new LineChart(t, "concept.priority.mean").height(1),
                

                
            new LineChart(t, "task.novel.add", "task.immediate_processed").height(3),
            new LineChart(t, "task.goal.process", "task.question.process", "task.judgment.process").height(3),
            new LineChart(t, "task.new.add").height(3)
            //new LineChart(t, "emotion.busy").height(1)
            
        )).show(800, 800, true);
    }
    
}
