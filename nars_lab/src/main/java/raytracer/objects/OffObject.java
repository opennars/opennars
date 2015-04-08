/*
 * OffObject.java                         STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.Ray;
import raytracer.basic.Transformation;
import raytracer.effects.NormalEffect;
import raytracer.exception.InvalidFormatException;
import raytracer.exception.LinearlyDependentException;
import raytracer.shader.Shader;
import raytracer.util.BoundingField;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

/**
 * Erzeugt ein neues OFF-Objekt aus einer OFF-Datei.
 * 
 * @author Mathias Kosch
 *
 */
public class OffObject extends SceneObject
{
    /** Sammlung zur Verwaltung aller Shapes. */
    SceneObjectCollection shapes = new EfficientCollection();
    
    /** Shader dieses Objets. */
    Shader shader = null;
    /** Normalen-Effekt f�r dieses Objekt. */
    NormalEffect normalEffect = null;
    
    
    /**
     * Erzeugt ein neues <code>OffObject</code>.
     * 
     * @param fileName Dateiname der OFF-Datei.
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    public OffObject(String fileName, Shader shader)
    throws IOException, InvalidFormatException
    {
        this(fileName, shader, null);
    }    
    
    /**
     * Erzeugt ein neues <code>OffObject</code>.
     * 
     * @param fileName Dateiname der OFF-Datei.
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     * @param normalEffect Normalen-Effekt f�r dieses Objekt.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    public OffObject(String fileName, Shader shader, NormalEffect normalEffect)
    throws IOException, InvalidFormatException
    {
        this.shader = shader;
        this.normalEffect = normalEffect;
        loadOffFile(fileName);
    }    
    
    
    @Override
    public OffObject clone()
    throws CloneNotSupportedException
    {
        OffObject clone = (OffObject)super.clone();
        clone.shapes = shapes.clone();
        return clone;
    }

    
    @Override
    public void getShapes(Collection<Shape> shapes)
    {
        this.shapes.getShapes(shapes);
    }
    
    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {
        shapes.getBoundingPoints(points);
    }
    
    
    @Override
    public void transform(Transformation t)
    {
        shapes.transform(t);
    }
    
    
    @Override
    public boolean intersect(Ray ray)
    {
        return shapes.intersect(ray);
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return shapes.occlude(ray);
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        shapes.display(drawable);
    }
    
    
    /**
     * Transformiert dieses Objekt so, dass die kleinste umschlie�ende Kugel
     * im Ursprung liegt.
     */
    public void center()
    {
        BoundingField.Sphere sphere = smallestEnclosingSphere();
        Transformation t = new Transformation();
        t.move(-sphere.center.x, -sphere.center.y, -sphere.center.z);
        transform(t);
    }
    
    /**
     * Transformiert dieses Objekt so, dass die kleinste umschlie�ende Kugel
     * im Ursprung mit Radius 1 liegt.
     */
    public void normalize()
    {
        BoundingField.Sphere sphere = smallestEnclosingSphere();
        Transformation t = new Transformation();
        t.move(-sphere.center.x, -sphere.center.y, -sphere.center.z);
        t.scale(1.0/sphere.radius, 1.0/sphere.radius, 1.0/sphere.radius);
        transform(t);
    }
    
    
    /**
     * Liest die Fl�cheninformationen aus einer OFF-Datei und erzeugt daraus
     * Dreiecke.
     * 
     * @param fileName Dateiname der OFF-Datei.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    private void loadOffFile(String fileName)
    throws InvalidFormatException
    {
        // Datei �ffnen:
        Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream(fileName));
        scanner.useLocale(Locale.US);

        Vector<Vector3d> vertices = new Vector<Vector3d>();

        try
        {
            // OFF-Format Kennung pr�fen:
            if (!scanner.hasNext() || !scanner.next().equals("OFF"))
                throw new InvalidFormatException();
            
            // Anzahl der Punkte, Fl�chen und Kanten bestimmen:
            int vertexCount = scanner.nextInt();
            int faceCount = scanner.nextInt();
            scanner.nextInt();
            
            // Punkte auslesen:
            int i;
            for (i = 0; i < vertexCount; i++)
            {
                double x = scanner.nextDouble();
                double y = scanner.nextDouble();
                double z = scanner.nextDouble();
                vertices.add(new Vector3d(x, y, z));
            }
            
            // Fl�chen auslesen und trianguliert zu diesem Objekt hinzuf�gen:
            for (i = 0; i < faceCount; i++)
            {
                int count = scanner.nextInt();
                if (count < 3)
                    throw new InvalidFormatException();
                
                // Ersten beiden Punkte ermitteln:
                Vector3d first = vertices.get(scanner.nextInt());
                Vector3d previous = vertices.get(scanner.nextInt());

                // Aus den restlichen Punkten der Fl�che Dreiecke erzeugen:
                count -= 2;
                do
                {
                    Vector3d v = vertices.get(scanner.nextInt());
                    try
                    {
                        Triangle triangle = new Triangle(first, previous, v, shader);
                        triangle.setNormalEffect(normalEffect);
                        shapes.add(triangle);
                    }
                    catch (LinearlyDependentException e)
                    {
                        // Falls bei den Koordinaten lineare Abh�ngigkeit besteht,
                        // verwerfe deises Dreieck.
                    }
                    previous = v;
                }
                while (--count > 0);
            }
        }
        finally
        {
            scanner.close();
        }
    }
}