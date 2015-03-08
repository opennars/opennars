/*
 * RaytracerConstants.java                STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.basic;

/**
 * Konstanten, die f�r den gesamten Raytracer gelten.
 * 
 * @author Mathias Kosch
 *
 */
public class RaytracerConstants
{
    /**
     * Anzahl der Strahlen, die pro Pixel gesendet werden.
     */
    public final static int RAYS_PER_PIXEL = 1;
    
    /**
     * Minimale Farb�nderung zwischen zwei Render-Schritten, die noch als eine
     * �nderung gilt.
     */
    public final static double AA_MIN_DIFFERENCE = 0.045;
    
    /**
     * Maximale Anzahl an Antialiasing-Schritten, nachdem sich der Farbwert
     * nicht mehr ge�ndert hat.
     */
    public final static int AA_MAX_UNCHANGED_COUNT = 3;
    
    
    /**
     * Maximale Anzahl an Objekten, die von einem fortgef�hrten Strahl getroffen
     * werden d�rfen.<br>
     * Nachdem ein Strahl so viele Objekte getroffen hat, wird er nicht weiter
     * verfolgt.
     */
    public final static int MAX_RAY_HITCOUNT = 4;
    
    /**
     * Maximale L�nge eines fortgef�hrten Strahls.<br>
     * Nachdem ein Strahl diese L�nge erreicht hat, wird er nicht weiter
     * verfolgt. DISTANCE LIMIT?
     */
    public final static double MAX_RAY_WAY = 200; //Double.POSITIVE_INFINITY;
    
    /**
     * Minimales Gewicht eines Strahls.<br>
     * Nachdem ein Strahl dieses Gewicht unterschreitet, wird er nicht weiter
     * verfolgt.
     */
    public final static double MIN_RAY_WEIGHT = 0.005;
    
    
    /**
     * Gibt an, ob Farbwerte �ber <code>1.0</code> abgeschnitten werden sollen.
     */
    public final static boolean LIMIT_COLOR_INTENSITY = true;
    
    
    /**
     * Konstanter Lichtabschw�chungsfaktor.
     */
    public final static float LIGHT_ATTENUATION_CONSTANT = 1.0f;
    
    /**
     * Linearer Lichtabschw�chungsfaktor.
     */
    public final static float LIGHT_ATTENUATION_LINEAR = 0.5f;
    
    /**
     * Quadratischer Lichtabschw�chungsfaktor.
     */
    public final static float LIGHT_ATTENUATION_QUADRATIC = 0.2f;

    
    /**
     * Gibt an, ob weiche Schatten aktiviert sind.<br>
     * Diese Option ist sehr rechenintensiv.
     */
    public final static boolean SOFT_SHADOWS_ENABLED = false;
    
    /**
     * Gibt an, wie viele Strahlen f�r weiche Schatten pro Fl�cheneinheit
     * verwendet werden.
     */
    public final static int RAYS_PER_UNIT_SPHERE = 50;
    
    /**
     * Gibt an, wie viele Strahlen mindestens (in folge) auf ein weiches Licht
     * geschickt werden.
     */
    public final static int MIN_RAYS_PER_SOFT_LIGHT = 2;
    
    /**
     * Gibt an, wie viele Strahlen h�chstens (in folge) auf ein weiches Licht
     * geschickt werden.
     */
    public final static int MAX_RAYS_PER_SOFT_LIGHT = 7;
    
    /**
     * Gibt an, wie oft sich die Anzahl der versendeten Strahlen
     * (f�r weiche Schatten) erh�hen darf, falls eine Schattengrenze
     * festgestellt wurde.
     */
    public final static int SOFT_LIGHT_RAY_INCREASE_COUNT = 3;
    
    
    /**
     * Gibt an, ob Texturen gegl�ttet werden sollen.
     */
    public final static boolean SMOOTH_TEXTURES = true;
    

    /**
     * Aufl�sung f�r Objekte in OpenGL.
     */
    public final static int GL_RESOLUTION = 50;
    
    /**
     * Gibt an, ob alle Objekte im Drahtgitter-Modus gezeichnet werden sollen.
     */
    public final static boolean GL_GRID_MODE_ENABLED = false;

    public static byte getLowResolution(int resX, int resY) {
        return (byte) (Math.ceil(Math.log((double) Math.max(resX, resY)) / Math.log(2)));
    }
}