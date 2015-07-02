package jhelp.util.stateMachine;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedVerySimpleTask;

/**
 * For manage a state machine.<br>
 * Extends this class.<br>
 * Call {@link #doState(int)} when you want pass to an other state
 * 
 * @author JHelp
 */
public abstract class StateMachine
      extends ThreadedVerySimpleTask
{
   /** Head of waiting states queue */
   private int         head;
   /** Maximum number of waiting state in queue */
   private final int   numberMaxOfWaitingState;
   /** Queue of waiting state queue */
   private int         queue;
   /** Actual playing state */
   private int         state;
   /** Waiting states queue */
   private final int[] waitingStates;

   /**
    * Create a new instance of StateMachine
    */
   public StateMachine()
   {
      this(128);
   }

   /**
    * Create a new instance of StateMachine
    * 
    * @param numberMaxOfWaitingState
    *           Number maximum of waiting state
    */
   public StateMachine(final int numberMaxOfWaitingState)
   {
      this.numberMaxOfWaitingState = Math.max(4, Math.min(numberMaxOfWaitingState, 1024));
      this.waitingStates = new int[this.numberMaxOfWaitingState];
      this.head = this.queue = 0;
   }

   /**
    * Called when want check if the passing to a state to an other one is legal
    * 
    * @param actualState
    *           Actual state
    * @param futureState
    *           State that want to go
    * @return {@code true} if it is authorize to got from actual state to future state
    */
   protected abstract boolean canPass(int actualState, int futureState);

   /**
    * Call this method to pass to an other state
    * 
    * @param state
    *           State to go
    */
   protected final void doState(final int state)
   {
      if(this.canDoMoreState() == false)
      {
         throw new IllegalStateException("Can wait more thread than " + this.numberMaxOfWaitingState);
      }

      final boolean launch = this.head == this.queue;

      this.waitingStates[this.queue] = state;
      this.queue = (this.queue + 1) % this.numberMaxOfWaitingState;

      if(launch == true)
      {
         ThreadManager.THREAD_MANAGER.doThread(this, null);
      }
   }

   /**
    * Called when a state have to be played <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @see ThreadedVerySimpleTask#doVerySimpleAction()
    */
   @Override
   protected final void doVerySimpleAction()
   {
      final int state = this.waitingStates[this.head];

      if(this.canPass(this.state, state) == false)
      {
         Debug.println(DebugLevel.WARNING, "Illegal pass state ", this.state, " to ", state);

         this.reportIllegalState(this.state, state);

         return;
      }

      this.playState(state);

      this.head = (this.head + 1) % this.numberMaxOfWaitingState;

      if(this.head != this.queue)
      {
         ThreadManager.THREAD_MANAGER.doThread(this, null);
      }
   }

   /**
    * Actual state
    * 
    * @return Actual state
    */
   protected final int getState()
   {
      return this.state;
   }

   /**
    * Called when a state have to play
    * 
    * @param state
    *           State to play
    */
   protected abstract void playState(int state);

   /**
    * Called each time a state to an other one is not allowed.<br>
    * <br>
    * Usually it signal a bug
    * 
    * @param actualState
    *           Actual state
    * @param futureState
    *           Desired state
    */
   protected abstract void reportIllegalState(int actualState, int futureState);

   /**
    * Indicates if the waiting state queue has more place or not
    * 
    * @return {@code true} if the waiting state queue has more place
    */
   public final boolean canDoMoreState()
   {
      if(this.head == this.queue)
      {
         return true;
      }

      if(this.queue > this.head)
      {
         return ((this.queue - this.head) + 1) < this.numberMaxOfWaitingState;
      }

      return (((this.queue + this.numberMaxOfWaitingState) - this.head) + 1) < this.numberMaxOfWaitingState;
   }
}