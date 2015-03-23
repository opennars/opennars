package vnc;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;
import nars.gui.output.BitmapPanel;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import vnc.drawing.Renderer;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import vnc.viewer.swing.Surface;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by me on 3/20/15.
 */
public class OCR {

    public static final BufferUpdateFastSortedSet<BufferUpdate> ocrPending = new BufferUpdateFastSortedSet<BufferUpdate>();
    final static int bufferSize = 64;
    private static long inputBufferDelay = 100; //min wait time before processing a bufferd image, allowing it time to potentially grow with subsequent buffers

    //limits for peforming OCR after exiting queue
    static final int minOCRWidth = 12;
    static final int minOCRHeight = 12;

    //limits for what is allowing into input queue
    static final int minWidth = 4;
    static final int minHeight = minOCRHeight;
    static final int minPixels = minWidth * minHeight * 1;


    static TessAPI api = LoadLibs.getTessAPIInstance();
    final static Tesseract t = null;
    private static final ITessAPI.TessBaseAPI apiHandle;

    static {
        String datapath = ("/usr/share/tessdata");

        String language = "eng";
        int ocrEngineMode = 0;

        apiHandle = api.TessBaseAPICreate();
        StringArray sarray = new StringArray(new String[] {});//(String[])this.configList.toArray(new String[0]));
        List<String> configList = new ArrayList();
        PointerByReference configs = new PointerByReference();
        configs.setPointer(sarray);
        api.TessBaseAPIInit1(apiHandle, datapath, language, ocrEngineMode, configs, configList.size());
        //if(this.psm > -1) {
            //api.TessBaseAPISetPageSegMode(this.handle, this.psm);
        //}


        /*
        For tesseract-ocr >= 3.01 try increasing the variables
            language_model_penalty_non_freq_dict_word (0.1 default)
            language_model_penalty_non_dict_word (0.15 default)
         */
        //t.setTessVariable("language_model_penalty_non_freq_dict_word", "0.4f"); //default=0.15
        //t.setTessVariable("language_model_penalty_non_dict_word", "0.6f"); //default=0.15
    }
    static final JFrame frame = new JFrame("OCR");
    private static final BitmapPanel frameBitmap;
    static {

        frame.setVisible(true);
        frame.setSize(500, 500);
        frame.getContentPane().add(frameBitmap = new BitmapPanel());

        //frame.pack();
    }

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

    private static long now = System.currentTimeMillis(); //for caching current time
    private static final ScheduledExecutorService tasks = Executors.newScheduledThreadPool(1);


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

    public static char loccharx(int x) {
        switch (x) {
            case -1:
                return 'L';
            case 0:
                return 'C';
            case 1:
                return 'R';
        }
        return 0;
    }

    public static char locchary(int x) {
        switch (x) {
            case -1:
                return 'U';
            case 0:
                return 'c'; //lowercase
            case 1:
                return 'D';
        }
        return 0;
    }

