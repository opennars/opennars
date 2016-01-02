package nars.testchamber;

import nars.NAR;
import nars.testchamber.Action.Forward;
import nars.testchamber.Action.Turn;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;



public abstract class GridAgent extends LocalGridObject {
    
    public final ArrayDeque<Action> actions = new ArrayDeque(); //pending
    public final ArrayDeque<Effect> effects = new ArrayDeque(); //results
    public final Set<Object> inventory = new HashSet();
    public NAR nar;
    
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public GridAgent(int x, int y, NAR nar) {
        super(x, y);
        this.nar=nar;
    }

    
    public void act(Action a) {
        a.createdAt = space.getTime();
        actions.add(a);
    }
    
    public void perceive(Effect e) {         effects.add(e);     }
    
    public Effect perceiveNext() {
        if (effects.isEmpty())
            return null;
        
        return effects.pop();
    }
        
    float animationLerpRate = 0.5f; //LERP interpolation rate
    
    public void forward(int steps) {     act(new Forward(steps));    }
    public void turn(int angle) {   act(new Turn(angle(angle)));  }

    @SuppressWarnings("AbstractMethodOverridesAbstractMethod")
    @Override
    public abstract void update(Effect nextEffect);
    
    @Override
    public void draw() {
        cx = (cx * (1.0f - animationLerpRate)) + (x * animationLerpRate);
        cy = (cy * (1.0f - animationLerpRate)) + (y * animationLerpRate);
        cheading = cheading * (1.0f - animationLerpRate/2.0f) + (heading * animationLerpRate/2.0f);
        
        float scale = (float)Math.sin(space.getTime()/ 7.0f)*0.05f + 1.0f;
        
        space.pushMatrix();
        space.translate(cx, cy);
        space.scale(scale*0.8f);
        
        /*if(!(nar.memory.executive.next.isEmpty())) {
            space.fill(Color.RED.getRGB(), 255);
        } else */
        space.fill(Color.ORANGE.getRGB(), 255);

        space.ellipse(0,0, 1, 1);
        
        //eyes
        space.fill(Color.BLUE.getRGB(), 255);
        space.rotate((float)(Math.PI/ 180.0f * cheading));
        space.translate(-0.15f, 0.4f);
        space.ellipse(0,0,0.2f,0.2f);
        space.translate(0.3f, 0.0f);
        space.ellipse(0,0,0.2f,0.2f);
        
        space.popMatrix();
    }
 
    
}

