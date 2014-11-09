package nars.grid2d;

import nars.core.NAR;
import nars.grid2d.Cell.Logic;
import static nars.grid2d.Cell.Logic.AND;
import static nars.grid2d.Cell.Logic.BRIDGE;
import static nars.grid2d.Cell.Logic.NOT;
import static nars.grid2d.Cell.Logic.OFFSWITCH;
import static nars.grid2d.Cell.Logic.OR;
import static nars.grid2d.Cell.Logic.SWITCH;
import static nars.grid2d.Cell.Logic.WIRE;
import static nars.grid2d.Cell.Logic.XOR;
import nars.grid2d.Cell.Machine;
import nars.grid2d.Cell.Material;
import nars.grid2d.object.Key;
import nars.grid2d.object.Pizza;

public class Hauto {
    private final NAR nar;

    
    
    boolean is_logic(Cell c){ 
        return (c.logic==OR || c.logic==XOR || c.logic==AND || c.logic==NOT); 
    }
    
    public static int doornumber(Cell c) {
        if(c.material!=Material.Door) {
            return -2;
        }
        return Integer.parseInt(c.name.replaceAll("door", "").replaceAll("\\}", "").replaceAll("\\{", ""));
    }
    
    //put to beginning because we will need this one most often
    public void ExecutionFunction(int t,int i,int j,Cell w,Cell r,Cell left,Cell right,Cell up,
            Cell down,Cell left_up,Cell left_down,Cell right_up,Cell right_down,Cell[][] readcells)
    {
        w.charge=r.charge;
        w.value=r.value;
        w.value2=r.value2;
        w.is_solid=r.is_solid;
        w.chargeFront=false;
        //
        if((r.machine==Machine.Light || r.machine==Machine.Turret) && r.charge==1)
        {
            if(r.light!=1.0f) {
                boolean nope=false;
                if(r.machine==Machine.Turret) {
                    for(GridObject gr : TestChamber.space.objects) {
                        if(gr instanceof LocalGridObject) {
                            LocalGridObject o=(LocalGridObject) gr;
                            if(o.x==i && o.y==j) {
                                nope=true;
                            }
                        }
                    }
                    if(!nope) {
                        TestChamber.space.add(new Pizza((int)i, (int)j, "pizza"+entityID.toString()));
                        nar.addInput("<pizza"+entityID.toString()+" --> pizza>."); 
                        entityID++;
                    }
                }
                nar.addInput("<"+r.name+" --> on>. :|:");
            }
            w.light=1.0f;
            
            
        }
        else
        {
            w.light=NeighborsValue2("op_max", i, j, readcells, "get_light", (r.is_solid || r.material==Material.StoneWall) ? 1 : 0)/1.1f; //1.1
        }
        ///door
        if(r.material==Material.Door) {
            if(NeighborsValue2("op_or", i, j, readcells, "having_charge", 1.0f) != 0) {
                w.is_solid=false;
                if(r.is_solid) {
                    nar.addInput("<"+r.name+" --> opened>. :|:");
                }
            }
            else {
                if(!r.is_solid && TestChamber.keyn!=doornumber(r)) {
                    w.is_solid=true;
                    nar.addInput("(--,<"+r.name+" --> opened>). :|:");
                }
            }
        }
        //////// WIRE / CURRENT PULSE FLOW /////////////////////////////////////////////////////////////				
        if(r.logic==WIRE)
        {
            if(!r.chargeFront && r.charge==0.0 && NeighborsValue2("op_or", i, j, readcells, "having_charge", 1.0f) != 0) { 
                w.charge = 1.0f;
                w.chargeFront=true;    //it's on the front of the wave of change
            }   
            if(!r.chargeFront && r.charge==1.0 && NeighborsValue2("op_or", i, j, readcells, "having_charge", 0.0f) != 0)
            { 
                w.charge=0.0f;
                w.chargeFront=true;    //it's on the front of the wave of change
            }
            if(r.chargeFront==false && r.charge==0 && (up.logic==SWITCH || down.logic==SWITCH || (left.logic==SWITCH || (is_logic(left) && left.value==1)) || (right.logic==SWITCH || (is_logic(right) && right.value==1))))
            {
                w.charge=1.0f;
                w.chargeFront=true;   //it's on the front of the wave of change
            }
            if(r.chargeFront==false && r.charge==1 && (up.logic==OFFSWITCH || down.logic==OFFSWITCH || (left.logic==OFFSWITCH || (is_logic(left) && left.value==0)) || (right.logic==OFFSWITCH || (is_logic(right) && right.value==0))))
            {
                w.charge=0.0f;
                w.chargeFront=true;    //it's on the front of the wave of change
            }
        }
        //////////// LOGIC ELEMENTS ////////////////////////////////////////////////////////////////////	
        if(r.logic==Cell.Logic.NOT && (up.charge==0 || up.charge==1))
            w.value=(up.charge==0 ? 1 : 0); //eval state from input connection
        if(r.logic==Cell.Logic.NOT && (down.charge==0 || down.charge==1))
            w.value=(down.charge==0 ? 1 : 0); //eval state from input connection
        if(r.logic==Cell.Logic.AND)
            w.value=(up.charge==1 && down.charge==1) ? 1.0f : 0.0f; //eval state from input connections
        if(r.logic==Cell.Logic.OR)
            w.value=(up.charge==1 || down.charge==1) ? 1.0f : 0.0f; //eval state from input connections
        if(r.logic==Cell.Logic.XOR)
            w.value=(up.charge==1 ^ down.charge==1) ? 1.0f : 0.0f;  //eval state from input connections

        //ADD BIDIRECTIONAL LOGIC BRIDGE TO OVERCOME 2D TOPOLOGY
        if(r.logic==BRIDGE)
        {
            if(left.chargeFront && left.logic==WIRE)
                w.value=left.charge;
            else
            if(right.chargeFront && right.logic==WIRE)
                w.value=right.charge;
            
            if(up.chargeFront && up.logic==WIRE)
                w.value2=up.charge;
            else
            if(down.chargeFront && down.logic==WIRE)
                w.value2=down.charge;
        }
        
        if(!r.chargeFront && r.charge==0 && (((right.logic==BRIDGE && right.value==1) || (left.logic==BRIDGE && left.value==1)) || ((down.logic==BRIDGE && down.value2==1) || (up.logic==BRIDGE && up.value2==1))))
        {
            w.charge=1;
            w.chargeFront=true;
        }
        if(!r.chargeFront && r.charge==1 && (((right.logic==BRIDGE && right.value==0) || (left.logic==BRIDGE && left.value==0)) || ((down.logic==BRIDGE && down.value2==0) || (up.logic==BRIDGE && up.value2==0))))
        {
            w.charge=0;
            w.chargeFront=true;
        }
        
        if (r.logic == Cell.Logic.Load) {
            w.charge = Math.max(up.charge, Math.max(down.charge, Math.max(left.charge, right.charge)));
            w.chargeFront = false;
        }
        if(r.machine==Machine.Light || r.machine==Machine.Turret) {
            if(r.light==1.0f && w.light!=1.0f) { //changed
                nar.addInput("(--,<"+r.name+" --> on>). :|:");
            }
        }
            //w.charge *= w.conductivity;
    }
    
