/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nars.constraint.gui.panels;

import nars.constraint.gui.GUI;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/06/2014
 */
public class LeftRightBranchPanel extends APanel implements IMonitorDownBranch, IMonitorRestart, IMonitorOpenNode {
    XYSeries serie1, serie2;
    int counter;

    public LeftRightBranchPanel(GUI frame) {
        super(frame);
        serie1 = new XYSeries("Left-Right decisions");
        serie2 = new XYSeries("Depth");
        XYSeriesCollection scoll = new XYSeriesCollection();
        scoll.addSeries(serie1);
        scoll.addSeries(serie2);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LR decisions", "Nodes", "Left-Right decisions", scoll);
        this.setChart(chart);
        solver.plugMonitor(this);
    }

    @Override
    public void plug(JTabbedPane tabbedpanel) {
        super.plug(tabbedpanel);
        tabbedpanel.addTab("LR decisions", this);
    }


    @Override
    public void beforeDownLeftBranch() {

    }

    @Override
    public void afterDownLeftBranch() {
        counter++;
        if (frame.canUpdate() && activate) {
            serie1.add(solver.getMeasures().getNodeCount(), counter);
        }
        if (flush) {
            serie1.clear();
            flushDone();
        }
    }

    @Override
    public void beforeDownRightBranch() {

    }

    @Override
    public void afterDownRightBranch() {
        counter--;
        if (frame.canUpdate() && activate) {
            serie1.add(solver.getMeasures().getNodeCount(), counter);
        }
        if (flush) {
            serie1.clear();
            serie2.clear();
            flushDone();
        }
    }

    @Override
    public void beforeRestart() {
        counter = 0;
    }

    @Override
    public void afterRestart() {

    }

    @Override
    public void beforeOpenNode() {
        if (frame.canUpdate() && activate) {
            serie2.add(solver.getMeasures().getNodeCount(), solver.getMeasures().getCurrentDepth());
        }
        if (flush) {
            serie1.clear();
            serie2.clear();
            flushDone();
        }
    }

    @Override
    public void afterOpenNode() {

    }
}