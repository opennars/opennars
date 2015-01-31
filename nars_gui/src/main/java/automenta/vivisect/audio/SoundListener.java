package automenta.vivisect.audio;

public interface SoundListener extends SoundSource {

    public static final SoundListener zero = new SoundListener() {

        @Override
        public float getX(float alpha) {
            return 0;
        }

        @Override
        public float getY(float alpha) {
            return 0;
        }
    };

}