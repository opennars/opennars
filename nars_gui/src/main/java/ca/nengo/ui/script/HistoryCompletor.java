/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "HistoryCompletor.java". Description:
"A list of commands that have been entered previously"

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
 * Created on 5-Nov-07
 */
package ca.nengo.ui.script;

import ca.nengo.ui.lib.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list of commands that have been entered previously.
 *
 * @author Bryan Tripp
 */
public class HistoryCompletor extends CommandCompletor {

	public static final String HISTORY_LOCATION_PROPERTY = "HistoryCompletor.File";

	private final File myFile;
	private final int NUM_COMMANDS_SAVED = 1000;

	public HistoryCompletor() {
		myFile = new File(System.getProperty(HISTORY_LOCATION_PROPERTY, "commandhistory.dat"));
		if(readFile()==null) {
            return;
        }
		getOptions().addAll(readFile());  //read in past commands from file and add them to the options
		resetIndex();
	}

	/**
	 * Add command string to CommandCompletor and update commandhistory file
	 */
	public void add(String command){
		getOptions().add(command);
		resetIndex();
		List<String> commands = getOptions();
		overwriteFile(commands);
	}

	/**
	 * Read commandhistory file.
	 * @return A list of strings from the commandhistory file
	 */
	@SuppressWarnings("unchecked")
    private List<String> readFile (){
		List<String> commands=null;

		if (!myFile.exists() || !myFile.canRead()) {
            return null;
        }

		try {
			ObjectInput input = new ObjectInputStream (new BufferedInputStream(new FileInputStream(myFile)));
			commands = (List<String>)input.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return commands;
	}

	/**
	 * Overwrite commandhistory file with specified list of command strings.
	 * @param commands: a list of commands to write to file
	 */
	private void overwriteFile (List<String> commands){
		List <String> commandsCopy=new ArrayList<String>(commands);
		Collections.copy(commandsCopy, commands);

		while (commandsCopy.size()>NUM_COMMANDS_SAVED) {
            commandsCopy.remove(0);
        }

		try {
			if (!myFile.exists()) {
                myFile.createNewFile();
            }
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!myFile.exists() || !myFile.canWrite()){
			Util.Assert(false, "Trouble writing console command history to file");
			return;
		}

		try {
			ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(myFile)));
			output.writeObject(commandsCopy);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}