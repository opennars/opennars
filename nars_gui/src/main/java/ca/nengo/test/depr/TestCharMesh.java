package ca.nengo.test.depr;


import automenta.vivisect.Video;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.ui.NengrowPanel;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import nars.gui.output.graph.nengo.DefaultUINetwork;

import java.awt.*;
import java.util.HashMap;

public class TestCharMesh  {


    public static class SmartChar extends AbstractWidget {

        private char c;
        Color textcolor = Color.WHITE;
        Color borderColor = Color.DARK_GRAY;
        Color bgColor = Color.BLUE;
        Font f = Video.monofont.deriveFont(16f);

        public static final Stroke border = new BasicStroke(1);
        public static final Stroke noStroke = new BasicStroke(0);

        public SmartChar(String name, char c) {
            super(name);
            this.c = c;
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        protected void paint(PaintContext paintContext, double width, double height) {
            Graphics2D g = paintContext.getGraphics();



            //border and background
            final int iw = (int)width;
            final int ih = (int)height;
            if (bgColor!=null || borderColor!=null) {
                if (bgColor!=null) {
                    //g.setStroke(noStroke);
                    g.setPaint(bgColor);
                    g.fillRect(0,0, iw, ih);
                }
                if (borderColor!=null) {
                    g.setStroke(border);
                    g.setPaint(borderColor);
                    g.drawRect(0,0,iw,ih);
                }

            }

            //draw glyph
            if (textcolor!=null) {
                g.setColor(textcolor);
                g.setFont(f);
                double x = ui.getX();
                double y = height / 2 - ui.getY() / 2;
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
    }

    public static class CharMesh extends AbstractMapNetwork<Long,Node> implements UIBuilder {


        private double charWidth;
        private double charHeight;
        private DefaultUINetwork ui;

        public static long index(int x, int y) {
            return (((long) y) << 32) | (x & 0xffffffffL);
        }
        public static int yCoord(long l) {
            return (int) (l >> 32);
        }
        public static int xCoord(long l) {
            return (int) (l);
        }


        public CharMesh(String name, double charWidth, double charHeight) {
            super(name);
            scaleChar(charWidth, charHeight);
            set(0,0,'?');
        }

        private void scaleChar(double charWidth, double charHeight) {
            this.charWidth = charWidth;
            this.charHeight = charHeight;

        }

        public Node set(int x, int y, char c) {
            long l = index(x, y);
            if ((c == ' ') || (c == 0)) {
                remove(l);
            }
            else {
                Node n;
                if (add(l, n = newChar(x, y, c))) {
                    return n;
                }
            }
            return null;
        }


        @Override
        public UINeoNode newUI(double width, double height) {
            return new DefaultUINetwork<>(this);
        }


        private Node newChar(int x, int y, char c) {
            SmartChar n = new SmartChar(name() + x + ',' + y, c);
            updateBounds(x, y, n);
            return n;
        }


        private void updateBounds(int x, int y, SmartChar n) {
            //n.ui.setOffset(x * charWidth, y * charHeight);
            n.setBounds(0, 0, charWidth, charHeight);
            n.move(x * charWidth, y * charHeight);
            n.ui.repaint();
        }

        @Override
        public Long name(Node node) {
            return null;
        }



    }

    public static void main(String[] args) {

        CharMesh mesh = new CharMesh("grid", 20,40);
        mesh.set(0, 0, 'a');
        mesh.set(1, 0, 'b');
        mesh.set(2, 0, 'c');
        new NengrowPanel(mesh).newWindow(800, 600);
    }
}
