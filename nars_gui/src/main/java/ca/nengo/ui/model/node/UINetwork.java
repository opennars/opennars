/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UINetwork.java". Description:
"UI Wrapper for a Network

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

package ca.nengo.ui.model.node;

import ca.nengo.model.Network;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.sim.Simulator;
import ca.nengo.ui.action.RunSimulatorAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.NetworkIcon;
import ca.nengo.ui.model.tooltip.TooltipBuilder;
import ca.nengo.ui.model.viewer.NetworkViewer;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.VisiblyChanges;
import ca.nengo.util.VisiblyChanges.Event;
import nars.gui.output.graph.nengo.DefaultUINetwork;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;

/**
 * UI Wrapper for a Network
 * 
 * @author Shu Wu
 */
public class UINetwork extends UINodeViewable {



    private class MySimulatorListener implements VisiblyChanges.Listener {
        private boolean simulatorUpdatePending = false;

        public void changed(Event e) {
            if (!simulatorUpdatePending) {
                simulatorUpdatePending = true;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        simulatorUpdatePending = false;
                        simulatorUpdated();
                    }
                });
            }
        }
    }

    public static final String typeName = "Network";

    public static void constructSimulatorMenu(PopupMenuBuilder menu, UINetwork network) {
        menu.addSection("Run");
        menu.addAction(new RunSimulatorAction("Simulate " + network.name(), network));
        //menu.addAction(new RunInteractivePlotsAction(network));
    }

    /**
     * @param wo WorldObject
     * @return The closest parent Network to wo
     */
    public static UINetwork getClosestNetwork(WorldObject wo) {
        while (true) {
            if (wo == null) {
                return null;
            }

            if (wo instanceof UINetwork) {
                return (UINetwork) wo;
            } else if (wo instanceof NodeViewer) {
                wo = ((NodeViewer) wo).getViewerParent();
            } else if (wo instanceof UINeoNode) {
                wo = ((UINeoNode) wo).getNetworkParent();
            } else {
                wo = wo.getParent();
            }
        }
    }

    private MySimulatorListener mySimulatorListener;

    public UINetwork(Network model) {
        super(model);
        setIcon(new NetworkIcon(this));
    }

    @Override
    public void attachViewToModel() {
        super.attachViewToModel();
        node().getSimulator().addChangeListener(mySimulatorListener);
    }

    @Override
    protected void constructMenu(PopupMenuBuilder menu) {
        super.constructMenu(menu);
        constructSimulatorMenu(menu, this);
    }

    @Override
    protected void constructTooltips(TooltipBuilder tooltips) {
        super.constructTooltips(tooltips);
        tooltips.addProperty("# Projections", String.valueOf(node().getProjections().length));
        tooltips.addProperty("Simulator", getSimulator().getClass().getSimpleName());
    }

    @Override
    public NodeViewer newViewer() {
        NodeViewer v = new DefaultUINetwork.UINARGraphViewer(this);
        v.getGridLayer().setBgColor(Color.BLACK);
        return v;
    }

    /** if gridBG == null, removes grid */
    public NodeViewer newViewer(Color gridBg, Color gridColor, float gridTransparency) {
        NetworkViewer nv = new DefaultUINetwork.UINARGraphViewer(this);

        if (gridBg == null)
            nv.getGridLayer().removeFromParent();
        else {
            nv.getGridLayer().setBgColor(gridBg);
            nv.getGridLayer().setGridColor(gridColor);
            nv.getGridLayer().setTransparency(gridTransparency);
        }
        return nv;
    }

    @Override
    public void detachViewFromModel() {
        super.detachViewFromModel();
        node().getSimulator().removeChangeListener(mySimulatorListener);
    }

    //    @Override
    //    public String getFileName() {
    //        return getSavedConfig().getFileName();
    //    }

    @Override
    public NetworkImpl node() {
        return (NetworkImpl) super.node();
    }

    @Override
    public String name() {
        if (node() == null) {
            return super.name();
        } else {
            return node().name();
        }
    }

    @Override
    public int getNodesCount() {
        if (node() != null) {
            //			return getModel().getNodes().length;
            return node().countNeurons();
        } else {
            return 0;
        }
    }

    @Override
    public int getDimensionality() {
        // What is the dimensionality of a network???
        return -1;
    }

    /**
     * @return UI Configuration manager associated with this network
     */
    //	public NetworkViewerConfig getSavedConfig() {
    //		NetworkViewerConfig layoutManager = null;
    //		try {
    //			Object obj = getModel().getMetaData(LAYOUT_MANAGER_KEY);
    //
    //			if (obj != null)
    //				layoutManager = (NetworkViewerConfig) obj;
    //		} catch (Throwable e) {
    //			UserMessages.showError("Could not access layout manager, creating a new one");
    //		}
    //
    //		if (layoutManager == null) {
    //			layoutManager = new NetworkViewerConfig(getName() + "."
    //					+ NengoGraphics.NEONODE_FILE_EXTENSION);
    //			setUICOnfig(layoutManager);
    //		}
    //
    //		return layoutManager;
    //	}

    /**
     * @return Simulator
     */
    public Simulator getSimulator() {
        return node().getSimulator();
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public NetworkViewer getViewer() {
        return (NetworkViewer) super.getViewer();
    }

    /**
     * @return Gets the existing viewer, if it exists. Otherwise, open it.
     */
    public NodeViewer getViewerEnsured() {
        NetworkViewer viewer = getViewer();
        return viewer != null ? viewer : openViewer();
    }

    @Override
    protected void initialize() {
        mySimulatorListener = new MySimulatorListener();
        super.initialize();
    }

    @Override
    protected void modelUpdated() {
        super.modelUpdated();

        if (getViewer() != null && !getViewer().isDestroyed()) {
            getViewer().updateViewFromModel();
        }
    }

    @Override
    public void saveContainerConfig() {

    }

    //    @Override
    //    public void saveModel(File file) throws IOException {
    //        getSavedConfig().setFileName(file.toString());
    //        super.saveModel(file);
    //    }

    /**
     *            UI Configuration manager
     */
    //    public void setUICOnfig(NetworkViewerConfig config) {
    //        getModel().setMetaData(LAYOUT_MANAGER_KEY, config);
    //    }

    private void simulatorUpdated() {
        if (getViewer() != null && !getViewer().isDestroyed()) {
            getViewer().updateSimulatorProbes();
        }
    }

    public void setWindow(Window x) {
        viewerWindowRef = new WeakReference<Window>(x);
    }
}
