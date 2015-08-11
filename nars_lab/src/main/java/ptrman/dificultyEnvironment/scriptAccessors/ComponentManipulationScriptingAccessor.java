package ptrman.dificultyEnvironment.scriptAccessors;

import ptrman.dificultyEnvironment.animation.AnimationEvent;
import ptrman.dificultyEnvironment.animation.ExecuteJavascriptAnimationEvent;
import ptrman.dificultyEnvironment.interactionComponents.AnimationComponent;
import ptrman.dificultyEnvironment.interactionComponents.IComponent;
import ptrman.dificultyEnvironment.interactionComponents.JavascriptComponent;
import ptrman.dificultyEnvironment.interactionComponents.TopDownViewWheeledPhysicsComponent;

import java.util.List;

// TODO< use reflection to abstract this code (just call the matching constructor) ? >
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

    public IComponent createTopDownViewWheeledPhysicsComponent(float linearThrustPerCycle, float angularSpeedPerCycle) {
        return new TopDownViewWheeledPhysicsComponent(linearThrustPerCycle, angularSpeedPerCycle);
    }

    public IComponent createJavascriptComponentWithScriptString(String spawnScript, String frameInteractionScript) {
        return JavascriptComponent.createFromRawSourcecode(spawnScript, frameInteractionScript);
    }

    // belongs into EvenManiputationScriptingAccessor
    public ExecuteJavascriptAnimationEvent createExecuteJavascriptAnimationEventWithScriptString(String script) {
        return ExecuteJavascriptAnimationEvent.createWithScriptString(script);
    }
}
