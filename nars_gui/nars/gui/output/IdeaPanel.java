/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.ConceptBeliefAdd;
import nars.core.Events.ConceptBeliefRemove;
import nars.core.Events.ConceptForget;
import nars.core.Events.ConceptGoalAdd;
import nars.core.Events.ConceptGoalRemove;
import nars.core.Events.ConceptNew;
import nars.core.Events.ConceptQuestionAdd;
import nars.core.Events.ConceptQuestionRemove;
import nars.core.NAR;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.io.Symbols.JUDGMENT_MARK;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.util.Idea;
import nars.util.Idea.IdeaSet;
import nars.util.Idea.SentenceType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author me
 */
public class IdeaPanel extends VerticalPanel implements EventObserver {

    
    private NAR nar;
    private final IdeaSet ideas;

    public final Map<Idea, IdeaSummary> ideaPanel = new WeakHashMap();
    
    boolean showTasks = true;
    boolean showConcepts = true;

    public IdeaPanel(NAR nar) {
        super();
        this.nar = nar;

        ideas = new IdeaSet(nar);

    }
    @Override
    protected void onShowing(boolean showing) {
        ideas.enable(showing);
        //nar.memory.event.set(this, showing, Output.DefaultOutputEvents);
        nar.memory.event.set(this, showing, 
                ConceptNew.class, ConceptForget.class, 
                ConceptBeliefAdd.class,
                ConceptBeliefRemove.class,
                ConceptQuestionAdd.class,
                ConceptQuestionRemove.class,
                ConceptGoalAdd.class,
                ConceptGoalRemove.class);
    }

    @Override
    public void event(Class event, Object[] args) {

        if (args[0] instanceof Task) {
            if (showTasks) {
                Task t = (Task) args[0];
                onOutputTask(t);
            }
        } else/* if (args[0] instanceof Concept)*/ {
            if (showConcepts) {
                Concept c = (Concept) args[0];
                onOutputConcept(c);
            }
        }
    }

    public class IdeaSummary extends JPanel {

        public final Idea idea;

        public IdeaSummary(Idea i) {
            super(new BorderLayout());
            this.idea = i;

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
            
            if (idea.size() == 1) {
                ConceptsPanel cp;
                add(cp = new ConceptsPanel(nar, idea.concepts.iterator().next()), CENTER);
                cp.onShowing(true);
                return;
            }

            JPanel operatorPanel = null;
            if (idea.getSentenceTypes().size() > 1) {
                operatorPanel = new JPanel();
                operatorPanel.setOpaque(false);
                operatorPanel.setLayout(new BoxLayout(operatorPanel, BoxLayout.PAGE_AXIS));
                for (SentenceType x : idea.getSentenceTypes()) {
                    operatorPanel.add(new SentenceTypeButton(x));
                }
                
            }
            
                    
            boolean centerFree = true;
            if (operatorPanel!=null) {
                if (idea.getArity() == 2) {
                    Term[] t = ((CompoundTerm)idea.getSampleTerm()).term;

            
                    JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    f.setOpaque(false);
                    f.add(new TermButton(t[0]));
                    f.add(operatorPanel);
                    f.add(new TermButton(t[1]));
                    add(f, BorderLayout.CENTER);
                    centerFree = false;
                }
                else {
                    add(operatorPanel, BorderLayout.WEST);
                }
            }
            if (centerFree) {
                
                JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT));
                f.setOpaque(false);
            
                for (Concept c : idea)
                    f.add(new TermButton(c));                    
                
                //get the only sentence type, use it's punctuation as a suffix button
                Iterator<SentenceType> oi = idea.getSentenceTypes().iterator();
                if (oi.hasNext()) {

                    SentenceType p = oi.next();
                    JButton pbutton = new JButton(Character.toString(p.punc));
                    f.add(pbutton);
                }

                add(f, BorderLayout.CENTER);
                
            }
            
            doLayout();
        }

        public void applyPriority(float priority) {
            //setFont(Video.monofont.deriveFont(12.0f + priority * 4f));
            
            setOpaque(true);
            final float hue = 0.3f + 0.5f * priority;
            
            Color c = Color.getHSBColor(hue, 0.4f, 0.2f + priority * 0.2f);
            
            setBackground(c);
            
            Color c2 = Color.getHSBColor(hue, 0.6f, 0.5f + priority * 0.5f);
            
            setBorder(BorderFactory.createMatteBorder(0, 14, 0, 0, c2));
            
            
            
            updateUI();
        }

    }

    public static class SentenceTypeButton extends JButton {

        public SentenceTypeButton(SentenceType x) {
            super(x.toString());
            
            switch (x.punc) {
                case JUDGMENT_MARK:
                    DescriptiveStatistics d = TruthValue.statistics(x.getSentences(), TruthValue.TruthComponent.Expectation);
                    float mean = (float) d.getMean();
                                        
                    float hue = 0.2f + mean*0.5f;
                    setForeground(Color.getHSBColor(hue, 0.9f, 0.95f));
                    break;
            }
            
        }
    
        
    }
    
    /** represents either a Term or its Concept (if exists).
        if a concept is involved, there may be additional data to display.     */
    public static class TermButton extends JButton {

        public TermButton(Concept c) {
            this(c.getTerm());
        }

        public TermButton(Term t) {
            super(t.toString());

        }

        public void update() {

        }

    }

    int y = 0;

    protected void onOutputTask(Task t) {
        Idea i = ideas.get(t);
        if (i==null)
            return;
        update(i, t.budget);
    }

    protected void update(Idea i, BudgetValue currentTaskBudget) {
        IdeaSummary p = getPanel(i);
        if (p == null)
            return;
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                
                
                if (currentTaskBudget!=null)
                    p.applyPriority(currentTaskBudget.getPriority());

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
        update(i, null);
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


}
