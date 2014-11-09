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
import nars.core.Param;
import nars.core.build.Default;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries.FirstOrderDifferenceTimeSeries;
import nars.gui.output.timeline.Timeline2DCanvas;
import nars.gui.output.timeline.BarChart;
import nars.gui.output.timeline.LineChart;
import nars.gui.output.timeline.StackedPercentageChart;
import nars.util.NARTrace;

public class NARCompare1 extends TimelineExample {
    
    public static void setA(Param p) {
        p.termLinkForgetDurations.set(10);
    }
    
    public static void setB(Param p) {
        p.termLinkForgetDurations.set(5);
    }
    
    public static void main(String[] args) {
        int cycles = 128;

        String input = "<a --> b>.\n" + "<b --> c>.\n" + "<a --> c>?\n";
        NAR a = new Default().build();
        setA(a.param);        
        NARTrace at = new NARTrace(a);
        a.addInput(input);
        a.finish(cycles);

        NAR b = new Default().build();
        setB(b.param);
        NARTrace bt = new NARTrace(b);
        b.addInput(input);
        b.finish(cycles);
        
        new NWindow("_", new Timeline2DCanvas(
            new LineChart(at, "task.novel.add", "task.immediate_processed").height(3),
            new LineChart(at, "task.goal.process", "task.question.process", "task.judgment.process").height(3),
            new LineChart(at, "concept.priority.mean").height(2),                
            new StackedPercentageChart(at, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
            new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", at.charts.get("concept.count"))),
                
            new LineChart(bt, "task.novel.add", "task.immediate_processed").height(3),
            new LineChart(bt, "task.goal.process", "task.question.process", "task.judgment.process").height(3),
            new LineChart(bt, "concept.priority.mean").height(2),
            new StackedPercentageChart(bt, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
            new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", bt.charts.get("concept.count")))
        )).show(900, 800, true);
    }
    
}