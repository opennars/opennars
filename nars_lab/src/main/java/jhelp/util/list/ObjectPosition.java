package jhelp.util.list;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describe an object position inside a {@link Bag2D}
 * 
 * @author JHelp
 * @param <OBJECT>
 *           Object type
 */
public class ObjectPosition<OBJECT extends SizedObject>
      implements Comparable<ObjectPosition<OBJECT>>
{
   /** Next position ID */
   private final static AtomicInteger NEXT = new AtomicInteger(1);
   /** Object itself */
   private final OBJECT               object;
   /** Object priority (Used by algorithm of place element inside a bag) */
   private long                       priority;
   /** Object index in the bag */
   final int                          index;
   /** X of up left cornner of object in the bag */
   int                                x;
   /** Y of up left cornner of object in the bag */
   int                                y;

   /**
    * Create a new instance of ObjectPosition
    * 
    * @param object
    *           Object itself
    * @param bagWidth
    *           Bag width
    * @param bagHeight
    *           Bag height
    */
   ObjectPosition(final OBJECT object, final int bagWidth, final int bagHeight)
   {
      this.index = ObjectPosition.NEXT.getAndIncrement();
      this.object = object;

      final int[][] bag = new int[bagWidth][bagHeight];
      final int w = object.getWidth();
      final int h = object.getHeight();
      for(int y = 0; y < h; y++)
      {
         for(int x = 0; x < w; x++)
         {
            bag[x][y] = 1;
         }
      }

      this.priority = 0;
      long ww;
      long hh;
      int mx;
      boolean eliminateLine;
      for(int yy = 0; yy < bagHeight; yy++)
      {
         for(int xx = 0; xx < bagWidth; xx++)
         {
            if(bag[xx][yy] == 0)
            {
               ww = hh = 1;
               bag[xx][yy] = -1;

               for(mx = xx + 1; (mx < bagWidth) && (bag[mx][yy] == 0); mx++)
               {
                  ww++;
                  bag[mx][yy] = -1;
               }

               eliminateLine = true;
               for(int yyy = yy + 1; (yyy < bagHeight) && (eliminateLine == true); yyy++)
               {
                  for(int xxx = xx; xxx < mx; xxx++)
                  {
                     if(bag[xxx][yyy] != 0)
                     {
                        eliminateLine = false;
                        break;
                     }
                  }

                  if(eliminateLine == true)
                  {
                     for(int xxx = xx; xxx < mx; xxx++)
                     {
                        bag[xxx][yyy] = -1;
                     }
                     hh++;
                  }
               }

               this.priority += ww * hh * ww * hh;
            }
         }
      }
   }

   /**
    * Compare the position with an other one.<br>
    * It returns :
    * <table border=0>
    * <tr>
    * <th>&lt; 0</th>
    * <td>:</td>
    * <td>If this position is before given one</td>
    * </tr>
    * <tr>
    * <th>0</th>
    * <td>:</td>
    * <td>If this position and given one have the same place</td>
    * </tr>
    * <tr>
    * <th>&gt; 0</th>
    * <td>:</td>
    * <td>If this position is after given one</td>
    * </tr>
    * </table>
    * <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param objectPosition
    *           Position to compare with
    * @return Comparison result
    * @see Comparable#compareTo(Object)
    */
   @Override
   public int compareTo(final ObjectPosition<OBJECT> objectPosition)
   {
      final int width = this.object.getWidth();
      final int height = this.object.getHeight();
      final int w = objectPosition.getObject().getWidth();
      final int h = objectPosition.getObject().getHeight();

      if((width <= w) && (height <= h))
      {
         return 1;
      }

      if((width >= w) && (height >= h))
      {
         return -1;
      }

      final long diff = objectPosition.priority - this.priority;

      if(diff < 0)
      {
         return -1;
      }

      if(diff > 0)
      {
         return 1;
      }

      return 0;
   }

   /**
    * Object itself
    * 
    * @return Object itself
    */
   public OBJECT getObject()
   {
      return this.object;
   }

   /**
    * X position of up left corner of object inside {@link Bag2D}
    * 
    * @return X position of up left corner of object inside {@link Bag2D}
    */
   public int getX()
   {
      return this.x;
   }

   /**
    * Y position of up left corner of object inside {@link Bag2D}
    * 
    * @return Y position of up left corner of object inside {@link Bag2D}
    */
   public int getY()
   {
      return this.y;
   }

   /**
    * Position string representation <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Position string representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return this.index + ":" + this.object;
   }
}