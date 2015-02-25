/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "BrainData.java". Description:
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

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * TODO
 * 
 * @author TODO
 */
public class BrainData {

    /**
     * TODO
     */
    public static final String DATA_FILE_NAME = "t1_icbm_normal_1mm_pn3_rf20.rawb";

    /**
     * TODO
     */
    public static final int X_DIMENSIONS = 181;

    /**
     * TODO
     */
    public static final int Y_DIMENSIONS = 217;

    /**
     * TODO
     */
    public static final int Z_DIMENSIONS = 181;

    /**
     * TODO
     */
    public static final int X_START = 72;

    /**
     * TODO
     */
    public static final int Y_START = 126;

    /**
     * TODO
     */
    public static final int Z_START = 90;

    /**
     * TODO
     */
    public static final String DATA_FOLDER = "data";

    static final File dataFile = new File(DATA_FOLDER, DATA_FILE_NAME);

    private static final byte[][][] VOXEL_DATA = new byte[Z_DIMENSIONS][Y_DIMENSIONS][X_DIMENSIONS];

    private static boolean fileProcessed = false;

    /**
     * TODO
     */
    public static void initVoxelData() {
        processFile();

    }

    private static void processFile() {
        if (fileProcessed) {
            return;
        }
        fileProcessed = true;

        try {
            FileInputStream fileStream = new FileInputStream(dataFile);

            for (int zIndex = 0; zIndex < Z_DIMENSIONS; zIndex++) {
                for (int yIndex = 0; yIndex < Y_DIMENSIONS; yIndex++) {
                    fileStream.read(VOXEL_DATA[zIndex][yIndex]);

                }
            }

            if (fileStream.available() != 0) {
                throw new IOException(
                        "File size incorrect, does not match data dimensions");
            }
            fileStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args TODO
     */
    public static void main(String[] args) {

        MyCanvas canvas = new MyCanvas();
        Frame frame = new Frame("BrainView");
        frame.add(canvas);
        frame.setSize(300, 200);
        frame.setVisible(true);

        for (int i = -50; i < 50; i++) {

            canvas.setImagePosition(i);
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static class MyCanvas extends Canvas {

        private static final long serialVersionUID = 1L;

        final BrainTopImage image;

        public void setImagePosition(int zCoord) {
            image.setCoord(zCoord);
            repaint();
        }

        MyCanvas() {

            image = new BrainTopImage();

            // Add a listener for resize events
            addComponentListener(new ComponentAdapter() {
                // This method is called when the component's size changes
                public void componentResized(ComponentEvent evt) {
                    Component c = (Component) evt.getSource();

                    // Regenerate the image
                    c.repaint();

                }
            });
        }

        public void paint(Graphics g) {
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }
    }

    protected static byte[][][] getVoxelData() {
        initVoxelData();
        return VOXEL_DATA;
    }
}
