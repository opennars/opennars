package jhelp.util.math.complex;

import jhelp.util.math.UtilMath;

/**
 * Represents a complex in its both form : <b>a + b i</b> and <b>r e<sup>i &theta;</sup></b>
 * 
 * @author JHelp
 */
public class Complex
{
   /** i */
   public static final Complex I         = new Complex(0, 1, UtilMath.PI_2, 1);
   /** -i */
   public static final Complex MINUS_I   = new Complex(0, -1, 3 * UtilMath.PI_2, 1);
   /** -1 */
   public static final Complex MINUS_ONE = new Complex(-1, 0, Math.PI, 1);
   /** 1 */
   public static final Complex ONE       = new Complex(1, 0, 0, 1);
   /** 0 */
   public static final Complex ZERO      = new Complex(0, 0, 0, 0);

   /**
    * Add 2 complex
    * 
    * @param complex1
    *           Complex 1
    * @param complex2
    *           Complex 2
    * @return Complex result
    */
   public static Complex add(final Complex complex1, final Complex complex2)
   {
      if(complex1.isNul() == true)
      {
         return complex2;
      }

      if(complex2.isNul() == true)
      {
         return complex1;
      }

      return Complex.createComplexRealImaginary(complex1.real + complex2.real, complex1.imaginary + complex2.imaginary);
   }

   /**
    * Create complex with the form <b>r e<sup>i &theta;</sup></b>
    * 
    * @param length
    *           r
    * @param angle
    *           &theta;
    * @return Created complex
    */
   public static Complex createComplexLengthAngle(final double length, double angle)
   {
      if(UtilMath.isNul(length) == true)
      {
         return Complex.ZERO;
      }

      angle = UtilMath.modulo(angle, UtilMath.TWO_PI);

      final Complex result = new Complex(length * Math.cos(angle), length * Math.sin(angle), angle, length);

      if(Complex.ZERO.equals(result) == true)
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(result) == true)
      {
         return Complex.ONE;
      }

      if(Complex.MINUS_ONE.equals(result) == true)
      {
         return Complex.MINUS_ONE;
      }

      if(Complex.I.equals(result) == true)
      {
         return Complex.I;
      }

      if(Complex.MINUS_I.equals(result) == true)
      {
         return Complex.MINUS_I;
      }

