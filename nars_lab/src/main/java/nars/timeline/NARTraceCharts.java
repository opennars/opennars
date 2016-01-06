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
// * along with this program.  If not.getCharts(see <http://www.gnu.org/licenses/>.
// */
//package nars.timeline;
//
//import automenta.vivisect.swing.NWindow;
//import nars.prototype.Default;
//import nars.NAR;
//import nars.gui.output.chart.MeterVisOld;
//import nars.nal.meta.NARMetrics;
//
///**
// *
// */
//public class NARTraceCharts extends TimelineExample {
//
//    public static void main(String[] args) {
//        int cycles = 500;
//
//        NAR nar = new NAR(new Default());
//        NARMetrics t = new NARMetrics(nar, 128);
//        nar.input("<a --> b>.");
//        nar.input("<b --> c>.");
//        nar.input("<(^pick,x) =\\> a>.");
//        nar.input("<(*, b, c) <-> x>.");
//        nar.input("a!");
//        nar.input("<a <-> ^pick>?");
//        nar.run(cycles);
//
//        System.out.println(t.getMetrics().getSignals());
//
//        nar.start(100);
//
//
//
//        new NWindow("_",
//                new MeterVisOld(nar, t.getMetrics()).newPanel()).show(800, 800, true);
//
//    }
//
// }
