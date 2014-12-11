/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.ConceptForget;
import nars.core.Events.ConceptNew;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.gui.WrapLayout;
import nars.io.Output;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.util.Idea;
import nars.util.Idea.IdeaSet;
import nars.util.Idea.OperatorPunctuation;

/**
 *
 * @author me
 */
public class IdeaPanel extends VerticalPanel implements EventObserver {

    private NAR nar;
    private final IdeaSet ideas;

    public final Map<Idea, IdeaSummary> ideaPanel = new WeakHashMap();

    public IdeaPanel(NAR nar) {
        super();
        this.nar = nar;

        ideas = new IdeaSet(nar);

    }

    @Override
    public void event(Class event, Object[] args) {

        if (args[0] instanceof Task) {
            Task t = (Task) args[0];
            onOutputTask(t);
        } else if (args[0] instanceof Concept) {
            Concept c = (Concept) args[0];
            onOutputConcept(c);
        }
    }

    public static class IdeaSummary extends JPanel {

        public final Idea idea;

        public IdeaSummary(Idea i) {
            super(new BorderLayout());
            this.idea = i;

            setBorder(LineBorder.createGrayLineBorder());
            update();
        }

        public void update() {
            removeAll();

            idea.update();

            //if (idea.concepts.size() > 1) {
            /*} else {
                add(new JLabel(idea.key().toString()), BorderLayout.NORTH);
            }*/

            //add(new JLabel(idea.getOperatorPunctuations().toString()), BorderLayout.SOUTH);

            JPanel operatorPanel = null;
            if (idea.getOperatorPunctuations().size() > 1) {
                operatorPanel = new JPanel();
                operatorPanel.setLayout(new BoxLayout(operatorPanel, BoxLayout.PAGE_AXIS));
                for (OperatorPunctuation x : idea.getOperatorPunctuations()) {
                    JButton j = new JButton(x.toString());
                    operatorPanel.add(j);
                }
                
            }

            boolean centerFree = true;
            if (operatorPanel!=null) {
                if (idea.getArity() == 2) {
                    Term[] t = ((CompoundTerm)idea.getSampleTerm()).term;

                    JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    f.add(new ConceptButton(t[0]));
                    f.add(operatorPanel);
                    f.add(new ConceptButton(t[1]));
                    add(f, BorderLayout.CENTER);
                    centerFree = false;
                }
                else {
                    add(operatorPanel, BorderLayout.WEST);
                }
            }
            if (centerFree) {
                JPanel conceptPanel = new JPanel(
                        new WrapLayout(FlowLayout.LEFT));
                for (Concept c : idea)
                    conceptPanel.add(new ConceptButton(c));                    
                
                //get the only sentence type, use it's punctuation as a suffix button
                Iterator<OperatorPunctuation> oi = idea.getOperatorPunctuations().iterator();
                if (oi.hasNext()) {

                    OperatorPunctuation p = oi.next();
                    JButton pbutton = new JButton(Character.toString(p.punc));
                    conceptPanel.add(pbutton);
                }

                add(conceptPanel, BorderLayout.CENTER);
                
            }
            
            doLayout();
        }

    }
    
    /** represents either a Term or its Concept (if exists) */
    public static class ConceptButton extends JButton {

        public ConceptButton(Concept c) {
            this(c.getTerm());

        }

        public ConceptButton(Term t) {
            super(t.toString());

        }

        public void update() {

        }

    }

    int y = 0;

    protected void onOutputTask(Task t) {
        Idea i = ideas.get(t);
        if (i == null) {
            /*System.err.println("no idea exist: " + t.getTerm() + " " + Idea.getKey(t));
            
             Concept c = nar.memory.concept(t.getTerm());
            
             if (c == null) {
             System.err.println("no concept exist: " + t.getTerm());
             return;
             }
            
             i = ideas.add(c);*/
            return;
        }
        update(i);
    }

    protected void update(Idea i) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                IdeaSummary p = getPanel(i);

                //SwingUtilities..
                content.remove(p);
                addPanel(y++, p);

                scrollBottom();

                doLayout();
                updateUI();
            }

        });
    }

    protected void onOutputConcept(Concept c) {
        Idea i = ideas.get(c);
        update(i);
    }

    protected IdeaSummary getPanel(Idea i) {
        IdeaSummary existing = ideaPanel.get(i);
        if (existing == null) {
            existing = new IdeaSummary(i);
            ideaPanel.put(i, existing);
        } else {
            existing.update();
        }

        return existing;
    }

    @Override
    protected void onShowing(boolean showing) {
        ideas.enable(showing);
        nar.memory.event.set(this, showing, Output.DefaultOutputEvents);
        nar.memory.event.set(this, showing, ConceptNew.class, ConceptForget.class);
    }

}