      return result;
   }

   /**
    * Create complex with the form <b>a + b i</b>
    * 
    * @param real
    *           a
    * @param imaginary
    *           b
    * @return Created complex
    */
   public static Complex createComplexRealImaginary(final double real, final double imaginary)
   {
      final double length = Math.sqrt((real * real) + (imaginary * imaginary));

      if(UtilMath.isNul(length) == true)
      {
         return Complex.ZERO;
      }

      final Complex result = new Complex(real, imaginary, UtilMath.modulo(Math.atan2(imaginary / length, real / length), UtilMath.TWO_PI), length);

      if(Complex.ZERO.equals(result) == true)
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(result) == true)
      {
         return Complex.ONE;
      }

      if(Complex.MINUS_ONE.equals(result) == true)
      {
         return Complex.MINUS_ONE;
      }

      if(Complex.I.equals(result) == true)
      {
         return Complex.I;
      }

      if(Complex.MINUS_I.equals(result) == true)
      {
         return Complex.MINUS_I;
      }

      return result;
   }

   /**
    * Dive 2 complex
    * 
    * @param complex1
    *           Numerator
    * @param complex2
    *           Denominator
    * @return Result
    */
   public static Complex divide(final Complex complex1, final Complex complex2)
   {
      if(complex2.isNul() == true)
      {
         throw new IllegalArgumentException("Can't divide by zero");
      }

      if(complex1.isNul() == true)
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(complex1) == true)
      {
         return complex2.invert();
      }

      if(Complex.MINUS_ONE.equals(complex1) == true)
      {
         return complex2.invert().opposite();
      }

      if(Complex.ONE.equals(complex2) == true)
      {
         return complex1;
      }

      if(Complex.MINUS_ONE.equals(complex2) == true)
      {
         return complex1.opposite();
      }

      if(complex1.equals(complex2) == true)
      {
         return Complex.ONE;
      }

      return Complex.createComplexLengthAngle(complex1.length / complex2.length, complex1.angle - complex2.angle);
   }

   /**
    * Multiply 2 complex
    * 
    * @param complex1
    *           Complex 1
    * @param complex2
    *           Complex 2
    * @return Result
    */
   public static Complex multiply(final Complex complex1, final Complex complex2)
   {
      if((complex1.isNul() == true) || (complex2.isNul() == true))
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(complex1) == true)
      {
         return complex2;
      }

      if(Complex.MINUS_ONE.equals(complex1) == true)
      {
         return complex2.opposite();
      }

      if(Complex.ONE.equals(complex2) == true)
      {
         return complex1;
      }

      if(Complex.MINUS_ONE.equals(complex2) == true)
      {
         return complex1.opposite();
      }

      return Complex.createComplexLengthAngle(complex1.length * complex2.length, complex1.angle + complex2.angle);
   }

   /**
    * Subtract 2 complex
    * 
    * @param complex1
    *           Complex 1
    * @param complex2
    *           Complex 2
    * @return Result
    */
   public static Complex subtract(final Complex complex1, final Complex complex2)
   {
      if(complex1.isNul() == true)
      {
         if(complex2.isNul() == true)
         {
            return Complex.ZERO;
         }
         else
         {
            return Complex.createComplexRealImaginary(-complex2.real, -complex2.imaginary);
         }
      }

      if(complex2.isNul() == true)
      {
         return complex1;
      }

      if(complex1.equals(complex2) == true)
      {
         return Complex.ZERO;
      }

      return Complex.createComplexRealImaginary(complex1.real - complex2.real, complex1.imaginary - complex2.imaginary);
   }

   /** Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b> */
   private final double angle;
   /** Complex imaginary part : b in <b>a + i b</b> */
   private final double imaginary;
   /** Complex length : r in <b>r e<sup>i &theta;</sup></b> */
   private final double length;
   /** Complex real part : a in <b>a + i b</b> */
   private final double real;

   /**
    * Create a new instance of Complex
    * 
    * @param real
    *           Complex real part : a in <b>a + i b</b>
    * @param imaginary
    *           Complex imaginary part : b in <b>a + i b </b>
    * @param angle
    *           Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b>
    * @param length
    *           Complex length : r in <b>r e<sup>i &theta;</sup></b>
    */
   private Complex(final double real, final double imaginary, final double angle, final double length)
   {
      this.real = real;
      this.imaginary = imaginary;
      this.angle = angle;
      this.length = length;
   }

   /**
    * Add this complex to an other one
    * 
    * @param complex
    *           Complex to add
    * @return Result
    */
   public Complex add(final Complex complex)
   {
      return Complex.add(this, complex);
   }

   /**
    * Complex complementary. C(a + i b) = a - i b
    * 
    * @return Complex complementary
    */
   public Complex complementary()
   {
      if(this.isNul() == true)
      {
         return Complex.ZERO;
      }

      if(this.isReal() == true)
      {
         return this;
      }

      return Complex.createComplexRealImaginary(this.real, -this.imaginary);
   }

   /**
    * Divide with an other complex
    * 
    * @param complex
    *           Complex to divide with
    * @return Result
    */
   public Complex divide(final Complex complex)
   {
      return Complex.divide(this, complex);
   }

   /**
    * Indicates if an object is equals to this complex <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param obj
    *           Object to compare with
    * @return {@code true} in equality
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if(this == obj)
      {
         return true;
      }

      if(obj == null)
      {
         return false;
      }

      if(this.getClass() != obj.getClass())
      {
         return false;
      }

      final Complex other = (Complex) obj;

      return ((UtilMath.equals(this.real, other.real) == true) && (UtilMath.equals(this.imaginary, other.imaginary) == true))
            || ((UtilMath.equals(this.length, other.length) == true) && (UtilMath.equals(this.angle, other.angle) == true));
   }

   /**
    * Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b>
    * 
    * @return Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b>
    */
   public double getAngle()
   {
      return this.angle;
   }

   /**
    * Complex imaginary part : b in <b>a + i b</b>
    * 
    * @return Complex imaginary part : b in <b>a + i b</b>
    */
   public double getImaginary()
   {
      return this.imaginary;
   }

   /**
    * Complex length : r in <b>r e<sup>i &theta;</sup></b>
    * 
    * @return Complex length : r in <b>r e<sup>i &theta;</sup></b>
    */
   public double getLength()
   {
      return this.length;
   }

   /**
    * Complex real part : a in <b>a + i b</b>
    * 
    * @return Complex real part : a in <b>a + i b</b>
    */
   public double getReal()
   {
      return this.real;
   }

   /**
    * Complex hash code <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Complex hash code
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(this.angle);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.imaginary);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.length);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.real);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      return result;
   }

   /**
    * Complex invert
    * 
    * @return Complex invert
    */
   public Complex invert()
   {
      if(this.isNul() == true)
      {
         throw new IllegalStateException("Can't invert a nul complex");
      }

      return Complex.createComplexLengthAngle(1 / this.length, -this.angle);
   }

   /**
    * Indicates if the complex is imaginary pure. That is to say the real part is 0
    * 
    * @return {@code true} if the complex is imaginary pure.
    */
   public boolean isImaginaryPure()
   {
      return UtilMath.isNul(this.real);
   }

   /**
    * Indicates if the complex is zero
    * 
    * @return {@code true} if the complex is zero
    */
   public boolean isNul()
   {
      return UtilMath.isNul(this.length);
   }

   /**
    * Indicates if the complex is a real. That is to say, the imaginary is 0
    * 
    * @return {@code true} if the complex is a real.
    */
   public boolean isReal()
   {
      return UtilMath.isNul(this.imaginary);
   }

   /**
    * Multiply the complex by an other one
    * 
    * @param complex
    *           Complex to multiply with
    * @return Result
    */
   public Complex multiply(final Complex complex)
   {
      return Complex.multiply(this, complex);
   }

   /**
    * Complex opposite
    * 
    * @return Complex opposite
    */
   public Complex opposite()
   {
      if(this.isNul() == true)
      {
         return Complex.ZERO;
      }

      return Complex.createComplexRealImaginary(-this.real, -this.imaginary);
   }

   /**
    * Subtract with an other complex
    * 
    * @param complex
    *           Complex to subtract
    * @return Result
    */
   public Complex subtract(final Complex complex)
   {
      return Complex.subtract(this, complex);
   }
}