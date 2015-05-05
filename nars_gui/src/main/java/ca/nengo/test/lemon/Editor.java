//package ca.nengo.test.lemon;
//
//import automenta.vivisect.Video;
//import automenta.vivisect.swing.ColorArray;
//import ca.nengo.model.Node;
//import ca.nengo.model.SimulationException;
//import ca.nengo.model.StructuralException;
//import ca.nengo.model.impl.AbstractMapNetwork;
//import ca.nengo.model.impl.DefaultNetwork;
//import ca.nengo.ui.lib.world.handler.KeyboardHandler;
//import ca.nengo.ui.lib.world.piccolo.object.*;
//import ca.nengo.ui.model.UIBuilder;
//import ca.nengo.ui.model.UINeoNode;
//import ca.nengo.ui.model.node.UINetwork;
//import ca.nengo.ui.model.plot.AbstractWidget;
//import ca.nengo.ui.model.viewer.NodeViewer;
//import ca.nengo.util.ScriptGenException;
//import nars.gui.output.graph.nengo.DefaultUINetwork;
//import org.piccolo2d.event.PInputEvent;
//import org.piccolo2d.util.PBounds;
//
//import java.awt.*;
//import java.awt.Window;
//import java.awt.event.KeyEvent;
//import java.awt.geom.Rectangle2D;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//
//public class Editor extends DefaultNetwork implements UIBuilder {
//    public class TextArea extends AbstractWidget {
//        public class Glyph {
//
//            public final Stroke border = new BasicStroke(1);
//            public final Stroke noStroke = new BasicStroke(0);
//            Color textcolor = Color.WHITE;
//            Color borderColor = new Color(70, 70, 70);
//            Color bgColor = new Color(40, 40, 40);
//            Font f = Video.monofont.deriveFont(64f);
//            public char c;
//            PBounds bounds;
//
//            public Glyph(char c) {
//                this.c = c;
//            }
//
//            protected void paint(Graphics2D g) {
//                //System.out.println("Glyph.paint " + bounds);
//                //border and background
//                final int iw = (int) bounds.width;
//                final int ih = (int) bounds.height;
//                int ix = (int) bounds.x;
//                int iy = (int) bounds.y;
//                if (bgColor != null || borderColor != null) {
//                    if (bgColor != null) {
//                        //g.setStroke(noStroke);
//                        //g.setPaint(bgColor);
//                        //g.fillRect(ix, iy, iw, ih);
//                    }
//                    if (borderColor != null) {
//                        g.setStroke(border);
//                        g.setPaint(borderColor);
//                        g.drawRect(ix, iy, iw, ih);
//                    }
//
//                }
//
//                //draw glyph
//                if (textcolor != null) {
//                    g.setColor(textcolor);
//                    g.setFont(f);
//                    Rectangle2D mb = f.getMaxCharBounds(g.getFontRenderContext());
//                    ix += mb.getWidth() / 2;
//                    iy += mb.getHeight();
//                    g.drawString(String.valueOf((char) c), (int) ix, (int) iy);
//                }
//            }
//
//
//            public void setTextColor(Color textcolor) {
//                this.textcolor = textcolor;
//            }
//
//            public void setBgColor(Color bgColor) {
//                this.bgColor = bgColor;
//            }
//
//            public char getChar() {
//                return c;
//            }
//        }
//
//        public class Line extends ArrayList<Glyph> {
//            public Line() {
//            }
//
//            public Line(String s) {
//                for (char c : s.toCharArray())
//                    add(new Glyph(c));
//            }
//
//            protected void paint(Graphics2D g) {
//                Iterator<Glyph> it = iterator();
//                while (it.hasNext()) {
//                    it.next().paint(g);
//                }
//            }
//
//            public String asString() {
//                StringBuilder text = new StringBuilder();
//                Iterator<Glyph> it = iterator();
//                while (it.hasNext()) {
//                    Glyph g = it.next();
//                    text.append((char) g.c);
//                }
//                return text.toString();
//            }
//
//
//        }
//
//        public class Lines extends ArrayList<Line> {
//            public Lines() {
//                super();
//            }
//
//            public void setLine(int r, String str) {
//
//                while (size() < r)
//                    add(new Line(""));
//                if (size() == r)
//                    add(new Line(str));
//                else
//                    set(r, new Line(str));
//                update();
//            }
//
//            public void layOut() {
//                for (int l = 0; l < size(); l++){
//                    Line line = get(l);
//                    for (int g = 0; g < line.size(); g++){
//                        line.get(g).bounds = makeBounds(g, l);
//                    }
//                }
//            }
//
//            private PBounds makeBounds(int x, int y) {
//                return new PBounds(x * charWidth, y * charHeight, charWidth, charHeight);
//            }
//        }
//
//        public Lines lines = new Lines();
//        public ArrayList<Glyph> text2glyphMap = new ArrayList<Glyph>();
//        public String text;
//        public Lang.Match root;
//        Color astColor = Color.BLUE;
//
//        public void cacheStuff() {
//            text2glyphMap.clear();
//            StringBuilder text = new StringBuilder();
//            Iterator<Line> lines_it = lines.iterator();
//            while (lines_it.hasNext()) {
//                Line line = lines_it.next();
//                Iterator<Glyph> it = line.iterator();
//                while (it.hasNext()) {
//                    Glyph g = it.next();
//                    text.append((char)g.c);
//                    text2glyphMap.add(g);
//                }
//                text.append("\n");
//                text2glyphMap.add(null);
//            }
//            this.text = text.toString();
//        }
//
//
//        public TextArea(String name){
//            super(name);
//            //lines.setLine(0, "[\"Hi, are you a bridge?\"]?");
//            //lines.setLine(0, "<[TEXT_SYSTEM] --> [almost_operational]>.");
//        }
//
//        @Override
//        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
//            return null;
//        }//use default?
//
//        @Override
//        public void run(float startTime, float endTime) throws SimulationException {
//
//        }
//
//        @Override
//        protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double ww, double hh) {
//            //System.out.println("paintContext = [" + paintContext + "], ww = [" + ww + "], hh = [" + hh + "]");
//            Graphics2D g = paintContext.getGraphics();
//            paintMatches(g);
//            Iterator<Line> it = lines.iterator();
//            while (it.hasNext()) {
//                it.next().paint(g);
//            }
//
//        }
//
//        public void grow(PBounds bounds){
//            bounds.x -= margin;
//            bounds.y -= margin;
//            bounds.width += margin * 2;
//            bounds.height += margin * 2;
//        }
//
//        private void updateMatchesBounds(){
//            //if (root == null) return;//this shouldnt happen anymore
//            root.accept(new Lang.MatchVisitor() {
//                @Override
//                public void visit(Lang.Match m) {
//                    m.bounds = new PBounds();
//                    for (int i = m.start; i < m.end; i++) {
//                        Glyph g = text2glyphMap.get(i);
//                        if (g != null)
//                            m.bounds.add(g.bounds);
//                    }
//                    if (m instanceof Lang.MatchWithChildren)
//                        for (Lang.Match i : ((Lang.MatchWithChildren)m).items) {
//                            m.bounds.add(i.bounds);
//                        }
//                    grow(m.bounds);
//                }
//            }, true);
//        }
//
//        private void paintMatches(Graphics2D g){
//            //if (root == null) return;
//            root.accept(new Lang.MatchVisitor() {
//                @Override
//                public void visit(Lang.Match m) {
//                    g.setPaint(Color.getHSBColor(m.level / 5.55f, 1, 0.3f));
//                    g.fillRoundRect((int) m.bounds.x, (int) m.bounds.y, (int) m.bounds.width, (int) m.bounds.height, 10, 10);
//                }
//            }, false);
//        }
//
//        private void update() {
//            area.lines.layOut();
//            cacheStuff();
//            root = lang.text2match(text);
//            updateMatchesBounds();
//            if (cursor.c < 0) cursor.c = 0;
//            if (cursor.r < 0) cursor.r = 0;
//            cursor.updateBounds(area.lines.makeBounds(cursor.c, cursor.r));
//        }
//
//        public void keyPressed(PInputEvent event) {
//            int c = event.getKeyCode();
//            String debug = "//getKeyCode:" + c +
//                    ", event: " + event + ", isActionKey: " + event.isActionKey() + "";
//            lines.setLine(5, debug);
//            System.out.println(debug);
//            switch (c) {
//                case KeyEvent.VK_UP:
//                    cursor.r -= 1;
//                    break;
//                case KeyEvent.VK_DOWN:
//                    cursor.r += 1;
//                    break;
//                case KeyEvent.VK_LEFT:
//                    cursor.c -= 1;
//                    break;
//                case KeyEvent.VK_RIGHT:
//                    cursor.c += 1;
//                    break;
//            }
//            update();
//
//        }
//        public void keyTyped(PInputEvent event) {
//            char in = event.getKeyChar();
//            //event.getKeyCode()
//            String debug = "//getKeyChar: " + String.valueOf((int) in) +
//                    ", event: " + event + ", isActionKey: " + event.isActionKey() + "";
//            lines.setLine(5, debug);
//            System.out.println(debug);
//            if (in == '\n') {
//                lines.add(cursor.r++, new Line());
//                cursor.c = 0;
//            } else if (in == 8) {
//                if (cursor.c > 0 && lines.size() > cursor.r && lines.get(cursor.r).size() >= cursor.c)
//                    lines.get(cursor.r).remove(--cursor.c);
//            } else if (in != 0) {
//                lines.get(cursor.r).add(cursor.c++, new Glyph(in));
//            } else return;
//            update();
//        }
//
//    }
//
//    final double margin = 5;
//    TextArea area;
//    int charWidth;
//    int charHeight;
//    public NodeViewer viewer;
//    private UINetwork ui;
//    public Lang lang;
//    private KeyboardHandler keyHandler;
//    Cursor cursor;
//
//    public Editor(String name, int charWidth, int charHeight, Lang lang) {
//        super(name);
//        this.lang = lang;
//        setCharSize(charWidth, charHeight);
//        cursor = new Cursor("cursor1");
//        area = new TextArea("area");
//        setNode("area", area);
//        setNode("cursor", cursor);
//        area.lines.setLine(0, "[TEXT_SYSTEM].");
//    }
//
//    private void setCharSize(int charWidth, int charHeight) {
//        this.charWidth = charWidth;
//        this.charHeight = charHeight;
//    }
//
//    @Override
//    public void run(float startTime, float endTime) throws SimulationException {
//        enableInput();
//    }
//
//    protected void enableInput() {
//        if ((keyHandler == null) && (viewer != null)) {
//            keyHandler = new KeyboardHandler() {
//
//                @Override
//                public void keyTyped(final PInputEvent event) {
//                    area.keyTyped(event);
//                }
//
//
//                @Override
//                public void keyReleased(PInputEvent event) {
//                    //editor.keyReleased(event);
//                }
//
//                @Override
//                public void keyPressed(PInputEvent event) {
//                    area.keyPressed(event);
//
//
//                }
//            };
//            //ui.getPNode().getRoot().addInputEventListener(keyHandler);
//            //ui.getViewer().getSky().addInputEventListener(keyHandler);
//            viewer.getSky().addInputEventListener(keyHandler);
//            //viewer.getSky().addInputEventListener(keyHandler);
//
//        }
//    }
//
//    @Override
//    public UINeoNode newUI(double width, double height) {
//        return null; //ui;
//    }
//
//    public ca.nengo.ui.lib.world.piccolo.object.Window newUIWindow(double w, double h, boolean title, boolean minMax, boolean close) {
//        //ca.nengo.ui.lib.world.piccolo.object.Window x= ((UINetwork)newUI(1,1)).getViewerWindow();
//        UINetwork inviisbleIconUI = new DefaultUINetwork(this); //((UINetwork) newUI(1, 1));
//
//        this.ui = inviisbleIconUI;
//
//        viewer = inviisbleIconUI.newViewer(new Color(25,50,25), new Color(128,128,128), 0.1f);
//
//        ca.nengo.ui.lib.world.piccolo.object.Window x = new ca.nengo.ui.lib.world.piccolo.object.Window(inviisbleIconUI, viewer, title, minMax, close);     x.setSize(w, h);
//        ui.setWindow(x);
//
//        return x;
//    }
//
//
//}
