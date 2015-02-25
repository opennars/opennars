/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ConfigurableMatrix.java". Description: 
""

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

package ca.nengo.ui.config.matrixEditor;
//package ca.nengo.ui.configurable.matrixEditor;
//
//import ca.nengo.ui.configurable.ConfigException;
//import ca.nengo.ui.configurable.IConfigurable;
//import ca.nengo.ui.configurable.PropertyDescriptor;
//import ca.nengo.ui.configurable.PropertyInputPanel;
//import ca.nengo.ui.configurable.PropertySet;
//
///**
// * Configurable transformation matrix
// * 
// * @author Shu Wu
// */
//public class ConfigurableMatrix implements IConfigurable {
//
//	/**
//	 * Number of columns
//	 */
//	private int fromSize;
//
//	/**
//	 * Matrix to be created
//	 */
//	private float[][] myMatrix;
//
//	/**
//	 * Config Descriptor for the Matrix
//	 */
//	private PropertyDescriptor pMatrix;
//
//	/**
//	 * Number of rows
//	 */
//	private int toSize;
//
//	/**
//	 * @param matrixValues
//	 *            Starting values for the matrix
//	 */
//	public ConfigurableMatrix(float[][] matrixValues) {
//		super();
//		this.fromSize = matrixValues[0].length;
//		this.toSize = matrixValues.length;
//
//		pMatrix = new CouplingMatrixProp(matrixValues);
//
//	}
//
//	/**
//	 * @param fromSize
//	 *            From size of the matrix to be created
//	 * @param toSize
//	 *            To size of the matrix to be created
//	 */
//	public ConfigurableMatrix(int fromSize, int toSize) {
//		super();
//		this.fromSize = fromSize;
//		this.toSize = toSize;
//
//		pMatrix = new CouplingMatrixProp(fromSize, toSize);
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see ca.nengo.ui.configurable.IConfigurable#completeConfiguration(ca.nengo.ui.configurable.ConfigParam)
//	 */
//	public void completeConfiguration(PropertySet properties) {
//		myMatrix = (float[][]) properties.getProperty(pMatrix);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see ca.nengo.ui.configurable.IConfigurable#getConfigSchema()
//	 */
//	public PropertyDescriptor[] getConfigSchema() {
//		PropertyDescriptor[] props = new PropertyDescriptor[1];
//		props[0] = pMatrix;
//		return props;
//	}
//
//	/**
//	 * @return Matrix
//	 */
//	public float[][] getMatrix() {
//		return myMatrix;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see ca.nengo.ui.configurable.IConfigurable#getTypeName()
//	 */
//	public String getTypeName() {
//		return fromSize + " to " + toSize + " Coupling Matrix";
//	}
//
//	public void preConfiguration(PropertySet props) throws ConfigException {
//		// do nothing
//	}
//
//}
//
///**
// * Input panel for a matrix
// * 
// * @author Shu Wu
// */
//class CouplingMatrixInputPanel extends PropertyInputPanel {
//
//	private static final long serialVersionUID = 1L;
//	private CouplingMatrixImpl couplingMatrix;
//
//	/**
//	 * Editor responsible for creating the spreadsheet-like matrix editing
//	 * Interface
//	 */
//	private MatrixEditor editor;
//
//	public CouplingMatrixInputPanel(PropertyDescriptor property, int fromSize,
//			int toSize) {
//		super(property);
//
//		couplingMatrix = new CouplingMatrixImpl(fromSize, toSize);
//		editor = new MatrixEditor(couplingMatrix);
//
//		addToPanel(editor);
//	}
//
//	@Override
//	public float[][] getValue() {
//		editor.finishEditing();
//
//		return couplingMatrix.getData();
//	}
//
//	@Override
//	public boolean isValueSet() {
//		return true;
//	}
//
//	@Override
//	public void setValue(Object value) {
//		/*
//		 * Transfers the new matrix values to the editor
//		 */
//		if (value instanceof float[][]) {
//			float[][] matrix = (float[][]) value;
//
//			for (int i = 0; i < matrix.length; i++) {
//
//				for (int j = 0; j < matrix[i].length; j++) {
//					editor.setValueAt(matrix[i][j], i, j);
//				}
//			}
//		}
//	}
//}
//
///**
// * Config Descriptor for a Coupling Matrix
// * 
// * @author Shu Wu
// */
//class CouplingMatrixProp extends PropertyDescriptor {
//
//	private static final long serialVersionUID = 1L;
//	private int fromSize, toSize;
//
//	public CouplingMatrixProp(float[][] matrixValues) {
//		super("Editor", matrixValues);
//		init(matrixValues[0].length, matrixValues.length);
//	}
//
//	public CouplingMatrixProp(int fromSize, int toSize) {
//		super("Editor");
//		init(fromSize, toSize);
//	}
//
//	private void init(int fromSize, int toSize) {
//		this.fromSize = fromSize;
//		this.toSize = toSize;
//	}
//
//	@Override
//	protected PropertyInputPanel createInputPanel() {
//		return new CouplingMatrixInputPanel(this, fromSize, toSize);
//	}
//
//	@Override
//	public Class<float[][]> getTypeClass() {
//		return float[][].class;
//	}
//
//	@Override
//	public String getTypeName() {
//		return "Coupling Matrix";
//	}
//
//}
