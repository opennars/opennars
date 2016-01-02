/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.world;

import nars.rover.PhysicsModel;
import nars.rover.RoverWorld;
import nars.rover.physics.gl.JoglAbstractDraw;
import nars.rover.physics.j2d.SwingDraw;
import nars.rover.physics.j2d.SwingDraw.LayerDraw;
import nars.testchamber.map.Maze;
import nars.util.data.random.XorShift1024StarRandom;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.Random;

/**
 *
 * @author me
 */
public class GridSpaceWorld extends RoverWorld implements LayerDraw {

    public static Grid2DSpace newMazePlanet() {
        int w = 40;
        int h = 40;
        int water_threshold = 30;
        Hauto cells = new Hauto(w, h, null);
        
        cells.forEach(0, 0, w, h, new CellFunction() {
            @Override
            public void update(Cell c) {
///c.setHeight((int)(Math.random() * 12 + 1));
                float smoothness = 20f;
                c.material = Cell.Material.GrassFloor;
                double n = SimplexNoise.noise(c.state.x / smoothness, c.state.y / smoothness);
                if ((n * 64) > water_threshold) {
                    c.material = Cell.Material.Water;
                }
                c.setHeight((int) (Math.random() * 24 + 1));
            }
        });
        
        Maze.buildMaze(cells, 3, 3, 23, 23);
        
        return new Grid2DSpace(cells, null);                
    }
    
    private final Grid2DSpace grid;
    private final int w;
    private final int h;
    private final float cw;
    private final float ch;
    private final float worldWidth;
    private final float worldHeight;
    private JoglAbstractDraw draw;


    @Override
    public void init(PhysicsModel p) {
        super.init(p);

        ((JoglAbstractDraw)p.draw()).addLayer(this);

        cells(new CellVisitor() {
            @Override public void cell(Cell c, float px, float py) {
                switch (c.material) {
                    case StoneWall:
                        Body w = addWall(px, py, cw/2f, ch/2f, 0f);
                        w.setUserData(new SwingDraw.DrawProperty() {

                            @Override
                            public void before(Body b, SwingDraw d) {
                                d.setFillColor(null);
                                d.setStroke(null);
                            }

                            @Override public String toString() {
                                return "wall";
                            }

                        });
                        break;
                }
            }
        }, false);

    }

    public GridSpaceWorld(Grid2DSpace g) {
        super();

        this.grid = g;
        
        w = grid.cells.w;
        h = grid.cells.h;
        
        //cell size
        cw = 6f;
        ch = cw;

        worldWidth = w * cw;
        worldHeight = w * ch;
        
    }
    
    public interface CellVisitor {
        public void cell(Cell c, float px, float py);
    }
    
    public void cells(CellVisitor v, boolean onlyVisible) {
        float px, py;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                px = x * cw - worldWidth/2f;
                py = -(y * ch) + worldHeight/2f;
                
                Cell c = grid.cells.at(x, y);
                v.cell(c, px, py);
            }
        }
    }
    
    final CellVisitor groundDrawer = new CellVisitor() {

        final Random rng = new XorShift1024StarRandom(1);

        @Override public void cell(Cell c, float px, float py) {
            
            float h = c.height;
            
            
            switch (c.material) {
                case DirtFloor:                                        
                    draw.drawSolidRect(px, py, cw, ch, 0.5f,c.height*0.01f, c.height*0.01f);
                    break;
                case StoneWall:                    
                    draw.drawSolidRect(px, py, cw, ch, 0.75f, 0.75f, 0.75f);
                    break;
                case GrassFloor:                    
                    draw.drawSolidRect(px, py, cw, ch,0.1f,0.5f + c.height*0.005f, 0.1f);
                    break;
                case Water:
                    float db = (float)rng.nextFloat()*0.04f;
                    
                    
                    float b = 0.5f + h*0.01f - db;
                    if (b < 0) b = 0; if (b > 1.0f) b = 1.0f;


                    draw.drawSolidRect(px, py, cw, ch, 0.1f,0.1f,b);
                    break;
            }
        }            
    };

    @Override
    public void drawGround(JoglAbstractDraw draw, World w) {
        this.draw = draw;
        //this.graphics = draw.getGraphics();
        cells(groundDrawer, true);
    }

    @Override
    public void drawSky(JoglAbstractDraw draw, World w) {
    
    }
    
}
