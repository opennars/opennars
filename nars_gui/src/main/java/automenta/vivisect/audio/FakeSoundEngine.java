package automenta.vivisect.audio;

import automenta.vivisect.Audio;
import automenta.vivisect.audio.sample.SonarSample;


public class FakeSoundEngine extends Audio
{
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