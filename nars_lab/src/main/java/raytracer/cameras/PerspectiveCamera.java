/*
 * PerspectiveCamera.java                          STATUS: In Bearbeitung
 * ----------------------------------------------------------------------
 * 
 * �NDERUNGEN:
 * [*] 'angle' ist der �ffnungswinkel des einschlie�enden Quadrats.
 * [*] 's' ist die Gr��e des inneren Quadrats des Ausgangspunkts der Strahlen.
 * 
 */

//TODO: Auch negative Alpha-Werte (im g�ltigen Bereich) zulassen!

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
public class PerspectiveCamera extends AsyncCamera
{
    private final Vector3d pos = new Vector3d(0.0, 0.0, 0.0);
    private double s = 0.0;
    private double angle = 10.0;
    
    
    
    private final Vector3d o = new Vector3d();
    /** X-Vektor des Kamerabildes (ist orthogonal zu DIR und UP). */
    private final Vector3d x = new Vector3d();
    /** Y-Vektor des Kamerabildes (ist orthogonal zu X und DIR). */
    private final Vector3d y = new Vector3d();
    private final Vector3d z = new Vector3d();
    private final Vector3d o2 = new Vector3d();
    private double s2;
    private double factor;
    
    
    /**
     * Erzeugt eine neue Perspektivische Kamera.
     * 
     * @param resX Horizontale Aufl�sung der Kamera.
     * @param resY Vertikale Aufl�sung der Kamera.
     * @param angle �ffnungswinkel der Kamera.
     * @param pos Position der Kamera.
     * @param dir Richtung, in die die Kamera zeigt.
     * @param up Vektor, der angibt, wo oben ist.
     */
    public PerspectiveCamera(int resX, int resY, double angle,
            Vector3d pos, Vector3d dir, Vector3d up)
    {
        this(resX, resY, 0.0, angle, pos, dir, up);
    }
    
    /**
     * Erzeugt eine neue Perspektivische Kamera.
     * 
     * @param resX Horizontale Aufl�sung der Kamera.
     * @param resY Vertikale Aufl�sung der Kamera.
     * @param s Gr��e des Filmes. Je gr��er dieser Wert ist, desto orthogonaler
     *        wird die Kamera.
     * @param angle �ffnungswinkel der Kamera.
     * @param pos Position der Kamera.
     * @param dir Richtung, in die die Kamera zeigt.
     * @param up Vektor, der angibt, wo oben ist.
     */
    public PerspectiveCamera(int resX, int resY, double s, double angle,
            Vector3d pos, Vector3d dir, Vector3d up)
    {
        super(resX, resY);
        
        // Sicher stellen, dass 's' und 'angle' im g�ltigen Wertebereich liegen:
        if ((s < 0.0) || (angle < 0.0) || (angle >= 180.0))
            throw new IllegalArgumentException();
        
        this.s = s;
        this.angle = angle*Math.PI/ 180.0;
        
        s2 = s+Math.tan(this.angle/2.0)*2.0;
        factor = (resX < resY) ? 1.0/ (double) resX : 1.0/ (double) resY;
        
        // Position und Ausrichtung festlegen:
        setPosition(pos);
        setDirection(dir, up);
    }
    
    
    public void setPosition(Vector3d pos)
    {
        // Neue Position �bernehmen:
        this.pos.set(pos);
        
        // Zweidimensionales Koordinatensysteme berechnen, die die
        // Ursprungsorte und Richtungsvektoren der Strahlen angeben:
        computeO();
    }
    
    public void setDirection(Vector3d dir, Vector3d up)
    {
        // X-Vektor des Kamerabildes berechnen:
        Vector3d x = new Vector3d();
        x.cross(dir, up);
        
        // Sicher stellen, dass ein eindeutiger X-Vektor existiert:
        if ((x.x == 0.0) && (x.y == 0.0) && (x.z == 0.0))
            throw new IllegalArgumentException();
        
        // Y-Vektor des Kamerabildes berechnen:
        this.y.cross(x, dir);
        this.x.set(x);
        
        // Z-Vektor des Kamerabildes berechnen:
        this.z.set(dir);
        
        // X, Y und Z-Vektoren normalisieren:
        this.x.normalize();
        this.y.normalize();
        this.z.normalize();
        
        // Zweidimensionales Koordinatensysteme berechnen, die die
        // Ursprungsorte und Richtungsvektoren der Strahlen angeben:
        computeO();
    }
    
    private void computeO()
    {
        Vector3d v = new Vector3d();
        
        o.set(pos);
        v.set(x);
        v.scale(factor*s* (double) resX /2.0);
        o.sub(v);
        v.set(y);
        v.scale(factor*s* (double) resY /2.0);
        o.sub(v);
        
        o2.set(z);
        o2.add(pos);
        v.set(x);
        v.scale(factor*s2* (double) resX /2.0);
        o2.sub(v);
        v.set(y);
        v.scale(factor*s2* (double) resY /2.0);
        o2.sub(v);
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
        
        Vector3d v = new Vector3d();
        Ray ray = new Ray();
        
        // Ursprung des Stahles berechnen:
        v.scaleAdd(factor*s*x, this.x, o);
        ray.org.scaleAdd(factor*s*((double) resY -y), this.y, v);
        
        // Richtung des Strahles berechnen:
        v.scaleAdd(factor*s2*x, this.x, o2);
        ray.dir.scaleAdd(factor*s2*((double) resY -y), this.y, v);
        ray.dir.sub(ray.org);

        // Farbwert zur�ckgeben. Darauf achten, dass er den G�ltigkeitsbereich
        // nicht �berschreitet:
        ColorEx color = scene.trace(ray);
        return color;
    }
}