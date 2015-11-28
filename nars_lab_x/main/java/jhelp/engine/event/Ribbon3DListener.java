package jhelp.engine.event;

import jhelp.engine.geom.Ribbon3D;

/**
 * Listner to know when ribbon is ready
 * 
 * @author JHelp
 */
public interface Ribbon3DListener
{
   /**
    * Called when ribbon ready
    * 
    * @param ribbon3d
    *           Ready ribbon
    */
   public void ribbonReady(Ribbon3D ribbon3d);
}