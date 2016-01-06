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
//import automenta.vivisect.TreeMLData.FirstOrderDifferenceTimeSeries;
//import automenta.vivisect.timeline.MultiTimeline;
//import nars.core.Memory;
//import nars.core.NAR;
//import nars.build.Discretinuous;
//import automenta.vivisect.swing.NWindow;
//import automenta.vivisect.timeline.BarChart;
//import automenta.vivisect.timeline.Chart;
//import automenta.vivisect.timeline.LineChart;
//import automenta.vivisect.timeline.StackedPercentageChart;
//import nars.gui.EventChart;
//import nars.other.NARTrace;
//import nars.other.NARTrace.ConceptTreeMLData.Mode;
//
///**
// *
// */
//public class ConceptDynamics extends TimelineExample {
//    
//    
//    public static void main(String[] args) throws Exception {
//        int cycles = 1000;
//        
//
//        new NWindow("_", new MultiTimeline(2) {
//
//            @Override
//            public Chart[] getCharts(int experiment) {
//                
//                Memory.resetStatic();
//                NAR nar = null;
//                switch (experiment) {
//                    case 0:                        
//                        Discretinuous d = new Discretinuous();
//                        d.param.setForgetting(Memory.Forgetting.Periodic);                        
//                        nar = d.build();
//                        (nar.param).conceptForgetDurations.set(5f);
//                        break;
//                    case 1:
//                        Discretinuous e = new Discretinuous();
//                        e.param.setForgetting(Memory.Forgetting.Periodic);                        
//                        nar = e.build();
//                        (nar.param).conceptForgetDurations.set(5f);                        
//                        break;
//                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/
//                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/
//                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/
//                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/
//                    /*case 2:
//                        nar = new ContinuousBagNARBuilder().build();
//                        break;*/
//                }
//
//
//                NARTrace t = new NARTrace(nar);
//                nar.addInput(
//                        (experiment == 0 ? 
//                                "$0.80;0.50$ " : "$0.90;0.60$ ") +
//                        "<(&&,<(*,$Z,$Y) --> parent>,<(*,$X,$Z) --> brother>) ==> <(*,$X,$Y) --> uncle>>.");
//                nar.addInput("<(*,adam,tom) --> parent>.");
//                nar.addInput("<(*,eva,tom) --> parent>.");
//                nar.addInput("<(*,tim,adam) --> brother>.");
//                nar.addInput("10");
//                nar.addInput("<(*,tim,tom) --> uncle>?");
//                //<(*,tim,tom) --> uncle>. %1.00;0.73% {281 ...
//                
//
//                try {
//                    ConceptTimeSeries[] ct1 = new ConceptTimeSeries[] {
//                        new ConceptTimeSeries(nar, "tim", cycles, Mode.Priority),
//                        new ConceptTimeSeries(nar, "tom", cycles, Mode.Priority),
//                        new ConceptTimeSeries(nar, "adam", cycles, Mode.Priority),
//                        new ConceptTimeSeries(nar, "eva", cycles, Mode.Priority),
//                    };
//
//                    ConceptTimeSeries[] ct2 = new ConceptTimeSeries[] {        
//                        new ConceptTimeSeries(nar, "brother", cycles, Mode.Priority),
//                        new ConceptTimeSeries(nar, "uncle", cycles, Mode.Priority),
//                        new ConceptTimeSeries(nar, "parent", cycles, Mode.Priority),                        
//                        new ConceptTimeSeries(nar, "<(*,tim,tom) --> uncle>", cycles, Mode.Priority),
//                    };
//
//                    ConceptTimeSeries[] ct3 = new ConceptTimeSeries[] {            
//                        new ConceptTimeSeries(nar, "<(*,tim,tom) --> uncle>", cycles, Mode.Priority),
//                        new ConceptTimeSeries(nar, "<(*,tim,tom) --> uncle>", cycles, Mode.BeliefConfidenceMax),
//                    };
//                    
//                    Chart[] charts = new Chart[] {
//                        //new SpectrumChart(t, "concept.priority.hist.0", 8).height(4),
//                        //new SpectrumChart(t, "concept.priority.hist.1", 8).height(4),
//                        new StackedPercentageChart(ct1).height(5),
//                        new StackedPercentageChart(ct2).height(5),
//                        new LineChart(ct3).height(5),
//
//                        //new EventChart(t, true, false, false).height(3),
//                        new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.charts.get("concept.count"))),
//
//                        /*new StackedPercentageChart(t, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
//                        new LineChart(t, "concept.priority.mean").height(1),*/
//                        
//
//                        new EventChart(t, true, false, false).height(3),
//
//                        new LineChart(t.getCharts("task.solution.best.priority.mean")).height(3),
//                        new BarChart(t.getCharts("task.solution.best")[0]).height(3),
//
//                        //new LineChart(t, "task.novel.add", "task.immediate_processed").height(1),
//                        //new LineChart(t, "task.goal.process", "task.question.process", "task.judgment.process").height(1),
//                        //new LineChart(t, "emotion.busy").height(1)
//                        //new EventChart(t, false, false, true).height(3)
//
//                    };
//                    
//                    nar.finish(cycles);
//
//                    //System.out.println( TextOutput.summarize( nar.memory.concepts ) );
//                    return charts;
//                    
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                    System.exit(1);
//                }
//
//                return null;
//                
//            }
//            
//        }).show(800, 800, true);
//    }
//    
// }
