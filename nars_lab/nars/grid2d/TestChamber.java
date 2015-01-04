package nars.grid2d;

import java.util.List;
import nars.core.Build;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.grid2d.Cell.Logic;
import nars.grid2d.Cell.Material;
import static nars.grid2d.Hauto.DOWN;
import static nars.grid2d.Hauto.LEFT;
import static nars.grid2d.Hauto.RIGHT;
import static nars.grid2d.Hauto.UP;
import nars.grid2d.map.Maze;
import nars.grid2d.object.Key;
import nars.grid2d.operator.Activate;
import nars.grid2d.operator.Deactivate;
import nars.grid2d.operator.Goto;
import nars.grid2d.operator.Pick;
import nars.gui.NARSwing;
import nars.plugin.app.plan.TemporalParticlePlanner;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import processing.core.PVector;

public class TestChamber {

    public static boolean staticInformation=false;
    //TIMING
    static int narUpdatePeriod = 20; /*milliseconds */
    int gridUpdatePeriod = 2;
    int automataPeriod = 2;
    int agentPeriod = 2;
    static long guiUpdateTime = 25; /* milliseconds */
        
    //OPTIONS
    public static boolean curiousity=false;

    
    public static void main(String[] args) {
        Build builder = new Default();
        
        //set NAR architecture parameters:
        //builder...
        Parameters.DEFAULT_JUDGMENT_DURABILITY=0.99f; //try to don't forget the input in TestChamber domain
        NAR nar = builder.build();
        //set NAR runtime parmeters:  

        for(NAR.PluginState pluginstate : nar.getPlugins()) {
            if(pluginstate.plugin instanceof InternalExperience || pluginstate.plugin instanceof FullInternalExperience) {
                nar.removePlugin(pluginstate);
            }
        }
  
        nar.addPlugin(new TemporalParticlePlanner());
        
        //(nar.param).duration.set(10);
        (nar.param).noiseLevel.set(0); 
        new NARSwing(nar);

        new TestChamber(nar);
                
        nar.start(narUpdatePeriod);   
        
    }

    
    
    static Grid2DSpace space;
    
    public PVector lasttarget = new PVector(5, 25); //not work
    public static boolean executed_going=false;
    static String goal = "";
    static String opname="";
    public static LocalGridObject inventorybag=null;  
    public static int keyn=-1;

    
    public static String getobj(int x,int y) {
        for(GridObject gridi : space.objects) {
            if(gridi instanceof LocalGridObject) { //Key && ((Key)gridi).doorname.equals(goal)) {
                LocalGridObject gridu=(LocalGridObject) gridi;
                if(gridu.x==x && gridu.y==y && !"".equals(gridu.doorname))
                    return gridu.doorname;
            }
        }
        return "";
    }
    
    public static void operateObj(String arg,String opnamer) {
        opname=opnamer;
        Hauto cells = space.cells;
        goal = arg;
        for (int i = 0; i < cells.w; i++) {
            for (int j = 0; j < cells.h; j++) {
                if (cells.readCells[i][j].name.equals(arg)) {
                    if(opname.equals("go-to"))
                        space.target = new PVector(i, j);
                }
            }
        }
        //if("pick".equals(opname)) {
            for(GridObject gridi : space.objects) {
                if(gridi instanceof LocalGridObject && ((LocalGridObject)gridi).doorname.equals(goal)) { //Key && ((Key)gridi).doorname.equals(goal)) {
                    LocalGridObject gridu=(LocalGridObject) gridi;
                    if(opname.equals("go-to"))
                        space.target = new PVector(gridu.x, gridu.y);
                }
            }
        //}
    }
    boolean invalid=false;
    public static boolean active=true;
    public static boolean executed=false;
    public static boolean needpizza=false;
    public static int hungry=250;
    public List<PVector> path=null;

    public TestChamber() {
        super();        
    }
    
    public TestChamber(NAR nar) {
        this(nar, true);
    }
    
