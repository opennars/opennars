/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nars.constraint.gui;

import nars.constraint.gui.panels.GraphPanel;
import nars.constraint.gui.panels.GraphPanel.ChocoMetrics;
import nars.constraint.gui.panels.Parameters;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.util.tools.StringUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/06/2014
 */
public class GUI extends JFrame implements IMonitorOpenNode, IMonitorInitialize, IMonitorSolution {

    Solver solver;
    Parameters parameters;

    // SWING
    JTabbedPane tabbedpanel = new JTabbedPane();

    JButton playB = new JButton("Run");
    JButton pauseB = new JButton("Pause");
    JButton flushallB = new JButton("Flush");
    JButton nextSolB = new JButton("Next solution");
    JButton nextNodeB = new JButton("Next node");

    AtomicBoolean play = new AtomicBoolean(false);
    AtomicBoolean nextSol = new AtomicBoolean(false);
    AtomicBoolean nextNode = new AtomicBoolean(false);

    String[] frequency = new String[]{"1", "10", "100", "1000", "10000"};
    JComboBox refreshCB = new JComboBox(frequency);
    AtomicInteger node_wait = new AtomicInteger(1);
    JPanel leftpanel = new JPanel(new GridLayout(0,1));
    JLabel[] statistics = new JLabel[10];

    private static final int VAR = 1, CSTR = 2, SOL = 3, FAI = 4, BCK = 5, NOD = 6, RES = 7, TIM = 8, NpS = 9;
    private GraphPanel graphPanel;


    public GUI(Solver solver) {
        this.solver = solver;
        init();
    }

    public void init() {
        parameters = new Parameters(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(newControlPanel(), BorderLayout.WEST);
        //getContentPane().add(tabbedpanel, BorderLayout.CENTER);
        
        ChocoMetrics m = new ChocoMetrics(solver);
        graphPanel = new GraphPanel(m);
        getContentPane().add(graphPanel.newPanel(), BorderLayout.CENTER);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(80, 80, 800, 800);
        setVisible(true);
        
        doLayout();

    }

    private JPanel newControlPanel() {
        leftpanel.add(playB);
        playB.setEnabled(true);
        leftpanel.add(pauseB);
        pauseB.setEnabled(false);
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                play.set(!play.get());

                playB.setEnabled(!play.get());
                nextNodeB.setEnabled(!play.get());
                nextSolB.setEnabled(!play.get());

                pauseB.setEnabled(play.get());

                nextNode.set(false);
                nextSol.set(false);

            }
        };
        playB.addActionListener(actionListener);
        pauseB.addActionListener(actionListener);

        leftpanel.add(nextSolB);
        nextSolB.setEnabled(true);
        nextSolB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextSol.set(true);

                pauseB.setEnabled(true);

                playB.setEnabled(false);
                nextSolB.setEnabled(false);
                nextNodeB.setEnabled(false);
            }
        });

        leftpanel.add(nextNodeB);
        nextSolB.setEnabled(true);
        nextNodeB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextNode.set(true);

                pauseB.setEnabled(true);

                playB.setEnabled(false);
                nextSolB.setEnabled(false);
                nextNodeB.setEnabled(false);
            }
        });


        leftpanel.add(flushallB);
        flushallB.setEnabled(true);
        flushallB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parameters.flushNow();
            }
        });

        /*leftpanel.add(samplingCB);
        samplingCB.setEnabled(true);
        samplingCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sampling.set(!sampling.get());
                samplingCB.setSelected(sampling.get());
            }
        });*/
        leftpanel.add(new JLabel("Refresh freq. (p. node)"));
        leftpanel.add(refreshCB);
        refreshCB.setEnabled(true);
        refreshCB.setSelectedIndex(0);
        refreshCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String freq = frequency[refreshCB.getSelectedIndex()];
                node_wait.set(Integer.parseInt(freq));
            }
        });
        for (int i = 0; i < statistics.length; i++) {
            statistics[i] = new JLabel();
            statistics[i].setHorizontalAlignment(JTextField.RIGHT);
            leftpanel.add(statistics[i]);
        }
        printStatistics();
        return leftpanel;
    }

    private void printStatistics() {
        statistics[VAR].setText(pad(solver.getNbVars() + " vars"));
        statistics[CSTR].setText(pad(solver.getNbCstrs() + " cstrs"));
        statistics[SOL].setText(pad(solver.getMeasures().getSolutionCount() + " sols"));
        statistics[FAI].setText(pad(solver.getMeasures().getFailCount() + " fails"));
        statistics[BCK].setText(pad(solver.getMeasures().getBackTrackCount() + " bcks"));
        statistics[NOD].setText(pad(solver.getMeasures().getNodeCount() + " nodes"));
        statistics[RES].setText(pad(solver.getMeasures().getRestartCount() + " restarts"));
        statistics[TIM].setText(pad(String.format("%.1f s.", solver.getMeasures().getTimeCount())));
        statistics[NpS].setText(pad(String.format("%.2f n/s", solver.getMeasures().getNodeCount() / solver.getMeasures().getTimeCount())));
        solver.getMeasures().updateTimeCount(); // to deal with the first print
    }

    private static String pad(String txt) {
        return StringUtils.pad(txt, -20, " ");
    }

    public Solver getSolver() {
        return solver;
    }

    private void refreshButtons() {
        playB.setEnabled(!play.get());
        pauseB.setEnabled(play.get());
        nextNodeB.setEnabled(!play.get());
        nextSolB.setEnabled(!play.get());
    }

    @Override
    public void beforeInitialize() {
        parameters.plug(tabbedpanel);
        while (!play.get() && !nextNode.get() && !nextSol.get()) ;
        refreshButtons();
    }

    @Override
    public void afterInitialize() {
    }

    @Override
    public void beforeOpenNode() {
        while (!play.get() && !nextNode.get() && !nextSol.get()) ;
        printStatistics();
        nextNode.set(false);
        refreshButtons();
    }


    @Override
    public void afterOpenNode() {
    }

    @Override
    public void onSolution() {
        graphPanel.update();
        
        nextSol.set(false);
        refreshButtons();
    }

    public boolean canUpdate() {
        return ((solver.getMeasures().getNodeCount() % node_wait.get()) == 0);
    }
}
