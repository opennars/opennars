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
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.variables.IntVar;
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
public class ObjectivePanel extends APanel implements IMonitorOpenNode {
    XYSeries objective, bounds;
    boolean isOpt;
    boolean isMax;

    public ObjectivePanel(GUI frame) {
        super(frame);
        isOpt = solver.getObjectiveManager().getPolicy() != ResolutionPolicy.SATISFACTION;
        isMax = solver.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
        objective = new XYSeries("Best value");
        bounds = new XYSeries(isMax ? "Upper bound" : "Lower bound");
        XYSeriesCollection coll = new XYSeriesCollection();
        coll.addSeries(objective);
        coll.addSeries(bounds);

        JFreeChart dchart = ChartFactory.createXYLineChart(
                "Objective", "Nodes", "Objective", coll);

        this.setChart(dchart);
        if (isOpt) {
            solver.plugMonitor(this);
        }
    }

    @Override
    public void plug(JTabbedPane tabbedpanel) {
        super.plug(tabbedpanel);
        if (isOpt) {
            tabbedpanel.addTab("Objective", this);
        }
    }

    @Override
    public void beforeOpenNode() {

    }

    @Override
    public void afterOpenNode() {
        if (frame.canUpdate() && isOpt && activate) {
            long ncount = solver.getMeasures().getNodeCount();
            if (solver.getMeasures().getSolutionCount() > 0) {
                objective.add(ncount, solver.getObjectiveManager().getBestSolutionValue());
            }
            bounds.add(ncount, isMax ?
                    ((IntVar) solver.getObjectiveManager().getObjective()).getUB() :
                    ((IntVar) solver.getObjectiveManager().getObjective()).getLB()
            );
        }
        if (flush) {
            objective.clear();
            bounds.clear();
            flushDone();
        }
    }
}
