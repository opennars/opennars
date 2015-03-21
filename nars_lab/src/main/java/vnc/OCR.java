package vnc;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by me on 3/20/15.
 */
public class OCR {

    final static Tesseract t = Tesseract.getInstance();
    static {
        t.setDatapath("/usr/share/tessdata");
    }

    private static ExecutorService tasks = Executors.newSingleThreadExecutor();


    public static void main(String[] args) throws IOException, TesseractException {
//        File file = new File("/tmp/h.png");
//        String x = t.doOCR(ImageIO.read(file));
//        System.out.println(x);
//
//        System.out.println("TessBaseAPIRect");
//        String expResult = expOCRResult;
//        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
//        File tiff = new File(filename);
//        BufferedImage image = ImageIO.read(tiff); // require jai-imageio lib to read TIFF
//        ByteBuffer buf = ImageIOHelper.convertImageData(image);
//        int bpp = image.getColorModel().getPixelSize();
//        int bytespp = bpp / 8;
//        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
//
//        api = new Tesseract1(). DllAPIImpl().getInstance();
//        handle = api.TessBaseAPICreate();
//
//        api.TessBaseAPIInit3(handle, datapath, language);
//        api.TessBaseAPISetPageSegMode(handle, ITessAPI.TessPageSegMode.PSM_AUTO);
//        Pointer utf8Text = api.TessBaseAPIRect(handle, buf, bytespp, bytespl, 90, 50, 862, 614);
//        String result = utf8Text.getString(0);
//        api.TessDeleteText(utf8Text);
//        System.out.println(result);
    }


    static final int minWidth = 6;
    static final int minHeight = 10;


    static long now = System.currentTimeMillis();

    final static int bufferSize = 8;
    public static final NavigableSet<BufferUpdate> buffers = Collections.synchronizedNavigableSet(new TreeSet<BufferUpdate>(new Comparator<BufferUpdate>() {

        @Override public int compare(BufferUpdate o1, BufferUpdate o2) {
            if (o1 == o2) return 0;
            int fc = Float.compare(o1.priority(now), o2.priority(now));
            if (fc != 0) return fc;
            else
                return o1.hashCode() < o2.hashCode() ? 1 : -1;

        }
    }) {
        @Override
        public boolean add(BufferUpdate o) {
            final long now = System.currentTimeMillis();
            final float op = o.priority(now);
            while (size()+1 >= bufferSize) {
                BufferUpdate least = last();
                if (least.priority(now) > op) {
                    System.out.println("discard " + o + " " + o.priority() );
                    return false; //too low input
                }
                System.out.println("remove " + least + " " + least.priority() );
                remove(least);
            }
            System.out.println("add " + o + " " + o.priority() + " size=" + size() );
            return super.add(o);
        }
    });

//    public static synchronized BufferUpdate next() {
//        now = System.currentTimeMillis();
//
//        if (buffers.isEmpty()) return null;
//
//        BufferUpdate top = buffers.first();
//        System.out.println("do " + top + " " + top.priority() );
//        //buffers.remove(top);
//        return top;
//    }

    public static class BufferUpdate {
        public final BufferedImage image;
        private OCRResultHandler callback;
        public final FramebufferUpdateRectangle rect;
        private boolean processed;
        private String text;

        public BufferUpdate(FramebufferUpdateRectangle rect, BufferedImage image, OCRResultHandler callback) {

            this.rect = rect;
            this.image = image;
            this.callback = callback;
        }

        public float priority(long now) {
            float size = image.getWidth() * image.getHeight();
            float age = now - rect.createdAt;

            double seconds = 5;
            return (float) (0.1 * size / (1 + age/(seconds * 1000.0)));
        }

        public void setProcessed() {
            this.processed = true;
        }

        public boolean isProcessed() {
            return processed;
        }

        public void setText(String x) {
            this.text = x;
        }

        public String getText() {
            return text;
        }

        public float priority() {
            return priority(System.currentTimeMillis());
        }

        @Override
        public String toString() {
            return rect + " " + priority();
        }
    }

    interface OCRResultHandler {
        public void next(BufferUpdate u);
    }


    public static boolean queue(BufferedImage image, FramebufferUpdateRectangle rect, OCRResultHandler callback) {

        if ((rect.width < minWidth) || (rect.height < minHeight))
            return false;

        BufferUpdate bu = new BufferUpdate(rect, image, callback);


        return exe(bu, false);
    }

    private static boolean exe(BufferUpdate bu, boolean continueNext) {

        Runnable rr = new Runnable() {
            final long queued = System.currentTimeMillis();

            @Override
            public void run() {
                if (!buffers.remove(bu)) return; //if removed, forget it


                final long start = System.currentTimeMillis();

                String x = null;
                try {
                    x = t.doOCR(bu.image, bu.rect.newRectangle());
                    t.setOcrEngineMode(0);
                    t.setHocr(false);
                    final long end = System.currentTimeMillis();
                    long waiting = start - queued;
                    long processing = end - start;
                    System.out.println(bu + " " + x + "(" + waiting + " waiting ms, " + processing + " processing ms, " + buffers.size() + " behind)");
                    //TODO add this timing information to 'bu' result class
                } catch (TesseractException e) {
                    e.printStackTrace();
                }
                bu.setProcessed();
                bu.setText(x);
                bu.callback.next(bu);


//                if (!buffers.isEmpty()) {
//                    exe(next(), true);
//                }
            }
        };

//        if (continueNext) {
//            rr.run();
//            return true;
//        }
//        else {
//            if (buffers.isEmpty()) {
//                tasks.execute(rr);
//                return true;
//            }
//            else {
//                return buffers.add(bu);
//            }
            tasks.execute(rr);
            return buffers.add(bu);




    }
}
