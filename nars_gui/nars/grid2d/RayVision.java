package nars.grid2d;


public class RayVision implements GridObject {
    
    public final CellBody body;

    public RayVision(CellBody body, double focusAngle, double distance, int r) {
        this.body = body;
    }
    
    @Override
    public void update(Grid2DSpace p) {
        
    }

    @Override
    public void draw(Grid2DSpace p) {
    }
    
}
