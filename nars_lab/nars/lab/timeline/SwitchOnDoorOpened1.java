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
package nars.lab.timeline;

import automenta.vivisect.TreeMLData.FirstOrderDifferenceTimeSeries;
import java.io.File;
import java.io.IOException;
import nars.NAR;
import nars.config.Default;
import nars.lab.grid2d.main.TestChamber;
import nars.gui.NARSwing;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import nars.io.TextInput;
import automenta.vivisect.timeline.TimelineVis;
import automenta.vivisect.timeline.BarChart;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.StackedPercentageChart;
import nars.gui.EventChart;
import nars.io.TextOutput;
import nars.gui.util.NARTrace;

/**
 *
 */
public class SwitchOnDoorOpened1 extends TimelineExample {
    
    public static void main(String[] args) throws Exception {
        int cycles = 1000;
        int inputDelay = 5;
        
        NAR nar = new NAR(new Default());
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

        
        new NWindow("_", new PCanvas(new TimelineVis(
                
            new BarChart(t.getCharts( "task.executed")[0]).height(2),
            new StackedPercentageChart(t.getCharts( "plan.graph.in.other.count", "plan.graph.in.operation.count", "plan.graph.in.interval.count")).height(3),
            new LineChart(t.getCharts( "plan.graph.in.delay_magnitude.mean")).height(1),
            new LineChart(t.getCharts( "plan.graph.edge.count", "plan.graph.vertex.count")).height(2),                
            new StackedPercentageChart(t.getCharts( "plan.task.executable", "plan.task.planned")).height(2),
            
                
            new EventChart(t, true, false, false).height(3),
            new EventChart(t, false, true, false).height(3),
            new EventChart(t, false, false, true).height(3),

            new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.charts.get("concept.count"))),
            
            new StackedPercentageChart(t.getCharts("concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3")).height(2),
            new LineChart(t.getCharts("concept.priority.mean")).height(1),
                

                
            new LineChart(t.getCharts("task.novel.add", "task.immediate_processed")).height(3),
            new LineChart(t.getCharts("task.goal.process", "task.question.process", "task.judgment.process")).height(3),
            new LineChart(t.getCharts("task.new.add")).height(3)
            //new LineChart(t.getCharts("emotion.busy").height(1)
            
        ))).show(800, 800, true);
    }
    
}
