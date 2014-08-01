package nars.grid2d;

import static nars.grid2d.Hauto.DOWN;
import static nars.grid2d.Hauto.DOWNLEFT;
import static nars.grid2d.Hauto.LEFT;
import static nars.grid2d.Hauto.RIGHT;
import static nars.grid2d.Hauto.UP;
import static nars.grid2d.Hauto.UPLEFT;
import static nars.grid2d.Hauto.UPRIGHT;

/**
 * Defines an action that may or may not be allowed by the game engine.
 * A corresponding Effect will be returned to the agent's buffer
 */
public class Action {
    
    long createdAt; //when created
    int expiresAt = -1; //allows an agent to set a time limit on the action
    

    public Effect process(Grid2DSpace p, GridAgent a) { return null; }
    
    
    public static class Forward extends Action {
        public final int steps;
        public Forward(int steps) { this.steps = steps;        }        
        
        /** rounds to the nearest cardinal direction and moves. steps can be postive or negative */
        @Override public Effect process(Grid2DSpace p, GridAgent a) {
            int tx = a.x;
            int ty = a.y;
            int heading = a.heading;
            
            boolean allowDiagonal = false;
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
            
            Effect e = p.getMotionEffect(a, this, a.x, a.y, tx, ty);
            if (e.success) {
                a.x = tx;
                a.y = ty;
            }
            return e;
        }
    
        //public void forward(int angle, int steps, boolean allowDiagonal) {    }
    
    }
    
    public static class Turn extends Action {
        public final int angle;
        public Turn(int angle) { this.angle = angle;        }

        @Override
        public Effect process(Grid2DSpace p, GridAgent a) {
            a.heading = angle;
            return new Effect(this, true, p.getTime());
        }
        
        
    }
    public static class Pickup extends Action {
        public final Object o;
        public Pickup(Object o) { this.o = o;        }        
    }
    public static class Drop extends Action {
        public final Object o;
        public Drop(Object o) { this.o = o;        }        
    }
    public static class Door extends Action {
        public final int x, y;
        public final boolean open;
        public Door(int x, int y, boolean open) { this.x = x;  this.y = y;  this.open = open; }
    }

    

}
