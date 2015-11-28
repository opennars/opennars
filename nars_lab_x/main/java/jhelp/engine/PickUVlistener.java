/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : PickUvlistener<br>
 * Date : 26 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine;

/**
 * Listener for UV picking <br>
 * <br>
 * Last modification : 26 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface PickUVlistener
{
   /**
    * Call when UV is pick
    * 
    * @param u
    *           U pick : [0, 255]
    * @param v
    *           V pick : [0, 255]
    * @param node
    *           Node pick
    */
   public void pickUV(int u, int v, Node node);
}