package nars.audio.demo;

import nars.Audio;
import nars.audio.SoundListener;
import nars.audio.granular.Granulize;
import nars.audio.granular.TimeStretchGui;
import nars.audio.sample.SampleLoader;
import nars.audio.sample.SamplePlayer;
import nars.audio.sample.SonarSample;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public enum GranulizerDemo {
    ;

    //http://forum.renoise.com/index.php/topic/35858-royalty-free-sample-packs/

    public static class GranularControlPanel extends JPanel {

        public GranularControlPanel(Granulize s) {
            super(new BorderLayout());
            //NSliderSwing p = new NSliderSwing(s.stretchFactor, 0.1f, 10.0f);
            //add(p, BorderLayout.NORTH);
        }

    }

    @SuppressWarnings("HardcodedFileSeparator")
    public static void main(String[] args) throws LineUnavailableException {


        Audio audio = new Audio(4);
//        float[] samples = new float[44100];
//
//        for (int i = 0; i < samples.length; i++) {
//            samples[i] = (float) Math.sin((i / 44100.0) * 60.0 * 2.0 * Math.PI);
//        }


        //player.loadFile(samples);
//		player.loadFile(PlayWAVDemo.class.getResourceAsStream("Beats example - Amen loop.wav"));

        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {*/
        JFrame frame = new JFrame("Basic Timestretch Demo");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                audio.shutDown();
                System.exit(0);
            }
        });

        TimeStretchGui gui = new TimeStretchGui(audio);

        SonarSample smp = SampleLoader.load("/tmp/317683__jalastram__sfx-pickup-04.wav");

        Granulize ts = new Granulize(smp, 0.3f, 0.2f);
        ts.setStretchFactor(0.5f);

        //audio.play(ts, SoundListener.zero, 1, 1);

        audio.play(new SamplePlayer(smp, 0.5f), SoundListener.zero, 1, 1);


        frame.setLayout(new GridLayout());
        frame.getContentPane().add(gui);
        frame.getContentPane().add(new GranularControlPanel(ts));
        frame.setSize(200, 360);
        frame.setVisible(true);

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
