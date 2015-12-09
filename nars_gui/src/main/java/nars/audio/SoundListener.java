package nars.audio;

public interface SoundListener extends SoundSource {

    SoundListener zero = new SoundListener() {

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