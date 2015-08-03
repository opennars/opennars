//package nars.rover.physics.external;
//
//import nars.checkers.Game;
//import nars.rover.physics.external.callback.FirstHitRayCastCallback;
//import org.jbox2d.common.Vec2;
//import org.jbox2d.dynamics.BodyDef;
//import org.jbox2d.dynamics.FixtureDef;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//
//public class BreakableRock extends DynamicEntity implements Health {
//  /**  How many smaller rocks this rock will break into */
//	private static final int NUM_SPLITS = 3;
//	/** How much smaller the new rocks will be (1.0 == same size, 0.5 == half, etc.)*/
//	private static final float SPLIT_SIZE = 0.75f;
//	/** Smallest sized rock possible (if new rock is smaller, just gets deleted) */
//	private static final float MIN_SIZE = 0.3f;
//	/** How far new rocks will check to see if they're spawning inside of something. Gets scaled with rocks (smaller rocks check shorter distances) */
//	private static final float CHECK_DISTANCE = 5.0f;
//
//
//	final static Random rng = new Random();
//
//	/** Possible rocks */
//	private static String[] rockNames = {"rock1", "rock2", "rock3", "rock4"};
//	/** Name of rock */
//	public String chosenName;
//
//	/** @return Random name from rockNames array */
//	private static String getRandomName(){ return rockNames[ rng.nextInt(rockNames.length)]; }
//
//	/**
//	 * @return Random gray color
//	 */
//	private static float[] getRandomColor(){
//		int max = 6, min = 4;
//		float color = ((float)rng.nextInt((max - min + 1) + min) / 10.0f;
//		return new float[]{color, color, color, 1.0f};
//	}
//
//	/** Scale of this rock, in regards to geometry and rendering data (1.0 == normal) */
//	private float scale;
//
//	/** How much health this rock has left */
//	private float health;
//
//	/** Color of this rock */
//	private float[] color;
//
//	/**
//	 * This constructor calls the superclass's constructor
//	 * @param renderer
//	 * @param layer
//	 * @param bodyDef
//	 * @param fixtureDef
//	 */
//	private BreakableRock(ObjectRenderer2D renderer, int layer, BodyDef bodyDef, FixtureDef fixtureDef){
//		super(renderer, layer, bodyDef, fixtureDef);
//	}
//
//	/**
//	 * Create a new breakable rock
//	 * @param location Location to put rock at
//	 * @param scale Scale to create rock at
//	 * @param layer Layer to put rock on
//	 */
//	public BreakableRock(Vec2 location, float scale, int layer){
//		this(getRandomName(), location, scale, getRandomColor(), layer);
//	}
//
//	/**
//	 * Create a new breakable rock
//	 * @param location Location to put rock at
//	 * @param scale Scale to create rock at
//	 * @param color Color to draw rock as
//	 * @param layer Layer to put rock on
//	 */
//	public BreakableRock(Vec2 location, float scale, float[] color, int layer){
//		this(getRandomName(), location, scale, color, layer);
//	}
//
//	/**
//	 * Create a new breakable rock
//	 * @param name Name of model to use
//	 * @param location Location to put rock at
//	 * @param scale Scale to create rock at
//	 * @param color Color to draw rock as
//	 * @param layer Layer to put rock on
//	 */
//	public BreakableRock(String name, Vec2 location, float scale, float[] color, int layer){
//		this(
//			//new PolygonRenderer(Game.resources.polygons.get(name), scale, color),
//			null,
//			layer,
//			getBodyDef(name, location),
//			getFixtureDef(name, scale)
//		);
//		this.health = 20;
//		this.scale = scale;
//		this.color = color;
//		this.chosenName = name;
//	}
//
////	/**
////	 * Gets the body def for a given rock at a given location
////	 * @param name Name of rock to get def for
////	 * @param location Location to set def to
////	 * @return Def from given rock set to given location
////	 */
////	private static BodyDef getBodyDef(String name, Vec2 location){
////		BodyDef bodDef = Game.resources.entityInfo.getEntityBodyDef(name);
////		bodDef.position.set(location);
////		return bodDef;
////	}
//
////	/**
////	 * Gets the fixture def for a given rock at a given scale
////	 * @param name Name of rock to get def for
////	 * @param scale Scale to create rock at
////	 * @return Def from given rock at given scale
////	 */
////	private static FixtureDef getFixtureDef(String name, float scale){
////		FixtureDef fixtDef = Game.resources.entityInfo.getEntityFixtureDef(name);
////		fixtDef.shape = Game.resources.polygons.get(name).getShape(scale);
////		return fixtDef;
////	}
//
//	/** @return Scale of this rock */
//	public float getScale(){ return scale; }
//
//	/** @return Color of this rock */
//	public float[] getColor(){ return color; }
//
//	@Override
//	public void hurt(float amount) {
//		health -= amount;
//
//		// if no more health, remove from world and add smaller rocks
//		if(/*Game.net.client == null && */health <= 0){
//			float newScale = this.scale * SPLIT_SIZE;
//			float checkDist = newScale * CHECK_DISTANCE;
//
//			if(newScale > MIN_SIZE){
//				for(int i = 0; i < NUM_SPLITS; i++){
//					/*
//					 *  Each direction is added to this arraylist
//					 *  Each iteration of the while loop, a random index is chosen and removed from the arraylist
//					 *  If there's nothing in that direction, a new rock is added and the loop terminates.
//					 *  If all the directions get checked and none are empty, a new rock isn't added
//					 */
//					ArrayList<Vec2> directions = new ArrayList<Vec2>(8);
//					directions.add(new Vec2(this.location.x, this.location.y + checkDist));            // up
//					directions.add(new Vec2(this.location.x, this.location.y - checkDist));            // down
//					directions.add(new Vec2(this.location.x + checkDist, this.location.y));            // right
//					directions.add(new Vec2(this.location.x - checkDist, this.location.y));            // left
//					directions.add(new Vec2(this.location.x + checkDist, this.location.y + checkDist));// up-right
//					directions.add(new Vec2(this.location.x - checkDist, this.location.y + checkDist));// up-left
//					directions.add(new Vec2(this.location.x + checkDist, this.location.y - checkDist));// down-right
//					directions.add(new Vec2(this.location.x - checkDist, this.location.y - checkDist));// down-left
//
//					boolean done = false;
//					while(!done){
//						// grab random location and remove it
//						int index = Game.random.nextInt(directions.size());
//						Vec2 checkLoc = directions.get(index);
//						directions.remove(index);
//
//						// terminate loop if no more directions to check
//						if(directions.size() == 0)
//							done = true;
//
//						FirstHitRayCastCallback callback = new FirstHitRayCastCallback();
//						Game.physics.rayCast(callback, this.location, checkLoc);
//						if(callback.getHit() == null){
//							Game.physics.addEntity(new BreakableRock(this.location, newScale, this.color, this.getLayer()), true);
//							done = true;
//						}
//					}
//				}
//			} //else{
//				// TODO loot drop?
//			//}
//
//			// eyyyy, get outta here!
//			Game.physics.removeEntity(this, true);
//		}
//	}
//
//	@Override
//	public void heal(float amount) { health += amount; }
//
//	@Override
//	public float currentHealth() { return this.health; }
//
//}