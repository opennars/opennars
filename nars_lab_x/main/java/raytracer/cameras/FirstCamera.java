/*
 * FirstCamera.java                       STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 * �NDERUNGEN:
 * [*] Das einschlie�ende Quadrat der Blickgr��e wird auf den Einheitsvektor
 *     abgebildet.
 *     
 */

package raytracer.cameras;

import raytracer.basic.ColorEx;
import raytracer.basic.Ray;

import javax.vecmath.Vector3d;

/**
 * Diese Kamera fertigt eine Aufnahme aus einer simplen Perspektive an.
 * 
 * @author Mathias Kosch
 * 
 */
public class FirstCamera extends AsyncCamera
{
    public final static Vector3d org = new Vector3d(0.0, 0.0, -10.0);
    
    
    private final double factor;
    private final double offsetX;
    private final double offsetY;
    
    
    /**
     * Erzeugt eine neue Kamera einer festen Aufl�sung.
     * 
     * @param resX Horizontale Aufl�sung der Kamera.
     * @param resY Vertikale Aufl�sung der Kamera.
     */
    public FirstCamera(int resX, int resY)
    {
        super(resX, resY);
        
        factor = (resX < resY) ? 1.0/ (double) resX : 1.0/ (double) resY;
        offsetX = -factor* (double) resX /2.0;
        offsetY = -factor* (double) resY /2.0;
    }
    
    
    @Override
    public ColorEx getColor(int x, int y)
    {
        return getColor((double) x +0.5, (double) y +0.5);
    }
    
    @Override
    public ColorEx getColor(double x, double y)
    {
        if ((x < 0.0) || (x > (double) resX) || (y < 0.0) || (y > (double) resY))
            throw new IndexOutOfBoundsException();
        
        Ray ray = new Ray();
        ray.org.set(org);
        ray.dir.x = offsetX+((double) resX -x)*factor;
        ray.dir.y = offsetY+((double) resY -y)*factor;
        ray.dir.z = 1.0;
        
        // Farbwert zur�ckgeben. Darauf achten, dass er den G�ltigkeitsbereich
        // nicht �berschreitet:
        ColorEx color = scene.trace(ray);
        return color;
    }
}