    String doorname="";
    public static Integer entityID=0;
    public static boolean allow_imitating=false;
    public void clicked(int x,int y, Grid2DSpace space)
    {
        if((int)x == 0 || (int) y==0 || (int)x == w-1 || (int) y==h-1)
            return;
        
        if(!doorname.equals("") && !doorname.contains("{")) {
            doorname="{"+doorname+"}";
        }
        
        if(oper.equals("perceive")) {
             readCells[(int) x][(int) y].name = "place"+entityID.toString();
            writeCells[(int) x][(int) y].name = "place"+entityID.toString();
            nar.addInput("<"+"place"+entityID.toString()+" --> place>.");
            if(TestChamber.curiousity) {
                space.nar.addInput("<(^go-to," + "place"+entityID.toString() + ") =/> <Self --> [curious]>>.");
            }
            entityID++;
            return;
        }
        
        if(!"".equals(oper)) {
            if(!"".equals(readCells[x][y].name) && !"pick".equals(oper)) {
                if(allow_imitating) {
                    nar.addInput("(^" + oper + ","+readCells[x][y].name+")! :|:"); //we will force the action
                }
                else {
                    nar.addInput("(^" + oper + ","+readCells[x][y].name+"). :|:");
                    TestChamber.operateObj(readCells[x][y].name, oper);
                }
                //nar.addInput("(^" + oper + ","+readCells[x][y].name+"). :|:"); //in order to make NARS an observer
                //--nar.step(1);
                //.operateObj(readCells[x][y].name, oper);
            }
            String s=TestChamber.getobj(x, y);
            if(!s.equals("")) {
                if(allow_imitating) {
                    nar.addInput("(^" + oper + ","+s+")! :|:"); 
                }
                else {
                    nar.addInput("(^" + oper + ","+s+"). :|:"); 
                    TestChamber.operateObj(s, oper);
                }
                //nar.addInput("(^" + oper + ","+s+"). :|:");
                //--nar.step(1);
               // TestChamber.operateObj(s, oper);
            }
            return;
        }
        
        if(!"".equals(wish)) {
            if(!"".equals(readCells[x][y].name)) {
                //nar.addInput("(^" + oper + ","+readCells[x][y].name+")!"); //we will force the action
                nar.addInput("<" + readCells[x][y].name+" --> "+wish+">! :|:"); //in order to make NARS an observer
                //--nar.step(1);
            }
            String s=TestChamber.getobj(x, y);
            if(!s.equals("")) {
                //nar.addInput("(^" + oper + ","+s+")!"); 
                nar.addInput("<" + s +" --> "+wish+">! :|:"); //in order to make NARS an observer
                //--nar.step(1);
            }
            return;
        }
        
        if(!"".equals(doorname) && selected.material==Material.Door) {
            space.add(new Key((int)x, (int)y, doorname.replace("door", "key")));
            nar.addInput("<"+doorname.replace("door", "key")+" --> key>.");
            if(TestChamber.curiousity) {
                space.nar.addInput("<(^go-to," +doorname.replace("door", "key") + ") =/> <Self --> [curious]>>.");
                space.nar.addInput("<(^pick," + doorname.replace("door", "key") + ") =/> <Self --> [curious]>>.");
            }
            doorname="";
            return;
        }
        if(selected.material==Material.Pizza) {
            doorname+="pizza"+entityID.toString();
        }
        if(!"".equals(doorname) && selected.material==Material.Pizza) {
            space.add(new Pizza((int)x, (int)y, doorname));
            nar.addInput("<"+doorname+" --> pizza>.");
            if(TestChamber.curiousity) {
                space.nar.addInput("<(^go-to," + doorname + ") =/> <Self --> [curious]>>.");
            }
            entityID++;
            doorname="";
            return;
        }
        if(!(selected.material==Material.Door) && !(selected.material==Material.Pizza))
            doorname="";
        
        readCells[(int) x][(int) y].charge = selected.charge;
        writeCells[(int) x][(int) y].charge = selected.charge;
        readCells[(int) x][(int) y].logic = selected.logic;
        writeCells[(int) x][(int) y].logic = selected.logic;
        readCells[(int) x][(int) y].material = selected.material;
        writeCells[(int) x][(int) y].material = selected.material;
        readCells[(int) x][(int) y].machine = selected.machine;
        writeCells[(int) x][(int) y].machine = selected.machine;
        
        if(selected.material==Material.Pizza || selected.material==Material.Door || selected.logic==Logic.OFFSWITCH || selected.logic==Logic.SWITCH || selected.machine==Machine.Light || selected.machine==Machine.Turret) //or other entity...
        {
            String name="";
            if(selected.material==Material.Door) {
                name="door";
            }
            if(selected.logic==Logic.SWITCH || selected.logic==Logic.OFFSWITCH) 
                name="switch";
            if(selected.machine==Machine.Light) 
                name="light";
            if(selected.machine==Machine.Turret) 
                name="firework";
            String Klass=name;
            name += (entityID.toString());
            if(selected.material==Material.Door) {
                doorname=name;
            }
            
            name="{"+name+"}";
            //if it has name already, dont allow overwrite

            if(readCells[(int) x][(int) y].name.equals("")) {
                nar.addInput("<"+name+" --> "+Klass+">.");
                readCells[(int) x][(int) y].name = name;
                writeCells[(int) x][(int) y].name = name;
                if(selected.logic==Logic.OFFSWITCH) {
                    nar.addInput("(--,<"+name+" --> "+"on>). :|:");
                    if(TestChamber.curiousity) {
                        space.nar.addInput("<(^go-to," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^activate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^deactivate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                    }
                }
                if(selected.logic==Logic.SWITCH) {
                    nar.addInput("<"+name+" --> "+"on>. :|:");
                    if(TestChamber.curiousity) {
                        space.nar.addInput("<(^go-to," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^activate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^deactivate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                    }
                }
            }
            else
            {
                if(selected.logic==Logic.OFFSWITCH) { //already has a name so use this one
                    nar.addInput("<"+readCells[(int) x][(int) y].name+" --> "+"off>. :|:");
                    if(TestChamber.curiousity) {
                        space.nar.addInput("<(^go-to," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^activate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^deactivate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                    }
                }
                if(selected.logic==Logic.SWITCH) {
                    nar.addInput("<"+readCells[(int) x][(int) y].name+" --> "+"on>. :|:");
                    if(TestChamber.curiousity) {
                        space.nar.addInput("<(^go-to," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^activate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                        space.nar.addInput("<(^deactivate," + readCells[(int) x][(int) y].name + ") =/> <Self --> [curious]>>.");
                    }
                }
            }
            
            entityID++;
        }
    }
    
    Cell selected=new Cell();
    String oper="";
    String label="";
    String wish="";

    public void click(String label, String oper, String wish) {
        this.label=label;
        this.oper=oper;
        this.wish=wish;
        if("".equals(label)) {
            return;
        }
        selected.is_solid=false;
        if("StoneWall".equals(label)) {
            selected.material = Material.StoneWall;     
            selected.is_solid=true;
            selected.logic=Logic.NotALogicBlock;
            selected.charge=0;
        }
        
        if("Water".equals(label)) {
            selected.material = Material.Water;     
            selected.is_solid=false;
            selected.logic=Logic.NotALogicBlock;
            selected.charge=0;
        }
        
        if("DirtFloor".equals(label)) {
            selected.material = Material.DirtFloor;     
            selected.is_solid=false;
            selected.logic=Logic.NotALogicBlock;
            selected.charge=0;
        }
        
        if("GrassFloor".equals(label)) {
            selected.material = Material.GrassFloor;     
            selected.is_solid=false;
            selected.logic=Logic.NotALogicBlock;
            selected.charge=0;
        }

        selected.machine = null;
        
        if("NOT".equals(label)) {
            selected.setLogic(Cell.Logic.NOT, 0);
        }
        if("AND".equals(label))
        {
            selected.setLogic(Cell.Logic.AND, 0);
        }
        if("OR".equals(label))
        {
            selected.setLogic(Cell.Logic.OR, 0);
        }
        if("XOR".equals(label))
        {
            selected.setLogic(Cell.Logic.XOR, 0);
        }
        if("bridge".equals(label))
        {
            selected.setLogic(Cell.Logic.BRIDGE, 0);
        }
        if("OnWire".equals(label))
        {
            selected.setLogic(Cell.Logic.WIRE, 1.0f);
            selected.chargeFront = true;
        }
        if("OffWire".equals(label))
        {
            selected.setLogic(Cell.Logic.WIRE, 0);
            selected.chargeFront = true;
        }
        if("onswitch".equals(label))
        {
            selected.setLogic(Cell.Logic.SWITCH, 1.0f);
        }
        if("offswitch".equals(label))
        {
            selected.setLogic(Cell.Logic.OFFSWITCH, 0);
        }

        if("Pizza".equals(label)) {
            selected.logic = Logic.NotALogicBlock;
            selected.material = Material.Pizza;
            selected.is_solid=false;
        }
        if("Door".equals(label)) {
            selected.logic = Logic.NotALogicBlock;
            selected.charge=0;
            selected.material = Material.Door;
            selected.is_solid=true;
        }
        if("Light".equals(label)) {
            selected.logic = Logic.Load;
            selected.material = Material.Machine;
            selected.machine = Machine.Light;
            selected.is_solid=true;
        }
        if("Turret".equals(label)) {
            selected.logic = Logic.Load;
            selected.material = Material.Machine;
            selected.machine = Machine.Turret;
            selected.is_solid=true;
        }
    
    }
    
    public float Neighbor_Value(Cell c,String mode,float data)
    {
        if("having_charge".equals(mode)) {
            if(c.logic!=Cell.Logic.WIRE)
                return -1.0f; //not a charge 
            return c.charge==data ? 1.0f : 0.0f; 
        }
        if("just_getcharge".equals(mode)) {
            return c.charge;
        }
        if("get_light".equals(mode) && (data==1 || !c.is_solid && !(c.material==Material.StoneWall))) {
            return Math.max(c.charge*0.2f, c.light);
        }
        return 0.0f;
    }

    final public static int RIGHT = -90;
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
    
    
    public Hauto(int w, int h, NAR nar) {
        this.nar=nar;
        this.w = w;
        this.h = h;
        readCells = new Cell[w][];
        writeCells = new Cell[w][];
        for (int i = 0; i < w; i++) {
            readCells[i] = new Cell[h];
            writeCells[i] = new Cell[h];
            for (int j = 0; j < h; j++) {
                CellState s = new CellState(i, j);
                readCells[i][j] = new Cell(s);
                writeCells[i][j] = new Cell(s);
                
                if ((i == 0) || (i == w-1))
                    readCells[i][j].setBoundary();
                else if ((j == 0) || (j == h-1))
                    readCells[i][j].setBoundary();
            }
        }
        
        copyReadToWrite();
        click("StoneWall","","");
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

    public Cell FirstNeighbor(int i, int j, Cell[][] readCells, String Condition, float data) {
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

    public float NeighborsValue(String op, int i, int j, Cell[][] readCells, String Condition, float data) {
        return Op(op, Op(op, Op(op, Op(op, Op(op, Op(op, Op(op, Neighbor_Value(readCells[i + 1][j], Condition, data), Neighbor_Value(readCells[i - 1][j], Condition, data)), Neighbor_Value(readCells[i + 1][j + 1], Condition, data)), Neighbor_Value(readCells[i - 1][j - 1], Condition, data)), Neighbor_Value(readCells[i][j + 1], Condition, data)), Neighbor_Value(readCells[i][j - 1], Condition, data)), Neighbor_Value(readCells[i - 1][j + 1], Condition, data)), Neighbor_Value(readCells[i + 1][j - 1], Condition, data));
    }

    public float NeighborsValue2(String op, int i, int j, Cell[][] readCells, String Condition, float data) {
        return Op(op, Op(op, Op(op, Neighbor_Value(readCells[i + 1][j], Condition, data), Neighbor_Value(readCells[i - 1][j], Condition, data)), Neighbor_Value(readCells[i][j + 1], Condition, data)), Neighbor_Value(readCells[i][j - 1], Condition, data));
    }
    
    public float Op(String op,float a,float b)
    {
        if("op_or".equals(op)) {
            return (a==1.0f || b==1.0f) ? 1.0f : 0.0f;
        }
        if("op_and".equals(op)) {
            return (a==1.0f && b==1.0f) ? 1.0f : 0.0f;
        }
        if("op_max".equals(op)) {
            return Math.max(a, b);
        }
        if("op_min".equals(op)) {
            return Math.min(a, b);
        }
        if("op_plus".equals(op)) {
            return a+b;
        }
        if("op_mul".equals(op)) {
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
