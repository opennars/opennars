package nars.tuprolog.gui.spyframe;

import nars.nal.AbstractSubGoalTree;
import nars.nal.term.Term;
import nars.tuprolog.*;
import nars.tuprolog.event.SpyEvent;
import nars.tuprolog.event.SpyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI-Window for tracing the solving process of a prolog goal.
 * This Frame runs its own prolog process and is its only SpyListener.
 * The prolog process is suspended at every step.
 *
 * @author franz.beslmeisl at googlemail.com
 */
public class SpyFrame extends JFrame implements ActionListener, SpyListener {

    private static final long serialVersionUID = 1L;
    /**
     * An anonymous singleton instance building a tree out of a list of ExecutionContexts.
     */
    public static final ToTree<List<ExecutionContext>> contexts2tree = new ToTree<List<ExecutionContext>>() {
        /**Constructs a tree using the information given in SpyEvents. Every entry
         * in the provided list is supposed to have a clause and some subgoals, one
         * of which is the current goal. The name of the clause is displayed as the
         * current subgoal of one level up whereas the arguments of the clause
         * become displayed in the form of subgoals.
         * All this can be displayed as one prolog term. The corresponding code
         * is therefore used.
         */
        @Override
        public Node makeTreeFrom(List<ExecutionContext> eclist) {
            return TermFrame.term2tree.makeTreeFrom(makeTermFrom(eclist));
        }

        private ArrayList<PTerm> elementi;

        Term makeTermFrom(List<ExecutionContext> eclist) {
            int levels = eclist.size();
            if (levels < 1) return null;
            Term bottom = null;
            for (int i = 0; i < levels; i++) {
                ExecutionContext ec = eclist.get(i);
                Struct c = ec.getClause();
                if (c instanceof Struct) {
                    Struct s = (Struct) c;
                    String name = s.getName();
                    ArrayList<Term> sub = new ArrayList<>();
                    for (AbstractSubGoalTree sgt : ec.getSubGoalStore().getSubGoals()) {
                        if (sgt.isRoot()) {
                            //SubGoalTree
                            cerca(sgt);
                            for (Term t : elementi) {
                                sub.add(t);
                            }
                        } else {
                            //SubGoalElement
                            sub.add(((SubGoalElement) sgt).getValue());
                        }
                    }
                    if (":-".equals(name))
                        sub.add(0, i + 1 < levels ? eclist.get(i + 1).getCurrentGoal() : s.getTermXP(0));
                    else if (",".equals(name)) name = " ";//don't want to build the ,-tree
                    else name = null;//indicates that we have a normal compound
                    int pos = sub.indexOf(ec.getCurrentGoal());
                    if (bottom != null) sub.set(pos, bottom);
                    if (name == null) bottom = sub.get(0);
                    else {
                        PTerm[] subt = new PTerm[sub.size()];
                        bottom = new Struct(name, sub.toArray(subt));
                    }
                } else bottom = c;
            }
            return bottom;//is at last the top
        }

        private void cerca(AbstractSubGoalTree sgt) {
            elementi = new ArrayList<>();
            int dim = ((SubGoalTree) sgt).size();
            for (int i = 0; i < dim; i++) {
                AbstractSubGoalTree ab = ((SubGoalTree) sgt).getChild(i);
                if (ab.isLeaf()) {
                    elementi.add(((SubGoalElement) ab).getValue());
                } else {
                    cerca(ab);
                }
            }

        }
    };
    Prolog prolog;
    Thread pprocess;
    JTextField number;
    JTextArea results;
    JButton next;
    int steps;
    Tree<List<ExecutionContext>> tree;

    /**
     * Creates the main window for spying a prolog goal finding process.
     *
     * @param theory for the prolog engine.
     * @param goal   the prolog term to be tested.
     * @throws InvalidTheoryException if we have no valid prolog theory.
     */
    public SpyFrame(Theory theory, final PTerm goal) throws InvalidTheoryException, InvalidLibraryException {
        //START of visible stuff
        super("SpyFrame");
        Container c = getContentPane();
        //Panel at NORTH containing the input of steps
        JPanel topp = new JPanel();

        topp.add(new JLabel("Number of steps to jump"));
        number = new JTextField("1", 2);
        topp.add(number);
        number.addActionListener(this);

        next = new JButton("Next");
        topp.add(next);
        next.addActionListener(this);

        steps = 1;
        c.add(topp, BorderLayout.NORTH);
        //JSplitPane at CENTER containing the tree and the results
        tree = new Tree<>(contexts2tree);
        results = new JTextArea("", 4, 40);
        JSplitPane jsp = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(results)
        );
        c.add(jsp, BorderLayout.CENTER);
        //get the screen dimensions to fill the screen
        //Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        //jsp.setDividerLocation(screen.height-200);
        //setSize(screen);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int h = screen.height - (screen.height / 4);
        int l = screen.width - (screen.width / 2);
        jsp.setDividerLocation(h - 250);
        setSize(l, h);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //END of the visible stuff
        prolog = new DefaultProlog();
        prolog.setTheory(theory);
        prolog.addSpyListener(this);
        prolog.setSpy(true);
        pprocess = new Thread() {
            @Override
            public void run() {
                PTerm sol;
                SolveInfo sinfo = prolog.solve(goal);
                if (sinfo != null) {
                    while (sinfo.isSuccess())
                        try {
                            sol = sinfo.getSolution();
                            results.append("\nsolution: " + sol);
                            results.append("\ninfo:     " + sinfo);
                            if (sinfo.hasOpenAlternatives()) sinfo = prolog.solveNext();
                            else break;
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                    results.append("\nNo more solutions.");
                    next.setEnabled(false);

                }
            }
        };
    }

    /**
     * Continues the prolog process and sets the number of steps to be skipped.
     * A step is done by every prolog-Call.
     *
     * @param e not used because at the moment we have only the input field
     *          producing this event. This might change in the future.
     */
    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        if (pprocess.getState() == Thread.State.NEW) pprocess.start();
        try {
            steps = Integer.parseInt(number.getText());
            number.setText("1");
        } catch (NumberFormatException ex) {
            steps = 1;
        }
        if (steps < 1) steps = 1;
        notifyAll();
    }

    /**
     * Display the spied information in form of a tree. This method reacts only
     * on prolog-Calls and skips even those if the number of steps set in
     * {@link actionPerformed} is bigger than 1. In this case the number is
     * decremented to skip these steps. After display the prolog process is
     * suspended until the user presses enter.
     *
     * @param e information about the actual state of the prolog process.
     */
    @Override
    public synchronized void onSpy(SpyEvent e) {
        Engine.State state = e.getSnapshot();
        if (state == null || !"Call".equals(state.getNextStateName())) return;
        if (--steps > 0) return;
        tree.setStructure(state.getExecutionStack());
        number.setText("1");
        while (steps < 1)
            try {
                wait();
            } catch (InterruptedException ex) {
            }
    }

    /**
     * Spies the solving process of a prolog goal.
     *
     * @param args array of length two containing the filename of the theory
     *             and the goal.
     * @throws Exception if the theory or the goal are nonsense.
     */
    public static void main(String[] args) throws Exception {
        Theory theory = new Theory(new FileInputStream(args[0]));
        PTerm goal = PTerm.createTerm(args[1]);
        System.out.println("goal:" + goal);
        System.out.println("in given theory\n---------------\n" + theory);
        SpyFrame tf = new SpyFrame(theory, goal);
        tf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}