package automenta.spacegraph.physics.light;


import org.jbox2d.common.Vec2;

public class Block {
	public int x, y, width, height;

	public Block(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Vec2[] getVertices() {
		return new Vec2[] {
				new Vec2(x, y),
				new Vec2(x, y + height),
				new Vec2(x + width, y + height),
				new Vec2(x + width, y)
		};
	}
}
