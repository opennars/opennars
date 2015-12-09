package nars.audio.granular.depr;

/**
 *
 * @author David Nadeau
 */
public enum Envelope {

    NONE, TRAPEXIUM;

    //Create envelope [0,1]
    //envelope grain by multiplying grain * envelope
    double[][] createTrapexiumEnvelope(int size, int channels) {
        double[][] trapezium = new double[channels][size];

        double attack = 0.2,
                sustain = 0.4,
                release = 0.4;
        double attackIncrement = 1 / (attack * size);
        double sustainIncrement = 1;
        double releaseIncrement = -1 * (1 / (release * size));

        for (int c = 0; c < channels; c++) {
            trapezium[c][0] = 0;
            for (int i = 1; i < size; i++) {
                if (i < attack * size) {
                    trapezium[c][i] = trapezium[c][i - 1] + attackIncrement;
                } else if (i < (attack + sustain) * size) {
                    trapezium[c][i] = sustainIncrement;
                } else {
                    trapezium[c][i] = trapezium[c][i - 1] + releaseIncrement;
                }
            }
        }
        return trapezium;
    }
    double[][] createEmpty(int size, int channels) {
        double[][] e = new double[channels][size];
        for (int i = 0; i < channels; i++) {
            for (int j = 0; j < size; j++) {
                e[i][j] = 1;
            }
        }
        return e;
    }

}
