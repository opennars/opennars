/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls;

import javafx.application.Platform;
import jurls.core.LearnerAndActor;
import jurls.core.approximation.ApproxParameters;
import jurls.core.reinforcementlearning.RLAgent;
import jurls.core.reinforcementlearning.RLParameters;
import jurls.core.utils.LineCharts;
import jurls.core.utils.MatrixImage;
import jurls.examples.menu.AgentMenu;
import jurls.examples.menu.DomainMenu;
import jurls.examples.menu.ObjectListMenu;
import jurls.reinforcementlearning.domains.RLDomain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author thorsten
 */
public class RLDemo extends javax.swing.JFrame {

    private LearnerAndActor agent;
    private RLDomain rLDomain;

    private int numIterationsPerLoop = 1;
    private final ApproxParameters approxParameters = new ApproxParameters(0.01, 0.1);
    private final RLParameters rLParameters = new RLParameters(0.9, 0.9, 0.9, 1);
    private int numPhysicsIterations = 0;
    private final AgentMenu agentMenu = new AgentMenu("", 2);
    private final DomainMenu domainMenu = new DomainMenu();
    private final ObjectListMenu iterationsMenu = new ObjectListMenu(
            "No. Iterations", 0, 1, 50, 500, 1000, 5000
    );
    private int action = 0;
    boolean visualize = true;
    public final LineCharts rewardChart = new LineCharts("Reward", 1, 200);
    public final MatrixImage parameterChart = new MatrixImage(50, 50);

