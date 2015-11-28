/*
 * FirstPhongShader.java                  STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;
import raytracer.basic.RaytracerConstants;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Dieser Shader arbeitet nach dem Phong-Blinn-Modell und verwendet eine
 * fest fixierte Lichtquelle.
 * 
 * @author Sassan Torabi-Goudarzi
 * @author Mathias Kosch
 * 
 */
public class FirstPhongShader implements Shader
{
    /** Haupt-Shader, der die Farbwerte liefert. */
    protected final Shader mainShader;
    
	/** Anteil des ambienten Lichts, den das Objekt reflektiert. */
    protected final Vector3f ambientRatio = new Vector3f();
    /** Anteil des diffusen Lichts, den das Objekt reflektiert. */
    protected final Vector3f diffuseRatio = new Vector3f();
    /** Anteil des spekularen Lichts, den das Objekt reflektiert. */
    protected final Vector3f specularRatio = new Vector3f();
    /** Anteil des emissiven Lichts, den das Objekt reflektiert. */
    protected final Vector3f emissionRatio = new Vector3f();
    
	/** Richtung des vordefinierten Lichts. */
    protected final Vector3d lightDirection = new Vector3d(-1.0, -1.0, 0.0);
    /** Farbe des vordefinierten Lichts. */
    protected final ColorEx lightColor = new ColorEx(0.8f, 0.8f, 0.8f);
    /** Ambienter Anteil des vordefinierten Lichts. */
    protected final ColorEx lightAmbient = new ColorEx(0.2f, 0.2f, 0.2f);
	
    
    /**
     * Erzeugt einen neuen <code>FirstPhongShader</code>.
     * 
     * @param mainShader Haupt-Shader, der die Farbwerte liefert.
     * @param ambientRatio Ambienter Anteil am Farbwert des Objekts.
     * @param diffuseRatio Diffuser Anteil am Farbwert des Objekts.
     * @param specularRatio Spekularer Anteil am Farbwert des Objekts.
     */
    public FirstPhongShader(Shader mainShader, Vector3f ambientRatio,
            Vector3f diffuseRatio, Vector3f specularRatio)
    {
        this.mainShader = mainShader;
        
        // Abspeichern der Parameterwerte:
        this.ambientRatio.set(ambientRatio);
        this.diffuseRatio.set(diffuseRatio);
        this.specularRatio.set(specularRatio);
        emissionRatio.x = 0.0f;
        emissionRatio.y = 0.0f;
        emissionRatio.z = 0.0f;
        
        // Licht-Richtungsvektor normalisieren:
        lightDirection.normalize();
    }
    
    
    /**
     * Erzeugt einen neuen <code>FirstPhongShader</code>.
     * 
     * @param mainShader Haupt-Shader, der die Farbwerte liefert.
     * @param ambientRatio Ambienter Anteil am Farbwert des Objekts.
     * @param diffuseRatio Diffuser Anteil am Farbwert des Objekts.
     * @param specularRatio Spekularer Anteil am Farbwert des Objekts.
     * @param emissionRatio Emissiver Anteil am Farbwert des Objekts.
     */
    public FirstPhongShader(Shader mainShader, Vector3f ambientRatio,
            Vector3f diffuseRatio, Vector3f specularRatio, Vector3f emissionRatio)
    {
        this.mainShader = mainShader;
        
        // Abspeichern der Parameterwerte:
        this.ambientRatio.set(ambientRatio);
        this.diffuseRatio.set(diffuseRatio);
        this.specularRatio.set(specularRatio);
        this.emissionRatio.set(emissionRatio);
        
        // Licht-Richtungsvektor normalisieren:
        lightDirection.normalize();
    }
    
    
	@Override
    public ColorEx shade(Intersection intersection)
	{
        // Farbe des Objekts bestimmen:
        ColorEx color = mainShader.shade(intersection);

        Vector3d eyelight = new Vector3d(intersection.ray.dir);
        Vector3d normal = intersection.getNormal();     // Bereits normalisiert
        
        // Vorzeichen anpassen: Die Normale muss immer in die Richtung Zeigen,
        // von der der Strahl kommt:
        byte sign = (byte)Math.signum(normal.dot(eyelight));
        
        // Diffusen Winkel 'cos(alpha)' nach dem Phong-Modell berechnen:
        float cosalpha = (float) (sign * normal.dot(lightDirection));
        float cosbeta;
        if ((double) cosalpha <= 0.0)
        {
            cosalpha = 0.0f;
            cosbeta = 0.0f;
        }
        else
        {
            // Spekularen Winkel 'cos(beta)' nach dem Blinn-Modell berechnen:
            eyelight.normalize();
            eyelight.add(lightDirection);
            cosbeta = (float)((double) sign *normal.dot(eyelight)/eyelight.length());
        }

        // Farbe berechnen:
        ColorEx result = new ColorEx();
        result.mul2Add(ambientRatio, lightAmbient);
        result.mul2Add(cosalpha, diffuseRatio, lightColor);
        result.mul2Add(cosbeta, specularRatio, lightColor);
        result.add(emissionRatio);
        result.mul(color);
        
        // Farbe zur�ckgeben:
        if (RaytracerConstants.LIMIT_COLOR_INTENSITY)
            result.clampMax(1.0f);
		return result;
	}
}