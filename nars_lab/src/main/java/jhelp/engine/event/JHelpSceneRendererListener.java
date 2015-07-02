/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.event<br>
 * Class : JHelpSceneRendererListener<br>
 * Date : 8 déc. 2010<br>
 * By JHelp
 */
package jhelp.engine.event;

import jhelp.engine.JHelpSceneRenderer;

/**
 * Listener of {@link JHelpSceneRenderer} states <br>
 * <br>
 * Last modification : 8 déc. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface JHelpSceneRendererListener
{
   /**
    * Call when scene renderer is initialized.<br>
    * After this call you can, by example manipulate lights
    * 
    * @param sceneRenderer
    *           Initialized scene renderer
    */
   public void sceneRendererIsInitialized(JHelpSceneRenderer sceneRenderer);
}