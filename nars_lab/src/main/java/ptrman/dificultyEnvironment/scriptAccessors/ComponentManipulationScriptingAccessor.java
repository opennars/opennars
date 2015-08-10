package ptrman.dificultyEnvironment.scriptAccessors;

import ptrman.dificultyEnvironment.animation.AnimationEvent;
import ptrman.dificultyEnvironment.animation.ExecuteJavascriptAnimationEvent;
import ptrman.dificultyEnvironment.interactionComponents.AnimationComponent;
import ptrman.dificultyEnvironment.interactionComponents.IComponent;

import java.util.List;

/**
 *
 */
public class ComponentManipulationScriptingAccessor {
    public IComponent createAnimationComponent(List animationEvents) {
        AnimationComponent result = new AnimationComponent();

        for( Object iterationObject : animationEvents ) {
            if( !(iterationObject instanceof AnimationEvent) ) {
                throw new RuntimeException("Object is not an AnimationEvent");
            }

            AnimationEvent iterationAnimationEvent = (AnimationEvent)iterationObject;
            result.events.add(iterationAnimationEvent);
        }

        return result;
    }

    // belongs into EvenManiputationScriptingAccessor
    public ExecuteJavascriptAnimationEvent createExecuteJavascriptAnimationEventWithScriptString(String script) {
        return ExecuteJavascriptAnimationEvent.createWithScriptString(script);
    }
}
