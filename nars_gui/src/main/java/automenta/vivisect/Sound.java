package automenta.vivisect;


import automenta.vivisect.audio.SoundListener;
import automenta.vivisect.audio.SoundProducer;
import automenta.vivisect.audio.SoundSource;

/** Auditory element */
public class Sound implements SoundSource, Comparable
{
    private static final double l10 = Math.log(10);
    
    private SoundProducer producer;
    private SoundSource source;
    private float volume;
    private float priority;
    
    private float x, y, z;
    private float score = 0;
    
    public float pan = 0;
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
		
        float p = -x;
        if (p<-1) p = -1;
        if (p>1) p = 1;

        pan = p;
        amplitude = volume / (1f + dist); //TODO i added /dist divisor
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

    @Override
    public float getX(float alpha) {
        return source.getX(alpha);
    }

    @Override
    public float getY(float alpha) {
        return source.getY(alpha);
    }
}