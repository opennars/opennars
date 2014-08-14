package nars.grid2d;

import java.util.ArrayList;
import java.util.List;
import nars.core.NAR;
import nars.grid2d.Cell.Material;
import static nars.grid2d.Hauto.DOWN;
import static nars.grid2d.Hauto.LEFT;
import static nars.grid2d.Hauto.RIGHT;
import static nars.grid2d.Hauto.UP;
import nars.grid2d.agent.ql.QLAgent;
import nars.grid2d.map.Maze;
import nars.grid2d.object.Key;
import nars.grid2d.operator.Goto;
import nars.grid2d.operator.Pick;
import processing.core.PVector;

public class TestChamber {

    Grid2DSpace space;
    boolean getfeedback = false;
    PVector target = new PVector(25, 25);
    String goal = "";
    String opname="";
    List<GridObject> inventory=new ArrayList<GridObject>();
    public void gotoObj(String arg,String opname) {
        this.opname=opname;
        Hauto cells = space.cells;
        goal = arg;
        for (int i = 0; i < cells.w; i++) {
            for (int j = 0; j < cells.h; j++) {
                if (cells.readCells[i][j].name.equals(arg)) {
                    target = new PVector(i, j);
                    getfeedback = true;
                }
            }
        }
        //if("pick".equals(opname)) {
            for(GridObject gridi : space.objects) {
                if(gridi instanceof Key && ((Key)gridi).doorname.equals(goal)) {
                    LocalGridObject gridu=(LocalGridObject) gridi;
                    if(opname.equals("go-to"))
                        target = new PVector(gridu.x, gridu.y);
                    getfeedback = true; //currently pick only allows to pick if goto was already done
                }
            }
        //}
    }

    public void create(NAR nar) {
//NAR n = new NAR();
        int w = 50;
        int h = 50;
        int water_threshold = 30;
        Hauto cells = new Hauto(w, h, nar);
        cells.forEach(0, 0, w, h, new CellFunction() {
            @Override
            public void update(Cell c) {
///c.setHeight((int)(Math.random() * 12 + 1));
                float smoothness = 20f;
                c.material = Material.GrassFloor;
                double n = SimplexNoise.noise(c.state.x / smoothness, c.state.y / smoothness);
                if ((n * 64) > water_threshold) {
                    c.material = Material.Water;
                }
                c.setHeight((int) (Math.random() * 24 + 1));
            }
        });
        
        Maze.buildMaze(cells, 4, 4, 24, 24);
        space = new Grid2DSpace(cells, nar);
        space.newWindow(1000, 800, true);
        cells.forEach(16, 16, 18, 18, new Hauto.SetMaterial(Material.DirtFloor));
        GridAgent a = new GridAgent(17, 17, nar) {
            public PVector lasttarget = new PVector(1, 1);

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
                if (target != lasttarget) {
                    getfeedback = true;
                }
                lasttarget = target;
                PVector current = new PVector(x, y);
                System.out.println(nextEffect);
                if (nextEffect == null) {
                    List<PVector> path = Grid2DSpace.Shortest_Path(space, this, current, target);
                    actions.clear();
                    System.out.println(path);
                    if (path != null) {
                        if (path.size() <= 1) {
                            nar.step(1);
                            System.out.println("at destination; didnt need to find path");
                            if (getfeedback && !"".equals(goal)) {
                                getfeedback = false;
                                nar.step(6);
                                if("pick".equals(opname)) {
                                    inventory.add(goal);
                                    GridObject remove=null;
                                    for(GridObject gridi : space.objects) {
                                        if(gridi instanceof Key && ((Key)gridi).doorname.equals(goal)) {
                                            remove=gridi;
                                            break;
                                        }
                                    }
                                    if(remove!=null)
                                        space.objects.remove(remove);
                                    nar.addInput("<(*,Self," + goal + ") --> hold>. :|:");
                                }
                                else
                                {
                                    nar.addInput("<(*,Self," + goal + ") --> at>. :|:");
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
        };
        Goto wu = new Goto(this, "^go-to");
        nar.memory.addOperator(wu);
        Pick wa = new Pick(this, "^pick");
        nar.memory.addOperator(wa);
        space.add(a);
//space.add(new QLAgent(10,20));
       // space.add(new Key(20, 20));
//space.add(new RayVision(a, 45, 10, 8));
    }

    public static void main(String[] arg) {
    }
}
