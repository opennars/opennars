/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ExampleRunner.java". Description: 
"Used to conveniently create a NeoGraphics instance with an existing Network
  model
  
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

package ca.nengo.test.depr;

import ca.nengo.model.Network;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.object.activity.TrackedStatusMsg;
import ca.nengo.ui.model.node.UINetwork;

import javax.swing.*;

/**
 * Used to conveniently create a NeoGraphics instance with an existing Network
 * model
 * 
 * @author Shu Wu
 */
abstract public class ExampleRunner extends Nengrow {

	private UINetwork networkUI;

	public ExampleRunner() {
        super();

		/**
		 * All UI funcitons and constructors must be invoked from the Swing
		 * Event Thread
		 */

	}


	protected void doStuff(UINetwork network) {

	}



	protected void processNetwork(UINetwork network) {

	}

    public abstract Network getNetwork();

    @Override
    public void init() throws Exception {

        TrackedStatusMsg task;
        task = new TrackedStatusMsg("Creating Model UI");
        if (networkUI == null) {

            networkUI = new UINetwork(getNetwork());
            getWorld().getGround().addChild(networkUI);
            networkUI.openViewer();
        }

        processNetwork(networkUI);
        task.finished();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                doStuff(networkUI);
            }
        });
    }
}
