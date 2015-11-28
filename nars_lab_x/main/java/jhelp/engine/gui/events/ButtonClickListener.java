/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.events<br>
 * Class : ButtonClickListener<br>
 * Date : 5 aoet 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.events;

import jhelp.engine.gui.components.Button;

/**
 * Listener on button click<br>
 * <br>
 * Last modification : 5 aoet 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface ButtonClickListener
{
   /**
    * Call when button clicked
    * 
    * @param button
    *           Button clicked
    */
   public void buttonClick(Button button);
}