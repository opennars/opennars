/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.events<br>
 * Class : Frame3DViewExitListener<br>
 * Date : 28 mars 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.events;

import jhelp.engine.gui.FrameView3D;

/**
 * Frame 3D view exit listener <br>
 * <br>
 * Last modification : 28 mars 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface Frame3DViewListener
{
   /**
    * Call before frame 3D view will exit
    * 
    * @param frameView3D
    *           Frame that will exit
    */
   public void frame3DExit(FrameView3D frameView3D);
}