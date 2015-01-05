package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JPanel;
//import nars.core.NAR;
//
///**
// *
// * @author me
// */
//
//
//public class ConceptGraphPanel extends ProcessingGraphPanel {
//    public int mode = 0;
//
//    boolean showBeliefs = false;
//    private final ConceptGraphCanvas cg;
//
//
//    public ConceptGraphPanel(NAR n, ConceptGraphCanvas graphCanvas) {
//        super(n, graphCanvas);
//        this.cg =  graphCanvas;
//    }
//    
//    
//
//    @Override
//    protected void init() {
//        super.init();
//                
//        final JComboBox modeSelect = new JComboBox();
//        modeSelect.addItem("GridSort");
//        modeSelect.addItem("Circle Anim");
//        modeSelect.addItem("Circle Fixed");       
//        modeSelect.addItem("Grid");
//        modeSelect.setSelectedIndex(cg.mode);
//        modeSelect.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                cg.mode = modeSelect.getSelectedIndex();
//                cg.setUpdateNext();
//            }
//        });
//        menu.add(modeSelect);
//
//        final JCheckBox termlinkEnable = new JCheckBox("TermLinks");
//        termlinkEnable.setSelected(cg.showTermlinks);
//        termlinkEnable.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                cg.showTermlinks = (termlinkEnable.isSelected());
//                cg.setUpdateNext();
//            }
//        });
//        menu.add(termlinkEnable);        
//        
//        final JCheckBox taskLinkEnable = new JCheckBox("TaskLinks");
//        taskLinkEnable.setSelected(cg.showTasklinks);
//        taskLinkEnable.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                cg.showTasklinks = (taskLinkEnable.isSelected());
//                cg.setUpdateNext();
//            }
//        });
//        menu.add(taskLinkEnable);
//        
//        final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
//        beliefsEnable.setSelected(cg.showBeliefs);
//        beliefsEnable.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                cg.showBeliefs = (beliefsEnable.isSelected());
//                cg.setUpdateNext();
//            }
//        });
//        menu.add(beliefsEnable);
//        
//        menu.doLayout();
//    }
//    
//    
// 
//    
//}
