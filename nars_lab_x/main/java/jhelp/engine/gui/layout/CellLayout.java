/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.layout<br>
 * Class : CellLayout<br>
 * Date : 27 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

import jhelp.engine.gui.components.Component;

/**
 * Layout by cells<br>
 * <br>
 * Last modification : 27 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class CellLayout
      extends Layout
{
   /** Number of cells in height size */
   private final int numberCellHeight;
   /** Number of cells in width size */
   private final int numberCellWidth;

   /**
    * Constructs CellLayout
    * 
    * @param numberCellWidth
    *           Number of cells in width size
    * @param numberCellHeight
    *           Number of cells in height size
    */
   public CellLayout(final int numberCellWidth, final int numberCellHeight)
   {
      this.numberCellWidth = numberCellWidth;
      this.numberCellHeight = numberCellHeight;
   }

   /**
    * Compute preferred size
    * 
    * @param width
    *           Space width
    * @param height
    *           Space height
    * @param elements
    *           Element to layout
    * @return Preferred size
    * @see Layout#computePrefferedSize(int, int, jhelp.engine.gui.layout.LayoutElement[])
    */
   @Override
   public Dimension computePrefferedSize(final int width, final int height, final LayoutElement... elements)
   {
      int w = 0;
      int h = 0;

      Component component;
      Dimension dimension = null;
      CellLayoutConstraints cellLayoutConstraints;

      for(final LayoutElement layoutElement : elements)
      {
         component = layoutElement.getComponent();
         cellLayoutConstraints = (CellLayoutConstraints) layoutElement.getConstraints();

         component.refreshPreferredSize();
         dimension = component.getPrefrerredSize(dimension);

         w = Math.max(w, (dimension.width * this.numberCellWidth) / cellLayoutConstraints.width);
         h = Math.max(h, (dimension.height * this.numberCellHeight) / cellLayoutConstraints.height);
      }

      return new Dimension(w, h);
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
    * @see Layout#layout(int, int, jhelp.engine.gui.layout.LayoutElement[])
    */
   @Override
   public void layout(final int width, final int height, final LayoutElement... elements)
   {
      Component component;
      CellLayoutConstraints cellLayoutConstraints;

      final int cellWidth = width / this.numberCellWidth;
      final int cellHeight = height / this.numberCellHeight;

      for(final LayoutElement layoutElement : elements)
      {
         component = layoutElement.getComponent();
         cellLayoutConstraints = (CellLayoutConstraints) layoutElement.getConstraints();

         component.refreshPreferredSize();
         component.setBounds(cellWidth * cellLayoutConstraints.x,//
               cellHeight * cellLayoutConstraints.y,//
               cellWidth * cellLayoutConstraints.width, cellHeight * cellLayoutConstraints.height);
      }
   }
}