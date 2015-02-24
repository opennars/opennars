/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UnitsHandler.java". Description:
"ConfigurationHandler for Units values"

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
package ca.nengo.config.handlers;

import ca.nengo.config.ui.ConfigurationChangeListener;
import ca.nengo.model.Units;

import javax.swing.*;
import java.awt.*;

/**
 * ConfigurationHandler for Units values.
 *
 * @author Bryan Tripp
 */
public class UnitsHandler extends BaseHandler {

	private static final Units[] myList = new Units[]{
			Units.UNK,
			Units.ACU,
			Units.AVU,
			Units.M,
			Units.M_PER_S,
			Units.mV,
			Units.N,
			Units.Nm,
			Units.RAD,
			Units.RAD_PER_S,
			Units.S,
			Units.SPIKES,
			Units.SPIKES_PER_S,
			Units.uA,
			Units.uAcm2
		};


	/**
	 * ConfigurationHandler for Units values.
	 */
	public UnitsHandler() {
		super(Units.class);
	}

	@Override
	public Component getEditor(Object o, ConfigurationChangeListener listener, JComponent parent) {
		Units unit = (Units) o;

		final JComboBox result = new JComboBox(myList);
		result.setSelectedItem(unit);

		listener.setProxy(new ConfigurationChangeListener.EditorProxy() {
            public Object getValue() {
				return result.getSelectedItem();
			}
		});
		result.addActionListener(listener);

		return result;
	}

	@Override
	public Object fromString(String s) {
		Units result = null;

		for (int i = 0; i < myList.length && result == null; i++) {
			if (s.equals(myList[i].toString())) {
				result = myList[i];
			}
		}

		if (result == null) {
			throw new RuntimeException("Units " + s + " not recognized");
		}

		return result;
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
    public Object getDefaultValue(Class<?> c) {
		return Units.UNK;
	}

}
