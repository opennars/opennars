package ptrman.dificultyEnvironment.test;

import org.jbox2d.common.Vec2;
import ptrman.dificultyEnvironment.EntityDescriptor;
import ptrman.dificultyEnvironment.Environment;
import ptrman.dificultyEnvironment.JavascriptDescriptor;
import ptrman.dificultyEnvironment.JavascriptEngine;
import ptrman.dificultyEnvironment.initialisationScripts.entity.EntitySpawner;
import ptrman.dificultyEnvironment.initialisationScripts.world.WorldInitialisation;
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

        // execute the initialisation script
        final String worldInitialisationScript =
                "function initializeWorld(environment) {" +
                "   environmentScriptingAccessor.physics2dCreateWorld();" +
                "}";

        WorldInitialisation.executeWorldInitialisationScript(environment, javascriptDescriptor, worldInitialisationScript);


        final String spawnScript =
                "function spawn() {" +
                "   var resultEntity = environmentScriptingAccessor.createNewEntity(helperScriptingAccessor.create2dArrayRealVector(0.0, 0.0));\n" +

                "   // create physics body of a rover body and set physics parameters\n" +
                "   var verticesPoints = helperScriptingAccessor.createList();\n" +
                "   verticesPoints.add(helperScriptingAccessor.create2dArrayRealVector(3.0, 0.0));\n" +
                "   verticesPoints.add(helperScriptingAccessor.create2dArrayRealVector(-1.0, 2.0));\n" +
                "   verticesPoints.add(helperScriptingAccessor.create2dArrayRealVector(-1.0, -2.0));\n" +

                "   var linearDamping = 0.9;\n" +
                "   var angularDamping = 0.6;\n" +
                "   var restitution = 0.9; // bounciness\n" +
                "   var friction = 0.5;\n" +
                "   var mass = 5.0;\n" +
                "   var static = false;\n" +
                "   var position = helperScriptingAccessor.create2dArrayRealVector(0.0, 0.0);\n" +
                "   resultEntity.physics2dBody = environmentScriptingAccessor.physics2dCreateBodyWithShape(static, position, verticesPoints, linearDamping, angularDamping, mass, restitution, friction);\n" +

                        "   " +
                //        "var eventList = helperScriptingAccessor.createList();" +
                //        "eventList.add(componentManipulationScriptingAccessor.createExecuteJavascriptAnimationEventWithScriptString(\"function animationEvent(entityDescriptor) {environmentScriptingAccessor.physics2dApplyForce(helperScriptingAccessor.create2dArrayRealVector(100.0, 0.0));}\"));" +
                //        "" +
                //        "resultEntity.components.add(componentManipulationScriptingAccessor.createAnimationComponent(eventList));" +


                "   resultEntity.components.addComponent(componentManipulationScriptingAccessor.createTopDownViewWheeledPhysicsComponent(8.0, 0.44 * 0.25));\n" +

                "   var spawnScript = \"\";\n" +
                "   var frameInteractionScript = \"function frameInteraction(entityDescriptor) {   var wheeledPhysicsComponent = entityDescriptor.components.getComponentByName(\\\"TopDownViewWheeledPhysicsComponent\\\"); wheeledPhysicsComponent.thrust(0.0, 5.0);      }\";\n" +
                "   resultEntity.components.addComponent(componentManipulationScriptingAccessor.createJavascriptComponentWithScriptString(spawnScript, frameInteractionScript));\n" +


                        "   " +
                "   return resultEntity;\n" +
                "}\n";

        EntityDescriptor entityDescriptor = EntitySpawner.spawn(javascriptDescriptor, spawnScript, new ArrayList<>());
        environment.entities.add(entityDescriptor);

        final float timeDelta = 0.1f;

        for(;;) {
            environment.stepFrame(timeDelta, javascriptDescriptor);

            for( EntityDescriptor iterationEntity : environment.entities ) {
                if( iterationEntity.physics2dBody == null ) {
                    continue;
                }

                final Vec2 physicsPosition = iterationEntity.physics2dBody.body.getPosition();
                System.out.print(physicsPosition.x);
                System.out.print(" ");
                System.out.print(physicsPosition.y);
                System.out.println();
            }
        }
    }
}
