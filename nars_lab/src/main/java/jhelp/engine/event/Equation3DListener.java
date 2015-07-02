/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.geom<br>
 * Class : Equation3DListener<br>
 * Date : 12 aoet 2010<br>
 * By JHelp
 */
package jhelp.engine.event;

import jhelp.engine.geom.Equation3D;

/**
 * Listener to know when equation 3D is ready<br>
 * <br>
 * Last modification : 12 aoet 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface Equation3DListener
{
   /**
    * Call when an equation 3D is ready
    * 
    * @param equation3D
    *           Equation ready
    */
   public void equation3Dready(Equation3D equation3D);
}