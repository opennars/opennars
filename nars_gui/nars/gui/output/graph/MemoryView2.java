package nars.gui.output.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.Observer;
import nars.core.Events.FrameEnd;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.gui.NARSwing;
import nars.gui.NCanvas;
import nars.gui.NSlider;
import nars.gui.output.graph.PGraphPanel;
import nars.language.Term;
import nars.util.NARGraph;
import nars.util.sort.IndexedTreeSet;


class GraphCanvasSwing extends NCanvas {

    NAR nar;

    int mouseScroll = 0;

    
    float selection_distance = 10;
    public float maxNodeSize = 40f;
    float FrameRate = 30f;
    float boostMomentum = 0.98f;
    float boostScale = 6.0f;
    
    float vertexTargetThreshold = 4;
    
    private boolean compressLevels = true;
    boolean drawn = false;
    
    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    
    int maxNodesWithLabels = 300;
    int maxNodes = 1000;
    int maxEdgesWithArrows = 500;
    int maxEdges = 1500;
    
    float minPriority = 0;
            
    public int mode = 0;
    
    boolean showBeliefs = false;
    
    float nodeSpeed = 0.1f;
    
    float sx = 800;
    float sy = 800;


    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 32;
    private boolean updateNext;
    float nodeSize = 10;
    
    float lineAlpha = 0.75f;
    float lineWidth = 3.8f;
    
    
    Map<Object,VertexDisplay> vertices = new HashMap();
    Set<Object> deadVertices = new HashSet();
    Map<Class,Integer> edgeColors = new HashMap(16);
    
    Graphics2D g;
    
    NARGraph graph;
    boolean showSyntax;

    //bounds of last positioned vertices
    float minX=0, minY=0, maxX=0, maxY=0;
    float motionBlur = 0.0f;
    float scale = 1.0f;
    int xOffset = 0, yOffset = 0;
    Font f = NARSwing.monofont.deriveFont(16f);

