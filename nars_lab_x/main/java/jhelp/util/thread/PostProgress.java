package jhelp.util.thread;

import jhelp.util.list.Pair;

/**
 * Task that post a progress information
 * 
 * @author JHelp
 * @param <PROGRESS>
 *           Progression information type
 */
final class PostProgress<PROGRESS>
      extends ThreadedTask<Pair<ThreadedTask<?, ?, PROGRESS>, PROGRESS>, Void, Void>
{
   /**
    * Post the progression information <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param parameter
    *           Pair of task to alert and the progression information to give
    * @return Unused here
    * @see jhelp.util.thread.ThreadedTask#doThreadAction(Object)
    */
   @Override
   protected Void doThreadAction(final Pair<ThreadedTask<?, ?, PROGRESS>, PROGRESS> parameter)
   {
      parameter.element1.doProgress(parameter.element2);

      return null;
   }
}