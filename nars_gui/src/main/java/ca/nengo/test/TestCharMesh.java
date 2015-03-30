package ca.nengo.test;


import automenta.vivisect.Video;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.ui.NengrowPanel;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.handler.KeyboardHandler;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.ui.model.plot.MeshCursor;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.ScriptGenException;
import nars.gui.output.graph.nengo.DefaultUINetwork;
import org.piccolo2d.event.PInputEvent;

import java.awt.*;
import java.util.HashMap;

public class TestCharMesh {


    public static void main(String[] args) {



//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run(){

                NengrowPanel panel = new NengrowPanel();

                {
                    //Lang lang = new Lang();

                    CharMeshEdit mesh = new CharMeshEdit("grid", 60, 80);//, lang);
                    mesh.set(0, 3, "[TEXT_SYSTEM]");

                    panel.add(mesh);
                    //lang.update(mesh);

                }
                /*{
                    CharMeshEdit mesh = new CharMeshEdit("grid2", 60, 80);
                    mesh.set(0, 0, 'x');
                    mesh.set(1, 0, 'y');
                    mesh.set(2, 0, 'z');

                    panel.add(mesh);
                    Window w2 = mesh.newUIWindow(600, 400, true, false, true);
                    panel.add(w2);
                }*/

                panel.newWindow(800, 600);
//            }
//        });

    }

    public static class SmartChar extends AbstractWidget {

        public static final Stroke border = new BasicStroke(1);
        public static final Stroke noStroke = new BasicStroke(0);
        Color textcolor = Color.WHITE;
        Color borderColor = new Color(70,70,70);
        Color bgColor = new Color(40,40,40);
        Font f = Video.monofont.deriveFont(64f);
        private char c;
        private boolean lockPos;

        public SmartChar(String name, char c) {
            super(name);
            this.c = c;
        }

        public void lockPosition(boolean l) {
            this.lockPos = l;
        }

