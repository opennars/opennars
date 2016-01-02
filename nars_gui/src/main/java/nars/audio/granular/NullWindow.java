package nars.audio.granular;

/**
 * Created by me on 9/11/15.
 */
public class NullWindow implements GrainWindow {

    private final int samples;

    public NullWindow(int samples) {
        this.samples = samples;
    }

    @Override
    public int getSize() {
        return samples;
    }

    @Override
    public float getFactor(int offset) {
        return 1.0f;
    }
}
