package nars.tuprolog.gui.spyframe;


import nars.nal.term.Term;
import nars.tuprolog.PNum;
import nars.tuprolog.PTerm;
import nars.tuprolog.Struct;
import nars.tuprolog.Var;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** GUI-Window containing a TermComponent that displays a prolog term.
 * Displaying should be a side effect of a corresponding prolog predicate
 * termframe(Term) that evaluates to constant true. Closing the window has
 * no consequences to the prolog process.
 * The windows also has an input field that shows the term. Changing this
 * will change the display without consequence to the prolog process.
 *
 * @author franz.beslmeisl at googlemail.com
 */
public class TermPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

/**Transforms prolog terms into trees.*/
  public static final ToTree<Term> term2tree=new ToTree<Term>(){
    @Override
    public Node makeTreeFrom(Term term){
      Node node=new Node(""+term);
      node.textcolor=node.bordercolor=Color.BLACK;
      //make it more specific if possible
      if(term instanceof Var){
        Var var=(Var)term;
        node.text=var.getName();
        node.textcolor=node.bordercolor=Color.BLUE;
        if(var.isBound()){
          node.kids=new Node[1];
          node.kids[0]=makeTreeFrom(var.getTerm());
        }
      } else if(term instanceof PNum){
        node.textcolor=node.bordercolor=Color.MAGENTA;
      } else if(term instanceof Struct){
        Struct struct=(Struct)term;
        node.text=struct.getName();
        int n=struct.size();
        node.kids=new Node[n];
        for(int i=0; i<n; i++)
          node.kids[i]=makeTreeFrom(struct.getTermX(i));
      }
      return node;
    }
  };

  JTextField input;
  Tree<Term> ptt;

  /** Constructs a new TermFrame.
   *  @param term the prolog term to be displayed.
   */
  public TermPanel(Term term){
    
    ptt=new Tree<>(term2tree, term);
    add(new JScrollPane(ptt));
    input=new JTextField();
    add(input, BorderLayout.SOUTH);
    input.setText(""+term);
    setVisible(true);
    input.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e){setTerm(input.getText());}

  /**Sets a new prolog term.
   * @param term to be displayed.
   */
  public void setTerm(PTerm term){
    ptt.setStructure(term);
    input.setText(""+term);
    validate();
  }

  /**Sets a new prolog term.
   * @param sterm to be displayed.
   */
  public void setTerm(String sterm){
    PTerm term;
    try{term= PTerm.createTerm(sterm);}
    catch(Exception ex){
      term= PTerm.createTerm("'>illegal prolog term<'");
    }
    setTerm(term);
  }

}