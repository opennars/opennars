package nars.gui.output;

import automenta.vivisect.Video;
import nars.core.Events.FrameEnd;
import nars.core.Events.TaskAdd;
import nars.core.Events.TaskRemove;
import nars.core.NAR;
import nars.event.Reaction;
import nars.gui.ReactionPanel;
import nars.gui.WrapLayout;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;
import nars.logic.entity.TruthValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.awt.BorderLayout.NORTH;

/**
 *
 * @author me
 */
public class TaskTree extends ReactionPanel implements Reaction, Runnable {

    long updatePeriodMS = 250;
    DefaultMutableTreeNode root;
    private DefaultTreeModel model;
    Map<Task, DefaultMutableTreeNode> nodes = new ConcurrentHashMap();
    private final JTree tree;
    final WeakHashMap<Task, TaskLabel> components = new WeakHashMap<>();
    float priorityThreshold = 0.01f;
    final Set<TreeNode> needRefresh = Collections.synchronizedSet(new HashSet());
    final ConcurrentLinkedDeque<Task> toAdd = new ConcurrentLinkedDeque<>();
    final ConcurrentLinkedDeque<Task> toRemove = new ConcurrentLinkedDeque<>();
    long lastUpdateTime = 0;
    
    boolean needsRestart = false;
    boolean showingJudgments = false;
    boolean showingQuestions = true;
    boolean showingGoals = true;

    public TaskTree(NAR nar) {
        super(nar, new BorderLayout());

        tree = new JTree();


        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);


        tree.setCellRenderer(new CustomDefaultRenderer());


        JPanel menu = new JPanel(new WrapLayout(FlowLayout.LEFT));
        
        JCheckBox showJudgments = new JCheckBox("Judgments");
        showJudgments.setSelected(showingJudgments);
        showJudgments.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showingJudgments = showJudgments.isSelected();
                reset();
            }            
        });
        menu.add(showJudgments);
        JCheckBox showQuestions = new JCheckBox("Questions");
        showQuestions.setSelected(showingQuestions);
        showQuestions.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showingQuestions = showQuestions.isSelected();
                reset();
            }            
        });
        menu.add(showQuestions);
        JCheckBox showGoals = new JCheckBox("Goals");
        showGoals.setSelected(showingGoals);
        showGoals.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showingGoals = showGoals.isSelected();
                reset();
            }            
        });
        menu.add(showGoals);
        
        add(menu, NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        reset();
    }
    
    protected void reset() {

        components.clear();
        nodes.clear();
        toAdd.clear();
        lastUpdateTime = 0;

        root = new DefaultMutableTreeNode("Tasks");
        model = new DefaultTreeModel(root);
        tree.setModel(model);

        needsRestart = true;
        update();
    }



    @Override
    public Class[] getEvents() {
        return new Class[] { TaskAdd.class, TaskRemove.class, FrameEnd.class /*, TaskRemove.class*/ };
    }

    public void add(Task t) {
        if (isVisible(t))
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

    protected boolean isVisible(final Task t) {
        if ((t.sentence.isJudgment()) && (!showingJudgments)) return false;
        if ((t.sentence.isQuestion()) && (!showingQuestions)) return false;
        if ((t.sentence.isGoal()) && (!showingGoals)) return false;
        return t.getPriority() >= priorityThreshold;
    }
    
    public void update() {
        //TODO get existing Tasks at the next frame event by new method: memory.getTasks() which iterates all concepts tasklinks
        if (needsRestart) {
            Set<Task> tasks = nar.memory.getTasks(true, false, false);
            for (Task t : tasks)
                if (isVisible(t))
                    add(t);
        }
        
        //remove dead tasks
        for (Task t : nodes.keySet()) {
            if (!isVisible(t))
                toRemove.add(t);            
        }

        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > updatePeriodMS) {
            SwingUtilities.invokeLater(this);            
        }        
    }

    @Override public void run() {
        for (Task t : toRemove) {
            DefaultMutableTreeNode node = nodes.remove(t);
            if (node!=null) {
                TreeNode p = node.getParent();
                node.removeFromParent();                
                needRefresh.add(p);
                needRefresh.remove(node);
            }
            components.remove(t);
            toAdd.remove(t);
        }
        
        for (Task t : toAdd) {
            Task parent = t.getParentTask();
            if (parent!=null && parent.equals(t)) {
                //System.err.println(t + " has parentTask equal to itself");
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

            //setOpaque(false);
            setOpaque(true);
            setFont(Video.monofont);
            
            updateTask();
        }

        protected void updateTask() {
            final Task t = task;
            
            Concept con = nar.memory.concept(t.getTerm());
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
            
            Color iColor;
            if (desire!=null) {
                float confidence = t.getDesire().getConfidence();
                iColor = new Color(0,confidence/1.5f,conPri/1.5f,0.75f + 0.25f * confidence);
            }        
            else {
                iColor = new Color(0,0,conPri/1.5f,1f);
            }
            setBorder(new MatteBorder(0,15,0,0,iColor));
            

            float meanPri = (conPri + taskPri) / 2f;
            final float mpi = 0.5f + 0.5f * meanPri;
            setForeground(new Color(mpi, mpi, mpi));
            
            final float hue = 0.3f + 0.5f * conPri;
            Color c = Color.getHSBColor(hue, 0.4f, conPri * 0.2f);
            //setBackground(new Color(1f-taskPri/4f,1f,1f-taskPri/4f));
            setBackground(c);
            
            setFont(Video.monofont.deriveFont(14f + taskPri * 4f));
            setText(t.toStringExternal2(false));
            
        }

        public void setSelected(boolean selected) {
            final int bt = 1;
            if (!selected) {
                setBorder(new EmptyBorder(bt,bt,bt,bt));
            }
            else {
                setBorder(new LineBorder(Color.GRAY, bt));
            }

        }
    }
    
//    protected void updateComponent(Task t, JLabel c) {
//        if (c instanceof TaskLabel)
//            ((TaskLabel)c).updateTask();
//    }
    
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
                Task tv = (Task)v;
                TaskLabel c = components.get(tv);
                if (c == null) {
                    c = new TaskLabel(tv);
                    components.put(tv, c);
                }
                else {
                    c.updateTask();
                }
                c.setSelected(hasFocus);
                return c;
            }
            
            return super.getTreeCellRendererComponent(
                    tree, value, selected,
                    expanded, leaf, row,
                    hasFocus);
        }

    }

    @Override
    public void event(Class channel, Object[] arguments) {

        if (channel == TaskAdd.class) {
            add((Task)arguments[0]);
        }
        else if (channel == TaskRemove.class) {
            toRemove.add((Task)arguments[0]);
        }
        else if (channel == FrameEnd.class) {
            update();
        }
    }

}
