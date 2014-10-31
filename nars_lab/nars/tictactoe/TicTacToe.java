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
import java.awt.Graphics;
import java.util.List;
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
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 *
 * @author tc
 */
public class TicTacToe extends JPanel {

    /**
     * Creates new form play
     */
    public final NAR nar;

    int[] field = new int[] {
        0, 0, 0,
        0, 0, 0,
        0, 0, 0
    };
    
    public TicTacToe() {
        super(new BorderLayout());

        
                
        nar = new DefaultNARBuilder().build();
        nar.memory.addOperator(new AddO("^addO"));
        nar.param().noiseLevel.set(0);
                
        addStartKnowledge();
        nar.addInput("<game --> reset>. :|:");
        
        
        add(new GridButtonPanel(nar, 3, 3) {

            @Override
            public Concept initTerm(int x, int y) {
                return nar.memory.conceptualize(new BudgetValue(0.5f, 0.5f, 0.5f), 
                        new Term( Integer.toString(1 + y * 3 + x)));
            }

            @Override
            public void onMouseClick(GridButtonPanel.ConceptButton c, boolean press, int wheelRotation) {
                int x = c.bx;
                int y = c.by;
                int i = c.by * 3 + c.bx;
                
                if (!en || field[i] != 0) {
                    return;
                }
                nar.addInput("<" + i + " --> set>. :|:");
                c.setText("X");
                field[i] = 1;
                updateField();
                if (!updateField()) {
                    //enableall(false);
                }
            }

            @Override
            public void repaintOverlay(Graphics g) {
                super.repaintOverlay(g); //To change body of generated methods, choose Tools | Templates.
            }

            public Color getBackgroundColor(float priority) {
                return Color.getHSBColor(0.2f + priority * 0.2f, 0.75f, 0.5f + priority * 0.5f);
            }
            
            @Override
            public void repaintButton(GridButtonPanel.ConceptButton b) {
                b.setBackground(getBackgroundColor(b.concept.getPriority()));

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
        nar.start(50, 500);
        
    }

    


    public class AddO extends Operator {

        public AddO(String name) {
            super(name);
        }

        @Override
        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        //Operation content = (Operation) task.getContent();
            //Operator op = content.getOperator();

            boolean success = true;
            System.out.println("Executed: " + this);
            if (args[0].toString().equals("1") && field[0] == 0) {
                jButton2.setText("O");
                field[0] = 2;
            } else if (args[0].toString().equals("2") && field[1] == 0) {
                jButton5.setText("O");
                field[1] = 2;
            } else if (args[0].toString().equals("3") && field[2] == 0) {
                jButton8.setText("O");
                field[2] = 2;
            } else if (args[0].toString().equals("4") && field[3] == 0) {
                jButton3.setText("O");
                field[3] = 2;
            } else if (args[0].toString().equals("5") && field[4] == 0) {
                jButton6.setText("O");
                field[4] = 2;
            } else if (args[0].toString().equals("6") && field[5] == 0) {
                jButton9.setText("O");
                field[5] = 2;
            } else if (args[0].toString().equals("7") && field[6] == 0) {
                jButton4.setText("O");
                field[6] = 2;
            } else if (args[0].toString().equals("8") && field[7] == 0) {
                jButton7.setText("O");
                field[7] = 2;
            } else if (args[0].toString().equals("9") && field[8] == 0) {
                nar.addInput("<input --> succeeded>. :|: %1.00;0.99%");
                jButton10.setText("O");
                field[8] = 2;
            } else {
                nar.addInput("<input --> succeeded>. :|: %0.00;0.99%");
                success = false;
            }

            if (success) {
                enableall(true);
                updateField();
                nar.step(100); //give time to see win condition
                nar.stop();
            }

        //for (Term t : args) {
            //    System.out.println(" --- " + t);
            // }
            return null;
        }

    }

    public boolean updateField() {
        int winner = 0; //player 1 won: won=1, nars won: won=2

        for (int p = 1; p <= 2; p++) { // p = player
            
            for (int i = 0; i < 3; i++) {
                if (field[i] == p && field[i + 3] == p && field[i + 3 + 3] == p) {
                    winner = p;
                }
            }

            for (int i = 0; i <= 6; i += 3) { //left right
                if (field[i] == p && field[i + 1] == p && field[i + 1 + 1] == p) {
                    winner = p;
                }
            }

            if (field[0] == p && field[4] == p && field[8] == p) { //left diagonale
                winner = p;
            }
            if (field[2] == p && field[4] == p && field[6] == p) { //right diagonale
                winner = p;
            }
        }

        if (winner == 1) {
            nar.emit(OUT.class, "Player won");
            nar.addInput("<goal --> reached>. %0.0;0.99%");
        }
        if (winner == 2) {
            nar.emit(OUT.class, "NARS won");
            nar.addInput("<goal --> reached>. %1.0;0.99%");
        }

        return winner != 0;
    }

    

    boolean en = true;

    public void enableall(boolean state) {
        /* jButton2.setEnabled(state);
         jButton3.setEnabled(state);
         jButton4.setEnabled(state);
         jButton5.setEnabled(state);
         jButton6.setEnabled(state);
         jButton7.setEnabled(state);
         jButton8.setEnabled(state);
         jButton9.setEnabled(state);
         jButton10.setEnabled(state);*/
        en = state;
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[0] != 0) {
            return;
        }
        nar.addInput("<1 --> set>. :|:");
        jButton2.setText("X");
        field[0] = 1;
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[3] != 0) {
            return;
        }
        nar.addInput("<4 --> set>. :|:");
        jButton3.setText("X");
        field[3] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[6] != 0) {
            return;
        }
        nar.addInput("<7 --> set>. :|:");
        jButton4.setText("X");
        field[6] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[1] != 0) {
            return;
        }
        nar.addInput("<2 --> set>. :|:");
        jButton5.setText("X");
        field[1] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[4] != 0) {
            return;
        }
        nar.addInput("<5 --> set>. :|:");
        jButton6.setText("X");
        field[4] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[7] != 0) {
            return;
        }
        nar.addInput("<8 --> set>. :|:");
        jButton7.setText("X");
        field[7] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[2] != 0) {
            return;
        }
        nar.addInput("<3 --> set>. :|:");
        jButton8.setText("X");
        field[2] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[5] != 0) {
            return;
        }
        nar.addInput("<6 --> set>. :|:");
        jButton9.setText("X");
        field[5] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!en || field[8] != 0) {
            return;
        }
        nar.addInput("<9 --> set>. :|:");
        jButton10.setText("X");
        field[8] = 1;
        updateField();
        if (!updateField()) {
            enableall(false);
        }
    }

    boolean beginner = false;

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        jButton2.setText("_");
        jButton3.setText("_");
        jButton4.setText("_");
        jButton5.setText("_");
        jButton6.setText("_");
        jButton7.setText("_");
        jButton8.setText("_");
        jButton9.setText("_");
        jButton10.setText("_");
        this.jLabel1.setText("playing...");
        field = new int[]{0, 0, 0,
            0, 0, 0,
            0, 0, 0
        };
        nar.addInput("<game --> reset>. :|:");
        addStartKnowledge();

        if (!beginner) {
            enableall(false);
        } else {
            nar.stop();
            enableall(true);
        }

        beginner = !beginner;
    }

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        addStartKnowledge();
    }

    public void addStartKnowledge() {
        nar.addInput("<goal --> reached>! %1.0;0.99%");

        //nar.addInput("<(^addO,$1) =/> <input --> succeeded>>."); //usually input succeeds
        //nar.addInput("<(&/,<1 --> set>,(^addO,$1)) =/> (--,<input --> succeeded>)>."); //usually input succeeds but not when it was set by player cause overwrite is not valid
        //nar.addInput("<(&/,(^addO,$1),(^addO,$1)) =/> (--,<input --> succeeded>)>."); //also overwriting on own is not valid
        nar.addInput("<(&|,(^addO,1),<input --> succeeded>,(^addO,2),<input --> succeeded>,(^addO,3),<input --> succeeded>) =/> <goal --> reached>>.");
        nar.addInput("<(&|,(^addO,4),<input --> succeeded>,(^addO,5),<input --> succeeded>,(^addO,6),<input --> succeeded>) =/> <goal --> reached>>.");
        nar.addInput("<(&|,(^addO,7),<input --> succeeded>,(^addO,8),<input --> succeeded>,(^addO,9),<input --> succeeded>) =/> <goal --> reached>>.");
        //also with 3 in a column:
        nar.addInput("<(&|,(^addO,1),<input --> succeeded>,(^addO,4),<input --> succeeded>,(^addO,7),<input --> succeeded>) =/> <goal --> reached>>.");
        nar.addInput("<(&|,(^addO,2),<input --> succeeded>,(^addO,5),<input --> succeeded>,(^addO,8),<input --> succeeded>) =/> <goal --> reached>>.");
        nar.addInput("<(&|,(^addO,3),<input --> succeeded>,(^addO,6),<input --> succeeded>,(^addO,9),<input --> succeeded>) =/> <goal --> reached>>.");
        //and with the 2 diagonals:
        nar.addInput("<(&|,(^addO,1),<input --> succeeded>,(^addO,5),<input --> succeeded>,(^addO,9),<input --> succeeded>) =/> <goal --> reached>>.");
        nar.addInput("<(&|,(^addO,3),<input --> succeeded>,(^addO,5),<input --> succeeded>,(^addO,7),<input --> succeeded>) =/> <goal --> reached>>.");
        //
        nar.addInput("<goal --> reached>! %1.0;0.99%");

        /*nar.addInput("(&/,<#1 --> field>,(^addO,#1))!"); //doing something is also a goal :D
         nar.addInput("(^addO,1)! %1.0;0.7%");
         nar.addInput("(^addO,2)! %1.0;0.7%");
         nar.addInput("(^addO,3)! %1.0;0.7%");
         nar.addInput("(^addO,4)! %1.0;0.7%");
         nar.addInput("(^addO,5)! %1.0;0.7%");
         nar.addInput("(^addO,6)! %1.0;0.7%");
         nar.addInput("<1 --> field>.");
         nar.addInput("<2 --> field>.");
         nar.addInput("<3 --> field>.");
         nar.addInput("<4 --> field>.");
         nar.addInput("<5 --> field>.");
         nar.addInput("<6 --> field>.");
         nar.addInput("<7 --> field>.");
         nar.addInput("<8 --> field>.");
         nar.addInput("<9 --> field>.");*/
        nar.addInput("<input --> succeeded>!");

    }

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

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration                   
}
