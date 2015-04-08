/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.menu;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import jurls.reinforcementlearning.domains.PoleBalancing2D;
import jurls.reinforcementlearning.domains.RLDomain;
import jurls.reinforcementlearning.domains.martialarts.MartialArts;
import jurls.reinforcementlearning.domains.tetris.Tetris;
import jurls.reinforcementlearning.domains.wander.Curiousbot;

/**
 *
 * @author thorsten2
 */
public class DomainMenu extends RLMenu {

    private final JRadioButtonMenuItem poleBalancing = new JRadioButtonMenuItem(new MyAction("Pole Balancing"));
    private final JRadioButtonMenuItem wanderBot = new JRadioButtonMenuItem(new MyAction("Wander Bot"));
    private final JRadioButtonMenuItem martialArts = new JRadioButtonMenuItem(new MyAction("Martial Arts"));

    public DomainMenu() {
        super("Domain");

        ButtonGroup bg = new ButtonGroup();
        bg.add(poleBalancing);
        bg.add(wanderBot);
        bg.add(martialArts);

        add(poleBalancing);
        add(wanderBot);
        add(martialArts);

        poleBalancing.setSelected(true);
    }

    public RLDomain getDomain() {
        if (poleBalancing.isSelected()) {
            return new PoleBalancing2D();
        }

        if (wanderBot.isSelected()) {
            return new Curiousbot();
        }

        if (martialArts.isSelected()) {
            return new MartialArts();
        }

        return null;
    }
}
