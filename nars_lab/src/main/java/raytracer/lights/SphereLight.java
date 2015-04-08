/*
 * SphereLight.java                       STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.lights;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.*;
import raytracer.objects.Shape;
import raytracer.objects.Sphere;
import raytracer.shader.Shader;
import raytracer.util.FloatingPoint;

import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Dieses Licht besitzt die Form einer Kugel und ist in der Szene sichtbar.
 * 
 * @author Jonas Stahl
 * @author Mathias Kosch
 *
 */
public class SphereLight extends Light implements Shader
{
    /** Objekt, das dieses Licht visuell darstellt. */
    protected Sphere sphere;
    
    /** Farbe und Intesit�t dieses Lichts. */
    protected ColorEx lightColor = new ColorEx();
    
    /** Mittelpunkt der kugelf�rmigen Lichtquelle. */
    protected Vector3d lightCenter = new Vector3d();
    /** Radius dieser kugelf�rmigen Lichtquelle. */
    protected final double lightRadius;
    
    /** Punkt, zu dem die Beleuchtung bestimmt werden soll. */
    protected Vector3d point = new Vector3d();
    /** Anzahl der Strahlen, die insgesamt zu <code>point</code> gesendet werden. */
    protected int estimatedRaysToSend = 0;
    /** Anzahl der Strahlen, die noch zu <code>point</code> gesendet werden sollen. */
    protected int raysToSend = 0;
    /** Gibt an, um welchen Faktor die zu sendenden Strahlen erh�ht werden. */
    protected int currentIncreaseCount;
    /** L�nge des zuletzt gesendeten Lichtstrahls. */
    protected float lastRayLength;
    
    /** Achse 1 der Ebene durch den Licht-Vektor. */
    protected Vector3d axis1 = new Vector3d();
    /** Achse 2 der Ebene durch den Licht-Vektor. */
    protected Vector3d axis2 = new Vector3d();
    
    
    
    
    /**
     * Erzeugt eine neue kurgelf�rmige Lichtquelle.
     * 
     * @param center Mittelpunkt der Lichtquelle.
     * @param radius Radius der Lichtquelle.
     * @param color Farbe und Intensit�t der Lichtquelle.
     */
	public SphereLight(Vector3d center, double radius, ColorEx color)
    {
		lightCenter.set(center);
		lightRadius = radius;
        lightColor.set(color);
        
        // Sichtbares Objekt zur Szene hinzuf�gen:
        sphere = new Sphere(lightCenter, lightRadius, this);
        sphere.isLight = true;
	}
    
    @Override
    public SphereLight clone()
    throws CloneNotSupportedException
    {
        SphereLight clone = (SphereLight)super.clone();
        
        clone.sphere = sphere.clone();
        clone.lightColor = new ColorEx(lightColor);
        clone.lightCenter = new Vector3d(lightCenter);
        clone.point = new Vector3d(point);
        clone.axis1 = new Vector3d(axis1);
        clone.axis2 = new Vector3d(axis2);

        return clone;
    }
    

	@Override
    public boolean isIlluminated(Vector3d point)
    {
        return true;
	}
    
    
    @Override
    public void startRay(Vector3d point)
    {
        // Bestimme, wie viele Strahlen vom aktuellen Punkt aus zu der Lichtquelle
        // geschickt werden sollen:
        if (RaytracerConstants.SOFT_SHADOWS_ENABLED)
        {
            this.point.sub(lightCenter, point);
            double length = this.point.length();
            estimatedRaysToSend = (int)Math.ceil(lightRadius*lightRadius/(4.0*length*length)* (double) RaytracerConstants.RAYS_PER_UNIT_SPHERE);
            if (estimatedRaysToSend < RaytracerConstants.MIN_RAYS_PER_SOFT_LIGHT)
                estimatedRaysToSend = RaytracerConstants.MIN_RAYS_PER_SOFT_LIGHT;
            if (estimatedRaysToSend > RaytracerConstants.MAX_RAYS_PER_SOFT_LIGHT)
                estimatedRaysToSend = RaytracerConstants.MAX_RAYS_PER_SOFT_LIGHT;
            raysToSend = estimatedRaysToSend;
            currentIncreaseCount = 0;
        }
        else
        {
            estimatedRaysToSend = 0;
            raysToSend = 1;
        }
        
        // Punkt festlegen, f�r den Strahlen zum Licht generiert werden:
        this.point.set(point);
        
        // Orthogonale aufspannende Vektoren der Ebene bestimmen, deren Normale
        // der Richtungsvektor zum Punkt ist.
        // Diese Ebene umfasst die sichtbare Kreisfl�che der Kugel.
        axis2.sub(lightCenter, point);
        
        // 1. Vektor bestimmen:
        axis1.x = 1.0; axis1.y = 0.0; axis1.z = 0.0;
        axis1.cross(axis1, axis2);
        if ((axis1.x == 0.0) && (axis1.y == 0.0) && (axis1.z == 0.0))
        {
            // Beide Vektoren sind linear abh�ngig. Versuche es erneut:
            axis1.z = 1.0;
            axis1.cross(axis1, axis2);
        }
        
        // 2. Vektor bestimmen:
        axis2.cross(axis1, axis2);
        
        // Aufspannende Vektoren auf die halbe L�nge des Radiuses normieren:
        axis1.scale(lightRadius/axis1.length());
        axis2.scale(lightRadius/axis2.length());
    }
    
