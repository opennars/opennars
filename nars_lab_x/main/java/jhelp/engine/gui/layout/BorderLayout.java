/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.layout<br>
 * Class : BorderLayout<br>
 * Date : 26 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

import jhelp.engine.gui.components.Component;
import jhelp.util.math.UtilMath;

/**
 * Border layout<br>
 * <br>
 * Last modification : 26 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class BorderLayout
      extends Layout
{

   /**
    * Constructs BorderLayout
    */
   public BorderLayout()
   {
   }

   /**
    * Compute preferred dimensions
    * 
    * @param width
    *           Space width
    * @param height
    *           Space height
    * @param elements
    *           Elements to layout
    * @return Preferred dimensions
    * @see Layout#computePrefferedSize(int, int, jhelp.engine.gui.layout.LayoutElement[])
    */
   @Override
   public Dimension computePrefferedSize(final int width, final int height, final LayoutElement... elements)
   {
      int topLeftWidth = 0;
      int topLeftHeight = 0;
      int topWidth = 0;
      int topHeight = 0;
      int topRightWidth = 0;
      int topRightHeight = 0;

      int leftWidth = 0;
      int leftHeight = 0;
      int centerWidth = 0;
      int centerHeight = 0;
      int rightWidth = 0;
      int rightHeight = 0;

      int bottomLeftWidth = 0;
      int bottomLeftHeight = 0;
      int bottomWidth = 0;
      int bottomHeight = 0;
      int bottomRightWidth = 0;
      int bottomRightHeight = 0;

      Component component;
      Dimension preferredDimension = null;

      for(final LayoutElement element : elements)
      {
         component = element.getComponent();
         component.refreshPreferredSize();

         preferredDimension = component.getPrefrerredSize(preferredDimension);

         switch((BorderLayoutConstraints) element.getConstraints())
         {
            case TOP_LEFT:
               topLeftWidth = preferredDimension.width;
               topLeftHeight = preferredDimension.height;
            break;
            case TOP:
               topWidth = preferredDimension.width;
               topHeight = preferredDimension.height;
            break;
            case TOP_RIGHT:
               topRightWidth = preferredDimension.width;
               topRightHeight = preferredDimension.height;
            break;

            case LEFT:
               leftWidth = preferredDimension.width;
               leftHeight = preferredDimension.height;
            break;
            case CENTER:
               centerWidth = preferredDimension.width;
               centerHeight = preferredDimension.height;
            break;
            case RIGHT:
               rightWidth = preferredDimension.width;
               rightHeight = preferredDimension.height;
            break;

            case BOTTOM_LEFT:
               bottomLeftWidth = preferredDimension.width;
               bottomLeftHeight = preferredDimension.height;
            break;
            case BOTTOM:
               bottomWidth = preferredDimension.width;
               bottomHeight = preferredDimension.height;
            break;
            case BOTTOM_RIGHT:
               bottomRightWidth = preferredDimension.width;
               bottomRightHeight = preferredDimension.height;
            break;
         }
      }

      return new Dimension(
            Math.max(UtilMath.maxIntegers(topLeftWidth, leftWidth, bottomLeftWidth) + UtilMath.maxIntegers(topWidth, centerWidth, bottomWidth) + UtilMath.maxIntegers(topRightWidth, rightWidth, bottomRightWidth), width),//
            Math.max(UtilMath.maxIntegers(topLeftHeight, leftHeight, bottomLeftHeight) + UtilMath.maxIntegers(topHeight, centerHeight, bottomHeight) + UtilMath.maxIntegers(topRightHeight, rightHeight, bottomRightHeight), height));
   }

   /**
    * Layout dimensions
    * 
    * @param width
    *           Space width
    * @param height
    *           Space height
    * @param elements
    *           Elements to layout
    * @see Layout#layout(int, int, jhelp.engine.gui.layout.LayoutElement[])
    */
   @Override
   public void layout(final int width, final int height, final LayoutElement... elements)
   {
      int topLeftWidth = 0;
      int topLeftHeight = 0;
      int topHeight = 0;
      int topRightWidth = 0;
      int topRightHeight = 0;

      int leftWidth = 0;
      int centerWidth = 0;
      int centerHeight = 0;
      int rightWidth = 0;

      int bottomLeftWidth = 0;
      int bottomLeftHeight = 0;
      int bottomHeight = 0;
      int bottomRightWidth = 0;
      int bottomRightHeight = 0;

      Component component;
      Dimension preferredDimension = null;

      for(final LayoutElement element : elements)
      {
         component = element.getComponent();
         component.refreshPreferredSize();

         preferredDimension = component.getPrefrerredSize(preferredDimension);

         switch((BorderLayoutConstraints) element.getConstraints())
         {
            case TOP_LEFT:
               topLeftWidth = preferredDimension.width;
               topLeftHeight = preferredDimension.height;
            break;
            case TOP:
               topHeight = preferredDimension.height;
            break;
            case TOP_RIGHT:
               topRightWidth = preferredDimension.width;
               topRightHeight = preferredDimension.height;
            break;

            case LEFT:
               leftWidth = preferredDimension.width;
            break;
            case CENTER:
            break;
            case RIGHT:
               rightWidth = preferredDimension.width;
            break;

            case BOTTOM_LEFT:
               bottomLeftWidth = preferredDimension.width;
               bottomLeftHeight = preferredDimension.height;
            break;
            case BOTTOM:
               bottomHeight = preferredDimension.height;
            break;
            case BOTTOM_RIGHT:
               bottomRightWidth = preferredDimension.width;
               bottomRightHeight = preferredDimension.height;
            break;
         }
      }

      final int top = UtilMath.maxIntegers(topLeftHeight, topHeight, topRightHeight);
      final int left = UtilMath.maxIntegers(topLeftWidth, leftWidth, bottomLeftWidth);
      final int bottom = UtilMath.maxIntegers(bottomLeftHeight, bottomHeight, bottomRightHeight);
      final int right = UtilMath.maxIntegers(topRightWidth, rightWidth, bottomRightWidth);
      centerWidth = width - left - right;
      centerHeight = height - top - bottom;
      final int x1 = left;
      final int y1 = top;
      final int x2 = width - right;
      final int y2 = height - bottom;

      int x, y, w, h;

      for(final LayoutElement element : elements)
      {
         component = element.getComponent();

         switch((BorderLayoutConstraints) element.getConstraints())
         {
            case TOP_LEFT:
               component.setBounds(0, 0, left, top);
            break;
            case TOP:
               x = x1;
               y = 0;
               w = centerWidth;
               h = top;
               if(topLeftWidth == 0)
               {
                  x = 0;
                  w += left;
               }
               if(topRightWidth == 0)
               {
                  w += right;
               }
               component.setBounds(x, y, w, h);
            break;
            case TOP_RIGHT:
               component.setBounds(x2, 0, right, top);
            break;
            case LEFT:
               component.setBounds(0, y1, left, centerHeight);
            break;
            case CENTER:
               x = x1;
               y = y1;
               w = centerWidth;
               h = centerHeight;
               if(leftWidth == 0)
               {
                  x = 0;
                  w += left;
               }
               else if(topHeight == 0)
               {
                  y = 0;
                  h += top;
               }

               if(rightWidth == 0)
               {
                  w += right;
               }
               else if(bottomHeight == 0)
               {
                  h += bottom;
               }
               component.setBounds(x, y, w, h);
            break;
            case RIGHT:
               component.setBounds(x2, y1, right, centerHeight);
            break;
            case BOTTOM_LEFT:
               component.setBounds(0, y2, left, bottom);
            break;
            case BOTTOM:
               x = x1;
               y = 0;
               w = centerWidth;
               h = top;
               if(bottomLeftWidth == 0)
               {
                  x = 0;
                  w += left;
               }
               if(bottomRightWidth == 0)
               {
                  w += right;
               }
               component.setBounds(x, y, w, h);
            break;
            case BOTTOM_RIGHT:
               component.setBounds(x2, y2, right, bottom);
            break;
         }
      }
   }
}