///Wurde ge�ndert!!!

package raytracer.basic;

import raytracer.objects.Shape;

import javax.vecmath.Vector3d;

/**
 * This is a intermediate class, will provide all necessary infromation for the
 * shading process. Thus it will be constructed from a Ray, a Shape and the
 * Scene. e.g. Point, Normal, TexCoord, Lights, the Eye.
 * 
 * At the moment this might be a bit useless, since everthing can be provided by
 * the Ray/Shape/Scene. But it gives you more flexibility , since you can easily
 * extend this class by other functions you find usefull.
 * 
 * It may also give you the possiblity to change the coordinate system.
 *  
 * @author Jonas Stahl
 * @author Sassan Torabi-Goudarzi
 * @author Mathias Kosch
 * 
 */
public class Intersection
{
	public Ray ray = null;
    public Scene scene = null;
    public Shape shape = null;

	public Intersection(Ray ray, Scene scene)
    {
		this.ray = ray;
        this.scene = scene;
		shape = ray.hit;
	}

	/**
     * Ermtitelt den Punkt, an dem der Strahl auf das Objekt trifft.
     * 
     * @return Auftreffpunkt des Strahles.
	 */
	public Vector3d getPoint()
    {
        return ray.getEnd();
	}

	/**
     * Ermittelt die normalisierte Normale des Auftreffpunktes des Strahls.
     * 
     * @return Normalisierte Normale des Strahl-Auftreffpunktes. 
	 */
	public Vector3d getNormal()
    {
		Vector3d normal = new Vector3d(shape.getNormal(getPoint()));
		normal.normalize();
		return normal;
	}

	/* Returns the position of the eye. */
	public Vector3d getEye()
    {
		return ray.org;
	}

	// Textures:
	/* returns the (u,v) texture coordinates */
	//Vector2d getTexCoord();
	/* Returns the tangent on the surface in the u direction */
	//Vector3d getDu();
	/* Returns the tangent on the surface in the v direction */
	//Vector3d getDv();
}