    public TestChamber(NAR nar, boolean showWindow) {
        super();

        int w = 50;
        int h = 50;
        int water_threshold = 30;
        Hauto cells = new Hauto(w, h, nar);
        cells.forEach(0, 0, w, h, new CellFunction() {
            @Override
            public void update(Cell c) {
///c.setHeight((int)(Math.random() * 12 + 1));
                float smoothness = 20f;
                c.material = Material.GrassFloor;
                double n = SimplexNoise.noise(c.state.x / smoothness, c.state.y / smoothness);
                if ((n * 64) > water_threshold) {
                    c.material = Material.Water;
                }
                c.setHeight((int) (Math.random() * 24 + 1));
            }
        });
        
        Maze.buildMaze(cells, 3, 3, 23, 23);
        
        space = new Grid2DSpace(cells, nar);
        space.FrameRate = 0;
        space.automataPeriod = automataPeriod/gridUpdatePeriod;
        space.agentPeriod = agentPeriod/gridUpdatePeriod;
        TestChamber into=this;
        nar.memory.event.on(Events.FrameEnd.class, new EventObserver() {
            private long lastDrawn = 0;
            
            @Override
            public void event(Class event, Object... arguments) {           
                if (nar.time() % gridUpdatePeriod == 0) {
                    space.update(into);
                    
                    long now = System.nanoTime();
                    if (now - lastDrawn > guiUpdateTime*1e6) {
                        space.redraw();                    
                        lastDrawn = now;
                    }
                }
            }
        });

        if (showWindow)
            space.newWindow(1000, 800, true);
        cells.forEach(16, 16, 18, 18, new Hauto.SetMaterial(Material.DirtFloor));
        GridAgent a = new GridAgent(17, 17, nar) {

            @Override
            public void update(Effect nextEffect) {
                if(active) {
                    //nar.stop();
                    executed=false;
                    if(path==null || path.size()<=0 && !executed_going) {
                        for (int i = 0; i < 5; i++) { //make thinking in testchamber bit faster
                            //nar.step(1);
                            if (executed) {
                                break;
                            }
                        }
                    }
                    if(needpizza) {
                        hungry--;
                        if(hungry<0) {
                            hungry=250;
                            nar.addInput("(&&,<#1 --> pizza>,<#1 --> [at]>)!"); //also works but better:
                            /*for (GridObject obj : space.objects) {
                                if (obj instanceof Pizza) {
                                    nar.addInput("<" + ((Pizza) obj).doorname + "--> at>!");
                                }
                            }*/
                        }
                    }
                }
// int a = 0;
// if (Math.random() < 0.4) {
// int randDir = (int)(Math.random()*4);
// if (randDir == 0) a = LEFT;
// else if (randDir == 1) a = RIGHT;
// else if (randDir == 2) a = UP;
// else if (randDir == 3) a = DOWN;
// /*else if (randDir == 4) a = UPLEFT;
// else if (randDir == 5) a = UPRIGHT;
// else if (randDir == 6) a = DOWNLEFT;
// else if (randDir == 7) a = DOWNRIGHT;*/
// turn(a);
// }
/*if (Math.random() < 0.2) {
                 forward(1);
                 }*/
                lasttarget = space.target;
                space.current = new PVector(x, y);
               // System.out.println(nextEffect);
                if (nextEffect == null) {
                    path = Grid2DSpace.Shortest_Path(space, this, space.current, space.target);
                    actions.clear();
                   // System.out.println(path);
                    if(path==null) {
                        executed_going=false;
                    }
                    if (path != null) {
                        if(inventorybag!=null) {
                            inventorybag.x=(int)space.current.x;
                            inventorybag.y=(int)space.current.y;
                            inventorybag.cx=(int)space.current.x;
                            inventorybag.cy=(int)space.current.y;
                        }
                        if(inventorybag==null || !(inventorybag instanceof Key)) {
                            keyn=-1;
                        }
                        if (path.size() <= 1) {
                            active=true;
                            executed_going=false;
                            //System.out.println("at destination; didnt need to find path");
                            if (!"".equals(goal) && space.current.equals(space.target)) {
                                //--nar.step(6);
                                GridObject obi=null;
                                if(!"".equals(opname)) {
                                    for(GridObject gridi : space.objects) {
                                        if(gridi instanceof LocalGridObject && ((LocalGridObject)gridi).doorname.equals(goal) &&
                                          ((LocalGridObject)gridi).x==(int)space.current.x &&
                                               ((LocalGridObject)gridi).y==(int)space.current.y ) {
                                            obi=gridi;
                                            break;
                                        }
                                    }
                                }
                                if(obi!=null || cells.readCells[(int)space.current.x][(int)space.current.y].name.equals(goal)) { //only possible for existing ones
                                    if("pick".equals(opname)) {
                                        if(inventorybag!=null && inventorybag instanceof LocalGridObject) {
                                            //we have to drop it
                                            LocalGridObject ob=(LocalGridObject) inventorybag;
                                            ob.x=(int)space.current.x;
                                            ob.y=(int)space.current.y;
                                            space.objects.add(ob);
                                        }
                                        inventorybag=(LocalGridObject)obi;
                                        if(obi!=null) {
                                            space.objects.remove(obi);
                                            if(inventorybag.doorname.startsWith("{key")) {
                                                keyn=Integer.parseInt(inventorybag.doorname.replaceAll("key", "").replace("}", "").replace("{", ""));
                                                for(int i=0;i<cells.h;i++) {
                                                    for(int j=0;j<cells.w;j++) {
                                                        if(Hauto.doornumber(cells.readCells[i][j])==keyn) {
                                                            cells.readCells[i][j].is_solid=false;
                                                            cells.writeCells[i][j].is_solid=false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        //nar.addInput("<"+goal+" --> hold>. :|:");
                                    }
                                    else
                                    if("deactivate".equals(opname)) {
                                        for(int i=0;i<cells.h;i++) {
                                            for(int j=0;j<cells.w;j++) {
                                                if(cells.readCells[i][j].name.equals(goal)) {
                                                    if(cells.readCells[i][j].logic==Logic.SWITCH) {
                                                        cells.readCells[i][j].logic=Logic.OFFSWITCH;
                                                        cells.writeCells[i][j].logic=Logic.OFFSWITCH;
                                                        cells.readCells[i][j].charge=0.0f;
                                                        cells.writeCells[i][j].charge=0.0f;
                                                        //nar.addInput("<"+goal+" --> off>. :|:");
                                                    }
                                                }
                                            }
                                        }
                                        
                                    }
                                    else
                                    if("activate".equals(opname)) {
                                        for(int i=0;i<cells.h;i++) {
                                            for(int j=0;j<cells.w;j++) {
                                                if(cells.readCells[i][j].name.equals(goal)) {
                                                    if(cells.readCells[i][j].logic==Logic.OFFSWITCH) {
                                                        cells.readCells[i][j].logic=Logic.SWITCH;
                                                        cells.writeCells[i][j].logic=Logic.SWITCH;
                                                        cells.readCells[i][j].charge=1.0f;
                                                        cells.writeCells[i][j].charge=1.0f;
                                                        //nar.addInput("<"+goal+" --> on>. :|:");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if("go-to".equals(opname)) {
                                        executed_going=false;
                                        nar.addInput("<"+goal+" --> [at]>. :|:");
                                        if (goal.startsWith("{pizza")) {
                                            GridObject ToRemove = null;
                                            for (GridObject obj : space.objects) { //remove pizza
                                                if (obj instanceof LocalGridObject) {
                                                    LocalGridObject obo = (LocalGridObject) obj;
                                                    if (obo.doorname.equals(goal)) {
                                                        ToRemove = obj;
                                                    }
                                                }
                                            }
                                            if (ToRemove != null) {
                                                space.objects.remove(ToRemove);
                                            }
                                            hungry=500;
                                            //nar.addInput("<"+goal+" --> eat>. :|:"); //that is sufficient:
                                            nar.addInput("<"+goal+" --> [at]>. :|:");
                                        }
                                        active=true;
                                    }
                                }
                            }
                            opname="";
                            nar.memory.setEnabled(true);
                            /*if(!executed && !executed_going)
                                nar.step(1);*/
                        } else {
                            //nar.step(10);
                            //nar.memory.setEnabled(false); 
                            
                            executed_going=true;
                            active=false;
                            //nar.step(1);
                            int numSteps = Math.min(10, path.size());
                            float cx = x;
                            float cy = y;
                            for (int i = 1; i < numSteps; i++) {
                                PVector next = path.get(i);
                                int dx = (int) (next.x - cx);
                                int dy = (int) (next.y - cy);
                                if ((dx == 0) && (dy == 1)) {
                                    turn(UP);
                                    forward(1);
                                }
                                if ((dx == 1) && (dy == 0)) {
                                    turn(RIGHT);
                                    forward(1);
                                }
                                if ((dx == -1) && (dy == 0)) {
                                    turn(LEFT);
                                    forward(1);
                                }
                                if ((dx == 0) && (dy == -1)) {
                                    turn(DOWN);
                                    forward(1);
                                }
                                cx = next.x;
                                cy = next.y;
                            }
                        }
                    }
                }
            }
        };
        Goto wu = new Goto(this, "^go-to");
        nar.memory.addOperator(wu);
        Pick wa = new Pick(this, "^pick");
        nar.memory.addOperator(wa);
        Activate waa = new Activate(this, "^activate");
        nar.memory.addOperator(waa);
        Deactivate waaa = new Deactivate(this, "^deactivate");
        nar.memory.addOperator(waaa);
        space.add(a);

    }


}