package nars.testchamber;

import nars.NAR;
import nars.testchamber.Action.Forward;
import nars.testchamber.Cell.Logic;
import nars.testchamber.Cell.Material;
import nars.testchamber.map.Maze;
import nars.testchamber.object.Key;
import nars.testchamber.operator.Activate;
import nars.testchamber.operator.Deactivate;
import nars.testchamber.operator.Goto;
import nars.testchamber.operator.Pick;
import processing.core.PVector;

import java.util.List;

import static nars.testchamber.Hauto.*;

public class TestChamber2 extends TestChamber {

    public PVector lasttarget = new PVector(25, 25); //not work
    boolean invalid=false;
    
    public static String getobj(int x,int y) {
        for(GridObject gridi : space.objects) {
            if(gridi instanceof LocalGridObject) { //Key && ((Key)gridi).doorname.equals(goal)) {
                LocalGridObject gridu=(LocalGridObject) gridi;
                if(gridu.x==x && gridu.y==y && gridu.doorname != null && !gridu.doorname.isEmpty())
                    return gridu.doorname;
            }
        }
        return "";
    }
    
    public static void operateObj(String arg,String opnamer) {
        opname=opnamer;
        Hauto cells = space.cells;
        goal = arg;
        for (int i = 0; i < cells.w; i++) {
            for (int j = 0; j < cells.h; j++) {
                if (cells.readCells[i][j].name.equals(arg)) {
                    if("go-to".equals(opname))
                        space.target = new PVector(i, j);
                }
            }
        }
        //if("pick".equals(opname)) {
        //Key && ((Key)gridi).doorname.equals(goal)) {
        space.objects.stream().filter(gridi -> gridi instanceof LocalGridObject && ((LocalGridObject) gridi).doorname.equals(goal)).forEach(gridi -> { //Key && ((Key)gridi).doorname.equals(goal)) {
            LocalGridObject gridu = (LocalGridObject) gridi;
            if ("go-to".equals(opname))
                space.target = new PVector(gridu.x, gridu.y);
        });
        //}
    }
    

