package jhelp.util.gui;

import jhelp.util.list.Pair;
import jhelp.util.list.SortedArray;
import jhelp.util.text.UtilText;

/**
 * Horizontal gradient with fixed step
 * 
 * @author JHelp
 */
public class JHelpGradientHorizontal
      implements JHelpPaint
{
   /**
    * Represents a step
    * 
    * @author JHelp
    */
   public class Percent
         implements Comparable<Percent>
   {
      /** Step color */
      final int color;
      /** Step percent */
      final int percent;

      /**
       * Create a new instance of Percent
       * 
       * @param percent
       *           Percent
       * @param color
       *           Color
       */
      Percent(final int percent, final int color)
      {
         this.percent = percent;
         this.color = color;
      }

      /**
       * Compare the step with an other one.<br>
       * It returns:
       * <table>
       * <tr>
       * <th>&lt 0</th>
       * <td>:</td>
       * <td>If this step is before the given one</td>
       * </tr>
       * <tr>
       * <th>0</th>
       * <td>:</td>
       * <td>If this step is equals to the given one</td>
       * </tr>
       * <tr>
       * <th>&gt; 0</th>
       * <td>:</td>
       * <td>If this step is after to the given one</td>
       * </tr>
       * </table>
       * <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param percent
       *           Step to compare with
       * @return Comparison result
       * @see Comparable#compareTo(Object)
       */
      @Override
      public int compareTo(final Percent percent)
      {
         return this.percent - percent.percent;
      }

      /**
       * Step color
       * 
       * @return Step color
       */
      public int getColor()
      {
         return this.color;
      }

      /**
       * Step percent
       * 
       * @return Step percent
       */
      public int getPercent()
      {
         return this.percent;
      }

      /**
       * String representation <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return String representation
       * @see Object#toString()
       */
      @Override
      public String toString()
      {
         return UtilText.concatenate(this.percent, "% ", UtilText.colorText(this.color));
      }
   }

   /** Gradient steps */
   private final SortedArray<Percent> percents;
   /** Width of the current shape to fill */
   private int                        width;

   /**
    * Create a new instance of JHelpGradientHorizontal
    * 
    * @param colorStart
    *           Color at start (left)
    * @param colorEnd
    *           Color at end (right)
    */
   public JHelpGradientHorizontal(final int colorStart, final int colorEnd)
   {
      this.percents = new SortedArray<Percent>(Percent.class);

      this.percents.add(new Percent(0, colorStart));
      this.percents.add(new Percent(100, colorEnd));
   }

   /**
    * Add a color step
    * 
    * @param percent
    *           Percent of the step
    * @param color
    *           Step color
    */
   public void addColor(final int percent, final int color)
   {
      if((percent < 0) || (percent > 100))
      {
         throw new IllegalArgumentException("percent must be in [0, 100] not " + percent);
      }

      final Percent per = new Percent(percent, color);

      final int index = this.percents.indexOf(per);
      if(index >= 0)
      {
         this.percents.remove(index);
      }

      this.percents.add(per);
   }

   /**
    * Called when the gradient is about to be used <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param width
    *           Area width
    * @param height
    *           Area height
    * @see JHelpPaint#initializePaint(int, int)
    */
   @Override
   public void initializePaint(final int width, final int height)
   {
      this.width = width;
   }

   /**
    * Compute a pixel color <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @return Computed color
    * @see JHelpPaint#obtainColor(int, int)
    */
   @Override
   public int obtainColor(final int x, final int y)
   {
      final int xx = (x * 100) / this.width;
      final Percent per = new Percent(xx, 0);

      final Pair<Integer, Integer> interval = this.percents.intervalOf(per);

      int start = interval.element1;
      int end = interval.element2;

      if(start < 0)
      {
         return this.percents.getElement(0).color;
      }

      if(end < 0)
      {
         return this.percents.getElement(start - 1).color;
      }

      if(start == end)
      {
         return this.percents.getElement(start).color;
      }

      final int col1 = this.percents.getElement(start).color;
      final int col2 = this.percents.getElement(end).color;

      start = this.percents.getElement(start).percent;
      end = this.percents.getElement(end).percent;

      final int length = end - start;
      final int pos = xx - start;
      final int sop = length - pos;

      return ((((((col1 >> 24) & 0xFF) * sop) + (((col2 >> 24) & 0xFF) * pos)) / length) << 24) | //
            ((((((col1 >> 16) & 0xFF) * sop) + (((col2 >> 16) & 0xFF) * pos)) / length) << 16) | //
            ((((((col1 >> 8) & 0xFF) * sop) + (((col2 >> 8) & 0xFF) * pos)) / length) << 8) | //
            ((((col1 & 0xFF) * sop) + ((col2 & 0xFF) * pos)) / length);
   }

   /**
    * Steps list
    * 
    * @return Steps list
    */
   public Percent[] otainPercents()
   {
      return this.percents.toArray();
   }
}