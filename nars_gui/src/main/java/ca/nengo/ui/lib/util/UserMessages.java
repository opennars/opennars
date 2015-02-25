package ca.nengo.ui.lib.util;

import ca.nengo.ui.lib.UIException;

import javax.swing.*;
import java.awt.*;

/**
 * Displays messages to the user through a popup dialog.
 * 
 * @author Shu Wu
 */
public class UserMessages {
	public static void showError(String msg) {
		showError(msg, UIEnvironment.getInstance());
	}

	public static String askDialog(String dialogMessage) throws DialogException {
		String userName = JOptionPane.showInputDialog(UIEnvironment.getInstance(), dialogMessage);

		if (userName == null || userName.compareTo("") == 0) {
			throw new DialogException();
		}
		return userName;
	}

	public static class DialogException extends UIException {

		private static final long serialVersionUID = 1L;

		public DialogException() {
			super("Dialog cancelled");
		}

		public DialogException(String arg0) {
			super(arg0);
		}
	}

	public static void showError(String msg, Component parent) {
		JOptionPane.showMessageDialog(parent, "<HTML>" + msg + "</HTML>", "Error",
				JOptionPane.ERROR_MESSAGE);
		(new Exception(msg)).printStackTrace();

	}

	public static void showTextDialog(String title, String msg) {
		showTextDialog(title, msg, JOptionPane.PLAIN_MESSAGE);
	}

	public static void showTextDialog(String title, String msg, int messageType) {
		JTextArea editor = new JTextArea(30, 50);
		editor.setText(msg);
		editor.setEditable(false);
		editor.setCaretPosition(0);
		JOptionPane.showMessageDialog(UIEnvironment.getInstance(), new JScrollPane(editor), title,
				messageType);
	}

	public static void showWarning(String msg) {
		showWarning(msg, UIEnvironment.getInstance());
	}

	public static void showDialog(String title, String msg) {
		showDialog(title,msg,UIEnvironment.getInstance());
	}

	public static void showDialog(String title, String msg, Component parent) {
		JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showWarning(String msg, Component parent) {
		JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.WARNING_MESSAGE);
	}
}
