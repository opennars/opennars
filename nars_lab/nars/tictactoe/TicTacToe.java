/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.tictactoe;

/*
 * Copyright (C) 2014 tc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nars.NARPrologMirror;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Discretinuous;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.io.Output.OUT;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 *
 * @author tc
 */
public class TicTacToe extends JPanel {

    final boolean HUMAN = false;
    final boolean COMPUTER = true;
    final boolean STARTING_PLAYER = HUMAN;
    
    boolean playing = STARTING_PLAYER;
    
    /**
     * Creates new form play
     */
    public final NAR nar;

    int[] field = new int[] {
        0, 0, 0,
        0, 0, 0,
        0, 0, 0
    };
    
    private final JLabel status;

    Set<Term> fieldTerms = new HashSet();
    
    
    
    public TicTacToe() {
        super(new BorderLayout());

        nar = new Discretinuous().
                setConceptBagSize(1000).
                setSubconceptBagSize(10000).
                simulationTime().build();
        
        new NARPrologMirror(nar, 0.75f, true).temporal(true, false);
        new NARPrologMirror(nar, 0.75f, true).temporal(false, true);
        
        nar.memory.addOperator(new AddO("^addO"));        
        (nar.param).duration.set(1000);
        (nar.param).noiseLevel.set(0);
        
        new NARSwing(nar);    
        nar.start(30, 2000);
        
        JPanel menu = new JPanel(new FlowLayout());

        JButton resetButton = new JButton("RESET");
        resetButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                reset();
            }            
        });        
        menu.add(resetButton);

        status = new JLabel("");
        menu.add(status);
        
        JButton teachButton = new JButton("TEACH");
        teachButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                teach();
            }            
        });
        menu.add(teachButton);
        
        add(menu, SOUTH);
        
        
        add(new GridButtonPanel(nar, 3, 3) {

            @Override
            public Concept initTerm(int x, int y) {
                Term t = new Term( Integer.toString(y * 3 + x) );
                fieldTerms.add(t);
                return nar.memory.conceptualize(new BudgetValue(0.5f, 0.5f, 0.5f), t);
            }

            @Override
            public void onMouseClick(GridButtonPanel.ConceptButton c, boolean press, int wheelRotation) {
                int x = c.bx;
                int y = c.by;
                int i = index(c);

                
                if ((playing != HUMAN) || field[i] != 0) {
                    return;
                }
                
                nar.addInput("<(*," + i + ",human) --> move>. :|:");
                c.setText("X");
                field[i] = 1;
                playing = !playing;
                updateField();
            }

            @Override
            public void onMouse(GridButtonPanel.ConceptButton c, boolean press, boolean hover, int wheel) {
                //enable NARS to cheat a bit by watching where mouse cursor is
                /*int i = index(c);                
                nar.addInput("$0.10$ <" + i + " --> moused>. :|:");*/
            }

            
            //TODO
            /*
            @Override
            public void repaintOverlay(Graphics g) {
                Set<Term> h = new HashSet();
                for (Concept c : nar.memory.concepts) {
                    h.clear();
                    if (!(c.term instanceof CompoundTerm)) continue;
                    for (Term t : ((CompoundTerm)(c.term)).term) 
                        h.add(t);
                    h.retainAll(fieldTerms);
                    
                    if (h.size() >= 2)
                        System.out.print(h + " ");
                }
                System.out.println();
            }
            */

            public Color getBackgroundColor(float priority) {
                return Color.getHSBColor(0.2f + priority * 0.2f, 0.75f, 0.5f + priority * 0.5f);
            }
            
            Color blue = new Color(0.5f, 0.5f, 1f);
            Color red = new Color(1f, 0.5f, 0.5f);
            
            @Override
            public void repaintButton(GridButtonPanel.ConceptButton b) {
                b.setBackground(getBackgroundColor(b.concept.getPriority()));
                b.setFont(buttonFont);
                
                String s = "";
                switch (field[index(b)]) {                    
                    case 1: s = "O"; b.setForeground(blue); break;
                    case 2: s = "X"; b.setForeground(red); break;
                }
                
                b.setText(s);
            }
            
        }, CENTER);       
        
        
        
        
        reset();
        
    }

    public int index(GridButtonPanel.ConceptButton cp) {
        return cp.bx + 3 * cp.by;
    }
    

    public class AddO extends Operator {

        public AddO(String name) {
            super(name);
        }

        @Override
        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
            
            int i = -1;
            try {
                i = Integer.parseInt( args[0].toString() );
            }
            catch (Throwable e) {
                return null;
            }
            
            if (!((i >=0) && (i <=8)))
                return null;
            
            if (playing == COMPUTER) {
                
                boolean success = false;
                
                if (field[i]==0) {
                    field[i] = 2;                
                    success = true;
                }
                
                if (success) {
                    nar.emit(TicTacToe.class, "NARS plays: " + i);
                    nar.addInput("<input --> succeeded>. :|: %" + (success ? "1.00" : "0.00") + "%" );
                    nar.addInput("<(*," + i + ",nars) --> move>. :|:");
                    playing = !playing;
                    
                    System.out.println( operation.getTask().getExplanation() );

                    updateField();

                }
                else {
                    nar.emit(TicTacToe.class, "NARS selects invalid cell: " + i);
                    nar.addInput("(--,<input --> succeeded>). :|:");
                }
            }
            else {
                nar.emit(TicTacToe.class, "NARS not playing, but wants: " + i);    
                nar.addInput("(--,<input --> succeeded>). :|:");
            }
            
            
            return null;
        }

    }

    public int[][] howToWin = {
        
        { 0, 1, 2 }, 
        { 3, 4, 5 }, 
        { 6, 7, 8 },
        
        { 0, 3, 6 },
        { 1, 4, 7 },
        { 2, 5, 8 },
        
        { 0, 4, 8 },
        { 2, 4, 6 }            
        
    };
    
    public void updateField() {
        if (playing == COMPUTER)
            status.setText("Thinking");
        else
            status.setText("Play");
                    
        Boolean winner = null;

        for (int p = 1; p <= 2; p++) { // 1=human, 2=nars            
            for (int[] h : howToWin) {
                if (field[h[0]]!=p) continue;
                if (field[h[1]]!=p) continue;
                if (field[h[2]]!=p) continue;
                winner = p == 1 ? HUMAN : COMPUTER;
            }
        }

        if (winner == null) {
            String s = "";
            for (int i = 0; i < 9; i++) {
                String f = "empty";
                switch (field[i]) {
                    case 1: f = "human"; break;
                    case 2: f = "nars";  break;
                }
                s += "<" + i + " --> " + f + ">. :|:\n";
            }
            nar.addInput(s);                       
        }
        else if (winner == HUMAN) {
            status.setText("Human wins");
            nar.emit(OUT.class, "Human wins");
            nar.addInput("<human --> win>. :|: %1.0;0.99%");
            nar.addInput("<nars --> win>. :|: %0.0;0.99%");
        }
        else if (winner == COMPUTER) {
            status.setText("NARS wins");
            nar.emit(OUT.class, "NARS wins");
            nar.addInput("<human --> win>. :|: %0.0;0.99%");
            nar.addInput("<nars --> win>. :|: %1.0;0.99%");
        }
    }



    public void reset() {
        playing = STARTING_PLAYER;
        Arrays.fill(field, 0);
        nar.addInput("<game --> reset>. :|:");
        teach();
        updateField();
    }



    public void teach() {
        
        String rules = "";
        rules+=("<nars --> win>! %1.0;0.99%\n");

        //+"<(^addO,$1) =/> <input --> succeeded>>.\n"); //usually input succeeds
        //+"<(&/,<1 --> set>,(^addO,$1)) =/> (--,<input --> succeeded>)>.\n"); //usually input succeeds but not when it was set by player cause overwrite is not valid
        //+"<(&/,(^addO,$1),(^addO,$1)) =/> (--,<input --> succeeded>)>.\n"); //also overwriting on own is not valid
                
        for (int[] h : howToWin) {                
            int a = h[0];
            int b = h[1];
            int c = h[2];
            rules+=("<(&|,(^addO," + a + "),<input --> succeeded>,(^addO," + b + "),<input --> succeeded>,(^addO," + c + "),<input --> succeeded>) =/> <nars --> win>>.\n");
            rules+=("<(&|,<" + a + " --> $1>,<" + b + " --> $1>,<" + c + " --> $1>) =/> <$1 --> win>>.\n");
        }
        
        //for NAL9 (anticipate)
        if(Parameters.INTERNAL_EXPERIENCE_FULL) {
            rules+=("<(&/,(--,<$1 --> empty>),(^add0,$1)) =/> (--,<input --> succeeded>)>>.\n");
            rules+=("<(&/,(--,<$1 --> field>),(^add0,$1)) =/> (--,<input --> succeeded>)>.\n");
        }
        
        rules+=("<nars --> win>! %1.0;0.99%\n");
        rules+=("<human --> win>! %0.0;0.99%\n");

        rules+=("(&/,<#1 --> field>,(^addO,#1))!\n"); //doing something is also a goal :D
        

        
        rules+=("(^addO,0)! %1.0;0.7%\n");
        rules+=("(^addO,1)! %1.0;0.7%\n");
        rules+=("(^addO,2)! %1.0;0.7%\n");
        rules+=("(^addO,3)! %1.0;0.7%\n");
        rules+=("(^addO,4)! %1.0;0.7%\n");
        rules+=("(^addO,5)! %1.0;0.7%\n");
        rules+=("(^addO,6)! %1.0;0.7%\n");
        rules+=("(^addO,7)! %1.0;0.7%\n");
        rules+=("(^addO,8)! %1.0;0.7%\n");
         
        
        rules+=("<{nars,human,empty} <-> field>.\n");        
        rules+=("(||,<human --> win>,<nars --> win>).\n");
        rules+=("<0 --> field>.\n");
        rules+=("<1 --> field>.\n");
        rules+=("<2 --> field>.\n");
        rules+=("<3 --> field>.\n");
        rules+=("<4 --> field>.\n");
        rules+=("<5 --> field>.\n");
        rules+=("<6 --> field>.\n");
        rules+=("<7 --> field>.\n");
        rules+=("<8 --> field>.\n");
         
        rules+=("<input --> succeeded>!\n");
        
        nar.addInput(rules);
        
        updateField();

    }
    
    private static final Font buttonFont = NARSwing.monofont.deriveFont(Font.BOLD).deriveFont(34f);

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        NARSwing.themeInvert();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NWindow("NARTacToe", new TicTacToe()).show(400,400,true);
            }
        });

    }

}
