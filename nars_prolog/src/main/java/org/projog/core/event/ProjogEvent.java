package org.projog.core.event;

/**
 * Represents an event that has occurred during the evaluation of Prolog goals.
 * 
 * @see ProjogEventsObservable#notifyObservers(ProjogEvent)
 */
public class ProjogEvent {
   private final ProjogEventType type;
   private final String message;
   private final Object source;

   /**
    * @param message a description of the event
    * @param source the object that generated the event
    */
   public ProjogEvent(ProjogEventType type, String message, Object source) {
      this.type = type;
      this.message = message;
      this.source = source;
   }

   public ProjogEventType getType() {
      return type;
   }

   /**
    * Returns the description of the event.
    */
   public String getMessage() {
      return message;
   }

   /**
    * Returns the object that generated this event.
    */
   public Object getSource() {
      return source;
   }
}