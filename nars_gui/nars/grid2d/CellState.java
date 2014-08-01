package nars.grid2d;




public class CellState {
    public float light;
    public int x, y;

    public CellState(int x, int y) {
        this.x = x;
        this.y = y;

        this.light = 0;
    }
    
}
