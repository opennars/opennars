/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.ConceptForget;
import nars.core.Events.ConceptNew;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.io.Output;
import nars.util.Idea;
import nars.util.Idea.IdeaSet;

/**
 *
 * @author me
 */
public class IdeaPanel extends VerticalPanel implements EventObserver {
    private NAR nar;
    private final IdeaSet ideas;

    public final Map<Idea,IdeaSummary> ideaPanel = new WeakHashMap();
    
    public IdeaPanel(NAR nar) {
        super();
        this.nar = nar;
        
        ideas = new IdeaSet(nar);
        
    }
    
    @Override
    public void event(Class event, Object[] args) {
        
        if (args[0] instanceof Task) {
            Task t = (Task)args[0];
            onOutputTask(t);
        }
        else if (args[0] instanceof Concept) {
            Concept c = (Concept)args[0];
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
            
            add(new JLabel(idea.key().toString()), BorderLayout.NORTH);
            add(new JLabel(idea.getOperatorPunctuations().toString()), BorderLayout.SOUTH);
            
            doLayout();
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
        if (existing==null) {
            existing = new IdeaSummary(i);
            ideaPanel.put(i, existing);
        }
        else
            existing.update();
        
        return existing;
    }
    

    
    
    
    
    @Override
    protected void onShowing(boolean showing) {
        ideas.enable(showing);
        nar.memory.event.set(this, showing, Output.DefaultOutputEvents);
        nar.memory.event.set(this, showing, ConceptNew.class, ConceptForget.class);
    }

    
}
