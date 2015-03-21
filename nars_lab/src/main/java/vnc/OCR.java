package vnc;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by me on 3/20/15.
 */
public class OCR {

    final static Tesseract t = Tesseract.getInstance();
    static {
        t.setDatapath("/usr/share/tessdata");
    }

    private static ExecutorService ocr = Executors.newSingleThreadExecutor();


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


    public static Future<String> queue(BufferedImage image, FramebufferUpdateRectangle rect) {


        final long start = System.currentTimeMillis();

        return ocr.submit(new Callable<String>() {

            @Override
            public String call() throws Exception {
                String x = null;
                try {
                    x = t.doOCR(image, rect.newRectangle());
                    final long end = System.currentTimeMillis();
                    long delay = end - start;
                    System.out.println(x + "( " + delay + " ms)");
                } catch (TesseractException e) {
                    e.printStackTrace();
                }
                return x;
            }
        });

    }
}
