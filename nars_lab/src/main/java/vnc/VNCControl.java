package vnc;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.io.narsese.NarseseParser;
import nars.nal.Concept;
import nars.nal.Task;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal8.NullOperator;
import nars.nal.term.Term;
import nars.prototype.Default;
import vnc.drawing.Renderer;
import vnc.rfb.client.ClientToServerMessage;
import vnc.rfb.client.PointerEventMessage;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;


abstract public class VNCControl extends VNCClient {

    private final NAR nar;

    Map<Concept,ActivityRectangle> positions = new LinkedHashMap();
//    int rx = 600; //sky resolution pixels
//    int ry = 40;
    List<Concept> incoming = new CopyOnWriteArrayList();
    Deque<OCR.BufferUpdate> ocrResults = new ConcurrentLinkedDeque<>();
    private SkyActivity skyActivity;

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

        nar.start(10,20);

        skyActivity = new SkyActivity(nar);


        //System.out.println(seeds);




        addAxioms();
        addOperators();

    }


    static final Set<Term> seeds = new LinkedHashSet();
    static {
        NarseseParser n = NarseseParser.newParser((Memory)null);

        //level 1
        for (double i = 0; i < 3; i++) {
            for (double j = 0; j < 3; j++) {
                seeds.add(n.parseTerm(OCR.get3x3CoordsTree(i / 3.0 + i/6.0f, j / 3.0 + j/6.0f, 1.0, 1.0, 1)));
            }
        }
        //level 2

        for (double i = 0; i < 9; i++) {
            for (double j = 0; j < 9; j++) {
                seeds.add(n.parseTerm(OCR.get3x3CoordsTree(i / 9.0 + i/18.0, j / 9.0 + i/18.0, 1.0, 1.0, 2)));
            }
        }
    }

    public class SkyActivity extends ConceptMap {



        //final public PImage node = new PImage();

        public boolean pendingReset = true;

        public SkyActivity(NAR nar) {
            super(nar);
        }

        //final ColorArray ca = new ColorArray(50, new Color(0.5f,0,0,0), new Color(0.75f, 0, 0.25f, 0.5f));
        //int framesPerConceptRefresh = 100;




        @Override
        public void reset() {

            pendingReset = false;

            if (getSurface()==null) return;

        }

        public void register(Concept c) {
            Product s = (Product)c.getTerm();
            float wx = 1f;
            float wy = 1f;
            float cx = 0f;
            float cy = 0f;
            while (s!=null) {
                wx/=3;
                wy/=3;
                Product next = null;

                int dx = 0, dy = 0;
                for (Term t : s) {
                    if (t instanceof Product) next = (Product) t;
                    else if (t.getClass() == Term.class) {
                        switch(t.toString()) {
                            case "L": dx = -1; break;
                            case "R": dx = 1; break;
                            case "U": dy = 1; break;
                            case "D": dy = -1; break;
                        }
                    }
                }
                cx += wx * dx;
                cy += wy * dy;
                s = next;
            }


            ActivityRectangle r = new ActivityRectangle(cx + 1/3.0f, cy + 1/3.0f, wx, wy);

            //System.out.println(c + " " + r);

            r.current = c.getPriority();
            positions.put(c, r);

        }
        @Override
        protected void onConceptForget(Concept c) {
            ActivityRectangle r = positions.remove(c);
            if (r!=null)
                r.current = -1;
            renderSky();
        }

        @Override
        protected void onConceptNew(Concept c) {

            incoming.add(c);

            //SwingUtilities.invokeLater(this::render);
            renderSky();

        }

        @Override
        public boolean contains(Concept c) {
            Term s = c.getTerm();
            return seeds.contains(s);
        }

        @Override
        protected void onFrame() {

            if (getSurface() == null) return;
            else if (pendingReset) reset();

            renderSky();

        }

    };

    protected synchronized void renderSky() {

        if (getSurface() == null) return;

        if (!incoming.isEmpty()) {
            Concept[] c = incoming.toArray(new Concept[incoming.size()]);
            incoming.clear();
            for (Concept x : c)
                skyActivity.register(x);
        }

        getSurface().renderSky(nar.time(), positions, ocrResults);
        repaint();
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


        Global.DEBUG = true;

        NAR nar = new NAR(new Default(4000, 1, 3)) {


        };

        nar.param.setTiming(Memory.Timing.RealMS);
        nar.param.duration.set(50); //ms
        nar.param.outputVolume.set(12);


        Video.themeInvert();
        NARSwing swing = new NARSwing(nar);

        swing.setSpeed(-1);
        swing.setFrameRate(4f);



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
                String loc = OCR.get3x3CoordsTree(pe.x, pe.y, getSurface().getWidth(), getSurface().getHeight(),3);
                /*String ii = "<(*,B" + pe.buttonMask + ", " + loc  + ") --> ON>. :|: " +
                        (mousePressed ? "%1.00;0.90%" : "%0.00;0.90%");*/

                String ii = "mouse(B" + pe.buttonMask + ", " + loc  + ")! :|: ";


                nar.input(
                        ii
                );
                System.out.println(nar.time() + ": " + ii);
            }
        }
    }

    private OCR.OCRResultHandler ocrHandler = new OCR.OCRResultHandler() {
        final int MAX_TEXT_LEN = 1024;
        final int MIN_WORD_LENGTH = 1;

        @Override
        public void next(OCR.BufferUpdate u) {
            if (!u.isProcessed()) return;
            ocrResults.push(u);

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

                try {
                    Task t = nar.believe(ii, u.getInputTime(), 1.0f, ocrConf, pri);
                }
                catch (Throwable x) {
                    System.err.println(ii + " -> " + x);
                    x.printStackTrace();
                }

                System.out.print(nar.time() + ": " + Texts.n2(pri) + "," + Texts.n2(ocrConf) + " " + ii + "\t" + u.getWaitingTime() + " wait, " + u.getProcessingTime() + " proc ms\n");
            }
        }
    };

    @Deprecated protected void keyEvent(KeyEvent e, boolean press) {
        /*String ii = "<{K" + e.getKeyCode() + "} --> ON>. :|: " +
                (press ? "%1.00;0.90%" : "%0.00;0.90%");*/
        String ii = "keyboard(K" + e.getKeyCode() + "," + (press ? "ON":"OFF") + ")! :|: ";
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
