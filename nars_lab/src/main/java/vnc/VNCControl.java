package vnc;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import nars.build.Default;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.logic.nal8.NullOperator;
import vnc.drawing.Renderer;
import vnc.rfb.client.ClientToServerMessage;
import vnc.rfb.client.PointerEventMessage;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


abstract public class VNCControl extends VNCClient {

    private final NAR nar;



    public VNCControl(NAR nar, String host, int port) {
        super(host, port);
        this.nar = nar;

        addOperators();
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

        NAR nar = new NAR(new Default());
        nar.param.setTiming(Memory.Timing.Real);
        nar.param.duration.set(750); //ms
        nar.param.noiseLevel.set(12);

        Video.themeInvert();
        NARSwing swing = new NARSwing(nar);

        swing.controls.setSpeed(-1);
        swing.controls.setFrameRate(4f);

        NWindow w = new NWindow("VNC",
                new VNCControl(nar, "localhost",5901) {
            @Override public String getParameter(String p) {
                return null;
            }
        }).show(800,600,true);


        nar.start(5,10);

    }

    @Override
    protected void videoUpdate(Renderer image, FramebufferUpdateRectangle rect) {
        super.videoUpdate(image, rect);
        OCR.queue(image, rect, ocrHandler);
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
                String ii = "<(*," + "B" + pe.buttonMask  + ",(*," + loc  + ")) --> ON>. :|: " +
                        (mousePressed ? "%1.00;0.90%" : "%0.00;0.90%");

                nar.input(
                        ii
                );
                System.out.println(ii);
            }
        }
    }

    private OCR.OCRResultHandler ocrHandler = new OCR.OCRResultHandler() {
        final int MAX_TEXT_LEN = 256;
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


                    words.add('\"' + s + '\"');
                }
                ii = ii + "(*,(*," + location + "),";
                if (words.size() == 0) {
                    return;
                }
                else if (words.size() == 1) {
                    ii += words.get(0);
                }
                else {
                    ii = ii + "(&/," + String.join(",", words) + ")";
                }
                ii = "<" + ii + ") --> SEE>. :|: \n";
                ii = "$" + Texts.n2(pri) + "$ " + ii;
                nar.input(ii);

                System.out.print(ii);
            }
        }
    };

    @Deprecated protected void keyEvent(KeyEvent e, boolean press) {
        String ii = "<{K" + e.getKeyCode() + "} --> ON>. :|: " +
                (press ? "%1.00;0.90%" : "%0.00;0.90%");
        nar.input(ii);
        System.out.println(ii);
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
