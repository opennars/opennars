/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.narclear;

import java.awt.Color;
import java.awt.Graphics2D;
import nars.core.Memory;
import nars.grid2d.Cell;
import nars.grid2d.CellFunction;
import nars.grid2d.Grid2DSpace;
import nars.grid2d.Hauto;
import nars.grid2d.SimplexNoise;
import nars.grid2d.map.Maze;
import nars.narclear.jbox2d.j2d.DrawPhy2D;
import nars.narclear.jbox2d.j2d.DrawPhy2D.LayerDraw;

import org.jbox2d.dynamics.World;

/**
 *
 * @author me
 */
public class GridSpaceWorld extends RoverWorld implements LayerDraw {

    static Grid2DSpace newMazePlanet() {
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
    private DrawPhy2D draw;
    private Graphics2D graphics;

    public GridSpaceWorld(PhysicsModel p, Grid2DSpace g) {
        super(p);
        
        ((DrawPhy2D)p.draw()).addLayer(this);
        
        this.grid = g;
        
        w = grid.cells.w;
        h = grid.cells.h;
        
        //cell size
        cw = 6f;
        ch = cw;

        worldWidth = w * cw;
        worldHeight = w * ch;
        
        cells(new CellVisitor() {
            @Override public void cell(Cell c, float px, float py) {
                switch (c.material) {
                    case StoneWall:
                        addWall(px, py, cw/2f, ch/2f, 0f);
                        break;
                }
            }
        }, false);
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
    
    CellVisitor groundDrawer = new CellVisitor() {
        
        @Override public void cell(Cell c, float px, float py) {
            
            float h = c.height;
            
            switch (c.material) {
                case DirtFloor:                    
                    Color color = new Color(0.5f,c.height*0.01f, c.height*0.01f);
                    draw.drawSolidRect(px, py, cw, ch, color);
                    break;
                case GrassFloor:
                    Color color2 = new Color(0.1f,0.5f + c.height*0.01f, 0.1f);
                    draw.drawSolidRect(px, py, cw, ch, color2);                    
                    break;
                case Water:
                    float db = Memory.randomNumber.nextFloat()*0.04f;
                    
                    
                    float b = 0.5f + h*0.02f - db;
                    if (b < 0) b = 0; if (b > 1.0f) b = 1.0f;
                    
                    Color color3 = new Color(0.1f,0.1f,b);
                    draw.drawSolidRect(px, py, cw, ch, color3);
                    break;
            }
        }            
    };

    @Override
    public void drawGround(DrawPhy2D draw, World w) {
        this.draw = draw;
        this.graphics = draw.getGraphics();
        cells(groundDrawer, true);
    }

    @Override
    public void drawSky(DrawPhy2D draw, World w) {
    
    }
    
}
