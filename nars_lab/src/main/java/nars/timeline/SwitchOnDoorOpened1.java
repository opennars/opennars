///*
// * Copyright (C) 2014 sue
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.timeline;
//
//import com.google.common.collect.Iterators;
//import nars.model.impl.Default;
//import nars.NAR;
//import nars.grid2d.TestChamber;
//import nars.gui.NARSwing;
//import nars.io.TextInput;
//import nars.io.TextOutput;
//import nars.nal.Task;
//import nars.meter.NARTrace;
//import nars.op.io.PauseInput;
//
//import java.io.File;
//import java.util.Iterator;
//
///**
// *
// */
//public class SwitchOnDoorOpened1 extends TimelineExample {
//
//    public static void main(String[] args) throws Exception {
//        int cycles = 1000;
//        int inputDelay = 5;
//
//        NAR nar = new NAR(new Default());
//        new TestChamber(nar, false);
//
//        NARTrace t = new NARTrace(nar);
//
//        new TextOutput(nar, System.out);
//        new NARSwing(nar);
//
//        TextInput i = new TextInput(nar.textPerception, new File("nal/TestChamber/TestChamberIndependentExperience/switch_on_door_opened.nal")) {
//
////            int c = 0;
////            public Iterator<Task> load() {
////
////                if (c++ % 2 == 0)
////                    return super.load();
////                else {
////                    return Iterators.singletonIterator(new PauseInput(inputDelay).newTask());
////                }
////            }
//        };
//
//        nar.input(i);
//
//        nar.frame(cycles);
//
//
////        new NWindow("_", new PCanvas(new TimelineVis(
////
////            new BarChart(t.getCharts( "task.executed")[0]).height(2),
////            new StackedPercentageChart(t.getCharts( "plan.graph.in.other.count", "plan.graph.in.operation.count", "plan.graph.in.interval.count")).height(3),
////            new LineChart(t.getCharts( "plan.graph.in.delay_magnitude.mean")).height(1),
////            new LineChart(t.getCharts( "plan.graph.edge.count", "plan.graph.vertex.count")).height(2),
////            new StackedPercentageChart(t.getCharts( "plan.task.executable", "plan.task.planned")).height(2),
////
////
////            new EventChart(t, true, false, false).height(3),
////            new EventChart(t, false, true, false).height(3),
////            new EventChart(t, false, false, true).height(3),
////
////            //new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.metrics.get("concept.count"))),
////
////            new StackedPercentageChart(t.getCharts("concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3")).height(2),
////            new LineChart(t.getCharts("concept.priority.mean")).height(1),
////
////
////
////            new LineChart(t.getCharts("task.novel.add", "task.immediate_processed")).height(3),
////            new LineChart(t.getCharts("task.goal.process", "task.question.process", "task.judgment.process")).height(3),
////            new LineChart(t.getCharts("task.new.add")).height(3)
////            //new LineChart(t.getCharts("emotion.busy").height(1)
////
////        ))).show(800, 800, true);
//    }
//
// }
