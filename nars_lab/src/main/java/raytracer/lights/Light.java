/*
 * PointLight.java                        STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.lights;

import raytracer.basic.ColorEx;
import raytracer.basic.Ray;
import raytracer.objects.SceneObject;

import javax.vecmath.Vector3d;

/**
 * Lichter erhellen die Szene.
 * 
 * @author Sassan Torabi-Goudarzi
 * @author Mathias Kosch
 *
 */
abstract public class Light extends SceneObject
{
    @Override
    public Light clone()
    throws CloneNotSupportedException
    {
        return (Light)super.clone();
    }
    
    
	/**
	 * Stellt fest, ob ein Punkt von diesem Licht beleuchtet wird.
     * 
     * @param point Punkt, von dem erfragt wird, ob er beleuchtet ist.
     * @return <code>true</code>, falls der Punkt an <code>point</code> von
     *         diesem Licht beleuchtet wird. Andernfalls <code>false</code>.
	 */
	abstract public boolean isIlluminated(Vector3d point);
	
	
    /**
     * Beginnt eine neue Gruppe von Strahlen zu diesem Licht.
     * 
     * @param point Punkt, von dem die Strahlen ausgehen.
     */
    abstract public void startRay(Vector3d point);
    
    /**
     * Erzeugt den n�chsten Strahl vom mittels <code>startRay</code>
     * festgelegten Ausgangspunkt zu diesem Licht.<br>
     * Falls das Licht gen�gend Strahlen von dem Ausgangspunkt erzeugt hat,
     * ist der R�ckgabewert <code>null</code>. In diesem Falle kann mittels
     * <code>increaseNumberOfRays</code> versucht werden, noch mehr Strahlen
     * anzufordern. Ist der R�ckgabewert anschlie�end jedoch wieder
     * <code>null</code>, so sind keine weiteren Strahlen in dieser Gruppe
     * m�glich.
     * 
     * @return Erzeugter Strahl zu diesem Licht, oder <code>null</code>.
     */
    abstract public Ray genRay();
    
    /**
     * Versucht, die Anzahl m�glicher Strahlen in dieser Gruppe (seit Aufruf von
     * <code>startRay</code>) zu erh�hen.
     */
    abstract public void increaseNumberOfRays();

    /** 
     * Ermittelt die Lichtst�rke an dem Punkt, auf den der zuvor mittels
     * <code>genRay</code> generierte Strahl zeigt.
     * 
     * @return Lichtst�rke zu dem zuvor generierten Lichtstrahl.
     */
    abstract public ColorEx getIlluminance();
}