    public GraphCanvasSwing() {
        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double r = e.getPreciseWheelRotation();
                
                if (r < 0) {
                    scale *= 0.9f;
                }
                else if (r > 0) {
                    scale *= 1.1f;
                }
                
                redraw();
            }
        });
        final MouseAdapter c;
        addMouseMotionListener(c = new MouseAdapter() {
            private Point startLocation;
            private int startXOffset, startYOffset;

            @Override
            public void mousePressed(MouseEvent e) {
                startLocation = e.getPoint();
                startXOffset = xOffset;
                startYOffset = yOffset;
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startLocation==null) {
                    startLocation = e.getPoint();
                    return;
                }
                Point currentLocation = e.getPoint();
                
                int deltaX  = currentLocation.x - startLocation.x;
                int deltaY  = currentLocation.y - startLocation.y;
                xOffset = startXOffset + deltaX;
                yOffset = startYOffset + deltaY;
                
                redraw();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                startLocation = null;
            }
            
        });
        addMouseListener(c);
        
    }

    
    public class VertexDisplay {
        float x, y, tx, ty;
        Color color;
        float radius;
        float alpha;
        String label;
        private final Object object;
        private float boost;
        float stroke;
        private boolean visible;

        public VertexDisplay(Object o) {
            this.object = o;
            
            x = y = 0;
            tx = x;
            ty = y;
            stroke = 0;
            radius = nodeSize;
            visible = true;
            
            if (o instanceof Concept) {
                label = ((Concept)o).term.toString();
            }
            else {
                label = o.toString();
            }
            
            if (label.length() > MAX_UNSELECTED_LABEL_LENGTH)
                label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH-3) + "..";
            
           
            update(o);

        }
        
        public void position(float level, float index, float priority) {
            float LEVELRAD = maxNodeSize * 2.5f;
            
            if (mode == 2) {
                tx = ((float)Math.sin(index/10d) * LEVELRAD) * 5 * ((10+index)/20);
                //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
                ty = (1.0f - priority) * LEVELRAD * 150;
            }
            else if (mode == 1) {

                //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
                double radius = (1.0 - priority) * LEVELRAD + 8;
                float angle = index; //TEMPORARY
                tx = (float)(Math.cos(angle/3.0) * radius) * LEVELRAD;
                ty = (float)(Math.sin(angle/3.0) * radius) * LEVELRAD;
            }
            else if (mode == 0) {
                //gridsort
                tx = index * LEVELRAD;
                ty = (1.0f - priority) * LEVELRAD * 100;
            }
            
        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return object.equals(obj);
        }
        
        public void draw(boolean text) {
            update();
            
            if (!visible) return;
             
            /*if (stroke > 0) {
              stroke(Color.WHITE.getRGB());
                strokeWeight(stroke);
            }*/
                        
            float r = (radius+boost*boostScale) * nodeSize / 2f;
            if (r > 0) {
                g.setPaint(color);
                g.fillOval((int)(x-(r/2)), (int)(y-(r/2)), (int)r, (int)r);

                if (text && (label!=null)) {

                    g.setPaint(Color.WHITE);
                    g.setFont(f);
                    //textSize(r/2);
                    g.drawString(label, x, y);
                }
            }
            
            /*if (stroke > 0) {                
                //reset stroke
                noStroke();
            }*/
        }
               
        protected void update() {            
            x = ( x * (1.0f - nodeSpeed) + tx * (nodeSpeed) );
            y = ( y * (1.0f - nodeSpeed) + ty * (nodeSpeed) );            
            
            if ((Math.abs(tx-x) + Math.abs(ty-y)) > vertexTargetThreshold) {
                //keep animating if any vertex hasnt reached its target
                drawn = false;
            }
            
            boost *= boostMomentum;
        }

        private void update(Object o) {
            visible = true;
            
            if (o instanceof Sentence) {
                 Sentence kb = (Sentence)o;
                 TruthValue tr = kb.truth;
                 float confidence = 0.5f;
                 if (tr!=null) {
                     confidence = tr.getConfidence();
                     double hue = 0.25 + (0.25 * (kb.truth.getFrequency()-0.5));
                     color = Color.getHSBColor((float)hue, 0.9f, 0.9f);
                 }
                 else {
                     color = Color.GRAY;
                 }
                alpha = confidence*0.75f+0.25f;
                
                Term t = ((Sentence) o).content;
                radius = (float)(Math.log(1+2 + confidence) * nodeSize);
             }
             else if (o instanceof Task) {
                Task ta = (Task)o;
                radius = 2.0f + ta.getPriority()*2.0f;
                alpha = ta.getDurability();
                color = NARSwing.getColor(o.getClass().toString(), 0.8f, 0.8f);
             }            
             else if (o instanceof Concept) {
                Concept co = (Concept)o;
                Term t = co.term;
                
                radius = (float)(2 + 6 * co.budget.summary() * nodeSize);
                alpha = PGraphPanel.vertexAlpha(o);                             
                color = NARSwing.getColor(t.getClass().toString(), 0.8f, 0.8f);
                stroke = 5;
             }
             else if (o instanceof Term) {
                Term t = (Term)o;                
                radius = (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
                alpha = PGraphPanel.vertexAlpha(o);                             
                color = NARSwing.getColor(o.getClass().toString(), 0.8f, 0.8f);
             }
        }

    }

    
    public VertexDisplay updateVertex(Object o) {
        VertexDisplay v = vertices.get(o);
        if (v!=null) {
            v.update(o);
            return v;
        }
        v = new VertexDisplay(o);
        
        vertices.put(o, v);
        
        return v;
    }
    
    public int getEdgeColor(Object e) {
        Integer i = edgeColors.get(e.getClass());
        if (i == null) {
            i = PGraphPanel.getColor(e.getClass());
            edgeColors.put(e.getClass(), i);
        }
        return i;
    }

    /** should be called from NAR update thread, not swing thread  */
    public void updateGraph() {
        final Sentence currentBelief = nar.memory.getCurrentBelief();
        final Concept currentConcept = nar.memory.getCurrentConcept();
        final Task currentTask = nar.memory.getCurrentTask();
        final IndexedTreeSet<Concept> concepts = new IndexedTreeSet(new Comparator<Concept>() {
            @Override public int compare(Concept o1, Concept o2) {
                return o1.name().toString().compareTo(o2.name().toString());
            }            
        });

        if ((nar.getTime() != lasttime) || (updateNext)) {
            updateNext = false;
            lasttime = nar.getTime();
            


            synchronized (vertices) {
                deadVertices.clear();
                
                if (mode == 0) {
                    //index concepts
                    concepts.addAll(nar.memory.getConcepts());              
                }
                deadVertices.addAll(vertices.keySet());

                    try {
                        graph = new NARGraph();
                        graph.add(nar, new NARGraph.ExcludeBelowPriority(minPriority), 
                                new NARGraph.DefaultGraphizer(showBeliefs, true, showBeliefs, true, false) {

                            float level;
                            float index = 0;
                            int levelContents = 0;
                            private float priority;
                            Term lastTerm = null;
                            VertexDisplay lastTermVertex = null;

                            public void preLevel(NARGraph g, int l) {
                                if (!compressLevels)
                                    level = l;

                                levelContents = 0;

                                if (mode == 1)
                                    index = 0;
                            }


                            public void postLevel(NARGraph g, int l) {                        
                                if (compressLevels) {
                                    if (levelContents > 0)
                                        level--;
                                }                        
                            }

                            @Override
                            public void onConcept(NARGraph g, Concept c) {
                                super.onConcept(g, c);

                                priority = c.getPriority();
                                level = (float)(priority*100.0);

                                if (mode == 0) {
                                    index = concepts.entryIndex(c);                                    
                                }
                                else {
                                    if ((lastTerm!=null) && (c.term.equals(lastTerm))) {
                                        //terms equal to concept, ordinarily displayed as subsequent nodes
                                        //should just appear at the same position as the concept
                                        //lastTermVertex.visible = false;
                                        lastTermVertex.position(level, index, priority);
                                        lastTermVertex.visible = false;
                                    }
                                    else
                                        index++;
                                }

                                VertexDisplay d = updateVertex(c);
                                d.position(level, index, priority);
                                deadVertices.remove(c);

                                if (currentConcept!=null)
                                    if (c.equals(currentConcept))
                                        d.boost = 1.0f;                        


                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;
                            }

                            @Override
                            public void onTerm(Term t) {

                                index++;                                               

                                VertexDisplay d = updateVertex(t);
                                d.position(level, index, priority);
                                deadVertices.remove(d);

                                lastTerm = t;
                                lastTermVertex = d;

                                levelContents++;

                            }

                            @Override
                            public void onBelief(Sentence kb) {
                                index+= 0.25f;

                                VertexDisplay d = updateVertex(kb);
                                d.position(level, index, priority);                    
                                deadVertices.remove(kb);

                                if (currentBelief!=null)
                                    if (kb.equals(currentBelief))
                                        d.boost = 1.0f;                        

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;

                            }

                            @Override
                            public void onQuestion(Task t) {
                                index+= 0.25f;

                                VertexDisplay d = updateVertex(t);
                                d.position(level, index, priority);                    
                                deadVertices.remove(t);

                                if (currentTask!=null)
                                    if (t.equals(currentTask))
                                        d.boost = 1.0f;                        

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;
                            }


                        });
                    } catch (Exception e)  { System.err.println(e); } 


                for (Object v : deadVertices)
                    vertices.remove(v);
            }
            
            drawn = false;
        }
        
        redraw();
    }
    
    Runnable redrawSwing = new Runnable() {

        @Override
        public void run() {
            showBuffer(g);            
        }
        
    };
    
    protected void redraw() {
        g = getBufferGraphics();
        draw();
        SwingUtilities.invokeLater(redrawSwing);
    }

    
    public void draw() {                
        
        if (graph== null) return;

        //if (drawn)
        //    return;
        
        drawn = true; //allow the vertices to invalidate again in drawit() callee

        AffineTransform tRoot = new AffineTransform();
        g.setTransform(tRoot);
        g.setColor(backgroundClearColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        AffineTransform tx = new AffineTransform();
        tx.translate(xOffset, yOffset);
        tx.scale(scale, scale);
        g.setTransform(tx);
        
        
//        if (motionBlur > 0) {
//            g.clearRect(0, 0, getWidth(), getHeight());            
//            //g.fillRect(0,0,0,255f*(1.0f - motionBlur));            
//        }
//        else {        
//            g.clearRect(0, 0, getWidth(), getHeight());
//        }
        

    
        
        synchronized (vertices) {
            //for speed:
            //strokeCap(SQUARE);
            //strokeJoin(PROJECT);

            int numEdges = graph.edgeSet().size();
            if (numEdges < maxEdges) {
                for (final Object edge : graph.edgeSet()) {

                    final VertexDisplay elem1 = vertices.get(graph.getEdgeSource(edge));
                    final VertexDisplay elem2 = vertices.get(graph.getEdgeTarget(edge));                
                    if ((elem1 == null) || (elem2 == null))
                        continue;
                    
                    

                    BasicStroke lineStroke = new BasicStroke(5f * (elem1.radius + elem2.radius)/2.0f / lineWidth );
                    
                    g.setStroke(lineStroke);       
                    g.setPaint(Color.GRAY);
                            
                    //stroke(getEdgeColor(edge), (elem1.alpha + elem2.alpha)/2f * 255f*lineAlpha );
                    //strokeWeight( (elem1.radius + elem2.radius)/2.0f / lineWidth );

                    float x1 = elem1.x;
                    float y1 = elem1.y;
                    float x2 = elem2.x;
                    float y2 = elem2.y;
                    
                    if (numEdges < maxEdgesWithArrows)
                        drawArrow(x1, y1, x2, y2);
                    else
                        drawArrow(x1, y1, x2, y2);

                    //float cx = (x1 + x2) / 2.0f;
                    //float cy = (y1 + y2) / 2.0f;
                    //text(edge.toString(), cx, cy);
                }
            }

            //g.setStroke(null);
            

            int numNodes = vertices.size();
            boolean text = numNodes < maxNodesWithLabels;
            if (numNodes < maxNodes)
                for (final VertexDisplay d : vertices.values())
                    d.draw(text);

        }   
        
    }

//    
//    void drawArrowAngle(final float cx, final float cy, final float len, final float angle){
//      pushMatrix();
//      translate(cx, cy);
//      rotate(radians(angle));
//      line(0,0,len, 0);
//      line(len, 0, len - 8*2, -8);
//      line(len, 0, len - 8*2, 8);
//      popMatrix();
//    }
//
    void drawArrow(final float x1, final float y1, final float x2, final float y2) {
        float cx = (x1+x2)/2f;
        float cy = (y1+y2)/2f;
        float len = (float)Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
        float a = (float)(Math.atan2(y2-y1,x2-x1)*180.0/Math.PI);

        g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
        //drawArrowAngle(x1, y1, len, a);
    }    
//    void drawLine(final float x1, final float y1, final float x2, final float y2) {
//        line(x1,y1,x2,y2);
//    }
    
    void setUpdateNext() {
        updateNext = true;
        drawn = false;        
    }


    public class Edge {

        public final int from;
        public final int to;
        public final int alpha;

        public Edge(final int from, final int to, int alpha) {
            this.from = from;
            this.to = to;
            this.alpha = alpha;
        }
        public Edge(final int from, final int to, float alpha) {
            this(from, to, (int)(255.0 * alpha));
        }
    }
}

