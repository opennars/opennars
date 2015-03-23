package vnc;

import automenta.vivisect.Video;
import automenta.vivisect.swing.ColorArray;
import automenta.vivisect.swing.NWindow;
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
import nars.logic.nal8.NullOperator;
import org.piccolo2d.PLayer;
import org.piccolo2d.PNode;
import org.piccolo2d.nodes.PPath;
import vnc.drawing.Renderer;
import vnc.rfb.client.ClientToServerMessage;
import vnc.rfb.client.PointerEventMessage;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


abstract public class VNCControl extends VNCClient {

    private final NAR nar;



    public VNCControl(NAR nar, String host, int port) {
        super(host, port);
        this.nar = nar;

        initNAR();

    }


    public void initNAR() {

        nar.start(100,50);

        new AbstractReaction(nar, Events.FrameEnd.class, Events.ResetStart.class) {

            public boolean pendingRender = false;
            public float[][] trinode0Prev = new float[3][3];
            final Concept triconcept[][] = new Concept[3][3];
            final PNode trinode0[][]  = new PNode[3][3];
            boolean needsReset = true;
            final ColorArray ca = new ColorArray(50, new Color(0.5f,0,0,0), new Color(0.75f, 0, 0.25f, 0.5f));
            int frame = -1;
            int framesPerConceptRefresh = 100;

            protected void reset() {
                needsReset = true;
            }


            protected void onFrame() {
                frame++;

                if (getSurface()==null) return;

                PLayer sky = VNCControl.this.getSurface().getSkyLayer();
                if (sky == null) return;

                int ww = getSurface().getRenderer().getWidth();
                float w3x = ww / 3f;
                int hh = getSurface().getRenderer().getHeight();
                float w3y = hh / 3f;
                if (needsReset) {

                    synchronized(triconcept) {

                        if (needsReset) {

                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {

                                    final PPath n = PPath.createRectangle(0, 0, 1, 1);
                                    n.setStroke(null);

                                    sky.addChild(n);

                                    trinode0[i][j] = n;
                                    trinode0Prev[i][j] = 0;
                                    frame = 0;

                                }
                            }
                        }

                        needsReset = false;

                    }

                }
                if (frame % framesPerConceptRefresh == 0) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {

                            float x = w3x * i;
                            float y = w3y * j;
                            float cx = x + w3x/2;
                            float cy = y + w3y/2;

                            if (triconcept[i][j] == null) {
                                String pos = OCR.get3x3CoordsTree((int) cx, (int) cy, (int) ww, (int) hh, 1);
                                Concept c = nar.concept(pos);
                                if (c!=null)
                                    triconcept[i][j] = c;
                                else
                                    triconcept[i][j] = null;
                            }

                        }
                    }
                }

                if (!pendingRender) {
                    pendingRender = true;
                    SwingUtilities.invokeLater(this::render);
                }

            }


            protected void render() {
                pendingRender = false;

                int ww = getSurface().getRenderer().getWidth();
                float w3x = ww / 3f;
                int hh = getSurface().getRenderer().getHeight();
                float w3y = hh / 3f;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        float x = w3x * i;
                        float y = w3y * j;

                        trinode0[i][j].setBounds(x, y, (int) w3x, (int) w3y);

                        Concept c = triconcept[i][j];


                        if (c!=null) {

                            float priority = c.getPriority();
                            float dp = trinode0Prev[i][j] - priority;
                            float ap = Math.abs(dp) * 4; if (ap > 1f) ap = 1f;
                            float opacity = 0.1f;
                            trinode0[i][j].setPaint(
                                    new Color(1f,
                                            dp > 0 ? 1f-ap : 1f,
                                            dp < 0 ? 1f-ap : 1f,
                                            opacity
                                    )
                            );
                            trinode0[i][j].setVisible(true);
                            trinode0Prev[i][j] = priority;
                        }
                        else {
                            trinode0[i][j].setVisible(false);
                        }







                    }
                }

                //getSurface().getSky().getCamera().getLayer(0).setBounds(0,0,getWidth(),getHeight());

                repaint();
            }

            @Override public void event(Class event, Object[] args) {
                if (event == Events.FrameEnd.class)
                    onFrame();
                else if (event == Events.ResetStart.class) {
                    reset();
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
