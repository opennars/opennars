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

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TestCharMesh {


    public static void main(String[] args) {



        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CharMesh mesh = new CharMesh("grid", 60, 80);
                new NengrowPanel(mesh).newWindow(800, 600);


                mesh.set(0, 0, 'a');
                mesh.set(1, 0, 'b');
                mesh.set(2, 0, 'c');
                mesh.set(0, 1, "TEXT SYSTEM");

                System.out.println(mesh.nodes());
                System.out.println(mesh.ui.getBounds());

            }
        });

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
    }

    public static class CharMesh extends AbstractMapNetwork<Long, Node> implements UIBuilder {


        private double charWidth;
        private double charHeight;
        private DefaultUINetwork ui;


        public CharMesh(String name, double charWidth, double charHeight) {
            super(name);
            scaleChar(charWidth, charHeight);

        }


        public static long index(int x, int y) {
            return (((long) y) << 32) | (x & 0xffffffffL);
        }

        public static int yCoord(long l) {
            return (int) (l >> 32);
        }

        public static int xCoord(long l) {
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
            if ((c == ' ') || (c == 0)) {
                removeNode(l);
                return null;
            } else {
                Node n;
                setNode(l, n = newChar(x, y, c));
                return n;
            }
        }


        @Override
        public UINeoNode newUI(double width, double height) {
            if (ui == null) {
                ui = new DefaultUINetwork<>(this);
            }
            return ui;
        }


        private Node newChar(int x, int y, char c) {
            SmartChar n = new SmartChar(name() + x + ',' + y, c);
            updateBounds(x, y, n);
            return n;
        }


        private void updateBounds(int x, int y, SmartChar n) {
            //n.ui.setOffset(x * charWidth, y * charHeight);
            n.setBounds(0, 0, charWidth, charHeight);
            n.lockPosition(false);
            n.move(x * charWidth, y * charHeight);
            n.lockPosition(true);
        }

        @Override
        public Long name(Node node) {
            return null;
        }


    }
}
