package automenta.vivisect;

import automenta.vivisect.Sound;
import automenta.vivisect.audio.ListenerMixer;
import automenta.vivisect.audio.SoundListener;
import automenta.vivisect.audio.SoundProducer;
import automenta.vivisect.audio.SoundSource;
import automenta.vivisect.audio.sample.SamplePlayer;
import automenta.vivisect.audio.sample.SonarSample;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


public class Audio implements Runnable
{
    private SonarSample silentSample;
    private SourceDataLine sdl;
    private int rate = 44100;
    private ListenerMixer listenerMixer;
    private int bufferSize = rate / 400; // 40 ms
    private ByteBuffer soundBuffer = ByteBuffer.allocate(bufferSize * 4);
    private float[] leftBuf, rightBuf;
    //private float amplitude = 1;
    //private float targetAmplitude = 1;
    private boolean alive = true;
    private float alpha = 0;

    protected Audio()
    {
    }
    
    public Audio(int maxChannels) throws LineUnavailableException {

        silentSample = new SonarSample(new float[] {0}, 44100);
        Mixer mixer = AudioSystem.getMixer(null);

        sdl = (SourceDataLine) mixer.getLine(new Line.Info(SourceDataLine.class));
        sdl.open(new AudioFormat(rate, 16, 2, true, false), bufferSize * 2 * 2 * 2 * 2 * 2);
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

    public void setListener(SoundListener soundListener)
    {
        listenerMixer.setSoundListener(soundListener);
    }

    public void shutDown()
    {
        alive = false;
    }


    public void play(SonarSample sample, SoundSource soundSource, float volume, float priority) {
        //play(new SamplePlayer(sample, rate), soundSource, volume, priority);
    }

    static class DefaultSource implements SoundSource {

        private final SoundProducer producer;
        final float distanceFactor = 1f;

        DefaultSource(SoundProducer p) {
            super();
            this.producer = p;
        }

        @Override
        public float getY(float alpha) {
            return 0 + (1f - producer.getAmplitude()) * distanceFactor;
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
//        synchronized (listenerMixer) {
//            listenerMixer.addSoundProducer(p, soundSource, volume, priority);
//        }
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
        final int max16 = 32767;
        final float gain = max16;
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

        sdl.write(soundBuffer.array(), 0, bufferSize * 2 * 2);
    }

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