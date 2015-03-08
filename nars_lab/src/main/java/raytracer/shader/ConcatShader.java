/*
 * ConcatShader.java                      STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;
import raytracer.basic.RaytracerConstants;

/**
 * Dieser Shader verbindet zwei Shader mit jeweils bestimmten Anteilen.
 * 
 * @author Mathias Kosch
 *
 */
public class ConcatShader implements Shader
{
    /** Anteil des Haupt-Shaders an der Farbberechnung. */
    protected final float mainRatio;
    /** Anteil des Zusatz-Shaders an der Farbberechnung. */
    protected final float subRatio;
    
    /** Haupt-Shader. */
    protected Shader mainShader = null;
    /** Zusatz-Shader. */
    protected Shader subShader = null;
    
    
    /**
     * Erzeugt einen neuen verbindenden Shader, der die Farbwerte des
     * Haupt-Shaders vollst�ndig �bernimmt.
     * 
     * @param mainShader Haupt-Shader.
     * @param subRatio Anteil des Zusatz-Shaders an der Farbberechnung.
     * @param subShader Zusatz-Shader.
     */
    public ConcatShader(Shader mainShader, float subRatio, Shader subShader)
    {
        // Falls die Licht-Intensit�t nicht limitiert ist, wird der Farbanteil
        // des Haupt-Shaders nicht reduziert:
        mainRatio = (RaytracerConstants.LIMIT_COLOR_INTENSITY) ? 1.0f-subRatio : 1.0f;
        
        this.mainShader = mainShader;
        this.subRatio = subRatio;
        this.subShader = subShader;
    }
    
    /**
     * Erzeugt einen neuen verbindenden Shader.
     * 
     * @param mainRatio Anteil des Haupt-Shaders an der Farbberechnung.
     * @param mainShader Haupt-Shader.
     * @param subRatio Anteil des Zusatz-Shaders an der Farbberechnung.
     * @param subShader Zusatz-Shader.
     */
    public ConcatShader(float mainRatio, Shader mainShader, float subRatio, Shader subShader)
    {
        this.mainRatio = mainRatio;
        this.mainShader = mainShader;
        this.subRatio = subRatio;
        this.subShader = subShader;
    }
    
    
    @Override
    public ColorEx shade(Intersection intersection)
    {
        float savedWeight = intersection.ray.weight;
        
        // Gespiegelte Farbe berechnen:
        intersection.ray.weight = savedWeight*mainRatio;
        ColorEx mainColor = mainShader.shade(intersection);
        
        // "Normal" geshaderte Farbe berechnen:
        intersection.ray.weight = savedWeight*subRatio;
        ColorEx subColor = subShader.shade(intersection);
        
        intersection.ray.weight = savedWeight;
        
        // Farbe berechnen und zur�ckgeben:
        subColor.scale(subRatio);
        mainColor.scaleAdd(mainRatio, subColor);
        return mainColor;
    }
}