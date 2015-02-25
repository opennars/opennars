/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DataTreeNode.java". Description:
"Tree Node with NEO Data

  @author Shu Wu"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
 */

package ca.nengo.ui.data;

import ca.nengo.ui.action.PlotSpikePattern;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.util.DataUtils;
import ca.nengo.util.SpikePattern;
import ca.nengo.util.TimeSeries;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;

/**
 * Tree Node with NEO Data
 * 
 * @author Shu Wu
 */
public abstract class DataTreeNode extends SortableMutableTreeNode {

    private static final long serialVersionUID = 1L;

    /**
     * @param userObject TODO
     */
    public DataTreeNode(Object userObject) {
        super(userObject);
    }

    /**
     * @param menu TODO
     * @param dataModel TODO
     */
    public abstract void constructPopupMenu(PopupMenuBuilder menu, SimulatorDataModel dataModel);

    /**
     * @return TODO
     */
    public abstract StandardAction getDefaultAction();

    /**
     * @return TODO
     */
    public abstract boolean includeInExport();

    public abstract String toString();
}

/**
 * Node containing probe data
 * 
 * @author Shu Wu
 */
class ProbeDataNode extends TimeSeriesNode {

    private static final long serialVersionUID = 1L;

    public ProbeDataNode(TimeSeries userObject, String stateName, boolean applyFilterByDefault) {
        super(userObject, stateName, applyFilterByDefault);
    }

    @Override
    public boolean includeInExport() {
        return true;
    }

    @Override
    public String toString() {
        return name + " (Probe data " + getUserObject().getDimension() + "D)";
    }

}

/**
 * Contains one-dimensional expanded data from a Probe
 * 
 * @author Shu Wu
 */
class ProbeDataExpandedNode extends TimeSeriesNode {

    private static final long serialVersionUID = 1L;

    public ProbeDataExpandedNode(TimeSeries userObject, int dim, boolean applyFilterByDefault) {
        super(userObject, String.valueOf(dim), applyFilterByDefault);
    }

    @Override
    public boolean includeInExport() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}

/**
 * Node containing a spike pattern
 * 
 * @author Shu WU
 */
class SpikePatternNode extends DataTreeNode {

    private static final long serialVersionUID = 1L;

    public SpikePatternNode(SpikePattern spikePattern) {
        super(spikePattern);
    }

    public void constructPopupMenu(PopupMenuBuilder menu, SimulatorDataModel dataModel) {
        menu.addAction(getDefaultAction());
    }

    @Override
    public StandardAction getDefaultAction() {
        return new PlotSpikePattern(getUserObject());
    }

    @Override
    public SpikePattern getUserObject() {
        return (SpikePattern) super.getUserObject();
    }

    @Override
    public boolean includeInExport() {
        return true;
    }

    public String toString() {
        return "Spike Pattern";
    }

}

/**
 * Node containing time series data
 * 
 * @author Shu Wu
 */
abstract class TimeSeriesNode extends DataTreeNode {
    private static final long serialVersionUID = 1L;

    protected final String name;
    private final boolean applyFilterByDefault;

    public TimeSeriesNode(TimeSeries userObject, String name, boolean applyFilterByDefault) {
        super(userObject);
        this.name = name;
        this.applyFilterByDefault = applyFilterByDefault;
    }

    public void constructPopupMenu(PopupMenuBuilder menu, SimulatorDataModel dataModel) {
        Collection<StandardAction> actions = ProbePlotHelper.getInstance().getPlotActions(getUserObject(),
                getPlotName());

        for (StandardAction action : actions) {
            menu.addAction(action);
        }

        menu.addAction(new ExtractDimenmsions(dataModel));
    }

    class ExtractDimenmsions extends StandardAction {

        private static final long serialVersionUID = 1L;
        private final SimulatorDataModel dataModel;

        public ExtractDimenmsions(SimulatorDataModel dataModel) {
            super("Extract dimensions");
            this.dataModel = dataModel;
        }

        @Override
        protected void action() throws ActionException {
            extractDimensions();
            dataModel.nodeStructureChanged(TimeSeriesNode.this);
        }

        public void extractDimensions() {
            TimeSeries probeData = getUserObject();
            /*
             * Extract dimensions
             */
            for (int dimCount = 0; dimCount < probeData.getDimension(); dimCount++) {
                TimeSeries oneDimData = DataUtils.extractDimension(probeData, dimCount);

                DefaultMutableTreeNode stateDimNode = new ProbeDataExpandedNode(oneDimData,
                        dimCount, applyFilterByDefault);
                add(stateDimNode);
            }
        }
    }

    public boolean isApplyFilterByDefault() {
        return applyFilterByDefault;
    }

    @Override
    public StandardAction getDefaultAction() {
        return ProbePlotHelper.getInstance().getDefaultAction(getUserObject(),
                getPlotName(),
                applyFilterByDefault);
    }

    private String getPlotName() {
        return "Probe data: " + name;
    }

    @Override
    public TimeSeries getUserObject() {
        return (TimeSeries) super.getUserObject();
    }

    @Override
    public abstract String toString();

}