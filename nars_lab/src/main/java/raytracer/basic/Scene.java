/*
 * Scene.java                             STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.basic;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.lights.Light;
import raytracer.objects.SceneObject;

import java.util.Collection;
import java.util.List;

/**
 * Eine Szene ist eine Ansammlung von Objekten mit bestimmten Eigenschaften.<br>
 * Mit Hilfe einer <code>Camera</code> k�nnen Bilder von einer Szene gerendert
 * werden.
 * 
 * @author Mathias Kosch
 *
 */
abstract public class Scene
{
    /** Ambientes Licht in dieser Szene. */
    protected final ColorEx ambientLight = new ColorEx(ColorEx.BLACK);
    
    /** Hintergrundfarbe dieser Szene. */
    protected final ColorEx backgroundColor = new ColorEx(ColorEx.BLACK);
    
    
    /**
     * F�gt ein Szenen-Objekt zu dieser Szene hinzu.
     * 
     * @param object Szenen-Objekt, das hinzu gef�gt werden soll.
     */
    abstract public void add(SceneObject object);
    
    /**
     * F�gt eine Menge von Szenen-Objekten zu dieser Szene hinzu.
     * 
     * @param objects Menge von Szenen-Objekten, die hinzu gef�gt werden soll.
     */
    public void addAll(Collection<SceneObject> objects)
    {
        for (SceneObject object : objects) add(object);
    }
    
    /**
     * Erzeugt einen Iterator �ber alle Lichter dieser Szene.
     * 
     * @return Iterator �ber alle Lichter dieser Szene.
     */
    abstract public List<Light> getLights();
    
    
	/**
     * Verfolgt einen Strahl in dieser Szene zur�ck und ermittelt den Farbwert.<br>
     * Dabei wird der Strahl so verk�rzt, dass er auf das zuerst getroffene
     * Szenen-Objekt zeigt.<br>
     * Falls der Strahl nicht mehr gen�gend Wichtigkeit hat, so wird er jedoch
     * verworfen.<br>
     * <br>
     * Falls ein Strahl nicht weiter verfolgt wird oder kein Objekt geschnitten
     * wird, so wird die Hintergrundfarbe der Szene zur�ck gegeben.
     * 
     * @param ray Strahl, der durch diese Szene verfolgt wird.
     * @return Farbwert an dem Auftreffpunkt des Strahls.
	 */
    abstract public ColorEx trace(Ray ray);
    
    /**
     * Ermittelt, ob ein gegebener Strahl weiter verfolgt werden soll.
     * 
     * @param ray Strahl.
     * @return <code>true</code>, falls <code>ray</code> weiter verfolgt werden
     *         soll. Andernfalls <code>false</code>.
     */
    public static boolean followRay(Ray ray)
    {
        return (double) ray.weight >= RaytracerConstants.MIN_RAY_WEIGHT;
    }
	
	/**
     * Ermittelt, ob ein Objekt dieser Szene von einem Strahl geschnitten wird.
     * 
     * @param ray Strahl, der in diese Szene geschickt wird.
     * 
     * @return <code>true</code>, falls ein Objekt dieser Szene vom Strahl
     *         getroffen wird. Andernfalls <code>false</code>.
	 */
    abstract public boolean occlude(Ray ray);

    
    /**
     * Ermittelt den Anteil des ambienten Lichts in dieser Szene.
     * 
     * @return Ambientes Licht in dieser Szene.
     */
    public ColorEx getAmbientLight()
    {
        return new ColorEx(ambientLight);
    }
    
    /**
     * Setzt den Anteil des ambienten Lichts in dieer Szene.
     * 
     * @param ambient Neues ambientes Licht in dieser Szene.
     */
    public void setAmbientLight(ColorEx ambient)
    {
        ambientLight.set(ambient);
    }
    
    
    /**
     * Ermittelt die Hintergrundfarbe der Szene.
     * 
     * @return Hintergrundfarbe der Szene.
     */
    public ColorEx getBackgroundColor()
    {
        return new ColorEx(backgroundColor);
    }
    
    /**
     * Setzt die Hintergrundfarbe der Szene.
     * 
     * @param color Neue Hintergrundfarbe der Szene.
     */
    public void setBackgroundColor(ColorEx color)
    {
        backgroundColor.set(color);
    }
    
    
    /**
     * Zeichnet alle Objekte dieser Szene mittels OpenGL.
     * 
     * @param drawable Informationen zum Zeichnen mittels OpenGL.
     */
    abstract public void display(GLAutoDrawable drawable);

    /** returns boolean if the scene needs to be updated */
    public boolean update(double t) { return false; }
}