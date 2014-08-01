package nars.grid2d;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import nars.grid2d.Action.Forward;
import nars.grid2d.Action.Turn;
import static nars.grid2d.Hauto.DOWN;
import static nars.grid2d.Hauto.DOWNLEFT;
import static nars.grid2d.Hauto.DOWNRIGHT;
import static nars.grid2d.Hauto.LEFT;
import static nars.grid2d.Hauto.RIGHT;
import static nars.grid2d.Hauto.UP;
import static nars.grid2d.Hauto.UPLEFT;
import static nars.grid2d.Hauto.UPRIGHT;



public class GridAgent extends LocalGridObject {
    
    public final ArrayDeque<Action> actions = new ArrayDeque(); //pending
    public final ArrayDeque<Effect> effects = new ArrayDeque(); //results
    public final Set<Object> inventory = new HashSet();
    
    
    public GridAgent(int x, int y) {
        super(x, y);
    }

    
    public void act(Action a) {
        a.createdAt = space.getTime();
        actions.add(a);
    }
    
    public void perceive(Effect e) {         effects.add(e);     }
    
    public boolean perceiveNext() {
        if (effects.size() == 0)
            return false;
        
        Effect e = effects.pop();        
        
        /*if (e.action instanceof Forward) {
            if (e.success) {
                System.out.println("moved to: " + x + "," + y);
            }
            else {
                System.out.println("can't move to: " + x + "," + y + " because: " + e);
            }
        }
        else {*/
            System.out.println(e);
        //}
        
        return true;
    }
        
    float animationLerpRate = 0.1f; //LERP interpolation rate
    
    public void forward(int steps) {     act(new Forward(steps));    }
    public void turn(int angle) {   act(new Turn(angle));  }

    public void update() {
        int a = 0;
        if (Math.random() < 0.4) {
            int randDir = (int)(Math.random()*4);
            if (randDir == 0)             a = LEFT;
            else if (randDir == 1)        a = RIGHT;
            else if (randDir == 2)        a = UP;
            else if (randDir == 3)        a = DOWN;
            else if (randDir == 4)        a = UPLEFT;
            else if (randDir == 5)        a = UPRIGHT;
            else if (randDir == 6)        a = DOWNLEFT;
            else if (randDir == 7)        a = DOWNRIGHT;
            turn(a);
        }
        
        if (Math.random() < 0.2) {
            forward(1);
        }
        
        perceiveNext();
    }
    
    @Override
    public void draw() {
        cx = (cx * (1.0f - animationLerpRate)) + (x * animationLerpRate);
        cy = (cy * (1.0f - animationLerpRate)) + (y * animationLerpRate);
        cheading = (cheading * (1.0f - animationLerpRate/2.0f)) + (heading * animationLerpRate/2.0f);
        
        float scale = (float)Math.sin(space.getTime()/7f)*0.05f + 1.0f;
        
        space.pushMatrix();
        space.translate(cx, cy);
        space.scale(scale);
        space.fill(Color.ORANGE.getRGB(), 230);
        space.ellipse(0,0, 1, 1);
        
        //eyes
        space.fill(Color.BLUE.getRGB(), 230);
        space.rotate((float)(Math.PI/180f * cheading));
        space.translate(-0.15f, 0.4f);
        space.ellipse(0,0,0.2f,0.2f);
        space.translate(0.3f, 0.0f);
        space.ellipse(0,0,0.2f,0.2f);
        
        space.popMatrix();
    }
 
    
}
