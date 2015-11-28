/*
 * AsyncCamera.java                       STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */


//TODO: Integer-Werte f�r Farben


package raytracer.cameras;

import org.apache.commons.math3.util.FastMath;
import raytracer.basic.ColorEx;
import raytracer.basic.RaytracerConstants;
import raytracer.basic.Scene;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Diese Kamera unterst�tzt das asynchrone Rendern einer Aufnahme.<br>
 * Dabei wird zun�chst ein grobk�rniges Bild der Szene erstellt, was
 * anschlie�end immer weiter verfeinert wird.<br>
 * Weiterhin wird adaptives Anti-Aliasing unterst�tzt.
 *
 * @author Mathias Kosch
 * @see raytracer.cameras.Camera
 * @see Scene
 */
abstract public class AsyncCamera extends Camera {
    /**
     * Gibt an, das derzeit keine Szene gerendert wurde.
     */
    final static int STATE_INVALID = 0;
    /**
     * Gibt an, dass gerade eine Szene gerendert wird.
     */
    final static int STATE_RENDERING = 1;
    /**
     * Gibt an, dass die letzte Szene fertig gerendert wurde.
     */
    final static int STATE_RENDERED = 2;


    /**
     * Gibt den Status der Rendering-Vorgangs an.
     */
    final AtomicInteger state = new AtomicInteger(STATE_INVALID);
    /**
     * Liste aller <code>RendererListener</code>-Objekte, die ereignisse empfangen.
     */
    final LinkedHashSet<RendererListener> listeners = new LinkedHashSet<RendererListener>();

    /** Thread zum Rendern der Szenen. */
    /**
     * Exponent der aufgerundeten Gr��e der gr��ten Seite des Bildes.
     */
    byte sizeExponent = (byte) 0;
    AsyncCameraThread thread = null;

    /**
     * Summe aller 'Rot'-Werte jedes Pixels.
     */
    final float rmap[];
    /**
     * Summe aller 'Gr�n'-Werte jedes Pixels.
     */
    final float gmap[];
    /**
     * Summe aller 'Blau'-Werte jedes Pixels.
     */
    final float bmap[];
    /**
     * Anzahl der Farb-Summanden jedes Pixels.
     */
    final int countmap[];
    /**
     * Gibt an, seit wie vielen Schritten sich ein Farbwert kaum ver�ndert hat.
     */
    final int aaUnchangedCount[];
    /**
     * RGB-Farbwert jedes Pixels.
     */
    final int bitmap[];
    private BufferedImage image;


    /**
     * Erzeugt eine neue asynchrone Kamera einer festen Aufl�sung.
     *
     * @param resX Horizontale Aufl�sung der Kamera.
     * @param resY Vertikale Aufl�sung der Kamera.
     */
    public AsyncCamera(int resX, int resY) {
        super(resX, resY);

        // Exponent, der die aufgerundete Gr��e der gr��ten Seite des
        // Bildes angibt:
        sizeExponent = RaytracerConstants.getLowResolution(resX, resY);

        // Arrays initialisieren, die die Farbwerte der Pixel beschreiben:
        int count = resX * resY;
        rmap = new float[count];
        gmap = new float[count];
        bmap = new float[count];
        countmap = new int[count];
        aaUnchangedCount = new int[count];
        bitmap = new int[count];
    }


    /**
     * Startet den Render-Vorgang der Szene.
     */
    public void render() {
        if (state.get() == STATE_RENDERING)
            throw new IllegalStateException();

        if (thread!=null)
            stop();

        state.set(STATE_RENDERING);

        Arrays.fill(rmap, 0);
        Arrays.fill(gmap, 0);
        Arrays.fill(bmap, 0);
        Arrays.fill(countmap, 0);
        Arrays.fill(aaUnchangedCount, 0);
        //Arrays.fill(bitmap, 0);

        // Thread starten:
        thread = new AsyncCameraThread(this);
        thread.start();
    }

