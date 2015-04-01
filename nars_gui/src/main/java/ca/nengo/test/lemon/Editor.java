package ca.nengo.test.lemon;

import automenta.vivisect.Video;
import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.ui.lib.world.handler.KeyboardHandler;
import ca.nengo.ui.lib.world.piccolo.object.*;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.ScriptGenException;
import nars.gui.output.graph.nengo.DefaultUINetwork;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Editor extends DefaultNetwork implements UIBuilder {
    public class TextArea extends AbstractWidget {
        public class Glyph {

            public final Stroke border = new BasicStroke(1);
            public final Stroke noStroke = new BasicStroke(0);
            Color textcolor = Color.WHITE;
            Color borderColor = new Color(70, 70, 70);
            Color bgColor = new Color(40, 40, 40);
            Font f = Video.monofont.deriveFont(64f);
            public int c;
            PBounds bounds;

            public Glyph(int c) {
                this.c = c;
            }

            protected void paint(Graphics2D g) {
                //System.out.println("Glyph.paint " + bounds);
                //border and background
                final int iw = (int) bounds.width;
                final int ih = (int) bounds.height;
                final int ix = (int) bounds.x;
                final int iy = (int) bounds.y;
                if (bgColor != null || borderColor != null) {
                    if (bgColor != null) {
                        //g.setStroke(noStroke);
                        g.setPaint(bgColor);
                        g.fillRect(ix, iy, iw, ih);
                    }
                    if (borderColor != null) {
                        g.setStroke(border);
                        g.setPaint(borderColor);
                        g.drawRect(ix, iy, iw, ih);
                    }

                }

                //draw glyph
                if (textcolor != null) {
                    g.setColor(textcolor);
                    g.setFont(f);
                    final int fs = f.getSize() / 2;
                    g.drawString(String.valueOf((char) c), (int) ix, (int) iy);
                }
            }


            public void setTextColor(Color textcolor) {
                this.textcolor = textcolor;
            }

            public void setBgColor(Color bgColor) {
                this.bgColor = bgColor;
            }

            public int getChar() {
                return c;
            }
        }

        public class Line extends ArrayList<Glyph> {
            public Line() {
            }

            public Line(String s) {
                int i = 0;
                for (char c : s.toCharArray())
                    add(new Glyph(c));
            }

            public String asString() {
                StringBuilder result = new StringBuilder();
                Iterator<Glyph> it = iterator();
                while(it.hasNext()){
                    Glyph g = it.next();
                    result.append(((Glyph) g).c);
                }
                return result.toString();
            }

            protected void paint(Graphics2D g) {
                Iterator<Glyph> it = iterator();
                while (it.hasNext()) {
                    it.next().paint(g);
                }
            }

        }

        public class Lines extends ArrayList<Line> {
            public Lines() {
                super();
            }

            public void setLine(int r, String str) {
                while (size() < r)
                    add(new Line(""));
                add(new Line(str));
                layOut();
            }

            public String asString() {
                StringBuilder result = new StringBuilder();
                /*for (Object l : nodeMap.values()) {
                    result.append(((Line) l).asString() + "\n");
                }*/
                return result.toString();
            }

            public void layOut() {
                for (int l = 0; l < size(); l++){
                    Line line = get(l);
                    for (int g = 0; g < line.size(); g++){
                        line.get(g).bounds = makeBounds(g, l);
                    }
                }
            }

            private PBounds makeBounds(int x, int y) {
                return new PBounds(x * charWidth, y * charHeight, charWidth, charHeight);
            }
        }

        public Lines lines = new Lines();

        public TextArea(String name){
            super(name);
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return null;
        }//use default?

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

        }

        @Override
        protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double ww, double hh) {
            //System.out.println("paintContext = [" + paintContext + "], ww = [" + ww + "], hh = [" + hh + "]");
            Graphics2D g = paintContext.getGraphics();
            Iterator<Line> it = lines.iterator();
            while (it.hasNext()) {
                it.next().paint(g);
            }
        }
    }

    TextArea area;
    int charWidth;
    int charHeight;
    public NodeViewer viewer;
    private UINetwork ui;
    public Lang lang;

    public Editor(String name, int charWidth, int charHeight) {
        super(name);
        area = new TextArea("area");
        setNode("area", area);
        setCharSize(charWidth, charHeight);
    }

    private void setCharSize(int charWidth, int charHeight) {
        this.charWidth = charWidth;
        this.charHeight = charHeight;
    }

    public void keyPressed(PInputEvent event, int c, int r) {
        char in = event.getKeyChar();

        String debug = "//getKeyChar:" + String.valueOf((int) in) +
                "event: " + event + "isActionKey: " + event.isActionKey() + "";
        area.lines.setLine(10, debug);
        System.out.println(debug);
        //System.out.println("nodemap: " + lines.nodeMap);
        /*
        if (in == '\n') {
            area.lines.insert(r, new Line());
        } else if (in == 8) {
            //
        } else if (in != 0) {
            lines.getLine(r).insert(c, new Glyph(in));
        } else return;
        updateLang();
        */
    }

    private void updateLang() {
        setNode("root", lang.text2match(area.lines.asString()));
    }

    @Override
    public UINeoNode newUI(double width, double height) {
        return null; //ui;
    }
    public ca.nengo.ui.lib.world.piccolo.object.Window newUIWindow(double w, double h, boolean title, boolean minMax, boolean close) {
        //ca.nengo.ui.lib.world.piccolo.object.Window x= ((UINetwork)newUI(1,1)).getViewerWindow();
        UINetwork inviisbleIconUI = new DefaultUINetwork(this); //((UINetwork) newUI(1, 1));

        this.ui = inviisbleIconUI;

        viewer = inviisbleIconUI.newViewer(new Color(25,50,25), new Color(128,128,128), 0.1f);

        ca.nengo.ui.lib.world.piccolo.object.Window x = new ca.nengo.ui.lib.world.piccolo.object.Window(inviisbleIconUI, viewer, title, minMax, close);     x.setSize(w, h);
        ui.setWindow(x);

        return x;
    }


}



/**
 * horizontal print
 */
            /*
            public void set(int x, int y, CharSequence word) {
                for (int i = 0; i < word.length(); i++) {
                    char c = word.charAt(i);
                    set(x + i, y, c);
                }
            }
            */
    /*
            // set one char
            public Node set(int x, int y, char c) {
                if (c == ' ') {
                    remove(l);
                    return null;
                }

                Node existing = get(l);
                if (existing != null && existing instanceof Glyph && (((Glyph) existing).getChar() == c))
                    return existing;


                Node n;
                set(l, n = newChar(x, y, c));

                return n;
            }
    */
