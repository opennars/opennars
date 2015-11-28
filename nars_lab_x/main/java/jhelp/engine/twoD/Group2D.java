/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.twoD<br>
 * Class : Panel2D<br>
 * Date : 14 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.twoD;

import java.util.ArrayList;

/**
 * Group of objects<br>
 * A group is to translate or change visibility of several 2D objects in same time, but have no graphic representation <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Group2D
{
   /** Developer additional information */
   private Object                    additionalInformation;
   /** Members of the group */
   private final ArrayList<Object2D> members;

   /**
    * Constructs Group2D<br>
    * Create an empty group
    */
   public Group2D()
   {
      this.members = new ArrayList<Object2D>();
   }

   /**
    * Add a member to the group
    * 
    * @param member
    *           Member adds
    */
   public void addMember(final Object2D member)
   {
      if(member == null)
      {
         throw new NullPointerException("The member musn't be null !");
      }

      this.members.add(member);
   }

   /**
    * Actual additionalInformation value
    * 
    * @return Actual additionalInformation value
    */
   public Object getAdditionalInformation()
   {
      return this.additionalInformation;
   }

   /**
    * Change additionalInformation
    * 
    * @param additionalInformation
    *           New additionalInformation value
    */
   public void setAdditionalInformation(final Object additionalInformation)
   {
      this.additionalInformation = additionalInformation;
   }

   /**
    * Change the visibility of all members of the group
    * 
    * @param visible
    *           New visibility
    */
   public void setVisible(final boolean visible)
   {
      for(final Object2D object2D : this.members)
      {
         object2D.setVisible(visible);
      }
   }

   /**
    * Translate all members of the group
    * 
    * @param x
    *           X translation
    * @param y
    *           Y translation
    */
   public void translate(final int x, final int y)
   {
      for(final Object2D object2D : this.members)
      {
         object2D.setX(object2D.getX() + x);
         object2D.setY(object2D.getY() + y);
      }
   }
}