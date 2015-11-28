package jhelp.util.thread;

/**
 * Threaded task very simple.<br>
 * It acts like {@link ThreadedTask} but with no parameter, result, nor progress. <br>
 * It is use full when need do task with no parameter and no waiting result
 * 
 * @author JHelp
 */
public abstract class ThreadedVerySimpleTask
      extends ThreadedSimpleTask<Void>
{
   /**
    * Do the action like a simple task <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param parameter
    *           Unused
    * @see ThreadedSimpleTask#doSimpleAction(Object)
    */
   @Override
   protected final void doSimpleAction(final Void parameter)
   {
      this.doVerySimpleAction();
   }

   /**
    * Do the very simple task in its own thread
    */
   protected abstract void doVerySimpleAction();
}