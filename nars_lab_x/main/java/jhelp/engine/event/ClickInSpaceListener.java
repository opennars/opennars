package jhelp.engine.event;

import java.util.EventListener;

/**
 * Listener when user click not in 3D object, nor 2D object
 * 
 * @author JHelp
 */
public interface ClickInSpaceListener
      extends EventListener
{
   /**
    * Called when user click not in 3D object, nor 2D object
    * 
    * @param mouseX
    *           Mouse X
    * @param mouseY
    *           Mouse Y
    * @param leftButton
    *           Indicates if left mouse button is down
    * @param rightButton
    *           Indicates if right mouse button is down
    */
   public void clickInSpace(int mouseX, int mouseY, boolean leftButton, boolean rightButton);
}