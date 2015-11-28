/*
 * AreaLight.java                         STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.lights;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.*;
import raytracer.objects.Shape;
import raytracer.objects.Triangle;
import raytracer.shader.Shader;

import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Dieses Licht besitzt die Form einer Raute und ist in der Szene sichtbar.
 * 
 * @author Jonas Stahl
 * @author Mathias Kosch
 *
 */
public class AreaLight extends Light implements Shader
{
    /** Objekt 1, das dieses Licht visuell darstellt. */
    protected Triangle triangle1;
    /** Objekt 2, das dieses Licht visuell darstellt. */
    protected Triangle triangle2;
    
    /** Farbe und Intesit�t dieses Lichts. */
    protected ColorEx lightColor = new ColorEx();
    
    /** Mittelpunkt der Raute, die dieses Licht darstellt. */
    protected Vector3d lightCenter = new Vector3d();
    /** Richtungsvektor 1, der die Dimension der Raute angibt. */
    protected Vector3d lightDim1 = new Vector3d();
    /** Richtungsvektor 2, der die Dimension der Raute angibt. */
    protected Vector3d lightDim2 = new Vector3d();
    /** Fl�cheninhalt der Raute. */
    protected final double lightArea;
    
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
    
	
	/**
     * Erzeugt eine neue (beidseitige) fl�chige Lichtquelle in Form einer Raute.
     * 
     * @param center Mittelpunkt des rautenf�rmigen Lichts.
     * @param dim1 Richtungsvektor 1, der die Dimension der Raute angibt.
     * @param dim2 Richtungsvektor 2, der die Dimension der Raute angibt.
     * @param color Farbe und Intensit�t des Lichts.
	 */
	public AreaLight(Vector3d center, Vector3d dim1, Vector3d dim2, ColorEx color)
    {
		lightCenter.set(center);
		lightDim1.set(dim1);
		lightDim2.set(dim2);
        lightColor.set(color);
        
        // Bestimmung der Fl�che des Objekts:
        Vector3d cross = new Vector3d();
        cross.cross(dim1, dim2);
        lightArea = 4.0*cross.length();
        
        // Sichtbares Objekt zur Szene hinzuf�gen:
        Vector3d  v1 = new Vector3d(lightCenter);
        v1.sub(lightDim1);
        v1.sub(lightDim2);
        Vector3d  v2 = new Vector3d(lightCenter);
        v2.add(lightDim1);
        v2.add(lightDim2);
        Vector3d  v = new Vector3d();
        v.add(lightCenter, lightDim1);
        v.sub(lightDim2);
        triangle1 = new Triangle(v1, v, v2, this);
        triangle1.isLight = true;
        v.sub(lightCenter, lightDim1);
        v.add(lightDim2);
        triangle2 = new Triangle(v1, v, v2, this);
        triangle2.isLight = true;
	}
    
    @Override
    public AreaLight clone()
    throws CloneNotSupportedException
    {
        AreaLight clone = (AreaLight)super.clone();
        
        clone.triangle1 = triangle1.clone();
        clone.triangle2 = triangle2.clone();
        clone.lightColor = new ColorEx(lightColor);
        clone.lightCenter = new Vector3d(lightCenter);
        clone.lightDim1 = new Vector3d(lightDim1);
        clone.lightDim2 = new Vector3d(lightDim2);
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
        // Bestimme, wie viele Strahlen vom aktuellen Punkt aus zu der Lichtquelle
        // geschickt werden sollen:
        if (RaytracerConstants.SOFT_SHADOWS_ENABLED)
        {
            this.point.sub(lightCenter, point);
            double length = this.point.length();
            estimatedRaysToSend = (int)Math.ceil(lightArea/(4.0*Math.PI*length*length)* (double) RaytracerConstants.RAYS_PER_UNIT_SPHERE);
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
            // Zuf�lligen Punkt auf der Raute erzeugen und Strahl darauf richten:
            ray.dir.scaleAdd(Math.random()*2.0-1.0, lightDim1, ray.dir);
            ray.dir.scaleAdd(Math.random()*2.0-1.0, lightDim2, ray.dir);
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
        triangle1.getShapes(shapes);
        triangle2.getShapes(shapes);
    }
    
    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {
        triangle1.getBoundingPoints(points);
        triangle2.getBoundingPoints(points);
    }
    
    
    @Override
    public void transform(Transformation t)
    {
        triangle1.transform(t);
        triangle2.transform(t);
        t.transformPoint(lightCenter);
        t.transformVector(lightDim1);
        t.transformVector(lightDim2);
    }
    
    
    @Override
    public boolean intersect(Ray ray)
    {
        return triangle1.intersect(ray) | triangle2.intersect(ray);
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return triangle1.occlude(ray) || triangle2.occlude(ray);
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        triangle1.display(drawable);
        triangle2.display(drawable);
    }
    
    
    @Override
    public ColorEx shade(Intersection intersection)
    {
        return new ColorEx(lightColor);
    }
    
    
    
    
/*
	public boolean preTest(Intersection intersection, Vector3d org) {
		//Test ob Punkt "oberhalb" der Lichtquelle liegt
		double skalar;
		Vector3d v = new Vector3d();
		Vector3d n = new Vector3d();
		
		v.sub(org,center);
		n.cross(dir1,dir2);
		
		skalar = v.dot(n);
		
		if(skalar > 0) return true;
		
		if(!RaytracerConstants.SOFT_SHADOWS_ENABLED) return true;
		
		//sendet 4 Teststrahlen, um Kernschatten und Lichtfl�che sofort auszusortieren
		Ray ray = new Ray();
		ray.org.set(org);
		int count = 0;
		for(int i=0 ; i<4 ; i++) {
			
			//Die 4 Eckpunkte des Parallelogramms werden getestet
			if(i==0) {
				ray.dir.add(center, dir1);
				ray.dir.add(dir2);
			}
			if(i==1) {
				ray.dir.add(center, dir1);
				ray.dir.scaleAdd(-1, dir2);
			}
			if(i==2) {
				ray.dir.add(center, dir2);
				ray.dir.scaleAdd(-1, dir1);
			}
			if(i==3) {
				ray.dir.scaleAdd(-1, dir1, center);
				ray.dir.scaleAdd(-1, dir2);
			}
			ray.dir.sub(ray.dir, org);
			//ray = genRay(intersection.getPoint());
			
    		if (!intersection.scene.occlude(ray)) {
    			count++;
    		}
    		else {
    			count--;
    		}
		}
		
		// Punkt liegt "wahrscheinlich im Schatten
		if(count == -4){
			return false;
		}
		//Punkt wird "wahrscheinlich" nicht von Objekt verdeckt
		if(count == 4){
			lightArea = true;
		}
		
		//Punkt wird teilweise verdeckt
		return false;
	}
*/
}