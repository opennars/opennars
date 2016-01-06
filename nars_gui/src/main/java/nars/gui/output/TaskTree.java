//package nars.gui.output;
//
//import automenta.vivisect.swing.NSliderSwing;
//import nars.Events;
//import nars.Events.FrameEnd;
//import nars.Events.OUT;
//import nars.Global;
//import nars.NAR;
//import nars.Video;
//import nars.concept.Concept;
//import nars.gui.ReactionPanel;
//import nars.gui.WrapLayout;
//import nars.task.Task;
//import nars.truth.Truth;
//import nars.util.event.Reaction;
//
//import javax.swing.*;
//import javax.swing.border.MatteBorder;
//import javax.swing.tree.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseEvent;
//import java.util.*;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedDeque;
//
//import static java.awt.BorderLayout.NORTH;
//
///**
// * @author me
// */
//public class TaskTree extends ReactionPanel implements Reaction<Class,Object[]>, Runnable {
//
//    final WeakHashMap<Task, TaskComponent> components = new WeakHashMap<>();
//    final Set<TreeNode> needRefresh = Collections.synchronizedSet(new HashSet());
//    final ConcurrentLinkedDeque<Task> toAdd = new ConcurrentLinkedDeque<>();
//    final ConcurrentLinkedDeque<Task> toRemove = new ConcurrentLinkedDeque<>();
//    private final JTree tree = new JTree();
//    long updatePeriodMS = 100;
//    DefaultMutableTreeNode root;
//    Map<Task, DefaultMutableTreeNode> nodes = new ConcurrentHashMap();
//
//    ConceptPanelBuilder cpBuilder;
//
//    float priorityThreshold = 0.00f; //TODO add an exclusion condition if the task is input
//
//    long lastUpdateTime = 0;
//    boolean needsRestart = false;
//
//    boolean showingJudgments = true;
//    boolean showingQuestions = true;
//    boolean showingGoals = true;
//
//    private DefaultTreeModel model;
//    private DefaultMutableTreeNode amnesia = null;
//    private final Set<Task> tasks = Global.newHashSet(256);
//
//    public TaskTree(NAR nar) {
//        super(nar, new BorderLayout());
//
//        cpBuilder = new ConceptPanelBuilder(nar);
//
//        tree.setEditable(true);
//
//
//
//        tree.setRootVisible(false);
//        tree.setShowsRootHandles(true);
//
//
//        tree.setCellRenderer(cellRenderer);
//        tree.setCellEditor(editor);
//
//
//        JPanel menu = new JPanel(new WrapLayout(FlowLayout.LEFT));
//
//        JToggleButton showJudgments = new JToggleButton("Judgments");
//        showJudgments.setSelected(showingJudgments);
//        showJudgments.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showingJudgments = showJudgments.isSelected();
//                reset();
//            }
//        });
//        menu.add(showJudgments);
//        JToggleButton showQuestions = new JToggleButton("Questions");
//        showQuestions.setSelected(showingQuestions);
//        showQuestions.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showingQuestions = showQuestions.isSelected();
//                reset();
//            }
//        });
//        menu.add(showQuestions);
//        JToggleButton showGoals = new JToggleButton("Goals");
//        showGoals.setSelected(showingGoals);
//        showGoals.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showingGoals = showGoals.isSelected();
//                reset();
//            }
//        });
//        menu.add(showGoals);
//
//        add(menu, NORTH);
//        add(new JScrollPane(tree), BorderLayout.CENTER);
//
//        reset();
//    }
//
//    protected void reset() {
//
//        components.clear();
//        nodes.clear();
//        toAdd.clear();
//        lastUpdateTime = 0;
//
//        root = new DefaultMutableTreeNode("Tasks");
//        model = new DefaultTreeModel(root);
//        tree.setModel(model);
//
//        needsRestart = true;
//        update();
//    }
//
//
//    @Override
//    public Class[] getEvents() {
//
//        return new Class[]{OUT.class,
//                //TaskRemove.class,
//                FrameEnd.class, Events.Restart.class /*, TaskRemove.class*/};
//    }
//
//    public void add(Task t) {
//        if (isVisible(t))
//            toAdd.add(t);
//    }
//
//    public void remove(Task t) {
//        toRemove.add(t);
//        toAdd.remove(t);
//    }
//
//    public DefaultMutableTreeNode getNode(final Task t) {
//        return nodes.get(t);
//    }
//
//    public DefaultMutableTreeNode newNode(final Task t) {
//        DefaultMutableTreeNode existing = nodes.get(t);
//        if (existing != null) {
//            return existing;
//        }
//
//        //String key = t.name().toString();
//        DefaultMutableTreeNode d = new DefaultMutableTreeNode(t);
//        nodes.put(t, d);
//        return d;
//    }
//
//    protected boolean isVisible(final Task t) {
//        if ((t.isJudgment()) && (!showingJudgments)) return false;
//        if ((t.isQuestion()) && (!showingQuestions)) return false;
//        if ((t.isGoal()) && (!showingGoals)) return false;
//        return t.getPriority() >= priorityThreshold;
//    }
//
//    public void update() {
//        //TODO get existing Tasks at the next frame event by new method: memory.getTasks() which iterates all concepts tasklinks
//        if (needsRestart) {
//            tasks.clear();
//            nar.getTasks(true, false, false, tasks);
//            for (Task t : tasks)
//                if (isVisible(t))
//                    add(t);
//        }
//
//        //remove dead tasks
//        for (Task t : nodes.keySet()) {
//            if (!isVisible(t))
//                toRemove.add(t);
//        }
//
//        long now = System.currentTimeMillis();
//        if (now - lastUpdateTime > updatePeriodMS) {
//            SwingUtilities.invokeLater(this);
//        }
//    }
//
//    @Override
//    public void run() {
//        for (Task t : toRemove) {
//            DefaultMutableTreeNode node = nodes.remove(t);
//            if (node != null) {
//                TreeNode p = node.getParent();
//                node.removeFromParent();
//                needRefresh.add(p);
//                needRefresh.remove(node);
//            }
//            components.remove(t);
//            toAdd.remove(t);
//        }
//
//        List<Task> remaining = new ArrayList<>();
//
//        for (Task t : toAdd) {
//
//            Task parent = t.getParentTask();
//
//            if (parent != null && parent.equals(t)) {
//                System.err.println((t + " has parentTask equal to itself"));
//                System.err.println(t.getExplanation());
//                throw new RuntimeException(t + " has parentTask equal to itself");
//            }
//
//            DefaultMutableTreeNode tnode = getNode(t);
//            if (tnode != null) {
//                continue;
//            }
//
//            if (t.isInput()) {
//
//                //System.out.println(tnode + " add to root");
//                root.add(newNode(t));
//                needRefresh.add(root);
//            } else {
//                DefaultMutableTreeNode pnode;
//                if (t.isAmnesiac()) {
//                    pnode = getAmnesiaNode();
//                }
//                else {
//                    pnode = getNode(parent);
//                }
//
//
//                if (pnode != null) {
//                    tnode = newNode(t);
//                    TreeNode parentNode = tnode.getParent();
//                    if ((parentNode != null) && (parentNode.equals(pnode))) {
//                        //just refresh, same location
//                        needRefresh.add(pnode);
//                    }
//                    else {
//                        tnode.removeFromParent();
//                        pnode.add(tnode);
//                        needRefresh.add(pnode);
//                    }
//
//                } else {
//                    //throw new RuntimeException(t + " unknown parent: " + t.getParentTask() );
//                    remaining.add(t);
//                }
//
//            }
//        }
//
//        for (TaskComponent t : components.values())
//            t.updateTask();
//
//        for (TreeNode t : needRefresh)
//            model.reload(t);
//
//
//        needRefresh.clear();
//        toAdd.clear();
//        toRemove.clear();
//
//        toAdd.addAll(remaining);
//
//        repaint();
//        lastUpdateTime = System.currentTimeMillis();
//    }
//
//    private DefaultMutableTreeNode getAmnesiaNode() {
//        if (amnesia == null) {
//            amnesia = new DefaultMutableTreeNode("(Amnesia)");
//            root.add(amnesia);
//        }
//        return amnesia;
//    }
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//        super.visibility(appearedOrDisappeared);
//        cpBuilder.setActive(appearedOrDisappeared);
//        if (!appearedOrDisappeared) {
//            reset();
//        }
//    }
//
//    @Override
//    public void event(Class channel, Object[] arguments) {
//
//        if (channel == OUT.class) {
//            add((Task) arguments[0]);
//        }
//        /*} else if (channel == TaskRemove.class) {
//            remove((Task) arguments[0]);
//        } */else if (channel == FrameEnd.class) {
//            update();
//        } else if (channel == Events.Restart.class) {
//            update();
//        }
//
//    }
//
//    public interface TaskComponent {
//        public void updateTask();
//
//        void setSelected(boolean hasFocus);
//    }
//
//    public class TaskEdit extends JPanel implements TaskComponent {
//        private final Task task;
//        private final TaskLabel label;
//        private final NSliderSwing priSlider;
//
//        public TaskEdit(Task t) {
//            super( new FlowLayout(WrapLayout.LEFT,4,1));
//            this.task = t;
//
//            {
//                priSlider = new NSliderSwing(0.5f, 0, 1f) {
//                    @Override
//                    public void onChange(float v) {
//                        t.getBudget().setPriority(v);
//                    }
//                };
//                priSlider.setPrefix("Pri");
//
//                Dimension dim = new Dimension(75, 25);
//                priSlider.setMaximumSize(dim);
//                priSlider.setPreferredSize(dim);
//
//                add(priSlider);
//            }
//
//            {
//                Concept c = nar.concept(task.getTerm());
//                if (c!=null) {
//                    ConceptPanelBuilder.ConceptPanel p = cpBuilder.newPanel(c, false, false, 32);
//                    add(p);
//                }
//            }
//
//
//            label = new TaskLabel(task);
//            add(label);
//
//
//        }
//
//        @Override
//        public void updateTask() {
//            label.updateTask();
//            priSlider.setValue(task.getPriority());
//            setBackground(label.getBackground());
//        }
//
//        @Override
//        public void setSelected(boolean selected) {
//
//            label.setSelected(selected);
//
//            //setBorder(label.getBorder());
//
//        }
//    }
//
////    protected void updateComponent(Task t, JLabel c) {
////        if (c instanceof TaskLabel)
////            ((TaskLabel)c).updateTask();
////    }
//
//    public class TaskLabel extends JLabel implements TaskComponent {
//        private final Task task;
//
//        public TaskLabel(Task t) {
//            this.task = t;
//
//            //setOpaque(false);
//            setOpaque(true);
//            setFont(Video.monofont);
//
//            updateTask();
//        }
//
//        public void updateTask() {
//            final Task t = task;
//
//            Concept con = nar.memory.concept(t.getTerm());
//            float conPri = 0;
//            if (con == null) {
//                /*System.err.println("TaskTree: " + t + " missing concept.  either memory was reset or concept should have been created but wasnt.");*/
//                //toRemove.add(t);
//            } else {
//                conPri = con.getPriority();
//            }
//            float taskPri = t.getPriority();
//            Truth desire = t.getDesire();
//
//            Color iColor;
//            if (desire != null) {
//                float confidence = t.getDesire().getConfidence();
//                iColor = new Color(0, confidence / 1.5f, conPri / 1.5f, 0.75f + 0.25f * confidence);
//            } else {
//                iColor = new Color(0, 0, conPri / 1.5f, 1f);
//            }
//            setBorder(new MatteBorder(0, 15, 0, 0, iColor));
//
//
//            float meanPri = (conPri + taskPri) / 2f;
//            final float mpi = 0.5f + 0.5f * meanPri;
//            setForeground(new Color(mpi, mpi, mpi));
//
//            final float hue = 0.3f + 0.5f * conPri;
//            Color c = Color.getHSBColor(hue, 0.4f, conPri * 0.2f);
//            //setBackground(new Color(1f-taskPri/4f,1f,1f-taskPri/4f));
//            setBackground(c);
//
//            setFont(Video.monofont.deriveFont(14f + taskPri * 4f));
//            setText(t.name().toString());
//
//        }
//
//        public void setSelected(boolean selected) {
//            /*final int bt = 1;
//            if (!selected) {
//                setBorder(new EmptyBorder(bt, bt, bt, bt));
//            } else {
//                setBorder(new LineBorder(Color.GRAY, bt));
//            }*/
//
//        }
//    }
//
//
//    final DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {
//
//        public Component getTreeCellRendererComponent(
//                JTree tree,
//                Object value,
//                boolean selected,
//                boolean expanded,
//                boolean leaf,
//                int row,
//                boolean hasFocus) {
//
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//            Object v = node.getUserObject();
//            if (v instanceof Task) {
//                Task tv = (Task) v;
//                TaskComponent c = components.get(tv);
//                if (c == null) {
//                    if (tv.isInput())
//                        c = new TaskEdit(tv);
//                    else
//                        c = new TaskLabel(tv);
//                    components.put(tv, c);
//                } else {
//                    c.updateTask();
//                }
//                c.setSelected(hasFocus);
//                return (JComponent) c;
//            }
//
//            return super.getTreeCellRendererComponent(
//                    tree, value, selected,
//                    expanded, leaf, row,
//                    hasFocus);
//        }
//
//    };
//
//    final TreeCellEditor editor = new DefaultTreeCellEditor(tree, cellRenderer) {
//        @Override
//        protected boolean canEditImmediately(EventObject event) {
//            if ((event instanceof MouseEvent) &&
//                    SwingUtilities.isLeftMouseButton((MouseEvent) event)) {
//                MouseEvent me = (MouseEvent) event;
//
//
//                return ((me.getClickCount() >= 1) &&
//                        inHitRegion(me.getX(), me.getY()));
//            }
//            return (event == null);
//        }
//
//        @Override
//        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
//            return cellRenderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
//        }
//    };
//
// }
