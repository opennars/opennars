package vnc;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.io.narsese.NarseseParser;
import nars.nal.Concept;
import nars.nal.Task;
import nars.nal.nal4.Product;
import nars.nal.nal8.NullOperator;
import nars.nal.nal8.Operation;
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

    final int resolutionLevels = 4;

    boolean narsCanType = true; //enable/disable for keyboard events from NARS

    boolean keyTypeOnly = true; //whether to only send keyTyped events and ignore the press/release timing (simpler)

    private final NAR nar;

    Map<Concept,ActivityRectangle> positions = new LinkedHashMap();
//    int rx = 600; //sky resolution pixels
//    int ry = 40;
    List<Concept> incoming = new CopyOnWriteArrayList();
    Deque<OCR.BufferUpdate> ocrResults = new ConcurrentLinkedDeque<>();
    private SkyActivity skyActivity;
    private boolean keyboardInputs = true;

    public VNCControl(NAR nar, String host, int port) {
        super(host, port);
        this.nar = nar;

        initNAR();

    }

    public static class ActivityRectangle extends Rectangle.Double {
        public float current = 0;
        public float prev = 0;
        public float prioritySum = 0;

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

    public static float[] get3x3Coordinates(Product s) {
        float wx = 1f;
        float wy = 1f;
        float cx = 0f;
        float cy = 0f;
        while (s!=null) {
            wx/=3;
            wy/=3;
            Product next = null;

            int dx = 0, dy = 0;

            String charX = s.term[0].toString();
            switch (charX) {
                case "L": dx = -1; break;
                case "R": dx = 1; break;
            }
            String charY = s.term[1].toString();
            switch (charY) {
                case "U": dy = -1; break;
                case "D": dy = 1; break;
            }
            if (s.term.length > 2 && s.term[2] instanceof Product) {
                next = (Product)s.term[2];
            }

            cx += wx * dx;
            cy += wy * dy;
            s = next;
        }
        return new float[] {  (cx  + 0.5f)-wx/2 , (cy + 0.5f)-wy/2, wx, wy };
    }


    static final Set<Term> seeds = new LinkedHashSet();
    static {
        NarseseParser n = NarseseParser.newParser();

        for (int scale = 1; scale<=3; scale++) {

            double div = Math.pow(3, scale);

            for (double i = 0; i < div; i++) {
                for (double j = 0; j < div; j++) {
                    seeds.add(n.parseTerm(OCR.get3x3CoordsTree(
                            i / div * 1.5,
                            j / div * 1.5,
                            1.0, 1.0, scale)));
                }
            }
        }
    }

    public class SkyActivity extends ConceptMap.SeededConceptMap {



        //final public PImage node = new PImage();

        public boolean pendingReset = true;

        public SkyActivity(NAR nar) {
            super(nar, VNCControl.this.seeds);
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
            float[] d = get3x3Coordinates(s);
            float cx = d[0];
            float cy = d[1];
            float wx = d[2];
            float wy = d[3];



            ActivityRectangle r = new ActivityRectangle(cx, cy, wx, wy);

            //System.out.println(c + " " + r);

            r.current = c.getPriority();
            positions.put(c, r);

        }
        @Override
        protected void onConceptForget(Concept c) {
            ActivityRectangle r = positions.remove(c);
            if (r!=null)
                r.current = -1;
            //renderSky();
        }

        @Override
        protected void onConceptNew(Concept c) {

            incoming.add(c);

            //SwingUtilities.invokeLater(this::render);
            //renderSky();

        }

        @Override
        public boolean contains(Concept c) {
            Term s = c.getTerm();
            return seeds.contains(s);
        }

        @Override
        protected void onCycle() {
            for (Map.Entry<Concept, VNCControl.ActivityRectangle> e : positions.entrySet()) {
                e.getValue().prioritySum += (e.getKey()).getPriority();
            }
        }

        @Override
        protected void onFrame() {

            if (getSurface() == null) return;
            else if (pendingReset) reset();

            renderSky(cycleInFrame);

        }

    };

    protected synchronized void renderSky(int cyclesSinceLast) {

        if (getSurface() == null) return;

        if (!incoming.isEmpty()) {
            Concept[] c = incoming.toArray(new Concept[incoming.size()]);
            incoming.clear();
            for (Concept x : c)
                skyActivity.register(x);
        }

        getSurface().renderSky(nar.time(), positions, ocrResults, cyclesSinceLast);
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
        nar.on(new NullOperator("^keyboard") {
            protected List<Task> execute(Operation operation, Term[] args, Memory memory) {


                if (!narsCanType || operation.getTask().isInput()) return null;

                if (args.length < 3) return null;

                int code = Integer.parseInt(args[0].toString());
                int modifiers = Integer.parseInt(args[1].toString());

                long now = System.currentTimeMillis();

                if (args.length == 3 ) {

                    //keytype
                    keyboardInputs = false;
                    inputKey(code, modifiers, KeyEvent.KEY_PRESSED);
                    inputKey(code, modifiers, KeyEvent.KEY_RELEASED);
                    keyboardInputs = true;

                    return null;
                }

                if (args.length==4) {

                    boolean pressed = args[2].toString().equals("ON");

                    keyboardInputs = false;
                    inputKey(code, modifiers, pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED);
                    keyboardInputs = true;
                }

                return null;
            }
        });
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
                String loc = OCR.get3x3CoordsTree(pe.x, pe.y, getSurface().getWidth(), getSurface().getHeight(), resolutionLevels);
                /*String ii = "<(*,B" + pe.buttonMask + ", " + loc  + ") --> ON>. :|: " +
                        (mousePressed ? "%1.00;0.90%" : "%0.00;0.90%");*/

                String ii = "mouse(B" + pe.buttonMask + ", " + loc  + ")! :|:";


//                {
//                    //Test accuracy of recursive representation
//                    Product t = (Product)nar.term(loc);
//                    float d[] = get3x3Coordinates(t);
//                    System.out.println(pe.x + "," + pe.y + " = " + loc + " = " +
//                            d[0] * getWidth() + "," + d[1] * getHeight());
//
//                }

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

                String location = u.getLocation(getSurface(), resolutionLevels);

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

    @Deprecated protected synchronized void keyEvent(KeyEvent e, boolean press) {
        /*String ii = "<{K" + e.getKeyCode() + "} --> ON>. :|: " +
                (press ? "%1.00;0.90%" : "%0.00;0.90%");*/
        //System.out.println(keyboardInputs + " " + keyTypeOnly + " " + e + " " +press);
        if (keyboardInputs) {
            String ii;
            String middle = (0 + e.getExtendedKeyCode()) + "," + e.getModifiersEx();
            if (keyTypeOnly) {
                if (!press) {
                    ii = "keyboard(" + middle + ")! :|: ";
                }
                else {
                    return;
                }
            }
            else {
                ii = "keyboard(" + middle + "," + (press ? "ON" : "OFF") + ")! :|: ";
            }
            nar.input(ii);
            System.out.println(nar.time() + ": " + ii);
        }
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
