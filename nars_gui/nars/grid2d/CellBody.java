package nars.grid2d;

import java.awt.Color;
import nars.grid2d.Grid2DSpace.MotionEffect;
import static nars.grid2d.Hauto.DOWN;
import static nars.grid2d.Hauto.DOWNLEFT;
import static nars.grid2d.Hauto.DOWNRIGHT;
import static nars.grid2d.Hauto.LEFT;
import static nars.grid2d.Hauto.RIGHT;
import static nars.grid2d.Hauto.UP;
import static nars.grid2d.Hauto.UPLEFT;
import static nars.grid2d.Hauto.UPRIGHT;



public class CellBody extends LocalGridObject {

    public CellBody(int x, int y) {
        super(x, y);
    }
    
    //LERP interpolation rate
    float lerpRate = 0.1f;
    
    /** rounds to the nearest cardinal direction and moves. steps can be postive or negative */
    public void forward(Grid2DSpace p, int steps, boolean allowDiagonal) {    
        int tx = x;
        int ty = y;
        switch (heading) {
            case LEFT: tx-=steps; break;
            case RIGHT: tx+=steps; break;
            case UP: ty+=steps; break;
            case DOWN: ty-=steps; break;
            default:
                if (allowDiagonal) {
                    switch (heading) {
                        case UPLEFT: tx-=steps; ty+=steps; break;
                        case UPRIGHT: tx+=steps; ty+=steps; break;
                        case DOWNLEFT: tx-=steps; ty-=steps; break;
                        //case DOWNRIGHT: x+=steps; y+=steps;  break;
                    }
                }
                break;                
        }
        MotionEffect e = p.getMotionEffect(x, y, tx, ty);
        
        if (e == MotionEffect.Moved) {
            x = tx;
            y = ty;
            System.out.println("moved to: " + x + "," + y);
        }
        else {
            System.out.println(e);
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
        
        if (Math.random() < 0.2) {
            forward(p, 1, true);
        }
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
