/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "MainHandler.java". Description:
"A composite ConfigurationHandler which delegates to other underlying ConfigurationHandlers
  that can handle specific classes"

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
package ca.nengo.config;

import ca.nengo.config.handlers.*;
import ca.nengo.config.ui.ConfigurationChangeListener;
import ca.nengo.config.ui.ConfigurationTreeModel.NullValue;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.Units;
import ca.nengo.neural.neuron.impl.IzhikevichSpikeGenerator;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A composite ConfigurationHandler which delegates to other underlying ConfigurationHandlers
 * that can handle specific classes.
 *
 * @author Bryan Tripp
 */
public final class MainHandler implements ConfigurationHandler {

	/**
	 * Java package with handlers
	 */
	public static final String HANDLERS_FILE_PROPERTY = "ca.nengo.config.handlers";

	private static final Logger ourLogger = LogManager.getLogger(ConfigurationHandler.class);
	private static MainHandler ourInstance;

	private final List<ConfigurationHandler> myHandlers;

	/**
	 * @return Singleton instance
	 */
	public static synchronized MainHandler getInstance() {
		if (ourInstance == null) {
			ourInstance = new MainHandler();
		}
		return ourInstance;
	}

	private MainHandler() {
		myHandlers = new ArrayList<ConfigurationHandler>(20);

		String fileName = System.getProperty(HANDLERS_FILE_PROPERTY, "handlers.txt");
		File file = new File(fileName);
		if (file.exists() && file.canRead()) {
			try {
				loadHandlers(file);
			} catch (IOException e) {
				String message = "Can't load handlers";
				ourLogger.error(message, e);
				throw new RuntimeException(message, e);
			}
		} else {
			ourLogger.warn("Can't open configuration handlers file " + fileName);
		}

		addHandler(new FloatHandler());
		addHandler(new StringHandler());
		addHandler(new IntegerHandler());
		addHandler(new LongHandler());
		addHandler(new BooleanHandler());
		addHandler(new VectorHandler());
		addHandler(new MatrixHandler());
		addHandler(new EnumHandler(SimulationMode.class, SimulationMode.DEFAULT));
		addHandler(new EnumHandler(Units.class, Units.UNK));
		addHandler(new EnumHandler(IzhikevichSpikeGenerator.Preset.class, IzhikevichSpikeGenerator.Preset.DEFAULT));
	}

	private void loadHandlers(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line = null;
		while ((line = reader.readLine()) != null) {
			String className = line.trim();
			try {
				ConfigurationHandler h = (ConfigurationHandler) Class.forName(className).newInstance();
				addHandler(h);
			} catch (Exception e) {
				String message = "Can't create handler";
				ourLogger.error(message, e);
				throw new RuntimeException(message, e);
			}
		}
	}

	/**
	 * @param handler New handler to which the MainHandler can delegate
	 */
	public void addHandler(ConfigurationHandler handler) {
		myHandlers.add(handler);
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#canHandle(java.lang.Class)
	 */
	public boolean canHandle(Class<?> c) {
		boolean result = false;
		for (int i = myHandlers.size()-1; i >= 0 && !result; i--) {
			if (myHandlers.get(i).canHandle(c)) {
                result = true;
            }
		}
		return result;
	}

	/**
	 * @param c The class of the object represented by s
	 * @param s A String representation of an object
	 * @return x.fromString(s), where x is a handler appropriate for the class c
	 */
	public Object fromString(Class<?> c, String s) {
		Object result = null;

		ConfigurationHandler handler = getHandler(myHandlers, c);
		if (handler != null) {
            result = handler.fromString(s);
        }

		return result;
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#fromString(java.lang.String)
	 */
	public Object fromString(String s) {
		return null;
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getEditor(Object, ConfigurationChangeListener, JComponent)
	 */
	public Component getEditor(Object o, ConfigurationChangeListener listener, JComponent parent) {
		Component result = null;

		Class<?> c = o.getClass();
		ConfigurationHandler handler = getHandler(myHandlers, c);
		if (handler != null) {
            result = handler.getEditor(o, listener, parent);
        }

		return result;
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getRenderer(java.lang.Object)
	 */
	public Component getRenderer(Object o) {
		Component result = null;

		if (o instanceof NullValue) {
            result = new JLabel("EMPTY");
        }

		Class<?> c = o.getClass();
		ConfigurationHandler handler = getHandler(myHandlers, c);
		if (handler != null) {
            result = handler.getRenderer(o);
        }

		return result;
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#toString(java.lang.Object)
	 */
	public String toString(Object o) {
		String result = null;

		Class<?> c = o.getClass();
		ConfigurationHandler handler = getHandler(myHandlers, c);
		if (handler != null) {
            result = handler.toString(o);
        }

		return result;
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
	public Object getDefaultValue(Class<?> c) {
		Object result = null;

		ConfigurationHandler handler = getHandler(myHandlers, c);
		if (handler != null) {
            result = handler.getDefaultValue(c);
        }

		return result;
	}

	//returns last handler that can handle given class
	private static ConfigurationHandler getHandler(List<ConfigurationHandler> handlers, Class<?> c) {
		ConfigurationHandler result = null;
		for (int i = handlers.size()-1; i >= 0 && result == null; i--) {
			if (handlers.get(i).canHandle(c)) {
				result = handlers.get(i);
			}
		}
		return result;
	}

}
