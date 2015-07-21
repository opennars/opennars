package nars.rover.util;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;

public abstract class Bodies implements BodyDefCallback, FixtureDefCallback {

	public World world;
	public float standard_friction;
	public float standard_density;
	public float standard_restitution = 0;



	public interface CollisionManager {

		public abstract void preSolve(Contact contact, Manifold point_manifold, boolean is_fix_a);
		public abstract void beginContact(Contact contact, boolean is_fix_a);
		public abstract void endContact(Contact contact, boolean is_fix_a);
		public abstract void postSolve(Contact contact, ContactImpulse impulse, boolean is_fix_a);

	}

	public Bodies(float standard_friction, float standard_density){
		this.standard_friction = standard_friction;
		this.standard_density = standard_density;
	}
	
	/*
	 * Default Usage
	 * 
	 * BodyDef def = SetupBody(position, body_instruction);
	 *  Body ret = world.createBody(def);
	 * 
	 * FixtureDef temp = createFix(shape, fixture_instruction)
	 * ret.createFixture(temp)
	 * 
	 * return ret;
	 * 
	 */

	public void init(World world) {
		this.world = world;
	}

	public static class FixtureData {
		public CollisionManager collision_manager;
		public Object game_specific;
		public int filterindex = 0;

		public FixtureData(){
		}


		public FixtureData(CollisionManager new_collision_manager){
			collision_manager = new_collision_manager;
		}

		public FixtureData(CollisionManager new_collision_manager, Object new_game_specific){
			collision_manager = new_collision_manager;
			game_specific = new_game_specific;
		}

		public FixtureData(CollisionManager new_collision_manager, Object new_game_specific, int filterindex){
			collision_manager = new_collision_manager;
			game_specific = new_game_specific;
			this.filterindex = filterindex;
		}


	}

	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, BodyDefCallback body_instruction, FixtureDefCallback fixture_instruction, CollisionManager colm){
		Body groundbody = world.createBody(setupBody(position, bodytype, body_instruction));

		FixtureDef temp;
		for(Shape s : shapes){
			temp = createFix(s, fixture_instruction);
			if(colm != null)temp.userData = new FixtureData(colm);
			groundbody.createFixture(temp);
		}
		
		return groundbody;
	}
	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, BodyDefCallback body_instruction, CollisionManager colm){
		return create(position, shapes, bodytype, body_instruction, this, colm);
	}
	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, FixtureDefCallback fixture_instruction, CollisionManager colm){
		return create(position, shapes, bodytype, this, fixture_instruction, colm);
	}
	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, CollisionManager colm){
		return create(position, shapes, bodytype, this, this, colm);
	}

	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, BodyDefCallback body_instruction, FixtureDefCallback fixture_instruction){
		return create(position, shapes, bodytype, body_instruction, fixture_instruction, null);
	}
	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, BodyDefCallback body_instruction){
		return create(position, shapes, bodytype, body_instruction, this, null);
	}
	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype, FixtureDefCallback fixture_instruction){
		return create(position, shapes, bodytype, this, fixture_instruction, null);
	}
	public Body create(Vec2 position, Shape[] shapes, BodyType bodytype){
		return create(position, shapes, bodytype, this, this, null);
	}

	
	
	public Body create(Vec2 position, Shape shape, BodyType bodytype, BodyDefCallback body_instruction, FixtureDefCallback fixture_instruction, CollisionManager colm){
		Body groundbody = world.createBody(setupBody(position, bodytype, body_instruction));

		FixtureDef temp = createFix(shape, fixture_instruction);
		if(colm != null)temp.userData = new FixtureData(colm);
		groundbody.createFixture(temp);
		
		return groundbody;
	}
	public Body create(Vec2 position, Shape shape, BodyType bodytype, BodyDefCallback body_instruction, CollisionManager colm){
		return create(position, shape, bodytype, body_instruction, this, colm);
	}
	public Body create(Vec2 position, Shape shape, BodyType bodytype, FixtureDefCallback fixture_instruction, CollisionManager colm){
		return create(position, shape, bodytype, this, fixture_instruction, colm);
	}
	public Body create(Vec2 position, Shape shape, BodyType bodytype, CollisionManager colm){
		return create(position, shape, bodytype, this, this, colm);
	}
	public Body create(Vec2 position, Shape shape, BodyType bodytype, BodyDefCallback body_instruction, FixtureDefCallback fixture_instruction){
		return create(position, shape, bodytype, body_instruction, fixture_instruction, null);
	}

	public Body create(Vec2 position, Shape shape, BodyType bodytype, BodyDefCallback body_instruction){
		return create(position, shape, bodytype, body_instruction, this, null);
	}
	public Body create(Vec2 position, Shape shape, BodyType bodytype, FixtureDefCallback fixture_instruction){
		return create(position, shape, bodytype, this, fixture_instruction, null);
	}
	public Body create(Vec2 position, Shape shape, BodyType bodytype){
		return create(position, shape, bodytype, this, this, null);
	}

	
	
	public BodyDef setupBody(Vec2 position, BodyType bodytype, BodyDefCallback instruction){
		BodyDef bd = new BodyDef();
		bd.type = bodytype;
		bd.angle = 0;
		bd.position.set(position.x, position.y);
		bd.userData = null;
		
		bd = instruction.bodyDefCallback(bd);
		
		return bd;
	}
	
	public BodyDef setupBody(Vec2 position, BodyType bodytype){
		return setupBody(position, bodytype, this);
	}

	
	public FixtureDef createFix(Shape shape, FixtureDefCallback instruction){
		FixtureDef boxing = new FixtureDef();
		boxing.shape = shape;
		boxing.friction = standard_friction;
		boxing.density = standard_density;
		boxing.restitution = standard_restitution;
		return instruction.fixDefCallback(boxing);
	}

	public FixtureDef createFix(Shape shape){
		return createFix(shape, this);
	}

	
	public static Shape circle(float radius){
		CircleShape gBox = new CircleShape();
		gBox.setRadius(radius);
		return gBox;
	}
	
	public static Shape rectangle(Vec2 width_height, Vec2 offset){
		return rectangle(width_height, offset, 0);
	}

	public static Shape rectangle(Vec2 width_height, Vec2 offset, float angle){
		PolygonShape gBox = new PolygonShape();
		gBox.setAsBox(width_height.x, width_height.y, offset, angle);
		return gBox;
	}
	public static Shape rectangle(Vec2 width_height){
		return rectangle(width_height, new Vec2(0,0));
	}

	public static Shape rectangle(float w, float h) {
		return rectangle(new Vec2(w, h));
	}


	public static Shape square(float side_length, Vec2 offset){
		return rectangle(new Vec2(side_length,side_length), offset);
		
	}
	public static Shape square(float side_length){
		return rectangle(new Vec2(side_length,side_length), new Vec2(0,0));
		
	}

	
	public static Shape edge(Vec2 vertex1,Vec2 vertex2){
		EdgeShape e = new EdgeShape();
		e.set(vertex1, vertex2);
		return e;
		
	}



	
}