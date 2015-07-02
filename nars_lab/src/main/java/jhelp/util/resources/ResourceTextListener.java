package jhelp.util.resources;

/**
 * Listener to alert each time the language is changed
 * 
 * @author JHelp
 */
public interface ResourceTextListener
{
   /**
    * Called when language changed
    * 
    * @param resourceText
    *           Resource text that have changed of language
    */
   public void resourceTextLanguageChanged(ResourceText resourceText);
}