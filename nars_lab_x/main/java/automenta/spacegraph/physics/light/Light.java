package automenta.spacegraph.physics.light;


import org.jbox2d.common.Vec2;

public class Light {
	public Vec2 location;
	public float red;
	public float green;
	public float blue;

	public Light(Vec2 location, float red, float green, float blue) {
		this.location = location;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
}