public class MemoryView2 extends JPanel {

    GraphCanvasSwing app = null;

    public MemoryView2(NAR n) {
        super(new BorderLayout());


        app = new GraphCanvasSwing();        
        app.nar = n;
        
        this.setSize(1000, 860);//initial size of the window
        this.setVisible(true);

        Container content = this;
        

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        final JComboBox modeSelect = new JComboBox();
        modeSelect.addItem("GridSort");
        modeSelect.addItem("Circle");
        modeSelect.addItem("Grid");
        modeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.mode = modeSelect.getSelectedIndex();
                app.setUpdateNext();
            }            
        });
        menu.add(modeSelect);
        
        
        final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
        beliefsEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                app.showBeliefs = (beliefsEnable.isSelected());        
                app.setUpdateNext();
            }
        });
        menu.add(beliefsEnable);
        
        /*
        final JCheckBox syntaxEnable = new JCheckBox("Syntax");
        syntaxEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                app.showSyntax = (syntaxEnable.isSelected());        
                app.setUpdateNext();
            }
        });
        menu.add(syntaxEnable);        
        */
        
        NSlider nodeSize = new NSlider(app.nodeSize, 1, app.maxNodeSize) {
            @Override
            public void onChange(float v) {
                app.nodeSize = (float)v;
                app.drawn = false;
            }          
        };        
        nodeSize.setPrefix("Node Size: ");
        nodeSize.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSize);

        
        
        //final int numLevels = ((Bag<Concept>)n.memory.concepts).levels;
        NSlider maxLevels = new NSlider(1, 0, 1) {
            @Override
            public void onChange(float v) {                
                app.minPriority = (float)(1.0 - v);
                app.setUpdateNext();
            }          
        };        
        maxLevels.setPrefix("Min Level: ");
        maxLevels.setPreferredSize(new Dimension(125, 25));
        menu.add(maxLevels);

        NSlider nodeSpeed = new NSlider(app.nodeSpeed, 0.001f, 0.99f) {
            @Override
            public void onChange(float v) {
                app.nodeSpeed = (float)v;
                app.drawn = false;
            }          
        };        
        nodeSpeed.setPrefix("Node Speed: ");
        nodeSpeed.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSpeed);        
        
        NSlider blur = new NSlider(0, 0, 1.0f) {
            @Override
            public void onChange(float v) {
                app.motionBlur = (float)v;
                app.drawn = false;
            }          
        };        
        blur.setPrefix("Blur: ");
        blur.setPreferredSize(new Dimension(85, 25));
        menu.add(blur);        
        
        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);
        

        n.memory.event.on(FrameEnd.class, new Observer() {
            @Override
            public void event(Class event, Object... arguments) {
                if (app!=null) {
                    app.updateGraph();                    
                }
                else
                    n.memory.event.off(FrameEnd.class, this);
            }  
        });
    
    }

//    @Override
//    protected void close() {
//        app.stop();
//        app.destroy();
//        getContentPane().removeAll();
//        app = null;
//    }
//
    
    
    
}
