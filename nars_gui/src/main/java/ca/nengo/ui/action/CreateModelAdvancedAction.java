/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CreateModelAdvancedAction.java". Description:
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

import ca.nengo.config.ui.NewConfigurableDialog;
import ca.nengo.model.Node;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.action.UserCancelledException;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.model.NodeContainer.ContainerException;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINodeViewable;

/**
 * TODO
 * 
 * @author TODO
 */
public class CreateModelAdvancedAction extends StandardAction {
    private final Class<?> objType;
    private final NodeContainer container;

    /**
     * @param container TODO
     * @param objType TODO
     */
    public CreateModelAdvancedAction(NodeContainer container, Class<?> objType) {
        super(objType.getSimpleName());
        this.objType = objType;
        this.container = container;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void action() throws ActionException {
        Object obj = NewConfigurableDialog.showDialog(UIEnvironment.getInstance(), objType, null);
        if (obj == null) {
            throw new UserCancelledException();
        } else if (obj instanceof Node) {
            Node node = (Node) obj;
            try {
                CreateModelAction.ensureNonConflictingName(node, container);
                UINeoNode nodeUI = container.addNodeModel(node);
                if (nodeUI instanceof UINodeViewable) {
                    ((UINodeViewable) (nodeUI)).openViewer();
                }
            } catch (ContainerException e) {
                throw new ActionException(e.getMessage(), e);
            }
        } else {
            throw new ActionException("Sorry we do not support adding that type of object yet");
        }

    }
}