/*
 * Copyright (C) 2014 me
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
package nars.gui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import nars.util.NARState;

/**
 * http://jchart2d.sourceforge.net/usage.shtml
 */
public class ChartPanel extends JPanel {
    private final Chart2D chart;
    private final HashMap<String,Trace2DLtd> params = new HashMap();

    public ChartPanel(String... params) {
        super(new BorderLayout());

        
        int historySize = 200;
        
        chart = new Chart2D();
        chart.setBorder(new EmptyBorder(0,0,0,0));
        //chart.setSize(200,200);
        chart.setUseAntialiasing(true);
        chart.setBackground(Color.BLACK);
        chart.setForeground(Color.WHITE);
        chart.setGridColor(Color.darkGray);
        for (IAxis left : chart.getAxesYLeft()) {
            left.setAxisTitle(new AxisTitle(""));
            left.setPaintGrid(true);
        }
 
        for (IAxis bottom : chart.getAxesXBottom()) {
            bottom.setVisible(false);
        }        

        for (String p : params) {
            Trace2DLtd t = new Trace2DLtd(historySize, p);
            t.setColor(Color.getHSBColor( ((float)(p.hashCode()%1024))/1024.0f, 0.5f, 1.0f));
            chart.addTrace(t);
            this.params.put(p, t);
        }
        
        
        add(chart, BorderLayout.CENTER);        
    }

    public void update(NARState state) {
        Map.Entry<Long, HashMap<String, Object>> entry = state.lastEntry();
        long when = entry.getKey();
        HashMap<String, Object> data = entry.getValue();
        
        for (String p : params.keySet()) {
            Object value = data.get(p);
            Trace2DLtd trace = params.get(p);
            if (value!=null) {
                if (value instanceof Double)
                   trace.addPoint(when, (Double)value);
                if (value instanceof Float)
                   trace.addPoint(when, (Float)value);
                if (value instanceof Integer)
                   trace.addPoint(when, (Integer)value);
            }
        }
    }
}
