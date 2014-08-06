package nars.grid2d;

import java.util.List;
import nars.grid2d.Cell.Material;
import static nars.grid2d.Hauto.DOWN;
import static nars.grid2d.Hauto.LEFT;
import static nars.grid2d.Hauto.RIGHT;
import static nars.grid2d.Hauto.UP;
import nars.grid2d.agent.ql.QLAgent;
import nars.grid2d.map.Maze;
import nars.grid2d.object.Key;
import processing.core.PVector;


public class TestChamber {

    
    public static void main(String[] arg) {
        //NAR n = new NAR();
        
        int w = 50;
        int h = 50;
        int water_threshold=30;
        Hauto cells = new Hauto(w,h);
        cells.forEach(0,0,w,h, new CellFunction() {
            @Override  public void update(Cell c) {
                ///c.setHeight((int)(Math.random() * 12 + 1));
                float smoothness = 20f;
                
                c.material = Material.GrassFloor;
               double n = SimplexNoise.noise(c.state.x/smoothness, c.state.y/smoothness);
                if((n*64)>water_threshold) {
                    c.material=Material.Water;
                }
                c.setHeight( (int)(Math.random() * 24 + 1));
            }
        });
        
        Maze.buildMaze(cells, 4,4,24,24);
                
                
        final Grid2DSpace space = new Grid2DSpace(cells);
        space.newWindow(1000, 800, true);
        
        
        cells.forEach(16, 16, 18, 18, new Hauto.SetMaterial(Material.DirtFloor));
        
        GridAgent a = new GridAgent(17,17) {

            @Override
            public void update(Effect nextEffect) {
//                int a = 0;
//                if (Math.random() < 0.4) {
//                    int randDir = (int)(Math.random()*4);
//                    if (randDir == 0)             a = LEFT;
//                    else if (randDir == 1)        a = RIGHT;
//                    else if (randDir == 2)        a = UP;
//                    else if (randDir == 3)        a = DOWN;
//                    /*else if (randDir == 4)        a = UPLEFT;
//                    else if (randDir == 5)        a = UPRIGHT;
//                    else if (randDir == 6)        a = DOWNLEFT;
//                    else if (randDir == 7)        a = DOWNRIGHT;*/
//                    turn(a);
//                }

                
                /*if (Math.random() < 0.2) {
                    forward(1);
                }*/
                
                PVector current = new PVector(x, y);
                PVector target = new PVector(1,1);

                System.out.println(nextEffect);
                if (nextEffect == null) {
                    List<PVector> path = Grid2DSpace.Shortest_Path(space, this, current, target);

                    actions.clear();
                    
                    System.out.println(path);
                    if (path!=null) {
                        if (path.size() <= 1) {
                            System.out.println("at destination; didnt need to find path");
                        }
                        else {
                            int numSteps = Math.min(10, path.size());
                            float cx = x;
                            float cy = y;
                            for (int i = 1; i < numSteps; i++) {
                                PVector next = path.get(i);
                                int dx = (int)(next.x - cx);
                                int dy = (int)(next.y - cy);


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
        space.add(a);
        
        space.add(new QLAgent(10,20));
        
        space.add(new Key(1,1));
        
        space.add(new RayVision(a, 45, 10, 8));
    }
    
}
