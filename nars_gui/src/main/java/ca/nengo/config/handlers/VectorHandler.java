/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "VectorHandler.java". Description:
"ConfigurationHandler for float[] values"

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

import ca.nengo.config.IconRegistry;
import ca.nengo.config.ui.ConfigurationChangeListener;
import ca.nengo.config.ui.MatrixEditor;

import javax.swing.*;
import java.awt.*;

/**
 * ConfigurationHandler for float[] values.
 *
 * @author Bryan Tripp
 */
public class VectorHandler extends MatrixHandlerBase {

	/**
	 * ConfigurationHandler for float[] values.
	 */
	public VectorHandler() {
		super(float[].class);
	}

	@Override
	public Component getRenderer(Object o) {
		JLabel result = new JLabel(MatrixHandler.toString(
				new float[][] { (float[]) o }, ' ', "\r\n"), IconRegistry
				.getInstance().getIcon(o), SwingConstants.LEFT);
		result.setFont(result.getFont().deriveFont(Font.PLAIN));
		return result;
	}

	@Override
	public String toString(Object o) {
		return MatrixHandler.toString(new float[][] { (float[]) o }, ',',
				"\r\n");
	}

	@Override
	public Object fromString(String s) {
		return MatrixHandler.fromString(s, ',', "\r\n")[0];
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
	public Object getDefaultValue(Class<?> c) {
		return new float[0];
	}

	@Override
	public MatrixEditor CreateMatrixEditor(Object o,
			ConfigurationChangeListener configListener) {
		final MatrixEditor matrixEditor;
		{
			float[] copy;
			{
				float[] vector = (float[]) o;
				copy = new float[vector.length];
				System.arraycopy(vector, 0, copy, 0, vector.length);
			}

			matrixEditor = new MatrixEditor(new float[][] { copy }, true, false);

			configListener
					.setProxy(new ConfigurationChangeListener.EditorProxy() {
						public Object getValue() {
							return matrixEditor.getMatrix()[0];
						}
					});

			return matrixEditor;
		}
	}

}
