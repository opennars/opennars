package nars.audio;

import nars.Audio;
import nars.audio.sample.SonarSample;

import javax.sound.sampled.LineUnavailableException;


public class FakeSoundEngine extends Audio
{
    public FakeSoundEngine(int maxChannels) throws LineUnavailableException {
        super(maxChannels);
    }

    public void setListener(SoundListener soundListener)
    {
    }

    public void shutDown()
    {
    }

    public SonarSample loadSample(String resourceName)
    {
        return null;
    }

    public void play(SonarSample sample, SoundSource soundSource, float volume, float priority, float rate)
    {
    }

    public void clientTick(float alpha)
    {
    }

    public void tick()
    {
    }

    public void run()
    {
    }
}