package nars.lab.nario.sonar.mixer;

import nars.lab.nario.sonar.SoundListener;
import nars.lab.nario.sonar.SoundProducer;
import nars.lab.nario.sonar.SoundSource;


public class Sound implements Comparable
{
    private static final double l10 = Math.log(10);
    
    private SoundProducer producer;
    private SoundSource source;
    private float volume;
    private float priority;
    
    private float x, y, z;
    private float score = 0;
    
    public float pan;
    public float amplitude;
    
    public Sound(SoundProducer producer, SoundSource source, float volume, float priority)
    {
        this.producer = producer;
        this.source = source;
        this.volume = volume;
        this.priority = priority;
    }
    
    public void update(SoundListener listener, float alpha)
    {
        x = source.getX(alpha)-listener.getX(alpha);
        y = source.getY(alpha)-listener.getY(alpha);
        
        float distSqr = x*x+y*y+z*z;
        float dist = (float)Math.sqrt(distSqr);
        
        float REFERENCE_DISTANCE = 1;
        float ROLLOFF_FACTOR = 2;
        
//        float dB = (float)(volume + (20 * (Math.log(1.0 / distSqr) / l10)));
        float dB = (float)(volume - 20*Math.log(1 + ROLLOFF_FACTOR*(dist-REFERENCE_DISTANCE)/REFERENCE_DISTANCE )/ l10);
        dB = Math.min(dB, +6);
//      dB = Math.max(dB, MIN_GAIN);
        
        score = dB*priority;

//        double angle = WMath.atan2(y, x);
		
        float p = -x/320.0f;
        if (p<-1) p = -1;
        if (p>1) p = 1;
        float dd = distSqr/16;
        if (dd>1) dd = 1;
        pan =(p*dd);
        amplitude = volume*1f;
    }

    public void read(float[] buf, int readRate)
    {
        producer.read(buf, readRate);
    }

    public void skip(int samplesToSkip, int readRate)
    {
        producer.skip(samplesToSkip, readRate);
    }

    public boolean isLive()
    {
        return producer.isLive();
    }

    public int compareTo(Object o)
    {
        Sound s = (Sound)o;
        if (s.score>score) return 1;
        if (s.score<score) return -1;
        return 0;
    }
}