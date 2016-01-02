/*
 * BoundingField.java                     STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.util;

import javax.vecmath.Vector3d;

/**
 * Umschlie�ende K�rper werden benutzt, um die Dimensionen einer Menge von
 * Punkten m�glichst genau zu berechnen.
 * 
 * @author Mathias Kosch
 *
 */
public class BoundingField
{
    
    
    /**
     * Berechnet den kleinsten umschlie�enden achsenparallelen Quader aus einer
     * Menge von Punkten.
     * 
     * @param points Punkte-Menge, deren kleinster umschlie�ender
     *        achsenparalleler Quader berechnet wird.
     *        Es muss mindestens ein Punkt enthalten sein.
     * @return Parameter des kleinsten umschlie�enden achsenparallelen Quaders.
     * 
     * @throws java.util.NoSuchElementException
     */
    public static CartesianCuboid smallestEnclosingCartesianCuboid(Collection<Vector3d> points)
    throws NoSuchElementException
    {
        CartesianCuboid cube = new CartesianCuboid();

        Iterator<Vector3d> it = points.iterator();
        
        // Wirft eine Exception, falls es keine Punkte gibt:
        Vector3d point = it.next();
        cube.minX = cube.maxX = point.x;
        cube.minY = cube.maxY = point.y;
        cube.minZ = cube.maxZ = point.z;
        
        while (it.hasNext())
        {
            point = it.next();
            
            // Ausma�e des W�rfels erweitern, falls n�tig:
            if (point.x < cube.minX) cube.minX = point.x;
            if (point.x > cube.maxX) cube.maxX = point.x;
            if (point.y < cube.minY) cube.minY = point.y;
            if (point.y > cube.maxY) cube.maxY = point.y;
            if (point.z < cube.minZ) cube.minZ = point.z;
            if (point.z > cube.maxZ) cube.maxZ = point.z;
        }
        
        return cube;
    }
    
    /**
     * Berechnet die kleinste umschlie�ende Kugel aus einer Menge von Punkten.
     * 
     * @param points Punkte-Menge, deren kleinste umschlie�ende Kugel berechnet
     *        wird. Es muss mindestens ein Punkt enthalten sein.
     * @return Parameter der kleinsten umschlie�enden Kugel.
     * 
     * @throws java.util.NoSuchElementException
     */
    public static Sphere smallestEnclosingSphere(Collection<Vector3d> points)
    throws NoSuchElementException
    {
        // Punkte zuf�llig sortieren:
        Vector<Vector3d> p = new Vector<Vector3d>(points);
        Collections.shuffle(p);
        
        int count = p.size();
        Sphere sphere = new Sphere();
        
        // Wirft eine Exception, falls es keine Punkte gibt:
        if (count <= 0)
            throw new NoSuchElementException();
        
        // Bei einem Punkt ist die Kugel sofort bekannt:
        if (count == 1)
        {
            sphere.center.set(p.get(0));
            sphere.radius = 0.0;
            return sphere;
        }

        // Nimm zwei beliebige Punkte:
        ComputeSphere(sphere, p.get(0), p.get(1));

        for (int i = 2; i < count; i++)
        {
            if (FloatingPoint.compareTolerated(Vectors.distance(sphere.center, p.get(i)), sphere.radius) <= 0)
                continue;

            // 'p[i]' liegt garantiert auf der Umkugel-Oberfl�che.
            // W�hle den zweiten Punkt beliebig:
            ComputeSphere(sphere, p.get(i), p.get(0));

            for (int j = 1; j < i; j++)
            {
                if (FloatingPoint.compareTolerated(Vectors.distance(sphere.center, p.get(j)), sphere.radius) <= 0)
                    continue;

                // 'p[j]' liegt garantiert auf der Umkugel-Oberfl�che.
                // 'p[i]' liegt garantiert in der Umkugel.
                ComputeSphere(sphere, p.get(i), p.get(j));

                for (int k = 0; k < j; k++)
                {
                    if (FloatingPoint.compareTolerated(Vectors.distance(sphere.center, p.get(k)), sphere.radius) <= 0)
                        continue;

                    // 'p[k]' liegt garantiert auf der Umkugel-Oberfl�che.
                    // 'p[i]', 'p[j]' liegen garantiert in der Umkugel.
                    ComputeSphere(sphere, p.get(i), p.get(j), p.get(k));

                    for (int l = 0; l < k; l++)
                    {
                        if (FloatingPoint.compareTolerated(Vectors.distance(sphere.center, p.get(l)), sphere.radius) <= 0)
                            continue;

                        // 'p[l]' liegt garantiert auf der Umkugel-Oberfl�che.
                        // 'p[i]', 'p[j]', 'p[k]' liegen garantiert in der Umkugel.
                        ComputeSphere(sphere, p.get(i), p.get(j), p.get(k), p.get(l));
                    }
                }
            }
        }
        
        return sphere;
    }
    
