package nars.testchamber;


public class RayVision implements GridObject {
    
    public final GridAgent body;

    public RayVision(GridAgent body, double focusAngle, double distance, int r) {
        this.body = body;
    }
    
    @Override
    public void init(Grid2DSpace p) {
        
    }
    
    @Override
    public void update(Effect nextEffect) {
        
    }

    @Override
    public void draw() {
    }
    
}
