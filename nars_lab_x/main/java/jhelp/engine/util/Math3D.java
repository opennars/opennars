/*
 * Cree le 4 oct. 06 Pour cedreo
 */
package jhelp.engine.util;

import jhelp.engine.Point2D;
import jhelp.engine.Point3D;

/**
 * Math for 3D and utilities <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Math3D
{
   /** Power of 2 list */
   private static final int[] powerOf2                            =
                                                                  {
         1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096
                                                                  };
   /** 0 in byte */
   public static final byte   BYTE_0                              = (byte) 0;
   /** 255 in byte */
   public static final byte   BYTE_255                            = (byte) 255;
   /** Theorical maximum of objects */
   public static final int    NUMBER_THEORICAL_MAXIMUM_OF_OBJECTS = (256 * 256 * 256) / Math3D.PICKING_PRECISION;
   /** PI */
   public static final float  PI                                  = (float) (Math.PI);
   /** PI / 2 */
   public static final float  PI_TWO                              = (float) (Math.PI / 2);
   /** Epsilon to say if two color are the same */
   public static final float  PICK_EPSILON                        = 1e-5f;
   /** Difference between tow succeed picking color number */
   public static final int    PICKING_PRECISION                   = 8;
   /** 2 PI */
   public static final float  TWO_PI                              = (float) (2d * Math.PI);

   /**
    * Compute the nearest power of 2 of an integer
    * 
    * @param integer
    *           Integer considered
    * @return Nearest power of 2
    */
   public static int computeNearestPowerOf2(final int integer)
   {
      final int nb = Math3D.powerOf2.length - 1;
      int index = 0;
      while((index < nb) && (integer > Math3D.powerOf2[index]))
      {
         index++;
      }
      return Math3D.powerOf2[index];
   }

   /**
    * Compute the bigger power of 2 lesser or equal to integer in parameter, and return couple made of the LOG2 of the power of
    * 2 number and the power of 2 it self.<br>
    * For example, for 5, the power of 2 is 4=2^2, return (2, 4).<br>
    * Other example 9 => (3, 8) ...
    * 
    * @param integer
    *           Integer look for near power of 2
    * @return The (LOG2, power 2) couple
    */
   public static int[] computePowerOf2couple(final int integer)
   {
      // For integer less or equal than 1, the return couple is (0, 1)
      if(integer <= 1)
      {
         return new int[]
         {
               0, 1
         };
      }

      // If the integer is bigger or equal than the maximum value, the return
      // the maximum
      int end = Math3D.powerOf2.length - 1;
      if(integer >= Math3D.powerOf2[end])
      {
         return new int[]
         {
               end, Math3D.powerOf2[end]
         };
      }

      // Search the value
      int start = 0;
      while((start + 1) < end)
      {
         final int middle = (start + end) >> 1;
         final int pui = Math3D.powerOf2[middle];
         if(pui == integer)
         {
            return new int[]
            {
                  middle, pui
            };
         }
         if(pui > integer)
         {
            end = middle;
         }
         else
         {
            start = middle;
         }
      }

      return new int[]
      {
            start, Math3D.powerOf2[start]
      };
   }

   /**
    * Extract decimal part
    * 
    * @param f
    *           Float to extract
    * @return Decimal part
    */
   public static float decimalPart(final float f)
   {
      return f - (int) f;
   }

   /**
    * Transform degre angle to radian
    * 
    * @param degre
    *           Degre angle
    * @return Radian angle
    */
   public static float degreToRadian(final float degre)
   {
      return (Math3D.TWO_PI * degre) / 360f;
   }

   /**
    * Compute distance between 2 points
    * 
    * @param x1
    *           First point X
    * @param y1
    *           First point Y
    * @param x2
    *           Second point X
    * @param y2
    *           Second point Y
    * @return Distance between them
    */
   public static float distance(final float x1, final float y1, final float x2, final float y2)
   {
      return Math3D.squareRoot(Math3D.square(x1 - x2) + Math3D.square(y1 - y2));
   }

   /**
    * Compute distance between 2 points
    * 
    * @param x1
    *           First point X
    * @param y1
    *           First point Y
    * @param z1
    *           First point Z
    * @param x2
    *           Second point X
    * @param y2
    *           Second point Y
    * @param z2
    *           Second point Z
    * @return Distance between them
    */
   public static float distance(final float x1, final float y1, final float z1, final float x2, final float y2, final float z2)
   {
      return Math3D.squareRoot(Math3D.square(x1 - x2) + Math3D.square(y1 - y2) + Math3D.square(z1 - z2));
   }

   /**
    * Compute distance between 2 points
    * 
    * @param p1
    *           First point
    * @param p2
    *           Second point
    * @return Distance between them
    */
   public static float distance(final Point2D p1, final Point2D p2)
   {
      return Math3D.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }

   /**
    * Compute distance between 2 points
    * 
    * @param p1
    *           First point
    * @param p2
    *           Second point
    * @return Distance between them
    */
   public static float distance(final Point3D p1, final Point3D p2)
   {
      return Math3D.distance(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
   }

   /**
    * Indicates if 2 floats are equals
    * 
    * @param f1
    *           First float
    * @param f2
    *           Second float
    * @return {@code true} if floats are equal
    */
   public static final boolean equal(final float f1, final float f2)
   {
      return Math.abs(f1 - f2) < 1e-5f;
   }

   /**
    * Indicates if 2 floats (in picking precision) are equals
    * 
    * @param f1
    *           First float
    * @param f2
    *           Second float
    * @return {@code true} if floats are equal
    */
   public static final boolean equalPick(final float f1, final float f2)
   {
      return Math.abs(f1 - f2) < Math3D.PICK_EPSILON;
   }

   /**
    * Create a copy of array
    * 
    * @param array
    *           Array to copy
    * @return Copy
    */
   public static final float[] getCopy(final float[] array)
   {
      if(array == null)
      {
         return null;
      }
      final float[] copy = new float[array.length];
      System.arraycopy(array, 0, copy, 0, array.length);
      return copy;
   }

   /**
    * Create a copy of array
    * 
    * @param array
    *           Array to copy
    * @return Copy
    */
   public static final int[] getCopy(final int[] array)
   {
      if(array == null)
      {
         return null;
      }
      final int[] copy = new int[array.length];
      System.arraycopy(array, 0, copy, 0, array.length);
      return copy;
   }

   /**
    * Transform float that progress linear [0, 1] to sinusoidal progression [0, 1]
    * 
    * @param rate
    *           Linear progression
    * @return Sinusoidal progression
    */
   public static float linearToSinusoidal(final float rate)
   {
      return (float) ((Math.sin((rate * Math.PI) - (Math.PI / 2d)) + 1d) / 2d);
   }

   /**
    * Indicates if a float is nul
    * 
    * @param f
    *           Float to test
    * @return {@code true} if the float is nul
    */
   public static final boolean nul(final float f)
   {
      return Math.abs(f) < 1e-5f;
   }

   /**
    * Transform radian angle to degre
    * 
    * @param radian
    *           Radian angle
    * @return Degre angle
    */
   public static float radianToDegre(final float radian)
   {
      return (360f * radian) / Math3D.TWO_PI;
   }

   /**
    * Compute square of a number
    * 
    * @param f
    *           Number to multiply by himself
    * @return The square
    */
   public static double square(final double f)
   {
      return f * f;
   }

   /**
    * Compute square of a number
    * 
    * @param f
    *           Number to multiply by himself
    * @return The square
    */
   public static float square(final float f)
   {
      return f * f;
   }

   /**
    * Compute square root with float precision
    * 
    * @param f
    *           Number to have its suare root
    * @return Suare root
    */
   public static float squareRoot(final float f)
   {
      return (float) Math.sqrt(f);
   }
}