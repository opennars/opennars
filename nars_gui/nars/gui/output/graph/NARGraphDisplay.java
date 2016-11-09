/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph;

import automenta.vivisect.Video;
import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import automenta.vivisect.swing.NSlider;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.gui.util.NARGraph;
/**
 *
 * @author me
 */
public class NARGraphDisplay<V,E> implements GraphDisplay<V,E> {

    float maxNodeSize = 160f;
    
    float lineWidth = 4f;
    float nodeSize = 16f;
    int maxLabelLen = 99999;
    float nodeSpeed = 0.2f;
    float textSize = 1f;
    
    int defaultEdgeColor = Video.color(127,127,127,200);
    int defaultTextColor = Video.color(255,255,255,255);

    public NARGraphDisplay setTextSize(float textSize, int maxLabelLen) {
        this.textSize = textSize;
        this.maxLabelLen = maxLabelLen;
        return this;
    }
    
    
            

    @Override
    public void vertex(AbstractGraphVis<V, E> g, VertexVis<V, E> v) {
        float alpha = 0.9f;
        V o = v.getVertex();
        
        v.shape = Shape.Ellipse;

        float rad = 1f;
        
        if (o instanceof Sentence) {
            Sentence kb = (Sentence) o;
            if (kb.truth!=null)  {            
                float confidence = kb.truth.getConfidence();            
                alpha = 0.5f + 0.5f * confidence;
            }
            //Term t = ((Sentence) o).content;
            //rad = (float) (Math.log(1 + 2 + confidence));
        } else if (o instanceof Task) {
            Task ta = (Task) o;
            rad = 2.0f + ta.getPriority() * 2.0f;
            alpha = ta.getDurability();                
            v.shape = Shape.Rectangle;
        } else if (o instanceof Concept) {
            Concept co = (Concept) o;
            //Term t = co.term;

            rad = (2 + 6 * co.budget.summary());
            
            
            if (!co.beliefs.isEmpty()) {
                float confidence = co.beliefs.get(0).sentence.truth.getConfidence();
                alpha = 0.5f + 0.5f * confidence;
            }
            
            //v.stroke = 5;
        } else if (o instanceof Term) {
            Term t = (Term) o;
            //rad = (float) (Math.log(1 + 2 + t.getComplexity()));
        }
        
        

        Object x = o;
        if (x instanceof Concept) x = ((Concept)o).getTerm();
        float hue = Video.hashFloat(x.hashCode());


        
        float brightness = 0.85f;
        float saturation = 0.85f;
        
        v.color = Video.colorHSB( hue, saturation, brightness, alpha );

        String label;
        if (o instanceof Concept) {
             label = ((Concept) o).term.toString();
         } else if (o instanceof Task) {
             label = ((Task)o).sentence.toString();
         } else {
             label = o.toString();
         }

         if (label.length() > maxLabelLen) {
             label = label.substring(0, maxLabelLen - 2) + "..";
         }
         
         v.label = label;         
         v.speed = nodeSpeed;
         v.radius = rad * nodeSize;
         v.textColor = defaultTextColor;
         v.textScale = textSize;
    }

    @Override
    public void edge(AbstractGraphVis<V, E> g, EdgeVis<V, E> e) {
   
        E edge = e.edge;
        

        int color = defaultEdgeColor;
        
        float thickness = lineWidth;
        if (edge instanceof NARGraph.TermLinkEdge) {
            TermLink t = ((NARGraph.TermLinkEdge)edge).getObject();
            float p = t.getPriority();            
            thickness = (1 + p) * lineWidth;            
            color = Video.color(255f * (0.5f + p*0.5f), 125f, 125f, 255f * (0.5f + p*0.5f) );
        }
        if (edge instanceof NARGraph.TaskLinkEdge) {
            TaskLink t = ((NARGraph.TaskLinkEdge)edge).getObject();
            float p = t.targetTask.getPriority();            
            thickness = (1 + p) * lineWidth;
            color = Video.color(125f, 255f * (0.5f + p*0.5f), 125f, 255f * (0.5f + p*0.5f) );
        }
    
        e.color = color;
        e.thickness = thickness;
    }

    

//
//    @Override
//    public int getTextColor(V v) {
//        return defaultTextColor;
//
//    }
//

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
        
        NSlider edgeWidth = new NSlider(this.lineWidth, 0f, maxNodeSize/4f) {
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

        NSlider fontSize = new NSlider(this.textSize, 0f, 2f) {
            @Override
            public void onChange(float v) {
                NARGraphDisplay.this.textSize = (float) v;
                //app.drawn = false;
            }
        };
        fontSize.setPrefix("Font: ");
        fontSize.setPreferredSize(new Dimension(70, 25));
        menu.add(fontSize);
        

        
        return menu;
    }

    @Override
    public boolean preUpdate(AbstractGraphVis<V, E> g) {
        return true;
    }

    @Override
    public boolean postUpdate(AbstractGraphVis<V, E> g) {
        return true;
    }
    
    
}
    

