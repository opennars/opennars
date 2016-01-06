package nars.gui.output.audio;

import nars.Audio;
import nars.NAR;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by me on 2/1/15.
 */
public class SoundEngineTestPanel extends JPanel {

	static final int maxChannels = 4;
	public static final Audio sound;
	static {
		Audio s;
		try {
			s = new Audio(maxChannels);
		} catch (LineUnavailableException e) {
			s = null;
			e.printStackTrace();
			System.exit(1);
		}
		sound = s;
	}

	public SoundEngineTestPanel(NAR n) {
		super(new BorderLayout());
		add(new MixerPanel(sound), BorderLayout.CENTER);

		try {
			initAmbient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void initAmbient() throws IOException,
			UnsupportedAudioFileException {

		// sound.play(new BinauralTones(12f, 200f), SoundListener.zero, 0.5f,
		// 1);

		// sound.play(SampleLoader.load("/tmp/p.wav"), SoundListener.zero, 1f,
		// 1f);

		/*
		 * Granulize ts = new Granulize(SampleLoader.load("/tmp/p.wav"), 0.1f,
		 * 0.1f); ts.setStretchFactor(4f); sound.play(ts, SoundListener.zero,
		 * 1f, 1);
		 */

	}
}
