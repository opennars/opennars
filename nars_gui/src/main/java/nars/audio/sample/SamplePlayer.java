package nars.audio.sample;

import nars.audio.SoundProducer;


public class SamplePlayer implements SoundProducer {
    private final SonarSample sample;
    private float pos = 0;
    public boolean alive = true;
    private final float rate;

    
    public SamplePlayer(SonarSample sample, float rate)
    {
        this.rate = rate;
        this.sample = sample;
    }
    
    @Override
    public float read(float[] buf, int readRate)
    {
        float step = (sample.rate*rate)/readRate;

        float[] sb = sample.buf;

        for (int i=0; i<buf.length; i++)
        {

            if (pos>= sb.length) {
                buf[i] = 0;
                alive = false;
            }
            else
            {
                buf[i]= sb[(int)(pos)];
            }
            pos+=step;
        }

        return 1;
    }

    @Override
    public void skip(int samplesToSkip, int readRate)
    {
        float step = sample.rate/readRate;
        pos+=step*samplesToSkip;
        
        if (pos>=sample.buf.length)
        {
            alive = false;
        }
    }

    @Override
    public boolean isLive()
    {
        return alive;
    }

    @Override
    public void stop() {
        alive = false;
    }
}