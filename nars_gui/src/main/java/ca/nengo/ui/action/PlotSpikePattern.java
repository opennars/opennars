/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PlotSpikePattern.java". Description:
"Action for Plotting the Spike Pattern

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
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.util.SpikePattern;

/**
 * Action for Plotting the Spike Pattern
 * 
 * @author Shu Wu
 */
public class PlotSpikePattern extends StandardAction {

    private static final long serialVersionUID = 1L;
    final SpikePattern spikePattern;

    /**
     * @param spikePattern TODO
     */
    public PlotSpikePattern(SpikePattern spikePattern) {
        super("Plot spike pattern", "Plot spikes");
        this.spikePattern = spikePattern;
    }

    @Override
    protected void action() throws ActionException {
        Plotter.plot(spikePattern);
    }

}