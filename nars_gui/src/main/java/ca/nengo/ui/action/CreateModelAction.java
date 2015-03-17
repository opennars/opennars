/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CreateModelAction.java". Description:
"Creates a new NEO model

  @author Shu"

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
import ca.nengo.model.StructuralException;
import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.ReversableAction;
import ca.nengo.ui.lib.action.UserCancelledException;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.model.NodeContainer.ContainerException;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.build.AbstractConstructable;
import ca.nengo.ui.model.build.CNode;
import ca.nengo.ui.model.build.ModelFactory;
import ca.nengo.ui.model.node.UINEFGroup;
import ca.nengo.ui.model.node.UINodeViewable;

import javax.swing.*;

/**
 * Creates a new NEO model
 * 
 * @author Shu
 */
public class CreateModelAction extends ReversableAction {

    private static final long serialVersionUID = 1L;

    private Double posX;
    private Double posY;


    /**
     * Prompts the user to select a non-conflicting name
     * 
     * @param node
     * @param container
     * @throws UserCancelledException
     *             If the user cancels
     */
    public static void ensureNonConflictingName(Node node, NodeContainer container)
            throws UserCancelledException {
        String newName = node.name();
        int i = 0;

        // Check if the name itself is valid
        while (true) {
	        try {
	            node.setName(newName);
	            break;
	        } catch (StructuralException e) {
	            newName = JOptionPane.showInputDialog(UIEnvironment.getInstance(),
	                    "Names cannot contain '.' or ':', please enter a new name", newName);
	            if (newName == null || newName.isEmpty()) {
	                throw new UserCancelledException();
	            }
	        }
        }
        String originalName = node.name();
        
        
        while (container.getNodeModel(newName) != null) {
            // Avoid duplicate names
            while (container.getNodeModel(newName) != null) {
                i++;
                newName = originalName + " (" + i + ')';

            }
            newName = JOptionPane.showInputDialog(UIEnvironment.getInstance(),
                    "Node already exists, please enter a new name", newName);

            if (newName == null || newName.isEmpty()) {
                throw new UserCancelledException();
            }
        }

        try {
            node.setName(newName);
        } catch (StructuralException e) {
            e.printStackTrace();
        }
    }

    /**
     * Node constructable
     */
    private final AbstractConstructable constructable;

    /**
     * Container to which the created node shall be added
     */
    private final NodeContainer container;

    /**
     * @param nodeContainer
     *            The container to which the created node should be added to
     * @param constructable
     *            Type of Node to be create, such as PNetwork
     */
    public CreateModelAction(NodeContainer nodeContainer, CNode constructable) {
        this(constructable.getTypeName(), nodeContainer, constructable);
    }

    /**
     * @param modelTypeName TODO
     * @param nodeContainer
     *            The container to which the created node should be added to
     * @param constructable
     *            Type of Node to be create, such as PNetwork
     */
    public CreateModelAction(String modelTypeName, NodeContainer nodeContainer,
            AbstractConstructable constructable) {
        super("Create new " + modelTypeName, modelTypeName, false);
        this.container = nodeContainer;
        this.constructable = constructable;
    }

    private UINeoNode nodeCreated;

    @Override
    protected void action() throws ActionException {
        try {

            Object model = ModelFactory.constructModel(constructable);
            if (model == null) {
            	throw new ActionException("No model was created");
            } else if (model instanceof Node) {
                final Node node = (Node) model;

                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            ensureNonConflictingName(node, container);
                            try {
                                nodeCreated = container.addNodeModel(node,posX,posY);
                                if (nodeCreated instanceof UINodeViewable) {
                                    if (nodeCreated instanceof UINEFGroup) {
                                        // don't open NEFEnsembles
                                    } else {
                                        ((UINodeViewable) (nodeCreated)).openViewer();
                                    }
                                }
                            } catch (ContainerException e) {
                                UserMessages.showWarning("Could not add node: " + e.getMessage());
                            }
                        } catch (UserCancelledException e) {
                            e.defaultHandleBehavior();
                        }

                    }
                });

            } else {
                throw new ActionException("Can not add model of the type: "
                        + model.getClass().getSimpleName());
            }

        } catch (ConfigException e) {
            e.defaultHandleBehavior();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException(e.getMessage(), e);
        }
    }

    /**
     * @param x TODO
     * @param y TODO
     */
    public void setPosition(double x, double y) {
        posX=new Double(x);
        posY=new Double(y);
    }

    @Override
    protected void undo() throws ActionException {

        if (nodeCreated != null) {
            nodeCreated.destroy();
        }

    }
}
