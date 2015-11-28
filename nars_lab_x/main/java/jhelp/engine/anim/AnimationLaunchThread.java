package jhelp.engine.anim;

import com.jogamp.opengl.GL;
import jhelp.engine.Animation;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedTask;

/**
 * Special animation that launch a threaded task when its turns comes
 * 
 * @author JHelp
 * @param <PARAMETER>
 *           Threaded task parameter type
 * @param <RESULT>
 *           Threaded task result type
 * @param <PROGRESS>
 *           Threaded task progress type
 */
public class AnimationLaunchThread<PARAMETER, RESULT, PROGRESS>
      implements Animation
{
   /** Threaded task parameter */
   private final PARAMETER                                 parameter;
   /** Threaded task */
   private final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask;

   /**
    * Create a new instance of AnimationLaunchThread
    * 
    * @param threadedTask
    *           Task to launch
    * @param parameter
    *           Parameter give to to the task when it will be launch
    */
   public AnimationLaunchThread(final ThreadedTask<PARAMETER, RESULT, PROGRESS> threadedTask, final PARAMETER parameter)
   {
      if(threadedTask == null)
      {
         throw new NullPointerException("threadedTask musn't be null");
      }

      this.threadedTask = threadedTask;
      this.parameter = parameter;
   }

   /**
    * Play the animation. Launch the task <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param gl
    *           Open GL context
    * @param absoluteFrame
    *           Absolute frame
    * @return {@code false} because animation is finished since task is launched
    * @see jhelp.engine.Animation#animate(javax.media.opengl.GL, float)
    */
   @Override
   public boolean animate(final GL gl, final float absoluteFrame)
   {
      ThreadManager.THREAD_MANAGER.doThread(this.threadedTask, this.parameter);

      return false;
   }

   /**
    * Called when animation start <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param startAbsoluteFrame
    *           Start absolute frame
    * @see jhelp.engine.Animation#setStartAbsoluteFrame(float)
    */
   @Override
   public void setStartAbsoluteFrame(final float startAbsoluteFrame)
   {
   }
}