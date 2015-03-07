/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "IconRegistry.java". Description:
"A registry of graphical Icons that can be used for displaying Property values.

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
 * Created on 12-Dec-07
 */
package ca.nengo.config;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.math.Function;
import ca.nengo.math.PDF;
import ca.nengo.model.*;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import ca.nengo.util.SpikePattern;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * A registry of graphical Icons that can be used for displaying Property values.
 *
 * @author Bryan Tripp
 */
public class IconRegistry {

	private static final Logger ourLogger = LogManager.getLogger(IconRegistry.class);
	private static IconRegistry ourInstance;

	private final List<Class<?>> myIconClasses;
	private final List<Icon> myIcons;

	/**
	 * @return Singleton instance
	 */
	public static IconRegistry getInstance() {
		if (ourInstance == null) {
			ourInstance = new IconRegistry();

			//TODO: move these somewhere configurable
			ourInstance.setIcon(Property.class, new Icon(){
                public void paintIcon(Component c, Graphics g, int x, int y) {
					g.drawPolygon(new int[]{8, 13, 8, 3}, new int[]{3, 8, 13, 8}, 4);
				}
                public int getIconWidth() {
					return 16;
				}
                public int getIconHeight() {
					return 16;
				}
			});
			ourInstance.setIcon(Boolean.class, "/ca/nengo/config/ui/boolean_icon.GIF");
			ourInstance.setIcon(Boolean.TYPE, "/ca/nengo/config/ui/boolean_icon.GIF");
			ourInstance.setIcon(Integer.class, "/ca/nengo/config/ui/integer_icon.GIF");
			ourInstance.setIcon(Integer.TYPE, "/ca/nengo/config/ui/integer_icon.GIF");
			ourInstance.setIcon(Float.class, "/ca/nengo/config/ui/float_icon.GIF");
			ourInstance.setIcon(Float.TYPE, "/ca/nengo/config/ui/float_icon.GIF");
			ourInstance.setIcon(float[].class, "/ca/nengo/config/ui/float_array_icon.GIF");
			ourInstance.setIcon(float[][].class, "/ca/nengo/config/ui/matrix_icon.GIF");
			ourInstance.setIcon(String.class, "/ca/nengo/config/ui/string_icon.JPG");

			ourInstance.setIcon(DynamicalSystem.class, "/ca/nengo/config/ui/dynamicalsystem02.jpg");
			ourInstance.setIcon(Integrator.class, "/ca/nengo/config/ui/integrator.jpg");
			ourInstance.setIcon(PDF.class, "/ca/nengo/config/ui/pdf.jpg");
			ourInstance.setIcon(Function.class, "/ca/nengo/config/ui/function.jpg");
			ourInstance.setIcon(Noise.class, "/ca/nengo/config/ui/noise01.jpg");
			ourInstance.setIcon(NSource.class, "/ca/nengo/config/ui/origin4.jpg");
			ourInstance.setIcon(SpikeGenerator.class, "/ca/nengo/config/ui/spikegenerator.jpg");
			ourInstance.setIcon(SimulationMode.class, "/ca/nengo/config/ui/simulationmode.jpg");
			ourInstance.setIcon(Units.class, "/ca/nengo/config/ui/units.jpg");
			ourInstance.setIcon(Enum.class, "/ca/nengo/config/ui/enum.gif");
			ourInstance.setIcon(SpikePattern.class, "/ca/nengo/config/ui/spikepattern.jpg");
			ourInstance.setIcon(NTarget.class, "/ca/nengo/config/ui/termination4.jpg");

			ourInstance.setIcon(Neuron.class, "/ca/nengo/config/ui/neuron.jpg");
			ourInstance.setIcon(Network.class, "/ca/nengo/config/ui/network.jpg");
			ourInstance.setIcon(Group.class, "/ca/nengo/config/ui/ensemble2.jpg");
			ourInstance.setIcon(Node.class, "/ca/nengo/config/ui/node.jpg");
			ourInstance.setIcon(SynapticIntegrator.class, "/ca/nengo/config/ui/synintegrator3.jpg");
		}

		return ourInstance;
	}

	private IconRegistry() {
		myIconClasses = new ArrayList<Class<?>>(10);
		myIcons = new ArrayList<Icon>(10);
	}

	/**
	 * @param o An object
	 * @return An icon to use in displaying the given object
	 */
	public Icon getIcon(Object o) {
		return (o == null) ? null : getIcon(o.getClass());
	}

	/**
	 * @param c Class of object
	 * @return An icon to use in displaying objects of the given class
	 */
	public Icon getIcon(Class<?> c) {
		Icon result = null;
		for (int i = 0; result == null && i < myIconClasses.size(); i++) {
			if (myIconClasses.get(i).isAssignableFrom(c)) {
				result = myIcons.get(i);
			}
		}

		if (result == null) {
			result = new DefaultIcon();
		}

		return result;
	}

	/**
	 * @param c A class
	 * @param icon An Icon to use for objects of the given class
	 */
	public void setIcon(Class<?> c, Icon icon) {
		myIconClasses.add(c);
		myIcons.add(icon);
	}

	/**
	 * @param c A class
	 * @param path Path to an image file from which to make an Icon for objects of the
	 * 		given class
	 */
	public void setIcon(Class<?> c, String path) {
		myIconClasses.add(c);
		myIcons.add(createImageIcon(path, ""));
	}

	private ImageIcon createImageIcon(String path, String description) {
		ImageIcon result = null;
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        result = new ImageIcon(imgURL, description);
	    } else {
	        ourLogger.warn("Can't load icon from " + path);
	    }

	    return result;
	}

	private static class DefaultIcon implements Icon {
        public int getIconHeight() {
			return 16;
		}

        public int getIconWidth() {
			return 16;
		}

        public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawOval(1, 1, 14, 14);
		}

	}

}
