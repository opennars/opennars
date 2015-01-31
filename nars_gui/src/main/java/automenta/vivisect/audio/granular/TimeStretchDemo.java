package automenta.vivisect.audio.granular;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

public class TimeStretchDemo {

	public static void main(String[] args) throws IOException {
		
		
		final AudioPlayer player = new JavaSoundAudioPlayer();
		float[] samples = new float[44100];
		
		for(int i = 0; i < samples.length; i++) {
			samples[i] = (float) Math.sin((i / 44100.0) * 440.0 * 2.0 * Math.PI);
		}
		
		player.loadFile(samples);
//		player.loadFile(TimeStretchDemo.class.getResourceAsStream("Beats example - Amen loop.wav"));
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Basic Timestretch Demo");
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						player.destroy();
						System.exit(0);
					}
				});
				TimeStretchGui gui = new TimeStretchGui(player);
				frame.setResizable(false);
				frame.setLayout(null);
				frame.add(gui);
				frame.setSize(200, 360);
				frame.setVisible(true);
			}
		});
	}

}
