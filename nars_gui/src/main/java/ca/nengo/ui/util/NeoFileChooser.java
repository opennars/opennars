/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NeoFileChooser.java". Description: 
"File chooser used for NEO Model files.
  
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

package ca.nengo.ui.util;

import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.util.NengoConfigManager.UserProperties;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

/**
 * File chooser used for NEO Model files.
 * 
 * @author Shu Wu
 */
public class NeoFileChooser {

	private final FileFilter allFileFilter;

	/**
	 * Swing File Chooser component
	 */
	private final JFileChooser fileChooser;

	public NeoFileChooser() {
		super();
		fileChooser = new JFileChooser();

		String workingDirectory = NengoConfigManager.getUserProperty(UserProperties.ModelWorkingLocation);
		if (workingDirectory != null) {
			fileChooser.setCurrentDirectory(new File(workingDirectory));
		}

		allFileFilter = new AllNeoFiles();
		fileChooser.setFileFilter(allFileFilter);
	}

	/**
	 * @return Selected file
	 */
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	/**
	 * @param file
	 *            File to select
	 */
	public void setSelectedFile(File file) {
		fileChooser.setSelectedFile(file);
	}

	/**
	 * Shows a dialog for opening files
	 * 
	 * @return value returned by the Swing File Chooser
	 * @throws HeadlessException
	 */
	public int showOpenDialog() throws HeadlessException {
		fileChooser.setFileFilter(allFileFilter);

		int response = fileChooser.showOpenDialog(UIEnvironment.getInstance());
		saveWorkingLocation();
		return response;
	}

	/**
	 * Shows a dialog for saving ensembles
	 * 
	 * @return value returned by Swing File Chooser
	 * @throws HeadlessException
	 */
	public int showSaveDialog() throws HeadlessException {
		fileChooser.setFileFilter(allFileFilter);

		int response = fileChooser.showSaveDialog(UIEnvironment.getInstance());
		saveWorkingLocation();
		return response;
	}

	/**
	 * Saves the current working directory
	 */
	private void saveWorkingLocation() {
		String currentDirectory = fileChooser.getCurrentDirectory().toString();
		NengoConfigManager.setUserProperty(UserProperties.ModelWorkingLocation, currentDirectory);
	}
}

/**
 * File filter which allows all NEO files
 * 
 * @author Shu Wu
 */
class AllNeoFiles extends FileExtensionFilter {

	@Override
	public boolean acceptExtension(String str) {

		return (str.equals(AbstractNengo.NEONODE_FILE_EXTENSION) || str.equals("py"));
	}

	@Override
	public String getDescription() {
		return "Nengo Files";
	}

}