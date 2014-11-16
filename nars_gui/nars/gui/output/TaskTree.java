package nars.gui.output;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import nars.core.EventEmitter.Observer;
import nars.core.Events.FrameEnd;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output.ERR;
import nars.io.Output.EXE;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.operator.io.Echo;

/**
 *
 * @author me
 */
public class TaskTree extends NPanel implements Observer, Runnable {

    long updatePeriodMS = 250;
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    private final DefaultTreeModel model;
    Map<Task, DefaultMutableTreeNode> nodes = new HashMap();
    private final JTree tree;
    private final NAR nar;
    final WeakHashMap<Task, TaskLabel> components = new WeakHashMap<>();
    float priorityThreshold = 0.01f;
    final Set<TreeNode> needRefresh = Collections.synchronizedSet(new HashSet());
    final ConcurrentLinkedDeque<Task> toAdd = new ConcurrentLinkedDeque<>();
    final ConcurrentLinkedDeque<Task> toRemove = new ConcurrentLinkedDeque<>();
    long lastUpdateTime = 0;
    
    
    public TaskTree(NAR nar) {
        super(new BorderLayout());

        tree = new JTree();
        model = new DefaultTreeModel(root);
        tree.setModel(model);
        tree.setCellRenderer(new CustomDefaultRenderer());

        this.nar = nar;        

        add(new JScrollPane(tree), BorderLayout.CENTER);

    }

    @Override
    public void onShowing(boolean b) {
        nar.memory.event.set(this, b, IN.class, OUT.class, ERR.class, Echo.class, EXE.class, FrameEnd.class);
    }

    public void add(Task t) {
        if (isActive(t))
            toAdd.add(t);        
    }

    public DefaultMutableTreeNode getNode(final Task t) {
        return nodes.get(t);
    }

    public DefaultMutableTreeNode newNode(final Task t) {
        DefaultMutableTreeNode existing = nodes.get(t);
        if (existing != null) {
            return existing;
        }

        //String key = t.name().toString();
        DefaultMutableTreeNode d = new DefaultMutableTreeNode(t);
        nodes.put(t, d);
        return d;
    }

    protected boolean isActive(final Task t) {
        return t.getPriority() >= priorityThreshold;
    }
    
    public void update() {
        //remove dead tasks
        for (Task t : nodes.keySet()) {
            if (!isActive(t))
                toRemove.add(t);            
        }

        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > updatePeriodMS) {
            SwingUtilities.invokeLater(this);            
        }        
    }

    @Override public void run() {
        for (Task t : toRemove) {
            DefaultMutableTreeNode node = nodes.get(t);
            if (node!=null) {
                TreeNode p = node.getParent();
                node.removeFromParent();                
                needRefresh.add(p);
                needRefresh.remove(node);
            }
            nodes.remove(t);
            components.remove(t);
            toAdd.remove(t);
        }
        
        for (Task t : toAdd) {
            Task parent = t.parentTask;
            if (parent!=null && parent.equals(t)) {
                System.err.println(t + " has parentTask equal to itself");
                parent = null;
            }

            DefaultMutableTreeNode tnode = getNode(t);
            if (tnode != null) {
                continue;
            }

            tnode = newNode(t);

            if (parent == null) {
                //System.out.println(tnode + " add to root");
                root.add(tnode);                
                needRefresh.add(root);
            } else {
                DefaultMutableTreeNode pnode = getNode(parent);

                if (pnode != null) {
                    //System.out.println(tnode + "Adding to: " + pnode);
                    if (tnode.getParent()!=null) { //pnode.isNodeAncestor(tnode)) {
                        tnode.removeFromParent();
                    }
                    pnode.add(tnode);
                    needRefresh.add(pnode);
                } else {
                    //missing parent, reparent to root?
                    //System.out.println(tnode + " add to root by default");
                    root.add(tnode);
                    needRefresh.add(root);
                }

            }
        }

        for (TaskLabel t : components.values())
            t.updateTask();

        for (TreeNode t : needRefresh)
            model.reload(t);
        
        
        needRefresh.clear();        
        toAdd.clear();
        toRemove.clear();
        
        repaint();        
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public class TaskLabel extends JLabel {
        private final Task task;

        public TaskLabel(Task t) {
            this.task = t;

            setOpaque(true);
            setFont(Video.monofont);
            
            updateTask();
        }

        protected void updateTask() {
            final Task t = task;
            
            Concept con = nar.memory.concept(t.getContent());
            float conPri = 0;
            if (con == null) {
                /*System.err.println("TaskTree: " + t + " missing concept.  either memory was reset or concept should have been created but wasnt.");*/
                //toRemove.add(t);            
            }
            else {
                conPri = con.getPriority();
            }
            float taskPri = t.getPriority();
            TruthValue desire = t.getDesire();
            if (desire!=null) {
                float confidence = t.getDesire().getConfidence();
                setForeground(new Color(0,0,conPri,confidence));
            }        
            setBackground(new Color(1f-taskPri/4f,1f,1f-taskPri/4f));

            setText(t.toStringExternal());
            //repaint();
        }
    }
    
    protected void updateComponent(Task t, JLabel c) {
        if (c instanceof TaskLabel)
            ((TaskLabel)c).updateTask();
    }
    
    protected class CustomDefaultRenderer
            extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            // Allow the original renderer to set up the label
            /*Component c = super.getTreeCellRendererComponent(
                    tree, value, selected,
                    expanded, leaf, row,
                    hasFocus);*/
            
         
            
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object v = node.getUserObject();
            if (v instanceof Task) {                
                TaskLabel c = new TaskLabel((Task)v);
                components.put((Task)v, c);
                return c;
            }
            
            return  super.getTreeCellRendererComponent(
                    tree, value, selected,
                    expanded, leaf, row,
                    hasFocus);
        }

    }

    @Override
    public void event(Class channel, Object[] arguments) {        
        if (channel == OUT.class) {
            Object o = arguments[0];
            if (o instanceof Task) {
                add((Task) o);
            }
        }
        else if (channel == FrameEnd.class) {
            update();
        }
    }

}
