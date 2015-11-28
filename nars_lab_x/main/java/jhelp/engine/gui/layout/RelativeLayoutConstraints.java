/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.layout<br>
 * Class : RelativeLayoutConstraints<br>
 * Date : 6 juil. 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Relative layout constraints<br>
 * <br>
 * Last modification : 6 juil. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class RelativeLayoutConstraints
      implements Constraints
{
   /** Constraint mode */
   public enum Mode
   {
      /** Expand mode, the component will take all free space */
      EXPAND,
      /** Warp mode, the component will take the minmum space to print */
      WRAP
   }

   /** Reference to a layout that this layout MUST be above */
   private String                  aboveOf;
   /** Reference to a layout that this layout MUST be bellow */
   private String                  bellowOf;
   /** Mode apply on height */
   private Mode                    height;

   /** Reference to a layout that this layout MUST be at left */
   private String                  leftOf;
   /** Constraints name */
   private final String            name;
   /** Place inside the parent */
   private BorderLayoutConstraints placeInParent;
   /** Reference to a layout that this layout MUST be at right */
   private String                  rightOf;

   /** Mode apply on width */
   private Mode                    width;

   /**
    * Constructs RelativeLayoutConstraints
    * 
    * @param name
    *           Layout name
    * @param width
    *           Width mode
    * @param height
    *           Height mode
    */
   public RelativeLayoutConstraints(final String name, final Mode width, final Mode height)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(width == null)
      {
         throw new NullPointerException("width musn't be null");
      }

      if(height == null)
      {
         throw new NullPointerException("height musn't be null");
      }

      this.name = name;
   }

   /**
    * Indicates if constraints is equals
    * 
    * @param constraints
    *           Constraints compare
    * @return {@code true} if equals
    * @see Constraints#equals(Constraints)
    */
   @Override
   public boolean equals(final Constraints constraints)
   {
      if((constraints == null) || ((constraints instanceof RelativeLayoutConstraints) == false))
      {
         return false;
      }

      return this.name.equals(((RelativeLayoutConstraints) constraints).name);
   }

   /**
    * Return aboveOf
    * 
    * @return aboveOf
    */
   public String getAboveOf()
   {
      return this.aboveOf;
   }

   /**
    * Return bellowOf
    * 
    * @return bellowOf
    */
   public String getBellowOf()
   {
      return this.bellowOf;
   }

   /**
    * Return height
    * 
    * @return height
    */
   public Mode getHeight()
   {
      return this.height;
   }

   /**
    * Return leftOf
    * 
    * @return leftOf
    */
   public String getLeftOf()
   {
      return this.leftOf;
   }

   /**
    * Return name
    * 
    * @return name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Return placeInParent
    * 
    * @return placeInParent
    */
   public BorderLayoutConstraints getPlaceInParent()
   {
      return this.placeInParent;
   }

   /**
    * Return rightOf
    * 
    * @return rightOf
    */
   public String getRightOf()
   {
      return this.rightOf;
   }

   /**
    * Return width
    * 
    * @return width
    */
   public Mode getWidth()
   {
      return this.width;
   }

   /**
    * Hash code
    * 
    * @return Hash code
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return this.name.hashCode();
   }

   /**
    * Modify aboveOf
    * 
    * @param aboveOf
    *           New aboveOf value
    */
   public void setAboveOf(final String aboveOf)
   {
      this.aboveOf = aboveOf;
   }

   /**
    * Modify bellowOf
    * 
    * @param bellowOf
    *           New bellowOf value
    */
   public void setBellowOf(final String bellowOf)
   {
      this.bellowOf = bellowOf;
   }

   /**
    * Modify leftOf
    * 
    * @param leftOf
    *           New leftOf value
    */
   public void setLeftOf(final String leftOf)
   {
      this.leftOf = leftOf;
   }

   /**
    * Modify placeInParent
    * 
    * @param placeInParent
    *           New placeInParent value
    */
   public void setPlaceInParent(final BorderLayoutConstraints placeInParent)
   {
      this.placeInParent = placeInParent;
   }

   /**
    * Modify rightOf
    * 
    * @param rightOf
    *           New rightOf value
    */
   public void setRightOf(final String rightOf)
   {
      this.rightOf = rightOf;
   }
}