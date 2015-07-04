// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.viewer.swing.gui;

import automenta.vnc.viewer.ConnectionWorker;
import automenta.vnc.viewer.swing.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog to ask password
 */
@SuppressWarnings("serial")
public class PasswordDialog extends JDialog {

	private String password = "";

	private static final int PADDING = 4;
	private final JLabel messageLabel;
	public PasswordDialog(Frame owner, final ConnectionWorker onCancel) {
		super(owner, "VNC Authentication", true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                onCancel.cancel();
            }
        });
		JPanel pane = new JPanel(new GridLayout(0, 1, PADDING, PADDING));
		add(pane);
		pane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		messageLabel = new JLabel("Server requires VNC authentication");
		pane.add(messageLabel);

		JPanel passwordPanel = new JPanel();
		passwordPanel.add(new JLabel("Password:"));
		final JPasswordField passwordField = new JPasswordField("", 20);
		passwordPanel.add(passwordField);
		pane.add(passwordPanel);

		JPanel buttonPanel = new JPanel();
		JButton loginButton = new JButton("Login");
		buttonPanel.add(loginButton);
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				password = new String(passwordField.getPassword());
				setVisible(false);
			}
		});

		JButton closeButton = new JButton("Cancel");
		buttonPanel.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                password = null;
				setVisible(false);
				onCancel.cancel();
			}
		});

		pane.add(buttonPanel);

		getRootPane().setDefaultButton(loginButton);
        Utils.decorateDialog(this);
        Utils.centerWindow(this);
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                passwordField.requestFocusInWindow();
            }
        });
	}

	public void setServerHostName(String serverHostName) {
		messageLabel.setText("Server '" + serverHostName + "' requires VNC authentication");
		pack();
	}

	public String getPassword() {
		return password;
	}



}
