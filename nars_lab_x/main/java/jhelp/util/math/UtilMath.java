package jhelp.util.math;

import jhelp.util.list.ArrayInt;

/**
 * Utilities for math.<br>
 * It complete the {@link Math} class
 * 
 * @author JHelp
 */
public final class UtilMath
{
   /** Epsilon */
   public static final double EPSILON = UtilMath.max(Double.MIN_NORMAL, Math.abs(Math.E - Math.exp(1)), Math.abs(Math.PI - Math.acos(-1)));
   /** PI / 2 */
   public static double       PI_2    = Math.PI / 2;
   /** 2 * PI */
   public static double       TWO_PI  = Math.PI * 2;

   /**
    * Compute the Bernouilli value
    * 
    * @param n
    *           Number of elements
    * @param m
    *           Total of elements
    * @param t
    *           Factor in [0, 1]
    * @return Bernouilli value
    */
   public static double Bernouilli(final int n, final int m, final double t)
   {
      return UtilMath.C(n, m) * Math.pow(t, n) * Math.pow(1d - t, m - n);
   }

   /**
    * Compute the combination of N elements in M
    * 
    * @param n
    *           Number of elements
    * @param m
    *           Total of elements
    * @return The combination of N elements in M
    */
   public static long C(final int n, final int m)
   {
      if((n <= 0) || (m <= 0) || (n >= m))
      {
         return 1;
      }

      final int diff = m - n;
      final int min = Math.min(n, diff);
      final int max = Math.max(n, diff);

      final ArrayInt arrayInt = new ArrayInt();
      for(int i = m; i > max; i--)
      {
         arrayInt.add(i);
      }

      int size = arrayInt.getSize();
      int test;
      int num, gcd;
      long result = 1;

      for(int i = min; i >= 2; i--)
      {
         num = i;

         for(int j = 0; (j < size) && (num > 1); j++)
         {
            test = arrayInt.getInteger(j);
            gcd = UtilMath.greaterCommonDivisor(num, test);

            if(gcd > 1)
            {
               test /= gcd;

               if(test == 1)
               {
                  arrayInt.remove(j);
                  size--;
                  j--;
               }
               else
               {
                  arrayInt.setInteger(j, test);
               }

               num /= gcd;
            }
         }
      }

      for(int i = 0; i < size; i++)
      {
         result *= arrayInt.getInteger(i);
      }

      return result;
   }

   /**
    * Indicates if 2 double are equal
    * 
    * @param real1
    *           First double
    * @param real2
    *           Second double
    * @return {@code true} if equals
    */
   public static boolean equals(final double real1, final double real2)
   {
      return Math.abs(real1 - real2) <= UtilMath.EPSILON;
   }

   /**
    * Compute the factorial of an integer
    * 
    * @param integer
    *           Integer to have is factorial
    * @return integer!
    */
   public static long factorial(int integer)
   {
      if(integer < 1)
      {
         return 0;
      }

      if(integer < 3)
      {
         return integer;
      }

      long factorial = integer;

      integer--;

      while(integer > 1)
      {
         factorial *= integer;

         integer--;
      }

      return factorial;
   }

   /**
    * Compute the Greater Common Divisor of two integers.<br>
    * If both integers are 0, 0 is return
    * 
    * @param integer1
    *           First integer
    * @param integer2
    *           Second integer
    * @return GCD
    */
   public static int greaterCommonDivisor(int integer1, int integer2)
   {
      integer1 = Math.abs(integer1);
      integer2 = Math.abs(integer2);

      int min = Math.min(integer1, integer2);
      int max = Math.max(integer1, integer2);

      int temp;

      while(min > 0)
      {
         temp = min;

         min = max % min;
         max = temp;
      }

      return max;
   }

   /**
    * Compute the Greater Common Divisor of two integers.<br>
    * If both integers are 0, 0 is return
    * 
    * @param integer1
    *           First integer
    * @param integer2
    *           Second integer
    * @return GCD
    */
   public static long greaterCommonDivisor(long integer1, long integer2)
   {
      integer1 = Math.abs(integer1);
      integer2 = Math.abs(integer2);

      long min = Math.min(integer1, integer2);
      long max = Math.max(integer1, integer2);

      long temp;

      while(min > 0)
      {
         temp = min;

         min = max % min;
         max = temp;
      }

      return max;
   }

