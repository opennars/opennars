package ptrman.difficultyEnvironment.scriptAccessors;

import ptrman.difficultyEnvironment.animation.AnimationEvent;
import ptrman.difficultyEnvironment.animation.ExecuteJavascriptAnimationEvent;
import ptrman.difficultyEnvironment.interactionComponents.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ComponentManipulationScriptingAccessor {
    private Set<Class<? extends IComponent>> allComponentClasses = new HashSet<>();

    public ComponentManipulationScriptingAccessor() {
        allComponentClasses.add(AnimationComponent.class);
        allComponentClasses.add(BiasedRandomAIComponent.class);
        allComponentClasses.add(JavascriptComponent.class);
        allComponentClasses.add(TopDownViewWheeledPhysicsComponent.class);
        allComponentClasses.add(TopDownViewWheeledControllerComponent.class);
        allComponentClasses.add(TwoDimensionalRaysComponent.class);
    }

    public IComponent createByComponentClassName(String className, Object... constructorParameters) {
        try {
            for( Class<? extends IComponent> iterationClass : allComponentClasses ) {
                if( iterationClass.getCanonicalName().equals(className) ) {
                    if( constructorParameters.length == 0 ) {
                        return iterationClass.getConstructor().newInstance();
                    }
                    else if( constructorParameters.length == 1 ) {
                        return iterationClass.getConstructor(constructorParameters[0].getClass()).newInstance(constructorParameters[0]);
                    }
                    else if( constructorParameters.length == 2 ) {
                        return iterationClass.getConstructor(constructorParameters[0].getClass(), constructorParameters[1].getClass()).newInstance(constructorParameters[0],constructorParameters[1]);
                    }
                    else if( constructorParameters.length == 3 ) {
                        return iterationClass.getConstructor(constructorParameters[0].getClass(), constructorParameters[1].getClass(), constructorParameters[2].getClass()).newInstance(constructorParameters[0],constructorParameters[1],constructorParameters[2]);
                    }
                    else if( constructorParameters.length == 4 ) {
                        return iterationClass.getConstructor(constructorParameters[0].getClass(), constructorParameters[1].getClass(), constructorParameters[2].getClass(), constructorParameters[3].getClass()).newInstance(constructorParameters[0],constructorParameters[1],constructorParameters[2],constructorParameters[3]);
                    }
                    else if( constructorParameters.length == 5 ) {
                        return iterationClass.getConstructor(constructorParameters[0].getClass(), constructorParameters[1].getClass(), constructorParameters[2].getClass(), constructorParameters[3].getClass(), constructorParameters[4].getClass()).newInstance(constructorParameters[0],constructorParameters[1],constructorParameters[2],constructorParameters[3], constructorParameters[4]);
                    }
                    else {
                        throw new InternalError();
                    }
                }
            }
        } catch (InstantiationException e) {
            throw new InternalError();
        } catch (IllegalAccessException e) {
            throw new InternalError();
        } catch (InvocationTargetException e) {
            throw new InternalError();
        } catch (NoSuchMethodException e) {
            throw new InternalError();
        }

        throw new InternalError();
    }

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

    public IComponent createTopDownViewWheeledControllerComponent(float maxSpeed) {
        return new TopDownViewWheeledControllerComponent(maxSpeed);
    }

    public IComponent createBiasedRandomAIComponent(float timerNextMovechange, float ratioOfMoveRotation, float angleScale) {
        return new BiasedRandomAIComponent(timerNextMovechange, ratioOfMoveRotation, angleScale);
    }

    public IComponent createTwoDimensionalRaysComponent(float raysStartDistance, float raysLength, int numberOfRays, float raysSpreadAngleInRadiants) {
        return new TwoDimensionalRaysComponent(raysStartDistance, raysLength, numberOfRays, raysSpreadAngleInRadiants);
    }

    // belongs into EvenManiputationScriptingAccessor
    public ExecuteJavascriptAnimationEvent createExecuteJavascriptAnimationEventWithScriptString(String script) {
        return ExecuteJavascriptAnimationEvent.createWithScriptString(script);
    }
}
