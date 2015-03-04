/*
 * RaytracerConstants.java                STATUS: Vorläufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.basic;

/**
 * Konstanten, die für den gesamten Raytracer gelten.
 * 
 * @author Mathias Kosch
 *
 */
public class RaytracerConstants
{
    /**
     * Anzahl der Strahlen, die pro Pixel gesendet werden.
     */
    public final static int RAYS_PER_PIXEL = 1000;
    
    /**
     * Minimale Farbänderung zwischen zwei Render-Schritten, die noch als eine
     * Änderung gilt.
     */
    public final static double AA_MIN_DIFFERENCE = 0.025;
    
    /**
     * Maximale Anzahl an Antialiasing-Schritten, nachdem sich der Farbwert
     * nicht mehr geändert hat.
     */
    public final static int AA_MAX_UNCHANGED_COUNT = 3;
    
    
    /**
     * Maximale Anzahl an Objekten, die von einem fortgeführten Strahl getroffen
     * werden dürfen.<br>
     * Nachdem ein Strahl so viele Objekte getroffen hat, wird er nicht weiter
     * verfolgt.
     */
    public final static int MAX_RAY_HITCOUNT = 10;
    
    /**
     * Maximale Länge eines fortgeführten Strahls.<br>
     * Nachdem ein Strahl diese Länge erreicht hat, wird er nicht weiter
     * verfolgt.
     */
    public final static double MAX_RAY_WAY = Double.POSITIVE_INFINITY;
    
    /**
     * Minimales Gewicht eines Strahls.<br>
     * Nachdem ein Strahl dieses Gewicht unterschreitet, wird er nicht weiter
     * verfolgt.
     */
    public final static double MIN_RAY_WEIGHT = 0.002;
    
    
    /**
     * Gibt an, ob Farbwerte über <code>1.0</code> abgeschnitten werden sollen.
     */
    public final static boolean LIMIT_COLOR_INTENSITY = true;
    
    
    /**
     * Konstanter Lichtabschwächungsfaktor.
     */
    public final static float LIGHT_ATTENUATION_CONSTANT = 1.0f;
    
    /**
     * Linearer Lichtabschwächungsfaktor.
     */
    public final static float LIGHT_ATTENUATION_LINEAR = 0.5f;
    
    /**
     * Quadratischer Lichtabschwächungsfaktor.
     */
    public final static float LIGHT_ATTENUATION_QUADRATIC = 0.2f;

    
    /**
     * Gibt an, ob weiche Schatten aktiviert sind.<br>
     * Diese Option ist sehr rechenintensiv.
     */
    public final static boolean SOFT_SHADOWS_ENABLED = false;
    
    /**
     * Gibt an, wie viele Strahlen für weiche Schatten pro Flächeneinheit
     * verwendet werden.
     */
    public final static int RAYS_PER_UNIT_SPHERE = 250;
    
    /**
     * Gibt an, wie viele Strahlen mindestens (in folge) auf ein weiches Licht
     * geschickt werden.
     */
    public final static int MIN_RAYS_PER_SOFT_LIGHT = 3;
    
    /**
     * Gibt an, wie viele Strahlen höchstens (in folge) auf ein weiches Licht
     * geschickt werden.
     */
    public final static int MAX_RAYS_PER_SOFT_LIGHT = 15;
    
    /**
     * Gibt an, wie oft sich die Anzahl der versendeten Strahlen
     * (für weiche Schatten) erhöhen darf, falls eine Schattengrenze
     * festgestellt wurde.
     */
    public final static int SOFT_LIGHT_RAY_INCREASE_COUNT = 3;
    
    
    /**
     * Gibt an, ob Texturen geglättet werden sollen.
     */
    public final static boolean SMOOTH_TEXTURES = true;
    

    /**
     * Auflösung für Objekte in OpenGL.
     */
    public final static int GL_RESOLUTION = 50;
    
    /**
     * Gibt an, ob alle Objekte im Drahtgitter-Modus gezeichnet werden sollen.
     */
    public final static boolean GL_GRID_MODE_ENABLED = false;
}