package nars.nario;

import nars.audio.SoundListener;

import java.awt.*;

public abstract class Scene implements SoundListener {
	public static boolean[] keys = new boolean[16];

	public void toggleKey(int key, boolean isPressed) {
		keys[key] = isPressed;
	}

	public abstract void init();

	public abstract void tick();

	public abstract void render(Graphics og, float alpha);
}