package jhelp.util.list;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.text.UtilText;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A bag in two dimension.<br>
 * The bag is a table with a fixed number of cell in width and in height.<br>
 * It can contains objects that have a size (width and height) that occupied cells of the bag.<br>
 * Two objects can't itersect each other,.<br>
 * Each time an object is add, free space are use to put it, if not enough room for put the object, then the bag will be
 * reorderes=d automatically to try to add the new object, if its not possible to add the new object, it is not added
 * 
 * @author JHelp
 * @param <OBJECT>
 *           Objects type
 */
public class Bag2D<OBJECT extends SizedObject>
      implements Iterable<ObjectPosition<OBJECT>>
{
   /**
    * Area in the bag
    * 
    * @author JHelp
    */
   class Area
   {
      /** Area priority */
      long priority;
      /** X top-left corner */
      int  x;
      /** Y top-left corner */
      int  y;

      /**
       * Create a new instance of Area
       * 
       * @param x
       *           X top-left corner
       * @param y
       *           Y top-left corner
       * @param width
       *           Width
       * @param height
       *           Height
       */
      Area(final int x, final int y, final int width, final int height)
      {
         this.x = x;
         this.y = y;

         final int h = Bag2D.this.height;
         final int w = Bag2D.this.width;

         for(int yy = (y + height) - 1; yy >= y; yy--)
         {
            for(int xx = (x + width) - 1; xx >= x; xx--)
            {
               Bag2D.this.bag[xx][yy] = -1;
            }
         }

         this.priority = 0;
         long ww;
         long hh;
         int mx;
         boolean eliminateLine;
         for(int yy = 0; yy < h; yy++)
         {
            for(int xx = 0; xx < w; xx++)
            {
               if(Bag2D.this.bag[xx][yy] == 0)
               {
                  ww = hh = 1;
                  Bag2D.this.bag[xx][yy] = -1;

                  for(mx = xx + 1; (mx < w) && (Bag2D.this.bag[mx][yy] == 0); mx++)
                  {
                     ww++;
                     Bag2D.this.bag[mx][yy] = -1;
                  }

                  eliminateLine = true;
                  for(int yyy = yy + 1; (yyy < h) && (eliminateLine == true); yyy++)
                  {
                     for(int xxx = xx; xxx < mx; xxx++)
                     {
                        if(Bag2D.this.bag[xxx][yyy] != 0)
                        {
                           eliminateLine = false;
                           break;
                        }
                     }

                     if(eliminateLine == true)
                     {
                        for(int xxx = xx; xxx < mx; xxx++)
                        {
                           Bag2D.this.bag[xxx][yyy] = -1;
                        }
                        hh++;
                     }
                  }

                  this.priority += ww * hh * ww * hh;
               }
            }
         }

         for(int yy = 0; yy < h; yy++)
         {
            for(int xx = 0; xx < w; xx++)
            {
               if(Bag2D.this.bag[xx][yy] < 0)
               {
                  Bag2D.this.bag[xx][yy] = 0;
               }
            }
         }
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
         return this.x + "," + this.y + " : " + this.priority;
      }
   }

   /** Objects stored */
   private final SortedArray<ObjectPosition<OBJECT>> objects;
   /** The bag */
   final int[][]                                     bag;
   /** Bag height */
   final int                                         height;
   /** Bag width */
   final int                                         width;

   /**
    * Create a new instance of Bag2D
    * 
    * @param width
    *           Bag width
    * @param height
    *           Bag height
    */
   @SuppressWarnings(
   {
         "unchecked", "rawtypes"
   })
   public Bag2D(final int width, final int height)
   {
      this.width = Math.max(1, width);
      this.height = Math.max(1, height);

      this.bag = new int[this.width][this.height];
      this.objects = new SortedArray(ObjectPosition.class);
   }

   /**
    * Compute the best location of free space to put an object
    * 
    * @param w
    *           Object width
    * @param h
    *           Object height
    * @return The bet location or {@code null} if not enough room for put the object (Mat have to reorder the bag before try
    *         again)
    */
   private Point searchBestFreeSpace(final int w, final int h)
   {
      Area area = null;
      Area a;

      final int maxX = this.width - w;
      final int maxY = this.height - h;

      boolean found;
      for(int y = 0; y <= maxY; y++)
      {
         for(int x = 0; x <= maxX; x++)
         {
            if(this.bag[x][y] == 0)
            {
               found = true;

               for(int yy = (y + h) - 1; (yy >= y) && (found == true); yy--)
               {
                  for(int xx = (x + w) - 1; xx >= x; xx--)
                  {
                     if(this.bag[xx][yy] != 0)
                     {
                        found = false;
                        break;
                     }
                  }
               }

               if(found == true)
               {
                  a = new Area(x, y, w, h);

                  if((area == null) || (area.priority < a.priority))
                  {
                     area = a;
                  }
               }
            }
         }
      }

      if(area == null)
      {
         return null;
      }

      return new Point(area.x, area.y);
   }

   /**
    * Clear the bag. The bag is empty after this
    */
   public void clearBag()
   {
      for(int y = 0; y < this.height; y++)
      {
         for(int x = 0; x < this.width; x++)
         {
            this.bag[x][y] = 0;
         }
      }

      this.objects.clear();
   }

   /**
    * Obtain the object coresponding to the index
    * 
    * @param index
    *           Index
    * @return Corresponding object
    */
   public OBJECT getObject(final int index)
   {
      return this.getObjectPosition(index).getObject();
   }

   /**
    * Obtain object position (That embed the object it self) at given cell
    * 
    * @param x
    *           Cell x
    * @param y
    *           Cell y
    * @return Object position or {@code null} if cell is empty
    */
   public ObjectPosition<OBJECT> getObjectAt(final int x, final int y)
   {
      if((x < 0) || (x >= this.width) || (y < 0) || (y >= this.height))
      {
         throw new IllegalArgumentException("The point (" + x + ", " + y + ") is outside of the bag : " + this.width + "x" + this.height);
      }

      final int index = this.bag[x][y];
      if(index == 0)
      {
         return null;
      }

      for(final ObjectPosition<OBJECT> objectPosition : this.objects)
      {
         if(objectPosition.index == index)
         {
            return objectPosition;
         }
      }

      Debug.println(DebugLevel.WARNING, "Shouldn't arrive here ! x=", x, " y=", y, " bag[x][y]=", this.bag[x][y], "\n", this);

      this.bag[x][y] = 0;
      return null;
   }

   /**
    * Obtain position (That embed the object it self) the object coresponding to the index
    * 
    * @param index
    *           Index
    * @return Corresponding object position
    */
   public ObjectPosition<OBJECT> getObjectPosition(final int index)
   {
      return this.objects.getElement(index);
   }

   /**
    * Index of object inside bag
    * 
    * @param object
    *           Searched object
    * @return Index object or -1 if not found
    */
   public int indexOf(final OBJECT object)
   {
      final int size = this.objects.getSize();

      for(int i = 0; i < size; i++)
      {
         if(this.objects.getElement(i).equals(object) == true)
         {
            return i;
         }
      }

      return -1;
   }

   /**
    * Index of object position inside bag
    * 
    * @param objectPosition
    *           Searched object position
    * @return Index object position or -1 if not found
    */
   public int indexOf(final ObjectPosition<OBJECT> objectPosition)
   {
      return this.objects.indexOf(objectPosition);
   }

   /**
    * List of all objects postion (Embed the objects) inside the bag <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return List of all objects postion (Embed the objects) inside the bag
    * @see Iterable#iterator()
    */
   @Override
   public Iterator<ObjectPosition<OBJECT>> iterator()
   {
      return this.objects.iterator();
   }

   /**
    * Number of objects in the bag
    * 
    * @return Number of objects in the bag
    */
   public int numberOfObjects()
   {
      return this.objects.getSize();
   }

   /**
    * Try to add an object in the bag.<br>
    * The bag is eventually reordered to be able add the object.<br>
    * If it is not possible to add the object (Reorder is not enough) the object is not added and {@code null} is return
    * 
    * @param object
    *           Object to add
    * @return Object position in the bag after loacate it or {@code null} if is impossible to add the object (Reorder is not
    *         enough)
    */
   public ObjectPosition<OBJECT> put(final OBJECT object)
   {
      final int w = object.getWidth();
      final int h = object.getHeight();

      if((w < 1) || (h < 1))
      {
         throw new IllegalArgumentException("Object must have size at least 1x1, not " + w + "x" + h);
      }

      if((w > this.width) || (h > this.height))
      {
         return null;
      }

      Point point = this.searchBestFreeSpace(w, h);
      if(point != null)
      {
         final ObjectPosition<OBJECT> objectPosition = new ObjectPosition<OBJECT>(object, this.width, this.height);
         objectPosition.x = point.x;
         objectPosition.y = point.y;
         final int index = objectPosition.index;

         for(int yy = (point.y + h) - 1; yy >= point.y; yy--)
         {
            for(int xx = (point.x + w) - 1; xx >= point.x; xx--)
            {
               this.bag[xx][yy] = index;
            }
         }

         this.objects.add(objectPosition);
         return objectPosition;
      }

      final int[][] oldBag = new int[this.width][this.height];

      for(int y = 0; y < this.height; y++)
      {
         for(int x = 0; x < this.width; x++)
         {
            oldBag[x][y] = this.bag[x][y];
            this.bag[x][y] = 0;
         }
      }

      final ObjectPosition<OBJECT> objectPos = new ObjectPosition<OBJECT>(object, this.width, this.height);
      this.objects.add(objectPos);
      OBJECT obj;
      int index, ww, hh;
      final ArrayList<Point> positions = new ArrayList<Point>();

      for(final ObjectPosition<OBJECT> objectPosition : this.objects)
      {
         obj = objectPosition.getObject();
         index = objectPosition.index;
         ww = obj.getWidth();
         hh = obj.getHeight();
         point = this.searchBestFreeSpace(ww, hh);

         if(point == null)
         {
            break;
         }

         positions.add(point);
         for(int yy = (point.y + hh) - 1; yy >= point.y; yy--)
         {
            for(int xx = (point.x + ww) - 1; xx >= point.x; xx--)
            {
               this.bag[xx][yy] = index;
            }
         }
      }

      if(point == null)
      {
         for(int y = 0; y < this.height; y++)
         {
            for(int x = 0; x < this.width; x++)
            {
               this.bag[x][y] = oldBag[x][y];
            }
         }

         this.objects.remove(objectPos);
         return null;
      }

      final int nb = this.objects.getSize();
      ObjectPosition<OBJECT> objectPosition;

      for(int i = 0; i < nb; i++)
      {
         point = positions.get(i);
         objectPosition = this.objects.getElement(i);

         objectPosition.x = point.x;
         objectPosition.y = point.y;
      }

      return objectPos;
   }

   /**
    * Remove an object on the bag
    * 
    * @param index
    *           Object index to remove
    */
   public void remove(int index)
   {
      final ObjectPosition<OBJECT> objectPosition = this.objects.remove(index);

      if(objectPosition == null)
      {
         return;
      }

      index = objectPosition.index;

      for(int y = 0; y < this.height; y++)
      {
         for(int x = 0; x < this.width; x++)
         {
            if(this.bag[x][y] == index)
            {
               this.bag[x][y] = 0;
            }
         }
      }
   }

   /**
    * Remove an object of the bag
    * 
    * @param object
    *           Object to remove
    */
   public void remove(final OBJECT object)
   {
      final int index = this.indexOf(object);

      if(index < 0)
      {
         return;
      }

      this.remove(index);
   }

   /**
    * Remove an object of the bag
    * 
    * @param objectPosition
    *           Object position with embed object to remove
    */
   public void remove(ObjectPosition<OBJECT> objectPosition)
   {
      objectPosition = this.objects.remove(objectPosition);

      if(objectPosition == null)
      {
         return;
      }

      final int index = objectPosition.index;

      for(int y = 0; y < this.height; y++)
      {
         for(int x = 0; x < this.width; x++)
         {
            if(this.bag[x][y] == index)
            {
               this.bag[x][y] = 0;
            }
         }
      }
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
      final StringBuilder builder = new StringBuilder(UtilText.concatenate("Bag2D : "));

      builder.append(this.width);
      builder.append("x");
      builder.append(this.height);

      for(int y = 0; y < this.height; y++)
      {
         builder.append('\n');

         for(int x = 0; x < this.width; x++)
         {
            builder.append('\t');
            builder.append(this.bag[x][y]);
         }
      }

      builder.append("\n");
      builder.append(this.objects);

      return builder.toString();
   }
}