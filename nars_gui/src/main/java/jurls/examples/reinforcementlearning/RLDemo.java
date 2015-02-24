/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.reinforcementlearning;

import jurls.core.utils.Physics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.DiffableFunctionGenerator;
import jurls.core.approximation.DiffableFunctionMarshaller;
import jurls.core.approximation.Generator;
import jurls.core.approximation.InputNormalizer;
import jurls.core.approximation.OutputNormalizer;
import jurls.core.approximation.ParameterizedFunction;
import jurls.core.approximation.Scalar;
import jurls.core.reinforcementlearning.ActionSelector;
import jurls.core.reinforcementlearning.ByQActionSelector;
import jurls.core.reinforcementlearning.EpsilonGreedyActionSelector;
import jurls.core.reinforcementlearning.QUpdateProcedure;
import jurls.core.reinforcementlearning.RLAgentMarshaller;
import jurls.core.reinforcementlearning.RLParameters;
import jurls.core.reinforcementlearning.SARSAUpdateProcedure;
import jurls.core.reinforcementlearning.UpdateProcedure;

/**
 *
 * @author thorsten
 */
public class RLDemo extends javax.swing.JFrame {

    private Physics2D physics2D;
    private Point agentPoint;
    private Point pendulumPoint;
    private RLAgentMarshaller rLAgentMarshaller = new RLAgentMarshaller();
    private List<Scalar> ps = new ArrayList<>();
    private double maxQ = -Double.MAX_VALUE;
    private double minQ = Double.MAX_VALUE;
    private int numIterationsPerPeriod = 1;
    private final ApproxParameters approxParameters = new ApproxParameters(0.001, 0.1);
    private final RLParameters rLParameters = new RLParameters(0.9, 0.9);
    private final EpsilonGreedyActionSelector.Parameters asParameters = new EpsilonGreedyActionSelector.Parameters(0.01);

