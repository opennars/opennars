/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph;

import automenta.vivisect.Video;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.ProcessingGraphCanvas.VertexDisplay;
import automenta.vivisect.swing.NSlider;
import automenta.vivisect.swing.Swing;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.util.NARGraph;
/**
 *
 * @author me
 */
public class NARGraphDisplay<V,E> implements GraphDisplay<V,E> {

    float maxNodeSize = 160f;
    
    float lineWidth = 4f;
    float nodeSize = 16f;
    static final int MAX_UNSELECTED_LABEL_LENGTH = 32;
    float nodeSpeed = 0.05f;
    
    /*
                if (o instanceof Sentence) {
                //Sentence kb = (Sentence) o;
                //TruthValue tr = kb.truth;
                //float confidence = 0.5f;
                //alpha = confidence * 0.75f + 0.25f;

                //Term t = ((Sentence) o).content;
                //radius = (float) (Math.log(1 + 2 + confidence) * nodeSize);
            } else if (o instanceof Task) {
                //Task ta = (Task) o;
                //radius = 2.0f + ta.getPriority() * 2.0f;
                //alpha = ta.getDurability();                
            } else if (o instanceof Concept) {
                //Concept co = (Concept) o;
                //Term t = co.term;

                //radius = (float) (2 + 6 * co.budget.summary() * nodeSize);
                //alpha = PGraphPanel.vertexAlpha(o);                
                //stroke = 5;
            } else if (o instanceof Term) {
                //Term t = (Term) o;
                //radius = (float) (Math.log(1 + 2 + t.getComplexity()) * nodeSize);
                //alpha = PGraphPanel.vertexAlpha(o);                
            }

    */

    @Override
    public GraphDisplay.Shape getVertexShape(V v) {
        if (v instanceof Task) {
            return Shape.Rectangle;
        }
        else {
            return Shape.Ellipse;
        }        
    }
    
    @Override public String getVertexLabel(final V o) {
        String label;
        if (o instanceof Concept) {
             label = ((Concept) o).term.toString();
         } else {
             label = o.toString();
         }

         if (label.length() > MAX_UNSELECTED_LABEL_LENGTH) {
             label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH - 3) + "..";
         }
         return label;
    }
    
    @Override
    public float getVertexSize(final V v) {
        //TODO normalize so it = 1
        float size = 1;


        if (v instanceof Item) {
            size *= ((Item)v).getPriority() * 0.5f + 0.5f;
        }
        else if (v instanceof Sentence) {
            //size *= ((Sentence)v).getPriority();
        }
                
        return size * nodeSize;
    }

    @Override
    public int getVertexColor(V o) {
        Object x = o;
        if (x instanceof Concept) x = ((Concept)o).term;
        return Swing.getColor(x.getClass().hashCode(), 0.75f, 0.95f).getRGB();
        
    }


    @Override
    public float getEdgeThickness(E edge, VertexDisplay source, VertexDisplay target) {        
        if (edge instanceof NARGraph.TermLinkEdge) {
            TermLink t = ((NARGraph.TermLinkEdge)edge).termLink;
            float p = t.getPriority();            
            return (1 + p) * lineWidth;
        }
        if (edge instanceof NARGraph.TaskLinkEdge) {
            TaskLink t = ((NARGraph.TaskLinkEdge)edge).taskLink;
            float p = t.targetTask.getPriority();            
            return (1 + p) * lineWidth;
        }

        return lineWidth;
    }

    int defaultEdgeColor = Video.color(127,127,127,127);
    int defaultTextColor = Video.color(255,255,255,255);
    
    @Override
    public int getEdgeColor(final E e) {
        if (e instanceof NARGraph.TermLinkEdge) {
            TermLink t = ((NARGraph.TermLinkEdge)e).termLink;
            float p = t.getPriority();
            return Video.color(255f * (0.5f + p*0.5f), 125f, 125f, 255f * (0.5f + p*0.5f) );
        }
        if (e instanceof NARGraph.TaskLinkEdge) {
            TaskLink t = ((NARGraph.TaskLinkEdge)e).taskLink;
            float p = t.targetTask.getPriority();
            return Video.color(125f, 255f * (0.5f + p*0.5f), 125f, 255f * (0.5f + p*0.5f) );
        }
        /*Integer i = edgeColors.get(e.getClass());
        if (i == null) {
            i = PGraphPanel.getColor(e.getClass());
            edgeColors.put(e.getClass(), i);
        }*/
        return defaultEdgeColor;
    }

    @Override
    public int getTextColor(V v) {
        return defaultTextColor;

    }

    @Override
    public boolean updateNext() {
        //will be updated manually
        return false;
    }

    public JPanel getControls() {
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));

        NSlider nodeSize = new NSlider(this.nodeSize, 1, maxNodeSize) {
            @Override
            public void onChange(float v) {
                NARGraphDisplay.this.nodeSize = v;
                //app.drawn = false;
            }
        };
        nodeSize.setPrefix("Node Size: ");
        nodeSize.setPreferredSize(new Dimension(80, 25));
        menu.add(nodeSize);
        
        NSlider edgeWidth = new NSlider(this.lineWidth, 0.95f, maxNodeSize/4f) {
            @Override public void onChange(float v) {
                lineWidth = v;
                //app.drawn = false;
            }
        };
        edgeWidth.setPrefix("Line Thick: ");
        edgeWidth.setPreferredSize(new Dimension(80, 25));
        menu.add(edgeWidth);

        

        NSlider nodeSpeed = new NSlider(this.nodeSpeed, 0.001f, 0.99f) {
            @Override
            public void onChange(float v) {
                NARGraphDisplay.this.nodeSpeed = (float) v;
                //app.drawn = false;
            }
        };
        nodeSpeed.setPrefix("Speed: ");
        nodeSpeed.setPreferredSize(new Dimension(70, 25));
        menu.add(nodeSpeed);

        NSlider blur = new NSlider(0, 0, 1.0f) {
            @Override
            public void onChange(float v) {
                //motionBlur = (float) v;
                //app.drawn = false;
            }
        };
        blur.setPrefix("Blur: ");
        blur.setPreferredSize(new Dimension(60, 25));
        menu.add(blur);

        
        
        return menu;
    }
    
    
}
    

