/*
 * SceneObject.java                       STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.Ray;
import raytracer.basic.Transformation;
import raytracer.util.BoundingField;

import javax.vecmath.Vector3d;
import java.util.Collection;
import java.util.Vector;

/**
 * Ein <code>SceneObject</code> ist ein Objekt innerhalb der Szene, das
 * aus einen oder mehreren <code>Shape</code>-Objekten bestehen kann.
 * 
 * @author Mathias Kosch
 *
 */
abstract public class SceneObject implements Cloneable
{
    /**
     * Erzeugt eine identische und unabh�ngige Kopie dieses Objekts.
     * 
     * @return Kopie dieses Objekts.
     * 
     * @throws CloneNotSupportedException
     */
    @Override
    public SceneObject clone()
    throws CloneNotSupportedException
    {
        return (SceneObject)super.clone();
    }
    
    
    /**
     * Ermittelt eine Liste mit allen <code>Shape</code>-Objekten, aus denen
     * dieses Objekt besteht.
     * 
     * @return Liste, allen <code>Shape</code>-Objekten.
     */
    protected Collection<Shape> getShapes()
    {
        Vector<Shape> shapes = new Vector<Shape>();
        getShapes(shapes);
        return shapes;
    }
    
    /**
     * F�llt eine Liste mit allen <code>Shape</code>-Objekten, aus denen dieses
     * Objekt besteht.
     * 
     * @param shapes Liste, die mit allen <code>Shape</code>-Objekten gef�llt wird.
     */
    abstract public void getShapes(Collection<Shape> shapes);
    
    /**
     * Ermittelt eine Liste mit allen (m�glicherweise nicht minimalen) Punkten,
     * die die Ausma�e dieses Objekts beschrieben.<br>
     * Falls alle Punkte in einem konvexen K�rper liegen, so liegt dieses
     * Objekt auch in dem konvexen K�rper. Der Umkehrschluss gilt jedoch
     * nicht notwendigerweise.
     * 
     * @return Liste mit allen Punkten.
     */
    protected Collection<Vector3d> getBoundingPoints()
    {
        Vector<Vector3d> points = new Vector<Vector3d>();
        getBoundingPoints(points);
        return points;
    }
    
    /**
     * F�llt eine Liste mit allen (m�glicherweise nicht minimalen) Punkten, die
     * die Ausma�e dieses Objekts beschrieben.<br>
     * Falls alle Punkte in einem konvexen K�rper liegen, so liegt dieses
     * Objekt auch in dem konvexen K�rper. Der Umkehrschluss gilt jedoch
     * nicht notwendigerweise.
     * 
     * @param points Liste, die mit allen Punkten gef�llt wird.
     */
    abstract public void getBoundingPoints(Collection<Vector3d> points);

    /**
     * Ermittelt den kleinsten umschlie�enden Quader dieses Objekts, dessen
     * Kanten parallel zu den Achsen des kartesischen Koordinaten Systems
     * verlaufen.
     * 
     * @return Kleinster umschlie�ender Quader dieses Objekts.
     */
    public BoundingField.CartesianCuboid smallestEnclosingCartesianCuboid()
    {
        return BoundingField.smallestEnclosingCartesianCuboid(getBoundingPoints());
    }
    
    /**
     * Ermittelt die kleinste umschlie�ende Kugel dieses Objekts.
     * 
     * @return Kleinste umschlie�ende Kugel dieses Objekts.
     */
    public BoundingField.Sphere smallestEnclosingSphere()
    {
        return BoundingField.smallestEnclosingSphere(getBoundingPoints());
    }
    
    
    /**
     * Wendet eine Transformation auf das Objekt an.
     * 
     * @param t Transformation, die auf dieses Objekt angewendet wird.
     */
    abstract public void transform(Transformation t);
    
    
    /**
     * Ermittelt, ob dieses Objekt von einem Strahl getroffen wird.<br>
     * Falls das Objekt von einem Strahl getroffen wird, wird dieser
     * bis zum Schnittpunkt verk�rzt und speichert einen Verweis auf dieses
     * Objekt.
     * 
     * @param ray Strahl, der auf einen Schnittpunkt mit diesem Objekt gepr�ft wird.
     * @return <code>true</code>, falls ein Schnittpuntk vorliegt.<br>
     *         Andernfalls <code>false</code>.
     */
    abstract public boolean intersect(Ray ray);

    /**
     * Ermittelt, ob dieses Objekt von einem Strahl getroffen wird.<br>
     * Der Strahl bleibt dabei unver�ndert.
     * 
     * @param ray Strahl, der auf einen Schnittpunkt mit diesem Objekt gepr�ft wird.
     * @return <code>true</code>, falls ein Schnittpuntk vorliegt.<br>
     *         Andernfalls <code>false</code>.
     */
    abstract public boolean occlude(Ray ray);
    
    
    /**
     * Zeichnet dieses Objekt mittels OpenGL.
     * 
     * @param drawable Informationen zum Zeichnen mittels OpenGL.
     */
    abstract public void display(GLAutoDrawable drawable);
}