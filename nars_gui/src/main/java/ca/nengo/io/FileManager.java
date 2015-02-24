/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FileManager.java". Description:
"Handles saving and loading of Node

  TODO: a better job (this is a quick one)
  TODO: is there any metadata to store?
  TODO: test

  @author Bryan Tripp"

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
 * Created on 7-Jun-2006
 */
package ca.nengo.io;

import ca.nengo.model.Node;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.util.TimeSeries;

import java.io.*;

/**
 * Handles saving and loading of Node
 *
 * TODO: a better job (this is a quick one)
 * TODO: is there any metadata to store?
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class FileManager {

	/**
	 * Extension for serialized NEF networks
	 */
	public static final String ENSEMBLE_EXTENSION = "nef";

	/**
	 * @param node Node to serialize
	 * @param destination File to save serialized Node in
	 * @throws IOException if there's a problem writing to disk
	 */
	public void save(Node node, File destination) throws IOException {
		saveObject(node, destination);
	}

	public void generate(Node node, String destination) throws IOException {
		NetworkImpl network = (NetworkImpl) node;
		network.dumpToScript(destination);
	}
	
	/**
	 * @param timeSeries TimeSeries to serialize
	 * @param destination File to save serialized TimeSeries in
	 * @throws IOException if there's a problem writing to disk
	 */
	public void save(TimeSeries timeSeries, File destination) throws IOException {
		saveObject(timeSeries, destination);
	}

	private static void saveObject(Object object, File destination) throws IOException {
		FileOutputStream fos = new FileOutputStream(destination);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(object);
		oos.flush();
		oos.close();
		fos.close();
	}

	/**
	 * @param source Serialized file to load
	 * @return Object represented by the serialized file
	 * @throws IOException if there's a problem writing to disk
	 * @throws ClassNotFoundException if the serialized file contains classes
	 *   not known in this context
	 */
	public Object load(File source) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(source);

		ObjectInputStream ois = new ObjectInputStream(fis);
		Object return_obj = ois.readObject();

		ois.close();
		fis.close();

		return return_obj;
	}

}
