package nars.grid2d;

import nars.grid2d.Cell.Material;
import nars.grid2d.map.Maze;
import nars.grid2d.object.Key;


public class TestChamber {

    
    public static void main(String[] arg) {
        //NAR n = new NAR();
        
        int w = 50;
        int h = 50;
        Hauto cells = new Hauto(w,h);
        cells.forEach(0,0,w,h, new CellFunction() {
            @Override  public void update(Cell c) {
                c.material = Material.DirtFloor;
                c.setHeight((int)(Math.random() * 12 + 1));
            }
        });
        
        Maze.buildMaze(cells, 4,4,18,18);
                
                
        Grid2DSpace v = new Grid2DSpace(cells);
        v.newWindow(1000, 800, true);
        
        
        cells.forEach(16, 16, 18, 18, new Hauto.SetMaterial(Material.DirtFloor));
        
        CellBody a = new CellBody(17,17);
        v.add(a);
        
        v.add(new Key(1,1));
        
        v.add(new RayVision(a, 45, 10, 8));
    }
    
}