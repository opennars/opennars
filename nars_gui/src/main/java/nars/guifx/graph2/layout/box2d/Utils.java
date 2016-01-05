/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package nars.guifx.graph2.layout.box2d;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author dilip
 */
public enum Utils {
    ;
    //Create a JBox2D world. 
    public static final World world = new World(
        new Vec2(0.0f, 25.0f)
    );
    
    //Screen width and height
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    //Ball radius in pixel
    public static final int BALL_SIZE = 8;
    
    //Total number of balls
    public static final int NO_OF_BALLS = 16;
    
    //Ball gradient
    private static final LinearGradient BALL_GRADIENT = new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.RED));
    
    //This method adds a ground to the screen. 
    public static void addGround(float width, float height){
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(width,height);
        
        FixtureDef fd = new FixtureDef();
        fd.shape = ps;

        BodyDef bd = new BodyDef();
        bd.position= new Vec2(0.0f,-10f);

        world.createBody(bd).createFixture(fd);
    }
    
    //This method creates a walls. 
    public static void addWall(float posX, float posY, float width, float height){
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(width,height);
        
        FixtureDef fd = new FixtureDef();
        fd.shape = ps;
        fd.density = 1.0f;
        fd.friction = 0.3f;    

        BodyDef bd = new BodyDef();
        bd.position.set(posX, posY);
        
        Utils.world.createBody(bd).createFixture(fd);
    }
    
    //This gives a look and feel to balls
    public static LinearGradient getBallGradient(Color color){
        if(color.equals(Color.RED))
            return BALL_GRADIENT;
        else
            return new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, color));
    }
   
    /** Gets a box shape with a given width and height */
    public static FixtureDef newBox(float width, float height, float density){
        PolygonShape box = new PolygonShape();
        box.setAsBox(width, height);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = box;
        //fixture.filter.categoryBits = CollisionFilters.ENTITY;
        //fixture.filter.maskBits = CollisionFilters.EVERYTHING;
        fixture.density = density;
        return fixture;
    }

}
