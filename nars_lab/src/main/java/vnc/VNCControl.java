package vnc;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import javolution.util.FastMap;
import nars.build.Default;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.AbstractReaction;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal3.SetExt;
import nars.logic.nal8.NullOperator;
import org.piccolo2d.PLayer;
import org.piccolo2d.nodes.PImage;
import vnc.drawing.Renderer;
import vnc.rfb.client.ClientToServerMessage;
import vnc.rfb.client.PointerEventMessage;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


abstract public class VNCControl extends VNCClient {

    private final NAR nar;



    public VNCControl(NAR nar, String host, int port) {
        super(host, port);
        this.nar = nar;

        initNAR();

    }

    abstract public static class ConceptMap extends AbstractReaction {

        int frame = -1;

        public int frame() { return frame; }

        abstract public void reset();

        public ConceptMap(NAR nar) {
            super(nar, Events.ConceptNew.class, Events.ConceptForget.class, Events.FrameEnd.class, Events.ResetStart.class);
        }
        abstract protected void onFrame();


        abstract public boolean contains(Concept c);

        @Override
        public void event(Class event, Object[] args) {
            if (event == Events.FrameEnd.class) {
                frame++;
                onFrame();
            }
            if (event == Events.ResetStart.class) {
                frame = 0;
                reset();
            }
            else if (event == Events.ConceptNew.class) {
                Concept c = (Concept)args[0];
                if (contains(c))
                    onConceptNew(c);
            }
            else if (event == Events.ConceptForget.class) {
                Concept c = (Concept) args[0];
                if (contains(c))
                    onConceptForget(c);
            }

        }

        protected abstract void onConceptForget(Concept c);

        protected abstract void onConceptNew(Concept c);

    }

    public static class ActivityRectangle extends Rectangle.Double {
        public float current = 0;
        public float prev = 0;

        public ActivityRectangle(float x, float y, float wx, float wy) {
            super(x, y, wx, wy);
        }
    }

    public void initNAR() {

        nar.start(100,50);


        Set<String> seeds = new LinkedHashSet();
        //level 1
        for (double i = 0; i < 3; i++) {
            for (double j = 0; j < 3; j++) {
                seeds.add(OCR.get3x3CoordsTree(i/3.0, j/3.0, 1.0, 1.0, 1));
            }
        }
        //level 2
        for (double i = 0; i < 9; i++) {
            for (double j = 0; j < 9; j++) {
                seeds.add(OCR.get3x3CoordsTree(i / 9.0, j / 9.0, 1.0, 1.0, 2));
            }
        }

        System.out.println(seeds);


        ConceptMap c = new ConceptMap(nar) {

            Map<Concept,ActivityRectangle> positions = new FastMap().atomic();

            int rx = 100;
            int ry = 100;

            BufferedImage bi = new BufferedImage(rx,ry,BufferedImage.TYPE_4BYTE_ABGR);

            final public PImage n = new PImage(bi);

            public boolean pendingReset = true;
            //final ColorArray ca = new ColorArray(50, new Color(0.5f,0,0,0), new Color(0.75f, 0, 0.25f, 0.5f));
            //int framesPerConceptRefresh = 100;


            protected void render() {

                Graphics2D g = (Graphics2D) bi.getGraphics();

                for (Map.Entry<Concept, ActivityRectangle> e : positions.entrySet()) {
                    Concept c = e.getKey();
                    ActivityRectangle r = e.getValue();
                    float priority = c.getPriority();
                    float dp = r.prev - priority;
                    float ap = Math.abs(dp) * 4; if (ap > 1f) ap = 1f;
                    float opacity = 0.1f;

                    Color color = new Color(1f,
                            dp > 0 ? 1f-ap : 1f,
                            dp < 0 ? 1f-ap : 1f,
                            opacity
                    );

                    float ww = g.getDeviceConfiguration().getBounds().width;
                    float hh = g.getDeviceConfiguration().getBounds().height;

                    //System.out.println(c + " " + r.x + " " + r.y + " " + r.width + " " + r.height + " : " + ww + " " + hh);

                    float cellScale = 0.9f;

                    g.setPaint(color);
                    g.fillRect( (int)((r.getX()+0.5)*ww),
                            (int)((r.getY()+0.5)*hh),
                            (int)(r.getWidth()*ww*cellScale),
                            (int)(r.getHeight()*hh*cellScale));

                    r.prev = priority;
                }

                n.setPaintInvalid(true);
                n.setImage(bi);

                getSurface().repaint();
            }


            @Override
            public void reset() {

                pendingReset = false;

                if (getSurface()==null) return;
                PLayer sky = VNCControl.this.getSurface().getSkyLayer();

                if (sky == null) return;


                getSurface().getSky().getCamera().setBounds(0,0,rx,ry);

                //if (n!=null) sky.removeChild(n);

                sky.addChild(n);
            }

            @Override
            protected void onConceptForget(Concept c) {
                positions.remove(c);
            }

            @Override
            protected void onConceptNew(Concept c) {
                SetExt s = (SetExt)c.getTerm();
                float wx = 1f;
                float wy = 1f;
                float cx = 0f;
                float cy = 0f;
                while (s!=null) {
                    wx/=3;
                    wy/=3;
                    SetExt next = null;

                    int dx = 1, dy = 1;
                    for (Term t : s) {
                        if (t instanceof SetExt) next = (SetExt) t;
                        else if (t.getClass() == Term.class) {
                            switch(t.toString()) {
                                case "L": dx = 0; break;
                                case "R": dx = 2; break;
                                case "U": dy = 0; break;
                                case "D": dy = 2; break;
                            }
                        }
                    }
                    cx += dx * wx - wx/2;
                    cy += dy * wy - wy/2;
                    s = next;
                }
                ActivityRectangle r = new ActivityRectangle(cx-wx/2, cy-wy/2, wx, wy);
                r.current = c.getPriority();
                positions.put(c, r);

                render();

            }

            @Override
            public boolean contains(Concept c) {
                String s = c.getTerm().toString();
                return seeds.contains(s);
            }

            @Override
            protected void onFrame() {

                if (getSurface() == null) return;
                else if (pendingReset) reset();

                if (!positions.isEmpty()) {
                    render();
                }

            }

        };

        addAxioms();
        addOperators();

    }

