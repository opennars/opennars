/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "MatrixHandler.java". Description:
"ConfigurationHandler for float[][] values"

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

//import java.awt.BorderLayout;

import ca.nengo.config.IconRegistry;
import ca.nengo.config.ui.ConfigurationChangeListener;
import ca.nengo.config.ui.MatrixEditor;
import ca.nengo.util.MU.MatrixExpander;
import ca.nengo.util.MU.VectorExpander;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;

/**
 * ConfigurationHandler for float[][] values.
 *
 * @author Bryan Tripp
 */
public class MatrixHandler extends MatrixHandlerBase {

	/**
	 * ConfigurationHandler for float[][] values.
	 */
	public MatrixHandler() {
		super(float[][].class);
	}

	public MatrixEditor CreateMatrixEditor(Object o,
			final ConfigurationChangeListener configListener) {
		/*
		 * The Matrix editor is created in a new JDialog.
		 */

		final float[][] matrix = (float[][]) o;

		// Create Matrix Editor
		final MatrixEditor matrixEditor;
		{

			float[][] copy = new float[matrix.length][];
			for (int i = 0; i < matrix.length; i++) {
				copy[i] = new float[matrix[i].length];
				System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
			}
			matrixEditor = new MatrixEditor(copy, false, false);
		}

		// Setup config listener's proxy
		{
			configListener.setProxy(new ConfigurationChangeListener.EditorProxy() {
				public Object getValue() {
					return matrixEditor.getMatrix();
				}
			});
		}
		return matrixEditor;
	}

	@Override
	public Component getRenderer(Object o) {
		JPanel result = new JPanel(new FlowLayout());

		float[][] matrix = (float[][]) o;
		String text = toString(matrix, '\t', "\r\n");
		result.add(new JLabel(IconRegistry.getInstance().getIcon(o)));
		result.add(new JTextArea(text));

		return result;
	}

	@Override
	public Object fromString(String s) {
		return fromString(s, ',', "\r\n");
	}

	@Override
	public String toString(Object o) {
		return toString((float[][]) o, ',', "\r\n");
	}

	/**
	 * @param s
	 *            A String representation of a matrix, eg from
	 *            toString(float[][], char, String)
	 * @param colDelim
	 *            The character used to delimit matrix columns in this string
	 * @param rowDelim
	 *            The string (can be >1 chars) used to delimit matrix rows in
	 *            this string
	 * @return The matrix represented by the string
	 */
	public static float[][] fromString(String s, char colDelim, String rowDelim) {
		String colDelimString = String.valueOf(colDelim);
		MatrixExpander result = new MatrixExpander();

		StringTokenizer rowTokenizer = new StringTokenizer(s, rowDelim, false);
		while (rowTokenizer.hasMoreTokens()) {
			StringTokenizer elemTokenizer = new StringTokenizer(rowTokenizer.nextToken(),
					colDelimString, false);
			VectorExpander row = new VectorExpander();
			while (elemTokenizer.hasMoreTokens()) {
				row.add(Float.parseFloat(elemTokenizer.nextToken()));
			}
			result.add(row.toArray());
		}
		return result.toArray();
	}

	/**
	 * @param matrix
	 *            A matrix
	 * @param colDelim
	 *            A character to be used to delimit matrix columns
	 * @param rowDelim
	 *            A String to be used to delimit matrix rows
	 * @return A String representation of the given matrix using the given
	 *         delimiters
	 */
	public static String toString(float[][] matrix, char colDelim, String rowDelim) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				result.append(matrix[i][j]);
				if (j < matrix[i].length - 1) {
                    result.append(colDelim);
                }
			}
			if (i < matrix.length - 1) {
                result.append(rowDelim);
            }
		}
		return result.toString();
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
	public Object getDefaultValue(Class<?> c) {
		return new float[0][];
	}

}
