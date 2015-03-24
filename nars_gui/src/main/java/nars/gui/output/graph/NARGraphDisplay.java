///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.gui.output.graph;
//
//import automenta.vivisect.Video;
//import automenta.vivisect.graph.AbstractGraphVis;
//import automenta.vivisect.graph.EdgeVis;
//import automenta.vivisect.graph.GraphDisplay;
//import automenta.vivisect.graph.VertexVis;
//import automenta.vivisect.swing.NSlider;
//import nars.NAR;
//import nars.nal.entity.*;
//import nars.util.graph.NARGraph;
//
//import javax.swing.*;
//import java.awt.*;
///**
// *
// * @author me
// */
//@Deprecated public class NARGraphDisplay<V,E> implements GraphDisplay<V,E> {
//
//    private final NAR nar;
//    float maxNodeSize = 160f;
//
//    float lineWidth = 4f;
//    float nodeSize = 16f;
//    int maxLabelLen = 16;
//    float nodeSpeed = 0.2f;
//    float textSize = 1f;
//
//    int defaultEdgeColor = Video.color(127,127,127,200);
//    int defaultTextColor = Video.color(255,255,255,255);
//
//    public NARGraphDisplay setTextSize(float textSize, int maxLabelLen) {
//        this.textSize = textSize;
//        this.maxLabelLen = maxLabelLen;
//        return this;
//    }
//
//    public NARGraphDisplay(NAR n) {
//        this.nar = n;
//    }
//
//
//
//    @Override
//    public void vertex(AbstractGraphVis<V, E> g, VertexVis<V, E> v) {
//        float alpha = 0.9f;
//        float saturation = 0.75f;
//        float brightness = 0.85f;
//
//        V o = v.getVertex();
//
//        v.shape = Shape.Ellipse;
//
//        float rad = 1f;
//
//        Item i;
//        if (o instanceof Item)
//            i = (Item)o;
//        else
//            i = null;
//
//        if (o instanceof Sentence) {
//            Sentence kb = (Sentence) o;
//            if (kb.truth!=null)  {
//                float confidence = kb.truth.getConfidence();
//                alpha = 0.5f + 0.5f * confidence;
//            }
//            //Term t = ((Sentence) o).content;
//            //rad = (float) (Math.log(1 + 2 + confidence));
//        } else if (o instanceof Task) {
//            rad = 1.0f + i.getPriority() * 2.0f;
//            alpha = i.budget.summary() * 0.5f + 0.5f;
//            saturation = 0.25f + 0.75f * i.getQuality();
//            v.shape = Shape.Rectangle;
//        } else if (o instanceof Term) {
//            Term t = (Term) o;
//            i = v.obj;
//            if (i == null) {
//                i = v.obj = nar.concept(t);
//            }
//            rad = (2 + 6 * i.budget.summary());
//            saturation = brightness = 0.25f + 0.75f * i.getQuality();
//        }
//
//
//
//        Object x = o;
//        float hue = Video.hashFloat(o.hashCode());
//
//
//
//
//
//        v.color = Video.colorHSB( hue, saturation, brightness, alpha );
//
//        String label;
//        if (o instanceof Concept) {
//             label = ((Concept) o).term.toString();
//
//         } else if (o instanceof Task) {
//             label = ((Task)o).sentence.toString();
//         } else {
//             label = o.toString();
//         }
//
//         if (label.length() > maxLabelLen) {
//             label = label.substring(0, maxLabelLen - 2) + "..";
//         }
//
//         v.label = label;
//         v.speed = nodeSpeed;
//         v.radius = rad * nodeSize;
//         v.textColor = defaultTextColor;
//         v.textScale = textSize;
//    }
//
//    @Override
//    public void edge(AbstractGraphVis<V, E> g, EdgeVis<V, E> e) {
//
//        E edge = e.edge;
//
//
//        int color = defaultEdgeColor;
//        float thickness = 2f;
//
//        if (edge instanceof NARGraph.TermLinkEdge) {
//            TermLink t = ((NARGraph.TermLinkEdge)edge).getObject();
//            float p = t.getPriority();
//            float d = t.getDurability();
//            thickness = (1 + t.getQuality()*2f) * lineWidth;
//            color = Video.color(255f * (0.15f + p*0.85f), 19f, 255f * (0.15f + d*0.85f), 255f * (0.1f + p*0.9f) );
//        }
//        if (edge instanceof NARGraph.TaskLinkEdge) {
//            TaskLink t = ((NARGraph.TaskLinkEdge)edge).getObject();
//            final Task tt = t.targetTask;
//            float tp = tt.getPriority(); //task priority
//            float lp = t.getPriority();  //link priority
//            float ap = (tp +lp)/2;
//            thickness = (1 + t.getQuality() + tt.getQuality()) * lineWidth;
//            color = Video.color(255f * (0.15f + lp*0.85f), 255f * (0.15f + tp*0.85f), 10f, 255f * (0.1f + ap*0.9f) );
//        }
//
//        e.color = color;
//        e.thickness = thickness;
//    }
//
//
//
////
////    @Override
////    public int getTextColor(V v) {
////        return defaultTextColor;
////
////    }
////
//
//    public JPanel getControls() {
//        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//        NSlider nodeSize = new NSlider(this.nodeSize, 1, maxNodeSize) {
//            @Override
//            public void onChange(float v) {
//                NARGraphDisplay.this.nodeSize = v;
//                //app.drawn = false;
//            }
//        };
//        nodeSize.setPrefix("DDNode Size: ");
//        nodeSize.setPreferredSize(new Dimension(80, 25));
//        menu.add(nodeSize);
//
//        NSlider edgeWidth = new NSlider(this.lineWidth, 0f, maxNodeSize/4f) {
//            @Override public void onChange(float v) {
//                lineWidth = v;
//                //app.drawn = false;
//            }
//        };
//        edgeWidth.setPrefix("Line Thick: ");
//        edgeWidth.setPreferredSize(new Dimension(80, 25));
//        menu.add(edgeWidth);
//
//
//
//        NSlider nodeSpeed = new NSlider(this.nodeSpeed, 0.001f, 0.99f) {
//            @Override
//            public void onChange(float v) {
//                NARGraphDisplay.this.nodeSpeed = v;
//                //app.drawn = false;
//            }
//        };
//        nodeSpeed.setPrefix("Speed: ");
//        nodeSpeed.setPreferredSize(new Dimension(70, 25));
//        menu.add(nodeSpeed);
//
//        NSlider fontSize = new NSlider(this.textSize, 0f, 2f) {
//            @Override
//            public void onChange(float v) {
//                NARGraphDisplay.this.textSize = v;
//                //app.drawn = false;
//            }
//        };
//        fontSize.setPrefix("Font: ");
//        fontSize.setPreferredSize(new Dimension(70, 25));
//        menu.add(fontSize);
//
//
//
//        return menu;
//    }
//
//    @Override
//    public boolean preUpdate(AbstractGraphVis<V, E> g) {
//        return true;
//    }
//
//    @Override
//    public boolean postUpdate(AbstractGraphVis<V, E> g) {
//        return true;
//    }
//
//
//}
//
//