    @Override
    public void increaseNumberOfRays()
    {
        while (currentIncreaseCount++ < RaytracerConstants.SOFT_LIGHT_RAY_INCREASE_COUNT)
            raysToSend += estimatedRaysToSend;
    }

	@Override
    public Ray genRay()
    {
        // Pr�fen, ob noch Strahlen zu senden sind:
        if (raysToSend <= 0)
            return null;
        raysToSend--;
        
        Ray ray = new Ray(point, lightCenter);
        
        if (RaytracerConstants.SOFT_SHADOWS_ENABLED)
        {
            // Bestimme einen beliebigen (gleich verteilten) Punkt auf dem
            // Einheitskreis:
            double x, y;
            do
            {
                x = FloatingPoint.nextRandom()*2.0-1.0;
                y = FloatingPoint.nextRandom()*2.0-1.0;
            }
            while (x*x+y*y > 1.0);
            
            // Zuf�lligen Punkt auf der sichtbaren Kreisfl�che der Kugel erzeugen:
            ray.dir.scaleAdd(x, axis1, ray.dir);
            ray.dir.scaleAdd(y, axis2, ray.dir);
        }
        ray.dir.sub(point);
        ray.length = 1.0;
        
        // Entfernung des betrachteten Lichtpunktes merken:
        lastRayLength = (float)ray.dir.length();
        
        ray.ignoreLights = true;
        return ray;
	}
    
    @Override
    public ColorEx getIlluminance()
    {
        // Lichtabschw�chung berechnen:
        ColorEx color = new ColorEx(lightColor);
        color.scale(1.0f /(RaytracerConstants.LIGHT_ATTENUATION_CONSTANT +
                RaytracerConstants.LIGHT_ATTENUATION_LINEAR*lastRayLength +
                RaytracerConstants.LIGHT_ATTENUATION_QUADRATIC*lastRayLength*lastRayLength));
        return color;
    }

    
    @Override
    public void getShapes(Collection<Shape> shapes)
    {
        sphere.getShapes(shapes);
    }
    
    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {
        sphere.getBoundingPoints(points);
    }
    
    
    @Override
    public void transform(Transformation t)
    {
        sphere.transform(t);
        t.transformPoint(lightCenter);
        t.transformVector(axis1);
        t.transformVector(axis2);
    }
    
    
    @Override
    public boolean intersect(Ray ray)
    {
        return sphere.intersect(ray);
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return sphere.occlude(ray);
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        sphere.display(drawable);
    }
    
    
    @Override
    public ColorEx shade(Intersection intersection)
    {
        return new ColorEx(lightColor);
    }
    
    
/*
	public boolean preTest(Intersection intersection, Vector3d org) {
		
		Ray ray = new Ray();
		Vector3d r = new Vector3d();
		Vector3d nXr = new Vector3d();
		Vector3d n = new Vector3d();
		lightArea = false;
		double t;
		n.sub(org,center);
		
		//Bestimmung der Ebene, auf die die Kugel reduziert wird.
		for(;;) {
			t = Math.random()*2-1;
			r.x = t;
			t = Math.random()*2-1;
			r.y = t;
			t = Math.random()*2-1;
			r.z = t;
			nXr.cross(n,r);
			if( nXr.x == 0 && nXr.y == 0 && nXr.z == 0) continue;
			else break;
		} 
		
		dir1.cross(nXr,n);
		dir1.normalize();
		dir1.scale(radius);
		
		dir2.cross(n, dir1);
		dir2.normalize();
		dir2.scale(radius);
		
		if(!RaytracerConstants.SOFT_SHADOWS_ENABLED) return true;
		
		int count  = 0;
		ray.org.set(org);
		for(int i=0 ; i<4 ; i++) {
			
			//4 Punkte auf der H�lle der Kugel werden getestet.
			if(i==0) ray.dir.add(center, dir1);
			if(i==1) ray.dir.add(center, dir2);
			if(i==2) ray.dir.scaleAdd(-1, dir1, center);
			if(i==3) ray.dir.scaleAdd(-1, dir2, center);
			ray.dir.sub(ray.dir, org);
			//ray = genRay(intersection.getPoint());
			
    		if (!intersection.scene.occlude(ray)) {
    			count++;
    		}
    		else {
    			count--;
    		}
		}
		if(count == -4){
			return false;
		}
		if(count == 4){
			lightArea = true;
		}
				
		return true;
	}
*/
}