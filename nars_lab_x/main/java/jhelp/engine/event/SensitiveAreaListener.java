package jhelp.engine.event;

/**
 * Listener of sensitive area events
 * 
 * @author JHelp
 */
public interface SensitiveAreaListener
{
   /**
    * Called when click on an area
    * 
    * @param area
    *           Clicked area
    */
   public void senstiveClick(int area);

   /**
    * Called when enter on an area
    * 
    * @param area
    *           Entered area
    */
   public void senstiveEnter(int area);

   /**
    * Called when exit on an area
    * 
    * @param area
    *           Exited area
    */
   public void senstiveExit(int area);
}