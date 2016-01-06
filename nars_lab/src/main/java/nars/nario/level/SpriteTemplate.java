package nars.nario.level;

import nars.nario.LevelScene;
import nars.nario.sprites.Enemy;
import nars.nario.sprites.FlowerEnemy;
import nars.nario.sprites.Sprite;

public class SpriteTemplate {
	public int lastVisibleTick = -1;
	public Sprite sprite;
	public boolean isDead = false;
	private final boolean winged;

	private final int type;

	public SpriteTemplate(int type, boolean winged) {
		this.type = type;
		this.winged = winged;
	}

	public void spawn(LevelScene world, int x, int y, int dir) {
		if (isDead)
			return;

		sprite = type == Enemy.ENEMY_FLOWER ? new FlowerEnemy(world,
				x * 16 + 15, y * 16 + 24) : new Enemy(world, x * 16 + 8,
				y * 16 + 15, dir, type, winged);
		sprite.spriteTemplate = this;
		world.addSprite(sprite);
	}
}