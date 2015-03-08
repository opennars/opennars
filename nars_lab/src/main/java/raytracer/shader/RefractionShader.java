/*
 * RefractionShader.java                           STATUS: In Bearbeitung
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;
import raytracer.basic.Ray;
import raytracer.util.FloatingPoint;

import javax.vecmath.Vector3d;

/**
 * Dieser Shader stellt eine totale Transparenz mit Lichtbrechung dar.<br>
 * Die Strahl-Brechung ist anh�ngig von der Geschwindigkeit des Lichts innerhalb
 * des getroffenen Objekts und dem Winkel, in dem der Strahl auf das Objekt
 * auftrifft.
 * 
 * @author Mathias Kosch
 *
 */
public class RefractionShader implements Shader
{
    /** Lichtgeschwindigkeit im Vakuum. */
    public final static float INDEX_VACUUM = 1.0f;
    /** Lichtgeschwindigkeit in der Luft. */
    public final static float INDEX_AIR = 1.000293f;
    /** Lichtgeschwindigkeit im Wasser. */
    public final static float INDEX_WATER = 1.333333f;
    /** Lichtgeschwindigkeit in Quarz. */
    public final static float INDEX_QUARTZ = 1.46f;
    /** Lichtgeschwindigkeit in Glas. */
    public final static float INDEX_GLASS = 1.51f;
    /** Lichtgeschwindigkeit in Diamant. */
    public final static float INDEX_DIAMOND = 2.4172f;
    
    /** Statisches <code>MirrorShader</code>-Objekt zum Berechnen des
     * reflektiven Anteils. */
    protected final static MirrorShader mirrorShader = new MirrorShader();

    
    /** Brechungsindex dieses Objekts. */
    protected final float refractiveIndex;
    /** Lichtdurchl�ssigkeit des Materials innerhalb dieses Objekts. */
    protected final ColorEx color = new ColorEx();
    

    /**
     * Erzeugt einen neuen Shader f�r Lichtbrechungs-Effekte ohne Lichtbrechung.
     * 
     * @param color Lichtdurchl�ssigkeit des Materials innerhalb dieses Objekts.
     */
    public RefractionShader(ColorEx color)
    {
        /*if ((speedOfLight <= 0) || (speedOfLight > SOL_VACUUM))
            throw new IllegalArgumentException();*/
        this.refractiveIndex = -1.0f;
        this.color.set(color);
    }
    
    
    /**
     * Erzeugt einen neuen Shader f�r Lichtbrechungs-Effekte.
     * 
     * @param refractiveIndex Brechungsindex dieses Objekts.
     * @param color Lichtdurchl�ssigkeit des Materials innerhalb dieses Objekts.
     */
    public RefractionShader(float refractiveIndex, ColorEx color)
    {
        this.refractiveIndex = refractiveIndex;
        this.color.set(color);
    }
    
    
    @Override
    public ColorEx shade(Intersection intersection)
    {
        // Fortf�hrenden Strahl erzeugen:
        Ray ray = Ray.continueRay(intersection.ray);
        if (ray == null)
            return intersection.scene.getBackgroundColor();

        final float n1, n2;
        Vector3d normal = intersection.getNormal();
        Vector3d x = new Vector3d(ray.dir);
        
        // Richtung der Normale ermitteln. Dadurch wird festgelegt, ob der
        // Strahl in dieses Objekt eindringt oder ob er es verl�sst:
        // (Positiv bei Eindringen, negativ bei Verlassen.)
        byte sign = (byte)Math.signum(-normal.dot(x));
        
        // Aktuellen Lichtbrechnungsindex berechnen:
        if (refractiveIndex < 0.0f)
            n1 = n2 = 0.0f;
        else if ((int) sign >= 0)
        {
            n1 = refractiveIndex;
            n2 = (ray.refractionStack.empty()) ?
                    INDEX_VACUUM : ray.refractionStack.peek();
            ray.refractionStack.push(refractiveIndex);
        }
        else
        {
            if (!ray.refractionStack.empty())
                ray.refractionStack.pop();
            n1 = (ray.refractionStack.empty()) ?
                    INDEX_VACUUM : ray.refractionStack.peek();
            n2 = refractiveIndex;
        }
        
        // Falls eine Brechung statt findet:
        float mirrorRatio;
        if (FloatingPoint.compareTolerated(n1, n2)==0)
            mirrorRatio = 0.0f;
        else
        {
            x.scale(1.0/(x.length()* (double) n1 / (double) n2));
            
            double nDotN = normal.dot(normal);
            double nDotX = -normal.dot(x);
            Vector3d k = new Vector3d();
            k.scaleAdd(nDotX/nDotN, normal, x);
            
            // H�he des neuen Richtungsvektors bez�glich der Objekt-Ebene
            // bestimmen. Falls diese '< 0', findet eine Totalreflexion statt:
            double height2 = 1.0-k.dot(k);
            if (height2 < 0.0)
                height2 = 0.0;
            
            // Gebrochenen Strahl bestimmen:
            ray.dir.scaleAdd((double) -(int) sign *Math.sqrt(height2/nDotN), normal, k);
            
            // Reflektiven Anteil nach der Fresnel'schen Formel berechnen:
            final float cosalpha = (float)Math.abs(normal.dot(intersection.ray.dir)/intersection.ray.dir.length());
            final float cosbeta = (float)Math.abs(normal.dot(ray.dir)/ray.dir.length());
            float Rs = (n1*cosalpha-n2*cosbeta)/(n1*cosalpha+n2*cosbeta);
            float Rp = (n2*cosalpha-n1*cosbeta)/(n2*cosalpha+n1*cosbeta);
            mirrorRatio = (Rs*Rs+Rp*Rp)/2.0f;
        }
            
        // Farbwerte des reflektiven und des gebrochenen Strahls bestimmen und
        // anteilig verrechnen:
        ColorEx color = new ColorEx();
        if ((double) mirrorRatio < 1.0)
        {
            ray.weight *= 1.0f-mirrorRatio;
            color.scale(1.0f-mirrorRatio, intersection.scene.trace(ray));
            
            // Verf�rbung anhand der dicke des durchlaufenen Materials:
            if ((int) sign >= 0)
                color.mulPow(this.color, (float)ray.length);
        }
        if ((double) mirrorRatio > 0.0)
        {
            float savedWeight = intersection.ray.weight;
            intersection.ray.weight *= mirrorRatio;
            color.scaleAdd(mirrorRatio, mirrorShader.shade(intersection), color);
            intersection.ray.weight = savedWeight;
        }
        
        return color;
    }
}