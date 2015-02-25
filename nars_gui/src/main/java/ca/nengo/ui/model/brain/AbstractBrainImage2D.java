/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "AbstractBrainImage2D.java". Description:
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

package ca.nengo.ui.model.brain;

import java.awt.image.*;

/**
 * TODO
 * 
 * @author TODO
 */
public abstract class AbstractBrainImage2D extends BufferedImage {
    private static ColorModel colorModel;

    private static ColorModel getBrainColorModel() {
        if (colorModel != null) {
            return colorModel;
        }

        byte[] arrR = new byte[256];
        byte[] arrG = new byte[256];
        byte[] arrB = new byte[256];

        for (int i = 0; i < 256; i++) {
            arrR[i] = (byte) (i * 0.8);
            arrG[i] = (byte) (i * 0.8);
            arrB[i] = (byte) (i * 1.0);
        }

        colorModel = new IndexColorModel(8, 256, arrR, arrG, arrB);

        return colorModel;
    }

    private static WritableRaster getBrainRaster(int width, int height) {
        return Raster.createWritableRaster(getBrainSampleModel(width, height),
                null);
    }

    private static SampleModel getBrainSampleModel(int width, int height) {
        int bitMasks[] = new int[] { 0xff };
        SinglePixelPackedSampleModel model = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, width, height, bitMasks);

        return model;
    }

    int viewCoord;

    final int imageWidth;
    final int imageHeight;

    /**
     * @param width TODO
     * @param height TODO
     */
    public AbstractBrainImage2D(int width, int height) {
        super(getBrainColorModel(), getBrainRaster(width, height), false, null);
        imageWidth = width;
        imageHeight = height;
        setCoord(getCoordDefault());
    }

    private void updateViewCoord() {

        byte[] imageArray = new byte[imageWidth * imageHeight];
        int imageArrayIndex = 0;
        for (int imageY = imageHeight - 1; imageY >= 0; imageY--) {

            for (int imageX = 0; imageX < imageWidth; imageX++) {
                // image.getRaster().setPixel(x, y, new int[] { 0 });

                imageArray[imageArrayIndex++] = getImageByte(imageX, imageY);
            }
        }
        DataBuffer buffer = new DataBufferByte(imageArray, imageArray.length, 0);

        WritableRaster raster = Raster.createWritableRaster(getSampleModel(),
                buffer, null);

        setData(raster);

    }

    /**
     * @return TODO
     */
    public int getCoordDefault() {
        return 0;
    }

    /**
     * @return TODO
     */
    public abstract int getCoordMax();

    /**
     * @return TODO
     */
    public abstract int getCoordMin();

    /**
     * @return TODO
     */
    public int getCoord() {
        return viewCoord;
    }

    /**
     * @param coord TODO
     */
    public void setCoord(int coord) {
        if (coord > getCoordMax()) {
            coord = getCoordMax();
        } else if (coord < getCoordMin()) {
            coord = getCoordMin();
        }
        viewCoord = coord;
        updateViewCoord();

    }

    /**
     * @param imageX TODO
     * @param imageY TODO
     * @return TODO
     */
    public abstract byte getImageByte(int imageX, int imageY);

    /**
     * @return TODO
     */
    public abstract String getViewName();

    /**
     * @return TODO
     */
    public abstract String getCoordName();

}
