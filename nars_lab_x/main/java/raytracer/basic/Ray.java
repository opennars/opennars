/*
 * Ray.java                               STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 * �NDERUNGEN:
 * [*] 'dir' ist nicht mehr normalisiert.
 * 
 */

package raytracer.basic;

import raytracer.objects.Shape;

import javax.vecmath.Vector3d;
import java.util.Stack;

/**
 * Strahlen werden in einer Szene umher geschossen, um Farbinformationen
 * der von ihnen getroffenen Objekte zu ermitteln.
 * 
 * <h3>L�nge des Strahls</h3>
 * Ein Strahl hat zu Beginn eine unendliche L�nge. Trifft er auf ein Objekt,
 * wird seine L�nge so verk�rzt, dass er genau bis zum Schnittpunkt reicht.
 * 
 * @author Mathias Kosch
 *
 */
public class Ray
{
	/** Ursprung des Strahls. */
	public final Vector3d org = new Vector3d();
	/** Richtung des Strahls. */
	public final Vector3d dir = new Vector3d();

    /** Anzahl der Objekte, die ein fortgef�hrter Strahl bereits getroffen hat. */
    public int hitCount = 0;
    /** L�nge des Weges, den ein fortgef�hrter Strahl bereits zur�ck gelegt hat. */
    public double hitLength = 0.0;
    
	/** L�nbge des Strahls. */
	public double length = Double.POSITIVE_INFINITY;

	/** Objekt, das aktuell vom Strahl getroffen wurde. */
    public Shape hit = null;
    /** Objekt, das vor dem aktuellen Objekt vom Strahl getroffen wurde. */
    public Shape prevHit = null;
    
    /** Stack aller Lichtgeschwindigkeiten der durchquerten Materialien. */
    public final Stack<Float> refractionStack = new Stack<Float>();

    /** Gewichtung dieses Strahls. */
    public float weight = 1.0f;
    
    /** Gibt an, ob der Strahl keine sichtbaren Lichtquellen schneidet. */
    public boolean ignoreLights = false;
    
    
    /**
     * Erzeugt einen neuen Strahl mit Standard-Werten.
     *
     */
    public Ray()
    {
    }

    /**
     * Erzeugt einen neuen Strahl aus einem bereits bestehenden Strahl.<br>
     * Der erzeugte Strahl ist identisch mit <code>ray</code>.
     * 
     * @param ray Strahl, dessen Attribute �bernommen werden sollen.
     */
    public Ray(Ray ray)
    {
        set(ray);
    }

    /**
     * Erzeugt einen neuen Strahl.
     * 
     * @param org Ursprung des neuen Strahls.
     * @param dir Richtung des neuen Strahls.
     */
    public Ray(Vector3d org, Vector3d dir)
    {
        this.org.set(org);
        this.dir.set(dir);
    }

	/**
     * Erzeugt einen neuen Strahl.
     * 
     * @param org Ursprung des neuen Strahls.
     * @param dir Richtung des neuen Strahls.
     * @param length L�nge des neuen Strahls.
	 */
	public Ray(Vector3d org, Vector3d dir, double length)
    {
        this.org.set(org);
        this.dir.set(dir);
		this.length = length*dir.length();
	}
    
    
    /**
     * Erzeugt einen neuen Strahl, der einen bereits bestehenden Strahl
     * fortf�hrt.<br>
     * Der neue Strahl beginnt dort, wo der alte endet und hat die selbe
     * Richtung wie der alte Strahl.<br>
     * <br>
     * Diese Methode muss verwendet werden, falls beim Schnitt eines Strahles
     * mit einer Fl�che neue Strahlen generiert werden. Falls der Strahl bereits
     * zu lange unterwegs war, wird <code>null</code> zur�ckgegeben.
     * 
     * @param ray Bestehender Strahl, der fortgesetzt werden soll.
     * @return Neuen Strahl, der <code>ray</code> fortsetzt, oder <code>null</code>.
     */
    public static Ray continueRay(Ray ray)
    {
        return continueRay(ray, ray.getEnd(), ray.dir);
    }
    
    /**
     * Erzeugt einen neuen Strahl, der einen bereits bestehenden Strahl
     * fortf�hrt.<br>
     * <br>
     * Diese Methode muss verwendet werden, falls beim Schnitt eines Strahles
     * mit einer Fl�che neue Strahlen generiert werden. Falls der Strahl bereits
     * zu lange unterwegs war, wird <code>null</code> zur�ckgegeben.
     * 
     * @param ray Bestehender Strahl, der fortgesetzt werden soll.
     * @param org Ursprung des fortf�hrenden Strahls.
     * @param dir Richtung des fortf�hrenden Strahls.
     * @return Neuen Strahl, der <code>ray</code> fortsetzt, oder <code>null</code>.
     */
    public static Ray continueRay(Ray ray, Vector3d org, Vector3d dir)
    {
        if (ray.ignoreLights)
            throw new IllegalStateException();
        
        int hitCount = ray.hitCount+1;
        double hitLength = ray.hitLength+ray.getNormalizedLength();
        
        if (hitCount >= RaytracerConstants.MAX_RAY_HITCOUNT)
            return null;
        if (hitLength >= RaytracerConstants.MAX_RAY_WAY)
            return null;
        
        Ray result = new Ray();
        result.org.set(org);
        result.dir.set(dir);
        result.hitLength = hitLength;
        result.hitCount = hitCount;
        result.prevHit = ray.hit;      
        result.refractionStack.addAll(ray.refractionStack);
        result.weight = ray.weight;
        return result;
    }
    
    
    /**
     * Ermittelt das Ende des Strahls.<br>
     * Ein Strahl endet dort, wo er auf ein Objekt trifft.
     * 
     * @return Punkt, wo der Strahl endet.
     */
    public Vector3d getEnd()
    {
        Vector3d point = new Vector3d();
        
        // ray.org + ray.dir*ray.length()
        point.scaleAdd(length, dir, org);
        return point;
    }
    
    /**
     * Ermittelt die tats�chliche L�nge des Strahls in L�ngeneinheiten.
     * 
     * @return Tats�chliche L�nge des Strahls in L�ngeneinheiten.
     */
    public double getNormalizedLength()
    {
        return length*dir.length();
    }
    
    
    /**
     * Weist dem Strahl die Werte eines anderen Rays zu.<br>
     * Nach dieser Operation ist dieser Strahl identisch mit <code>ray</code>.
     * 
     * @param ray Strahl, dessen Werte �bernommen werden sollen.
     */
    public void set(Ray ray)
    {
        org.set(ray.org);
        dir.set(ray.dir);
        length = ray.length;
        hitCount = ray.hitCount;
        hitLength = ray.hitLength;
        hit = ray.hit;
        prevHit = ray.prevHit;
        weight = ray.weight;
        ignoreLights = ray.ignoreLights;
        
        refractionStack.clear();
        refractionStack.addAll(ray.refractionStack);
    }
}