    private final Action menuAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (RLDemo.this) {
                timer.stop();

                rLDomain = domainMenu.getDomain();
                if (agent != null) {
                    agent.stop();
                }
                agent = agentMenu.getAgent(
                        rLDomain.numActions(),
                        rLDomain.observe(), approxParameters, rLParameters
                );

                parameterChart.removeAll();

                renderPanel.removeAll();
                renderPanel.add(rLDomain.component());

                action = 0;
                numPhysicsIterations = 0;

                timer.start();
            }
        }
    };
    private Timer timer = new Timer(5, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (RLDemo.this) {
                for (int i = 0; i < numIterationsPerLoop; ++i) {
                    double[] previousState = rLDomain.observe();
                    rLDomain.takeAction(action);
                    rLDomain.worldStep();
                    double[] nextState = rLDomain.observe();

                    action = agent.learnAndAction(
                            nextState,
                            rLDomain.reward(),
                            previousState,
                            action
                    );
                    numPhysicsIterations++;
                }

                jTextArea1.setText(agent.getDebugString(0));
                debugLabel.setText("@" + numPhysicsIterations);

                if (visualize) {
                    final double r = rLDomain.reward();

                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            rewardChart.updateReward(numPhysicsIterations, new double[]{r});
                        }
                    });

                    if (agent instanceof RLAgent) {
                        double[] d = ((RLAgent)agent).getStateNormalized();
                        int numParam = d.length;
                        final int cw = (int) Math.ceil(Math.sqrt(numParam));
                        final int ch = numParam / cw;

                        parameterChart.draw(agent.getFunction());
                        //parameterChart.draw(d, 0, 1, false);

//                        parameterChart.draw(new MatrixImage.Data2D() {
//
//                            @Override
//                            public double getValue(final int x, final int y) {
//                                final int i = y * ch + x;
//                                if (i < numParam)
//                                    return p.getParameter(i);
//
//                                return 0;
//                            }
//
//                        }, cw, ch, -1.0, 1.0);
                    }

                }

                rLDomain.component().repaint();
            }
        }
    });

    /**
     * Creates new form RLDemo
     */
    public RLDemo() {
        initComponents();

        timer.setCoalesce(true);

        jMenuBar1.add(agentMenu);
        agentMenu.addActionListener(menuAction);
        jMenuBar1.add(domainMenu);
        domainMenu.addActionListener(menuAction);
        jMenuBar1.add(iterationsMenu);

        iterationsMenu.addActionListener((ActionEvent e) -> {
            numIterationsPerLoop = (int) iterationsMenu.getObject();
        });
        iterationsMenu.notifyListeners();

        approxParameters.setAlpha(Double.parseDouble(alphaTextField.getText()));
        approxParameters.setMomentum(Double.parseDouble(momentumTextField.getText()));
        rLParameters.setEpsilon(Double.parseDouble(qEpsilonTextField.getText()));
        rLParameters.setAlpha(Double.parseDouble(qAlphaTextField.getText()));
        rLParameters.setGamma(Double.parseDouble(gammaTextField.getText()));

        menuAction.actionPerformed(null);

        timer.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        approximatorButtonGroup = new javax.swing.ButtonGroup();
        actionSelectorButtonGroup = new javax.swing.ButtonGroup();
        numFeaturesButtonGroup = new javax.swing.ButtonGroup();
        agentButtonGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        alphaTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        momentumTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        qAlphaTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        gammaTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        qEpsilonTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        debugLabel = new javax.swing.JLabel();
        renderPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jTextArea1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1000, 800));

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel9.setLayout(new java.awt.GridLayout(0, 4));

        jLabel1.setText("Approximator Learning Rate (Alpha)");
        jPanel9.add(jLabel1);

        alphaTextField.setText("0.001");
        alphaTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphaTextFieldActionPerformed(evt);
            }
        });
        jPanel9.add(alphaTextField);

        jLabel2.setText("Approximator Momentum");
        jPanel9.add(jLabel2);

        momentumTextField.setText("0.9");
        momentumTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                momentumTextFieldActionPerformed(evt);
            }
        });
        jPanel9.add(momentumTextField);

        jLabel9.setText("Q Learning Rate (Alpha)");
        jPanel9.add(jLabel9);

        qAlphaTextField.setText("0.9");
        qAlphaTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qAlphaTextFieldActionPerformed(evt);
            }
        });
        jPanel9.add(qAlphaTextField);

        jLabel10.setText("Q Farsight (Gamma)");
        jPanel9.add(jLabel10);

        gammaTextField.setText("0.9");
        gammaTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gammaTextFieldActionPerformed(evt);
            }
        });
        jPanel9.add(gammaTextField);

        jLabel3.setText("Q Randomness (Epsilon)");
        jPanel9.add(jLabel3);

        jPanel1.setLayout(new java.awt.BorderLayout());

        qEpsilonTextField.setText("1");
        qEpsilonTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qEpsilonTextFieldActionPerformed(evt);
            }
        });
        jPanel1.add(qEpsilonTextField, java.awt.BorderLayout.CENTER);

        jLabel4.setText("* factor1 * factor2");
        jPanel1.add(jLabel4, java.awt.BorderLayout.EAST);

        jPanel9.add(jPanel1);

        jPanel2.add(jPanel9, java.awt.BorderLayout.PAGE_START);

        jPanel3.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        debugLabel.setText("jLabel5");
        jPanel3.add(debugLabel, java.awt.BorderLayout.NORTH);

        renderPanel.setLayout(new java.awt.GridLayout(1, 0));
        jPanel3.add(renderPanel, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Demo", jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Debug 1", jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jPanel5.add(jScrollPane2, java.awt.BorderLayout.CENTER);


        jTabbedPane1.addTab("Debug 2", jPanel5);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        jPanel6.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Debug 3", jPanel6);


        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);


        JPanel outputPanel = new JPanel(new GridLayout(0, 1));
        outputPanel.add(new JScrollPane(jTextArea1));
        outputPanel.add(parameterChart);
        outputPanel.add(rewardChart);


        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        getContentPane().add(outputPanel, java.awt.BorderLayout.WEST);
        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void alphaTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphaTextFieldActionPerformed
        approxParameters.setAlpha(Double.parseDouble(alphaTextField.getText()));
    }//GEN-LAST:event_alphaTextFieldActionPerformed

    private void momentumTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_momentumTextFieldActionPerformed
        approxParameters.setMomentum(Double.parseDouble(momentumTextField.getText()));
    }//GEN-LAST:event_momentumTextFieldActionPerformed

    private void qAlphaTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qAlphaTextFieldActionPerformed
        rLParameters.setAlpha(Double.parseDouble(qAlphaTextField.getText()));
    }//GEN-LAST:event_qAlphaTextFieldActionPerformed

    private void gammaTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gammaTextFieldActionPerformed
        rLParameters.setGamma(Double.parseDouble(gammaTextField.getText()));
    }//GEN-LAST:event_gammaTextFieldActionPerformed

    private void qEpsilonTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qEpsilonTextFieldActionPerformed
        rLParameters.setEpsilon(Double.parseDouble(qEpsilonTextField.getText()));
    }//GEN-LAST:event_qEpsilonTextFieldActionPerformed

    @Override
    public void dispose() {
        timer.stop();
        if (agent != null) {
            agent.stop();
        }
        super.dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RLDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RLDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RLDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RLDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RLDemo().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup actionSelectorButtonGroup;
    private javax.swing.ButtonGroup agentButtonGroup;
    private javax.swing.JTextField alphaTextField;
    private javax.swing.ButtonGroup approximatorButtonGroup;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel debugLabel;
    private javax.swing.JTextField gammaTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField momentumTextField;
    private javax.swing.ButtonGroup numFeaturesButtonGroup;
    private javax.swing.JTextField qAlphaTextField;
    private javax.swing.JTextField qEpsilonTextField;
    private javax.swing.JPanel renderPanel;
    // End of variables declaration//GEN-END:variables
}
