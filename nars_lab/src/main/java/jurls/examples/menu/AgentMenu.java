/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.menu;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import jurls.core.LearnerAndActor;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.brain.Brain;
import jurls.core.reinforcementlearning.EpsilonGreedyActionSelector;
import jurls.core.reinforcementlearning.QUpdateProcedure;
import jurls.core.reinforcementlearning.QZeroAgent;
import jurls.core.reinforcementlearning.RLAgent;
import jurls.core.reinforcementlearning.RLParameters;
import jurls.core.reinforcementlearning.SARSAUpdateProcedure;
import jurls.core.reinforcementlearning.UpdateProcedure;

/**
 *
 * @author thorsten2
 */
public class AgentMenu extends RLMenu {

    public ApproximatorMenu approximatorMenu = new ApproximatorMenu(true);
    public JRadioButtonMenuItem qlambda = new JRadioButtonMenuItem(new MyAction("Q(lambda)"));
    public JRadioButtonMenuItem sarsalambda = new JRadioButtonMenuItem(new MyAction("SARSA(lmabda)"));
    public JRadioButtonMenuItem qzero = new JRadioButtonMenuItem(new MyAction("Q(0)"));
    public JRadioButtonMenuItem brain = null;
    public AgentMenu soulMenu = null;

    public AgentMenu(String prefix, int depth) {
        super(prefix + "RL Agent");

        ButtonGroup bg = new ButtonGroup();
        bg.add(qlambda);
        bg.add(sarsalambda);
        bg.add(qzero);

        add(qlambda);
        add(sarsalambda);
        add(qzero);
        if (depth > 0) {
            brain = new JRadioButtonMenuItem(new MyAction("Brain"));
            bg.add(brain);
            soulMenu = new AgentMenu("Brain's ", depth - 1);
            soulMenu.addActionListener(new MyAction(""));
            addSeparator();
            add(brain);
            add(soulMenu);
            sarsalambda.setSelected(true);
        } else {
            sarsalambda.setSelected(true);
        }

        addSeparator();
        add(approximatorMenu);

        approximatorMenu.addActionListener(new MyAction(""));
    }

    public LearnerAndActor getAgent(
            int numActions,
            double[] s0,
            ApproxParameters approxParameters,
            RLParameters rLParameters
    ) {
        UpdateProcedure up = null;
        if (qlambda.isSelected()) {
            up = new QUpdateProcedure();
        }
        if (sarsalambda.isSelected()) {
            up = new SARSAUpdateProcedure();
        }
        ParameterizedFunctionGenerator pfg = approximatorMenu.getFunctionGenerator(approxParameters);

        if (qlambda.isSelected() || sarsalambda.isSelected()) {
            return new RLAgent(
                    pfg, up,
                    new EpsilonGreedyActionSelector(),
                    numActions,
                    s0,
                    approxParameters,
                    rLParameters
            );
        }

        if (qzero.isSelected()) {
            return new QZeroAgent(
                    rLParameters,
                    pfg,
                    new EpsilonGreedyActionSelector(),
                    s0,
                    numActions
            );
        }

        if (brain.isSelected()) {
            return new Brain(
                    s0.length, numActions, rLParameters, pfg,
                    soulMenu.getAgent(numActions, s0, approxParameters, rLParameters)
            );
        }

        return null;
    }

}
