/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning.experiment;

import jurls.reinforcementlearning.domains.RLDomain;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.DiffableFunctionGenerator;
import jurls.core.approximation.DiffableFunctionMarshaller;
import jurls.core.approximation.InputNormalizer;
import jurls.core.approximation.Scalar;
import jurls.core.reinforcementlearning.ActionSelector;
import jurls.core.reinforcementlearning.RLAgentMarshaller;
import jurls.core.reinforcementlearning.RLParameters;
import jurls.core.reinforcementlearning.UpdateProcedure;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;

/**
 *
 * @author thorsten
 */
public class Experiment {

    private ActionSelector actionSelector = null;
    private UpdateProcedure updateProcedure = null;
    private DiffableFunctionGenerator diffableFunctionGenerator = null;
    private int numberOfFeatures = 5;
    long visualizationUpdateMS = 150;
    private RLDomain rLDomain = null;
    private ApproxParameters approxParameters = null;
    private RLParameters rLParameters = null;
    private DiffableFunctionMarshaller diffableFunctionMarshaller = null;
    private LineCharts rewardChart;
    private MatrixImage parameterChart;
    private JComboBox speedComboBox = new JComboBox(
            new Object[]{
                "maximum speed",
                "20 iterations per second",
                "100 iterations per second"
            }
    );
    private int fps = Integer.MAX_VALUE;
    private String debugText = "";
    private JTextArea textArea;

    public void setNumberOfFeatures(int numberOfFeatures) {
        this.numberOfFeatures = numberOfFeatures;
    }

    public void setActionSelector(ActionSelector actionSelector) {
        this.actionSelector = actionSelector;
    }

    public void setUpdateProcedure(UpdateProcedure updateProcedure) {
        this.updateProcedure = updateProcedure;
    }

    public void setDiffableFunctionGenerator(DiffableFunctionGenerator diffableFunctionGenerator) {
        this.diffableFunctionGenerator = diffableFunctionGenerator;
    }

    public void setrLDomain(RLDomain rLDomain) {
        this.rLDomain = rLDomain;
    }

    public void setApproxParameters(ApproxParameters approxParameters) {
        this.approxParameters = approxParameters;
    }

    public void setrLParameters(RLParameters rLParameters) {
        this.rLParameters = rLParameters;
    }

    public double run(int numSteps, boolean visualize) {
        JFrame f = null;
        JFrame debug = null;
        textArea = new JTextArea();

        RLAgentMarshaller h = new RLAgentMarshaller();

        diffableFunctionMarshaller = new DiffableFunctionMarshaller(
                diffableFunctionGenerator,
                rLDomain.observe().length + 1,
                numberOfFeatures
        );

        h.reset(
                new InputNormalizer(
                        diffableFunctionMarshaller
                ),
                updateProcedure,
                actionSelector,
                rLDomain.observe(),
                approxParameters,
                rLParameters,
                rLDomain.numActions()
        );

        if (visualize) {
            f = new JFrame();
            f.setTitle("RL Exepriment is running...");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(800, 600);
            f.setLayout(new BorderLayout());
            f.add(rLDomain.component(), BorderLayout.CENTER);
            f.setVisible(true);

            rewardChart = new LineCharts("Reward", 1, 200);
            parameterChart = new MatrixImage(300, 300);

            debug = new JFrame();
            debug.setTitle("Experiment");
            debug.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            debug.setSize(300, 600);
            debug.setLayout(new BorderLayout());
            JPanel p = new JPanel(new BorderLayout());
            p.add(textArea, BorderLayout.NORTH);
            p.add(speedComboBox,BorderLayout.CENTER);
            debug.add(p, BorderLayout.NORTH);
            debug.add(rewardChart, BorderLayout.CENTER);
            debug.add(parameterChart, BorderLayout.SOUTH);

            debug.setVisible(true);
            
            speedComboBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    switch(speedComboBox.getSelectedIndex()){
                        case 0:
                            fps = Integer.MAX_VALUE;
                            break;
                        case 1:
                            fps = 20;
                            break;
                        case 2:
                            fps = 100;
                            break;
                    }
                }
            });
        }

        long t0 = System.currentTimeMillis();
        long t2 = t0;
        int counter = 0;
        double sumOfRewards = 0;
        int iterationsPerSecond = 0;

        for (int i = 0; i < numSteps; ++i) {
            long t1 = System.currentTimeMillis();
            final double r = rLDomain.reward();
            sumOfRewards += r;
            final int _i = i;
            
            if (visualize) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        rewardChart.updateReward(_i, new double[]{r});
                    }
                });
            }

            h.getRLAgent().learn(rLDomain.observe(), r);
            rLDomain.takeAction(h.getRLAgent().chooseAction());
            rLDomain.worldStep();

            if (visualize && (t1 - t0 > visualizationUpdateMS) && h.getParameterizedFunction()!=null) {
                final int i0 = i;

                StringBuilder sb = new StringBuilder();
                sb.append("minQ : ").append(h.getParameterizedFunction().minOutputDebug()).append('\n').append("maxQ : ").append(h.getParameterizedFunction().maxOutputDebug()).append('\n').append("progress : ").append(100 * i0 / numSteps).append("%\n").append("iterations per second : ").append(iterationsPerSecond);
                debugText = sb.toString();

//                for (Scalar p : diffableFunctionMarshaller.parameters) {
//                    //sb.append(p.getName() + " = " + p.value() + "\n");
//                }


                int numParam = diffableFunctionMarshaller.parameters.length;
                final int cw = (int) Math.ceil(Math.sqrt(numParam));
                final int ch = numParam / cw;
                parameterChart.draw(new Data2D() {

                    @Override
                    public double getValue(int x, int y) {
                        int i = y * ch + x;
                        if (i < numParam) {
                            return diffableFunctionMarshaller.parameters[i].value();
                        }
                        return 0;
                    }

                }, cw, ch, -1.0, 1.0);

                EventQueue.invokeLater(redraw);
                t0 = t1;
                
            }
            counter++;
            if(t1 - t2 > 1000) {
                iterationsPerSecond = counter;
                counter = 0;
                t2 = System.currentTimeMillis();
            }
            try {
                Thread.sleep(1000 / fps);
            } catch (InterruptedException ex) {
                Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (visualize) {
            f.setVisible(false);
            debug.setVisible(false);
        }
        return sumOfRewards;
    }

    final Runnable redraw = new Runnable() {

        @Override
        public void run() {
                textArea.setText(debugText);
            rLDomain.component().repaint();

        }

    };
}
