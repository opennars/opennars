package nars.grid2d;


public class TestChamber {

    
    public static void main(String[] arg) {
        //NAR n = new NAR();
        
        Hauto cells = new Hauto(30,20);
        cells.forEach(0,0,30,20, new CellFunction() {
            @Override  public void update(Cell c) {
                c.setHeight((int)(Math.random() * 60 + 64));
            }
        });
                
                
        Grid2DSpace v = new Grid2DSpace(cells);
        v.newWindow(1000, 800, true);
        
        
        
        CellAgent a = new CellAgent();
        v.add(a);
    }
    
}