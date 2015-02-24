/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "CommandCompletor.java". Description: 
"Base class for command completors, which provide suggestions for filling in the 
  remainder of partially-specified scripting commands"

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
 * Created on 11-Nov-07
 */
package ca.nengo.ui.script;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for command completors, which provide suggestions for filling in the 
 * remainder of partially-specified scripting commands. 
 * 
 * @author Bryan Tripp
 */
public abstract class CommandCompletor {

	private final List<String> myOptions;
	private int myIndex;

	public CommandCompletor() {
		myOptions = new ArrayList<String>(100);
		myIndex = myOptions.size();
	}
	
	/**
	 * @return The list of completion options currently under consideration 
	 */
	protected List<String> getOptions() {
		return myOptions;
	}

	/**
	 * Resets the index to the list of completion options to its default location.
	 */
	public void resetIndex() {
		myIndex = myOptions.size();
	}
	
	protected int getIndex() {
		return myIndex;
	}
	
	/**
	 * @param partial Partial command string 
	 * @return Next most recent command (from current index in options list) that begins with 
	 * 		given partial command. Returns the arg if end of list is reached.  
	 */
	public String previous(String partial) {
		String result = null;
		for (int i = myIndex-1; i >= 0 && result == null; i--) {
			if (myOptions.get(i).startsWith(partial)) {
				result = myOptions.get(i);
				myIndex = i;
			}
		}
		if (result == null) {
			result = partial;
			myIndex = -1;
		}
		return result;
	}
	
	/**
	 * @param partial Partial command string 
	 * @return Next command (from current index in options list) that begins with 
	 * 		given partial command. Returns the arg if end of list is reached.  
	 */
	public String next(String partial) {
		String result = null;
		for (int i = myIndex+1; i < myOptions.size() && result == null; i++) {
			if (myOptions.get(i).startsWith(partial)) {
				result = myOptions.get(i);
				myIndex = i;
			}
		}
		if (result == null) {
			result = partial;
			myIndex = myOptions.size();
		}
		return result;		
	}
	
}
