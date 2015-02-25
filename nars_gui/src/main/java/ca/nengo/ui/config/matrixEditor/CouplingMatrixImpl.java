/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CouplingMatrixImpl.java". Description:
"Default implementation of coupling matrix.

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
 * Created on Jan 6, 2004
 */
package ca.nengo.ui.config.matrixEditor;

/**
 * Default implementation of coupling matrix.
 * 
 * @author Bryan Tripp
 */
public class CouplingMatrixImpl implements CouplingMatrix {

    private float[][] myData;
    private int myFromSize;
    private int myToSize;

    /**
     *  TODO
     */
    public CouplingMatrixImpl() {

    }

    /**
     * Creates a new instance.
     * 
     * @param theFromSize
     *            the number of rows/columns in the matrix
     * @param theToSize TODO
     */
    public CouplingMatrixImpl(int theFromSize, int theToSize) {
        myFromSize = theFromSize;
        myToSize = theToSize;
        myData = new float[theToSize][theFromSize];
        if (theFromSize==theToSize) {
            for (int i=0; i<theToSize; i++)
            {
                myData[i][i]=1;
            }
        }
    }

    // convenience method for producing an error message
    private void checkIndex(int theIndex, boolean isRow) {
        String rowOrCol = (isRow) ? "row" : "col";
        int size = (isRow) ? getToSize() : getFromSize();
        if (theIndex < 1 || theIndex > size) {
            throw new IllegalArgumentException("There is no " + rowOrCol + '#'
                    + theIndex);
        }
    }

    /**
     * @return TODO
     */
    public float[][] getData() {
        return myData;
    }

    /**
     * @see ca.nengo.ui.config.matrixEditor.CouplingMatrix#getElement(int,
     *      int)
     */
    public float getElement(int theRow, int theCol) {
        checkIndex(theRow, true);
        checkIndex(theCol, false);
        return myData[theRow - 1][theCol - 1];
    }

    public int getFromSize() {
        return myFromSize;
    }

    public int getToSize() {
        return myToSize;
    }

    /**
     * @param theData TODO
     */
    public void setData(float[][] theData) {
        myData = theData;
    }

    public void setElement(float theValue, int row, int col) {
        myData[row - 1][col - 1] = theValue;
    }

}
