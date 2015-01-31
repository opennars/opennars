package automenta.vivisect.audio.demo;

import automenta.vivisect.audio.SonarSoundEngine;
import automenta.vivisect.audio.SoundListener;
import automenta.vivisect.audio.SoundSource;
import automenta.vivisect.audio.granular.TimeStretchGui;
import automenta.vivisect.audio.sample.SampleLoader;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class PlayWAVDemo {

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
    public static void main(String[] args) throws IOException, LineUnavailableException, UnsupportedAudioFileException {


        final SonarSoundEngine player = new SonarSoundEngine(4);
        float[] samples = new float[44100];

        for(int i = 0; i < samples.length; i++) {
            samples[i] = (float) Math.sin((i / 44100.0) * 60.0 * 2.0 * Math.PI);
        }

        player.setListener(new SoundListener() {

            @Override
            public float getX(float alpha) {
                return 0;
            }

            @Override
            public float getY(float alpha) {
                return 0;
            }
        });

        //player.loadFile(samples);
//		player.loadFile(PlayWAVDemo.class.getResourceAsStream("Beats example - Amen loop.wav"));

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Basic Timestretch Demo");
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        player.shutDown();
                        System.exit(0);
                    }
                });
                TimeStretchGui gui = new TimeStretchGui(player);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.add(gui);
                frame.setSize(200, 360);
                frame.setVisible(true);


                try {
                    player.play(SampleLoader.loadSample("/tmp/p.wav"), new SoundSource() {

                        @Override
                        public float getX(float alpha) {
                            return 0;
                        }

                        @Override
                        public float getY(float alpha) {
                            return 0;
                        }
                    }, 1, 1, 1);
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
