/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "RunSimulatorAction.java". Description:
"Runs the Simulator

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

package ca.nengo.ui.action;

import ca.nengo.model.SimulationException;
import ca.nengo.sim.Simulator;
import ca.nengo.sim.SimulatorEvent;
import ca.nengo.sim.SimulatorListener;
import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.ConfigSchemaImpl;
import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.descriptors.PBoolean;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.config.managers.ConfigManager;
import ca.nengo.ui.config.managers.ConfigManager.ConfigMode;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.object.activity.TrackedAction;
import ca.nengo.ui.lib.object.activity.TrackedStatusMsg;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.model.node.UINetwork;

import javax.swing.*;

/**
 * Runs the Simulator
 * 
 * @author Shu Wu
 */
public class RunSimulatorAction extends StandardAction {
    private static final Property pEndTime = new PFloat("End time");
    private static final Property pShowDataViewer = new PBoolean(
            "Open data viewer after simulation");
    private static final Property pStartTime = new PFloat("Start time");
    private static final Property pStepSize = new PFloat("Step size");
    
    private static final long serialVersionUID = 1L;

    private static final ConfigSchemaImpl zProperties = new ConfigSchemaImpl(new Property[] {
            pStartTime, pStepSize, pEndTime, pShowDataViewer});

    private final UINetwork uiNetwork;

    /**
     * @param actionName
     *            Name of this action
     * @param uiNetwork
     *            Simulator to run
     */
    public RunSimulatorAction(String actionName, UINetwork uiNetwork) {
        super("Run simulator", actionName);
        this.uiNetwork = uiNetwork;
        pStartTime.setDescription("Time (in seconds) of the start of the simulation (usually 0)");
        pStepSize.setDescription("Size (in seconds) of the simulation timestep (usually 0.001)");
        pEndTime.setDescription("Time (in seconds) of the end of the simulation");
        pShowDataViewer.setDescription("Whether to automatically display any Probed data after running the simulation");
    }

    private boolean configured = false;
    private float startTime;
    private float endTime;
    private float stepTime;
    private boolean showDataViewer;

    /**
     * @param actionName TODO
     * @param uiNetwork TODO
     * @param startTime TODO
     * @param endTime TODO
     * @param stepTime TODO
     */
    public RunSimulatorAction(String actionName, UINetwork uiNetwork, float startTime,
            float endTime, float stepTime) {
        super("Run simulator", actionName, false);
        this.uiNetwork = uiNetwork;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stepTime = stepTime;
        configured = true;
    }

    @Override
    protected void action() throws ActionException {

        try {
            if (!configured) {
                ConfigResult properties = ConfigManager.configure(zProperties, uiNetwork
                        .getTypeName(), "Run " + uiNetwork.getFullName(), UIEnvironment
                        .getInstance(), ConfigMode.TEMPLATE_NOT_CHOOSABLE);

                startTime = (Float) properties.getValue(pStartTime);
                endTime = (Float) properties.getValue(pEndTime);
                stepTime = (Float) properties.getValue(pStepSize);
                showDataViewer = (Boolean) properties.getValue(pShowDataViewer);
            }
            
            RunSimulatorActivity simulatorActivity = new RunSimulatorActivity(startTime, endTime,
                    stepTime, showDataViewer);
            simulatorActivity.doAction();

        } catch (ConfigException e) {
            e.defaultHandleBehavior();

            throw new ActionException("Simulator configuration not complete", false, e);

        }

    }

    /**
     * Activity which will run the simulation
     * 
     * @author Shu Wu
     */
    class RunSimulatorActivity extends TrackedAction implements SimulatorListener {

        private static final long serialVersionUID = 1L;
        private float currentProgress = 0;
        private final float endTime;
        private TrackedStatusMsg progressMsg;
        private final boolean showDataViewer;
        private final float startTime;
        private final float stepTime;

        public RunSimulatorActivity(float startTime, float endTime, float stepTime,
                boolean showDataViewer) {
            super("Simulation started");
            this.startTime = startTime;
            this.endTime = endTime;
            this.stepTime = stepTime;
            this.showDataViewer = showDataViewer;
        }

        @Override
        protected void action() throws ActionException {
            try {
                Simulator simulator = uiNetwork.getSimulator();

                simulator.resetNetwork(false, true);
                simulator.addSimulatorListener(AbstractNengo.getInstance().getProgressIndicator());
                
                try {
                    simulator.run(startTime, endTime, stepTime);
                } finally {
                    simulator.removeSimulatorListener(AbstractNengo.getInstance().getProgressIndicator());
                }

                ((AbstractNengo) (UIEnvironment.getInstance())).captureInDataViewer(uiNetwork
                        .node());

                if (showDataViewer) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ((AbstractNengo) (UIEnvironment.getInstance()))
                            .setDataViewerVisible(true);
                        }
                    });

                }

            } catch (SimulationException e) {
                UserMessages.showError("Simulator problem: " + e.toString());
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see ca.nengo.sim.SimulatorListener#processEvent(ca.nengo.sim.SimulatorEvent)
         */
        public void processEvent(SimulatorEvent event) {
            /*
             * Track events from the simulator and show progress in the UI
             */

            if (event.getType() == SimulatorEvent.Type.FINISHED) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (progressMsg != null) {
                            progressMsg.finished();
                        }
                    }
                });

                return;
            }

            if ((event.getProgress() - currentProgress) > 0.01) {
                currentProgress = event.getProgress();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (progressMsg != null) {
                            progressMsg.finished();
                        }
                        progressMsg = new TrackedStatusMsg((int) (currentProgress * 100)
                                + "% - simulation running");

                    }
                });
            }
        }
    }
}
