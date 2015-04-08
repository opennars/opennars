/*
 * Shape.java                             STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.Transformation;
import raytracer.effects.NormalEffect;
import raytracer.shader.Shader;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Ein <code>Shape</code> ist ein visuelles Objekt innerhalb der Szene,
 * welches mit einem Shader dargestellt wird.
 * 
 * @author Mathias Kosch
 *
 */
abstract public class Shape extends SceneObject
{
    /** Shader dieses Objekts. */
    protected Shader shader = null;
    
    /** Effekt, der auf Normalenvektoren angewendet wird. */
    protected NormalEffect normalEffect = null;
    
    /** Gibt an, ob dieses <code>Shape</code>-Objekt zu einem Licht geh�rt. */
    public boolean isLight = false;
    
    
    /**
     * Erzeugt ein neues Szenen-Objekt.
     * 
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     */
    public Shape(Shader shader)
    {
        this.shader = shader;
    }
    
    
    @Override
    public Shape clone()
    throws CloneNotSupportedException
    {
        return (Shape)super.clone();
    }

    
    @Override
    public void getShapes(Collection<Shape> shapes)
    {
        shapes.add(this);
    }
    
    @Override
    abstract public void getBoundingPoints(Collection<Vector3d> points);
    
    
    /**
     * Ermittelt den Shader, der dieses Objekt darstellt.
     * 
     * @return Shader, der dieses Objekt darstellt.
     */
    public Shader getShader()
    {
        return shader;
    }
    
    /**
     * Setzt den Shader, der dieses Objekt darstellt.
     * 
     * @param shader Neuer Shader f�r dieses Objekt.
     */
    public void setShader(Shader shader)
    {
        this.shader = shader;
    }
    
    /**
     * Ermittelt den Normalenvektor dieses Objekts an einem bestimmten Punkt
     * auf dem Objekt.<br>
     * Die Normale zeigt immer in Richtung der Objekt-Au�enseite.
     * 
     * @param point Punkt auf dem Objekt, an dem der Normalenvektor bestimmt wird.
     * @return Normalenvektor des Objekts am Punkt <code>point</code>.
     */
    abstract public Vector3d getNormal(Vector3d point);
    
    
    /**
     * Ermittelt, ob dieses Objekt endlich ist.<br>
     * Endliche Objekte haben einen endlichen Schwerpunkt.
     * 
     * @return <code>true</code>, falls dieses Objekt endlich ist.<br>
     *         Andernfalls <code>false</code>.
     */
    abstract public boolean isFinite();
    
    /**
     * Ermittelt eine Koordinate des Schwerpunktes des Objekts.<br>
     * Falls das Objekt keinen endlichen Schwerpunkt hat, wird
     * <code>POSITIVE_INFINITY</code> zur�ckgegeben.
     * 
     * @param axisId 0-basierter Index der Achse.
     * @return Eine Koordinate des Schwerpunkts.
     */
    abstract public double getCentroid(byte axisId);

    /**
     * Ermittelt die Lage des Objekts bez�glich einer Ebene durch eine Achse.<br>
     * Die Ebene geht durch den Punkt eienr Achse mit dieser als Normalenvektor.<br>
     * <br>
     * Falls alle Punkte dieses Objekts kleiner als der Vergleichswert sind, ist
     * der R�ckgabewert <code>-1</code>.<br>
     * Falls alle Punkte dieses Objekts gr��er als der Vergleichswert sind, ist
     * der R�ckgabewert <code>+1</code>.<br>
     * Ansonsten ist der R�ckgabewert <code>0</code>.
     * 
     * @param axisId 0-basierter Index der Achse.
     * @param axisValue Punkt auf der Achse, durch den die Ebene geht.
     * @return Lage des Objekts bez�glich der Ebene.
     */
    abstract public byte compareAxis(byte axisId, double axisValue);
    
    /**
     * Ermittelt den kleinsten Koordinatenwert dieses Objekts bez�glich einer
     * Achse.
     * 
     * @param axisId 0-basierter Index der Achse.
     * @return Kleinster Koordinatenwert bez�glich der Achse <code>axisId</code>.
     */
    abstract public double minAxisValue(byte axisId);
    
    /**
     * Ermittelt den gr��ten Koordinatenwert dieses Objekts bez�glich einer
     * Achse.
     * 
     * @param axisId 0-basierter Index der Achse.
     * @return Gr��ter Koordinatenwert bez�glich der Achse <code>axisId</code>.
     */
    abstract public double maxAxisValue(byte axisId);
    
    
    /**
     * Ermittelt die Koordinaten innerhalb einer Textur, die um das Objekt
     * gelegt wird.
     * 
     * @param point Punkt auf dem Objekt, an dem die Texturkoordinaten bestimmt
     *        werden sollen.
     * @return Texturkoordinaten am Schnittpunkt des Objekts.
     */
    abstract public Vector2d getTextureCoords(Vector3d point);

    /**
     * Wendet eine Transformation auf die Texturkoordinaten dieses Objekts an.
     * 
     * @param t Transformation, die auf die Texturkoordinaten angewendet wird.
     */
    abstract public void transformTexture(Transformation t);
    
    
    /**
     * Setzt einen Effekt, der auf die Normalenvektoren dieses Objekts
     * angewendet wird.<br>
     * Dadurch lassen sich beispielweise rauhe Oberfl�chen simulieren.
     * 
     * @param normalEffect Normalen-Effekt f�r dieses Objekt.
     */
    public void setNormalEffect(NormalEffect normalEffect)
    {
        this.normalEffect = normalEffect;
    }
    
    /**
     * Passt eine Normale dieses Objekts entsprechend des Normalen-Effekts an.
     * 
     * @param normal Normale dieses Objekts, die ver�ndert wird.
     * @param point Punkt, zu dem der Normalenvektor geh�rt.
     * @return Gibt <code>normal</code> zur�ck.
     */
    protected Vector3d adjustNormal(Vector3d normal, Vector3d point)
    {
        if (normalEffect != null)
            normalEffect.adjustNormal(normal, point);
        return normal;
    }
    
    
    @Override
    abstract public void display(GLAutoDrawable drawable);
}