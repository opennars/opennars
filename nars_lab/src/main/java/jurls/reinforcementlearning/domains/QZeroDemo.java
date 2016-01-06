///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.reinforcementlearning.domains;
//
//import jurls.core.utils.Physics2D;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.List;
//import javax.swing.AbstractAction;
//import javax.swing.Action;
//import javax.swing.Timer;
//import jurls.core.LearnerAndActor;
//import jurls.core.approximation.ApproxParameters;
//import jurls.core.approximation.Generator;
//import jurls.core.approximation.ParameterizedFunction;
//import jurls.core.approximation.ParameterizedFunctionGenerator;
//import jurls.core.approximation.Scalar;
//import jurls.core.brain.Brain;
//import jurls.core.reinforcementlearning.ActionSelector;
//import jurls.core.reinforcementlearning.ByQActionSelector;
//import jurls.core.reinforcementlearning.EpsilonGreedyActionSelector;
//import jurls.core.reinforcementlearning.QZeroAgent;
//import jurls.core.reinforcementlearning.QZeroParameters;
//
///**
// *
// * @author thorsten
// */
//public class QZeroDemo extends javax.swing.JFrame {
//
//    private Physics2D physics2D;
//    private Point agentPoint;
//    private Point pendulumPoint;
//    private LearnerAndActor agent;
//    private int numIterationsPerPeriod = 1;
//    private final ApproxParameters approxParameters = new ApproxParameters(0.01, 0.1);
//    private final EpsilonGreedyActionSelector.Parameters asParameters = new EpsilonGreedyActionSelector.Parameters(0.01);
//    private final QZeroParameters qZeroParameters = new QZeroParameters(0.9, 0.9);
//    private final Brain.Parameters brainParameters = new Brain.Parameters(0.1);
//    private int numPhysicsIterations = 0;
//
//    private final Action menuAction = new AbstractAction() {
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            newAgent();
//        }
//    };
//
//    /**
//     * Creates new form RLDemo
//     */
//    public QZeroDemo() {
//        initComponents();
//
//        physics2D = new Physics2D(0.1, 300);
//        agentPoint = new Point(400, 300, 0, 0, 0.99, 0);
//        pendulumPoint = new Point(401, 50, 0, 0, 0.99, 0.99);
//        Connection c = new Connection(250, agentPoint, pendulumPoint);
//        physics2D.points.add(agentPoint);
//        physics2D.points.add(pendulumPoint);
//        physics2D.connections.add(c);
//        physicsRenderer1.physics2D = physics2D;
//
//        approxParameters.setAlpha(Double.parseDouble(alphaTextField.getText()));
//        approxParameters.setMomentum(Double.parseDouble(momentumTextField.getText()));
//        asParameters.setEpsilon(Double.parseDouble(qEpsilonTextField.getText()));
//        qZeroParameters.setAlpha(Double.parseDouble(qAlphaTextField.getText()));
//        qZeroParameters.setGamma(Double.parseDouble(gammaTextField.getText()));
//        brainParameters.epsilon = Double.parseDouble(brainEpsilonTextField.getText());
//
//        newAgent();
//
//        new Timer(20, new ActionListener() {
//            double[] previousState = observe();
//            int action = 0;
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                for (int i = 0; i < numIterationsPerPeriod; ++i) {
//                    double a = action - 0.5;
//
//                    agentPoint.vx += 2 * a;
//                    physics2D.step(1);
//                    if (agentPoint.x < 50) {
//                        agentPoint.x = 50;
//                    }
//                    if (agentPoint.x > 750) {
//                        agentPoint.x = 750;
//                    }
//                    double[] nextState = observe();
//                    action = agent.learnAndAction(
//                            observe(),
//                            300.0 - pendulumPoint.y,
//                            previousState,
//                            action
//                    );
//                    previousState = nextState;
//                    numPhysicsIterations++;
//                }
//
//                physicsRenderer1.repaint();
//                jTextArea1.setText(agent.getDebugString());
//                debugLabel.setText("physics iterations : " + numPhysicsIterations);
//           }
//        }).start();
//    }
//
//    private void newAgent() {
//        double[] s0 = observe();
//        numPhysicsIterations = 0;
//
//        int nf = 0;
//        if (features1MenuItem.isSelected()) {
//            nf = 1;
//        }
//        if (features2MenuItem.isSelected()) {
//            nf = 2;
//        }
//        if (features3MenuItem.isSelected()) {
//            nf = 3;
//        }
//        if (features4MenuItem.isSelected()) {
//            nf = 4;
//        }
//        if (features5MenuItem.isSelected()) {
//            nf = 5;
//        }
//        if (features6MenuItem.isSelected()) {
//            nf = 6;
//        }
//
//        ParameterizedFunctionGenerator pfg = null;
//        if (fourierBasisMenuItem.isSelected()) {
//            pfg = Generator.generateGradientFourierBasis(approxParameters, nf);
//        }
//        if (atanFFNNMenuItem.isSelected()) {
//            pfg = Generator.generateGradientATanFFNN(approxParameters, nf);
//        }
//        if (cnfFunctionMenuItem.isSelected()) {
//            pfg = Generator.generateCNFFunction(15, 10);
//        }
//
//        ActionSelector as = null;
//        if (epsilonGreedyMenuItem.isSelected()) {
//            as = new EpsilonGreedyActionSelector(asParameters);
//        }
//        if (byQMenuItem.isSelected()) {
//            as = new ByQActionSelector();
//        }
//
//        agent = null;
//        if (qZeroMenuItem.isSelected()) {
//            agent = new QZeroAgent(qZeroParameters, pfg, as, s0, 2);
//        }
//        else if (brainMenuItem.isSelected()) {
//            agent = new Brain(
//                    s0.length,
//                    nf,
//                    2,
//                    qZeroParameters,
//                    asParameters,
//                    pfg,
//                    brainParameters
//            );
//        }
//    }
//
//    private double[] observe() {
//        return new double[]{
//            agentPoint.x,
//            agentPoint.vx,
//            pendulumPoint.x,
//            pendulumPoint.y,
//            pendulumPoint.vx,
//            pendulumPoint.vy
//        };
//    }
//
//    /**
//     * This method is called from within the constructor to initialize the form.
//     * WARNING: Do NOT modify this code. The content of this method is always
//     * regenerated by the Form Editor.
//     */
//    @SuppressWarnings("unchecked")
//    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
//    private void initComponents() {
//
//        buttonGroup1 = new javax.swing.ButtonGroup();
//        approximatorButtonGroup = new javax.swing.ButtonGroup();
//        actionSelectorButtonGroup = new javax.swing.ButtonGroup();
//        numFeaturesButtonGroup = new javax.swing.ButtonGroup();
//        agentButtonGroup = new javax.swing.ButtonGroup();
//        jTabbedPane1 = new javax.swing.JTabbedPane();
//        jPanel3 = new javax.swing.JPanel();
//        physicsRenderer1 = new jurls.core.utils.PhysicsRenderer();
//        jPanel2 = new javax.swing.JPanel();
//        jPanel8 = new javax.swing.JPanel();
//        jRadioButton1 = new javax.swing.JRadioButton();
//        jRadioButton3 = new javax.swing.JRadioButton();
//        jRadioButton2 = new javax.swing.JRadioButton();
//        jPanel9 = new javax.swing.JPanel();
//        jLabel1 = new javax.swing.JLabel();
//        alphaTextField = new javax.swing.JTextField();
//        jLabel2 = new javax.swing.JLabel();
//        momentumTextField = new javax.swing.JTextField();
//        jLabel9 = new javax.swing.JLabel();
//        qAlphaTextField = new javax.swing.JTextField();
//        jLabel10 = new javax.swing.JLabel();
//        gammaTextField = new javax.swing.JTextField();
//        jLabel3 = new javax.swing.JLabel();
//        qEpsilonTextField = new javax.swing.JTextField();
//        jLabel4 = new javax.swing.JLabel();
//        brainEpsilonTextField = new javax.swing.JTextField();
//        debugLabel = new javax.swing.JLabel();
//        jPanel4 = new javax.swing.JPanel();
//        jScrollPane1 = new javax.swing.JScrollPane();
//        jTextArea1 = new javax.swing.JTextArea();
//        jPanel5 = new javax.swing.JPanel();
//        jScrollPane2 = new javax.swing.JScrollPane();
//        jTextArea2 = new javax.swing.JTextArea();
//        jPanel6 = new javax.swing.JPanel();
//        jScrollPane3 = new javax.swing.JScrollPane();
//        jTextArea3 = new javax.swing.JTextArea();
//        jMenuBar1 = new javax.swing.JMenuBar();
//        jMenu4 = new javax.swing.JMenu();
//        qZeroMenuItem = new javax.swing.JRadioButtonMenuItem();
//        brainMenuItem = new javax.swing.JRadioButtonMenuItem();
//        jMenu1 = new javax.swing.JMenu();
//        fourierBasisMenuItem = new javax.swing.JRadioButtonMenuItem();
//        atanFFNNMenuItem = new javax.swing.JRadioButtonMenuItem();
//        cnfFunctionMenuItem = new javax.swing.JRadioButtonMenuItem();
//        jMenu2 = new javax.swing.JMenu();
//        epsilonGreedyMenuItem = new javax.swing.JRadioButtonMenuItem();
//        byQMenuItem = new javax.swing.JRadioButtonMenuItem();
//        jMenu3 = new javax.swing.JMenu();
//        features1MenuItem = new javax.swing.JRadioButtonMenuItem();
//        features2MenuItem = new javax.swing.JRadioButtonMenuItem();
//        features3MenuItem = new javax.swing.JRadioButtonMenuItem();
//        features4MenuItem = new javax.swing.JRadioButtonMenuItem();
//        features5MenuItem = new javax.swing.JRadioButtonMenuItem();
//        features6MenuItem = new javax.swing.JRadioButtonMenuItem();
//
//        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
//
//        jTabbedPane1.setPreferredSize(new java.awt.Dimension(800, 600));
//
//        jPanel3.setLayout(new java.awt.BorderLayout());
//
//        javax.swing.GroupLayout physicsRenderer1Layout = new javax.swing.GroupLayout(physicsRenderer1);
//        physicsRenderer1.setLayout(physicsRenderer1Layout);
//        physicsRenderer1Layout.setHorizontalGroup(
//            physicsRenderer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGap(0, 0, Short.MAX_VALUE)
//        );
//        physicsRenderer1Layout.setVerticalGroup(
//            physicsRenderer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGap(0, 263, Short.MAX_VALUE)
//        );
//
//        jPanel3.add(physicsRenderer1, java.awt.BorderLayout.CENTER);
//
//        jPanel2.setLayout(new java.awt.BorderLayout());
//
//        buttonGroup1.add(jRadioButton1);
//        jRadioButton1.setText("Training 500 Mode");
//        jRadioButton1.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                jRadioButton1ActionPerformed(evt);
//            }
//        });
//        jPanel8.add(jRadioButton1);
//
//        buttonGroup1.add(jRadioButton3);
//        jRadioButton3.setText("Training 50 Mode");
//        jRadioButton3.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                jRadioButton3ActionPerformed(evt);
//            }
//        });
//        jPanel8.add(jRadioButton3);
//
//        buttonGroup1.add(jRadioButton2);
//        jRadioButton2.setSelected(true);
//        jRadioButton2.setText("Run Mode");
//        jRadioButton2.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                jRadioButton2ActionPerformed(evt);
//            }
//        });
//        jPanel8.add(jRadioButton2);
//
//        jPanel2.add(jPanel8, java.awt.BorderLayout.CENTER);
//
//        jPanel9.setLayout(new java.awt.GridLayout(0, 4));
//
//        jLabel1.setText("Approximator Learning Rate (Alpha)");
//        jPanel9.add(jLabel1);
//
//        alphaTextField.setText("0.01");
//        alphaTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                alphaTextFieldActionPerformed(evt);
//            }
//        });
//        jPanel9.add(alphaTextField);
//
//        jLabel2.setText("Approximator Momentum");
//        jPanel9.add(jLabel2);
//
//        momentumTextField.setText("0.5");
//        momentumTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                momentumTextFieldActionPerformed(evt);
//            }
//        });
//        jPanel9.add(momentumTextField);
//
//        jLabel9.setText("Q Learning Rate (Alpha)");
//        jPanel9.add(jLabel9);
//
//        qAlphaTextField.setText("0.9");
//        qAlphaTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                qAlphaTextFieldActionPerformed(evt);
//            }
//        });
//        jPanel9.add(qAlphaTextField);
//
//        jLabel10.setText("Q Farsight (Gamma)");
//        jPanel9.add(jLabel10);
//
//        gammaTextField.setText("0.9");
//        gammaTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                gammaTextFieldActionPerformed(evt);
//            }
//        });
//        jPanel9.add(gammaTextField);
//
//        jLabel3.setText("Q Randomness (Epsilon)");
//        jPanel9.add(jLabel3);
//
//        qEpsilonTextField.setText("0");
//        qEpsilonTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                qEpsilonTextFieldActionPerformed(evt);
//            }
//        });
//        jPanel9.add(qEpsilonTextField);
//
//        jLabel4.setText("Brain Randomness (Epsilon)");
//        jPanel9.add(jLabel4);
//
//        brainEpsilonTextField.setText("0");
//        brainEpsilonTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                brainEpsilonTextFieldActionPerformed(evt);
//            }
//        });
//        jPanel9.add(brainEpsilonTextField);
//
//        jPanel2.add(jPanel9, java.awt.BorderLayout.PAGE_START);
//
//        jPanel3.add(jPanel2, java.awt.BorderLayout.PAGE_END);
//
//        debugLabel.setText("jLabel5");
//        jPanel3.add(debugLabel, java.awt.BorderLayout.NORTH);
//
//        jTabbedPane1.addTab("Demo", jPanel3);
//
//        jPanel4.setLayout(new java.awt.BorderLayout());
//
//        jTextArea1.setColumns(20);
//        jTextArea1.setRows(5);
//        jScrollPane1.setViewportView(jTextArea1);
//
//        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);
//
//        jTabbedPane1.addTab("Debug 1", jPanel4);
//
//        jPanel5.setLayout(new java.awt.BorderLayout());
//
//        jTextArea2.setColumns(20);
//        jTextArea2.setRows(5);
//        jScrollPane2.setViewportView(jTextArea2);
//
//        jPanel5.add(jScrollPane2, java.awt.BorderLayout.CENTER);
//
//        jTabbedPane1.addTab("Debug 2", jPanel5);
//
//        jPanel6.setLayout(new java.awt.BorderLayout());
//
//        jTextArea3.setColumns(20);
//        jTextArea3.setRows(5);
//        jScrollPane3.setViewportView(jTextArea3);
//
//        jPanel6.add(jScrollPane3, java.awt.BorderLayout.CENTER);
//
//        jTabbedPane1.addTab("Debug 3", jPanel6);
//
//        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);
//
//        jMenu4.setText("Agent");
//
//        qZeroMenuItem.setAction(menuAction);
//        agentButtonGroup.add(qZeroMenuItem);
//        qZeroMenuItem.setText("Q(0)");
//        qZeroMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
//            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                qZeroMenuItemStateChanged(evt);
//            }
//        });
//        jMenu4.add(qZeroMenuItem);
//
//        brainMenuItem.setAction(menuAction);
//        agentButtonGroup.add(brainMenuItem);
//        brainMenuItem.setSelected(true);
//        brainMenuItem.setText("Brain");
//        brainMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
//            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                brainMenuItemStateChanged(evt);
//            }
//        });
//        jMenu4.add(brainMenuItem);
//
//        jMenuBar1.add(jMenu4);
//
//        jMenu1.setText("Approximator");
//
//        fourierBasisMenuItem.setAction(menuAction);
//        approximatorButtonGroup.add(fourierBasisMenuItem);
//        fourierBasisMenuItem.setSelected(true);
//        fourierBasisMenuItem.setText("Fourier Basis");
//        fourierBasisMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
//            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                fourierBasisMenuItemStateChanged(evt);
//            }
//        });
//        jMenu1.add(fourierBasisMenuItem);
//
//        atanFFNNMenuItem.setAction(menuAction);
//        approximatorButtonGroup.add(atanFFNNMenuItem);
//        atanFFNNMenuItem.setText("ATan FeedFowardNeuralNet");
//        atanFFNNMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
//            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                atanFFNNMenuItemStateChanged(evt);
//            }
//        });
//        jMenu1.add(atanFFNNMenuItem);
//
//        cnfFunctionMenuItem.setAction(menuAction);
//        approximatorButtonGroup.add(cnfFunctionMenuItem);
//        cnfFunctionMenuItem.setText("CNF Function");
//        cnfFunctionMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
//            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                cnfFunctionMenuItemStateChanged(evt);
//            }
//        });
//        cnfFunctionMenuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                cnfFunctionMenuItemActionPerformed(evt);
//            }
//        });
//        jMenu1.add(cnfFunctionMenuItem);
//
//        jMenuBar1.add(jMenu1);
//
//        jMenu2.setText("ActionSelector");
//
//        epsilonGreedyMenuItem.setAction(menuAction);
//        actionSelectorButtonGroup.add(epsilonGreedyMenuItem);
//        epsilonGreedyMenuItem.setSelected(true);
//        epsilonGreedyMenuItem.setText("Epsilon Greedy");
//        jMenu2.add(epsilonGreedyMenuItem);
//
//        byQMenuItem.setAction(menuAction);
//        actionSelectorButtonGroup.add(byQMenuItem);
//        byQMenuItem.setText("By Q");
//        jMenu2.add(byQMenuItem);
//
//        jMenuBar1.add(jMenu2);
//
//        jMenu3.setText("No. Features");
//
//        features1MenuItem.setAction(menuAction);
//        numFeaturesButtonGroup.add(features1MenuItem);
//        features1MenuItem.setText("1");
//        jMenu3.add(features1MenuItem);
//
//        features2MenuItem.setAction(menuAction);
//        numFeaturesButtonGroup.add(features2MenuItem);
//        features2MenuItem.setText("2");
//        jMenu3.add(features2MenuItem);
//
//        features3MenuItem.setAction(menuAction);
//        numFeaturesButtonGroup.add(features3MenuItem);
//        features3MenuItem.setText("3");
//        jMenu3.add(features3MenuItem);
//
//        features4MenuItem.setAction(menuAction);
//        numFeaturesButtonGroup.add(features4MenuItem);
//        features4MenuItem.setText("4");
//        jMenu3.add(features4MenuItem);
//
//        features5MenuItem.setAction(menuAction);
//        numFeaturesButtonGroup.add(features5MenuItem);
//        features5MenuItem.setSelected(true);
//        features5MenuItem.setText("5");
//        jMenu3.add(features5MenuItem);
//
//        features6MenuItem.setAction(menuAction);
//        numFeaturesButtonGroup.add(features6MenuItem);
//        features6MenuItem.setText("6");
//        jMenu3.add(features6MenuItem);
//
//        jMenuBar1.add(jMenu3);
//
//        setJMenuBar(jMenuBar1);
//
//        pack();
//    }// </editor-fold>//GEN-END:initComponents
//
//    private void jRadioButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
//        numIterationsPerPeriod = 500;
//    }//GEN-LAST:event_jRadioButton1ActionPerformed
//
//    private void jRadioButton2ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
//        numIterationsPerPeriod = 1;
//    }//GEN-LAST:event_jRadioButton2ActionPerformed
//
//    private void jRadioButton3ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
//        numIterationsPerPeriod = 50;
//    }//GEN-LAST:event_jRadioButton3ActionPerformed
//
//    private void alphaTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_alphaTextFieldActionPerformed
//        approxParameters.setAlpha(Double.parseDouble(alphaTextField.getText()));
//    }//GEN-LAST:event_alphaTextFieldActionPerformed
//
//    private void momentumTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_momentumTextFieldActionPerformed
//        approxParameters.setMomentum(Double.parseDouble(momentumTextField.getText()));
//    }//GEN-LAST:event_momentumTextFieldActionPerformed
//
//    private void qAlphaTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_qAlphaTextFieldActionPerformed
//        qZeroParameters.setAlpha(Double.parseDouble(qAlphaTextField.getText()));
//    }//GEN-LAST:event_qAlphaTextFieldActionPerformed
//
//    private void gammaTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_gammaTextFieldActionPerformed
//        qZeroParameters.setAlpha(Double.parseDouble(gammaTextField.getText()));
//    }//GEN-LAST:event_gammaTextFieldActionPerformed
//
//    private void qEpsilonTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_qEpsilonTextFieldActionPerformed
//        asParameters.setEpsilon(Double.parseDouble(qEpsilonTextField.getText()));
//    }//GEN-LAST:event_qEpsilonTextFieldActionPerformed
//
//    private void qZeroMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_qZeroMenuItemStateChanged
//
//    }//GEN-LAST:event_qZeroMenuItemStateChanged
//
//    private void brainMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_brainMenuItemStateChanged
//
//    }//GEN-LAST:event_brainMenuItemStateChanged
//
//    private void fourierBasisMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fourierBasisMenuItemStateChanged
//
//    }//GEN-LAST:event_fourierBasisMenuItemStateChanged
//
//    private void atanFFNNMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_atanFFNNMenuItemStateChanged
//
//    }//GEN-LAST:event_atanFFNNMenuItemStateChanged
//
//    private void cnfFunctionMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cnfFunctionMenuItemActionPerformed
//
//    }//GEN-LAST:event_cnfFunctionMenuItemActionPerformed
//
//    private void cnfFunctionMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cnfFunctionMenuItemStateChanged
//
//    }//GEN-LAST:event_cnfFunctionMenuItemStateChanged
//
//    private void brainEpsilonTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_brainEpsilonTextFieldActionPerformed
//        brainParameters.epsilon = Double.parseDouble(brainEpsilonTextField.getText());
//    }//GEN-LAST:event_brainEpsilonTextFieldActionPerformed
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(QZeroDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(QZeroDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(QZeroDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(QZeroDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new QZeroDemo().setVisible(true);
//            }
//        });
//    }
//
//    // Variables declaration - do not modify//GEN-BEGIN:variables
//    private javax.swing.ButtonGroup actionSelectorButtonGroup;
//    private javax.swing.ButtonGroup agentButtonGroup;
//    private javax.swing.JTextField alphaTextField;
//    private javax.swing.ButtonGroup approximatorButtonGroup;
//    private javax.swing.JRadioButtonMenuItem atanFFNNMenuItem;
//    private javax.swing.JTextField brainEpsilonTextField;
//    private javax.swing.JRadioButtonMenuItem brainMenuItem;
//    private javax.swing.ButtonGroup buttonGroup1;
//    private javax.swing.JRadioButtonMenuItem byQMenuItem;
//    private javax.swing.JRadioButtonMenuItem cnfFunctionMenuItem;
//    private javax.swing.JLabel debugLabel;
//    private javax.swing.JRadioButtonMenuItem epsilonGreedyMenuItem;
//    private javax.swing.JRadioButtonMenuItem features1MenuItem;
//    private javax.swing.JRadioButtonMenuItem features2MenuItem;
//    private javax.swing.JRadioButtonMenuItem features3MenuItem;
//    private javax.swing.JRadioButtonMenuItem features4MenuItem;
//    private javax.swing.JRadioButtonMenuItem features5MenuItem;
//    private javax.swing.JRadioButtonMenuItem features6MenuItem;
//    private javax.swing.JRadioButtonMenuItem fourierBasisMenuItem;
//    private javax.swing.JTextField gammaTextField;
//    private javax.swing.JLabel jLabel1;
//    private javax.swing.JLabel jLabel10;
//    private javax.swing.JLabel jLabel2;
//    private javax.swing.JLabel jLabel3;
//    private javax.swing.JLabel jLabel4;
//    private javax.swing.JLabel jLabel9;
//    private javax.swing.JMenu jMenu1;
//    private javax.swing.JMenu jMenu2;
//    private javax.swing.JMenu jMenu3;
//    private javax.swing.JMenu jMenu4;
//    private javax.swing.JMenuBar jMenuBar1;
//    private javax.swing.JPanel jPanel2;
//    private javax.swing.JPanel jPanel3;
//    private javax.swing.JPanel jPanel4;
//    private javax.swing.JPanel jPanel5;
//    private javax.swing.JPanel jPanel6;
//    private javax.swing.JPanel jPanel8;
//    private javax.swing.JPanel jPanel9;
//    private javax.swing.JRadioButton jRadioButton1;
//    private javax.swing.JRadioButton jRadioButton2;
//    private javax.swing.JRadioButton jRadioButton3;
//    private javax.swing.JScrollPane jScrollPane1;
//    private javax.swing.JScrollPane jScrollPane2;
//    private javax.swing.JScrollPane jScrollPane3;
//    private javax.swing.JTabbedPane jTabbedPane1;
//    private javax.swing.JTextArea jTextArea1;
//    private javax.swing.JTextArea jTextArea2;
//    private javax.swing.JTextArea jTextArea3;
//    private javax.swing.JTextField momentumTextField;
//    private javax.swing.ButtonGroup numFeaturesButtonGroup;
//    private javax.swing.JTextField qAlphaTextField;
//    private javax.swing.JTextField qEpsilonTextField;
//    private javax.swing.JRadioButtonMenuItem qZeroMenuItem;
//    // End of variables declaration//GEN-END:variables
// }
