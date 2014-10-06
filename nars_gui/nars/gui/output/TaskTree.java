package nars.gui.output;

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
import nars.core.Events;
import nars.core.Events.FrameEnd;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.gui.NARSwing;
import nars.gui.NPanel;
import nars.io.Output;

/**
 *
 * @author me
 */
public class TaskTree extends NPanel implements Output, Observer, Runnable {

    long updatePeriodMS = 250;
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    private final DefaultTreeModel model;
    Map<Task, DefaultMutableTreeNode> nodes = new HashMap();
    private final JTree tree;
    private final NAR nar;
    final WeakHashMap<Task, Component> components = new WeakHashMap<Task, Component>();
    float priorityThreshold = 0.05f;
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
        nar.addOutput(this);

        add(new JScrollPane(tree), BorderLayout.CENTER);

    }

    @Override
    public void onShowing(boolean b) {
        nar.memory.event.set(this, b, Events.FrameEnd.class);
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

        String key = t.name().toString();
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

        for (Map.Entry<Task, Component> t : components.entrySet())
            updateComponent(t.getKey(), (JLabel) t.getValue());

        for (TreeNode t : needRefresh)
            model.reload(t);
        
        needRefresh.clear();        
        toAdd.clear();
        toRemove.clear();
        
        repaint();        
        lastUpdateTime = System.currentTimeMillis();
    }
    
    protected void updateComponent(Task t, JLabel c) {
        Concept con = nar.memory.concept(t.getContent());
        if (con == null) {
            System.err.println("TaskTree: " + t + " missing concept.  either memory was reset or concept should have been created but wasnt.");
            toRemove.add(t);
            return;
        }
        float conPri = con.getPriority();
        float taskPri = t.getPriority();
        TruthValue desire = t.getDesire();
        if (desire!=null) {
            float confidence = t.getDesire().getConfidence();
            c.setForeground(new Color(0,0,conPri,confidence));
        }        
        c.setBackground(new Color(1f-taskPri/4f,1f,1f-taskPri/4f));
        c.setText(t.toStringExternal());
        c.repaint();
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
            Component c = super.getTreeCellRendererComponent(
                    tree, value, selected,
                    expanded, leaf, row,
                    hasFocus);
         
            
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object v = node.getUserObject();
            if (v instanceof Task) {                
                components.put((Task)v, c);
                
                JLabel jj = (JLabel)c;
                jj.setOpaque(true);
                jj.setFont(NARSwing.monofont);                
                updateComponent((Task)v, jj);
            }
            return c;
        }

    }

    @Override
    public void output(Class channel, Object o) {
        if (channel == OUT.class) {
            if (o instanceof Task) {
                add((Task) o);
            }
        }
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (event == FrameEnd.class) {
            update();
        }
    }

}
