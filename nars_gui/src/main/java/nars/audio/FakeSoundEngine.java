package nars.audio;

import nars.Audio;
import nars.audio.sample.SonarSample;

import javax.sound.sampled.LineUnavailableException;


public class FakeSoundEngine extends Audio
{
    public FakeSoundEngine(int maxChannels) throws LineUnavailableException {
        super(maxChannels);
    }

    @Override
    public void setListener(SoundListener soundListener)
    {
    }

    @Override
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

    @Override
    public void clientTick(float alpha)
    {
    }

    @Override
    public void tick()
    {
    }

    @Override
    public void run()
    {
    }
}