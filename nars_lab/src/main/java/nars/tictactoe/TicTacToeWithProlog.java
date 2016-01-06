///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.tictactoe;
//
///*
// * Copyright (C) 2014 tc
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//import automenta.vivisect.Video;
//import automenta.vivisect.swing.NWindow;
//import nars.Events.FrameEnd;
//import nars.Events.OUT;
//import nars.Global;
//import nars.NAR;
//import nars.prolog.NARPrologMirror;
//import nars.budget.Budget;
//import nars.event.Reaction;
//import nars.gui.NARSwing;
//import nars.io.narsese.InvalidInputException;
//import nars.nal.Task;
//import nars.nal.concept.Concept;
//
//import nars.nal.nal8.Operator;
//import nars.nal.term.Atom;
//import nars.nal.term.Term;
//import nars.prototype.Default;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.*;
//import java.util.List;
//
//import static java.awt.BorderLayout.CENTER;
//import static java.awt.BorderLayout.SOUTH;
//
///**
// *
// * @author tc
// */
//public class TicTacToeWithProlog extends JPanel {
//
//    static { Global.DEBUG =true; }
//
//    final boolean HUMAN = false;
//    final boolean COMPUTER = true;
//    final boolean STARTING_PLAYER = HUMAN;
//
//    boolean playing = STARTING_PLAYER;
//
//    /**
//     * Creates new form play
//     */
//    public final NAR nar;
//
//    int[] field = new int[] {
//        0, 0, 0,
//        0, 0, 0,
//        0, 0, 0
//    };
//
//    private final JLabel status;
//
//    Set<Term> fieldTerms = new HashSet();
//
//
//
//    public TicTacToeWithProlog() {
//        super(new BorderLayout());
//
//        nar = new NAR(new Default().
//                setConceptBagSize(1000).
//                setSubconceptBagSize(10000).
//                setInternalExperience(null).
//                simulationTime());
//
//        new NARPrologMirror(nar, 0.9f, true, true, true);
//
//
//        nar.on(new AddO("^addO"));
//        nar.param.duration.set(5);
//        nar.param.outputVolume.set(0);
//        nar.param.shortTermMemoryHistory.set(8);
//        nar.setCyclesPerFrame(64);
//
//        new NARSwing(nar);
//        nar.on(new Reaction() {
//
//            @Override
//            public void event(Class event, Object[] args) {
//                nar.memory.timeSimulationAdd(1);
//            }
//
//        }, FrameEnd.class);
//        nar.start(40);
//
//
//        JPanel menu = new JPanel(new FlowLayout());
//
//        JButton resetButton = new JButton("RESET");
//        resetButton.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                reset();
//            }
//        });
//        menu.add(resetButton);
//
//        status = new JLabel("");
//        menu.add(status);
//
//        JButton teachButton = new JButton("Re-Explain rules to NARS");
//        teachButton.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                teach();
//            }
//        });
//        menu.add(teachButton);
//
//        add(menu, SOUTH);
//
//
//        add(new GridButtonPanel(nar, 3, 3) {
//
//            @Override
//            public Concept initTerm(int x, int y) {
//                Term t = Atom.get(Integer.toString(y * 3 + x));
//                fieldTerms.add(t);
//                return nar.memory.conceptualize(new Budget(0.5f, 0.5f, 0.5f), t);
//            }
//
//            @Override
//            public void onMouseClick(GridButtonPanel.ConceptButton c, boolean press, int wheelRotation) {
//                int x = c.bx;
//                int y = c.by;
//                int i = index(c);
//
//
//                if ((playing != HUMAN) || field[i] != 0) {
//                    return;
//                }
//
//                nar.input("<(*," + i + ",human) --> move>. :|:");
//                c.setText("X");
//                field[i] = 1;
//                playing = !playing;
//                updateField();
//            }
//
//            @Override
//            public void onMouse(GridButtonPanel.ConceptButton c, boolean press, boolean hover, int wheel) {
//                //enable NARS to cheat a bit by watching where mouse cursor is
//                /*int i = index(c);
//                nar.addInput("$0.10$ <" + i + " --> moused>. :|:");*/
//            }
//
//
//            //TODO
//            /*
//            @Override
//            public void repaintOverlay(Graphics g) {
//                Set<Term> h = new HashSet();
//                for (Concept c : nar.memory.concepts) {
//                    h.clear();
//                    if (!(c.term instanceof CompoundTerm)) continue;
//                    for (Term t : ((CompoundTerm)(c.term)).term)
//                        h.add(t);
//                    h.retainAll(fieldTerms);
//
//                    if (h.size() >= 2)
//                        System.out.print(h + " ");
//                }
//                System.out.println();
//            }
//            */
//
//            public Color getBackgroundColor(float priority) {
//                return Color.getHSBColor(0.2f + priority * 0.2f, 0.75f, 0.5f + priority * 0.5f);
//            }
//
//            Color blue = new Color(0.5f, 0.5f, 1f);
//            Color red = new Color(1f, 0.5f, 0.5f);
//
//            @Override
//            public void repaintButton(GridButtonPanel.ConceptButton b) {
//                b.setBackground(getBackgroundColor(b.concept.getPriority()));
//                b.setFont(buttonFont);
//
//                String s = "";
//                switch (field[index(b)]) {
//                    case 1: s = "O"; b.setForeground(blue); break;
//                    case 2: s = "X"; b.setForeground(red); break;
//                }
//
//                b.setText(s);
//            }
//
//        }, CENTER);
//
//
//
//
//        reset();
//
//    }
//
//    public int index(GridButtonPanel.ConceptButton cp) {
//        return cp.bx + 3 * cp.by;
//    }
//
//
//
//    public class AddO extends Operator {
//
//        public AddO(String name) {
//            super(name);
//        }
//
//        @Override
//        protected List<Task> execute(Operation operation, Term[] args) {
//
//            int i = -1;
//            try {
//                i = Integer.parseInt( args[0].toString() );
//            }
//            catch (Throwable e) {
//                return inputSuccess(false);
//            }
//
//            if (!((i >=0) && (i <=8)))
//                return inputSuccess(false);
//
//            if (playing == COMPUTER) {
//
//                boolean success = false;
//
//                if (field[i]==0) {
//                    field[i] = 2;
//                    success = true;
//                }
//
//                if (success) {
//                    nar.emit(TicTacToeWithProlog.class, "NARS plays: " + i);
//                    nar.input("<(*," + i + ",nars) --> move>. :|:");
//                    playing = !playing;
//
//                    System.out.println( operation.getTask().getExplanation() );
//
//                    updateField();
//                    return inputSuccess(true);
//                }
//                else {
//                    nar.emit(TicTacToeWithProlog.class, "NARS selects invalid cell: " + i);
//                    return inputSuccess(false, ("<" + i + " --> human>. :|:"));
//                }
//            }
//            else {
//                nar.emit(TicTacToeWithProlog.class, "NARS not playing, but wants: " + i);
//                return inputSuccess(false, "<(*,undecided,human) --> move>. :|:");
//            }
//
//        }
//
//
//
//        private List<Task> inputSuccess(boolean succes, String... reason) {
//
//            List<Task> l = new ArrayList();
//
//            Task t = null;
//            try {
//                for (String r : reason)
//                    l.add(nar.task(r));
//                String truth = (succes ? "%1.00;" : "%0.00;") + "0.95%";
//                t = nar.task("<input --> succeeded>. :|: " + truth);
//                l.add(t);
//            } catch (InvalidInputException e) {
//                e.printStackTrace();
//            }
//
//            return l;
//
//        }
//
//    }
//
//    public int[][] howToWin = {
//
//        { 0, 1, 2 },
//        { 3, 4, 5 },
//        { 6, 7, 8 },
//
//        { 0, 3, 6 },
//        { 1, 4, 7 },
//        { 2, 5, 8 },
//
//        { 0, 4, 8 },
//        { 2, 4, 6 }
//
//    };
//
//    public void updateField() {
//        if (playing == COMPUTER) {
//            status.setText("NARS Turn");
//            nar.input("<(*,undecided,nars) --> move>. :|:");
//        }
//        else {
//            status.setText("Humans Turn");
//            nar.input("<(*,undecided,human) --> move>. :|:");
//        }
//
//        Boolean winner = null;
//
//
//
//        for (int p = 1; p <= 2; p++) { // 1=human, 2=nars
//            for (int[] h : howToWin) {
//                if (field[h[0]]!=p) continue;
//                if (field[h[1]]!=p) continue;
//                if (field[h[2]]!=p) continue;
//                if (p == 1) winner = HUMAN;
//                else if (p == 2) winner = COMPUTER;
//                else
//                    winner = null;
//            }
//        }
//
//        if (winner == null) {
//            String s = "";
//            for (int i = 0; i < 9; i++) {
//                String f = "empty";
//                switch (field[i]) {
//                    case 1: f = "human"; break;
//                    case 2: f = "nars";  break;
//                }
//                s += "<" + i + " --> " + f + ">. :|:\n";
//            }
//            nar.input(s);
//        }
//        else if (winner == HUMAN) {
//            status.setText("Human wins");
//            nar.emit(OUT.class, "Human wins");
//            nar.input("<human --> win>. :|: %1.0;0.99%");
//            nar.input("<nars --> win>. :|: %0.0;0.99%");
//        }
//        else if (winner == COMPUTER) {
//            status.setText("NARS wins");
//            nar.emit(OUT.class, "NARS wins");
//            nar.input("<human --> win>. :|: %0.0;0.99%");
//            nar.input("<nars --> win>. :|: %1.0;0.99%");
//        }
//    }
//
//
//
//    public void reset() {
//        playing = STARTING_PLAYER;
//        Arrays.fill(field, 0);
//        nar.input("<game --> reset>. :|:");
//        teach();
//        updateField();
//    }
//
//
//
//    public void teach() {
//
//        String rules = "";
//        //rules+=("<nars --> win>! %1.0;0.99%\n");
//
//        //+"<(^addO,$1) =/> <input --> succeeded>>.\n"); //usually input succeeds
//        //+"<(&/,<1 --> set>,(^addO,$1)) =/> (--,<input --> succeeded>)>.\n"); //usually input succeeds but not when it was set by player cause overwrite is not valid
//        //+"<(&/,(^addO,$1),(^addO,$1)) =/> (--,<input --> succeeded>)>.\n"); //also overwriting on own is not valid
//
//        for (int[] h : howToWin) {
//            int a = h[0];
//            int b = h[1];
//            int c = h[2];
//            //rules+=("<(&|,(^addO," + a + "),<input --> succeeded>,(^addO," + b + "),<input --> succeeded>,(^addO," + c + "),<input --> succeeded>) =/> <nars --> win>>.\n");
//            rules+=("$0.80;0.95$ <(&&,<" + a + " --> $1>,<" + b + " --> $1>,<" + c + " --> $1>) ==> <$1 --> win>>. %1.00;0.99%\n");
//        }
//
//        //for NAL9 (anticipate)
//        //DOESNT PARSE THIS CORRECTLY
//        /*
//        if (nar.memory.getOperator("^anticipate")!=null) {
//            rules+=("< (&/,(--,<$1 --> empty>),(^add0,$1)) =/> (--,<input --> succeeded>)>.\n");
//            rules+=("< (&/,(--,<$1 --> field>),(^add0,$1)) =/> (--,<input --> succeeded>)>.\n");
//        }*/
//
//        //no current winners
//        rules+=("<nars --> win>. %0.50;0.99%\n");
//        rules+=("<human --> win>. %0.50;0.99%\n");
//
//        rules+=("$0.99;0.90$ <nars --> win>! %1.00;0.99%\n");
//        rules+=("$0.99;0.90$ <human --> win>! %0.00;0.99%\n");
//        rules+=("$0.70;0.85$ <input --> succeeded>!\n");
//
//        rules+=("(&/,<#1 --> field>,(^addO,#1))!\n"); //doing something is also a goal :D
//
//        rules+=("(^addO,0)! %1.0;0.5%\n");
//        rules+=("(^addO,1)! %1.0;0.5%\n");
//        rules+=("(^addO,2)! %1.0;0.5%\n");
//        rules+=("(^addO,3)! %1.0;0.5%\n");
//        rules+=("(^addO,4)! %1.0;0.5%\n");
//        rules+=("(^addO,5)! %1.0;0.5%\n");
//        rules+=("(^addO,6)! %1.0;0.5%\n");
//        rules+=("(^addO,7)! %1.0;0.5%\n");
//        rules+=("(^addO,8)! %1.0;0.5%\n");
//
//        rules+=("<{nars,human} --> players>.\n");
//        rules+=("<field --> [nars,human,empty]>.\n");
//        rules+=("(||,<human --> win>,<nars --> win>).\n");
//        rules+=("<{0,1,2,3,4,5,6,7,8} --> field>.\n");
//        /*rules+=("<1 --> field>.\n");
//        rules+=("<2 --> field>.\n");
//        rules+=("<3 --> field>.\n");
//        rules+=("<4 --> field>.\n");
//        rules+=("<5 --> field>.\n");
//        rules+=("<6 --> field>.\n");
//        rules+=("<7 --> field>.\n");
//        rules+=("<8 --> field>.\n");*/
//
//
//        String[] rs = rules.split("\n");
//        for (String x : rs) {
//            nar.input(x);
//        }
//
//        updateField();
//
//    }
//
//
//    private static final Font buttonFont = Video.monofont.deriveFont(Font.BOLD).deriveFont(34f);
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//
//        /* Set the Nimbus look and feel */
//        Video.themeInvert();
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new NWindow("NARTacToe", new TicTacToeWithProlog()).show(400,400,true);
//            }
//        });
//
//    }
//
// }
