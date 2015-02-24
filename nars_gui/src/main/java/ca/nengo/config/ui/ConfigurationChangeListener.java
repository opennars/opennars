/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigurationChangeListener.java". Description:
"A listener for changes to Property values"

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

/*
 * Created on 17-Dec-07
 */
package ca.nengo.config.ui;

import javax.swing.*;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A listener for changes to Property values.
 *
 * TODO: is there a better option than EditorProxy?
 * TODO: can we avoid references to this class from ca.nengo.config?
 *
 * @author Bryan Tripp
 */
public class ConfigurationChangeListener implements ActionListener {

	private final JTree myTree;
	private final ConfigurationTreeModel myModel;
	private final TreeCellEditor myEditor;
	private final TreePath myPath;
	private EditorProxy myEditorProxy;
	private boolean isChangeCommited = false;
	private boolean isChangeCancelled = false;

	/**
	 * @param tree Parent tree object
	 * @param path Current path
	 */
	public ConfigurationChangeListener(JTree tree, TreePath path) {
		myTree = tree;
		myModel = (ConfigurationTreeModel) tree.getModel();
		myEditor = tree.getCellEditor();
		myPath = path;
	}

	/**
	 * @return Already committed?
	 */
	public boolean isChangeCommited() {
		return isChangeCommited;
	}

	/**
	 * @return Has change been cancelled?
	 */
	public boolean isChangeCancelled() {
		return isChangeCancelled;
	}

	/**
	 * Called by a ConfigurationHandler's editor.
	 *
	 * @param proxy
	 *            Provides access to an updated property value after it is
	 *            changed by the user
	 */
	public void setProxy(EditorProxy proxy) {
		myEditorProxy = proxy;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		commitChanges();
	}

	/**
	 * Event for when changes are committed
	 */
	public void commitChanges() {
		if (isChangeCommited) {
            return;
        }

		try {
			myModel.setValue(myPath, myEditorProxy.getValue());
		} catch (Exception ex) {
			String message = "The new value is invalid. The old value will be retained.";
			if (ex.getMessage() != null) {
                message = ex.getMessage();
            }
			ConfigExceptionHandler.handle(ex, message, myTree);
		}
		myEditor.stopCellEditing();
		isChangeCommited =true;
	}

	/**
	 *
	 */
	public void cancelChanges() {
		if (isChangeCancelled) {
            return;
        }

		myEditor.cancelCellEditing();
		isChangeCancelled = true;
	}

	/**
	 * An editor component (from ConfigurationHandler.getEditor(...)) must
	 * implement EditorProxy in order to allow retrieval of a new value when
	 * editing is complete. For example if the component is a JTextField, the
	 * implementation could be <code>getValue() { jtf.getText(); }</code>.
	 *
	 * @author Bryan Tripp
	 */

	public interface EditorProxy {
		/**
		 * @return Current value of edited object
		 */
		public Object getValue();

	}

}