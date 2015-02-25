/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PlotFunctionNodeAction.java". Description:
""

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

import ca.nengo.math.Function;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.plot.Plotter;
import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.config.descriptors.PInt;
import ca.nengo.ui.config.managers.ConfigManager;
import ca.nengo.ui.config.managers.ConfigManager.ConfigMode;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.util.UIEnvironment;

/**
 * TODO
 * 
 * @author TODO
 */
public class PlotFunctionNodeAction extends StandardAction {
    private static final long serialVersionUID = 1L;

    static final Property pEnd = new PFloat("End");
    static final Property pIncrement = new PFloat("Increment");
    static final Property pStart = new PFloat("Start");
    // static final PropDescriptor pTitle = new PTString("Title");
    private final FunctionInput functionInput;
    private Property pFunctionIndex;
    private final String plotName;

    /**
     * @param plotName TODO
     * @param actionName TODO
     * @param functionInput TODO
     */
    public PlotFunctionNodeAction(String plotName, String actionName,
            FunctionInput functionInput) {
        super("Plot function input", actionName);
        this.plotName = plotName;
        this.functionInput = functionInput;

    }

    @Override
    protected void action() throws ActionException {
        pFunctionIndex = new PInt("Function index", 0, 0, functionInput
                .getFunctions().length - 1);
        Property[] propDescripters = { pFunctionIndex, pStart,
                pIncrement, pEnd };
        try {
            ConfigResult properties = ConfigManager.configure(propDescripters,
                    "Function plotter", UIEnvironment.getInstance(),
                    ConfigMode.TEMPLATE_NOT_CHOOSABLE);

            completeConfiguration(properties);

        } catch (ConfigException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param properties TODO
     * @throws ConfigException TODO
     */
    public void completeConfiguration(ConfigResult properties)
            throws ConfigException {
        String title = plotName + " - Function Plot";

        int functionIndex = (Integer) properties.getValue(pFunctionIndex);
        float start = (Float) properties.getValue(pStart);
        float end = (Float) properties.getValue(pEnd);
        float increment = (Float) properties.getValue(pIncrement);

        if (increment == 0) {
            throw new ConfigException(
                    "Cannot plot with infinite steps because step size is 0");
        }

        Function[] functions = functionInput.getFunctions();

        if (functionIndex >= functions.length) {
            throw new ConfigException("Function index out of bounds");

        }
        Function function = functionInput.getFunctions()[functionIndex];
        Plotter.plot(function, start, increment, end, title + " ("
                + function.getClass().getSimpleName() + ')');

    }
}