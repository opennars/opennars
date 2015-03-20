//// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
//// All rights reserved.
////
////-------------------------------------------------------------------------
//// This file is part of the TightVNC software.  Please visit our Web site:
////
////                       http://www.tightvnc.com/
////
//// This program is free software; you can redistribute it and/or modify
//// it under the terms of the GNU General Public License as published by
//// the Free Software Foundation; either version 2 of the License, or
//// (at your option) any later version.
////
//// This program is distributed in the hope that it will be useful,
//// but WITHOUT ANY WARRANTY; without even the implied warranty of
//// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//// GNU General Public License for more details.
////
//// You should have received a copy of the GNU General Public License along
//// with this program; if not, write to the Free Software Foundation, Inc.,
//// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
////-------------------------------------------------------------------------
////
//
//package vnc.viewer.swing.ssh;
//
//import vnc.utils.Strings;
//import vnc.viewer.swing.Utils;
//import com.jcraft.jsch.UIKeyboardInteractive;
//import com.jcraft.jsch.UserInfo;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.lang.reflect.InvocationTargetException;
//import java.util.logging.Logger;
//
///**
//* @author dime at tightvnc.com
//*/
//class SwingSshUserInfo implements UserInfo, UIKeyboardInteractive {
//	private String password;
//    private String passphrase;
//	private final JFrame parentFrame;
//
//    SwingSshUserInfo(JFrame parentFrame) {
//		this.parentFrame = parentFrame;
//    }
//
//	@Override
//	public String getPassphrase() {
//		return passphrase;
//	}
//
//	@Override
//	public String getPassword() {
//		return password;
//	}
//
//	@Override
//	public boolean promptPassword(final String message) {
//        final int[] result = new int[1];
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    final JTextField passwordField = new JPasswordField(20);
//                    Object[] ob = {message, passwordField};
//                    JOptionPane pane = new JOptionPane(ob, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
//                    final JDialog dialog = pane.createDialog(parentFrame, "SSH Authentication");
//                    Utils.decorateDialog(dialog);
//                    dialog.addWindowFocusListener(new WindowAdapter() {
//                        @Override
//                        public void windowGainedFocus(WindowEvent e) {
//                            passwordField.requestFocusInWindow();
//                        }
//                    });
//                    dialog.setVisible(true);
//                    result[0] = pane.getValue() != null ? (Integer)pane.getValue() : JOptionPane.CLOSED_OPTION;
//                    if (JOptionPane.OK_OPTION == result[0]) {
//                        password = passwordField.getText();
//                    }
//                    dialog.dispose();
//                }
//            });
//        } catch (InterruptedException e) {
//            getLogger().severe(e.getMessage());
//        } catch (InvocationTargetException e) {
//            getLogger().severe(e.getMessage());
//        }
//
//        return JOptionPane.OK_OPTION == result[0];
//	}
//
//	@Override
//	public boolean promptPassphrase(final String message) {
//        final int[] result = new int[1];
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    JTextField passphraseField = new JPasswordField(20);
//                    Object[] ob = {message, passphraseField};
//                    JOptionPane pane =
//                            new JOptionPane(ob, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
//                    final JDialog dialog = pane.createDialog(parentFrame, "SSH Authentication");
//                    Utils.decorateDialog(dialog);
//                    dialog.setVisible(true);
//                    result[0] = pane.getValue() != null ? (Integer)pane.getValue() : JOptionPane.CLOSED_OPTION;
//                    if (JOptionPane.OK_OPTION == result[0]) {
//                        passphrase = passphraseField.getText();
//                    }
//                    dialog.dispose();
//                }
//            });
//        }  catch (InterruptedException e) {
//            getLogger().severe(e.getMessage());
//        } catch (InvocationTargetException e) {
//            getLogger().severe(e.getMessage());
//        }
//        return JOptionPane.OK_OPTION == result[0];
//	}
//
//	@Override
//	public boolean promptYesNo(final String message) {
//		final int[] result = new int[1];
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
//                    final JDialog dialog = pane.createDialog(parentFrame, "SSH: Warning");
//                    Utils.decorateDialog(dialog);
//                    dialog.setVisible(true);
//                    result[0] = pane.getValue() != null ? (Integer)pane.getValue() : JOptionPane.CLOSED_OPTION;
//                    dialog.dispose();
//                }
//            });
//        }  catch (InterruptedException e) {
//            getLogger().severe(e.getMessage());
//        } catch (InvocationTargetException e) {
//            getLogger().severe(e.getMessage());
//        }
//        return JOptionPane.YES_OPTION == result[0];
//	}
//
//	@Override
//	public void showMessage(final String message) {
//		try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
//                    final JDialog dialog = pane.createDialog(parentFrame, "SSH");
//                    Utils.decorateDialog(dialog);
//                    dialog.setVisible(true);
//                    dialog.dispose();
//                }
//            });
//        }  catch (InterruptedException e) {
//            getLogger().severe(e.getMessage());
//        } catch (InvocationTargetException e) {
//            getLogger().severe(e.getMessage());
//        }
//	}
//
//	@Override
//	public String[] promptKeyboardInteractive(final String destination,
//			final String name,
//			final String instruction,
//			final String[] prompt,
//			final boolean[] echo) {
//        class WrapRes {
//            String[] stringsRes;
//        }
//        final WrapRes wrapRes = new WrapRes();
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    Container panel = new JPanel();
//                    panel.setLayout(new GridBagLayout());
//
//                    final GridBagConstraints gbc =
//                            new GridBagConstraints(0, 0, 1, 1, 1, 1,
//                                    GridBagConstraints.NORTHWEST,
//                                    GridBagConstraints.NONE,
//                                    new Insets(0, 0, 0, 0), 0, 0);
//                    gbc.weightx = 1.0;
//                    gbc.gridwidth = GridBagConstraints.REMAINDER;
//                    gbc.gridx = 0;
//                    panel.add(new JLabel(instruction), gbc);
//                    gbc.gridy++;
//                    gbc.gridwidth = GridBagConstraints.RELATIVE;
//                    JTextField[] texts = new JTextField[prompt.length];
//                    for (int i = 0; i < prompt.length; i++) {
//                        gbc.fill = GridBagConstraints.NONE;
//                        gbc.gridx = 0;
//                        gbc.weightx = 1;
//                        panel.add(new JLabel(prompt[i]), gbc);
//                        gbc.gridx = 1;
//                        gbc.fill = GridBagConstraints.HORIZONTAL;
//                        gbc.weighty = 1;
//                        if (echo[i]) {
//                            texts[i] = new JTextField(20);
//                        } else {
//                            texts[i] = new JPasswordField(20);
//                        }
//                        panel.add(texts[i], gbc);
//                        gbc.gridy++;
//                    }
//
//                    final String title = "SSH authentication for " + destination + (Strings.isTrimmedEmpty(name) ? "" : (": " + name));
//
//                    final JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
//                    final JDialog dialog = pane.createDialog(parentFrame, title);
//                    Utils.decorateDialog(dialog);
//                    dialog.setVisible(true);
//                    int result = pane.getValue() != null ? (Integer)pane.getValue() : JOptionPane.CLOSED_OPTION;
//                    wrapRes.stringsRes = null;
//                    if (JOptionPane.OK_OPTION == result) {
//                        wrapRes.stringsRes = new String[prompt.length];
//                        for (int i = 0; i < prompt.length; i++) {
//                            wrapRes.stringsRes[i] = texts[i].getText();
//                        }
//                    }
//                    dialog.dispose();
//                }
//            });
//        } catch (InterruptedException e) {
//            getLogger().severe(e.getMessage());
//        } catch (InvocationTargetException e) {
//            getLogger().severe(e.getMessage());
//        }
//		return wrapRes.stringsRes;
//	}
//
//    private Logger getLogger() {
//        return Logger.getLogger(getClass().getName());
//    }
//}
