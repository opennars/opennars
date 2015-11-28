/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.events<br>
 * Class : InternalFrameListener<br>
 * Date : 28 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.events;

import jhelp.engine.gui.components.InternalFrame;

/**
 * Listener selection internal frame<br>
 * <br>
 * Last modification : 28 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface InternalFrameListener
{
   /**
    * Call on internal frame selection
    * 
    * @param internalFrame
    *           Internal frame selected
    */
   public void internalFrameSelect(InternalFrame internalFrame);
}