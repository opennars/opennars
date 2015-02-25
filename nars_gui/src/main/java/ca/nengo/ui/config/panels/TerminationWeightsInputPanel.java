/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "TerminationWeightsInputPanel.java". Description:
"Input panel for Termination Weights Matrix

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

package ca.nengo.ui.config.panels;

import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.PropertyInputPanel;
import ca.nengo.ui.config.descriptors.PCouplingMatrix;
import ca.nengo.ui.config.descriptors.PTerminationWeights;
import ca.nengo.ui.config.managers.ConfigManager;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Input panel for Termination Weights Matrix
 * 
 * @author Shu
 */
public class TerminationWeightsInputPanel extends PropertyInputPanel {

    /**
     * The termination weights matrix
     */
    private float[][] matrix;

    /**
     * Text field containing the user-entered dimensions of the weights
     */
    private JTextField tf;

    /**
     * @param property TODO
     */
    public TerminationWeightsInputPanel(PTerminationWeights property) {
        super(property);
        initPanel();
    }

    /**
     * @return The dimensions of this termination
     */
    private int getDimensions() {

        Integer integerValue = new Integer(tf.getText());
        return integerValue.intValue();

    }

    /**
     * @return True if dimensions have been set
     */
    private boolean isDimensionsSet() {
        String textValue = tf.getText();

        if (textValue == null || textValue.compareTo("") == 0) {
            return false;
        }

        try {
            @SuppressWarnings("unused")
            Integer value = getDimensions();

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * @param dimensions
     *            New dimensions
     */
    private void setDimensions(int dimensions) {
        tf.setText(String.valueOf(dimensions));
    }

    /**
     * Edits the termination weights matrix
     */
    protected void editMatrix() {
        if (!isDimensionsSet()) {
            UserMessages.showWarning("Input dimensions not set");
            return;
        }

        /*
         * get the JDialog parent
         */
        Container parent = getJPanel().getParent();
        while (parent != null) {
            if (parent instanceof JDialog) {
                break;
            }
            parent = parent.getParent();
        }

        if (parent != null && parent instanceof JDialog) {
            Property pCouplingMatrix;
            if (isValueSet()) {
                /*
                 * Create a property descriptor with a set matrix
                 */
                pCouplingMatrix = new PCouplingMatrix(getValue());
            }
            else {
                /*
                 * Create a property descriptor with no default value
                 */
                pCouplingMatrix = new PCouplingMatrix(getFromSize(), getToSize());
            }

            String configName = getFromSize() + " to " + getToSize() + " Coupling Matrix";

            try {
                ConfigResult result = ConfigManager.configure(
                        new Property[] { pCouplingMatrix }, configName, parent,
                        ConfigManager.ConfigMode.STANDARD);

                setValue(result.getValue(pCouplingMatrix));
            } catch (ConfigException e) {
                e.defaultHandleBehavior();
            }

        } else {
            UserMessages.showError("Could not attach properties dialog");
        }

    }

    /**
     * @return From size, of the matrix to be created
     */
    protected int getFromSize() {
        return getDimensions();
    }

    /**
     * @return To size, of the matrix to be created
     */
    protected int getToSize() {
        return getDescriptor().getEnsembleDimensions();
    }

    private void initPanel() {
        JLabel dimensions = new JLabel("Input Dim: ");
        tf = new JTextField(10);
        add(dimensions);
        add(tf);

        JButton configureFunction = new JButton(new EditMatrixAction());

        add(tf);
        add(configureFunction);
    }

    @Override
    public PTerminationWeights getDescriptor() {
        return (PTerminationWeights) super.getDescriptor();
    }

    @Override
    public float[][] getValue() {
        return matrix;
    }

    @Override
    public boolean isValueSet() {
        if (!isDimensionsSet()) {
            return false;
        }

        if (matrix != null && matrix[0].length == getDimensions()) {
            return true;
        } else if (getFromSize() == getToSize()){
            matrix = new float[getFromSize()][getToSize()];
            for (int i=0; i<getFromSize(); i++) {
                for (int j=0; j<getFromSize(); j++) {
                    if (i==j) {
                        matrix[i][j]=1;
                    } else {
                        matrix[i][j]=0;
                    }
                }
            }
            return true; // default to identity matrix when applicable, if not already set
        }
        setStatusMsg("Matrix not set");
        return false;
    }

    @Override
    public void setValue(Object value) {
        if ((value != null) && (value instanceof float[][])
                && (getToSize() == ((float[][]) value).length)) {
            matrix = (float[][]) value;
            setDimensions(matrix[0].length);

            if (isValueSet()) {
                setStatusMsg("");
            }
        } else {
            Util.debugMsg("Saved termination weights don't fit, they will be replaced");
        }
    }

    /**
     * User triggered action to edit the termination weights matrix
     * 
     * @author Shu Wu
     */
    class EditMatrixAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public EditMatrixAction() {
            super("Set Weights");
        }

        public void actionPerformed(ActionEvent e) {
            editMatrix();
        }
    }
}
