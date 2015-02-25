/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FnCustom.java". Description:
"Property descriptor for a function expression.

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

package ca.nengo.ui.config.descriptors.functions;

import ca.nengo.math.Function;
import ca.nengo.math.FunctionInterpreter;
import ca.nengo.math.impl.DefaultFunctionInterpreter;
import ca.nengo.math.impl.PostfixFunction;
import ca.nengo.ui.action.PlotFunctionAction;
import ca.nengo.ui.config.*;
import ca.nengo.ui.config.descriptors.PFunction;
import ca.nengo.ui.config.descriptors.PInt;
import ca.nengo.ui.config.descriptors.PString;
import ca.nengo.ui.config.managers.ConfigDialog;
import ca.nengo.ui.config.managers.ConfigManager;
import ca.nengo.ui.config.managers.UserConfigurer;
import ca.nengo.ui.config.panels.StringPanel;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.UserMessages;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * TODO
 * 
 * @author TODO
 */
public class FnCustom extends AbstractFn {

	private static final String DIMENSION_STR = "Input Dimensions";
	private static final String DIMENSION_DESC = "The number of input dimensions to the function";

    private static final String EXPRESSION_STR = "Expression";
    private static final String EXPRESSION_DESC = "Expression to evaluate. " + 
    	"Refer to ensemble values as \"x#\", where # is the dimension index " + 
    	"(e.g., x0*x0 returns the square of the first dimension).<br>" +
    	"(Full documentation: <a href=\"http://nengo.ca/docs/html/tutorial3.html\">http://nengo.ca/docs/html/tutorial3.html</a>)";
    private static final DefaultFunctionInterpreter interpreter = new DefaultFunctionInterpreter();

    private final int myInputDimensions;
    private Property pExpression;
    private Property pDimensions;
    InterpreterFunctionConfigurer configurer;
    final boolean isInputDimEditable;

    /**
     * @param inputDimensions TODO
     * @param isInputDimEditable TODO
     */
    public FnCustom(int inputDimensions, boolean isInputDimEditable) {
        super("User-defined Function", PostfixFunction.class);
        this.myInputDimensions = inputDimensions;
        this.isInputDimEditable = isInputDimEditable;
    }

    private Function parseFunction(ConfigResult props) throws ConfigException {
        String expression = (String) props.getValue(pExpression);
//        int dimensions = (Integer) props.getValue(DIMENSION_STR);
        int dimensions = (Integer) props.getValue(pDimensions);

        Function function;
        try {
            function = interpreter.parse(expression, dimensions);
        } catch (Exception e) {
            throw new ConfigException(e.getMessage());
        }

        return function;
    }

    @Override
    protected Function createFunction(ConfigResult props) throws ConfigException {
        return parseFunction(props);
    }

    @Override
    public Function configureFunction(Dialog parent) {
        if (configurer == null) {
            configurer = new InterpreterFunctionConfigurer(this, parent, interpreter);
        }
        try {
            configurer.configureAndWait();
            return getFunction();
        } catch (ConfigException e) {
            e.defaultHandleBehavior();
        }
        return null;
    }

    public ConfigSchema getSchema() {
        String expression = null;
        int dim = myInputDimensions;

        PostfixFunction function = getFunction();

        if (function != null) {
            expression = function.getExpression();

            if (isInputDimEditable) {
                dim = function.getDimension();
            }
        }

        pExpression = new PString(EXPRESSION_STR, EXPRESSION_DESC, expression);
        pDimensions = new PInt(DIMENSION_STR, DIMENSION_DESC, dim);
        pDimensions.setEditable(isInputDimEditable);

        Property[] props = new Property[] { pExpression, pDimensions };
        return new ConfigSchemaImpl(props);
    }

    @Override
    public PostfixFunction getFunction() {
        return (PostfixFunction) super.getFunction();
    }

    @Override
    public void preConfiguration(ConfigResult props) throws ConfigException {
        /*
         * Try to parse the expression and throw an exception if it dosen't
         * succeed
         */
        parseFunction(props);
    }

    /**
     * Property descriptor for a function expression.
     * 
     * @author Shu Wu
     */
    static class PExpression extends PString {
        private static final long serialVersionUID = 1L;

        public PExpression(String name) {
            super(name);
        }

        @Override
        protected PropertyInputPanel createInputPanel() {
            /*
             * This custom String Panel will check that the expression is
             * correct before returning that value is set. This allows a UI to
             */

            return new StringPanel(this) {
                @Override
                public boolean isValueSet() {
                    return super.isValueSet();
                }

            };
        }

    }
}

