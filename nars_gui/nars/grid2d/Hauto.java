package nars.grid2d;

public class Hauto {

    final public static int RIGHT = -9;
    final public static int DOWN = 180;
    final public static int LEFT = 90;
    final public static int UP = 0;
    final public static int UPLEFT = (UP+LEFT)/2;
    final public static int UPRIGHT = (UP+RIGHT)/2;
    final public static int DOWNLEFT = (DOWN+LEFT)/2;
    final public static int DOWNRIGHT = (DOWN+RIGHT)/2;
    
    public int t = 0;
    public Cell[][] readCells; //2D-array(**) of Cell objects(*)
    public Cell[][] writeCells; //2D-array(**) of Cell objects(*)
    public final int w;
    public final int h;
    
    public static int irand(int max) {
        return (int)(Math.random()*max);
    }
    
    
    public Hauto(int w, int h) {
        this.w = w;
        this.h = h;
        readCells = new Cell[w][];
        writeCells = new Cell[w][];
        for (int i = 0; i < w; i++) {
            readCells[i] = new Cell[h];
            writeCells[i] = new Cell[h];
            for (int j = 0; j < h; j++) {
                readCells[i][j] = new Cell();
                writeCells[i][j] = new Cell();
                
                if ((i == 0) || (i == w-1))
                    readCells[i][j].setBoundary();
                else if ((j == 0) || (j == h-1))
                    readCells[i][j].setBoundary();
            }
        }
        
        copyReadToWrite();
    }

    
    public void copyReadToWrite() {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                writeCells[i][j].copyFrom(readCells[i][j]);                
            }
        }
    }

    public void Exec() {
        this.t++;
        for (int i = 1; i < this.w - 1; i++) {
            for (int j = 1; j < this.h - 1; j++) {
                ExecutionFunction(this.t, i, j, this.writeCells[i][j], this.readCells[i][j], this.readCells[i - 1][j], this.readCells[i + 1][j], this.readCells[i][j + 1], this.readCells[i][j - 1], this.readCells[i - 1][j + 1], this.readCells[i - 1][j - 1], this.readCells[i + 1][j + 1], this.readCells[i + 1][j - 1], this.readCells);
            }
        }
        //change write to read and read to write
        Cell[][] temp2 = this.readCells;
        this.readCells = this.writeCells;
        this.writeCells = temp2;
    }

    public Cell Hauto_OBJ_FirstNeighbor(int i, int j, Cell[][] readCells, String Condition, float data) {
        int k;
        int l;
        for (k = i - 1; k <= i + 1; k++) {
            for (l = j - 1; l <= j + 1; l++) {
                if (!(k == i && j == l)) {
                    if (Neighbor_Value(readCells[k][l], Condition, data) != 0) {
                        return readCells[k][l];
                    }
                }
            }
        }
        return null;
    }

    public float Hauto_OBJ_NeighborsValue(String op, int i, int j, Cell[][] readCells, String Condition, float data) {
        return Op(op, Op(op, Op(op, Op(op, Op(op, Op(op, Op(op, Neighbor_Value(readCells[i + 1][j], Condition, data), Neighbor_Value(readCells[i - 1][j], Condition, data)), Neighbor_Value(readCells[i + 1][j + 1], Condition, data)), Neighbor_Value(readCells[i - 1][j - 1], Condition, data)), Neighbor_Value(readCells[i][j + 1], Condition, data)), Neighbor_Value(readCells[i][j - 1], Condition, data)), Neighbor_Value(readCells[i - 1][j + 1], Condition, data)), Neighbor_Value(readCells[i + 1][j - 1], Condition, data));
    }

    public float Hauto_OBJ_NeighborsValue2(String op, int i, int j, Cell[][] readCells, String Condition, float data) {
        return Op(op, Op(op, Op(op, Neighbor_Value(readCells[i + 1][j], Condition, data), Neighbor_Value(readCells[i - 1][j], Condition, data)), Neighbor_Value(readCells[i][j + 1], Condition, data)), Neighbor_Value(readCells[i][j - 1], Condition, data));
    }
    
    public void ExecutionFunction(int t,int i,int j,Cell w,Cell r,Cell left,Cell right,Cell up,
            Cell down,Cell left_up,Cell left_down,Cell right_up,Cell right_down,Cell[][] readcells)
    {
        w.charge=r.charge;
        w.chargeFront=false;
        //////// WIRE / CURRENT PULSE FLOW /////////////////////////////////////////////////////////////				
        float neighborCharge = Hauto_OBJ_NeighborsValue("op_or", i, j, readcells, "being_a", 1.0f);
        if(r.chargeFront==false && r.charge==0.0 && neighborCharge > 0) { 
            w.charge = 1.0f;
            w.chargeFront=true;    //it's on the front of the wave of change
        } 
        //w.charge *= w.conductivity;
    }

    public float Neighbor_Value(Cell c,String mode,float data)
    {
        if(mode=="being_a") {
            //return c.charge==data ? 1.0f : 0.0f;
            return c.charge;
        }
        return 0.0f;
    }
    public float Op(String op,float a,float b)
    {
        if(op=="op_or") {
            return (float) Math.min(1.0, a+b);
        }
        if(op=="op_and") {
            return (float) Math.min(1.0, a*b);
        }
        if(op=="op_plus") {
            return a+b;
        }
        if(op=="op_mul") {
            return a*b;
        }
        return 0.0f;
    }

    public void forEach(int x1, int y1, int x2, int y2, CellFunction c) {
        x1 = Math.max(1, x1);
        x2 = Math.min(w-1, x2);
        x2 = Math.max(x1, x2);
        y1 = Math.max(1, y1);
        y2 = Math.min(h-1, y2);
        y2 = Math.max(y1, y2);
        
        for (int tx = x1; tx < x2; tx++)
           for (int ty = y1; ty < y2; ty++) {
               c.update(readCells[tx][ty]);
           }
        copyReadToWrite();
    }

    public void at(int x, int y, CellFunction c) {
        c.update(readCells[x][y]);
        copyReadToWrite();
    }
    
    public Cell at(int x, int y) {
        return readCells[x][y];
    }
    
    public static class SetMaterial implements CellFunction {
        private final Cell.Material material;

        public SetMaterial(Cell.Material m) {
            this.material = m;
        }
        
        @Override
        public void update(Cell cell) {
            cell.material = material;
            cell.height = (material == Cell.Material.StoneWall) ? 64 : 1;
        }
        
    }    
}