    protected void addAxioms() {
        //vertical
        nar.input("<{D,c,U} --> VERTICAL>.");
        //horizontal
        nar.input("<{L,C,R} --> HORIZONTAL>.");
        //center
        nar.input("<{c,C} --> CENTER>.");

        nar.input("<<#x --> ON> ==> <#y --> SEE>>?");


    }
    private void addOperators() {
        nar.on(new NullOperator("^keyboard"));
        nar.on(new NullOperator("^mouse"));
        /*{

            @Override
            public Term function(Term[] x) {
                System.out.println("keyboard: " + Arrays.toString(x));
                return null;
            }
        });*/
    }

    public static void main(String[] args) {


        Parameters.DEBUG = true;

        NAR nar = new NAR(new Default(4000, 1, 3)) {


        };

        nar.param.setTiming(Memory.Timing.Real);
        nar.param.duration.set(50); //ms
        nar.param.noiseLevel.set(12);


        Video.themeInvert();
        NARSwing swing = new NARSwing(nar);

        swing.controls.setSpeed(-1);
        swing.controls.setFrameRate(4f);



        VNCControl vnc;
        NWindow w = new NWindow("VNC",
                vnc = new VNCControl(nar, "localhost",5901) {
            @Override public String getParameter(String p) {
                return null;
            }
        }).show(1024,768,true);





    }


    @Override
    protected void videoUpdate(Renderer image, FramebufferUpdateRectangle rect) {
        super.videoUpdate(image, rect);

        OCR.queue(image, rect, ocrHandler, nar.time());

    }

    boolean mousePressed = false;

    @Override
    protected void onMessageSend(ClientToServerMessage m) {
        super.onMessageSend(m);

        if (m instanceof PointerEventMessage) {
            PointerEventMessage pe = (PointerEventMessage)m;
            boolean input = false;
            if ((pe.buttonMask > 0) && (!mousePressed)) {
                input = true;
                mousePressed = true;
            }
            else if (mousePressed && pe.buttonMask == 0) {
                mousePressed = false;
                input= true;
            }

            if (input) {
                String loc = OCR.get3x3CoordsTree(pe.x, pe.y, getSurface().getWidth(), getSurface().getHeight());
                String ii = "<(*,B" + pe.buttonMask + ", " + loc  + ") --> ON>. :|: " +
                        (mousePressed ? "%1.00;0.90%" : "%0.00;0.90%");

                nar.input(
                        ii
                );
                System.out.println(nar.time() + ": "  +ii);
            }
        }
    }

    private OCR.OCRResultHandler ocrHandler = new OCR.OCRResultHandler() {
        final int MAX_TEXT_LEN = 1024;
        final int MIN_WORD_LENGTH = 1;

        @Override
        public void next(OCR.BufferUpdate u) {
            if (!u.isProcessed()) return;

            String text = u.getText();
            if (text!=null && !text.isEmpty() && text.length() < MAX_TEXT_LEN) {

                //TODO tokenize better
                String[] t2 = text.split("[^a-zA-Z0-9]+");

                //u.getLocation()
                String ii = "";

                String location = u.getLocation(getSurface());

                float pri = 0.4f + 0.3f * (float)Math.sqrt(u.getScreenFraction(getSurface()));

                List<String> words = new ArrayList();
                for (String s : t2) {
                    s = s.trim().toLowerCase();
                    if (s.length() < MIN_WORD_LENGTH)
                        continue;


                    //String ii = "$" + Texts.n2(pri) + "$ <\"" + s + "\" --> SEE>. :|:";


                    words.add("{\"" + s + "\"}");
                }
                ii = ii + "(*," + location + ",";
                if (words.size() == 0) {
                    return;
                }
                else if (words.size() == 1) {
                    ii += words.get(0);
                }
                else {
                    ii = ii + "(&/," + String.join(",", words) + ")";
                }
                //TODO use creationTime of when the image was recieved, not processed

                ii = "<" + ii + ") --> SEE>";
                //ii = "$" + Texts.n2(pri) + "$ " + ii;
                //nar.input(ii + "\n");

                float ocrConf = u.getConfidence();

                Task t = nar.believe(ii, u.getInputTime(), 1.0f, ocrConf, pri);

                System.out.print(nar.time() + ": " + Texts.n2(pri) + "," + Texts.n2(ocrConf) + " " + ii + "\t" + u.getWaitingTime() + " wait, " + u.getProcessingTime() + " proc ms\n");
            }
        }
    };

    @Deprecated protected void keyEvent(KeyEvent e, boolean press) {
        String ii = "<{K" + e.getKeyCode() + "} --> ON>. :|: " +
                (press ? "%1.00;0.90%" : "%0.00;0.90%");
        nar.input(ii);
        System.out.println(nar.time() + ": " + ii);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyEvent(e, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyEvent(e, false);
    }

}
