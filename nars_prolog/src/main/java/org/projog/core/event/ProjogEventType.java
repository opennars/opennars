package org.projog.core.event;

/**
 * A property of {@link ProjogEvent} used to categorise events.
 */
public enum ProjogEventType {
   /** The event type generated when an attempt is first made to evaluate a goal. */
   CALL,
   /** The event type generated when an attempt is made to re-evaluate a goal. */
   REDO,
   /** The event type generated when an attempt to evaluate a goal succeeds. */
   EXIT,
   /** The event type generated when all attempts to evaluate a goal have failed. */
   FAIL,
   /** The event type generated to warn clients of an event. */
   WARN,
   /** The event type generated to inform clients of an event. */
   INFO
}