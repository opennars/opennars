package nars.grid2d.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nars.grid2d.Grid2DSpace;
import nars.grid2d.TestChamber;

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
        machineMenu.add(new EditorMode("Firework") {
            @Override public void run() { s.cells.click("Turret","",""); }
        });    
        
        machineMenu.add(new EditorMode("Door and Key") {
            @Override public void run() { s.cells.click("Door","",""); }
        });  
        

        
        
        actionMenu.add(new EditorMode("Go-To named") {
            
            @Override public void run() { s.cells.click("","go-to","");}
        });  
        
        
        actionMenu.add(new EditorMode("Pick named") {  
            @Override public void run() { s.cells.click("","pick","");}
        }); 
        
        actionMenu.add(new EditorMode("switch on/open") {  
            @Override public void run() { s.cells.click("","open","");}
        });  
        
        actionMenu.add(new EditorMode("switch off/close") {  
            @Override public void run() { s.cells.click("","close","");}
        });  
        
        actionMenu.add(new EditorMode("perceive/name") {  
            @Override public void run() { s.cells.click("","perceive","");}
        });  
        
        
        goalMenu.add(new EditorMode("be somewhere") {  
            @Override public void run() { s.cells.click("","","at");}
        });  
        
        goalMenu.add(new EditorMode("hold something") {  
            @Override public void run() { s.cells.click("","","hold");}
        });  
        
        goalMenu.add(new EditorMode("make switched on/opened") {  
            @Override public void run() { s.cells.click("","","opened");}
        });  
        
        goalMenu.add(new EditorMode("maked switched off/closed") {  
            @Override public void run() { s.cells.click("","","closed");}
        });   
        
        
        
        knowMenu.add(new EditorMode("if you go to somewhere you will be there") {  
            @Override public void run() { s.nar.addInput("<(^go-to,$1) =/> <$1 --> at>>."); }
        });  
        
        
         knowMenu.add(new EditorMode("if you are somewhere and you pick whats there, you will hold it") {  
            @Override public void run() { /*s.nar.addInput("<(&/,<$1 --> at>,(^pick,$1)) =/> <$1 --> hold>>."); */
            s.nar.addInput("<(&/,<key0 --> at>,(^pick,key0)) =/> <key0 --> hold>>.");
            s.nar.addInput("<(&/,<key1 --> at>,(^pick,key1)) =/> <key1 --> hold>>.");
            s.nar.addInput("<(&/,<key2 --> at>,(^pick,key2)) =/> <key2 --> hold>>.");
            s.nar.addInput("<(&/,<key3 --> at>,(^pick,key3)) =/> <key3 --> hold>>.");}
        });  //s.nar.addInput("<(&/,<$1 --> at>,(^pick,$1)) =/> <$1 --> hold>>.");
        
        
       /* knowMenu.add(new EditorMode("every key opens a door") {  
            @Override public void run() { }
        });  
        
        knowMenu.add(new EditorMode("for every door there exists a key which opens the door") {  
            @Override public void run() { s.nar.addInput(""); }
        });  */
        
        

        
        
        
    }
    
    
}
