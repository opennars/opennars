package ptrman.dificultyEnvironment.test;

import ptrman.dificultyEnvironment.EntityDescriptor;
import ptrman.dificultyEnvironment.Environment;
import ptrman.dificultyEnvironment.JavascriptDescriptor;
import ptrman.dificultyEnvironment.JavascriptEngine;
import ptrman.dificultyEnvironment.entity.EntitySpawner;
import ptrman.dificultyEnvironment.scriptAccessors.ComponentManipulationScriptingAccessor;
import ptrman.dificultyEnvironment.scriptAccessors.EnvironmentScriptingAccessor;
import ptrman.dificultyEnvironment.scriptAccessors.HelperScriptingAccessor;

import java.util.ArrayList;

/**
 *
 */
public class Test {
    public static void main(String[] args) {
        Environment environment = new Environment();

        JavascriptDescriptor javascriptDescriptor = new JavascriptDescriptor();
        javascriptDescriptor.engine = new JavascriptEngine();
        javascriptDescriptor.environmentScriptingAccessor = new EnvironmentScriptingAccessor(environment);
        javascriptDescriptor.helperScriptingAccessor = new HelperScriptingAccessor();
        javascriptDescriptor.componentManipulationScriptingAccessor = new ComponentManipulationScriptingAccessor();

        final String spawnScript =
                "function spawn() {" +
                "   var resultEntity = environmentScriptingAccessor.createNewEntity(helperScriptingAccessor.create2dArrayRealVector(0.0, 0.0));" +
                "   " +
                        "var eventList = helperScriptingAccessor.createList();" +
                        "eventList.add(componentManipulationScriptingAccessor.createExecuteJavascriptAnimationEventWithScriptString(\"function animationEvent(entityDescriptor) {environmentScriptingAccessor.physics2dApplyForce(helperScriptingAccessor.create2dArrayRealVector(100.0, 0.0));}\"));" +
                        "" +
                        "resultEntity.components.add(componentManipulationScriptingAccessor.createAnimationComponent(eventList));" +
                "   " +
                "   return resultEntity;" +
                "}";

        EntityDescriptor entityDescriptor = EntitySpawner.spawn(javascriptDescriptor, spawnScript, new ArrayList<>());
        environment.entities.add(entityDescriptor);
    }
}
