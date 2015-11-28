///*
// * SimpleScene.java                       STATUS: Vorl�ufig abgeschlossen
// * ----------------------------------------------------------------------
// *
// */
//
//package raytracer.basic;
//
//import Light;
//import SceneObject;
//import java.util.Iterator;
//import java.util.Vector;
//
//
///**
// * Eine <code>SimpleScene</code> verwaltet alle Szenen-Objekte separat und ist
// * weder effizient noch ineffizient.
// *
// * @author Mathias Kosch
// * @author Sassan Torabi-Goudarzi
// *
// */
//public class SimpleScene extends Scene
//{
//    /** Objekte dieser Szene. */
//    private final Vector<Light> lights = new Vector<Light>();
//    /** Lichter dieser Szene. */
//    private final Vector<SceneObject> objects = new Vector<SceneObject>();
//
//
//    @Override
//    public void add(SceneObject object)
//    {
//        try
//        {
//            objects.add(object.clone());
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
//        boolean hit = false; //Hilfsvariable
//
//        for (SceneObject object : objects)
//            if (object.intersect(ray))
//                hit = true; //Ein Objekt wurde geschnitten
//
//        //Liefert die Farbe des Objektes das geschnitten wurde zurueck
//        if (hit)
//            return ray.hit.getShader().shade(new Intersection(ray,this));
//
//        // Kein Objekt wurde geschnitten. Liefere die Hintergrundfarbe:
//        return new ColorEx(backgroundColor);
//    }
//
//    @Override
//    public boolean occlude(Ray ray)
//    {
//        for (SceneObject object : objects)
//            if (object.occlude(ray))
//                return true;
//        return false;
//    }
//
//
//    @Override
//    public void display(GLAutoDrawable drawable)
//    {
//        for (SceneObject object : objects) object.display(drawable);
//    }
//}