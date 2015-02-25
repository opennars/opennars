/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FunctionPanel.java". Description:
"Input Panel for editing an individual Function

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

package ca.nengo.ui.config.panels;

import ca.nengo.math.Function;
import ca.nengo.ui.action.PlotFunctionAction;
import ca.nengo.ui.config.PropertyInputPanel;
import ca.nengo.ui.config.descriptors.PFunction;
import ca.nengo.ui.config.descriptors.functions.AbstractFn;
import ca.nengo.ui.config.descriptors.functions.ConfigurableFunction;
import ca.nengo.ui.config.descriptors.functions.FnAdvanced;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Input Panel for editing an individual Function
 * 
 * @author Shu Wu
 */
public class FunctionPanel extends PropertyInputPanel {

    /**
     * Combo box for selecting a function type, types of function are stored in
     * PTFunction.functions
     */
    private JComboBox comboBox;

    private final ConfigurableFunction[] configurableFunctionsList;

    /**
     * Function
     */
    private Function function = new ca.nengo.math.impl.ConstantFunction(1,0f);

    private JButton newBtn;

    private JButton previewBtn;
    private JButton configureBtn;
    /**
     * Currently selected item in the comboBox
     */
    private ConfigurableFunction selectedConfigurableFunction;

    /**
     * @param property TODO
     * @param functions TODO
     */
    public FunctionPanel(PFunction property, ConfigurableFunction[] functions) {
        super(property);
        this.configurableFunctionsList = functions;

        initPanel();
    }

    private void initPanel() {
        comboBox = new JComboBox(configurableFunctionsList);
        selectedConfigurableFunction = (AbstractFn) comboBox.getSelectedItem();

        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (comboBox.getSelectedItem() != selectedConfigurableFunction) {
                    setValue(null);
                    updateSelection(comboBox.getSelectedItem());
                }
            }
        });

        add(comboBox);

        newBtn = new JButton(new NewParametersAction());
        add(newBtn);

        configureBtn = new JButton(new EditAction());
        add(configureBtn);

        previewBtn = new JButton(new PreviewFunctionAction());
        add(previewBtn);

        updateSelection(comboBox.getSelectedItem());
    }

    private void updateSelection(Object selectedItem) {
        selectedConfigurableFunction = (ConfigurableFunction) comboBox.getSelectedItem();

        if (selectedItem instanceof FnAdvanced) {
            newBtn.setEnabled(true);
        } else {
            newBtn.setEnabled(false);
        }

    }

    /**
     * Previews the function
     */
    protected void previewFunction() {

        if (function != null) {
            (new PlotFunctionAction("Function preview", function, getDialogParent())).doAction();
        } else {
            UserMessages.showWarning("Please set this function first.");
        }
    }

    /**
     * Sets up the function using the configurable Function wrapper
     * 
     * @param resetValue
     *            Whether to reset the ConfigurableFunction's value before
     *            editing
     */
    protected void setParameters(boolean resetValue) {

        /*
         * get the JDialog parent
         */
        JDialog parent = getDialogParent();

        if (parent != null) {
            if (resetValue) {
                selectedConfigurableFunction.setFunction(null);
            }

            /*
             * Configure the function
             */
            Function function = selectedConfigurableFunction.configureFunction(parent);

            setValue(function);
        } else {
            UserMessages.showError("Could not attach properties dialog");
        }

    }

    @Override
    public PFunction getDescriptor() {
        return (PFunction) super.getDescriptor();
    }

    @Override
    public Function getValue() {
        return function;
    }

    @Override
    public boolean isValueSet() {
        if (function != null) {

            if (function.getDimension() != getDescriptor().getInputDimension()) {
                setStatusMsg("Input dimension must be " + getDescriptor().getInputDimension()
                        + ", it is currently " + function.getDimension());
                return false;
            }
            return true;

        } else {
            setStatusMsg("Function parameters not set");

            return false;
        }

    }

    @Override
    public void setValue(Object value) {

        if (value != null && value instanceof Function) {

            function = (Function) value;
            boolean configurableFunctionFound = false;

            /*
             * Updates the combo box to reflect the function type set
             */
            for (ConfigurableFunction element : configurableFunctionsList) {

                if (element.getFunctionType().isInstance(function)) {
                    selectedConfigurableFunction = element;
                    selectedConfigurableFunction.setFunction(function);

                    comboBox.setSelectedItem(selectedConfigurableFunction);
                    configurableFunctionFound = true;
                    break;
                }
            }

            if (!configurableFunctionFound) {
                Util.Assert(false, "Unsupported function");
            }

            if (isValueSet()) {
                setStatusMsg("");
            }

        } else {
            function = new ca.nengo.math.impl.ConstantFunction(1,0f);
        }

    }

    /**
     * Set up the parameters of a new function
     * 
     * @author Shu Wu
     */
    class NewParametersAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public NewParametersAction() {
            super("New");
        }

        public void actionPerformed(ActionEvent e) {
            setParameters(true);
        }

    }

    /**
     * Preview the funciton
     * 
     * @author Shu Wu
     */
    class PreviewFunctionAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public PreviewFunctionAction() {
            super("Preview");
        }

        public void actionPerformed(ActionEvent e) {
            previewFunction();
        }

    }

    /**
     * Set up the parameters of the existing function
     * 
     * @author Shu Wu
     */
    class EditAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public EditAction() {
            super("Set");
        }

        public void actionPerformed(ActionEvent e) {
            setParameters(false);
        }

    }

}
