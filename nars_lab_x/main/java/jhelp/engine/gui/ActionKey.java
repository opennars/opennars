package jhelp.engine.gui;

import java.awt.event.KeyEvent;

/**
 * Action for a key
 * 
 * @author JHelp
 */
public enum ActionKey
{
   /** Key for do an "Action" */
   ACTION(KeyEvent.VK_SPACE),
   /** Key for do a "backward" */
   BACKWARD(KeyEvent.VK_PAGE_DOWN),
   /** Key for do a "cancel" */
   CANCEL(KeyEvent.VK_X),
   /** Key for do a "down" */
   DOWN(KeyEvent.VK_DOWN),
   /** Key for do a "duck" */
   DUCK(KeyEvent.VK_CONTROL),
   /** Key for do a "exit" */
   EXIT(KeyEvent.VK_ESCAPE),
   /** Key for do a "forward" */
   FORWARD(KeyEvent.VK_PAGE_UP),
   /** Key for do a "Jump" */
   JUMP(KeyEvent.VK_W),
   /** Key for do a "left" */
   LEFT(KeyEvent.VK_LEFT),
   /** Key for do a "menu" */
   MENU(KeyEvent.VK_M),
   /** Key for do a "right" */
   RIGHT(KeyEvent.VK_RIGHT),
   /** Key for do a "rotate down" */
   ROTATE_DOWN(KeyEvent.VK_S),
   /** Key for do a "rotate left" */
   ROTATE_LEFT(KeyEvent.VK_Q),
   /** Key for do a "rotate right" */
   ROTATE_RIGHT(KeyEvent.VK_D),
   /** Key for do a "rotate up" */
   ROTATE_UP(KeyEvent.VK_Z),
   /** Key for do a "special" */
   SPECIAL(KeyEvent.VK_C),
   /** Key for do a "up" */
   UP(KeyEvent.VK_UP);

   /** Default key code */
   private final int defaultKeyCode;

   /**
    * Create a new instance of ActionKey
    * 
    * @param defaultKeyCode
    *           Default key code
    */
   ActionKey(final int defaultKeyCode)
   {
      this.defaultKeyCode = defaultKeyCode;
   }

   /**
    * Default- key code
    * 
    * @return Default- key code
    */
   public int getDefaultKeyCode()
   {
      return this.defaultKeyCode;
   }
}