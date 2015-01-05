/**
 * L2FProd Common v9.2 License.
 *
 * Copyright 2005 - 2009 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package automenta.vivisect.swing.property.sheet.editor;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import automenta.vivisect.swing.property.propertysheet.Property;


/**
 * Percentage editor.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class PercentageEditor extends SpinnerEditor {

	/**
	 * Percentage editor. Argument has to be an object so javax.bean API handles
	 * it correctly.
	 * 
	 * @param property the property object (instance of {@link Property})
	 */
	public PercentageEditor(Object property) {
		super();

		if (!(property instanceof Property)) {
			throw new IllegalArgumentException(String.format("Property has to be %s instance. Instead found %s", Property.class, property.getClass()));
		}

		Property prop = (Property) property;
		Class<?> type = prop.getType();

		int pstart = 0;
		int pmin = 0;
		int pmax = 100;
		int pstep = 1;

		Number start = null;
		Comparable<?> min = null;
		Comparable<?> max = null;
		Number step = null;

		if (type == Byte.class || type == byte.class) {
			start = Byte.valueOf((byte) pstart);
			min = Byte.valueOf((byte) pmin);
			max = Byte.valueOf((byte) pmax);
			step = Byte.valueOf((byte) pstep);
		} else if (type == Short.class || type == short.class) {
			start = Short.valueOf((short) pstart);
			min = Short.valueOf((short) pmin);
			max = Short.valueOf((short) pmax);
			step = Short.valueOf((short) pstep);
		} else if (type == Integer.class || type == int.class) {
			start = Integer.valueOf(pstart);
			min = Integer.valueOf(pmin);
			max = Integer.valueOf(pmax);
			step = Integer.valueOf(pstep);
		} else if (type == Long.class || type == long.class) {
			start = Long.valueOf(pstart);
			min = Long.valueOf(pmin);
			max = Long.valueOf(pmax);
			step = Long.valueOf(pstep);
		} else if (type == Float.class || type == float.class) {
			start = Float.valueOf(pstart);
			min = Float.valueOf(pmin);
			max = Float.valueOf(pmax);
			step = Float.valueOf(pstep);
		} else if (type == Double.class || type == double.class) {
			start = Double.valueOf(pstart);
			min = Double.valueOf(pmin);
			max = Double.valueOf(pmax);
			step = Double.valueOf(pstep);
		} else if (type == BigDecimal.class) {
			start = new BigDecimal(pstart);
			min = new BigDecimal(pmin);
			max = new BigDecimal(pmax);
			step = new BigDecimal(pstep);
		} else if (type == BigInteger.class) {
			start = new BigInteger(Integer.toString(pstart), 10);
			min = new BigInteger(Integer.toString(pmin), 10);
			max = new BigInteger(Integer.toString(pmax), 10);
			step = new BigInteger(Integer.toString(pstep), 10);
		}

		SpinnerModel model = new SpinnerNumberModel(start, min, max, step);

		spinner.setModel(model);

		formatSpinner();
	}
}