    public static String get3x3CoordsFlat(int tx, int ty, int height, int width) {

        int dx = width / 3, dy = height / 3;
        int cx = 0, cy = 0;
        List<String> loc = new ArrayList();
        while (dx > minWidth) {
            int ux, uy;
            if (tx > cx + dx * 2) {
                ux = 1;
                cx += (dx * 2);
            } else if (tx > cx + dx) {
                ux = 0;
                cx += dx;
            } else
                ux = -1;
            if (ty > cy + dy * 2) {
                uy = 1;
                cy += dy * 2;
            } else if (ty > cy + dy) {
                uy = 0;
                cy += dy;
            } else
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

        int dx = width / 3, dy = height / 3;
        int cx = 0, cy = 0;
        String j = "";
        int count = 0;
        while (dx > minWidth) {
            int ux, uy;
            if (tx > cx + dx * 2) {
                ux = 1;
                cx += (dx * 2);
            } else if (tx > cx + dx) {
                ux = 0;
                cx += dx;
            } else
                ux = -1;
            if (ty > cy + dy * 2) {
                uy = 1;
                cy += dy * 2;
            } else if (ty > cy + dy) {
                uy = 0;
                cy += dy;
            } else
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

    public static boolean queue(Renderer renderer, FramebufferUpdateRectangle rect, OCRResultHandler callback, long now) {

        if ((rect.width < minWidth) || (rect.height < minHeight) || (rect.width * rect.height < minPixels))
            return false;

        BufferUpdate bu = new BufferUpdate(rect, callback);


        return exe(now, renderer, bu);
    }

    public static BufferedImage getGrayscaleSubImage(BufferedImage image, int x, int y, int width, int height) {
        BufferedImage tmp = new BufferedImage(width, height, 10);
        Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image.getSubimage(x, y, width, height), 0, 0, (ImageObserver) null);
        g2.dispose();
        return tmp;
    }

    public static ByteBuffer convertImageData(BufferedImage bi, Rectangle r) {
        DataBuffer buff = bi.getRaster().getDataBuffer();

        if (!(buff instanceof DataBufferByte)) {

            int b = 0; //border
            int ww = r.width + b * 2;
            int hh = r.height + b * 2;

            BufferedImage tmp = new BufferedImage(ww, hh, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2 = tmp.createGraphics();

            if (b > 0) {
                //draw border (TODO find background color and use it)*/
                g2.setPaint(Color.GRAY);
                g2.fillRect(0, 0, ww, b);
                g2.fillRect(0, hh - b, ww, b);
                g2.fillRect(0, 0, b, hh);
                g2.fillRect(ww - b, 0, b, hh);
            }

            g2.drawImage(bi,
                    b, b, b + r.width, b + r.height,
                    r.x, r.y, r.x + r.width, r.y + r.height,
                    (ImageObserver) null);
            g2.dispose();

            frameBitmap.setImage(tmp);

            //bi = ImageHelper.convertImageToGrayscale(bi.getSubimage(r.x, r.y, r.width, r.height));
            //bi = getGrayscaleSubImage(bi, r.x, r.y, r.width, r.height);
            //bi = ImageHelper.convertImageToBinary(bi.getSubimage(r.x, r.y, r.width, r.height));
            buff = tmp.getRaster().getDataBuffer();
            return ByteBuffer.wrap(((DataBufferByte) buff).getData());

        } else {
            bi = bi.getSubimage(r.x, r.y, r.width, r.height);
            buff = bi.getRaster().getDataBuffer();
            return ByteBuffer.wrap(((DataBufferByte) buff).getData());
        }

    }

    private static boolean exe(long inputTime, Renderer renderer, BufferUpdate bu) {


        Runnable rr = new Runnable() {
            final long queued = System.currentTimeMillis();

            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                long waiting = start - queued;


                if (!ocrPending.remove(bu)) return; //if removed, forget it

                String x = null;
                long end = 0;

                if ((bu.rect.width >= minOCRWidth) && (bu.rect.height >= minOCRHeight)) {


                        //t.setOcrEngineMode(0);


                        FramebufferUpdateRectangle r = bu.rect;
                        BufferedImage frame = renderer.getFrame();


                        //x = t.doOCR(frame, bu.rect.newRectangle());


                        Rectangle rr = r.newRectangle();
                        ByteBuffer ci = convertImageData(frame, rr);
                        Rectangle subFrame = new Rectangle(0, 0, rr.width, rr.height);
                        int subBPP = 8; //image.getColorModel().getPixelSize()
                        //x = t.doOCR(subFrame.width, subFrame.height, ci, subFrame, subBPP);

                        try {
                            int bytespp = subBPP / 8;
                            int xsize = subFrame.width;
                            int ysize = subFrame.height;
                            int bytespl = (int)Math.ceil((double)(xsize * subBPP) / 8.0D);
                            api.TessBaseAPISetImage(apiHandle, ci, xsize, ysize, bytespp, bytespl);

                            api.TessBaseAPISetRectangle(apiHandle, subFrame.x, subFrame.y, subFrame.width, subFrame.height);


                            Pointer utf8Text = api.TessBaseAPIGetUTF8Text(apiHandle);
                            String str = utf8Text.getString(0L);
                            api.TessDeleteText(utf8Text);

                            x = str;

                        } catch (Exception var12) {
                            //logger.log(Level.SEVERE, var12.getMessage(), var12);
                            var12.printStackTrace();
                        }


                        end = System.currentTimeMillis();
                        //System.out.println(bu + " " + x + "(" + waiting + " waiting ms, " + processing + " processing ms, " + buffers.size() + " behind)");
                        //TODO add this timing information to 'bu' result class


                    long processing = end - start;

                    bu.setProcessed();
                    bu.setText(x, inputTime, waiting, processing);
                    bu.callback.next(bu);
                }
            }
        };

        tasks.schedule(rr, inputBufferDelay, TimeUnit.MILLISECONDS);
        return ocrPending.add(bu);
    }

    interface OCRResultHandler {
        public void next(BufferUpdate u);
    }

    public static class BufferUpdate {
        public final FramebufferUpdateRectangle rect;
        private OCRResultHandler callback;
        private boolean processed;
        private String text;
        private long inputTime, waitingTime, processingTime;

        public BufferUpdate(FramebufferUpdateRectangle rect, OCRResultHandler callback) {

            this.rect = rect;
            this.callback = callback;

        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            BufferUpdate bu = (BufferUpdate)obj;
            return rect.equals(bu.rect);
        }

        @Override
        public int hashCode() {
            return rect.hashCode();
        }

        public float priority(long now) {
            if (processed) return -1;

            float size = rect.width * rect.height;
            float age = now - rect.createdAt;
            age-= inputBufferDelay; //dont penalize items still in the initial delay period
            if (age < 0) age = 0;

            double seconds = 5;

            float pri = (float) (size / (1f + age / (seconds * 1000.0f)));

            //System.out.println(age + " " + rect.width + " " + rect.height + " = " + pri);

            return pri;
        }

        public void setProcessed() {
            this.processed = true;
        }

        public boolean isProcessed() {
            return processed;
        }

        public void setText(String x, long inputTime, long waitingTime, long processingTime) {
            this.text = x;
            this.inputTime = inputTime;
            this.waitingTime = waitingTime;
            this.processingTime = processingTime;
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
            return ((float) (rect.width * rect.height)) / ((float) ((surface.getWidth() * surface.getHeight())));
        }

        public long getWaitingTime() {
            return waitingTime;
        }

        public long getProcessingTime() {
            return processingTime;
        }


        public long getInputTime() {
            return inputTime;
        }

        public boolean commonEdges(BufferUpdate o) {
            //common right (this) | left (o) coords
            int minXDist = 4;
            if (Math.abs((rect.x) - (o.rect.x)) > minXDist)
                return false;
            if (Math.abs((rect.x + rect.width) - (o.rect.x + o.rect.width)) > minXDist)
                return false;

            //common top and bottom coord
            int minYDist = 4;
            if (Math.abs((rect.y) - (o.rect.y)) > minYDist)
                return false;
            if (Math.abs((rect.y + rect.height) - (o.rect.y + o.rect.height)) > minYDist)
                return false;

            return true;
        }

        public boolean commonRightEdge(BufferUpdate o) {
            //common right (this) | left (o) coords
            int minXDist = 4;
            if (Math.abs((rect.x + rect.width) - (o.rect.x)) > minXDist)
                return false;

            //common top and bottom coord
            int minYDist = 4;
            if (Math.abs((rect.y) - (o.rect.y)) > minYDist)
                return false;
            if (Math.abs((rect.y + rect.height) - (o.rect.y + o.rect.height)) > minYDist)
                return false;

            return true;
        }

        /**
         * grow the area to include another
         */
        public void grow(BufferUpdate o) {
            int ax, ay, bx, by;
            ax = Math.min(rect.x, o.rect.x);
            ay = Math.min(rect.y, o.rect.y);
            bx = Math.max(rect.x + rect.width, o.rect.x + o.rect.width);
            by = Math.max(rect.y + rect.height, o.rect.y + o.rect.height);

            System.out.print("grow " + rect + " with " + o.rect);

            rect.x = ax;
            rect.y = ay;
            rect.width = (bx - ax);
            rect.height = (by - ay);

            System.out.println(" to " + rect);

        }
    }

    private static class BufferUpdateFastSortedSet<E extends BufferUpdate> extends ConcurrentSkipListSet<E> {


//        public BufferUpdateFastSortedSet() {
//            super(new AtomicSortedSetImpl(new FastSortedMapImpl<E,Void>(comp
//                    , Equalities.IDENTITY).keySet())
//            );
//        }


        public BufferUpdateFastSortedSet() {
            super(new Comparator<BufferUpdate>() {

                @Override
                public int compare(BufferUpdate o1, BufferUpdate o2) {
                    if (o1.equals(o2)) return 0;
                    int fc = Float.compare(o1.priority(OCR.now), o2.priority(OCR.now));
                    if (fc != 0) return fc;
                    else
                        return o1.hashCode() < o2.hashCode() ? 1 : -1;
                }
            });
        }


        @Override
        public boolean add(E o) {

            synchronized (this) {
                for (E prev : this) {
                    if (prev.equals(o)) {

                        return false;
                    }
                    if (prev != null && !prev.isProcessed()) { //TODO combine into an atomic operation: ifNotProcessedThenTryToGrow(..)
                        //TODO combine into one method with a boolean vector
                        if (prev.commonEdges(o) || prev.commonRightEdge(o)) {
                            prev.grow(o);
                            return false;
                        }

                    }

                }

            }

            /*System.out.println();
            for (Object x : ocrPending) {
                System.out.println(x);
            }*/


            now = System.currentTimeMillis();
            final float op = o.priority(now);
            final int s = size();
            if (s+1 >= bufferSize) {

                E least = last();
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
    }
}
