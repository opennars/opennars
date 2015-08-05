package org.projog.core.event;

import org.projog.core.KB;
import org.projog.core.KnowledgeBaseUtils;

import java.util.Observable;
import java.util.Observer;

/**
 * Controls the registering and notification of observers of a {@link KB}.
 * <p>
 * Each {@code ProjogEventsObservable} has it's own internal {@code java.util.Observable} that it delegates to.
 * <p>
 * Each {@link KB} has a single unique {@code ProjogEventsObservable} instance.
 * 
 * @see KnowledgeBaseUtils#getProjogEventsObservable(KB)
 */
public class ProjogEventsObservable {
   private final Observable observable = new Observable() {
      @Override
      public void notifyObservers(Object arg) {
         super.setChanged();
         super.notifyObservers(arg);
      }
   };

   /**
    * Adds an observer to the set of observers for this objects internal {@code Observable}.
    * 
    * @param observer an observer to be added
    */
   public void addObserver(Observer observer) {
      observable.addObserver(observer);
   }

   /**
    * Deletes an observer from the set of observers of this objects internal {@code Observable}.
    * 
    * @param observer an observer to be deleted
    */
   public void deleteObserver(Observer observer) {
      observable.deleteObserver(observer);
   }

   /**
    * Notify all observers.
    * <p>
    * Each observer has its <code>update</code> method called with two arguments: this objects internal
    * {@code Observable} object and the <code>event</code> argument.
    */
   public void notifyObservers(ProjogEvent event) {
      observable.notifyObservers(event);
   }
}