/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jurls.core.utils;

import jurls.core.approximation.ParameterizedFunction;
import jurls.core.brain.NeuroMap;
import jurls.core.brain.NeuroMap.InputOutput;
import org.apache.commons.math3.linear.RealMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders a matrix as an image. Positive elements are shades of red, negative
 * shades of blue, 0 is black.
 *
 * @author Peter Abeles
 */
public class MatrixImage extends JComponent {

    protected BufferedImage image;
    private double maxValue;
    private double minValue;

    public MatrixImage(int width, int height) {

        setDoubleBuffered(true);
        setIgnoreRepaint(true);

        setBackground(Color.BLACK);

        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

    }
    public MatrixImage(int width, int height, double min, double max) {
        this(width, height);
        minValue = min;
        maxValue = max;
    }

    public int val2col(double n, double min, double max) {
        double mean = (max + min) / 2.0;
        double n5 = min + 2.0 * (max - min) / 3.0;
        int r;
        r = n < mean ? (int) (255.0 * (min - n) / (mean - min)) + 255 : 0;
        if (r < 0) {
            r = 0;
        }
        if (r > 255) {
            r = 255;
        }
        int g;
        if (n < mean) {
            g = (int) (255.0 * (n - min) / (mean - min));
        } else if (n < n5) {
            g = 255;
        } else {
            g = (int) (255.0 * (n5 - n) / (max - n5)) + 255;
        }
        if (g < 0) {
            g = 0;
        }
        if (g > 255) {
            g = 255;
        }
        int b;
        if (n < mean) {
            b = 0;
        } else if (n < n5) {
            b = (int) (255.0 * (n - mean) / (n5 - mean));
        } else {
            b = 255;
        }
        if (b < 0) {
            b = 0;
        }
        if (b > 255) {
            b = 255;
        }

        return 255 << 24 | b << 16 | g << 8 | r;
    }

    public int getColorRedBlue(double value) {
        if (value == 0) {
            return 255 << 24;
        } else if (value > 0) {
            int p = 255 - (int) (255.0 * (value - minValue) / (maxValue - minValue));
            return 255 << 24 | 255 << 16 | p << 8 | p;
        } else {
            int p = 255 + (int) (255.0 * (value - minValue) / (maxValue - minValue));
            return 255 << 24 | p << 16 | p << 8 | 255;
        }

    }

    public int getColor(double value) {
        return val2col(value, -1, 1);
    }

    public interface Data2D {

        double getValue(int x, int y);
    }

    public void draw(double[] v, double minValue, double maxValue, boolean vertical) {
        int w, h;
        if (vertical) {
            w = 1;
            h = v.length;
        } else {
            w = v.length;
            h = 1;
        }

        draw((x, y) -> v[x + y], w, h, minValue, maxValue);
    }

    public void draw(ParameterizedFunction f) {
        int numParam = f.numberOfParameters();
        int cw = (int) Math.ceil(Math.sqrt(numParam));
        int ch = numParam / cw;
        draw((x, y) -> {
            int i = y * ch + x;
            if (i < numParam) {
                return f.getParameter(i);
            }
            return 0;
        }, cw, ch, -1.0, 1.0);

    }

    public void draw(RealMatrix M, double minValue, double maxValue) {
        draw(M::getEntry, M.getColumnDimension(), M.getRowDimension(), minValue, maxValue);
    }

    /* todo move these variables to a subclass specifically for visualizing neuromap */
    InputOutput io = null;
    int lx = -1, ly = -1;
    int row = 0;

    public void draw(NeuroMap m, double minValue, double maxValue, int maxRows) {

        int entries = Math.min(maxRows, m.getCapacity());
        int row = m.getIndex() - entries;

        draw(new Data2D() {

            double[] input, output; //cache for speed

            @Override
            public double getValue(int y, int x) {

                if (y != ly) {
                    io = m.memory[y + row];
                    input = io.input;
                    output = io.output;
                }

                ly = y;
                lx = x;

                if ((io != null) && (input != null) && (output != null)) {
                    int ioil = input.length;
                    if (lx < ioil) {
                        return input[lx];
                    } else {
                        lx -= ioil;
                        if (lx < output.length) {
                            return output[lx];
                        }
                    }
                }
                return 0;
            }

        }, (m.getDimensions(true, true)), entries, minValue, maxValue);
    }


    public void draw(Data2D d, int cw, int ch, double minValue, double maxValue) {

        if ((cw == 0) || (ch == 0)) {
            image = null;
            return;
        }

        if (image == null || image.getWidth() != cw || image.getHeight() != ch) {
            image = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_RGB);
        }


        this.minValue = minValue;
        this.maxValue = maxValue;

        int w = image.getWidth();
        int h = image.getHeight();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                double value = d.getValue(i, j);
                pixel(image, j, i, value);
            }
        }

        repaint();
    }

    protected void pixel(BufferedImage image, int j, int i, double value) {
        image.setRGB(j, i, getColor(value));
    }


    @Override
    public void paint(Graphics g) {

        if (image == null) {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        else
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

    }

}
