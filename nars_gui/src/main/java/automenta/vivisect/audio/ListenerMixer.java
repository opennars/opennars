package automenta.vivisect.audio;

import automenta.vivisect.Sound;

import java.util.*;


public class ListenerMixer implements StereoSoundProducer
{
    public final List<Sound> sounds = Collections.synchronizedList( new ArrayList<>() );
    private float[] buf = new float[0];
    private int maxChannels;
    private SoundListener soundListener;

    public ListenerMixer(int maxChannels)
    {
        this.maxChannels = maxChannels;
    }

    public void setSoundListener(SoundListener soundListener)
    {
        this.soundListener = soundListener;
    }

    public void addSoundProducer(SoundProducer producer, SoundSource soundSource, float volume, float priority)
    {
        //sounds.add(new Sound(producer, soundSource, volume, priority));
    }

    public void update(float alpha)
    {
        for (Iterator it = sounds.iterator(); it.hasNext();)         {

            Sound sound = (Sound) it.next();

            if (soundListener!=null)
                sound.update(soundListener, alpha);

            if (!sound.isLive()) {
                it.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public float read(float[] leftBuf, float[] rightBuf, int readRate)
    {
        if (buf.length != leftBuf.length) buf = new float[leftBuf.length];

        if (sounds.size() > maxChannels) {
            Collections.sort(sounds);
        }

        Arrays.fill(leftBuf, 0);
        Arrays.fill(rightBuf, 0);
        float maxAmplitude = 0;

        for (int i = 0; i < sounds.size(); i++)        {
            Sound sound = sounds.get(i);

            if (i < maxChannels) {
                sound.read(buf, readRate);
                final float rp = (sound.pan<0?1:1-sound.pan)*sound.amplitude;
                final float lp = (sound.pan>0?1:1+sound.pan)*sound.amplitude;
                final int l = leftBuf.length;


                for (int j = 0; j < l; j++) {
                    float lb = leftBuf[j];
                    float rb = rightBuf[j];
                    final float bj = buf[j];
                    lb += bj*lp;
                    rb += bj*rp;
                    if (lb>maxAmplitude) maxAmplitude = lb;
                    if (rb>maxAmplitude) maxAmplitude = rb;
                    leftBuf[j] = lb;
                    rightBuf[j] = rb;
                }
            }
            else
            {
                sound.skip(leftBuf.length, readRate);
            }
        }
        
        return maxAmplitude;
    }

    public void skip(int samplesToSkip, int readRate)
    {
        for (int i = 0; i < sounds.size(); i++) {
            Sound sound = sounds.get(i);
            sound.skip(samplesToSkip, readRate);
        }
    }
}