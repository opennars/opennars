package nars.audio.granular.depr;

import nars.util.data.random.XORShiftRandom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author David Nadeau
 */
public class Granulator extends Wave {

    private static final Random random = new XORShiftRandom();
    private int REPEAT = 1;
    private final ArrayList<String> samples;
    private List<int[][]> grains;
    private int wavetableIndex = 0;

    public Granulator(int sc, int bps, int c, int sr, int f, ArrayList<String> l,
            String n) {
        super(sc, bps, c, sr, f, n);
        samples = l;
    }

    public void synthesize(int size, double randDur, int numGrains, double pitch,
            double randPitch,
            int distance, int density, int duration, boolean SYNCMODE) {

        //grains = createGrains(size, randDur, pitch, randPitch,
        //        Envelope.TRAPEXIUM);
        super.clearData();
        grains = grainDistance(distance);
        REPEAT = numGrains;

        for (int i = 0; i < REPEAT; i++) {
            int[][] grainCloud = grainDensity(density, duration, SYNCMODE);
            print(grainCloud);
            System.out.println(i + "th : GRAIN COMPLETE");
        }

        super.setSampleCount(super.getSampleCount());
        super.addHeaders();
    }

    private void print(int[][] grainCloud) {
        for (int i = 0; i < grainCloud[0].length; i++) {
            addSample(grainCloud[0][i] + "\t" + grainCloud[1][i]);
        }
    }
    public void createGrains(int numberOfGrains, int dur, double randDur,
            double pitch, double randPitch, Envelope envType) {
        grains = new LinkedList();
        int[][] grain;

        double amps = 0;
        int duration = dur;
        double pitchh = pitch;

        //while there are still samples in the input source,
        //and we still have more grains to create
        while (Math.ceil(amps) < samples.size() && numberOfGrains-- > 0) {
            //add random deviation to pitch and duration
            pitchh = pitch + (((randPitch * pitch) * (Math.random() > 0.5
                    ? 1 : -1) * Math.random()));
            duration = (int) (dur + (((randDur * dur) * (Math.random() > 0.5
                    ? 1 : -1) * Math.random())));

            grain = new int[super.getNumChannels()][duration];
            for (int i = 0; i < duration; i++) {
                for (int j = 0; j < super.getNumChannels(); j++) {
                    if (Math.ceil(amps) < samples.size()) {
                        //decimal part of 0.05 and lower are considered integers
                        grain[j][i] = amps - Math.floor(amps) > 0.05 ? super.interpolate(
                                samples.get((int) amps).split("\t")[j],
                                samples.get((int) Math.ceil(amps))
                                        .split("\t")[j]) : Integer.parseInt(
                                samples.get((int) amps).split("\t")[j]);
                    }
                }
                amps += pitchh;
            }

            if (grain[0].length > 0) {
                int[][] envelopped = envelopeGrain(envType, grain);
                grains.add(envelopped);
            }
        }
        System.out.println("FINSIHED CREATING GRAINS");
    }
    private List<int[][]> grainDistance(int distance) {

        List<int[][]> tempgrains = new ArrayList(grains.size());
        for (int[][] grain : grains) {
            int[][] g = new int[super.getNumChannels()][grain[0].length + distance];
            int i = distance;
            while (i < grain[0].length + distance) {
                for (int j = 0; j < super.getNumChannels(); j++)
                    g[j][i] = grain[j][i - distance];
                i++;
            }
            tempgrains.add(g);
        }
        return tempgrains;
    }

    private int[][] envelopeGrain(Envelope env, int[][] grain) {
        double[][] envelopedShape;

        //create the requested envelope
        switch (env) {
            case TRAPEXIUM:
                envelopedShape = env.createTrapexiumEnvelope(grain[0].length,
                        super.getNumChannels());
                break;
            case NONE:
            default:
                envelopedShape = env.createEmpty(grain[0].length, super
                        .getNumChannels());
        }

        int[][] g = new int[super.getNumChannels()][grain[0].length];
        for (int i = 0; i < grain[0].length; i++)
            for (int j = 0; j < super.getNumChannels(); j++)
                g[j][i] = (int) (grain[j][i] * envelopedShape[j][i]);
        return g;
    }
    private int[][] grainDensity(int density, int duration, boolean SYNCMODE) {
        int[][] grainCloud = new int[super.getNumChannels()][duration];
        int min = 0, max = 0;

        for (int i = 0; i < density; i++) {
            int[][] grain;
            grain = SYNCMODE ? grains.get(wavetableIndex++ % grains.size()) : grains.get(random.nextInt(grains.size()));
            //if grain is larger then grainCloud duration, only play a portion
            //of the grain.
            int grainPortion = (grainCloud[0].length < grain[0].length)
                    ? grainCloud[0].length - 1
                    : grain[0].length - 1;

            int offset = random.nextInt(
                    grainCloud[0].length - grainPortion);

            for (int j = 0; j < super.getNumChannels(); j++) {
                for (int k = 0; k < grainPortion; k++) {
                    grainCloud[j][offset + k] += grain[j][k];
                    min = grainCloud[j][offset + k] < min
                            ? grainCloud[j][offset + k]
                            : min;
                    max = grainCloud[j][offset + k] > max
                            ? grainCloud[j][offset + k]
                            : max;
                }
            }

        }

        int[][] normCloud = new int[super.getNumChannels()][duration];
        for (int j = 0; j < duration; j++) {
            for (int c = 0; c < super.getNumChannels(); c++) {
                normCloud[c][j] = super.normalize(min, max, grainCloud[c][j]);
            }
        }
        return normCloud;
    }

}