    /**
     * Berechnet die kleinste Umkugel mit zwei festen Punkten auf ihrer Oberfl�che.
     *
     * @param sphere Bekommt die Parameter der berechneten Umkugel zugeweisen.
     * @param p1 Erster Punkt auf der Kugel-Oberfl�che.
     * @param p2 Zweiter Punkt auf der Kugel-Oberfl�che.
     */
    protected static void ComputeSphere(Sphere sphere, Vector3d p1, Vector3d p2)
    {
        // Zentrum des Umkreises bestimmen:
        sphere.center.add(p1, p2);
        sphere.center.scale(0.5);

        // Radius des Umkreises bestimmen:
        sphere.radius = Vectors.distance(sphere.center, p1);
    }
    
    /**
     * Berechnet die kleinste Umkugel mit drei festen Punkten auf ihrer Oberfl�che.
     *
     * @param sphere Bekommt die Parameter der berechneten Umkugel zugeweisen.
     * @param p1 Erster Punkt auf der Kugel-Oberfl�che.
     * @param p2 Zweiter Punkt auf der Kugel-Oberfl�che.
     * @param p3 Dritter Punkt auf der Kugel-Oberfl�che.
     */
    protected static void ComputeSphere(Sphere sphere, Vector3d p1, Vector3d p2, Vector3d p3)
    {
        Vector3d n = new Vector3d(), d = new Vector3d(), p = new Vector3d();

        // Richtung 'd' des Strahls bestimmen:
        n.sub(p3, p1);
        p.sub(p2, p1);
        d.cross(p, n);
        d.cross(n, d);

        // Pr�fen, ob alle Punkte auf einer Geraden liegen:
        if ((FloatingPoint.compareTolerated(d.x, 0.0) == 0) &&
                (FloatingPoint.compareTolerated(d.y, 0.0) == 0) && 
                (FloatingPoint.compareTolerated(d.z, 0.0) == 0))
        {
            // Alle Punkte liegen auf einer Geraden. Nimm die beiden Punkte
            // mit gr��tem Abstand und berechne die Umkugel:
            double distanceAB2 = Vectors.distance(p1, p2);
            double distanceAC2 = Vectors.distance(p1, p3);
            double distanceBC2 = Vectors.distance(p2, p3);

            if (distanceAB2 < distanceAC2)
            {
                if (distanceAC2 < distanceBC2)
                    ComputeSphere(sphere, p2, p3);
                else
                    ComputeSphere(sphere, p1, p3);
            }
            else
            {
                if (distanceAB2 < distanceBC2)
                    ComputeSphere(sphere, p2, p3);
                else
                    ComputeSphere(sphere, p1, p2);
            }

            return;
        }

        // Normale 'n' der Ebene bestimmen:
        n.sub(p);

        // Schnittpunkt aus Strahl und Ebene berechnen.
        // Das ist der Schnittpunkt zweier Mittelsenkrechten des
        // Dreiecks '(p1, p2, p3)':
        sphere.center.add(p1, p3);
        sphere.center.scale(0.5);
        sphere.center.scaleAdd(n.dot(p)/(n.dot(d)*2.0), d, sphere.center);

        // Radius berechnen:
        sphere.radius = Vectors.distance(sphere.center, p1);
    }
    
    
    /**
     * Berechnet die kleinste Umkugel mit vier festen Punkten auf ihrer Oberfl�che.
     *
     * @param sphere Bekommt die Parameter der berechneten Umkugel zugeweisen.
     * @param p1 Erster Punkt auf der Kugel-Oberfl�che.
     * @param p2 Zweiter Punkt auf der Kugel-Oberfl�che.
     * @param p3 Dritter Punkt auf der Kugel-Oberfl�che.
     * @param p4 Vierter Punkt auf der Kugel-Oberfl�che.
     */
    protected static void ComputeSphere(Sphere sphere, Vector3d p1, Vector3d p2, Vector3d p3, Vector3d p4)
    {
        Vector3d p = new Vector3d(), n = new Vector3d(), d = new Vector3d();

        // Normalen zweier Dreiecke berechnen:
        p.sub(p3, p1);
        d.sub(p4, p1);
        d.cross(p, d);
        n.sub(p2, p1);

        // Pr�fen, ob alle Punkte auf einer Ebene liegen:
        if (FloatingPoint.compareTolerated(n.dot(d), 0.0) == 0)
        {
            Sphere temp = new Sphere();
            
            // Alle Punkte liegen auf einer Ebene. Nimm die drei Punkte
            // mit kleinstem Umkreis, der alle vier Punkte umschlie�t:
            sphere.radius = Double.POSITIVE_INFINITY;
            ComputeSphere(temp, p1, p2, p3);
            if (FloatingPoint.compareTolerated(Vectors.distance(temp.center, p4), temp.radius) <= 0)
                if (temp.radius < sphere.radius)
                    sphere.set(temp);
            ComputeSphere(temp, p1, p2, p4);
            if (FloatingPoint.compareTolerated(Vectors.distance(temp.center, p3), temp.radius) <= 0)
                if (temp.radius < sphere.radius)
                    sphere.set(temp);
            ComputeSphere(temp, p1, p3, p4);
            if (FloatingPoint.compareTolerated(Vectors.distance(temp.center, p2), temp.radius) <= 0)
                if (temp.radius < sphere.radius)
                    sphere.set(temp);
            ComputeSphere(temp, p2, p3, p4);
            if (FloatingPoint.compareTolerated(Vectors.distance(temp.center, p1), temp.radius) <= 0)
                if (temp.radius < sphere.radius)
                    sphere.set(temp);
            return;
        }

        n.cross(n, p);
        n.cross(n, p);

        // Umkreis-Mittelpunkte zweier Dreiecke berechnen:
        ComputeSphere(sphere, p1, p2, p3);
        p.set(sphere.center);
        ComputeSphere(sphere, p1, p3, p4);

        // Schnittpunkt der beiden Normalen an den Umkreismittelpunkten
        // berechnen:
        p.sub(sphere.center);
        sphere.center.scaleAdd(n.dot(p)/n.dot(d), d, sphere.center);

        // Radius berechnen:
        sphere.radius = Vectors.distance(sphere.center, p1);
    }
    
    
    /**
     * Diese Datenstruktur speichert die Parameter eines achsenparallelen
     * Quaders.
     * 
     * @author Mathias Kosch
     *
     */
    public static class CartesianCuboid
    {
        /** Kleinste Koordiante auf der x-Achse. */
        public double minX;
        /** Gr��te Koordiante auf der x-Achse. */
        public double maxX;
        
        /** Kleinste Koordiante auf der y-Achse. */
        public double minY;
        /** Gr��te Koordiante auf der y-Achse. */
        public double maxY;
        
        /** Kleinste Koordiante auf der y-Achse. */
        public double minZ;
        /** Gr��te Koordiante auf der y-Achse. */
        public double maxZ;
    }
    
    
    /**
     * Diese Datenstruktur speichert die Parameter einer umschlie�enden Kugel.
     * 
     * @author Mathias Kosch
     *
     */
    public static class Sphere
    {
        /** Mittelpunkt der Kugel. */
        public final Vector3d center = new Vector3d();
        /** Radius der Kugel. */
        public double radius;
        
        
        /**
         * Setzt die Parameter dieser Kugel durch eine andere Kugel.
         * 
         * @param sphere Kugel, deren Parameter kopiert werden sollen.
         */
        protected void set(Sphere sphere)
        {
            center.set(sphere.center);
            radius = sphere.radius;
        }
    }
}