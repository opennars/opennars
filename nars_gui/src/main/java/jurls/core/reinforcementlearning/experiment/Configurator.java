/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning.experiment;

import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.Generator;
import jurls.core.reinforcementlearning.*;
import jurls.reinforcementlearning.domains.Follow1D;
import jurls.reinforcementlearning.domains.PoleBalancing2D;
import jurls.reinforcementlearning.domains.RLDomain;
import jurls.reinforcementlearning.domains.martialarts.MartialArts;
import jurls.reinforcementlearning.domains.tetris.Tetris;
import jurls.reinforcementlearning.domains.wander.Curiousbot;

import javax.swing.*;

/**
 *
 * @author thorsten
 */
public class Configurator extends javax.swing.JFrame implements Runnable {

    private RLDomain specificDomain;

    /**
     * Creates new form Configurator
     */
    public Configurator() {
        initComponents();
        setTitle("Configure Your Experiment");
    }

    public Configurator(RLDomain domain, boolean autostart) {
        this();

        domainComboBox.setVisible(false);

        setVisible(true);

        this.specificDomain = domain;
        Configurator c = new Configurator();
        c.getDomainComboBox().setSelectedIndex(2);

        if (autostart) {
            setVisible(false);
            run();
        }

    }

    public void run() {
        final Experiment experiment = new Experiment();

        switch (approxComboBox.getSelectedIndex()) {
            case 0:
                experiment.setDiffableFunctionGenerator(Generator.generateFourierBasis());
                break;

            case 1:
                experiment.setDiffableFunctionGenerator(Generator.generateRBFNet());
                break;

            case 2:
                experiment.setDiffableFunctionGenerator(Generator.generateTanhFFNN());
                break;

            case 3:
                experiment.setDiffableFunctionGenerator(Generator.generateATanFFNN());
                break;

            case 4:
                experiment.setDiffableFunctionGenerator(Generator.generateLogisticFFNN());
                break;
        }

        switch (updateComboBox.getSelectedIndex()) {
            case 0:
                experiment.setUpdateProcedure(new QUpdateProcedure());
                break;

            case 1:
                experiment.setUpdateProcedure(new SARSAUpdateProcedure());
                break;
        }

        switch (actionSelectorComboBox.getSelectedIndex()) {
            case 0:
                experiment.setActionSelector(
                        new EpsilonGreedyActionSelector(
                                new EpsilonGreedyActionSelector.Parameters(
                                        Double.parseDouble(epsilonTextField.getText())
                                )
                        )
                );
                break;

            case 1:
                experiment.setActionSelector(
                        new ByQActionSelector()
                );
                break;
        }

        experiment.setApproxParameters(
                new ApproxParameters(
                        Double.parseDouble(alphaTextField.getText()),
                        Double.parseDouble(momentumTextField.getText())
                )
        );

        experiment.setrLParameters(
                new RLParameters(
                        Double.parseDouble(gammaTextField.getText()),
                        Double.parseDouble(lambdaTextField.getText())
                )
        );

        experiment.setNumberOfFeatures(Integer.parseInt(featuresTextField.getText()));

        new Thread() {

            @Override
            public void run() {
                if (specificDomain == null) {
                    switch (domainComboBox.getSelectedIndex()) {

                        case 0:
                            experiment.setrLDomain(new PoleBalancing2D());
                            break;

                        case 1:
                            experiment.setrLDomain(new Follow1D());
                            break;

                        case 2:
                            experiment.setrLDomain(new Curiousbot());
                            break;

                        case 3:
                            experiment.setrLDomain(new Tetris(10, 20));
                            break;

                        case 4:
                            experiment.setrLDomain(new MartialArts());
                            break;

                    }
                } else {
                    experiment.setrLDomain(specificDomain);
                }
                
                double r = experiment.run(
                        Integer.parseInt(stepsTextField.getText()),
                        true
                );

                JOptionPane.showMessageDialog(null, "Sum of rewards = " + r);
            }

        }.start();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        actionSelectorComboBox = new javax.swing.JComboBox();
        approxComboBox = new javax.swing.JComboBox();
        featuresTextField = new javax.swing.JTextField();
        updateComboBox = new javax.swing.JComboBox();
        alphaTextField = new javax.swing.JTextField();
        momentumTextField = new javax.swing.JTextField();
        epsilonTextField = new javax.swing.JTextField();
        lambdaTextField = new javax.swing.JTextField();
        gammaTextField = new javax.swing.JTextField();
        domainComboBox = new javax.swing.JComboBox();
        stepsTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        actionSelectorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "EpsilonGreedy", "ByQValue" }));
        jPanel1.add(actionSelectorComboBox);

        approxComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Fourier Basis", "Radial Basis Functions Net", "Tanh Feed Forward Neural Net", "ATan Feed Forward Neural Net", "Logistic Sigmoid Feed Forward Neural Net" }));
        jPanel1.add(approxComboBox);

        featuresTextField.setText("5");
        jPanel1.add(featuresTextField);

        updateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Q(Lambda)", "SARSA(Lambda)" }));
        jPanel1.add(updateComboBox);

        alphaTextField.setText("0.01");
        alphaTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphaTextFieldActionPerformed(evt);
            }
        });
        jPanel1.add(alphaTextField);

        momentumTextField.setText("0.2");
        jPanel1.add(momentumTextField);

        epsilonTextField.setText("0.01");
        jPanel1.add(epsilonTextField);

        lambdaTextField.setText("0.9");
        jPanel1.add(lambdaTextField);

        gammaTextField.setText("0.9");
        jPanel1.add(gammaTextField);

        domainComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pole Balancing 2D", "Follow 1D", "Curious Bot", "Tetris", "Martial Arts" }));
        domainComboBox.setSelectedIndex(4);
        jPanel1.add(domainComboBox);

        stepsTextField.setText("50000");
        jPanel1.add(stepsTextField);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(0, 1));

        jLabel1.setText("Action Selector");
        jPanel2.add(jLabel1);

        jLabel2.setText("Approximation Function");
        jPanel2.add(jLabel2);

        jLabel11.setText("Number Of Features");
        jPanel2.add(jLabel11);

        jLabel3.setText("UpdateProcedure");
        jPanel2.add(jLabel3);

        jLabel4.setText("Learning Rate Alpha");
        jPanel2.add(jLabel4);

        jLabel5.setText("Learning Momentum");
        jPanel2.add(jLabel5);

        jLabel6.setText("Randomization Epsilon");
        jPanel2.add(jLabel6);

        jLabel7.setText("Learning Backward Effect Lambda");
        jPanel2.add(jLabel7);

        jLabel8.setText("Forward Sight Gamma");
        jPanel2.add(jLabel8);

        jLabel9.setText("Domain");
        jPanel2.add(jLabel9);

        jLabel10.setText("Number Of Steps");
        jPanel2.add(jLabel10);

        getContentPane().add(jPanel2, java.awt.BorderLayout.WEST);

        jButton1.setText("Run");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        run();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void alphaTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphaTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_alphaTextFieldActionPerformed

    public JComboBox getDomainComboBox() {
        return domainComboBox;
    }

    public JButton getRunButton() {
        return jButton1;
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
            java.util.logging.Logger.getLogger(Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Configurator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Configurator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox actionSelectorComboBox;
    private javax.swing.JTextField alphaTextField;
    private javax.swing.JComboBox approxComboBox;
    private javax.swing.JComboBox domainComboBox;
    private javax.swing.JTextField epsilonTextField;
    private javax.swing.JTextField featuresTextField;
    private javax.swing.JTextField gammaTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField lambdaTextField;
    private javax.swing.JTextField momentumTextField;
    private javax.swing.JTextField stepsTextField;
    private javax.swing.JComboBox updateComboBox;
    // End of variables declaration//GEN-END:variables
}
