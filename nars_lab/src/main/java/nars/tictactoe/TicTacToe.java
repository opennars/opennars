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
//import automenta.vivisect.swing.NWindow;
//
//import nars.NAR;
//import nars.Video;
//import nars.budget.Budget;
//import nars.clock.SimulatedClock;
//import nars.concept.Concept;
//
//import nars.nal.nal8.operator.SynchOperator;
//import nars.nar.Default;
//import nars.task.Task;
//import nars.term.Atom;
//import nars.term.Term;
//import nars.util.event.Reaction;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static java.awt.BorderLayout.CENTER;
//import static java.awt.BorderLayout.SOUTH;
//
///**
// *
// * @author tc
// */
//public class TicTacToe extends JPanel {
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
//    public TicTacToe() {
//        super(new BorderLayout());
//
//        SimulatedClock clock;
//        nar = new NAR(new Default(1000, 1, 3).
//                setClock(clock = new SimulatedClock()));
//
//        nar.on(new AddO());
//        (nar.param).duration.set(100);
//        (nar.param).outputVolume.set(0);
//
//        new NARSwing(nar);
//        nar.on(new Reaction<Class,Object[]>() {
//
//            @Override
//            public void event(Class event, Object[] args) {
//                clock.add(500);
//            }
//
//        }, FrameEnd.class);
//        nar.start(30);
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
//                Term t = Atom.the(Integer.toString(y * 3 + x));
//                fieldTerms.add(t);
//                return nar.memory.conceptualize(t, new Budget(0.5f, 0.5f, 0.5f));
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
//                return Color.getHSBColor(0.1f + priority * 0.4f, 0.75f, 0.5f + priority * 0.5f);
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
//        init();
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
//    public class AddO extends SynchOperator {
//
//        public AddO() {
//            super("^add0");
//        }
//
//        @Override
//        public List<Task> apply(Operation operation) {
//
//            int i = -1;
//            try {
//                i = Integer.parseInt( operation.arg(0).toString() );
//            }
//            catch (Throwable e) {
//                return null;
//            }
//
//            if (!((i >=0) && (i <=8)))
//                return null;
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
//                    nar.emit(TicTacToe.class, "NARS plays: " + i);
//                    nar.input("<input --> succeeded>. :|: %" + (success ? "1.00" : "0.00") + "%");
//                    nar.input("<(*," + i + ",nars) --> move>. :|:");
//                    playing = !playing;
//
//
//                    updateField();
//
//                }
//                else {
//                    nar.emit(TicTacToe.class, "NARS selects invalid cell: " + i);
//                    nar.input("(--,<input --> succeeded>). :|:");
//                }
//            }
//            else {
//                nar.emit(TicTacToe.class, "NARS not playing, but wants: " + i);
//                nar.input("(--,<input --> succeeded>). :|:");
//            }
//
//
//            System.out.println(operation.getTask().getExplanation());
//
//            return null;
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
//        if (playing == COMPUTER)
//            status.setText("NARS Turn");
//        else
//            status.setText("Humans Turn");
//
//        Boolean winner = null;
//
//        for (int p = 1; p <= 2; p++) { // 1=human, 2=nars
//            for (int[] h : howToWin) {
//                if (field[h[0]]!=p) continue;
//                if (field[h[1]]!=p) continue;
//                if (field[h[2]]!=p) continue;
//                winner = p == 1 ? HUMAN : COMPUTER;
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
//        nar.input("<empty --> win>. :|:");
//        teach();
//        updateField();
//    }
//
//
//
//    public void init() {
//
//                nar.input("<nars --> win>! %1.0;0.99%");
//                nar.input("<nars --> win>! :|: %1.0;0.99%");
//
//                nar.input("<human --> win>! %0.0;0.99%");
//                nar.input("<human --> win>! :|: %0.0;0.99%");
//                nar.input("<empty --> win>! %0.0;0.99%");
//                nar.input("<empty --> win>! :|: %0.0;0.99%");
//
//                nar.input("<input --> succeeded>! %1.0;0.99%");
//                nar.input("<input --> succeeded>! :|: %1.0;0.99%");
//
//                nar.input("<{nars,human,empty} <-> field>.");
//                nar.input("<<$1 --> empty> =/> (||,<$1 --> nars>,<$1 --> human>)>.");
//                nar.input("<<$1 --> nars> =/> (--,<$1 --> human>)>.");
//                nar.input("<<$1 --> human> =/> (--,<$1 --> nars>)>.");
//
//
//
//                nar.input("<<nars --> win> <-> (--,<human --> win>)>.");
//                nar.input("<<human --> win> <-> (--,<nars --> win>)>.");
//
//                nar.input("<(&/,<$1 --> empty>, (^add0,$1,SELF)) =/> <input --> succeeded>>.");
//
//
//                nar.input("<{0,1,2,3,4,5,6,7,8} --> field>.");
//
//
//        //rules+=("<<#1 --> field> =/> (^addO,#1,SELF)>. %0.5;0.5%\n"); //doing something is also a goal :D
//
//        for (int[] h : howToWin) {
//            int a = h[0];
//            int b = h[1];
//            int c = h[2];
//            //rules+=("<(&|,(^addO," + a + "),<input --> succeeded>,(^addO," + b + "),<input --> succeeded>,(^addO," + c + "),<input --> succeeded>) =/> <nars --> win>>.\n");
//            String tt = "<(&&,<" + a + " --> $1>,<" + b + " --> $1>,<" + c + " --> $1>) =/> <$1 --> win>>";
//            nar.input(tt + ". %1.0;0.99%");
//        }
//
//
//    }
//
//    public void teach() {
//
//        String rules = "";
//
//        //+"<(^addO,$1) =/> <input --> succeeded>>.\n"); //usually input succeeds
//        //+"<(&/,<1 --> set>,(^addO,$1)) =/> (--,<input --> succeeded>)>.\n"); //usually input succeeds but not when it was set by player cause overwrite is not valid
//        //+"<(&/,(^addO,$1),(^addO,$1)) =/> (--,<input --> succeeded>)>.\n"); //also overwriting on own is not valid
//
//
//        //for NAL9 (anticipate)
////        if (nar.memory.operator("^anticipate")!=null) {
////            rules+=("<(&/,(--,<$1 --> empty>),(^add0,$1)) =/> (--,<input --> succeeded>)>>.\n");
////            rules+=("<(&/,(--,<$1 --> field>),(^add0,$1)) =/> (--,<input --> succeeded>)>.\n");
////        }
//
//
//        //rules+=("<nars --> win>! %1.0;0.99%\n");
//        //rules+=("<human --> win>! %0.0;0.99%\n");
//
//
//        for (int i = 0; i < 9; i++) {
//            rules += ("add0(" + i + ")@\n");
//            rules += ("add0(" + i + ")! %1.0;0.75%\n");
//        }
//
//
//        //rules+=("<{human,nars,empty} --> win>.\n");
//
//
//        nar.input(rules);
//
//        updateField();
//
//    }
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
//                new NWindow("NARTacToe", new TicTacToe()).show(400,400,true);
//            }
//        });
//
//    }
//
// }
