package ptrman.dificultyEnvironment.interactionComponents;

import ptrman.dificultyEnvironment.EntityDescriptor;
import ptrman.dificultyEnvironment.JavascriptDescriptor;
import ptrman.dificultyEnvironment.animation.AnimationEvent;
import ptrman.dificultyEnvironment.animation.DelayAnimationEvent;
import ptrman.dificultyEnvironment.animation.EventsJumpAnimationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * executes "animations", which are just sequences of events.
 *
 */
public class AnimationComponent implements IComponent {
    public List<AnimationEvent> events = new ArrayList<>();
    public int currentIndex = 0; // -1 if anaimation is disabled

    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {
        if( currentIndex == -1 ) {
            return;
        }

        if( currentIndex >= events.size() ) {
            currentIndex = -1;
            return;
        }

        AnimationEvent currentEvent = events.get(currentIndex);
        if( currentEvent instanceof DelayAnimationEvent ) {
            DelayAnimationEvent currentEventDelay = (DelayAnimationEvent)currentEvent;

            currentEventDelay.decrement(timedelta);
        }
        else if( currentEvent instanceof EventsJumpAnimationEvent) {
            EventsJumpAnimationEvent currentEventJump = (EventsJumpAnimationEvent)currentEvent;
            currentIndex = currentEventJump.destination;
            return;
        }

        if( currentEvent.isFiring() ) {
            currentEvent.fire(javascriptDescriptor, entityDescriptor);
            currentIndex++;
        }
    }

    @Override
    public String getLongName() {
        return "AnimationComponent";
    }
}
