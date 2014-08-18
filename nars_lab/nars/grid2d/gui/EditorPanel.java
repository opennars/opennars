package nars.grid2d.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nars.grid2d.Cell;
import nars.grid2d.Cell.Logic;
import nars.grid2d.Grid2DSpace;
import nars.grid2d.GridObject;
import nars.grid2d.LocalGridObject;
import nars.grid2d.TestChamber;
import nars.grid2d.object.Key;

/**
 *
 * @author me
 */


public class EditorPanel extends JPanel {

    abstract public static class EditorMode extends DefaultMutableTreeNode {

        public EditorMode(String label) {
            super(label);
        }
        
        abstract public void run();
    }
    
    public EditorPanel(final Grid2DSpace s) {
        super(new BorderLayout());
        
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        
        DefaultMutableTreeNode structMenu = new DefaultMutableTreeNode("Structural");
        root.add(structMenu);
        
        DefaultMutableTreeNode logicMenu = new DefaultMutableTreeNode("Logic");
        root.add(logicMenu);

        DefaultMutableTreeNode machineMenu = new DefaultMutableTreeNode("Machine");
        root.add(machineMenu);
        
        DefaultMutableTreeNode actionMenu = new DefaultMutableTreeNode("Force Action");
        root.add(actionMenu);
        
         DefaultMutableTreeNode goalMenu = new DefaultMutableTreeNode("Request Goal");
        root.add(goalMenu);
        
         DefaultMutableTreeNode knowMenu = new DefaultMutableTreeNode("Predefine knowledge");
        root.add(knowMenu);
        
        
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        
        final JTree toolTree = new JTree(model);       
        toolTree.expandRow(0);
        add(toolTree, BorderLayout.CENTER);
        
        toolTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
                Object o = toolTree.getLastSelectedPathComponent();
                if (o instanceof EditorMode) {
                    EditorMode m = (EditorMode)o;
                    m.run();
                }
            }            
        });
        
        structMenu.add(new EditorMode("Stone Wall") {
            @Override public void run() { s.cells.click("StoneWall","",""); }            
        });
        structMenu.add(new EditorMode("Dirt Floor") {
            @Override public void run() { s.cells.click("DirtFloor","",""); }            
        });
         structMenu.add(new EditorMode("Grass Floor") {
            @Override public void run() { s.cells.click("GrassFloor","",""); }            
        });
         
                structMenu.add(new EditorMode("Water") {
            @Override public void run() { s.cells.click("Water","",""); }            
        });
        
        
        logicMenu.add(new EditorMode("On Wire") {
            @Override public void run() { s.cells.click("OnWire","",""); }            
        });
        logicMenu.add(new EditorMode("Off Wire") {
            @Override public void run() { s.cells.click("OffWire","",""); }            
        });
        logicMenu.add(new EditorMode("And") {
            @Override public void run() { s.cells.click("AND","",""); }
        });
        logicMenu.add(new EditorMode("Or") {
            @Override public void run() { s.cells.click("OR","",""); }
        });
        logicMenu.add(new EditorMode("Xor") {
            @Override public void run() { s.cells.click("XOR","",""); }
        });
        logicMenu.add(new EditorMode("Not") {
            @Override public void run() { s.cells.click("NOT","",""); }
        });        
        logicMenu.add(new EditorMode("Bridge") {
            @Override public void run() { s.cells.click("bridge","",""); }
        });
        logicMenu.add(new EditorMode("Off Switch") {
            @Override public void run() { s.cells.click("offswitch","",""); }
        });
        logicMenu.add(new EditorMode("On Switch") {
            @Override public void run() { s.cells.click("onswitch","",""); }
        });        
        
        machineMenu.add(new EditorMode("Light") {
            @Override public void run() { s.cells.click("Light","",""); }
        });        
        //since firework doesnt serve a special functionality yet
        /*machineMenu.add(new EditorMode("Firework") {
            @Override public void run() { s.cells.click("Turret","",""); }
        }); */   
        
        machineMenu.add(new EditorMode("Door and Key") {
            @Override public void run() { s.cells.click("Door","",""); }
        });  
        

        
        actionMenu.add(new EditorMode("Go-To named") {
            
            @Override public void run() { s.cells.click("","go-to","");}
        });  
        
        
        actionMenu.add(new EditorMode("Pick named") {  
            @Override public void run() { s.cells.click("","pick","");}
        }); 
        
        actionMenu.add(new EditorMode("activate switch") {  
            @Override public void run() { s.cells.click("","activate","");}
        });  
        
        actionMenu.add(new EditorMode("deactivate switch") {  
            @Override public void run() { s.cells.click("","deactivate","");}
        });  
        
        actionMenu.add(new EditorMode("perceive/name") {  
            @Override public void run() { s.cells.click("","perceive","");}
        });  
        
        
        goalMenu.add(new EditorMode("Be curious") {
            
            @Override public void run() { 
                for(GridObject g : s.objects) {
                    if(g instanceof LocalGridObject) {
                        LocalGridObject obi=(LocalGridObject) g;
                        if(obi instanceof Key) {
                            s.nar.addInput("<(^go-to,"+obi.doorname+") =/> <Self --> [curious]>>.");
                            s.nar.addInput("<(^pick,"+obi.doorname+") =/> <Self --> [curious]>>.");
                        }
                    }
                }
                for(int i=0;i<s.cells.w;i++) {
                    for(int j=0;j<s.cells.h;j++) {
                        if(s.cells.readCells[i][j].name.startsWith("switch") || s.cells.readCells[i][j].name.startsWith("key")) {
                            s.nar.addInput("<(^go-to,"+s.cells.readCells[i][j].name+") =/> <Self --> [curious]>>.");
                        }
                        if(s.cells.readCells[i][j].logic==Logic.SWITCH || s.cells.readCells[i][j].logic==Logic.OFFSWITCH) {
                            s.nar.addInput("<(^activate,"+s.cells.readCells[i][j].name+") =/> <Self --> [curious]>>.");
                            s.nar.addInput("<(^deactivate,"+s.cells.readCells[i][j].name+") =/> <Self --> [curious]>>.");
                        }
                    }
                }
                s.nar.addInput("<Self --> [curious]>!");
                s.nar.addInput("<Self --> [curious]>!");
                s.nar.addInput("<Self --> [curious]>!");
                s.nar.addInput("<Self --> [curious]>!");
                s.nar.addInput("<Self --> [curious]>!");
            }
            
        });  
        
        goalMenu.add(new EditorMode("be somewhere") {  
            @Override public void run() { s.cells.click("","","at");}
        });  
        
        goalMenu.add(new EditorMode("hold something") {  
            @Override public void run() { s.cells.click("","","hold");}
        });  
        
        goalMenu.add(new EditorMode("make switched on") {  
            @Override public void run() { s.cells.click("","","on");}
        });  
        
        goalMenu.add(new EditorMode("make switched off") {  
            @Override public void run() { s.cells.click("","","off");}
        });   
        
        goalMenu.add(new EditorMode("make opened") {  
            @Override public void run() { s.cells.click("","","opened");}
        });  
        
        goalMenu.add(new EditorMode("make closed") {  
            @Override public void run() { s.cells.click("","","closed");}
        });  
        
        
        
        knowMenu.add(new EditorMode("if you go to somewhere you will be there") {  
            @Override public void run() { s.nar.addInput("<(^go-to,$1) =/> <$1 --> at>>."); }
        });  
        
        
         knowMenu.add(new EditorMode("if you are somewhere and you pick whats there, you will hold it") {  
            @Override public void run() { /*s.nar.addInput("<(&/,<$1 --> at>,(^pick,$1)) =/> <$1 --> hold>>."); */
            for(GridObject g : s.objects) {
                if(g instanceof LocalGridObject) {
                    LocalGridObject obi=(LocalGridObject) g;
                    if(obi instanceof Key) {
                        s.nar.addInput("<(&/,<"+ obi.doorname+" --> at>,(^pick,"+obi.doorname+")) =/> <"+obi.doorname+" --> hold>>.");
                    }
                }
            }
            /*s.nar.addInput("<(&/,<key0 --> at>,(^pick,key0)) =/> <key0 --> hold>>.");
            s.nar.addInput("<(&/,<key1 --> at>,(^pick,key1)) =/> <key1 --> hold>>.");
            s.nar.addInput("<(&/,<key2 --> at>,(^pick,key2)) =/> <key2 --> hold>>.");
            s.nar.addInput("<(&/,<key3 --> at>,(^pick,key3)) =/> <key3 --> hold>>.");*/}
        });  //s.nar.addInput("<(&/,<$1 --> at>,(^pick,$1)) =/> <$1 --> hold>>.");
        
        
       /* knowMenu.add(new EditorMode("every key opens a door") {  
            @Override public void run() { }
        });  
        
        knowMenu.add(new EditorMode("for every door there exists a key which opens the door") {  
            @Override public void run() { s.nar.addInput(""); }
        });  */
        
        

        
        
        
    }
    
    
}
