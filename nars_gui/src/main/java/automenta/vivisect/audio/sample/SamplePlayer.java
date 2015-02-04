package automenta.vivisect.audio.sample;

import automenta.vivisect.audio.SoundProducer;


public class SamplePlayer implements SoundProducer {
    private SonarSample sample;
    private float pos = 0;
    public boolean alive = true;
    private float rate;
    
    public SamplePlayer(SonarSample sample, float rate)
    {
        this.rate = rate;
        this.sample = sample;
    }
    
    public float read(float[] buf, int readRate)
    {
        float step = (sample.rate*rate)/readRate;
        
        for (int i=0; i<buf.length; i++)
        {
            if (pos>=sample.buf.length)
            {
                buf[i] = 0;
                alive = false;
            }
            else
            {
                buf[i]=sample.buf[(int)(pos)];
            }
            pos+=step;
        }
        
        return 1;
    }

    public void skip(int samplesToSkip, int readRate)
    {
        float step = sample.rate/readRate;
        pos+=step*samplesToSkip;
        
        if (pos>=sample.buf.length)
        {
            alive = false;
        }
    }

    public boolean isLive()
    {
        return alive;
    }

    public void stop() {
        this.alive = false;
    }
}