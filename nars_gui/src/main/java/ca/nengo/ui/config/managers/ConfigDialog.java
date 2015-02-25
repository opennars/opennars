/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigDialog.java". Description:
"Configuration dialog

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

package ca.nengo.ui.config.managers;

import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.config.*;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.object.activity.TrackedAction;
import ca.nengo.ui.lib.util.UserMessages;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Configuration dialog
 * 
 * @author Shu Wu
 */
public class ConfigDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel myPanel;
    private JPanel myPropertyPanel;

    /**
     * Parent ConfigurationManager
     */
    private UserConfigurer myConfigManager;

    protected Vector<PropertyInputPanel> propertyInputPanels;

    /**
     * @param configManager
     *            Parent Configuration Manager
     * @param owner
     *            Component this dialog shall be added to
     */
    public ConfigDialog(UserConfigurer configManager, Frame owner) {
        super(owner, configManager.getConfigurable().getDescription());

        initialize(configManager, owner);

    }

    /**
     * @param configManager
     *            Parent Configuration Manager
     * @param owner
     *            Component this dialog shall be added to
     */
    public ConfigDialog(UserConfigurer configManager, Dialog owner) {
        super(owner, configManager.getConfigurable().getDescription());

        initialize(configManager, owner);

    }

    /**
     * @param setPropertyFields
     *            if True, the user's values will be applied to the properties
     *            set
     * @return Whether the user has set all the values on the dialog correctly
     */
    private boolean processPropertiesInternal(boolean setPropertyFields, boolean showMessage) {
        Iterator<PropertyInputPanel> it = propertyInputPanels.iterator();

        while (it.hasNext()) {
            PropertyInputPanel inputPanel = it.next();
            Property property = inputPanel.getDescriptor();

            if (inputPanel.isValueSet()) {
                if (setPropertyFields) {

                    myConfigManager.setProperty(property.getName(), inputPanel.getValue());
                }
            } else {
                if (showMessage) {
                    UserMessages.showWarning(property.getName() + " is not set or is incomplete");
                }
                pack();
                return false;
            }

        }
        pack();
        return true;
    }

    /**
     * User wants to cancel the configuration
     */
    private void cancelAction() {

        //setVisible(false);   // doing both this and dispose() seems to cause problems in OpenJDK (the setVisible(true) call never returns)

        myConfigManager.dialogConfigurationFinished(new ConfigDialogClosedException());
        super.dispose();
    }

    /**
     * Creates ok, cancel buttons on the dialog
     */
    private void createButtons(JPanel panel) {
        JPanel buttonsPanel = new VerticalLayoutPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 5));

        JButton addToWorldButton = new JButton("Ok");
        ActionListener okActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okAction();
            }
        };
        addToWorldButton.addActionListener(okActionListener);
        addToWorldButton.registerKeyboardAction(okActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        buttonsPanel.add(addToWorldButton);

        JButton cancelButton = new JButton("Cancel");
        ActionListener cancelActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelAction();
            }
        };
        cancelButton.addActionListener(cancelActionListener);
        cancelButton.registerKeyboardAction(cancelActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        buttonsPanel.add(cancelButton);

        advancedButton = new JButton("Advanced");
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                advancedAction();
            }
        });
        buttonsPanel.add(advancedButton);

        if (myConfigManager.getConfigurable().getSchema().getAdvancedProperties().size() == 0) {
            advancedButton.setVisible(false);
        }

        panel.add(buttonsPanel);
    }

    private JButton advancedButton;

    private boolean isAdvancedShown = false;

    private void advancedAction() {
        if (!isAdvancedShown) {
            isAdvancedShown = true;
            List<Property> advancedDescriptors = myConfigManager.getConfigurable().getSchema()
                    .getAdvancedProperties();

            addDescriptors(advancedDescriptors);
        }
        // hide the button once it's been pressed
        advancedButton.setVisible(false);
    }

    private Component owner;

    /**
     * Initialization to be called from the constructor
     * 
     * @param configManager
     *            Configuration manager parent
     * @param owner
     *            Component the dialog is to be added to
     */
    protected void initialize(UserConfigurer configManager, Component owner) {
        this.myConfigManager = configManager;
        this.owner = owner;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                cancelAction();

            }
        });

        setResizable(false);
        setModal(true);

        myPanel = new VerticalLayoutPanel();
        myPanel.setVisible(true);
        myPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initPanelTop(myPanel);

        myPropertyPanel = new VerticalLayoutPanel();
        myPanel.add(myPropertyPanel);

        ConfigSchema schema = configManager.getConfigurable().getSchema();
        addDescriptors(schema.getProperties());

        initPanelBottom(myPanel);

        createButtons(myPanel);

        //add(myPanel);

        while (true) {
            try {
                add(myPanel);
                break;
            } catch (RuntimeException e) {
                //e.printStackTrace();
                // Ubuntu 11.04 throws a sun.awt.X11.XException ~80% of the time here
            }
        }


        setMinimumSize(new Dimension(200, this.getHeight()));
        updateBounds();

    }

    private void updateBounds() {
        pack();
        setLocationRelativeTo(owner);
    }

    protected void completeConfiguration() throws ConfigException {
        myConfigManager.getConfigurable().completeConfiguration(createConfigResult());
    }

    private ConfigResult createConfigResult() {
        return new ConfigResult(myConfigManager.getProperties());
    }

    /**
     * What happens when the user presses the OK button
     */
    private void okAction() {
        if (applyProperties()) {
            boolean preConfigurationSuccess = true;
            try {
                myConfigManager.getConfigurable().preConfiguration(createConfigResult());
            } catch (ConfigException e1) {
                e1.defaultHandleBehavior();
                preConfigurationSuccess = false;
            }

            if (preConfigurationSuccess) {
                //setVisible(false);  // doing both this and dispose() seems to cause problems in OpenJDK (the setVisible(true) call never returns)
                dispose();

                (new TrackedAction("Configuring " + myConfigManager.getConfigurable().getTypeName()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void action() throws ActionException {
                        ConfigException configException = null;

                        try {
                            completeConfiguration();
                        } catch (ConfigException e) {
                            configException = e;                            
                        } catch (RuntimeException e) {
                        	Throwable cause=e;
                        	while (cause.getCause()!=null) cause=cause.getCause();
                        	configException = new ConfigException(cause.getMessage());
                        }
                        
                        if (configException!=null) {
                        	JOptionPane.showMessageDialog(AbstractNengo.getInstance(), configException.getMessage());
                        }

                        myConfigManager.dialogConfigurationFinished(configException);

                    }
                }).doAction();
            }
        }
    }

    protected boolean checkPropreties() {
        return processPropertiesInternal(false, false);
    }

    /**
     * Gets value entered in the dialog and applies them to the properties set
     * 
     * @return Whether operation was successful
     */
    protected boolean applyProperties() {
        /*
         * first check if all the fields have been set correctly, then set them
         */
        if (processPropertiesInternal(false, true)) {
            processPropertiesInternal(true, false);
            return true;
        }
        return false;

    }

    /**
     * Adds property descriptors to the panel
     */
    protected void addDescriptors(List<Property> propDescriptors) {
        if (propertyInputPanels == null) {
            propertyInputPanels = new Vector<PropertyInputPanel>(propDescriptors.size());
        }

        MutableAttributeSet properties = myConfigManager.getProperties();

        for (Property property : propDescriptors) {

            PropertyInputPanel inputPanel = property.getInputPanel();
            myPropertyPanel.add(inputPanel.getJPanel());

            /*
             * Try to get the configurer's current value and apply it to the
             * input panels
             */
            Object currentValue = properties.getAttribute(inputPanel.getName());
            if (currentValue != null) {
                inputPanel.setValue(currentValue);
            }

            propertyInputPanels.add(inputPanel);
        }

        checkPropreties();
        updateBounds();
    }

    /**
     * Initializes the dialog contents top
     */
    protected void initPanelTop(JPanel panel) {
         //Used by subclasses to add elements to the panel
    }

    /**
     * Initializes the dialog contents bottom
     */
    protected void initPanelBottom(JPanel panel) {
        /*
         * Used by subclasses to add elements to the panel
         */
    }

    /**
     * @return TODO
     */
    public UserConfigurer getConfigurer() {
        return myConfigManager;
    }

}

/**
 * Exception to be thrown if the Dialog is intentionally closed by the User
 * 
 * @author Shu
 */
class ConfigDialogClosedException extends ConfigException {

    private static final long serialVersionUID = 1L;

    public ConfigDialogClosedException() {
        super("Config dialog closed");

    }

    @Override
    public void defaultHandleBehavior() {
        /*
         * Do nothing
         */
    }

}