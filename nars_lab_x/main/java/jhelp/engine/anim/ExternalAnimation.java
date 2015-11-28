package jhelp.engine.anim;

/**
 * Animation that not need Open GL and direct link to JOGL.<br>
 * So can be used from external easily
 * 
 * @author JHelp
 */
public interface ExternalAnimation
{
   /**
    * Called when animation initialized
    */
   public void initializeAnimation();

   /**
    * Called when animation refreshed
    * 
    * @param frame
    *           Animation frame
    * @return {@code true} if animation have to continue. {@code false} if animation finished
    */
   public boolean playAnimation(final float frame);
}