///*
// * InefficientScene.java                  STATUS: Vorl�ufig abgeschlossen
// * ----------------------------------------------------------------------
// *
// */
//
//package raytracer.basic;
//
//import Light;
//import InefficientCollection;
//import SceneObject;
//import SceneObjectCollection;
//import java.util.Iterator;
//import java.util.Vector;
//
//
///**
// * Diese Implementierung der Szene ist ineffizient.
// *
// * @author Mathias Kosch
// * @author Sassan Torabi-Goudarzi
// *
// */
//public class InefficientScene extends Scene
//{
//    /** Objekte dieser Szene. */
//    protected final SceneObjectCollection objects = new InefficientCollection();
//    /** Lichter dieser Szene. */
//    protected final Vector<Light> lights = new Vector<Light>();
//
//
//    @Override
//    public void add(SceneObject object)
//    {
//        try
//        {
//            objects.add(object);
//            if (object instanceof Light)
//                lights.add((Light)object.clone());
//        }
//        catch (CloneNotSupportedException e)
//        {
//            throw new IllegalStateException();
//        }
//    }
//
//    @Override
//    public Iterator<Light> lightIterator()
//    {
//        return lights.iterator();
//    }
//
//
//    @Override
//    public ColorEx trace(Ray ray)
//    {
//        // Falls der Strahl nicht verfolgt wird, liefere die Hintergrundfarbe:
//        if (!followRay(ray))
//            return new ColorEx(backgroundColor);
//
//        // Falls ein Objekt vom Strahl getroffen wurde:
//        if (objects.intersect(ray))
//            return ray.hit.getShader().shade(new Intersection(ray,this));
//
//        // Kein Objekt wurde geschnitten. Liefere die Hintergrundfarbe:
//        return new ColorEx(backgroundColor);
//    }
//
//    @Override
//    public boolean occlude(Ray ray)
//    {
//        return objects.occlude(ray);
//    }
//
//
//    @Override
//    public void display(GLAutoDrawable drawable)
//    {
//        objects.display(drawable);
//    }
//}