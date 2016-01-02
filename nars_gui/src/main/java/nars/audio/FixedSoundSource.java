package nars.audio;

/**
 * @author Administrator
 */
public class FixedSoundSource implements SoundSource
{
	private final float x;
	private final float y;

	public FixedSoundSource(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public FixedSoundSource(SoundSource soundSource)
	{
		x = soundSource.getX(1);
		y = soundSource.getY(1);
	}

    @Override
	public float getX(float alpha)
    {
        return x;
    }

    @Override
	public float getY(float alpha)
    {
        return y;
    }
}