    public TestChamber2(NAR nar) {
        int w = 50;
        int h = 50;
        int water_threshold = 30;
        Hauto cells = new Hauto(w, h, nar);
        cells.forEach(0, 0, w, h, c -> {
///c.setHeight((int)(Math.random() * 12 + 1));
            float smoothness = 20.0f;
            c.material = Material.GrassFloor;
            double n = SimplexNoise.noise(c.state.x / smoothness, c.state.y / smoothness);
            if ((n * 64) > water_threshold) {
                c.material = Material.Water;
            }
            c.setHeight((int) (Math.random() * 24 + 1));
        });
        
        Maze.buildMaze(cells, 3, 3, 23, 23);
        space = new Grid2DSpace(cells, nar);
        space.FrameRate = 0;
        space.agentPeriod = 20;
        space.automataPeriod = 5;
        
        space.newWindow(1000, 800, true);
        
        
        cells.forEach(16, 16, 18, 18, new Hauto.SetMaterial(Material.DirtFloor));
        //noinspection OverlyComplexAnonymousInnerClass
        GridAgent a = new GridAgent(17, 17, nar) {

            @Override
            public void update(Effect nextEffect) {
// int a = 0;
// if (Math.random() < 0.4) {
// int randDir = (int)(Math.random()*4);
// if (randDir == 0) a = LEFT;
// else if (randDir == 1) a = RIGHT;
// else if (randDir == 2) a = UP;
// else if (randDir == 3) a = DOWN;
// /*else if (randDir == 4) a = UPLEFT;
// else if (randDir == 5) a = UPRIGHT;
// else if (randDir == 6) a = DOWNLEFT;
// else if (randDir == 7) a = DOWNRIGHT;*/
// turn(a);
// }
/*if (Math.random() < 0.2) {
                 forward(1);
                 }*/
                lasttarget = space.target;
                PVector current = new PVector(x, y);
               // System.out.println(nextEffect);
                if (nextEffect == null) {
                    List<PVector> path = Grid2DSpace.Shortest_Path(space, this, current, space.target);
                    actions.clear();
                   // System.out.println(path);
                    if (path != null) {
                        if(inventorybag!=null) {
                            inventorybag.x=(int)current.x;
                            inventorybag.y=(int)current.y;
                            inventorybag.cx=(int)current.x;
                            inventorybag.cy=(int)current.y;
                        }
                        if(!(inventorybag instanceof Key)) {
                            keyn=-1;
                        }
                        if (path.size() <= 1) {
                            //nar.step(1);
                            //System.out.println("at destination; didnt need to find path");
                            if (goal != null && !goal.isEmpty() && current.equals(space.target)) {
                                //--nar.step(6);
                                GridObject obi=null;
                                if(opname != null && !opname.isEmpty()) {
                                    for(GridObject gridi : space.objects) {
                                        if(gridi instanceof LocalGridObject && ((LocalGridObject)gridi).doorname.equals(goal) &&
                                          ((LocalGridObject)gridi).x==(int)current.x &&
                                               ((LocalGridObject)gridi).y==(int)current.y ) {
                                            obi=gridi;
                                            break;
                                        }
                                    }
                                }
                                if(obi!=null || cells.readCells[(int)current.x][(int)current.y].name.equals(goal)) { //only possible for existing ones
                                    if("pick".equals(opname)) {
                                        if(inventorybag instanceof LocalGridObject) {
                                            //we have to drop it
                                            LocalGridObject ob= inventorybag;
                                            ob.x=(int)current.x;
                                            ob.y=(int)current.y;
                                            space.objects.add(ob);
                                        }
                                        inventorybag=(LocalGridObject)obi;
                                        if(obi!=null) {
                                            space.objects.remove(obi);
                                            if(inventorybag.doorname.startsWith("key")) {
                                                keyn=Integer.parseInt(inventorybag.doorname.replaceAll("key", ""));
                                                for(int i=0;i<cells.h;i++) {
                                                    for(int j=0;j<cells.w;j++) {
                                                        if(Hauto.doornumber(cells.readCells[i][j])==keyn) {
                                                            cells.readCells[i][j].is_solid=false;
                                                            cells.writeCells[i][j].is_solid=false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        nar.input("(*,hold," + goal + "). :|:");
                                    }
                                    else
                                    if("deactivate".equals(opname)) {
                                        for(int i=0;i<cells.h;i++) {
                                            for(int j=0;j<cells.w;j++) {
                                                if(cells.readCells[i][j].name.equals(goal)) {
                                                    if(cells.readCells[i][j].logic==Logic.SWITCH) {
                                                        cells.readCells[i][j].logic=Logic.OFFSWITCH;
                                                        cells.writeCells[i][j].logic=Logic.OFFSWITCH;
                                                        cells.readCells[i][j].charge=0.0f;
                                                        cells.writeCells[i][j].charge=0.0f;
                                                        nar.input("(off," + goal + "). :|:");
                                                    }
                                                }
                                            }
                                        }
                                        
                                    }
                                    else
                                    if("activate".equals(opname)) {
                                        for(int i=0;i<cells.h;i++) {
                                            for(int j=0;j<cells.w;j++) {
                                                if(cells.readCells[i][j].name.equals(goal)) {
                                                    if(cells.readCells[i][j].logic==Logic.OFFSWITCH) {
                                                        cells.readCells[i][j].logic=Logic.SWITCH;
                                                        cells.writeCells[i][j].logic=Logic.SWITCH;
                                                        cells.readCells[i][j].charge=1.0f;
                                                        cells.writeCells[i][j].charge=1.0f;
                                                        nar.input("(*,on," + goal + "). :|:");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if("go-to".equals(opname)) {
                                        nar.input("(*,at," + goal + "). :|:");
                                    }
                                }
                            }
                            opname="";
                        } else {
                            int numSteps = Math.min(10, path.size());
                            float cx = x;
                            float cy = y;
                            for (int i = 1; i < numSteps; i++) {
                                PVector next = path.get(i);
                                int dx = (int) (next.x - cx);
                                int dy = (int) (next.y - cy);
                                if ((dx == 0) && (dy == 1)) {
                                    turn(UP);
                                    forward(1);
                                }
                                if ((dx == 1) && (dy == 0)) {
                                    turn(RIGHT);
                                    forward(1);
                                }
                                if ((dx == -1) && (dy == 0)) {
                                    turn(LEFT);
                                    forward(1);
                                }
                                if ((dx == 0) && (dy == -1)) {
                                    turn(DOWN);
                                    forward(1);
                                }
                                cx = next.x;
                                cy = next.y;
                            }
                        }
                    }
                }
            }
            
            
            @Override
            public void perceive(Effect e) {                
                super.perceive(e);
                
                
                String action = e.action.getClass().getSimpleName().toString();
                String actionParam = e.action.toParamString();                
                String success = String.valueOf(e.success);
                if (actionParam == null) actionParam = "";
                if (!actionParam.isEmpty()) actionParam = "(*," + actionParam + ')';
                
                nar.input("$0.60$ (*,effect," + action + ',' + actionParam + ',' + success + "). :|:");
                
                int SightPeriod = 32;
                if ((e.action instanceof Forward) || (space.getTime()%SightPeriod == 0)) {
                    String seeing = "(*,";

                    seeing += cellOn().material + ",";
                    seeing += cellAbsolute(0).material + ",";
                    seeing += cellAbsolute(90).material + ",";
                    seeing += cellAbsolute(180).material + ",";
                    seeing += cellAbsolute(270).material + ")";


                    nar.input("$0.50$ (*,see," + seeing + "). :|:");
                }
                
            }
        };


        nar.onExec(new Goto(this, "goto"));
        nar.onExec(new Pick(this, "pick"));
        nar.onExec(new Activate(this, "activate"));
        nar.onExec(new Deactivate(this, "deactivate"));
        space.add(a);
        
        
        
        //space.add(new QLAgent(10,20, new ContinuousBagNARBuilder(true).build()));
       // space.add(new Key(20, 20));
//space.add(new RayVision(a, 45, 10, 8));
    }

//    public static void main(String[] arg) {
//        NAR nar = new Default();
//
//
//        new TestChamber2(nar);
//
//        //new NARSwing(nar);
//
//        nar.start(200);
//    }
}
