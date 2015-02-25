/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PlotAdvanced.java". Description:
"Action for Plotting with additional options

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

import ca.nengo.plot.Plotter;
import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.config.descriptors.PInt;
import ca.nengo.ui.config.managers.ConfigManager.ConfigMode;
import ca.nengo.ui.config.managers.UserConfigurer;
import ca.nengo.ui.data.ProbePlotHelper;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.util.DataUtils;
import ca.nengo.util.TimeSeries;

/**
 * Action for Plotting with additional options
 * 
 * @author Shu Wu
 */
public class PlotTimeSeries extends StandardAction {

    private static final long serialVersionUID = 1L;
    private final TimeSeries timeSeries;
    private final String plotName;
    private boolean showUserConfigDialog = false;
    private float tauFilter;
    private int subSampling;

    /**
     * @param actionName TODO
     * @param timeSeries TODO
     * @param plotName TODO
     * @param showUserConfigDialog TODO
     * @param defaultTau TODO
     * @param defaultSubSampling TODO
     */
    public PlotTimeSeries(
            String actionName,
            TimeSeries timeSeries,
            String plotName,
            boolean showUserConfigDialog,
            float defaultTau,
            int defaultSubSampling) {
        super(actionName);
        this.timeSeries = timeSeries;
        this.showUserConfigDialog = showUserConfigDialog;
        this.plotName = plotName + "  [ " + timeSeries.getName() + " ]";
        this.tauFilter = defaultTau;
        this.subSampling = defaultSubSampling;
    }

    @Override
    protected void action() throws ActionException {
        try {
            PFloat pTauFilter = new PFloat("Time constant of display filter [0 = off]", tauFilter);
            PInt pSubSampling = new PInt("Subsampling [0 = off]", subSampling);

            if (showUserConfigDialog) {
                ConfigResult result;
                try {
                    result = UserConfigurer.configure(new Property[] { pTauFilter, pSubSampling },
                            "Plot Options",
                            UIEnvironment.getInstance(),
                            ConfigMode.TEMPLATE_NOT_CHOOSABLE);

                    tauFilter = (Float) result.getValue(pTauFilter);
                    subSampling = (Integer) result.getValue(pSubSampling);

                    ProbePlotHelper.getInstance().setDefaultTauFilter(tauFilter);
                    ProbePlotHelper.getInstance().setDefaultSubSampling(subSampling);

                } catch (ConfigException e) {
                    e.defaultHandleBehavior();
                    return;
                }

            }

            TimeSeries timeSeriesToShow = timeSeries;

            if (subSampling != 0) {
                timeSeriesToShow = DataUtils.subsample(timeSeriesToShow, subSampling);
            }

            if (tauFilter != 0) {
                timeSeriesToShow = DataUtils.filter(timeSeriesToShow, tauFilter);
            }

            Plotter.plot(timeSeriesToShow, plotName);

        } catch (java.lang.NumberFormatException exception) {
            exception.printStackTrace();
            UserMessages.showWarning("Could not parse number");
        }

    }

}
