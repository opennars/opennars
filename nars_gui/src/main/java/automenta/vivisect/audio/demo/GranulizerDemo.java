package automenta.vivisect.audio.demo;

import automenta.vivisect.Audio;
import automenta.vivisect.audio.SoundListener;
import automenta.vivisect.audio.granular.TimeStretchGui;
import automenta.vivisect.audio.granular.Granulize;
import automenta.vivisect.audio.sample.SampleLoader;
import automenta.vivisect.swing.NSlider;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class GranulizerDemo {

    //http://forum.renoise.com/index.php/topic/35858-royalty-free-sample-packs/

    public static class GranularControlPanel extends JPanel {

        public GranularControlPanel(Granulize s) {
            super(new BorderLayout());
            NSlider p = new NSlider(s.stretchFactor, 0.1f, 10.0f);
            add(p, BorderLayout.NORTH);
        }

    }

    public static void main(String[] args) throws IOException, LineUnavailableException, UnsupportedAudioFileException {


        final Audio audio = new Audio(4);
        float[] samples = new float[44100];

        for (int i = 0; i < samples.length; i++) {
            samples[i] = (float) Math.sin((i / 44100.0) * 60.0 * 2.0 * Math.PI);
        }


        //player.loadFile(samples);
//		player.loadFile(PlayWAVDemo.class.getResourceAsStream("Beats example - Amen loop.wav"));

        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {*/
        JFrame frame = new JFrame("Basic Timestretch Demo");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                audio.shutDown();
                System.exit(0);
            }
        });
        TimeStretchGui gui = new TimeStretchGui(audio);


        Granulize ts = new Granulize(SampleLoader.load("/tmp/p.wav"), 0.01f, 0.2f);
        ts.setStretchFactor(1f);


        frame.setLayout(new GridLayout());
        frame.getContentPane().add(gui);
        frame.getContentPane().add(new GranularControlPanel(ts));
        frame.setSize(200, 360);
        frame.setVisible(true);

        audio.play(ts, SoundListener.zero, 1, 1);


        /*    }
        });*/
    }

//	public static void main0(String[] args) throws IOException {
//
//
//		final AudioPlayer player = new JavaSoundAudioPlayer();
//		float[] samples = new float[44100];
//
//		for(int i = 0; i < samples.length; i++) {
//			samples[i] = (float) Math.sin((i / 44100.0) * 60.0 * 2.0 * Math.PI);
//		}
//
//		player.loadFile(samples);
////		player.loadFile(PlayWAVDemo.class.getResourceAsStream("Beats example - Amen loop.wav"));
//
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				JFrame frame = new JFrame("Basic Timestretch Demo");
//				frame.addWindowListener(new WindowAdapter() {
//					public void windowClosing(WindowEvent e) {
//						player.destroy();
//						System.exit(0);
//					}
//				});
//				TimeStretchGui gui = new TimeStretchGui(player);
//				frame.setResizable(false);
//				frame.setLayout(null);
//				frame.add(gui);
//				frame.setSize(200, 360);
//				frame.setVisible(true);
//			}
//		});
//	}
}