   /**
    * Compute exponential interpolation.<br>
    * f : [0, 1] -> [0, 1]<br>
    * f(0)=0<br>
    * f(1)=1<br>
    * f is strictly increase
    * 
    * @param t
    *           Value to interpolate in [0, 1]
    * @return Interpolated result in [0, 1]
    */
   public static double interpolationExponential(final double t)
   {
      return Math.expm1(t) / (Math.E - 1d);
   }

   /**
    * Compute logarithm interpolation.<br>
    * f : [0, 1] -> [0, 1]<br>
    * f(0)=0<br>
    * f(1)=1<br>
    * f is strictly increase
    * 
    * @param t
    *           Value to interpolate in [0, 1]
    * @return Interpolated result in [0, 1]
    */
   public static double interpolationLogarithm(final double t)
   {
      return Math.log1p(t) / Math.log(2d);
   }

   /**
    * Compute sinus interpolation.<br>
    * f : [0, 1] -> [0, 1]<br>
    * f(0)=0<br>
    * f(1)=1<br>
    * f is strictly increase
    * 
    * @param t
    *           Value to interpolate in [0, 1]
    * @return Interpolated result in [0, 1]
    */
   public static double interpolationSinus(final double t)
   {
      return 0.5d + (Math.sin((t * Math.PI) - UtilMath.PI_2) / 2d);
   }

   /**
    * Indicates if a double is zero
    * 
    * @param real
    *           Double to test
    * @return {@code true} if zero
    */
   public static boolean isNul(final double real)
   {
      return Math.abs(real) <= UtilMath.EPSILON;
   }

   /**
    * Return the given integer, if the integer is in [0, 255]. If integer<0, we return 0, if integer>255, we return 255
    * 
    * @param integer
    *           Integer to limit in [0, 255]
    * @return Limited integer
    */
   public static int limit0_255(final int integer)
   {
      return integer <= 0
            ? 0
            : (integer >= 255
                  ? 255
                  : integer);
   }

   /**
    * Compute the Lower Common Multiple of two integers.<br>
    * If both integers are 0, 0 is return
    * 
    * @param integer1
    *           First integer
    * @param integer2
    *           Second integer
    * @return LCM
    */
   public static int lowerCommonMultiple(final int integer1, final int integer2)
   {
      final int gcd = UtilMath.greaterCommonDivisor(integer1, integer2);

      if(gcd == 0)
      {
         return 0;
      }

      return integer1 * (integer2 / gcd);
   }

   /**
    * Compute the Lower Common Multiple of two integers.<br>
    * If both integers are 0, 0 is return
    * 
    * @param integer1
    *           First integer
    * @param integer2
    *           Second integer
    * @return LCM
    */
   public static long lowerCommonMultiple(final long integer1, final long integer2)
   {
      final long gcd = UtilMath.greaterCommonDivisor(integer1, integer2);

      if(gcd == 0)
      {
         return 0;
      }

      return integer1 * (integer2 / gcd);
   }

   /**
    * Maximum of several double
    * 
    * @param doubles
    *           Doubles to have the maximum
    * @return Maximum of doubles
    */
   public static final double max(final double... doubles)
   {
      double max = doubles[0];

      for(final double real : doubles)
      {
         max = real > max
               ? real
               : max;
      }

      return max;
   }

   /**
    * Maximum of several integers
    * 
    * @param integers
    *           Integer to have the maximum
    * @return Maximum of integers
    */
   public static final int maxIntegers(final int... integers)
   {
      int max = integers[0];

      for(final int integer : integers)
      {
         max = integer > max
               ? integer
               : max;
      }

      return max;
   }

   /**
    * Minimum of several double
    * 
    * @param doubles
    *           Doubles to have the minimum
    * @return Minimum of doubles
    */
   public static final double min(final double... doubles)
   {
      double min = doubles[0];

      for(final double real : doubles)
      {
         min = real < min
               ? real
               : min;
      }

      return min;
   }

   /**
    * Minimum of several integers
    * 
    * @param integers
    *           Integer to have the minimum
    * @return Minimum of integers
    */
   public static final int minIntegers(final int... integers)
   {
      int min = integers[0];

      for(final int integer : integers)
      {
         min = integer < min
               ? integer
               : min;
      }

      return min;
   }

