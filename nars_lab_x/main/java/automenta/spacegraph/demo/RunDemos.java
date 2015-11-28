///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo;
//
//import automenta.netention.demo.Demo;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.netention.demo.spacegraph.DemoRectTilt;
//import automenta.netention.demo.swing.RunAbout;
//import automenta.netention.demo.swing.RunDetailEdit;
//import automenta.netention.demo.swing.RunDialogPanel;
//import automenta.netention.demo.swing.RunFinanceGraph;
//import automenta.netention.demo.swing.RunSelfBrowser;
//import automenta.netention.demo.swing.RunSelfGraphPanel;
//import automenta.netention.demo.swing.RunTextCutup;
//import java.awt.BorderLayout;
//import java.awt.EventQueue;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
///**
// *
// * @author seh
// */
//public class RunDemos extends JPanel {
//
//    private JPanel demoPanel;
//    private final JTextArea descriptionArea;
//
//    public RunDemos(Collection<Demo> demos) {
//        super(new BorderLayout());
//
//        JPanel buttonColumn = new JPanel();
//        buttonColumn.setLayout(new GridBagLayout());
//
//        GridBagConstraints gc = new GridBagConstraints();
//
//        gc.gridx = 1;
//        gc.gridy = 1;
//        gc.weightx = 1.0;
//        gc.weighty = 0.1;
//        gc.fill = gc.BOTH;
//        gc.anchor = gc.NORTH;
//
//        for (final Demo d : demos) {
//            final JButton b = new JButton(d.getName());
//            b.addActionListener(new ActionListener() {
//
//                @Override public void actionPerformed(ActionEvent e) {
//                    setDemo(b, d);
//                }
//            });
//            buttonColumn.add(b, gc);
//            gc.gridy++;
//        }
//
//        add(new JScrollPane(buttonColumn), BorderLayout.WEST);
//
//        descriptionArea = new JTextArea();
//        descriptionArea.setLineWrap(true);
//        descriptionArea.setWrapStyleWord(true);
//        //descriptionArea.setRows(3);
//        descriptionArea.setFont(descriptionArea.getFont().deriveFont((float) (descriptionArea.getFont().getSize() * 1.5f)));
//        add(/*new JScrollPane(*/descriptionArea, BorderLayout.NORTH);
//    }
//
//    public void setDemo(JButton b, final Demo d) {
//        if (demoPanel != null) {
//            remove(demoPanel);
//        }
//
//        demoPanel = null;
//
//        if (d != null) {
//            demoPanel = d.newPanel();
//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override public void run() {
//                    descriptionArea.setText(d.getClass().getSimpleName() + ": " + d.getDescription());
//                    add(demoPanel, BorderLayout.CENTER);
//                    updateUI();
//                }
//            });
//        } else {
//            updateUI();
//        }
//
//
//    }
//
//    public static void main(String[] args) {
//        final List<Demo> demos = new LinkedList();
//        demos.add(new RunAbout());
//        demos.add(new RunTextCutup());
//        demos.add(new RunSelfBrowser());
//        demos.add(new RunDetailEdit());
//        demos.add(new RunFinanceGraph());
//        demos.add(new RunSelfGraphPanel());
//        //demos.add(new RunSpikingGraph());
//        demos.add(new RunDialogPanel());
//        //demos.add(new RunGraphMix());
//        demos.add(new DemoRectTilt());
//        //        demos.add(new Demo("Self Browser", "..") {
//        //            @Override public JPanel newPanel() {
//        //                return RunSelfBrowser.newPanel();
//        //            }
//        //        });
//        //        demos.add(new Demo("Detail Editing", "..") {
//        //            @Override public JPanel newPanel() {
//        //                return RunDetailEdit.newPanel();
//        //            }
//        //        });
//        //        demos.add(new Demo("Hyperassociative Finance", "..") {
//        //            @Override public JPanel newPanel() {
//        //                return RunFinanceGraph.newPanel();
//        //            }
//        //        });
//
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                    //UIManager.setLookAndFeel(new SubstanceMagellanLookAndFeel());
//                    //UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
//                    //UIManager.setLookAndFeel(new SubstanceMistAquaLookAndFeel());
//
//                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//                } catch (Exception ex) {
//                    System.err.println(ex);
//                }
//                SwingWindow w = new SwingWindow(new RunDemos(demos), 900, 700, true);
//
//                w.setTitle("Netention - Demos");
//            }
//        });
//
//    }
//}