/**
 * This Configurer uses a custom panel for registering new functions
 * 
 * @author Shu Wu
 */
class InterpreterFunctionConfigurer extends UserConfigurer {
    final FunctionInterpreter interpreter;
    final Dialog parent;

    public InterpreterFunctionConfigurer(IConfigurable configurable, Dialog parent,
            FunctionInterpreter interpreter) {
        super(configurable, parent);
        this.interpreter = interpreter;
        this.parent = parent;
    }

    @Override
    protected ConfigDialog createConfigDialog() {
        return new FunctionDialog(this, parent);
    }

    /**
     * This config dialog contains additional elements for configuring
     * registered functions
     * 
     * @author Shu Wu
     */
    class FunctionDialog extends ConfigDialog {

        private static final long serialVersionUID = 1L;

        private JComboBox registeredFunctionsList;

        public FunctionDialog(UserConfigurer configManager, Dialog owner) {
            super(configManager, owner);

        }

        @Override
        protected void initPanelBottom(JPanel panel) {

            JPanel savedFilesPanel = new JCustomPanel();

            JPanel dropDownPanel = new JCustomPanel();

            Map<String, Function> reigsteredFunctions = interpreter.getRegisteredFunctions();

            registeredFunctionsList = new JComboBox(reigsteredFunctions.keySet().toArray());

            savedFilesPanel.add(new JLabel("Registered Functions"));

            dropDownPanel.add(registeredFunctionsList);
            dropDownPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            savedFilesPanel.add(dropDownPanel);

            JPanel buttonsPanel = new JCustomPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
            buttonsPanel.add(Box.createHorizontalGlue());
            buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 5));

            JButton button;
            button = new JButton("New");
            button.addActionListener(new NewFunctionAL());
            button.setFont(NengoStyle.FONT_SMALL);
            buttonsPanel.add(button);

            button = new JButton("Remove");
            button.addActionListener(new RemoveFunctionAL());
            button.setFont(NengoStyle.FONT_SMALL);
            buttonsPanel.add(button);

            button = new JButton("Preview");
            button.addActionListener(new PreviewFunctionAL());
            button.setFont(NengoStyle.FONT_SMALL);
            buttonsPanel.add(button);

            savedFilesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            savedFilesPanel.add(buttonsPanel);

            JPanel wrapperPanel = new JCustomPanel();
            wrapperPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            wrapperPanel.add(savedFilesPanel);

            JPanel seperator = new JCustomPanel();
            seperator.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            panel.add(wrapperPanel);
            panel.add(seperator);
        }

        class NewFunctionAL implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                try {
                    PString pFnName = new PString("Name");
                    PFunction pFunction = new PFunction("New Function", 1, true, null);

                    ConfigResult props = ConfigManager.configure(new Property[] { pFnName,
                            pFunction }, "Register fuction", FunctionDialog.this,
                            ConfigMode.TEMPLATE_NOT_CHOOSABLE);

                    String name = (String) props.getValue(pFnName);
                    Function fn = (Function) props.getValue(pFunction);

                    interpreter.registerFunction(name, fn);
                    registeredFunctionsList.addItem(name);
                } catch (ConfigException e1) {
                    e1.defaultHandleBehavior();
                }
            }
        }

        class PreviewFunctionAL implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                String functionName = (String) registeredFunctionsList.getSelectedItem();

                if (functionName != null) {
                    Function function = interpreter.getRegisteredFunctions().get(functionName);

                    if (function != null) {
                        PlotFunctionAction action = new PlotFunctionAction("Function preview",
                                function, FunctionDialog.this);
                        action.doAction();

                    } else {
                        UserMessages.showWarning("No function selected");
                    }
                }
            }
        }

        class RemoveFunctionAL implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String functionName = (String) registeredFunctionsList.getSelectedItem();
                if (functionName != null) {
                    interpreter.removeRegisteredFunction(functionName);
                    registeredFunctionsList.removeItem(functionName);
                } else {
                    UserMessages.showWarning("No function selected");
                }
            }

        }
    }

}

/**
 * A JPanel which has some commonly used settings
 * 
 * @author Shu
 */
class JCustomPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public JCustomPanel() {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(TOP_ALIGNMENT);
        setAlignmentX(LEFT_ALIGNMENT);
    }

}