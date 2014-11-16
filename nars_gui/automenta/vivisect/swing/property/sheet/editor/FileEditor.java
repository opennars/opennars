package automenta.vivisect.swing.property.sheet.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import automenta.vivisect.swing.property.beans.editor.AbstractPropertyEditor;
import automenta.vivisect.swing.property.sheet.I18N;
import automenta.vivisect.swing.property.swing.UserPreferences;


/**
 * File editor.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class FileEditor extends AbstractPropertyEditor {

	protected class FileEditorComponent extends JPanel {

		private static final long serialVersionUID = 411604969565728959L;

		private JTextField textfield = null;
		private JButton button = null;
		private JButton cancelButton = null;

		public FileEditorComponent() {

			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			textfield = new JTextField();
			textfield.setEditable(false);
			textfield.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			textfield.setTransferHandler(new FileTransferHandler());
			textfield.setAlignmentX(JTextField.LEFT_ALIGNMENT);
			textfield.setBackground(UIManager.getColor("Table.selectionBackground"));
			textfield.setForeground(UIManager.getColor("Table.selectionForeground"));

			Dimension size = new Dimension(24, 15);

			Image file = null;
			Image reset = null;
			try {
				file = ImageIO.read(getClass().getClassLoader().getResourceAsStream("com/l2fprod/common/propertysheet/icons/file.png"));
				reset = ImageIO.read(getClass().getClassLoader().getResourceAsStream("com/l2fprod/common/propertysheet/icons/reset.png"));
			} catch (IOException e) {
				throw new RuntimeException("Cannot load resource", e);
			}

			button = new JButton();
			button.setPreferredSize(size);
			button.setSize(size);
			button.setMaximumSize(size);
			button.setMinimumSize(size);
			button.setAction(new AbstractAction("", new ImageIcon(file)) {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					selectFile();
				}
			});

			cancelButton = new JButton();
			cancelButton.setPreferredSize(size);
			cancelButton.setSize(size);
			cancelButton.setMaximumSize(size);
			cancelButton.setMinimumSize(size);
			cancelButton.setAction(new AbstractAction("", new ImageIcon(reset)) {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					selectNull();
				}
			});

			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
			container.add(button);
			container.add(cancelButton);
			container.setPreferredSize(new Dimension(size.width * 2, 25));
			container.setAlignmentX(JPanel.RIGHT_ALIGNMENT);

			add(textfield, BorderLayout.WEST);
			add(container, BorderLayout.CENTER);
		}

		public void setText(String text) {
			textfield.setText(text);
		}

		public String getText() {
			return textfield.getText().trim();
		}

		public void setFile(File file) {
			setText(file.getAbsolutePath());
		}

		public File getFile() {
			String file = getText();
			if ("".equals(file)) {
				return null;
			} else {
				return new File(file);
			}
		}
	}

	private class FileTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 7574211768508397674L;

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			for (int i = 0, c = transferFlavors.length; i < c; i++) {
				if (transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {

				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) t.getTransferData(DataFlavor.javaFileListFlavor);

				if (list.size() > 0) {

					File oldFile = (File) getValue();
					File newFile = (File) list.get(0);

					fileEditor.setFile(newFile);

					firePropertyChange(oldFile, newFile);
				}
			} catch (Exception e) {
				throw new RuntimeException("Cannot import data", e);
			}
			return true;
		}
	}

	protected FileEditorComponent fileEditor = null;

	public FileEditor() {
		editor = fileEditor = new FileEditorComponent();
	}

	@Override
	public Object getValue() {
		return fileEditor.getFile();
	}

	protected void selectNull() {
		Object oldFile = getValue();
		fileEditor.setText("");
		firePropertyChange(oldFile, null);
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof File) {
			fileEditor.setFile((File) value);
		} else {
			fileEditor.setText("");
		}
	}

	protected void selectFile() {

		JFileChooser chooser = UserPreferences.getDefaultFileChooser();
		chooser.setDialogTitle(I18N.CHOOSE_FILE);
		chooser.setApproveButtonText(I18N.SELECT);

		if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {

			File oldFile = (File) getValue();
			File newFile = chooser.getSelectedFile();

			fileEditor.setFile(newFile);

			firePropertyChange(oldFile, newFile);
		}
	}
}