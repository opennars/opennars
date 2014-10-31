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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.io.Output.OUT;
import nars.language.CompoundTerm;
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
    
    boolean playing = HUMAN;
    
    /**
     * Creates new form play
     */
    public final NAR nar;

    int[] field = new int[] {
        0, 0, 0,
        0, 0, 0,
        0, 0, 0
    };

    
    
    public int index(GridButtonPanel.ConceptButton cp) {
        return cp.bx + 3 * cp.by;
    }
    
    Set<Term> fieldTerms = new HashSet();
    
    public TicTacToe() {
        super(new BorderLayout());

        
                
        nar = new DefaultNARBuilder().realTime().build();
        nar.memory.addOperator(new AddO("^addO"));
        nar.param().duration.set(1000);
        nar.param().noiseLevel.set(0);
                
        addStartKnowledge();
        nar.addInput("<game --> reset>. :|:");
        
        
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
                
                nar.addInput("<" + i + " --> set>. :|:");
                c.setText("X");
                field[i] = 1;
                updateField();
                playing = !playing;
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
            
            @Override
            public void repaintButton(GridButtonPanel.ConceptButton b) {
                b.setBackground(getBackgroundColor(b.concept.getPriority()));
                b.setFont(buttonFont);
                
                String s = "";
                switch (field[index(b)]) {                    
                    case 1: s = "O"; break;
                    case 2: s = "X"; break;
                }
                b.setText(s);
            }
            
            

            
            
        }, CENTER);

//        jButton1.setBackground(new java.awt.Color(0, 0, 0));
//        jButton1.setForeground(new java.awt.Color(255, 255, 255));
//        jButton1.setText("Reset");
//        jButton1.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                jButton1ActionPerformed(evt);
//            }
//        });
//
//        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
//        jLabel1.setText("playing...");
//        jLabel1.setToolTipText("");
//
//        jButton12.setBackground(new java.awt.Color(0, 0, 0));
//        jButton12.setForeground(new java.awt.Color(255, 255, 255));
//        jButton12.setText("remind game");
//        jButton12.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                jButton12ActionPerformed(evt);
//            }
//        });

        new NARSwing(nar);    
        nar.start(50, 50);
        
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
                nar.emit(TicTacToe.class, "NARS plays: " + i);
                boolean success = false;
                if (field[i]==0) {
                    field[i] = 2;                
                    success = true;
                }
                if (success) {
                    nar.addInput("<input --> succeeded>. :|: %" + (success ? "1.00" : "0.00") + "%" );
                    updateField();

                    playing = !playing;
                }
            }
            else {
                nar.emit(TicTacToe.class, "NARS not playing, but wants: " + i);    
                nar.addInput("<input --> failed>. :|:");
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
            
        }
        else if (winner == HUMAN) {
            nar.emit(OUT.class, "Player won");
            nar.addInput("<goal --> reached>. :|: %0.0;0.99%");
        }
        else if (winner == COMPUTER) {
            nar.emit(OUT.class, "NARS won");
            nar.addInput("<goal --> reached>. :|: %1.0;0.99%");
        }
        
    }



    public void reset() {
        Arrays.fill(field, 0);
        nar.addInput("<game --> reset>. :|:");
        addStartKnowledge();
    }



    public void addStartKnowledge() {
        nar.addInput("<goal --> reached>! %1.0;0.99%");

        //nar.addInput("<(^addO,$1) =/> <input --> succeeded>>."); //usually input succeeds
        //nar.addInput("<(&/,<1 --> set>,(^addO,$1)) =/> (--,<input --> succeeded>)>."); //usually input succeeds but not when it was set by player cause overwrite is not valid
        //nar.addInput("<(&/,(^addO,$1),(^addO,$1)) =/> (--,<input --> succeeded>)>."); //also overwriting on own is not valid
                
        for (int[] h : howToWin) {                
            int a = h[0];
            int b = h[1];
            int c = h[2];
            nar.addInput("<(&|,(^addO," + a + "),<input --> succeeded>,(^addO," + b + "),<input --> succeeded>,(^addO," + c + "),<input --> succeeded>) =/> <goal --> reached>>.");
        }
        
        
        nar.addInput("<goal --> reached>! %1.0;0.99%");

        nar.addInput("(&/,<#1 --> field>,(^addO,#1))!"); //doing something is also a goal :D

        nar.addInput("(^addO,0)! %1.0;0.7%");
        nar.addInput("(^addO,1)! %1.0;0.7%");
        nar.addInput("(^addO,2)! %1.0;0.7%");
        nar.addInput("(^addO,3)! %1.0;0.7%");
        nar.addInput("(^addO,4)! %1.0;0.7%");
        nar.addInput("(^addO,5)! %1.0;0.7%");
        nar.addInput("(^addO,6)! %1.0;0.7%");
        nar.addInput("(^addO,7)! %1.0;0.7%");
        nar.addInput("(^addO,8)! %1.0;0.7%");
         
         
        nar.addInput("<0 --> field>.");
        nar.addInput("<1 --> field>.");
        nar.addInput("<2 --> field>.");
        nar.addInput("<3 --> field>.");
        nar.addInput("<4 --> field>.");
        nar.addInput("<5 --> field>.");
        nar.addInput("<6 --> field>.");
        nar.addInput("<7 --> field>.");
        nar.addInput("<8 --> field>.");
         
        nar.addInput("<input --> succeeded>!");
        nar.addInput("(--,<input --> failed>)!");

    }
    
    private static final Font buttonFont = NARSwing.monofont.deriveFont(24f);

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
