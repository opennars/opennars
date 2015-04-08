/*
 * DirectionalLight.java                  STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.lights;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.ColorEx;
import raytracer.basic.Ray;
import raytracer.basic.Transformation;
import raytracer.objects.Shape;

import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Dieses Licht ist gerichtet und unendlich weit entfernt.
 * 
 * @author Sassan Torabi-Goudarzi
 * @author Jonas Stahl
 * 
 */
public class DirectionalLight extends Light
{
    /** Farbe und Intensit�t des Lichts. */
    protected ColorEx lightColor = new ColorEx();
    
	/** Richtung, in die das Licht leuchtet. */
    protected Vector3d lightDir = new Vector3d();
    
    /** Punkt, zu dem die Beleuchtung bestimmt werden soll. */
    protected Vector3d point = new Vector3d();
    /** Gibt an, ob noch ein Strahl zu <code>point</code> gesendet werden soll. */
    protected boolean rayToSend = false;
	
    
	/**
     * Erzeugt ein neues gerichtetes Licht.
     * 
	 * @param dir Richtung, in die das Licht leuchtet.
	 * @param color Farbe und Intensit�t des Lichts.
	 * */
	public DirectionalLight(Vector3d dir, ColorEx color)
	{
		lightDir.set(dir);
		lightColor.set(color);
	}
    
    @Override
    public DirectionalLight clone()
    throws CloneNotSupportedException
    {
        DirectionalLight clone = (DirectionalLight)super.clone();
        
        clone.lightColor = new ColorEx(lightColor);
        clone.lightDir = new Vector3d(lightDir);
        clone.point = new Vector3d(point);

        return clone;
    }
	
    
	@Override
    public boolean isIlluminated(Vector3d point)
	{
        // Jeder Punkt wird beleuchtet:
        return true;
	}


    @Override
    public void startRay(Vector3d point)
    {
        // Punkt festlegen, f�r den Strahlen zum Licht generiert werden:
        this.point.set(point);
        rayToSend = true;
    }
    
    @Override
    public void increaseNumberOfRays()
    {
        
    }
    
    @Override
    public Ray genRay()
    {
        // F�r jeden Punkt genau einen Strahl setzen:
        if (!rayToSend)
            return null;
        rayToSend = false;
        
        Ray ray = new Ray(point, lightDir);
        ray.dir.negate();
        
        ray.ignoreLights = true;
        return ray;
    }
    
    @Override
    public ColorEx getIlluminance()
    {
        // Dieses Licht ist sehr weit entfernt, sodass die Lichtabschw�chung
        // keine Rolle mehr spielt:
        return new ColorEx(lightColor);
    }
    
    
    @Override
    public void getShapes(Collection<Shape> shapes)
    {

    }
    
    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {

    }
    
    
    @Override
    public void transform(Transformation t)
    {
        t.transformVector(lightDir);
    }
    
    
    @Override
    public boolean intersect(Ray ray)
    {
        return false;
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return false;
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {

    }
}