package jhelp.util.thread;

/**
 * Do a simple task.<br>
 * Some task just need to have a parameter a don't care about result and progression information. see {@link ThreadedTask}
 * 
 * @author JHelp
 * @param <PARAMETER>
 *           Parameter type
 */
public abstract class ThreadedSimpleTask<PARAMETER>
      extends ThreadedTask<PARAMETER, Void, Void>
{
   /**
    * Do the simple action
    * 
    * @param parameter
    *           Parameter to use
    */
   protected abstract void doSimpleAction(PARAMETER parameter);

   /**
    * Do the action, just call {@link #doSimpleAction(Object)} <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param parameter
    *           Parameter to use
    * @return {@code null}
    * @see ThreadedTask#doThreadAction(Object)
    */
   @Override
   protected final Void doThreadAction(final PARAMETER parameter)
   {
      this.doSimpleAction(parameter);

      return null;
   }
}