//package nars.gui;
//
//import automenta.vivisect.swing.NWindow;
//import nars.NAR;
//import nars.concept.Concept;
//import nars.gui.output.graph.nengo.GraphPanelNengo;
//import nars.util.graph.DerivationTree;
//import nars.util.graph.TermLinkGraph;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
///**
// * Created by me on 5/15/15.
// */
//public class ConceptMenu extends JPopupMenu {
//
//    abstract public static class ConceptMenuItem extends JMenuItem implements ActionListener, Runnable {
//        private final Concept concept;
//
//        public ConceptMenuItem(Concept c, String label) {
//            super(label);
//            this.concept = c;
//            addActionListener(this);
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            SwingUtilities.invokeLater(this);
//        }
//    }
//
//    public ConceptMenu(NAR n, Concept c) {
//        super();
//
//        /*
//        add(new ConceptMenuItem(c, "Explain") {
//            @Override
//            public void run() {
//                new NWindow(c.toString() + " Explanation @ " + n.time(),
//                        new MessagePanel(
//                                ...explanations...
//                        )).show(500, 400);
//            }
//        });
//        */
//
//        add(new ConceptMenuItem(c, "TermLink Graph") {
//            @Override
//            public void run() {
//                /*new NWindow(c.toString() + " TermLinks @ " + n.time(),
//                        new JGraphXGraphPanel(
//                                new TermLinkGraph().add(c, true)
//                        )).show(500, 400);*/
//                new NWindow(c.toString() + " TermLinks (1st Level, Outgoing)",
//                        new GraphPanelNengo(
//                                new TermLinkGraph().add(c, true)
//                        )).show(500, 400);
//            }
//        });
//        add(new ConceptMenuItem(c, "Derivation Tree") {
//            @Override
//            public void run() {
//                /*new NWindow(c.toString() + " Derivations",
//                        new JGraphXGraphPanel(
//                                new DerivationTree().add(c, 4)
//                        )).show(500, 400);*/
//                new NWindow(c.toString() + " Derivations",
//                        new GraphPanelNengo(
//                                new DerivationTree().add(c, 4)
//                        )).show(500, 400);
//            }
//        });
//
//        addSeparator();
//
//        add(new ConceptMenuItem(c, "Delete") {
//            @Override
//            public void run() {
//                c.delete();
//            }
//        });
//
//    }
// }
