package nars.grid2d;

class Hauto {

    public int t = 0;
    public Cell[][] readCells; //2D-array(**) of Cell objects(*)
    public Cell[][] writeCells; //2D-array(**) of Cell objects(*)
    public int n;
    
    float OFFCURRENT=0;
    float CURRENT=1;
    
    public Hauto(int n) {
        this.n = n;
        readCells = new Cell[n][];
        writeCells = new Cell[n][];
        for (int i = 0; i < n; i++) {
            readCells[i] = new Cell[n];
            writeCells[i] = new Cell[n];
            for (int j = 0; j < n; j++) {
                readCells[i][j] = new Cell();
                writeCells[i][j] = new Cell();
            }
        }
    }

    public void Exec() {
        this.t++;
        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
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
    
    public void ExecutionFunction(int t,int i,int j,Cell writeme,Cell readme,Cell left,Cell right,Cell up,
            Cell down,Cell left_up,Cell left_down,Cell right_up,Cell right_down,Cell[][] readcells)
    {
        writeme.state=readme.state;
        writeme.wavefront=0;
        //////// WIRE / CURRENT PULSE FLOW /////////////////////////////////////////////////////////////				
        if(readme.wavefront==0 && readme.state==OFFCURRENT && Hauto_OBJ_NeighborsValue("op_or", i, j, readcells, "being_a", CURRENT)!=0)
        { 
            writeme.state=CURRENT;
            writeme.wavefront=1;    //it's on the front of the wave of change
        } 
    }

    public float Neighbor_Value(Cell c,String mode,float data)
    {
        if(mode=="being_a") {
            return c.state==data ? 1.0f : 0.0f;
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
    
}
