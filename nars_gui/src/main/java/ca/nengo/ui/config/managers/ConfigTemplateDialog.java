/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigTemplateDialog.java". Description:
"A Configuration dialog which allows the user to manage templates

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

package ca.nengo.ui.config.managers;

import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.PropertyInputPanel;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

/**
 * A Configuration dialog which allows the user to manage templates
 * 
 * @author Shu
 */
public class ConfigTemplateDialog extends ConfigDialog {

    private static final long serialVersionUID = 5650002324576913316L;
    
    private JComboBox templateList;

    /**
     * @param configManager TODO
     * @param owner TODO
     */
    public ConfigTemplateDialog(UserTemplateConfigurer configManager, Dialog owner) {
        super(configManager, owner);
        init();
    }

    /**
     * @param configManager TODO
     * @param owner TODO
     */
    public ConfigTemplateDialog(UserTemplateConfigurer configManager, Frame owner) {
        super(configManager, owner);
        init();
    }

    private void init() {

        if (checkPropreties()) {
            /*
             * Use existing properties
             */
            templateList.setSelectedItem(null);
        } else {
            /*
             * Selects the last used template
             */
            boolean foundTemplate = false;
            for (int i = 0; i < templateList.getItemCount(); i++) {
                if (templateList.getItemAt(i).toString().compareTo(
                        UserTemplateConfigurer.PREFERRED_TEMPLATE_NAME) == 0) {
                    templateList.setSelectedIndex(i);
                    foundTemplate = true;
                    break;
                }
            }

            /*
             * Failing that, selects the default template
             */
            if (!foundTemplate){
                for (int i = 0; i < templateList.getItemCount(); i++) {
                    if (templateList.getItemAt(i).toString().compareTo(
                            UserTemplateConfigurer.DEFAULT_TEMPLATE_NAME) == 0) {
                        templateList.setSelectedIndex(i);
                        break;
                    }
                }
            }

            updateFromTemplate();
        }
    }

    @Override
    protected void completeConfiguration() throws ConfigException {
        super.completeConfiguration();

        getConfigurer().savePropertiesFile(UserTemplateConfigurer.PREFERRED_TEMPLATE_NAME);

    }

    @Override
    protected void initPanelTop(JPanel panel) {
         //Add existing templates
    	
    	String desc=getConfigurer().getConfigurable().getExtendedDescription();
    	if (desc!=null) {
            JPanel labelPanel=new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
            labelPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

            final String helpText="<html><table width='250px'>"+desc+"</table></html>";
            JLabel label = new JLabel(getConfigurer().getConfigurable().getDescription());
    		label.setToolTipText(helpText);
            label.setForeground(NengoStyle.COLOR_DARK_BLUE);
            label.setFont(NengoStyle.FONT_BOLD);
            labelPanel.add(label);

            final JButton help=new JButton("<html><u>?</u></html>");
            help.setFocusable(false);
            help.setForeground(new java.awt.Color(120,120,180));
            help.setBorderPainted(false);
            help.setContentAreaFilled(false);
            help.setFocusPainted(false);
            help.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(help,helpText,getConfigurer().getConfigurable().getDescription(),JOptionPane.INFORMATION_MESSAGE,null);
                }
            });
            labelPanel.add(help);
            
    		panel.add(labelPanel);
    	}

        String[] files = getConfigurer().getPropertyFiles();

        templateList = new JComboBox(files);
        templateList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFromTemplate();
            }
        });
        templateList.setMaximumSize(new Dimension(300, 100));
        templateList.setPreferredSize(new Dimension(100, templateList.getHeight()));
    }

    /**
     * Loads the properties associated with the item selected in the file drop
     * down list
     */
    protected void updateFromTemplate() {
        try {
            if (templateList.getSelectedItem() != null) {
                getConfigurer().loadPropertiesFromFile((String) templateList.getSelectedItem());
                Iterator<PropertyInputPanel> it = propertyInputPanels.iterator();
                while (it.hasNext()) {
                    PropertyInputPanel panel = it.next();

                    Object currentValue = getConfigurer().getProperty(panel.getName());
                    if (currentValue != null && panel.isEnabled()) {
                        panel.setValue(currentValue);
                    }

                }
            }
        } catch (ClassCastException e) {
            Util.debugMsg("Saved template has incompatible data, it will be ignored");
        }
    }

    @Override
    public UserTemplateConfigurer getConfigurer() {
        return (UserTemplateConfigurer) super.getConfigurer();
    }

}

/**
 * A JPanel which has some commonly used settings
 * 
 * @author Shu
 */
class VerticalLayoutPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public VerticalLayoutPanel() {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(TOP_ALIGNMENT);
        setAlignmentX(LEFT_ALIGNMENT);
    }

}