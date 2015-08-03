package nars.rover.physics.external;


import org.jbox2d.common.Vec2;

/**
 * Base Entity class. Every frame, each Entity that is in an Entities list has its <code>update()</code>
 * method called before having its <code>render()</code> method called.
 * Anything that is in the physics world is a {@link DynamicEntity}, anything that's just rendered and
 * doesn't interact (i.e. a background) is just an Entity.
 * 
 * @author TranquilMarmot
 * @see DynamicEntity
 */
public class Entity {
	public interface ObjectRenderer2D {

	}

	/** EntityRenderer used to draw this entity */
	public ObjectRenderer2D renderer;
	
	/** Current location of entity */
	protected Vec2 location;
	
	/** Current rotation of entity (in radians) */
	protected float angle;
	
	/** What layer the entity gets rendered on */
	private int layer;
	
	/** Hash value for entity */
	private Integer hash;
	
	/** No-args constructor for serialization only! */
	public Entity(){
		renderer = null;
		location = new Vec2();
		layer = 0; //Entities.NUM_LAYERS / 2;
		angle = 0.0f;
	}
	
	public Entity(ObjectRenderer2D renderer, int layer){
		this.renderer = renderer;
		this.layer = layer;
		location = new Vec2();
		angle = 0.0f;
	}
	
	public Entity(ObjectRenderer2D renderer, int layer, Vec2 location){
		this(renderer, layer);
		this.location = location;
	}
	
	public Entity(ObjectRenderer2D renderer, int layer, Vec2 location, float angle){
		this(renderer, layer, location);
		this.angle = angle;
	}
	
	/** Set an entity's location */
	public void setLocation(Vec2 newLocation){ location.set(newLocation); }
	/** Set an entity'a angle */
	public void setAngle(float newAngle){ this.angle = newAngle; }
	
	/** @return Current location of entity */
	public Vec2 getLocation(){ return location; }
	/** @return Current angle of entity */
	public float getAngle(){ return angle; }
	
	/**
	 * Updates the entity- this can pretty much do anything and is called every frame
	 * @param timeStep How much time has passed since last update (in seconds)
	 */
	public void update(float timeStep){}
	
	/**
	 * Clean up any resources this entity may have allocated
	 * (called right before the entity gets removed from the world)
	 */
	public void delete(){}
	
	/** @return Which layer this entity resides on */
	public int getLayer(){ return layer; }
	
	/** Set the hash code for this entity */
	public void setHashCode(int newHash){ this.hash = newHash; }
	
	@Override
	public int hashCode(){
		// use default hash if none has been given
		if(hash == null)
			hash = super.hashCode();
		return hash;
	}
	
	/*
	public void read(Kryo kryo, Input input){
		//this.renderer = Renderers.values()[input.readInt()].renderer;
		this.layer = input.readInt();
		this.location.set(kryo.readObject(input, Vec2.class));
		this.angle = input.readFloat();
	}
	
	public void write(Kryo kryo, Output output){
		//Renderers renderers = Renderers.valueOf(this.renderer);
		//output.writeInt(renderers.ordinal());
		output.writeInt(this.layer);
		kryo.writeObject(output, this.location);
		output.writeFloat(this.angle);
	}
	*/
}