   /**
    * Compute the modulo of a real
    * 
    * @param real
    *           Real to modulate
    * @param modulo
    *           Modulo to use
    * @return Result
    */
   public static double modulo(final double real, final double modulo)
   {
      return UtilMath.moduloInterval(real, 0, modulo);
   }

   /**
    * Mathematical modulo.<br>
    * For computer -1 modulo 2 is -1, but in Mathematic -1[2]=1 (-1[2] : -1 modulo 2)
    * 
    * @param integer
    *           Integer to modulate
    * @param modulo
    *           Modulo to apply
    * @return Mathematical modulo : <code>integer[modulo]</code>
    */
   public static int modulo(int integer, final int modulo)
   {
      integer %= modulo;

      if(((integer < 0) && (modulo > 0)) || ((integer > 0) && (modulo < 0)))
      {
         integer += modulo;
      }

      return integer;
   }

   /**
    * Mathematical modulo.<br>
    * For computer -1 modulo 2 is -1, but in Mathematic -1[2]=1 (-1[2] : -1 modulo 2)
    * 
    * @param integer
    *           Integer to modulate
    * @param modulo
    *           Modulo to apply
    * @return Mathematical modulo : <code>integer[modulo]</code>
    */
   public static long modulo(long integer, final long modulo)
   {
      integer %= modulo;

      if(((integer < 0) && (modulo > 0)) || ((integer > 0) && (modulo < 0)))
      {
         integer += modulo;
      }

      return integer;
   }

   /**
    * Modulate a real inside an interval
    * 
    * @param real
    *           Real to modulate
    * @param min
    *           Minimum of interval
    * @param max
    *           Maximum of interval
    * @return Modulated value
    */
   public static double moduloInterval(double real, double min, double max)
   {
      if(min > max)
      {
         final double temp = min;
         min = max;
         max = temp;
      }

      final double space = max - min;

      if(UtilMath.isNul(space) == true)
      {
         throw new IllegalArgumentException("Can't take modulo in empty interval");
      }

      while(real < min)
      {
         real += space;
      }

      while(real > max)
      {
         real -= space;
      }

      return real;
   }

   /**
    * Compute the cubic interpolation
    * 
    * @param cp
    *           Start value
    * @param p1
    *           First control point
    * @param p2
    *           Second control point
    * @param p3
    *           Third control point
    * @param t
    *           Factor in [0, 1]
    * @return Interpolation
    */
   public static double PCubique(final double cp, final double p1, final double p2, final double p3, final double t)
   {
      final double u = 1d - t;
      return (u * u * u * cp) + (3d * t * u * u * p1) + (3d * t * t * u * p2) + (t * t * t * p3);
   }

   /**
    * Compute several cubic interpolation
    * 
    * @param cp
    *           Start value
    * @param p1
    *           First control point
    * @param p2
    *           Second control point
    * @param p3
    *           Third control point
    * @param precision
    *           Number of interpolation
    * @param cub
    *           Where write interpolations
    * @return Interpolations
    */
   public static double[] PCubiques(final double cp, final double p1, final double p2, final double p3, final int precision, double[] cub)
   {
      double step;
      double actual;
      int i;

      if((cub == null) || (cub.length < precision))
      {
         cub = new double[precision];
      }

      step = 1.0 / (precision - 1.0);
      actual = 0;
      for(i = 0; i < precision; i++)
      {
         if(i == (precision - 1))
         {
            actual = 1.0;
         }
         cub[i] = UtilMath.PCubique(cp, p1, p2, p3, actual);
         actual += step;
      }
      return cub;
   }

   /**
    * Power of integer, more fast than {@link Math#pow(double, double)} for some case.<br>
    * integer<sup>pow</sup>
    * 
    * @param integer
    *           Integer to power of
    * @param pow
    *           Power to put
    * @return The result
    */
   public static long pow(final long integer, final long pow)
   {
      if(pow < 0)
      {
         throw new IllegalArgumentException("pow must be >=0, not " + pow);
      }

      if(pow == 0)
      {
         return 1;
      }

      if(pow == 1)
      {
         return integer;
      }

      if(pow == 2)
      {
         return integer * integer;
      }

      if(pow == 3)
      {
         return integer * integer * integer;
      }

      final long result = UtilMath.pow(integer, pow >> 1);

      if((pow & 1) == 0)
      {
         return result * result;
      }

      return integer * result * result;
   }

