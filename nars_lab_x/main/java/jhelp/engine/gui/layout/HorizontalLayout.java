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
 * Horizontal layout <br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class HorizontalLayout
      extends Layout
{
   /**
    * Comparator of elements <br>
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
       * Compare 2 elements
       * 
       * @param layoutElement1
       *           First
       * @param layoutElement2
       *           Second
       * @return <0 if first<second, 0 if first=second, or >0 if first>second
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
   public HorizontalLayout()
   {
   }

   /**
    * Compute preferred size
    * 
    * @param width
    *           Space with
    * @param height
    *           Space height
    * @param elements
    *           Elements to layout
    * @return Preferred size
    * @see Layout#computePrefferedSize(int, int, LayoutElement...)
    */
   @Override
   public Dimension computePrefferedSize(final int width, final int height, final LayoutElement... elements)
   {
      final Dimension dimension = new Dimension(0, height);
      Component component;
      Dimension preferred = null;

      for(final LayoutElement layoutElement : elements)
      {
         component = layoutElement.getComponent();

         component.refreshPreferredSize();
         preferred = component.getPrefrerredSize(preferred);

         dimension.width += preferred.width;
         dimension.height = Math.max(preferred.height, dimension.height);
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
    *           Elements to layout
    * @see Layout#layout(int, int, LayoutElement[])
    */
   @Override
   public void layout(final int width, final int height, final LayoutElement... elements)
   {
      Arrays.sort(elements, HorizontalLayout.COMPARATOR);

      int x = 0;
      Component component;
      Dimension preferred = null;

      for(final LayoutElement layoutElement : elements)
      {
         component = layoutElement.getComponent();

         preferred = component.getPrefrerredSize(preferred);

         component.setBounds(x, 0, preferred.width, height);

         x += preferred.width;
      }

      component = null;
      preferred = null;
   }
}