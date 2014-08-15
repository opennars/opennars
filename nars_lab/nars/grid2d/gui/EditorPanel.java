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
        
        DefaultMutableTreeNode actionMenu = new DefaultMutableTreeNode("Actions");
        root.add(actionMenu);
        
        root.add(new DefaultMutableTreeNode("Tools"));
        
        
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
            @Override public void run() { s.cells.click("StoneWall",""); }            
        });
        structMenu.add(new EditorMode("Dirt Floor") {
            @Override public void run() { s.cells.click("DirtFloor",""); }            
        });
         structMenu.add(new EditorMode("Grass Floor") {
            @Override public void run() { s.cells.click("GrassFloor",""); }            
        });
        
        
        logicMenu.add(new EditorMode("On Wire") {
            @Override public void run() { s.cells.click("OnWire",""); }            
        });
        logicMenu.add(new EditorMode("Off Wire") {
            @Override public void run() { s.cells.click("OffWire",""); }            
        });
        logicMenu.add(new EditorMode("And") {
            @Override public void run() { s.cells.click("AND",""); }
        });
        logicMenu.add(new EditorMode("Or") {
            @Override public void run() { s.cells.click("OR",""); }
        });
        logicMenu.add(new EditorMode("Xor") {
            @Override public void run() { s.cells.click("XOR",""); }
        });
        logicMenu.add(new EditorMode("Not") {
            @Override public void run() { s.cells.click("NOT",""); }
        });        
        logicMenu.add(new EditorMode("Bridge") {
            @Override public void run() { s.cells.click("bridge",""); }
        });
        logicMenu.add(new EditorMode("Off Switch") {
            @Override public void run() { s.cells.click("offswitch",""); }
        });
        logicMenu.add(new EditorMode("On Switch") {
            @Override public void run() { s.cells.click("onswitch",""); }
        });        
        
        machineMenu.add(new EditorMode("Light") {
            @Override public void run() { s.cells.click("Light",""); }
        });        
        machineMenu.add(new EditorMode("Firework") {
            @Override public void run() { s.cells.click("Turret",""); }
        });    
        
        machineMenu.add(new EditorMode("Door and Key") {
            @Override public void run() { s.cells.click("Door",""); }
        });  
        

        
        
        actionMenu.add(new EditorMode("Go-To named") {
            
            @Override public void run() { s.cells.click("","go-to");}
        });  
        
        
        actionMenu.add(new EditorMode("Pick named") {
            
            @Override public void run() { s.cells.click("","pick");}
        });  
        
        
    }
    
    
}
