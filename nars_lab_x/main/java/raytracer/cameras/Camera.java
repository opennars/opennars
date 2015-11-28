/*
 * Camera.java                            STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.cameras;

import raytracer.basic.ColorEx;
import raytracer.basic.Scene;

/**
 * Eine Kamera fertigt Aufnahmen einer Szene an.<br>
 * Sie befindet sich in einer Szene und besitzt dabei eine bestimmte Aufl�sung.
 * 
 * @author Mathias Kosch
 * @see AsyncCamera
 * @see Scene
 * 
 */
abstract public class Camera
{
    /** Horizontale Aufl�sung der Kamera in Pixel. */
    protected final int resX;
    /** Vertikale Aufl�sung der Kamera in Pixel. */
    protected final int resY;
    
    /** Szene, in der sich die Kamera befindet. */
    protected Scene scene = null;
    
    
    /**
     * Erzeugt eine neue Kamera einer festen Aufl�sung.
     * 
     * @param resX Horizontale Aufl�sung der Kamera.
     * @param resY Vertikale Aufl�sung der Kamera.
     */
    public Camera(int resX, int resY)
    {
        this.resX = resX;
        this.resY = resY;
    }
    
    
	/**
     * Ermittelt die horizontale Aufl�sung der Kamera.
     * 
     * @return Horizontale Aufl�sung der Kamera in Pixel.
	 */
    public int getResX()
    {
        return resX;
    }

    /**
     * Ermittelt die vertikale Aufl�sung der Kamera.
     * 
     * @return Vertikale Aufl�sung der Kamera in Pixel.
     */
    public int getResY()
    {
        return resY;
    }
    
    
    /**
     * Ermittelt die Szene, in der sich die Kamera befindet.
     * 
     * @return Szene, in der sich die Kamera befindet.
     */
    public Scene getScene()
    {
        return scene;
    }

    /**
     * Setzt die Szene, in der sich die Kamera befindet.
     * 
     * @param scene Szene, in der sich die Kamera befindet.
     */
    public void setScene(Scene scene)
    {
        this.scene = scene;
    }
    
    
    /**
     * Ermittelt den Farbwert eines Pixels der Kamera.<br>
     * <br>
     * Der x-Parameter muss dabei im Intervall <code>0..getResX()-1</code>
     * liegen.<br>
     * Der y-Parameter muss dabei im Intervall <code>0..getResY()-1</code>
     * liegen.
     * 
     * @param x x-Koordinate des Pixels der Kamera.
     * @param y y-Koordinate des Pixels der Kamera.
     * @return Ermittelter Farbwert des Pixels der Kamera.
     */
    abstract public ColorEx getColor(int x, int y);
    
    /**
     * Ermittelt den Farbwert eines Pixels der Kamera.<br>
     * Diese Methode verwendet unscharfe Pixel-Koordinaten. Dadurch k�nnen
     * beispielsweise auch die Farbwerte zwischen zwei Pixeln ermittelt werden,
     * um damit Anti-Alisasing durchzuf�hren.
     * <br>
     * Der x-Parameter muss dabei im Intervall <code>0..getResX()</code>
     * liegen.<br>
     * Der y-Parameter muss dabei im Intervall <code>0..getResY()</code>
     * liegen.
     * 
     * @param x x-Koordinate des Pixels der Kamera.
     * @param y y-Koordinate des Pixels der Kamera.
     * @return Ermittelter Farbwert des Pixels der Kamera.
     */
    abstract public ColorEx getColor(double x, double y);

}