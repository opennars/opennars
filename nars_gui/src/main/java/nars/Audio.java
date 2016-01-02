package nars;

import nars.audio.ListenerMixer;
import nars.audio.SoundListener;
import nars.audio.SoundProducer;
import nars.audio.SoundSource;
import nars.audio.sample.SamplePlayer;
import nars.audio.sample.SonarSample;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


public class Audio implements Runnable
{
    private final AudioFormat audioFormat;
    private final Line.Info info;
    private final int bufferBytes;
    public final int maxChannels
            ;
    private final SonarSample silentSample;
    private final SourceDataLine sdl;
    private final int rate = 44100;
    private final ListenerMixer listenerMixer;
    private final int bufferSize = rate / 16;
    private final ByteBuffer soundBuffer = ByteBuffer.allocate(bufferSize * 4);
    private final float[] leftBuf, rightBuf;
    //private float amplitude = 1;
    //private float targetAmplitude = 1;
    private boolean alive = true;
    private float alpha = 0;


    private FileOutputStream rec;


    public Audio(int maxChannels) throws LineUnavailableException {

        this.maxChannels = maxChannels;
        silentSample = new SonarSample(new float[] {0}, 44100);
        Mixer mixer = AudioSystem.getMixer(null);

        bufferBytes = bufferSize * 2 * 2 * 2 * 2 * 2;

        sdl = (SourceDataLine) mixer.getLine(info = new Line.Info(SourceDataLine.class));
        sdl.open(audioFormat = new AudioFormat(rate, 16, 2, true, false), bufferBytes);
        soundBuffer.order(ByteOrder.LITTLE_ENDIAN);
        sdl.start();



        try {
            FloatControl volumeControl = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volumeControl.getMaximum());
        }
        catch (IllegalArgumentException e)        {
            System.out.println("Failed to set the sound volume");
        }

        listenerMixer = new ListenerMixer(maxChannels);
        setListener(SoundListener.zero);

        leftBuf = new float[bufferSize];
        rightBuf = new float[bufferSize];

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setPriority(10);
        thread.start();

    }

    public void record(String path) throws LineUnavailableException, IOException {

        //if (rec != null) //...

        System.out.println("recording to: " + path);
        rec = new FileOutputStream(new File(path), false);

//        PipedInputStream pi = new PipedInputStream();
//        pi.connect(rec = new PipedOutputStream());
//
//        AudioInputStream ais = new AudioInputStream(
//                pi,
//                audioFormat, bufferBytes);
//
//
//        // start recording
//        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(path));

    }

    public void setListener(SoundListener soundListener)
    {
        listenerMixer.setSoundListener(soundListener);
    }

    public void shutDown()
    {
        alive = false;
    }


    public void play(SonarSample sample, SoundSource soundSource, float volume, float priority) {
        play(new SamplePlayer(sample, rate), soundSource, volume, priority);
    }

    static class DefaultSource implements SoundSource {

        private final SoundProducer producer;
        final float distanceFactor = 1.0f;

        DefaultSource(SoundProducer p) {
            producer = p;
        }

        @Override
        public float getY(float alpha) {
            return 0 + (1.0f - producer.getAmplitude()) * distanceFactor;
        }

        @Override
        public float getX(float alpha) {
            return 0;
        }
    }


    public void play(SoundProducer p, float volume, float priority) {
        play(p, new DefaultSource(p), volume, priority);
    }

    public void play(SoundProducer p, SoundSource soundSource, float volume, float priority)
    {
//        if (!alive)
//            return;
//
        synchronized (listenerMixer) {
            listenerMixer.addSoundProducer(p, soundSource, volume, priority);
        }
    }

    public List<Sound> getSounds() { return listenerMixer.sounds; }

    public void clientTick(float alpha)
    {
        synchronized (listenerMixer) {
            listenerMixer.update(alpha);
        }
    }

    public void tick()
    {
        //soundBuffer.clear();

        //        targetAmplitude = (targetAmplitude - 1) * 0.9f + 1;
        //        targetAmplitude = (targetAmplitude - 1) * 0.9f + 1;
        synchronized (listenerMixer)        {
            float maxAmplitude = listenerMixer.read(leftBuf, rightBuf, rate);
            ////            if (maxAmplitude > targetAmplitude) targetAmplitude = maxAmplitude;
        }

        soundBuffer.clear();
        int max16 = 32767;
        float gain = max16;
        for (int i = 0; i < bufferSize; i++)
        {
            //            amplitude += (targetAmplitude - amplitude) / rate;
            //          amplitude = 1;
            //              float gain = 30000;
            int l = (int) (leftBuf[i] * gain);
            int r = (int) (rightBuf[i] * gain);
            if (l > max16) l = max16;
            else if (l < -max16) l = -max16;
            if (r > max16) r = max16;
            else if (r < -max16) r = -max16;

            soundBuffer.putShort((short)l);
            soundBuffer.putShort((short)r);

            //doesnt work right:
            //soundBuffer.putInt( ((short)r) | ( ((short)l) << 16));


        }

        byte[] ba = soundBuffer.array();
        sdl.write(ba, 0, bufferSize * 2 * 2);
        if (rec!=null)
            try {
              rec.write(ba, 0, bufferSize * 2 * 2);
              rec.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void run()
    {
        while (alive)
        {
            clientTick(alpha);
            tick();
        }
    }



    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}