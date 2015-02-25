/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FunctionArrayPanel.java". Description:
"Input panel for entering an Array of Functions

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
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.ui.config.*;
import ca.nengo.ui.config.descriptors.PFunction;
import ca.nengo.ui.config.descriptors.PFunctionArray;
import ca.nengo.ui.config.managers.UserConfigurer;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Input panel for entering an Array of Functions
 * 
 * @author Shu Wu
 */
public class FunctionArrayPanel extends PropertyInputPanel {

    /**
     * Function array
     */
    private Function[] myFunctionsWr;

    /**
     * Text field component for entering the dimensions of the function array
     */
    private JTextField tf;
    private final int inputDimension;

    /**
     * @param property TODO
     * @param inputDimension TODO
     */
    public FunctionArrayPanel(PFunctionArray property, int inputDimension) {
        super(property);
        initPanel();
        this.inputDimension = inputDimension;
    }

    /**
     * Edits the Function Array using a child dialog
     */
    protected void editFunctionArray() {
        if (!isOutputDimensionsSet()) {
            UserMessages.showWarning("Output dimensions not set");
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
            ConfigurableFunctionArray configurableFunctions = new ConfigurableFunctionArray(
                    getInputDimension(), getOutputDimension(), getValue());

            UserConfigurer config = new UserConfigurer(configurableFunctions, parent);
            try {
                config.configureAndWait();
                setValue(configurableFunctions.getFunctions());
            } catch (ConfigException e) {
                e.defaultHandleBehavior();
            }

        } else {
            UserMessages.showError("Could not attach properties dialog");
        }

    }

    @Override
    public PFunctionArray getDescriptor() {
        return (PFunctionArray) super.getDescriptor();
    }

    /**
     * @return TODO
     */
    public int getOutputDimension() {
        Integer integerValue = new Integer(tf.getText());
        return integerValue.intValue();
    }

    @Override
    public Function[] getValue() {
        return myFunctionsWr;
    }

    private void initPanel() {
        JLabel dimensions = new JLabel("Output Dimensions: ");
        tf = new JTextField(10);
        add(dimensions);
        add(tf);

        JButton configureFunction = new JButton(new EditFunctions());
        add(tf);
        add(configureFunction);

    }

    /**
     * @return True if Function Array dimensions has been set
     */
    public boolean isOutputDimensionsSet() {
        String textValue = tf.getText();

        if (textValue == null || textValue.compareTo("") == 0) {
            return false;
        }

        try {
            @SuppressWarnings("unused")
            Integer value = getOutputDimension();

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isValueSet() {
    	if (!isOutputDimensionsSet()) {
    		return false;
    	}
    	
        if (myFunctionsWr != null && (myFunctionsWr.length == getOutputDimension())) {
            return true;
        } else {
            setStatusMsg("Functions not set");
        }
        
        if (myFunctionsWr == null || myFunctionsWr.length != getOutputDimension()) {
            myFunctionsWr = new Function[getOutputDimension()];
            for (int i=0; i<getOutputDimension(); i++) {
                myFunctionsWr[i] = new ConstantFunction(inputDimension, 0.0f);
            }
            return true;
        }

        return false;
    }

    /**
     * @param dimensions
     *            Dimensions of the function array
     */
    public void setDimensions(int dimensions) {
        tf.setText(String.valueOf(dimensions));

    }

    @Override
    public void setValue(Object value) {
        Function[] functions = (Function[]) value;

        /*
         * Check that the functions are of the correct dimension before
         * committing
         */
        for (Function function : functions) {
            if (function.getDimension() != getInputDimension()) {
                Util.debugMsg("Saved functions are of a different dimension, they can't be used");
                return;
            }
        }

        if (value != null) {
            myFunctionsWr = functions;
            setDimensions(myFunctionsWr.length);
            setStatusMsg("");
        } else {

        }
    }

    /**
     * Edit Functions Action
     * 
     * @author Shu Wu
     */
    class EditFunctions extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public EditFunctions() {
            super("Set Functions");
        }

        public void actionPerformed(ActionEvent e) {
            editFunctionArray();

        }

    }

    /**
     * @return TODO
     */
    public int getInputDimension() {
        return inputDimension;
    }
}

/**
 * Configurable object which creates an array of functions
 * 
 * @author Shu Wu
 */
/**
 * @author Shu
 */
class ConfigurableFunctionArray implements IConfigurable {

    /**
     * Number of functions to be created
     */
    private int outputDimension;

    /**
     * Dimensions of the functions to be created
     */
    private int inputDimension;

    /**
     * Array of functions to be created
     */
    private Function[] myFunctions;

    private final Function[] defaultValues;

    /**
     * @param inputDimension TODO
     * @param outputDimension
     *            Number of functions to create
     * @param defaultValues TODO
     */
    public ConfigurableFunctionArray(int inputDimension, int outputDimension,
            Function[] defaultValues) {
        super();
        this.defaultValues = defaultValues;
        init(inputDimension, outputDimension);

    }

    /**
     * Initializes this instance
     * 
     * @param outputDimension
     *            number of functions to create
     */
    private void init(int inputDimension, int outputDimension) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.nengo.ui.configurable.IConfigurable#completeConfiguration(ca.nengo.ui.configurable.ConfigParam)
     */
    public void completeConfiguration(ConfigResult properties) {
        myFunctions = new Function[outputDimension];
        for (int i = 0; i < outputDimension; i++) {
            myFunctions[i] = ((Function) properties.getValue("Function " + i));

        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.nengo.ui.configurable.IConfigurable#getConfigSchema()
     */
    public ConfigSchema getSchema() {
        Property[] props = new Property[outputDimension];

        for (int i = 0; i < outputDimension; i++) {

            Function defaultValue = null;

            if (defaultValues != null && i < defaultValues.length && defaultValues[i] != null) {
                defaultValue = defaultValues[i];

            }
            PFunction function = new PFunction("Function " + i, inputDimension, false, defaultValue);
            function.setDescription("The function to use for dimension "+i);

            props[i] = function;
        }

        return new ConfigSchemaImpl(props);
    }

    /**
     * @return Functions created
     */
    public Function[] getFunctions() {
        return myFunctions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.nengo.ui.configurable.IConfigurable#getTypeName()
     */
    public String getTypeName() {
        return outputDimension + "x Functions";
    }

    public void preConfiguration(ConfigResult props) throws ConfigException {
        // do nothing
    }

    public String getDescription() {
        return getTypeName();
    }
	public String getExtendedDescription() {
		return null;
	}
    

}
