package automenta.vivisect.audio;

/**
 * @author Administrator
 */
public class FixedSoundSource implements SoundSource
{
	private float x;
	private float y;

	public FixedSoundSource(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public FixedSoundSource(SoundSource soundSource)
	{
		this.x = soundSource.getX(1);
		this.y = soundSource.getY(1);
	}

    public float getX(float alpha)
    {
        return x;
    }

    public float getY(float alpha)
    {
        return y;
    }
}