        @Override
        public boolean isDraggable() {
            return !lockPos;
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        protected void paint(PaintContext paintContext, double width, double height) {
            Graphics2D g = paintContext.getGraphics();


            //border and background
            final int iw = (int) width;
            final int ih = (int) height;
            if (bgColor != null || borderColor != null) {
                if (bgColor != null) {
                    //g.setStroke(noStroke);
                    g.setPaint(bgColor);
                    g.fillRect(0, 0, iw, ih);
                }
                if (borderColor != null) {
                    g.setStroke(border);
                    g.setPaint(borderColor);
                    g.drawRect(0, 0, iw, ih);
                }

            }

            //draw glyph
            if (textcolor != null) {
                g.setColor(textcolor);
                g.setFont(f);
                final int fs = f.getSize()/2;
                double x = width/2  - ui.getX()/2 - fs/2;
                double y = height / 2 - ui.getY() / 2 + fs/2;
                g.drawString(String.valueOf(c), (int) x, (int) y);
            }
        }



        public void setTextColor(Color textcolor) {
            this.textcolor = textcolor;
            ui.repaint();
        }

        public void setBgColor(Color bgColor) {
            this.bgColor = bgColor;
            ui.repaint();
        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {


        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return null;
        }


        public char getChar() {
            return c;
        }
    }

    public static class CharMeshEdit extends DefaultNetwork implements UIBuilder {

        private MeshCursor cursor;
        public final CharMesh mesh;
        private KeyboardHandler keyHandler;
        private UINetwork ui;
        private NodeViewer viewer;
        //private Lang lang;

        public CharMeshEdit(String name, double charWidth, double charHeight){//, Lang lang) {
            super(name);
            //this.lang = lang;
            this.mesh = new CharMesh(charWidth, charHeight) {

                @Override
                protected void remove(long l) {
                    Node x = CharMeshEdit.this.remove(l);

                    //TODO make this automatic as part of remove()
                    if (x instanceof AbstractWidget) {
                        ((AbstractWidget)x).ui.destroy();
                    }
                }

                @Override
                protected void set(long l, Node node) {

                    setNode(l,node);
                    /*if (ui!=null)
                        ui.repaint();
                    if (viewer!=null)
                        viewer.repaint();*/

                }

                @Override
                public Node get(long i) {
                    return getNode(i);
                }
            };

            cursor = new MeshCursor(name + '.' + "cursor", (int)charWidth/8, (int)charHeight, this);
            cursor(0, 3);
            updateCursor();

            try {
                addNode(cursor);
            } catch (StructuralException e) {
                e.printStackTrace();
            }

        }


        protected MeshCursor cursor(int x, int y) {
            cursor.set(x, y);
            updateCursor();
            return cursor;
        }
        protected void updateCursor() {
            mesh.updateBounds(cursor.getX(), cursor.getY(), cursor);
            cursor.ui.getPNode().raiseToTop();
        }

        public long index() {
            return mesh.index(cursor.getX(), cursor.getY());
        }

//        @Override
//        public void run(float startTime, float endTime, int stepsPerCycle) throws SimulationException {
//            super.run(startTime, endTime, stepsPerCycle);
//
//        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {
            enableInput();
            super.run(startTime, endTime);
        }

        protected void enableInput() {
            if ((keyHandler==null) && (viewer!=null)) {
                keyHandler = new KeyboardHandler() {

                    @Override
                    public void keyReleased(PInputEvent event) {
                        CharMeshEdit.this.keyReleased(event);
                    }

                    @Override
                    public void keyPressed(PInputEvent event) {
                        CharMeshEdit.this.keyPressed(event);
                    }
                };
                //ui.getPNode().getRoot().addInputEventListener(keyHandler);
                //ui.getViewer().getSky().addInputEventListener(keyHandler);
                viewer.getSky().addInputEventListener(keyHandler);
                //viewer.getSky().addInputEventListener(keyHandler);

            }
        }


        public void keyPressed(PInputEvent event) {
            char in = event.getKeyChar();

            String debug = String.valueOf((int) in) + " "+cursor + " ";
            System.out.println("event: " + event + "isActionKey: " + event.isActionKey() + "cursor: " + cursor);
            mesh.set(0, 0, debug);
            System.out.println("nodemap: "+nodeMap);

            int cx = cursor.getX();
            int cy = cursor.getY();

            if (in == '\n') {
                cursor(0, ++cy);
            }
            else if (in == 8){
                //goToSide(-1);
                mesh.set(--cx, cy, ' ');
                cursor(cx, cy);
            }
            else {
                if (in!=0) {
                    insert(in);
                    //lang.update(this);
                }
            }

        }

        public void keyReleased(PInputEvent event) {

        }

        public void insert(char c) {
            int cx = cursor.getX();
            int cy = cursor.getY();
            mesh.set(cx++, cy, c);
            cursor(cx, cy);
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


        public void set(int x, int y, char c) {
            mesh.set(x, y, c);
        }
        public void set(int x, int y, String s) {
            mesh.set(x, y, s);
        }

        public ca.nengo.ui.lib.world.piccolo.object.Window newUIWindow(double w, double h, boolean title, boolean minMax, boolean close) {
            //ca.nengo.ui.lib.world.piccolo.object.Window x= ((UINetwork)newUI(1,1)).getViewerWindow();
            UINetwork inviisbleIconUI = new DefaultUINetwork(this); //((UINetwork) newUI(1, 1));

            this.ui = inviisbleIconUI;

            viewer = inviisbleIconUI.newViewer(new Color(25,50,25), new Color(128,128,128), 0.1f);

            ca.nengo.ui.lib.world.piccolo.object.Window x = new Window(inviisbleIconUI, viewer, title, minMax, close);     x.setSize(w, h);
            ui.setWindow(x);

            return x;
        }
    }

    abstract public static class CharMesh  {

        private double charWidth;
        private double charHeight;


        public CharMesh(double charWidth, double charHeight){

            scaleChar(charWidth, charHeight);
        }

        public static long index(int x, int y) {
            return (((long) y) << 32) | (x & 0xffffffffL);
        }

        public static int y(long l) {
            return (int) (l >> 32);
        }

        public static int x(long l) {
            return (int) (l);
        }

        private void scaleChar(double charWidth, double charHeight) {
            this.charWidth = charWidth;
            this.charHeight = charHeight;

        }


        /** horizontal print */
        public void set(int x, int y, CharSequence word) {
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                set(x + i, y, c);
            }
        }

        public Node set(int x, int y, char c) {
            long l = index(x, y);

            if (c == ' ') {
                remove(l);
                return null;
            }

            Node existing = get(l);
            if (existing!=null && existing instanceof SmartChar && (((SmartChar)existing).getChar() == c) )
                return existing;


            Node n;
            set(l, n = newChar(x, y, c));

            return n;
        }

        protected abstract void remove(long l);
        protected abstract void set(long l, Node node);
        public abstract Node get(long i);



//        @Override
//        public UINeoNode newUI(double width, double height) {
//            if (ui == null) {
//                //ui = new DefaultUINetwork(this);
//            }
//            return ui;
//        }


        int charSerial = 0;
        private Node newChar(int x, int y, char c) {
            SmartChar n = new SmartChar(String.valueOf(charSerial++), c);
            updateBounds(x, y, n);
            return n;
        }


        private void updateBounds(int x, int y, AbstractWidget n) {
            n.setBounds(0, 0, charWidth, charHeight);
            n.move(charPosX(x), charPosY(y));
        }
        private void updateBounds(int x, int y, SmartChar n) {
            n.lockPosition(false);
            updateBounds(x, y, ((AbstractWidget) n));
            n.lockPosition(true);
        }

        private int charPosX(int x)
        {
            return x * (int)charWidth;
        }

        private int charPosY(int y)
        {
            return y * (int)charHeight;
        }

        public int findNonblank(int x, int y)
        {
            while (x >= 0){
                Node n = get(index(x, y));
                if(n != null)
                    return x;
                x-=1;
            }
            return 0;
        }

        public String asString(){
            StringBuilder result = new StringBuilder();

            for(int line = 3; line < 4; line++) {
                for (int column = 0; column < 333; column++) {
                    Node n = get(index(column, line));
                    if (n == null) break;
                    //System.out.println("" + n + " len: "+result.length());
                    result.append(((SmartChar) n).getChar());
                }
                //result.append("\n");
            }
            return result.toString();
        }
    }
}