   /**
    * Compute the quadric interpolation
    * 
    * @param cp
    *           Start value
    * @param p1
    *           First control point
    * @param p2
    *           Second control point
    * @param t
    *           Factor in [0, 1]
    * @return Interpolation
    */
   public static double PQuadrique(final double cp, final double p1, final double p2, final double t)
   {
      final double u = 1d - t;
      return (u * u * cp) + (2d * t * u * p1) + (t * t * p2);
   }

   /**
    * Compute several quadric interpolation
    * 
    * @param cp
    *           Start value
    * @param p1
    *           First control point
    * @param p2
    *           Second control point
    * @param precision
    *           Number of interpolation
    * @param quad
    *           Where write interpolations
    * @return Interpolations
    */
   public static double[] PQuadriques(final double cp, final double p1, final double p2, final int precision, double[] quad)
   {
      double step;
      double actual;
      int i;

      if((quad == null) || (quad.length < precision))
      {
         quad = new double[precision];
      }

      step = 1.0 / (precision - 1.0);
      actual = 0;
      for(i = 0; i < precision; i++)
      {
         if(i == (precision - 1))
         {
            actual = 1.0;
         }
         quad[i] = UtilMath.PQuadrique(cp, p1, p2, actual);
         actual += step;
      }
      return quad;
   }

   /**
    * Sign of a double.<br>
    * The answer is like follow table:
    * <table border=1>
    * <tr>
    * <td><b><center>Double is</center></b></td>
    * <td><b><center>Then return</center></b></td>
    * </tr>
    * <tr>
    * <td><b><center>&lt; 0</center></b></td>
    * <td><b><center>-1</center></b></td>
    * </tr>
    * <tr>
    * <td><b><center>= 0</center></b></td>
    * <td><b><center>0</center></b></td>
    * </tr>
    * <tr>
    * <td><b><center>&gt; 0</center></b></td>
    * <td><b><center>1</center></b></td>
    * </tr>
    * </table>
    * 
    * @param real
    *           Double to have its sign
    * @return Double sign (-1, 0 or 1)
    */
   public static int sign(final double real)
   {
      if(UtilMath.isNul(real) == true)
      {
         return 0;
      }

      if(real < 0)
      {
         return -1;
      }

      return 1;
   }

   /**
    * Sign of an integer.<br>
    * It returns
    * <table>
    * <tr>
    * <th>-1</th>
    * <td>If integer is <0</td>
    * </tr>
    * <tr>
    * <th>0</th>
    * <td>If integer is 0</td>
    * </tr>
    * <tr>
    * <th>1</th>
    * <td>If integer is >0</td>
    * </tr>
    * </table>
    * 
    * @param integer
    *           Integer to have sign
    * @return Integer sign
    */
   public static final int sign(final int integer)
   {
      if(integer > 0)
      {
         return 1;
      }

      if(integer < 0)
      {
         return -1;
      }

      return 0;
   }

   /**
    * Sign of an integer.<br>
    * The answer is like follow table:
    * <table border=1>
    * <tr>
    * <td><b><center>Integer is</center></b></td>
    * <td><b><center>Then return</center></b></td>
    * </tr>
    * <tr>
    * <td><b><center>&lt; 0</center></b></td>
    * <td><b><center>-1</center></b></td>
    * </tr>
    * <tr>
    * <td><b><center>= 0</center></b></td>
    * <td><b><center>0</center></b></td>
    * </tr>
    * <tr>
    * <td><b><center>&gt; 0</center></b></td>
    * <td><b><center>1</center></b></td>
    * </tr>
    * </table>
    * 
    * @param integer
    *           Integer to have its sign
    * @return Integer sign (-1, 0 or 1)
    */
   public static int sign(final long integer)
   {
      if(integer == 0)
      {
         return 0;
      }
      if(integer > 0)
      {
         return 1;
      }
      return -1;
   }

   /**
    * To avoid instance
    */
   private UtilMath()
   {
   }

   public static int limit(int v, int min, int max) {
      if (v < min) v = min;
      if (v > max) v = max;
      return v;
   }
}