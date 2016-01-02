/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package nars.guifx.graph2.layout.box2d;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nars.guifx.util.Animate;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import java.util.Random;

/**
 *
 * @author dilip
 */
public class JFXwithJBox2d extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        primaryStage.setTitle("Hello JBox2d World!");
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(false);
        
        final Group root = new Group(); //Create a group for holding all objects on the screen
        final Scene scene = new Scene(root, Utils.WIDTH, Utils.HEIGHT,Color.BLACK);
        
        //Ball array for hold the  balls
        final Ball[] ball = new Ball[Utils.NO_OF_BALLS];
                
        Random r = new Random(System.currentTimeMillis());
        

        for(int i=0;i<Utils.NO_OF_BALLS;i++) {
            ball[i]=new Ball(r.nextInt(90)+350,r.nextInt(100)+200);
        }
     
        //Add ground to the application, this is where balls will land
        Utils.addGround(100, 500);
        
        //Add left and right walls so balls will not move outside the viewing area.
        Utils.addWall(0,100,1,100); //Left wall
        Utils.addWall(99,100,1,100); //Right wall
        

        Utils.world.setSleepingAllowed(false);

        //Duration duration = Duration.seconds(1.0/60.0); // Set duration for frame.

        new Animate(30, a-> {
            //Create time step. Set Iteration count 8 for velocity and 3 for positions
            Utils.world.step(1.0f/60.f, 8, 3);

            //Move balls to the new position computed by JBox2D
            for(int i=0;i<Utils.NO_OF_BALLS;i++) {
                Node n = ball[i].node;

                Body body = (Body) n.getUserData();
                Vec2 p = body.getWorldCenter(); //body.getPosition();



                //System.out.println(body + " " + xpos + " " + ypos);

                n.setRotate(body.getAngle()*180/3.14159);
                n.setTranslateX(p.x);
                n.setTranslateY(p.y);
            }

        }).start();

        
        /**
         * Set ActionEvent and duration to the KeyFrame. 
         * The ActionEvent is trigged when KeyFrame execution is over. 
         */
//        KeyFrame frame = new KeyFrame(duration, ae, null,null);
//
//        timeline.getKeyFrames().add(frame);

//        //Create button to start simulation.
//        final Button btn = new Button();
//        btn.setLayoutX((Utils.WIDTH/2));
//        btn.setLayoutY((Utils.HEIGHT-30));
//        btn.setText("Start");
//        btn.setOnAction(new EventHandler<ActionEvent>() {
//            public void handle(ActionEvent event) {
//                        timeline.playFromStart();
//                        btn.setVisible(false);
//            }
//        });
//
//        //Add button to the root group
//        root.getChildren().add(btn);

        //Add all balls to the root group
        for(int i=0;i<Utils.NO_OF_BALLS;i++) {
            root.getChildren().add(ball[i].node);
        }
        
        //Draw hurdles on mouse event.
        EventHandler<MouseEvent> addHurdle = new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent me) {
                    //Get mouse's x and y coordinates on the scene
                    float dragX = (float)me.getSceneX();
                    float dragY = (float)me.getSceneY();
                    
                    //Draw ball on this location. Set balls body type to static.
                    Ball hurdle = new Ball(dragX, dragY, 2, BodyType.STATIC, Color.BLUE);
                    //Add ball to the root group
                    root.getChildren().add(hurdle.node);
            }
        };
        
        scene.setOnMouseDragged(addHurdle);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
