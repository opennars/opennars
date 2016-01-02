/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.layout<br>
 * Class : VerticalLayout<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

import jhelp.engine.gui.components.Component;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Vertical layout to place components in vertical<br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class VerticalLayout
      extends Layout
{
   /**
    * Compare 2 layout elements <br>
    * <br>
    * Last modification : 30 nov. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   private static class ComparatorLayoutElement
         implements Comparator<LayoutElement>
   {
      /**
       * Constructs ComparatorLayoutElement
       */
      ComparatorLayoutElement()
      {
      }

      /**
       * Compare 2 layout elements
       * 
       * @param layoutElement1
       *           First
       * @param layoutElement2
       *           Second
       * @return -1 if first after second, 0 if equals, 1 if first before second
       * @see Comparator#compare(Object, Object)
       */
      @Override
      public int compare(final LayoutElement layoutElement1, final LayoutElement layoutElement2)
      {
         return layoutElement1.getConstraints().hashCode() - layoutElement2.getConstraints().hashCode();
      }
   }

   /** Comparator singleton */
   private static final ComparatorLayoutElement COMPARATOR = new ComparatorLayoutElement();

   /**
    * Constructs VerticalLayout
    */
   public VerticalLayout()
   {
   }

   /**
    * Compute layout preferred size
    * 
    * @param elements
    *           Elements in layout
    * @return Preferred size
    * @see Layout#computePrefferedSize(int, int, LayoutElement...)
    */
   @Override
   public Dimension computePrefferedSize(final int width, final int height, final LayoutElement... elements)
   {
      final Dimension dimension = new Dimension(width, 0);
      Component component;
      Dimension preferred = null;

      for(final LayoutElement layoutElement : elements)
      {
         component = layoutElement.getComponent();

         component.refreshPreferredSize();
         preferred = component.getPrefrerredSize(preferred);

         dimension.width = Math.max(dimension.width, preferred.width);
         dimension.height += preferred.height;
      }

      component = null;
      preferred = null;

      return dimension;
   }

   /**
    * Layout components
    * 
    * @param width
    *           Space width
    * @param height
    *           Space height
    * @param elements
    *           Elemnts to layout
    * @see Layout#layout(int, int, LayoutElement[])
    */
   @Override
   public void layout(final int width, final int height, final LayoutElement... elements)
   {
      Arrays.sort(elements, VerticalLayout.COMPARATOR);

      int y = 0;
      Component component;
      Dimension preferred = null;

      for(final LayoutElement layoutElement : elements)
      {
         component = layoutElement.getComponent();

         preferred = component.getPrefrerredSize(preferred);

         component.setBounds(0, y, width, preferred.height);

         y += preferred.height;
      }

      component = null;
      preferred = null;
   }
}