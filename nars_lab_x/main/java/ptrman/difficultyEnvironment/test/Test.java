package ptrman.difficultyEnvironment.test;


import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import nars.rover.physics.PhysicsCamera;
import nars.rover.physics.gl.JoglAbstractDraw;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jbox2d.common.Vec2;
import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.Environment;
import ptrman.difficultyEnvironment.JavascriptDescriptor;
import ptrman.difficultyEnvironment.JavascriptEngine;
import ptrman.difficultyEnvironment.entity.RandomSampler;
import ptrman.difficultyEnvironment.initialisationScripts.entity.EntitySpawner;
import ptrman.difficultyEnvironment.initialisationScripts.world.WorldInitialisation;
import ptrman.difficultyEnvironment.scriptAccessors.ComponentManipulationScriptingAccessor;
import ptrman.difficultyEnvironment.scriptAccessors.EnvironmentScriptingAccessor;
import ptrman.difficultyEnvironment.scriptAccessors.HelperScriptingAccessor;
import ptrman.difficultyEnvironment.view.DrawerRunnable;
import ptrman.difficultyEnvironment.view.JoglDraw;
import ptrman.difficultyEnvironment.view.JoglPanel;

import java.util.ArrayList;
import java.util.List;

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

                "   var physicsComponent = componentManipulationScriptingAccessor.createTopDownViewWheeledPhysicsComponent(8.0, 0.44 * 0.25);" +
                "   resultEntity.components.addComponent(physicsComponent);\n" +

                //"   var spawnScript = \"\";\n" +
                //"   var frameInteractionScript = \"function frameInteraction(entityDescriptor) {   var wheeledPhysicsComponent = entityDescriptor.components.getComponentByName(\\\"TopDownViewWheeledPhysicsComponent\\\"); wheeledPhysicsComponent.thrust(0.0, 1.0);      }\";\n" +
                //"   resultEntity.components.addComponent(componentManipulationScriptingAccessor.createJavascriptComponentWithScriptString(spawnScript, frameInteractionScript));\n" +

                "   var controllerComponent = componentManipulationScriptingAccessor.createTopDownViewWheeledControllerComponent(1.0);" +
                "   controllerComponent.physicsComponent = physicsComponent;" +
                "   resultEntity.components.addComponent(controllerComponent);\n" +



                "   var biasedRandomAIComponent = componentManipulationScriptingAccessor.createBiasedRandomAIComponent(3.0, 0.5, 0.5);" +
                "   biasedRandomAIComponent.topDownViewWheeledControllerComponent = controllerComponent;" +
                "   resultEntity.components.addComponent(biasedRandomAIComponent);\n" +


                        "   " +
                "   return resultEntity;\n" +
                "}\n";

        EntityDescriptor entityDescriptor = EntitySpawner.spawn(javascriptDescriptor, spawnScript, new ArrayList<>());
        environment.entities.add(entityDescriptor);



        // spawn testboxes manually
        final List<ArrayRealVector> boxPositions = RandomSampler.sample(new ArrayRealVector(new double[]{10.0, 10.0}), 10);
        for( final ArrayRealVector boxPosition : boxPositions ) {
            EntityDescriptor createdEntity = javascriptDescriptor.environmentScriptingAccessor.createNewEntity(javascriptDescriptor.helperScriptingAccessor.create2dArrayRealVector(0.0f, 0.0f));

            // create physics body of a rover body and set physics parameters
            List verticesPoints = javascriptDescriptor.helperScriptingAccessor.createList();
            verticesPoints.add(javascriptDescriptor.helperScriptingAccessor.create2dArrayRealVector(-.5f, 0.5f));
            verticesPoints.add(javascriptDescriptor.helperScriptingAccessor.create2dArrayRealVector(0.5f, 0.5f));
            verticesPoints.add(javascriptDescriptor.helperScriptingAccessor.create2dArrayRealVector(0.5f, -0.5f));
            verticesPoints.add(javascriptDescriptor.helperScriptingAccessor.create2dArrayRealVector(-0.5f, -0.5f));

            float linearDamping = 0.9f;
            float angularDamping = 0.6f;
            float restitution = 0.9f;
            float friction = 0.5f;
            float mass = 5.0f;
            boolean isStatic = false;
            ArrayRealVector position = boxPosition;
            createdEntity.physics2dBody = javascriptDescriptor.environmentScriptingAccessor.physics2dCreateBodyWithShape(isStatic, position, verticesPoints, linearDamping, angularDamping, mass, restitution, friction);


            environment.entities.add(createdEntity);
        }



        final float timeDelta = 0.1f;

        final float cameraZoomScaleDiff = 2.0f;
        final float initScale = 50.0f;
        final Vec2 initPosition = new Vec2(-10.0f, -10.0f);
        PhysicsCamera physicsCamera = new PhysicsCamera(initPosition, initScale, cameraZoomScaleDiff);

        JoglAbstractDraw debugDraw = new JoglDraw(physicsCamera);



        GLCapabilities config = new GLCapabilities(GLProfile.getDefault());
        config.setHardwareAccelerated(true);
        config.setAlphaBits(8);
        config.setAccumAlphaBits(8);
        config.setAccumRedBits(8);
        config.setAccumGreenBits(8);
        config.setAccumBlueBits(8);

        config.setNumSamples(1);
        //config.setBackgroundOpaque(false);

        JoglPanel panel = new JoglPanel(environment.physicsWorld2d, debugDraw, null, config);

        debugDraw.setPanel(panel);

        DrawerRunnable drawerRunnable = new DrawerRunnable();
        drawerRunnable.panel = panel;

        JFrame window = new JFrame();
        window.setTitle("Testbed");
        window.setLayout(new BorderLayout());
        window.add(panel, "Center");
        window.pack();
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for(;;) {
            environment.stepFrame(timeDelta, javascriptDescriptor);

            for( EntityDescriptor iterationEntity : environment.entities ) {
                if( iterationEntity.physics2dBody == null ) {
                    continue;
                }
            }

            // draw
            SwingUtilities.invokeLater(drawerRunnable);

            try {
                Thread.sleep((int)(timeDelta * 1000.0f));
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
