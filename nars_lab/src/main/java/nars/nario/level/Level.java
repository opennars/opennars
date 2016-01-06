package nars.nario.level;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Level {
	public static final String[] BIT_DESCRIPTIONS = {//
	"BLOCK UPPER", //
			"BLOCK ALL", //
			"BLOCK LOWER", //
			"SPECIAL", //
			"BUMPABLE", //
			"BREAKABLE", //
			"PICKUPABLE", //
			"ANIMATED",//
	};

	public static byte[] TILE_BEHAVIORS = new byte[256];

	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
	public static final int BIT_BLOCK_LOWER = 1 << 2;
	public static final int BIT_SPECIAL = 1 << 3;
	public static final int BIT_BUMPABLE = 1 << 4;
	public static final int BIT_BREAKABLE = 1 << 5;
	public static final int BIT_PICKUPABLE = 1 << 6;
	public static final int BIT_ANIMATED = 1 << 7;

	private static final int FILE_HEADER = 0x271c4178;
	public int width;
	public int height;

	public byte[][] map;
	public byte[][] data;

	public SpriteTemplate[][] spriteTemplates;

	public int xExit;
	public int yExit;

	public Level(int width, int height) {
		this.width = width;
		this.height = height;

		xExit = 10;
		yExit = 10;
		map = new byte[width][height];
		data = new byte[width][height];
		spriteTemplates = new SpriteTemplate[width][height];
	}

	public static void loadBehaviors(DataInputStream dis) throws IOException {
		dis.readFully(Level.TILE_BEHAVIORS);
	}

	public static void saveBehaviors(DataOutputStream dos) throws IOException {
		dos.write(Level.TILE_BEHAVIORS);
	}

	public static Level load(DataInputStream dis) throws IOException {
		long header = dis.readLong();
		if (header != Level.FILE_HEADER)
			throw new IOException("Bad level header");
		int version = dis.read() & 0xff;

		int width = dis.readShort() & 0xffff;
		int height = dis.readShort() & 0xffff;
		Level level = new Level(width, height);
		level.map = new byte[width][height];
		level.data = new byte[width][height];
		for (int i = 0; i < width; i++) {
			dis.readFully(level.map[i]);
			dis.readFully(level.data[i]);
		}
		return level;
	}

	public void save(DataOutputStream dos) throws IOException {
		dos.writeLong(Level.FILE_HEADER);
		dos.write((byte) 0);

		dos.writeShort((short) width);
		dos.writeShort((short) height);

		for (int i = 0; i < width; i++) {
			dos.write(map[i]);
			dos.write(data[i]);
		}
	}

	public void tick() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[x][y] > 0)
					data[x][y]--;
			}
		}
	}

	public byte getBlockCapped(int x, int y) {
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (x >= width)
			x = width - 1;
		if (y >= height)
			y = height - 1;
		return map[x][y];
	}

	public byte getBlock(float _x, float _y) {
		int x = Math.round(_x / 16.0f);
		int y = Math.round(_y / 16.0f);
		if ((x < 0) || (y < 0) || (x >= width) || (y >= height))
			return -1;
		return map[x][y];
	}

	public byte getData(float _x, float _y) {
		int x = Math.round(_x / 16.0f);
		int y = Math.round(_y / 16.0f);
		if ((x < 0) || (y < 0) || (x >= width) || (y >= height))
			return -1;
		return data[x][y];
	}

	public byte getBlock(int x, int y) {
		if (x < 0)
			x = 0;
		if (y < 0)
			return 0;
		if (x >= width)
			x = width - 1;
		if (y >= height)
			y = height - 1;
		return map[x][y];
	}

	public void setBlock(int x, int y, byte b) {
		if (x < 0)
			return;
		if (y < 0)
			return;
		if (x >= width)
			return;
		if (y >= height)
			return;
		map[x][y] = b;
	}

	public void setBlockData(int x, int y, byte b) {
		if (x < 0)
			return;
		if (y < 0)
			return;
		if (x >= width)
			return;
		if (y >= height)
			return;
		data[x][y] = b;
	}

	public boolean isBlocking(int x, int y, float xa, float ya) {
		byte block = getBlock(x, y);
		boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
		blocking |= (ya > 0)
				&& ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
		blocking |= (ya < 0)
				&& ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

		return blocking;
	}

	public SpriteTemplate getSpriteTemplate(int x, int y) {
		if (x < 0)
			return null;
		if (y < 0)
			return null;
		if (x >= width)
			return null;
		if (y >= height)
			return null;
		return spriteTemplates[x][y];
	}

	public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate) {
		if (x < 0)
			return;
		if (y < 0)
			return;
		if (x >= width)
			return;
		if (y >= height)
			return;
		spriteTemplates[x][y] = spriteTemplate;
	}
}