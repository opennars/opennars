//package nars.grid2d.agent;
//
//import nars.core.NAR;
//import nars.grid2d.Cell;
//import nars.grid2d.Effect;
//import nars.grid2d.GridAgent;
//import nars.ql.ql.QLearner;
//import static nars.grid2d.Hauto.DOWN;
//import static nars.grid2d.Hauto.LEFT;
//import static nars.grid2d.Hauto.RIGHT;
//import static nars.grid2d.Hauto.UP;
//
///**
// *
// * @author me
// */
//
//
//public class QLAgent extends GridAgent {
//
//    QLearner q = new QLearner();
//    public QLAgent(int x, int y, NAR nar) {
//        super(x, y, nar);
//        
//        q.init(6, 4);
//    }
//    
//    @Override
//    public void update(Effect nextEffect) {
//        
//        q.sensor[0] = nextEffect!=null ? 1.0 : 0;        
//        q.sensor[1] = nextEffect!=null ? nextEffect.success ? 1.0 : -1.0 : 0;
//        
//        
//        Cell up = cellRelative(UP);
//        if (up!=null)  q.sensor[2] = space.whyNonTraversible(this, x, y, up.state.x, up.state.y)==null ? 1.0 : 0; 
//        Cell left = cellRelative(LEFT);
//        if (left!=null)  q.sensor[3] = space.whyNonTraversible(this, x, y, left.state.x, left.state.y)==null ? 1.0 : 0; 
//        Cell right = cellRelative(RIGHT);
//        if (right!=null)  q.sensor[4] = space.whyNonTraversible(this, x, y,right.state.x,right.state.y)==null ? 1.0 : 0; 
//        Cell down = cellRelative(DOWN);
//        if (down!=null)  q.sensor[5] = space.whyNonTraversible(this, x, y, down.state.x, down.state.y)==null ? 1.0 : 0; 
//        
//        double r = reward();
//        int action = q.step(r);
//        //System.out.println(nextEffect + " " + Arrays.toString(q.sensor)  + " " + action + " -> " + r);
//        
//        if (action == 0) {
//            
//        }
//        else if (action == 1) {
//            forward(1);
//        }
//        else if (action == 2) {
//            turn(heading + LEFT);
//        }
//        else if (action == 3) {
//            turn(heading + RIGHT);            
//        }
//    }
//    
//    double lastX = x;
//    double lastY = y;
//    
//    public double reward() {
//        double dx = Math.abs(x-lastX);
//        double dy = Math.abs(y-lastY);
//        lastX = x;
//        lastY = y;
//        return dx + dy;
//    }
//    
//    
// }