    /**
     * Creates new form RLDemo
     */
    public RLDemo() {
        initComponents();

        physics2D = new Physics2D(0.1, 300);
        agentPoint = new Point(400, 300, 0, 0, 0.99, 0);
        pendulumPoint = new Point(401, 50, 0, 0, 0.99, 0.99);
        Connection c = new Connection(250, agentPoint, pendulumPoint);
        physics2D.points.add(agentPoint);
        physics2D.points.add(pendulumPoint);
        physics2D.connections.add(c);
        physicsRenderer1.physics2D = physics2D;
        newAgent();

        new Timer(20, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < numIterationsPerPeriod; ++i) {
                    double a = rLAgentMarshaller.getRLAgent().chooseAction() - 0.5;

                    agentPoint.vx += 2 * a;
                    physics2D.step(1);
                    if (agentPoint.x < 50) {
                        agentPoint.x = 50;
                    }
                    if (agentPoint.x > 750) {
                        agentPoint.x = 750;
                    }
                    rLAgentMarshaller.getRLAgent().learn(observe(), 300.0 - pendulumPoint.y);
                }

                physicsRenderer1.repaint();
                iterationsLabel.setText("No. iterations : " + rLAgentMarshaller.getRLAgent().numIterations);

                int j = ps.size() / 2;

                StringBuilder sb = new StringBuilder();
                sb.append("Parameters : \n");
                for (int i = 0; i < j; ++i) {
                    Scalar p = ps.get(i);
                    sb.append(p.getName() + " = " + p.value() + "\n");
                }
                jTextArea1.setText(sb.toString());

                sb = new StringBuilder();
                sb.append("Parameters : \n");
                for (int i = j; i < ps.size(); ++i) {
                    Scalar p = ps.get(i);
                    sb.append(p.getName() + " = " + p.value() + "\n");
                }
                jTextArea2.setText(sb.toString());

                sb = new StringBuilder();
                sb.append("minQ = " + minQ + "\n");
                sb.append("maxQ = " + maxQ + "\n");
                jTextArea3.setText(sb.toString());
            }
        }).start();
    }

    private void newAgent() {
        maxQ = -Double.MAX_VALUE;
        minQ = Double.MAX_VALUE;

        double[] s0 = observe();

        DiffableFunctionGenerator dfg = null;
        switch (aFunctionComboBox.getSelectedIndex()) {
            case 0:
                dfg = Generator.generateFourierBasis();
                break;

            case 1:
                dfg = Generator.generateRBFNet();
                break;

            case 2:
                dfg = Generator.generateTanhFFNN();
                break;

            case 3:
                dfg = Generator.generateATanFFNN();
                break;
        }

        UpdateProcedure up = null;
        switch (agentComboBox.getSelectedIndex()) {
            case 0:
                up = new QUpdateProcedure();
                break;

            case 1:
                up = new SARSAUpdateProcedure();
                break;
        }

        ActionSelector as = null;
        switch (actionSelectionComboBox1.getSelectedIndex()) {
            case 0:
                as = new EpsilonGreedyActionSelector(asParameters);
                break;
                
            case 1:
                as = new ByQActionSelector();
                break;
        }

        ParameterizedFunction f =
                new OutputNormalizer(
                        new InputNormalizer(
                                new DiffableFunctionMarshaller(dfg, s0.length + 1, 5)
                        )
                );
        rLAgentMarshaller.reset(f, up, as, s0, approxParameters, rLParameters, 2);
    }

    private double[] observe() {
        return new double[]{
            agentPoint.x,
            agentPoint.y,
            //agentPoint.vx,
            //agentPoint.vy,
            pendulumPoint.x,
            pendulumPoint.y
        //pendulumPoint.vx,
        //pendulumPoint.vy
        };
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        iterationsLabel = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        alphaComboBox = new javax.swing.JComboBox();
        momentumComboBox = new javax.swing.JComboBox();
        epsilonComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        aFunctionComboBox = new javax.swing.JComboBox();
        agentComboBox = new javax.swing.JComboBox();
        actionSelectionComboBox1 = new javax.swing.JComboBox();
        jPanel12 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        physicsRenderer1 = new jurls.core.utils.PhysicsRenderer();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 600));

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Training 500 Mode");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });
        jPanel8.add(jRadioButton1);

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Training 50 Mode");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });
        jPanel8.add(jRadioButton3);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setSelected(true);
        jRadioButton2.setText("Run Mode");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });
        jPanel8.add(jRadioButton2);

        iterationsLabel.setText("jLabel1");
        jPanel8.add(iterationsLabel);

        jPanel2.add(jPanel8, java.awt.BorderLayout.CENTER);

        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel10.setLayout(new java.awt.GridLayout(0, 1));

        jLabel1.setText("Learning Rate (Alpha)");
        jPanel10.add(jLabel1);

        jLabel2.setText("Momentum");
        jPanel10.add(jLabel2);

        jLabel3.setText("Randomness (Epsilon)");
        jPanel10.add(jLabel3);

        jPanel9.add(jPanel10, java.awt.BorderLayout.WEST);

        jPanel11.setLayout(new java.awt.GridLayout(0, 1));

        alphaComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.01", "0.001" }));
        alphaComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphaComboBoxActionPerformed(evt);
            }
        });
        jPanel11.add(alphaComboBox);

        momentumComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.1", "0.95" }));
        momentumComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                momentumComboBoxActionPerformed(evt);
            }
        });
        jPanel11.add(momentumComboBox);

        epsilonComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.01", "0.1", "0.5", "0.9" }));
        epsilonComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                epsilonComboBoxActionPerformed(evt);
            }
        });
        jPanel11.add(epsilonComboBox);

        jPanel9.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel9, java.awt.BorderLayout.PAGE_START);

        jPanel3.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.GridLayout(0, 1));

        aFunctionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Fourier Basis", "Radial Basis Functions Net", "Tanh Feed Forward Neural Net", "ATan Feed Forward Neural Net" }));
        aFunctionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aFunctionComboBoxActionPerformed(evt);
            }
        });
        jPanel7.add(aFunctionComboBox);

        agentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Q(lambda)", "SARSA(lambda)" }));
        agentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agentComboBoxActionPerformed(evt);
            }
        });
        jPanel7.add(agentComboBox);

        actionSelectionComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Epsilon-Greedy", "Q value is probability" }));
        actionSelectionComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionSelectionComboBox1ActionPerformed(evt);
            }
        });
        jPanel7.add(actionSelectionComboBox1);

        jPanel1.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel12.setLayout(new java.awt.GridLayout(0, 1));

        jLabel4.setText("Function Approximator");
        jPanel12.add(jLabel4);

        jLabel5.setText("Reinforcement Learner");
        jPanel12.add(jLabel5);

        jLabel6.setText("Action Selector");
        jPanel12.add(jLabel6);

        jPanel1.add(jPanel12, java.awt.BorderLayout.WEST);

        jPanel3.add(jPanel1, java.awt.BorderLayout.PAGE_START);

        javax.swing.GroupLayout physicsRenderer1Layout = new javax.swing.GroupLayout(physicsRenderer1);
        physicsRenderer1.setLayout(physicsRenderer1Layout);
        physicsRenderer1Layout.setHorizontalGroup(
            physicsRenderer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 571, Short.MAX_VALUE)
        );
        physicsRenderer1Layout.setVerticalGroup(
            physicsRenderer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 208, Short.MAX_VALUE)
        );

        jPanel3.add(physicsRenderer1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Demo", jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

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

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void aFunctionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aFunctionComboBoxActionPerformed
        newAgent();
    }//GEN-LAST:event_aFunctionComboBoxActionPerformed

    private void agentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agentComboBoxActionPerformed
        newAgent();
    }//GEN-LAST:event_agentComboBoxActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        numIterationsPerPeriod = 500;
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        numIterationsPerPeriod = 1;
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        numIterationsPerPeriod = 50;
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void alphaComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphaComboBoxActionPerformed
        approxParameters.setAlpha(Double.parseDouble(alphaComboBox.getSelectedItem().toString()));
    }//GEN-LAST:event_alphaComboBoxActionPerformed

    private void momentumComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_momentumComboBoxActionPerformed
        approxParameters.setMomentum(Double.parseDouble(momentumComboBox.getSelectedItem().toString()));
    }//GEN-LAST:event_momentumComboBoxActionPerformed

    private void epsilonComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_epsilonComboBoxActionPerformed
        asParameters.setEpsilon(Double.parseDouble(epsilonComboBox.getSelectedItem().toString()));
    }//GEN-LAST:event_epsilonComboBoxActionPerformed

    private void actionSelectionComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionSelectionComboBox1ActionPerformed
        newAgent();
    }//GEN-LAST:event_actionSelectionComboBox1ActionPerformed

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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RLDemo().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox aFunctionComboBox;
    private javax.swing.JComboBox actionSelectionComboBox1;
    private javax.swing.JComboBox agentComboBox;
    private javax.swing.JComboBox alphaComboBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox epsilonComboBox;
    private javax.swing.JLabel iterationsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JComboBox momentumComboBox;
    private jurls.core.utils.PhysicsRenderer physicsRenderer1;
    // End of variables declaration//GEN-END:variables
}
