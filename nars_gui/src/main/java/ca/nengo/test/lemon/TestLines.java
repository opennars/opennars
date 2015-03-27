package ca.nengo.test.lemon;


import automenta.vivisect.Video;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.ui.NengrowPanel;
import ca.nengo.ui.lib.world.handler.KeyboardHandler;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.ScriptGenException;
import nars.gui.output.graph.nengo.DefaultUINetwork;
import org.piccolo2d.event.PInputEvent;

import java.awt.*;
import java.util.HashMap;

public class TestLines {

    public static void main(String[] args) {
        NengrowPanel panel = new NengrowPanel();
        {
            Lang lang = new Lang();
            Lines lines = new Lines("lines", 60, 80, lang);
            lines.setLine(3, "<[TEXT_SYSTEM] --> [operational]>.");
            panel.add(lines);
            //lang.update(mesh);
        }
        panel.newWindow(800, 600);
    }


    public static class Lines extends DefaultNetwork implements UIBuilder {


        public static class Line extends DefaultNetwork implements UIBuilder {
            public Line() {
            }

            public Line(String s) {
                int i = 0;
                for (char c:s.toCharArray())
                    setNode(i++, new Glyph(c));

            }

            public String asString() {
                StringBuilder result = new StringBuilder();
                for(Object g: nodeMap.values()){
                    result.append(((Glyph)g).c);
                }
                return result.toString();
            }

            @Override
            public UINeoNode newUI(double width, double height) {
                return null; //ui;
            }

            public void insert(int x, Glyph g) {

            }

        }


        private Cursor cursor;
        private KeyboardHandler keyHandler;
        private UINetwork ui;
        private NodeViewer viewer;
        private Lang lang;
        private Lang.Match root;
        private double charWidth;
        private double charHeight;

        public Lines(String name, double charWidth, double charHeight, Lang lang) {
            super(name);
            setCharSize(charWidth, charHeight);
            this.lang = lang;
            cursor = new Cursor(name + '.' + "cursor", this);
            updateCursorBounds();
            try {
                addNode(cursor);
            } catch (StructuralException e) {
                e.printStackTrace();
            }
            cursor.move(0, 2);
        }

        public void moveCursor(int c, int r) {
            cursor.move(c, r);
            updateCursorBounds();
        }

        public void updateCursorBounds() {
            updateBounds(cursor.c, cursor.r, cursor);
            cursor.ui.getPNode().raiseToTop();
        }


        public void keyPressed(PInputEvent event) {
            char in = event.getKeyChar();

            String debug = "//getKeyChar:" + String.valueOf((int) in) + ",cursor: " + cursor + "\n" +
                    "event: " + event + "isActionKey: " + event.isActionKey() + "";
            setLine(10, debug);
            System.out.println(debug);
            System.out.println("nodemap: " + nodeMap);

            if (in == '\n') {
                newLine();
            } else if (in == 8) {
                //
            } else if (in != 0) {
                insert(in);
            } else return;
            updateLang();
        }

        private void updateLang() {
            root = lang.text2match(asString());
        }

        private void newLine() {

        }

        public void insert(int c) {
            getLine(cursor.r).insert(cursor.c++, new Glyph(c));
            updateCursorBounds();
        }

        public Line getLine(int r) {
            Line l = (Line) getNode(r);
            if (l == null)
                l = new Line();
            return l;
        }

        public void setLine(int r, String str) {
            setNode(r, new Line(str));
        }


        public void keyReleased(PInputEvent event) {

        }

        private void setCharSize(double charWidth, double charHeight) {
            this.charWidth = charWidth;
            this.charHeight = charHeight;
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
        public String asString() {
            StringBuilder result = new StringBuilder();
            for(Object l: nodeMap.values()){
                result.append(((Line)l).asString() + "\n");
            }
            return result.toString();
        }


        private void updateBounds(int x, int y, AbstractWidget n) {
            n.setBounds(0, 0, charWidth, charHeight);
            n.move(charPosX(x), charPosY(y));
        }

        private void updateBounds(int x, int y, Glyph n) {
            n.lockPosition(false);
            updateBounds(x, y, ((AbstractWidget) n));
            n.lockPosition(true);
        }

        private int charPosX(int x) {
            return x * (int) charWidth;
        }

        private int charPosY(int y) {
            return y * (int) charHeight;
        }


        @Override
        public void run(float startTime, float endTime) throws SimulationException {
            enableInput();
            super.run(startTime, endTime);
        }

        protected void enableInput() {
            if ((keyHandler == null) && (viewer != null)) {
                keyHandler = new KeyboardHandler() {

                    @Override
                    public void keyReleased(PInputEvent event) {
                        keyReleased(event);
                    }

                    @Override
                    public void keyPressed(PInputEvent event) {
                        keyPressed(event);
                    }
                };
                //ui.getPNode().getRoot().addInputEventListener(keyHandler);
                //ui.getViewer().getSky().addInputEventListener(keyHandler);
                viewer.getSky().addInputEventListener(keyHandler);
                //viewer.getSky().addInputEventListener(keyHandler);

            }
        }


        @Override
        public UINeoNode newUI(double width, double height) {
            //            if (ui == null) {
            //                ui = new DefaultUINetwork(this);
            //                /*ui = new ca.nengo.ui.lib.world.piccolo.object.Window(
            //                        new DefaultUINetwork(this).,
            //                        ui.createViewerInstance());*/
            //            }
            return null; //ui;

            //return newUIWindow(400, 400, true, false, true);
        }


        public Window newUIWindow(double w, double h, boolean title, boolean minMax, boolean close) {
            //ca.nengo.ui.lib.world.piccolo.object.Window x= ((UINetwork)newUI(1,1)).getViewerWindow();
            UINetwork inviisbleIconUI = new DefaultUINetwork(this); //((UINetwork) newUI(1, 1));

            this.ui = inviisbleIconUI;

            viewer = inviisbleIconUI.newViewer(new Color(25, 50, 25), new Color(128, 128, 128), 0.1f);

            Window x = new Window(inviisbleIconUI, viewer, title, minMax, close);
            x.setSize(w, h);
            ui.setWindow(x);

            return x;
        }
    }
}