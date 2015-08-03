package nars.rover.physics.external;


import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.BodyDef;

/**
 * A circle-shaped {@link DynamicEntity}
 * 
 * @author TranquilMarmot
 */
public class CircleEntity extends DynamicEntity {
	/** Color to draw entity in (4 floats, between 0.0 and 1.0) */
	public float[] color;
	
	/** Radius of the entity's circle */
	private float radius;
	
	/** No-args constructor for kryo only! */
	public CircleEntity(){
		super();
		color = new float[4];
	}
	
	/**
	 * @param color Color to use when rendering entity, 4 floats, rgba
	 */
	public CircleEntity(ObjectRenderer2D renderer, int layer, float radius, BodyDef bodyDef, float density, float[] color){
		super(renderer, layer, bodyDef, getCircleShape(radius), density);
		this.radius = radius;
		this.color = color;
	}
	
	/** @return Circle shape with given radius */
	private static CircleShape getCircleShape(float radius){
		CircleShape circ = new CircleShape();
		circ.setRadius(radius);
		return circ;
	}
	
	public float getRadius(){ return this.radius; }
	
	/*
	@Override
	public void write(Kryo kryo, Output output){
		super.write(kryo, output);
		
		output.writeFloat(radius);
		
		output.writeFloat(color[0]);
		output.writeFloat(color[1]);
		output.writeFloat(color[2]);
		output.writeFloat(color[3]);
	}
	
	@Override
	public void read(Kryo kryo, Input input){
		super.read(kryo, input);
		
		this.radius = input.readFloat();
		
		color[0] = input.readFloat();
		color[1] = input.readFloat();
		color[2] = input.readFloat();
		color[3] = input.readFloat();
	}
	*/
}
