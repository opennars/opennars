/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PropertyInputPanel.java". Description:
"Swing Input panel to be used to enter in the value for a ConfigParam

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

package ca.nengo.ui.config;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.OpenURLAction;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Swing Input panel to be used to enter in the value for a ConfigParam
 *
 * @author Shu
 */
public abstract class PropertyInputPanel {
    private final JPanel innerPanel;
    private final JPanel outerPanel;

    private final Property propDescriptor;

    private final JLabel statusMessage;

    /**
     * @param property
     *            A description of the Configuration parameter to be configured
     */
    public PropertyInputPanel(Property property) {
        super();
        this.propDescriptor = property;
        outerPanel = new JPanel();
        outerPanel.setName(property.getName());
        outerPanel.setToolTipText(property.getTooltip());

        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);

        outerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel labelPanel=new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel label = new JLabel(property.getName());
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
            	JEditorPane editor = new JEditorPane("text/html", propDescriptor.getTooltip());
                editor.setEditable(false);
                editor.setOpaque(false);
                editor.addHyperlinkListener(new HyperlinkListener() {
                	public void hyperlinkUpdate(HyperlinkEvent hle) {
                		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                			OpenURLAction a = new OpenURLAction(hle.getDescription(),hle.getDescription());
                			a.doAction();
                		}            		
                	}
                });
                JOptionPane.showMessageDialog(help, editor, propDescriptor.getName(), JOptionPane.INFORMATION_MESSAGE, null);
            }
        });
        labelPanel.add(help);

        //labelPanel.add(Box.createHorizontalGlue());               // use this to right-justify question marks
        labelPanel.setMaximumSize(labelPanel.getMinimumSize());     // use this to keep question marks on left

        outerPanel.add(labelPanel);

        innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
        innerPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        outerPanel.add(innerPanel);

        statusMessage = new JLabel("");
        statusMessage.setForeground(NengoStyle.COLOR_HIGH_SALIENCE);

        outerPanel.add(statusMessage);

    }

    /**
     * @param comp
     *            Component to be added to the input panel
     */
    protected void add(Component comp) {
        innerPanel.add(comp);

    }

    /**
     * @return
     */
    protected JDialog getDialogParent() {
        /*
         * get the JDialog parent
         */
        Container parent = outerPanel.getParent();
        while (parent != null) {
            if (parent instanceof JDialog) {
                return (JDialog) parent;
            }
            parent = parent.getParent();
        }

        throw new RuntimeException("Input panel does not have a dialog parent");

    }

    /**
     * @param comp
     *            Component to be removed from the input panel
     */
    protected void removeFromPanel(Component comp) {
        innerPanel.remove(comp);
    }

    /**
     * @param msg
     */
    protected void setStatusMsg(String msg) {
        statusMessage.setText(msg);
    }

    /**
     * @return Descriptor of the configuration parameter
     */
    public Property getDescriptor() {
        return propDescriptor;
    }

    /**
     * @return TODO
     */
    public JPanel getJPanel() {
        return outerPanel;
    }

    /**
     * @return TODO
     */
    public String getName() {
        return outerPanel.getName();
    }

    /**
     * @return Value of the parameter
     */
    public abstract Object getValue();

    /**
     * @return TODO
     */
    public boolean isEnabled() {
        return innerPanel.isEnabled();
    }

    /**
     * @return True if configuration parameter is set
     */
    public abstract boolean isValueSet();

    /**
     * @param enabled TODO
     */
    public void setEnabled(boolean enabled) {
        innerPanel.setEnabled(enabled);
    }

    /**
     * @param value
     *            Sets the configuration parameter
     */
    public abstract void setValue(Object value);

}
