package vnc;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import nars.build.Default;
import nars.core.Memory;
import nars.core.NAR;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.logic.entity.Term;
import nars.logic.nal8.TermFunction;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import vnc.viewer.cli.VNCProperties;
import vnc.viewer.swing.ParametersHandler;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;


abstract public class VNCControl extends VNCClient {

    private final NAR nar;



    public VNCControl(NAR nar, VNCProperties param) {
        super(param);
        this.nar = nar;

        addOperators();
    }

    private void addOperators() {
        nar.on(new TermFunction("^keyboard") {

            @Override
            public Term function(Term[] x) {
                System.out.println("keyboard: " + Arrays.toString(x));
                return null;
            }
        });
    }

    public static void main(String[] args) {


        VNCProperties param = new VNCProperties("localhost",5091);

        ParametersHandler.completeParserOptions(param);

        param.parse(args);
        if (param.isSet(ParametersHandler.ARG_HELP)) {
            printUsage(param.optionsUsage());
            System.exit(0);
        }

        NAR nar = new NAR(new Default());
        nar.param.setTiming(Memory.Timing.Real);
        nar.param.duration.set(1000); //ms
        nar.param.noiseLevel.set(7);

        Video.themeInvert();
        NARSwing swing = new NARSwing(nar);

        swing.controls.setSpeed(-1);
        swing.controls.setFrameRate(2f);

        NWindow w = new NWindow("VNC",
                new VNCControl(nar, param) {
            @Override public String getParameter(String p) {
                return null;
            }
        }).show(800,600,true);


        nar.start(10,10);

    }

    @Override
    protected void videoUpdate(BufferedImage image, FramebufferUpdateRectangle rect) {
        super.videoUpdate(image, rect);
        OCR.queue(image, rect, ocrHandler);
    }

    private OCR.OCRResultHandler ocrHandler = new OCR.OCRResultHandler() {
        final int MAX_TEXT_LEN = 32;

        @Override
        public void next(OCR.BufferUpdate u) {
            if (!u.isProcessed()) return;

            String text = u.getText();
            if (text!=null && !text.isEmpty() && text.length() < MAX_TEXT_LEN) {

                //TODO tokenize better
                String[] t2 = text.split("[^a-zA-Z0-9]+");

                for (String s : t2) {
                    s = s.toLowerCase();

                    float pri = 0.5f + (0.1f * s.length() / text.length());

                    nar.input(
                            "$" + Texts.n2(pri) + "$ <\"" + s + "\" --> READ>. :|:"
                    );
                }
            }
        }
    };

    protected void keyEvent(KeyEvent e, boolean press) {
        nar.input(
                "keyboard(" + e.getKeyCode() + "," +
                        (press ? "PRESS" : "RELEASE") + ")!"
        );
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