    /**
     * Bricht den Render-Vorgang einer Szene ab.
     */
    public void stop() {
        if (state.get() != STATE_RENDERING)
            return;

        state.set(STATE_INVALID);

        // Thread anhalten:
        if (thread!=null) {
            thread.die();
            thread = null;
        }



    }

    /**
     * Ermittelt, ob gerade eine Szene gerendert wird.
     *
     * @return <code>true</code>, falls gerade eine Szene gerendert wird.<br>
     * Andernfalls <code>false</code>.
     */
    public boolean isRendering() {
        return state.get() == STATE_RENDERING;
    }

    /**
     * Ermittelt, ob eine Szene bereits fertig gerendert wurde.
     *
     * @return <code>true</code>, falls eine Szene bereits gerendert wurde.<br>
     * Andernfalls <code>false</code>.
     */
    public boolean isRendered() {
        return state.get() == STATE_RENDERED;
    }

    /**
     * Ermittelt das Kamera-Bild der gerenderten Szene.<br>
     * Diese Funktion liefert nur ein Ergebnis, falls eine Szene gerade
     * gerendert wird oder bereits gerendert wurde.
     *
     * @return Bild der gerenderten Szene oder <code>null</code>.
     */
    public BufferedImage getImage() {
        final int ss = state.get();
        if ((ss != STATE_RENDERING) && (ss != STATE_RENDERED))
            return null;

        // Bild aus dem Pixel-Array erzeugen und zur�ckgeben:
        if (image ==null || image.getWidth()!=resX || image.getHeight()!=resY)
            image = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, resX, resY, bitmap, 0, resX);
        return image;
    }

    /**
     * Ermittelt das Kamera-Bild der gerenderten Szene.<br>
     * Dabei werden die Bilddaten in ein bereits bestehendes Bild kopiert, das
     * die gleiche Gr��e haben muss. Dadurch wird die Allokation eines
     * neuen Speicherblocks vermieden.
     *
     * @param image Bestehendes Bild, in das die Bilddaten geschrieben werden.
     */
    public void getImage(BufferedImage image) {
        final int ss = state.get();
        if ((ss != STATE_RENDERING) && (ss != STATE_RENDERED))
            return;

        if ((image.getWidth() != resX) || (image.getHeight() != resY))
            throw new IllegalArgumentException();

        // Bilddaten kopieren:
        image.setRGB(0, 0, resX, resY, bitmap, 0, resX);
    }


    /**
     * F�gt einen neuen <code>RendererListener</code> zu dieser Instanz hinzu.
     *
     * @param l <code>RendererListener</code>, der hinzugef�gt werden soll.
     */
    public void addRendererListener(RendererListener l) {
        listeners.add(l);
    }

    /**
     * Sendet das <code>renderUpdate</code>-Ereignis an alle eingetragenen
     * <code>RendererListener</code>.
     *
     * @param progress Fortschritt des Ereignisses.
     */
    void fireRenderUpdate(double progress) {
        RendererEvent event = new RendererEvent(this, progress);
        //TODO multiplex these into one thread
        for (RendererListener listener : listeners)
            (new Thread(new RenderUpdateEventRunner(event, listener))).start();
    }

    /**
     * Sendet das <code>renderFinished</code>-Ereignis an alle eingetragenen
     * <code>RendererListener</code>.
     */
    void fireRenderFinished() {
        RendererEvent event = new RendererEvent(this, 1.0);
        //TODO multiplex these into one thread
        for (RendererListener listener : listeners)
            (new Thread(new RenderFinishedEventRunner(event, listener))).start();
    }

    public void absorb(final int index, final ColorEx color) {
        rmap[index] += color.x;
        gmap[index] += color.y;
        bmap[index] += color.z;
        countmap[index]++;
    }

    public double pixelDifference(final ColorEx color, final int index, final float count) {
        return (FastMath.abs(rmap[index] - color.x * count)+
                FastMath.abs(gmap[index]-color.y*  count)+
                FastMath.abs(bmap[index]-color.z* count)) / (count + /* * */ count + count);
    }
}