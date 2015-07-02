/**
 */
package jhelp.engine.event;

import jhelp.engine.twoD.Object2D;

/**
 * Listener of 2D object'd event <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public interface Object2DListener
{
   /**
    * Call when mouse click on a object
    * 
    * @param object2D
    *           Object under mouse
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param leftButton
    *           Indicates if the left button is down
    * @param rightButton
    *           Indicates if the right button is down
    */
   public void mouseClick(Object2D object2D, int x, int y, boolean leftButton, boolean rightButton);

   /**
    * Call when mouse drag on a object
    * 
    * @param object2D
    *           Object under mouse
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param leftButton
    *           Indicates if the left button is down
    * @param rightButton
    *           Indicates if the right button is down
    */
   public void mouseDrag(Object2D object2D, int x, int y, boolean leftButton, boolean rightButton);

   /**
    * Call when mouse enter on a object
    * 
    * @param object2D
    *           Object enter
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    */
   public void mouseEnter(Object2D object2D, int x, int y);

   /**
    * Call when mouse exit on a object
    * 
    * @param object2D
    *           Object exit
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    */
   public void mouseExit(Object2D object2D, int x, int y);

   /**
    * Call when mouse move on a object
    * 
    * @param object2D
    *           Object under mouse
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    */
   public void mouseMove(Object2D object2D, int x, int y);
}