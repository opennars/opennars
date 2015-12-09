/*
 * MessageDialog.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Pop-up message for the user to accept
 */
public class MessageDialog extends JDialog implements ActionListener, WindowListener {

    protected JButton button;
    protected JTextArea text;

    /**
     * Constructor
     * @param parent The parent Frame
     * @param message The text to be displayed
     */
    public MessageDialog(String message) {
        super((Dialog)null, "Message", false);
        setLayout(new BorderLayout(5, 5));
        
        text = new JTextArea(message);

        add("Center", text);
        button = new JButton(" OK ");
        button.addActionListener(this);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        p.add(button);
        add("South", p);
        setModal(true);
        setBounds(200, 250, 400, 180);
        addWindowListener(this);
        setVisible(true);
    }

    /**
     * Handling button click
     * @param e The ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            close();
        }
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        close();
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }
}
