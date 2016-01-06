/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package nars.guifx.graph2.layout.box2d;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import nars.guifx.demo.SubButton;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
//import org.jbox2d.collision.shapes.CircleShape;
//import org.jbox2d.dynamics.Body;
//import org.jbox2d.dynamics.BodyDef;
//import org.jbox2d.dynamics.BodyType;
//import org.jbox2d.dynamics.FixtureDef;

/**
 * 
 * @author dilip
 */
public class Ball {

	// JavaFX UI for ball
	public Node node;

	// X and Y position of the ball in JBox2D world
	private float posX;
	private float posY;

	/*
	 * There are three types bodies in JBox2D – Static, Kinematic and dynamic In
	 * this application static bodies (BodyType.STATIC – non movable bodies) are
	 * used for drawing hurdles and dynamic bodies (BodyType.DYNAMIC–movable
	 * bodies) are used for falling balls
	 */
	private final BodyType bodyType;

	// Gradient effects for balls
	private final LinearGradient gradient;

	public Ball(float posX, float posY) {
		this(posX, posY, Utils.BALL_SIZE, BodyType.DYNAMIC, Color.RED);
		this.posX = posX;
		this.posY = posY;
	}

	public Ball(float posX, float posY, int radius, BodyType bodyType,
			Color color) {
		this.posX = posX;
		this.posY = posY;
		this.bodyType = bodyType;
		this.gradient = Utils.getBallGradient(color);
		node = create();
	}

	private Node createCircle() {
		// Create an UI for ball - JavaFX code
		Circle ball = new Circle();
		ball.setFill(gradient); // set look and feel
		/*
		 * Set ball position on JavaFX scene. We need to convert JBox2D
		 * coordinates to JavaFX coordinates which are in pixels.
		 */
		ball.setLayoutX(posX);
		ball.setLayoutY(posY);
		ball.setCache(true); // Cache this object for better performance

		// ball.setUserData(body);

		return ball;
	}

	/*
	 * This method creates a ball by using Circle object from JavaFX and
	 * CircleShape from JBox2D
	 */
	private Node create() {

		// Create an JBox2D body defination for ball.
		BodyDef bd = new BodyDef();
		bd.type = bodyType;
		bd.position.set(posX, posY);

		float w = 25f;
		float h = 25f;
		FixtureDef fd = Utils.newBox(w, h, 1f);
		// CircleShape cs = new CircleShape();
		// cs.m_radius = radius * 0.1f; //We need to convert radius to JBox2D
		// equivalent

		// Create a fixture for ball
		// FixtureDef fd = new FixtureDef();
		// fd.shape = cs;
		// fd.density = 0.9f;
		// fd.friction = 0.3f;
		// fd.restitution = 0.6f;

		/*
		 * Virtual invisible JBox2D body of ball. Bodies have velocity and
		 * position. Forces, torques, and impulses can be applied to these
		 * bodies.
		 */
		Body body = Utils.world.createBody(bd);
		body.createFixture(fd);

		Group r = new Group();
		Rectangle s = new Rectangle(w * 2f, h * 2f);
		r.getChildren().add(s);
		r.getChildren().add(SubButton.make("a"));

		s.setManaged(false);
		s.setFill(new Color(0.5f, 0.5f, 0.5f, 0.5f));
		r.setTranslateX(posX);
		r.setTranslateY(posY);
		r.setUserData(body);
		return r;
	}
}
