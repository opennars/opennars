/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.audio.granular.depr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author David Nadeau
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class Wave {

    private int sampleCount;
    private final int bitsPerSample;
    private final int channels;
    private final int sampleRate;
    private final int frequency;
    private List<String> data;
    private final String name;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public Wave(int sc, int bps, int c, int sr, int f, String n) {
        data = new ArrayList();
        sampleCount = sc;
        bitsPerSample = bps;
        channels = c;
        sampleRate = sr;
        frequency = f;
        name = n;
        addHeaders();
    }

    public void addHeaders() {
        String[] headers = {
            "SAMPLES:\t" + sampleCount,
            "BITSPERSAMPLE:\t" + bitsPerSample,
            "CHANNELS:\t" + channels,
            "SAMPLERATE:\t" + sampleRate,
            "NORMALIZED:\tFALSE"
        };
        for (int i = 0; i < headers.length; i++) {
            data.add(i, headers[i]);
        }
    }

    public void stripHeader() {
        for (int i = 0; i < 5; i++) {
            data.remove(0);
        }
    }

    //return a linear interpolation of two values
    public int interpolate(String a, String b) {
        return (Integer.parseInt(a) + Integer.parseInt(b)) / 2;
    }

    //find global min and global max values in list
    public int[] findGlobalMaximums(int[][] s) {
        int c1, c2;
        int[] m = new int[2];
        for (int i = 0; i < s.length; i++) {
            for (int j = 0; j < getNumChannels(); j++) {
                m[0] = s[j][i] < m[0] ? s[j][i] : m[0];
                m[1] = s[j][i] > m[1] ? s[j][i] : m[1];
            }
        }
        return m;
    }

    //change amplitude of sample to reside in acceptable range
    public int normalize(int min, int max, int i) {
        double minPossible = 0 - Math.pow(2, 15);
        double maxPossible = Math.pow(2, 15) - 1;
        double half = (max - min) / 2;

        double f1 = (double) (i - min) / (max - min);

        return (int) ((f1 < 0.5)
                ? minPossible - (2 * (minPossible * ((half * f1) / half)))
                : 2 * maxPossible * ((half * (f1 - 0.5)) / half));
    }

    //return immutable sample table
    public List<String> getData() {
        return Collections.unmodifiableList(data);
    }

    //waves will override this method
    public void synthesize() {
    }

    public String getType() {
        return "defualt";
    }

    public void addSample(String s) {
        data.add(s);
    }

    public int getSampleCount() {
        return data.size();
    }
    public void setSampleCount(int count) {
        sampleCount = count;
    }
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public int getNumChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getName() {
        return name;
    }

    public void clearData() {
        data = new LinkedList<>();
    }

}
