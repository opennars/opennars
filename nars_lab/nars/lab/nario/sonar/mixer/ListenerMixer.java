package nars.lab.nario.sonar.mixer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import nars.lab.nario.sonar.SoundListener;
import nars.lab.nario.sonar.SoundProducer;
import nars.lab.nario.sonar.SoundSource;
import nars.lab.nario.sonar.StereoSoundProducer;


public class ListenerMixer implements StereoSoundProducer
{
    private List<Sound> sounds = new ArrayList<>();
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
        sounds.add(new Sound(producer, soundSource, volume, priority));
    }

    public void update(float alpha)
    {
        for (Iterator it = sounds.iterator(); it.hasNext();)
        {
            Sound sound = (Sound) it.next();
            sound.update(soundListener, alpha);
            if (!sound.isLive())
            {
                it.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public float read(float[] leftBuf, float[] rightBuf, int readRate)
    {
        if (buf.length != leftBuf.length) buf = new float[leftBuf.length];

        if (sounds.size() > maxChannels)
        {
            Collections.sort(sounds);
        }

        Arrays.fill(leftBuf, 0);
        Arrays.fill(rightBuf, 0);
        float maxAmplitude = 0;
        for (int i = 0; i < sounds.size(); i++)
        {
            Sound sound = (Sound) sounds.get(i);
            if (i < maxChannels)
            {
                sound.read(buf, readRate);
                float rp = (sound.pan<0?1:1-sound.pan)*sound.amplitude;
                float lp = (sound.pan>0?1:1+sound.pan)*sound.amplitude;
                for (int j = 0; j < leftBuf.length; j++)
                {
                    leftBuf[j] += buf[j]*lp;
                    rightBuf[j] += buf[j]*rp;
                    if (leftBuf[j]>maxAmplitude) maxAmplitude = leftBuf[j];
                    if (rightBuf[j]>maxAmplitude) maxAmplitude = rightBuf[j];
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
        for (int i = 0; i < sounds.size(); i++)
        {
            Sound sound = (Sound) sounds.get(i);
            sound.skip(samplesToSkip, readRate);
        }
    }
}