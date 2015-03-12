/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CutAction.java". Description:
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

import ca.nengo.model.Node;
import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.action.UserCancelledException;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.WorldObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 * 
 * @author Chris Eliasmith
 */
public class ClearAllAction extends StandardAction {

    /**
     * @param description TODO
     */
    public ClearAllAction(String description) {
        super(description);
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected final void action() throws UserCancelledException {
        int response = JOptionPane.showConfirmDialog(UIEnvironment
                .getInstance(),
                "Are you sure you want to remove all objects from Nengo?",
                "Clear all?", JOptionPane.YES_NO_OPTION);
        if (response == 0) {
            AbstractNengo nengo = AbstractNengo.getInstance();
            Iterable<WorldObject> modelsToRemove = nengo.getWorld().getGround().getChildren();
            Iterator<WorldObject> iter = modelsToRemove.iterator();
            List<WorldObject> copy = new ArrayList<WorldObject>();
            while (iter.hasNext())
                copy.add(iter.next());
            for (WorldObject modelToRemove : copy) {
        		nengo.removeNodeModel((Node) ((ModelObject) modelToRemove).node());
        	}
            
//            //clear script console
//            nengo.getScriptConsole().reset(false);
            
        } else {
            throw new UserCancelledException();
        }
    }

}
