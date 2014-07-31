package nars.grid2d;

import java.awt.Color;


/** an agent body that is 1 cell in size */
public class CellAgent extends GridObject {
    
    final static int RIGHT = -9;
    final static int DOWN = 180;
    final static int LEFT = 90;
    final static int UP = 0;
    final static int UPLEFT = (UP+LEFT)/2;
    final static int UPRIGHT = (UP+RIGHT)/2;
    final static int DOWNLEFT = (DOWN+LEFT)/2;
    final static int DOWNRIGHT = (DOWN+RIGHT)/2;
    
    public float cx, cy, cheading;
    public int x;
    public int y;
    public int heading; //in degrees
    
    //LERP interpolation rate
    float lerpRate = 0.1f;

    
    /** rounds to the nearest cardinal direction and moves. steps can be postive or negative */
    public void forward(int steps, boolean allowDiagonal) {    
        switch (heading) {
            case LEFT: x-=steps; break;
            case RIGHT: x+=steps; break;
            case UP: y+=steps; break;
            case DOWN: y-=steps; break;
            default:
                if (allowDiagonal) {
                    switch (heading) {
                        case UPLEFT: x-=steps; y+=steps; break;
                        case UPRIGHT: x+=steps; y+=steps; break;
                        case DOWNLEFT: x-=steps; y-=steps; break;
                        //case DOWNRIGHT: x+=steps; y+=steps;  break;
                    }
                }
                break;                
        }
    }
    
    public void forward(int angle, int steps, boolean allowDiagonal) {    }
    
    /**  */
    public void turn(int deltaAngle) {       }

    
    public void update(Grid2DSpace p) {
        int randDir = (int)(Math.random()*4);
        
        if (Math.random() < 0.4) {
            if (randDir == 0)             heading = LEFT;
            else if (randDir == 1)        heading = RIGHT;
            else if (randDir == 2)        heading = UP;
            else if (randDir == 3)        heading = DOWN;
            else if (randDir == 4)        heading = UPLEFT;
            else if (randDir == 5)        heading = UPRIGHT;
            else if (randDir == 6)        heading = DOWNLEFT;
            else if (randDir == 7)        heading = DOWNRIGHT;
        }
        
        if (Math.random() < 0.2)
            forward(1, true);        
    }
    
    @Override
    public void draw(Grid2DSpace p) {
        cx = (cx * (1.0f - lerpRate)) + (x * lerpRate);
        cy = (cy * (1.0f - lerpRate)) + (y * lerpRate);
        cheading = (cheading * (1.0f - lerpRate/2.0f)) + (heading * lerpRate/2.0f);
        
        float scale = (float)Math.sin(p.getTime()/7f)*0.05f + 1.0f;
        
        p.pushMatrix();
        p.translate(cx, cy);
        p.scale(scale);
        p.fill(Color.ORANGE.getRGB(), 230);
        p.ellipse(0,0, 1, 1);
        
        //eyes
        p.fill(Color.BLUE.getRGB(), 230);
        p.rotate((float)(Math.PI/180f * cheading));
        p.translate(-0.15f, 0.4f);
        p.ellipse(0,0,0.2f,0.2f);
        p.translate(0.3f, 0.0f);
        p.ellipse(0,0,0.2f,0.2f);
        
        p.popMatrix();
    }
 
    
}
