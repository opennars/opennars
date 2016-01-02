package nars.audio;

import nars.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ListenerMixer implements StereoSoundProducer
{
    public final List<Sound> sounds =
            Collections.synchronizedList( new ArrayList<>() );

    private float[] buf = new float[0];

    private final int maxChannels;

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
        sounds.add(new Sound(producer, soundSource, volume, priority));
    }

    public void update(float alpha)
    {
        boolean updating = (soundListener!=null);

        for (int i = 0; ; ) {
            if ((i >= 0) && (i < sounds.size())) {
                Sound sound = sounds.get(i);
                if (updating)
                    sound.update(soundListener, alpha);

                if (!sound.isLive()) {
                    sounds.remove(i);
                } else {
                    i++;
                }
            }
            else
                break;
        }

//        for (Iterator it = sounds.iterator(); it.hasNext();)         {
//
//            Sound sound = (Sound) it.next();
//
//            if (updating)
//                sound.update(soundListener, alpha);
//
//            if (!sound.isLive()) {
//                it.remove();
//            }
//        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public float read(float[] leftBuf, float[] rightBuf, int readRate)
    {
        if (buf.length != leftBuf.length)
            buf = new float[leftBuf.length];

        if (sounds.size() > maxChannels) {
            Collections.sort(sounds);
        }

        Arrays.fill(leftBuf, 0);
        Arrays.fill(rightBuf, 0);
        float maxAmplitude = 0;

        for (int i = 0; i < sounds.size() && i >= 0; )  {
            Sound sound = sounds.get(i);

            if (i < maxChannels) {
                float[] buf = this.buf;

                sound.read(buf, readRate);

                float pan = sound.pan;

                float rp = (pan <0?1:1- pan)*sound.amplitude;
                float lp = (pan >0?1:1+ pan)*sound.amplitude;

                int l = leftBuf.length;

                for (int j = 0; j < l; j++) {
                    float lb = leftBuf[j];
                    float rb = rightBuf[j];
                    float bj = buf[j];
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

            if (!sound.isLive())
                sounds.remove(i);
            else
                i++;
        }
        
        return maxAmplitude;
    }

    @Override
    public void skip(int samplesToSkip, int readRate)
    {
        for (Sound sound : sounds) {
            sound.skip(samplesToSkip, readRate);
        }
    }
}