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
package automenta.vivisect.swing.property.swing.renderer;

import javax.swing.*;
import java.awt.*;

/**
 * ColorCellRenderer.
 */
public class ColorCellRenderer extends DefaultCellRenderer {

  public static String toHex(Color color) {
    String red = Integer.toHexString(color.getRed());
    String green = Integer.toHexString(color.getGreen());
    String blue = Integer.toHexString(color.getBlue());

    if (red.length() == 1) {
      red = '0' + red;
    }
    if (green.length() == 1) {
      green = '0' + green;
    }
    if (blue.length() == 1) {
      blue = '0' + blue;
    }
    return ('#' + red + green + blue).toUpperCase();
  }

  @Override
  protected String convertToString(Object value) {
    if (value instanceof Integer) {
      value = new Color((Integer) value);
    }
    if (!(value instanceof Color)) { return null; }

    Color color = (Color)value;
    return "R:" + color.getRed() + " G:" + color.getGreen() + " B:"
      + color.getBlue() + " - " + toHex(color);
  }

  @Override
  protected Icon convertToIcon(Object value) {
    if (value == null) { return null; }
    if (value instanceof Integer) {
      value = new Color((Integer) value);
    }
    return new PaintIcon((Paint)value);
  }

  public static class PaintIcon implements Icon {
    private final Paint color;
    private final int width;
    private final int height;

    public PaintIcon(Paint color) {
      this(color, 20, 10);
    }

    public PaintIcon(Paint color, int width, int height) {
      this.color = color;
      this.width = width;
      this.height = height;
    }

    @Override
    public int getIconHeight() {
      return height;
    }
    @Override
    public int getIconWidth() {
      return width;
    }
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2d = (Graphics2D)g;
      Paint oldPaint = g2d.getPaint();

      if (color != null) {
        g2d.setPaint(color);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
      }

      g.setColor(UIManager.getColor("controlDkShadow"));
      g.drawRect(x, y, getIconWidth(), getIconHeight());

      g2d.setPaint(oldPaint);
    }
  }
}
