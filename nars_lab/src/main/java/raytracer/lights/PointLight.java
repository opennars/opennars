/*
 * PointLight.java                        STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.lights;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.ColorEx;
import raytracer.basic.Ray;
import raytracer.basic.RaytracerConstants;
import raytracer.basic.Transformation;
import raytracer.objects.Shape;

import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Dieses Licht ist punktf�rmig und nicht in der Szene sichtbar.
 * 
 * @author Sassan Torabi-Goudarzi
 * @author Jonas Stahl
 * 
 */
public class PointLight extends Light
{
    /** Farbe und Intensit�t des Lichts. */
    protected ColorEx lightColor = new ColorEx();
    
	/** Position der Punktlichtquelle. */
    protected Vector3d lightPoint = new Vector3d();
    
    /** Punkt, zu dem die Beleuchtung bestimmt werden soll. */
    protected Vector3d point = new Vector3d();
    /** Gibt an, ob noch ein Strahl zu <code>point</code> gesendet werden soll. */
    protected boolean rayToSend = false;
	
    
	/**
     * Erzeugt eine neue Punktlichtquelle.
     * 
	 * @param pos Position der Punktlichtquelle.
	 * @param color Farbe und Intensit�t des Lichts.
     */
	public PointLight(Vector3d pos, ColorEx color)
	{
        lightPoint.set(pos);
		lightColor.set(color);
	}

    @Override
    public PointLight clone()
    throws CloneNotSupportedException
    {
        PointLight clone = (PointLight)super.clone();
        
        clone.lightColor = new ColorEx(lightColor);
        clone.lightPoint = new Vector3d(lightPoint);
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
        
        Ray ray = new Ray(point, lightPoint);
        ray.dir.sub(point);
        ray.length = 1.0;
        
        ray.ignoreLights = true;
        return ray;
    }
    
    @Override
    public ColorEx getIlluminance()
    {
        Vector3d v = new Vector3d();
        v.sub(lightPoint, point);
        float length = (float)v.length();

        // Lichtabschw�chung berechnen:
        ColorEx color = new ColorEx(lightColor);
        color.scale(1.0f /(RaytracerConstants.LIGHT_ATTENUATION_CONSTANT +
                RaytracerConstants.LIGHT_ATTENUATION_LINEAR*length +
                RaytracerConstants.LIGHT_ATTENUATION_QUADRATIC*length*length));
        return color;
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
        t.transformPoint(lightPoint);
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