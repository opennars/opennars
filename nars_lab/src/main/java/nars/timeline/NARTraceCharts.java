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
 * along with this program.  If not.getCharts(see <http://www.gnu.org/licenses/>.
 */
package nars.timeline;

import nars.core.NAR;
import nars.core.build.Default;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.AxisPlot;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.TimelineVis;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import nars.gui.output.chart.MeterVis;
import nars.io.meter.SignalData;
import nars.util.NARTrace;

/**
 *
 */
public class NARTraceCharts extends TimelineExample {

    public static void main(String[] args) {
        int cycles = 500;

        NAR nar = new NAR(new Default());
        NARTrace t = new NARTrace(nar);
        nar.addInput("<a --> b>.");
        nar.addInput("<b --> c>.");
        nar.addInput("<(^pick,x) =\\> a>.");
        nar.addInput("<(*, b, c) <-> x>.");
        nar.addInput("a!");
        nar.addInput("<a <-> ^pick>?");
        nar.run(cycles);

        System.out.println(t.metrics.getSignals());
        //t.metrics.printCSV(System.out);

        nar.start(100);

        new NWindow("_", 
                new MeterVis(nar,t.metrics,null).newPanel()).show(800, 800, true);

    }

}
