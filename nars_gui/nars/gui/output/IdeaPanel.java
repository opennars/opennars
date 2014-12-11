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
import nars.core.EventEmitter.EventObserver;
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
    }
    
    public static class IdeaSummary extends JPanel {
        
        public final Idea idea;

        public IdeaSummary(Idea i) {
            super(new BorderLayout());
            this.idea = i;
            
            update();
        }
        
        protected void update() {
            removeAll();
            
            String s = idea.toString();
            add(new JLabel(s));
            
            doLayout();
        }
    }
    
    int y = 0;
    
    protected void onOutputTask(Task t) {
        Idea i = ideas.get(t);
        if (i == null) {
            System.err.println("no idea exist: " + t.getTerm() + " " + Idea.getKey(t));
            Concept c = nar.memory.concept(t.getTerm());
            return;
        }
        IdeaSummary p = getPanel(i);
        remove(p);
        addPanel(y++, p);
    }
    
    
    protected IdeaSummary getPanel(Idea i) {
        IdeaSummary existing = ideaPanel.get(i);
        if (existing==null) {
            existing = new IdeaSummary(i);
            ideaPanel.put(i, existing);
        }
        return existing;
    }
    

    
    
    
    
    @Override
    protected void onShowing(boolean showing) {
        ideas.enable(showing);
        nar.memory.event.set(this, showing, Output.DefaultOutputEvents);
    }

    
}
