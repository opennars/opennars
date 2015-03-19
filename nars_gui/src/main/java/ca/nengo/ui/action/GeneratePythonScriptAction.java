/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SaveNodeAction.java". Description:
"Saves a PNodeContainer

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

import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.action.UserCancelledException;
import ca.nengo.ui.lib.object.activity.TrackedAction;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.model.UINeoNode;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Generates a script from a UINeoNode
 * 
 * @author Chris Eliasmith
 */
public class GeneratePythonScriptAction extends StandardAction {
    private static final long serialVersionUID = 1L;

    /**
     * File to be saved to
     */
    private File file;

    private final UINeoNode nodeUI;

    /**
     * Whether or not the last save attempt was successful.
     */
    private boolean saveSuccessful;

    /**
     * @param nodeUI
     *            Node to be saved
     */
    public GeneratePythonScriptAction(UINeoNode nodeUI) {
        this(nodeUI, false);
    }

    /**
     * @param nodeUI
     *            Node to be saved
     */
    public GeneratePythonScriptAction(UINeoNode nodeUI, boolean isBlocking) {
        super("Generate script for " + nodeUI.name());

        this.nodeUI = nodeUI;
    }

    @Override
    protected void action() throws ActionException {
        saveSuccessful = false;
        int returnVal = JFileChooser.CANCEL_OPTION;

        AbstractNengo.getFileChooser().setSelectedFile(new File(nodeUI.getFileName().replace(' ', '_').replace(".nef", "") + ".py"));
		
        returnVal = AbstractNengo.getFileChooser().showSaveDialog();

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = AbstractNengo.getFileChooser().getSelectedFile();
            new TrackedAction("Generating script") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void action() throws ActionException {
                    saveSuccessful = generateScript();
                }
            }.doAction();
        } else {
            throw new UserCancelledException();
        }
    }

    private boolean generateScript() {
        try {
            nodeUI.generateScript(file);
        } catch (IOException e) {
            UserMessages.showError("Could not generate script: " + e.toString());
            return false;
        } catch (OutOfMemoryError e) {
            UserMessages.showError("Out of memory, please increase memory size: " + e.toString());
            return false;
        }
        return true;
    }

    /**
     * @return TODO
     */
    public boolean getSaveSuccessful() {
        return saveSuccessful;
    }
}