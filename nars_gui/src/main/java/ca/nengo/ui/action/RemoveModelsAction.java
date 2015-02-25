/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "RemoveModelsAction.java". Description:
"Action for removing a collection of UI Wrappers and their models

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

import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.util.UIEnvironment;

import javax.swing.*;
import java.util.Collection;

/**
 * Action for removing a collection of UI Wrappers and their models
 * 
 * @author Shu Wu
 */
public class RemoveModelsAction extends StandardAction {

    private static final long serialVersionUID = 1L;
    private final Collection<ModelObject> objectsToRemove;
    private final String typeName;
    private final boolean showWarning;

    /**
     * @param objectsToRemove TODO
     */
    public RemoveModelsAction(Collection<ModelObject> objectsToRemove) {
        this(objectsToRemove, "Objects", false);
    }

    /**
     * @param objectsToRemove TODO
     * @param typeName TODO
     * @param showWarning TODO
     */
    public RemoveModelsAction(Collection<ModelObject> objectsToRemove, String typeName,
            boolean showWarning) {
        super("Remove " + typeName + 's');
        this.objectsToRemove = objectsToRemove;
        this.typeName = typeName;
        this.showWarning = showWarning;
    }

    @Override
    protected void action() throws ActionException {
        boolean remove = true;
        if (showWarning) {
            int response = JOptionPane.showConfirmDialog(UIEnvironment.getInstance(), "Once these "
                    + typeName + "s have been removed, it cannot be undone.", "Are you sure?",
                    JOptionPane.YES_NO_OPTION);

            if (response != 0) {
                remove = false;
            }
        }
        if (remove) {
            try {
                for (ModelObject model : objectsToRemove) {
                    model.destroyModel();
                }
            } catch (Exception e) {
                throw new ActionException("Could not remove all objects: " + e.getMessage(), e);
            }
        } else {
            throw new ActionException("Action cancelled by user", false);
        }
    }
}