package vnc;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import vnc.drawing.Renderer;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import vnc.viewer.swing.Surface;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by me on 3/20/15.
 */
public class OCR {

    final static Tesseract t = Tesseract.getInstance();
    static {
        t.setDatapath("/usr/share/tessdata");

        /*
        For tesseract-ocr >= 3.01 try increasing the variables
            language_model_penalty_non_freq_dict_word (0.1 default)
            language_model_penalty_non_dict_word (0.15 default)
         */
        t.setTessVariable("language_model_penalty_non_freq_dict_word","0.4f"); //default=0.15
        t.setTessVariable("language_model_penalty_non_dict_word","0.6f"); //default=0.15
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


    static final int minWidth = 12;
    static final int minHeight = 12;
    static final int minPixels = minWidth * minHeight * 2;


    static long now = System.currentTimeMillis();

    final static int bufferSize = 16;
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
                    //System.out.println("discard " + o + " " + o.priority() );
                    return false; //too low input
                }
                //System.out.println("discard " + least + " " + least.priority() );
                remove(least);
            }
            //System.out.println("add " + o + " " + o.priority() + " size=" + size() );
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

    public static char loccharx(int x) {
        switch(x) {
            case -1: return 'L';
            case 0: return 'C';
            case 1: return 'R';
        }
        return 0;
    }
    public static char locchary(int x) {
        switch(x) {
            case -1: return 'U';
            case 0: return 'c'; //lowercase
            case 1: return 'D';
        }
        return 0;
    }
    public static String get3x3CoordsFlat(int tx, int ty, int height, int width) {

        int dx = width/3, dy = height/3;
        int cx = 0, cy = 0;
        List<String> loc = new ArrayList();
        while (dx > minWidth) {
            int ux, uy;
            if (tx > cx + dx * 2)
            { ux = 1; cx += (dx*2); }
            else if (tx > cx + dx)
            { ux = 0; cx += dx; }
            else
                ux = -1;
            if (ty > cy + dy * 2)
            { uy = 1; cy += dy*2; }
            else if (ty > cy + dy)
            { uy = 0; cy += dy; }
            else
                uy = -1;
            String p = "{" + loccharx(ux) + "," + locchary(uy) + "}";
            loc.add(p);

            dx /= 3;
            dy /= 3;
            if ((dx < minWidth) || (dy < minHeight))
                break;
        }
        return String.join(",", loc);

    }
    public static String get3x3CoordsTree(int tx, int ty, int height, int width) {

        int dx = width/3, dy = height/3;
        int cx = 0, cy = 0;
        String j = "";
        int count = 0;
        while (dx > minWidth) {
            int ux, uy;
            if (tx > cx + dx * 2)
            { ux = 1; cx += (dx*2); }
            else if (tx > cx + dx)
            { ux = 0; cx += dx; }
            else
                ux = -1;
            if (ty > cy + dy * 2)
            { uy = 1; cy += dy*2; }
            else if (ty > cy + dy)
            { uy = 0; cy += dy; }
            else
                uy = -1;
            String p = ",{" + loccharx(ux) + "," + locchary(uy);
            j += p;
            count++;

            dx /= 3;
            dy /= 3;
            if ((dx < minWidth) || (dy < minHeight))
                break;
        }
        if (j.isEmpty()) return "";
        for (int i = 0; i < count; i++)
            j += "}";
        j = j.substring(1, j.length()); //remove leading comma
        return j;

    }

    public static class BufferUpdate {
        private OCRResultHandler callback;
        public final FramebufferUpdateRectangle rect;
        private boolean processed;
        private String text;

        public BufferUpdate(FramebufferUpdateRectangle rect, OCRResultHandler callback) {

            this.rect = rect;
            this.callback = callback;
        }

        public float priority(long now) {
            float size = rect.width * rect.height;
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



        public String getLocation(Surface surface) {

            BufferedImage frame = surface.getRenderer().getFrame();

            return get3x3CoordsTree(rect.x + rect.width / 2, rect.y + rect.height / 2, surface.getWidth(), surface.getHeight());
        }



        public float getScreenFraction(Surface surface) {
            return ((float)(rect.width * rect.height)) / ((float)((surface.getWidth() * surface.getHeight())));
        }
    }

    interface OCRResultHandler {
        public void next(BufferUpdate u);
    }


    public static boolean queue(Renderer renderer, FramebufferUpdateRectangle rect, OCRResultHandler callback) {

        if ((rect.width < minWidth) || (rect.height < minHeight) || (rect.width*rect.height < minPixels))
            return false;

        BufferUpdate bu = new BufferUpdate(rect, callback);


        return exe(renderer, bu, false);
    }
//    public static BufferedImage getGrayscaleSubImage(BufferedImage image, int x, int y, int width, int height) {
//        BufferedImage tmp = new BufferedImage(width, height, 10);
//        Graphics2D g2 = tmp.createGraphics();
//        g2.drawImage(image.getSubimage(x, y, width, height), 0, 0, (ImageObserver)null);
//        g2.dispose();
//        return tmp;
//    }
//    public static ByteBuffer convertImageData(BufferedImage bi, Rectangle r) {
//        DataBuffer buff = bi.getRaster().getDataBuffer();
//
//        if(!(buff instanceof DataBufferByte)) {
//            bi = ImageHelper.convertImageToGrayscale(bi.getSubimage(r.x, r.y, r.width, r.height));
//            //bi = getGrayscaleSubImage(bi, r.x, r.y, r.width, r.height);
//            //bi = ImageHelper.convertImageToBinary(bi.getSubimage(r.x, r.y, r.width, r.height));
//            buff = bi.getRaster().getDataBuffer();
//        }
//        else {
//            bi = bi.getSubimage(r.x, r.y, r.width, r.height);
//            buff = bi.getRaster().getDataBuffer();
//        }
//
//        r.x = 0;
//        r.y = 0;
//
//        byte[] pixelData = ((DataBufferByte)buff).getData();
//        ByteBuffer buf = ByteBuffer.allocateDirect(r.width * r.height * 1);
//        buf.order(ByteOrder.nativeOrder());
//        buf.put(pixelData);
//        buf.flip();
//        return buf;
//    }
    private static boolean exe(Renderer renderer, BufferUpdate bu, boolean continueNext) {



        Runnable rr = new Runnable() {
            final long queued = System.currentTimeMillis();

            @Override
            public void run() {
                if (!buffers.remove(bu)) return; //if removed, forget it


                final long start = System.currentTimeMillis();

                String x = null;
                try {

                    t.setOcrEngineMode(0);


                    FramebufferUpdateRectangle r = bu.rect;
                    BufferedImage frame = renderer.getFrame();






                    x = t.doOCR(frame, bu.rect.newRectangle());


                    /*
                    Rectangle rr = r.newRectangle();
                    ByteBuffer ci = convertImageData(frame, rr);
                    x = t.doOCR(rr.width,rr.height, ci, rr, frame.getColorModel().getPixelSize());
                    */

                    final long end = System.currentTimeMillis();
                    long waiting = start - queued;
                    long processing = end - start;
                    //System.out.println(bu + " " + x + "(" + waiting + " waiting ms, " + processing + " processing ms, " + buffers.size() + " behind)");
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
