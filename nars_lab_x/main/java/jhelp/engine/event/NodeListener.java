/**
 */
package jhelp.engine.event;

import jhelp.engine.Node;

/**
 * Node event's listener <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public interface NodeListener
{
   /**
    * Call when mouse enter on a node
    * 
    * @param node
    *           Node enter
    */
   public void mouseEnter(Node node);

   /**
    * Call when mouse exit on a node
    * 
    * @param node
    *           Node exit
    */
   public void mouseExit(Node node);

   /**
    * Call when mouse click on a node
    * 
    * @param node
    *           Node click
    * @param leftButton
    *           Indicates if the left button is down
    * @param rightButton
    *           Indicates if the right button is down
    */
   public void mouseClick(Node node, boolean leftButton, boolean rightButton);
}