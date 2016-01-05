package nars.util.signal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.arraycopy;
import static java.lang.System.out;

/**
 * ============================================================================
 * @author Vladimir Kulyukin
 * 
 * An implementation of 1D Ordered Fast Haar Wavelet Transform, Ordered Fast Inverse 
 * Haar Wavelet Transform, Inplace Fast Haar Wavelet Transform, and Inplace Fast Inverse 
 * Haar Wavelet Transform as specified in 
 * 
 * Ch. 01 of "Wavelets Made Easy" by Yves Nievergelt & "Ripples in Mathematics" by
 * A. Jensen, A. La Cour-Harbo.
 * 
 * Bugs to vladimir dot kulyukin at gmail dot com
 * ============================================================================
    https://github.com/cscheiblich/JWave/
 */
public enum OneDHaar {
    ;

    private static final double FSNORM = Math.sqrt(2);
    private static final double FDNORM = 1/FSNORM;
    public static final double log2 = Math.log(2);
    
    private static final double ISNORM = FDNORM;
    private static final double IDNORM = FSNORM;
    
    private static void displaySample(double[] sample) {
        out.print("Sample: ");
        for (double aSample : sample) {
            out.print(aSample + " ");
        }
        out.println();
    }

    private static boolean isPowerOf2(int n) {
        if (n < 1) {
            return false;
        } else {
            double p_of_2 = (Math.log(n) / log2);
            return Math.abs(p_of_2 - Math.round((int) p_of_2)) == 0;
        }
    }
    
    public static int largestPowerOf2NoGreaterThan(int i) {
        if ( isPowerOf2(i) )
            return i;
        else {
            int curr = i-1;
            while ( curr > 0 ) {
                if ( isPowerOf2(curr) ) {
                    return curr;
                }
                else {
                    --curr;
                }
            }
            return 0;
        }
    }
    
    public static double[] largestSubsignalOfPowerOf2(double[] signal) {
        if ( isPowerOf2(signal.length) )
            return signal;
        else {
            int i = largestPowerOf2NoGreaterThan(signal.length);
            if ( i == 0 ) return null;
            double[] subsignal = new double[i];
            arraycopy(signal, 0, subsignal, 0, i);
            return subsignal;
        }
    }

    // compute in-place fast haar wavelet transform. 
    public static void inPlaceFastHaarWaveletTransform(double[] sample) {
        if (sample.length < 2) {
            return;
        }
        if (!isPowerOf2(sample.length)) {
            return;
        }
        int num_sweeps = (int) (Math.log(sample.length) / log2);
        inPlaceFastHaarWaveletTransform(sample, num_sweeps);
    }
    // compute in-place fast haar wavelet transform.
    public static void inPlaceFastHaarWaveletTransform(float[] sample) {
//        if (sample.length < 2) {
//            throw new RuntimeException(sample.length + " is not enough samples");
//        }
//        if (!isPowerOf2(sample.length)) {
//            throw new RuntimeException(sample.length + " is not power of 2");
//        }
        int num_sweeps = (int) (Math.log(sample.length) / log2);
        inPlaceFastHaarWaveletTransform(sample, num_sweeps);
    }

    // apply in-place fast haar wavelet transform for num_sweeps sweeps.
    public static void inPlaceFastHaarWaveletTransform(float[] sample, int num_iters) {
        if (sample.length < 2) {
            throw new RuntimeException(sample.length + " is not enough samples");
        }
        if (!isPowerOf2(sample.length)) {
            throw new RuntimeException(sample.length + " is not power of 2");
        }
        int NUM_SAMPLE_VALS = sample.length; // number of values in the sample
        int n = (int) (Math.log(NUM_SAMPLE_VALS) / Math.log(2));
        if (num_iters < 1 || num_iters > n) {
            throw new RuntimeException(sample.length + " invalid number sweeps");
        }
        int GAP_SIZE = 2; // number of elements b/w averages
        int I = 1; // index increment
        for (int ITER_NUM = 1; ITER_NUM <= num_iters; ITER_NUM++) {
            NUM_SAMPLE_VALS /= 2;
            for (int K = 0; K < NUM_SAMPLE_VALS; K++) {
                int KGAPSIZE = GAP_SIZE * K;
                float sampleAtKGAPSIZE = sample[KGAPSIZE];
                float sampleAtKGAPSIZEPlusI = sample[KGAPSIZE + I];
                float a = (sampleAtKGAPSIZE + sampleAtKGAPSIZEPlusI) / 2;
                float c = (sampleAtKGAPSIZE - sampleAtKGAPSIZEPlusI) / 2;
                sample[KGAPSIZE] = a;
                sample[KGAPSIZE + I] = c;
            }
            I = GAP_SIZE;
            GAP_SIZE *= 2;
        }
    }
    // apply in-place fast haar wavelet transform for num_sweeps sweeps.
    private static void inPlaceFastHaarWaveletTransform(double[] sample, int num_iters) {
        if (sample.length < 2) {
            return;
        }
        if (!isPowerOf2(sample.length)) {
            return;
        }
        int NUM_SAMPLE_VALS = sample.length; // number of values in the sample
        int n = (int) (Math.log(NUM_SAMPLE_VALS) / Math.log(2));
        if (num_iters < 1 || num_iters > n) {
            return;
        }
        int GAP_SIZE = 2; // number of elements b/w averages
        int I = 1; // index increment
        for (int ITER_NUM = 1; ITER_NUM <= num_iters; ITER_NUM++) {
            NUM_SAMPLE_VALS /= 2;
            for (int K = 0; K < NUM_SAMPLE_VALS; K++) {
                int KGAPSIZE = GAP_SIZE * K;
                double sampleAtKGAPSIZE = sample[KGAPSIZE];
                double sampleAtKGAPSIZEPlusI = sample[KGAPSIZE + I];
                double a = (sampleAtKGAPSIZE + sampleAtKGAPSIZEPlusI) / 2;
                double c = (sampleAtKGAPSIZE - sampleAtKGAPSIZEPlusI) / 2;
                sample[KGAPSIZE] = a;
                sample[KGAPSIZE + I] = c;
            }
            I = GAP_SIZE;
            GAP_SIZE *= 2;
        }
    }

    // do the n-th sweep of In-Place Fast Haar Wavelet Transform
    public static void doNthSweepOfInPlaceFastHaarWaveletTransform(double[] sample, int sweep_number) {
        if (sample.length % 2 != 0 || sample.length == 0) {
            return;
        }
        int I = (int) (Math.pow(2.0, sweep_number - 1));
        int GAP_SIZE = (int) (Math.pow(2.0, sweep_number));
        int NUM_SAMPLE_VALS = sample.length;
        int n = (int) (Math.log(NUM_SAMPLE_VALS) / Math.log(2));
        if (sweep_number < 1 || sweep_number > n) {
            return;
        }
        NUM_SAMPLE_VALS /= (int) (Math.pow(2.0, sweep_number));
        double c;
        double a;
        for (int K = 0; K < NUM_SAMPLE_VALS; K++) {
            a = (sample[GAP_SIZE * K] + sample[GAP_SIZE * K + I]) / 2;
            c = (sample[GAP_SIZE * K] - sample[GAP_SIZE * K + I]) / 2;
            sample[GAP_SIZE * K] = a;
            sample[GAP_SIZE * K + I] = c;
        }
    }
    
    public static void primitiveDoublesToFile(double[] signal, String filePath) throws IOException {
        FileWriter file = new FileWriter(filePath); 
        BufferedWriter buffer = new BufferedWriter(file);
        
        for(double d: signal) {
            buffer.write(Double.toString(d));
            buffer.newLine();
        }
        buffer.flush();
        //Close the input stream
        buffer.close();
        file.close();
    }
    
    private static double[] fileToPrimitiveDoubles(String filePath) throws IOException {
        FileInputStream fstream = new FileInputStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        List<Double> aryOfDoubles = new ArrayList<>();

        String strLine;
        //Read File Line By Line
        while (( strLine = br.readLine()) != null )   {
            // Print the content on the console
            out.println (strLine);
            aryOfDoubles.add(Double.valueOf(strLine));
        }

        //Close the input stream
        br.close();
        //aryOfDou
        double[] prims = new double[aryOfDoubles.size()];
        int i = 0;
        for(double d: aryOfDoubles) {
            prims[i++] = d;
        }

        return prims;
    }

    private static void orderedFastHaarWaveletTransform(double[] signal) {
        int n = signal.length;
        // if n is not an integral power of 2, then return
        if (!isPowerOf2(n)) {
            return;
        }
        // compute the number of sweeps; e.g., if n = 8, then NUM_SWEEPS is 3.
        int NUM_SWEEPS = (int) (Math.log(n) / Math.log(2.0));
        double acoeff, ccoeff;
        if (NUM_SWEEPS == 1) {
            acoeff = (signal[0] + signal[1]) / 2.0;
            ccoeff = (signal[0] - signal[1]) / 2.0;
            signal[0] = acoeff;
            signal[1] = ccoeff;
            return;
        }
        for (int SWEEP_NUM = 1; SWEEP_NUM < NUM_SWEEPS; SWEEP_NUM++) {
            // size is the number of a-coefficients and c-coefficients at
            // sweep SWEEP_NUM. for example, if the signal has 8 elements;
            // at sweep 1, we have 4 a-coefficients and 4 c-coefficients;
            // at sweep 2, we have 2 a-coefficients and 2 c-coefficients;
            // at sweep 3, we have 1 a-coefficient and 1 c-coefficient.
            int size = (int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM));
            double[] acoeffs = new double[size];
            double[] ccoeffs = new double[size];
            int ai = 0; // index over acoeffs
            int ci = 0; // index over ccoeffs
            // end is the index of the last a-coefficient in signal[] at
            // sweep SWEEP_NUM. For example, if NUM_SWEEPS = 3, then
            // at sweep 1, end = 2^{3-1+1} - 1 = 7
            // at sweep 2, end = 2^{3-2+1} - 1 = 3
            int end = ((int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM + 1))) - 1;
            for (int i = 0; i <= end; i += 2) {
                acoeffs[ai++] = (signal[i] + signal[i + 1]) / 2.0;
                ccoeffs[ci++] = (signal[i] - signal[i + 1]) / 2.0;
            }

            // the following for-loop places the a-coeffs into the left half of the array 
            // and c-coeffs into the right half of the array
            // for example, assume that the length of signal is 3 and NUM_SWEEPS = 3, 
            // then at sweep 1, size = 4. Thus,
            // signal[0] = a^{2}_{0}, signal[0+4] = c^{2}_{0}
            // signal[1] = a^{2}_{1}, signal[1+4] = c^{2}_{1},
            // signal[2] = a^{2}_{2}, signal[2+4] = c^{2}_{c},
            // signal[3] = a^{2}_{3}, signal[3+4] = c^{2}_{3}
            // in other words, 
            // signal[0], signal[1], signal[2], signal[3] are the 4 a-coefficients and
            // signal[4], signal[5], signal[6], signal[7] are the 4 c-coefficients
            // at sweep 2, size = 2. Thus,
            // signal[0] = a^{1}_{0}, signal[0+2] = c^{1}_{0}
            // signal[1] = a^{1}_{1}, signal[1+2] = c^{1}_{1}
            for (int i = 0; i < size; i++) {
                signal[i] = acoeffs[i];
                signal[i + size] = ccoeffs[i];
            }
            //System.out.print("Forward SWEEP_NUM: " + SWEEP_NUM + " ");
            //OneDHaar.displaySample(signal);
        }
        // now we compute a^{0}_{0} and c^{0}_{0} at store them
        // in signal[0] and signal[1]
        acoeff = (signal[0] + signal[1]) / 2.0;
        ccoeff = (signal[0] - signal[1]) / 2.0;
        signal[0] = acoeff;
        signal[1] = ccoeff;
        //System.out.print("Forward SWEEP_NUM: " + NUM_SWEEPS + " ");
        //OneDHaar.displaySample(signal);
    }
    
    // same as orderedFastHaarWaveletTransform except takes its input from a
    // a file where each sample is a double written on a separate line.
    public static double[] orderedFastHaarWaveletTransform(String filePath) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedFastHaarWaveletTransform(signal);
        return signal;
    }
    
    // the transform as defined on p. 21 in "Ripples in Mathematics."
    private static void orderedNormalizedFastHaarWaveletTransform(double[] sample) {
        int n = sample.length;
        // if n is not an integral power of 2, then return
        if (!isPowerOf2(n)) {
            return;
        }
        // compute the number of sweeps; e.g., if n = 8, then NUM_SWEEPS is 3.
        int NUM_SWEEPS = (int) (Math.log(n) / Math.log(2.0));
        double acoeff, ccoeff;
        if (NUM_SWEEPS == 1) {
            acoeff = FSNORM * (sample[0] + sample[1])/2.0;
            ccoeff = FDNORM * (sample[0] - sample[1]);
            sample[0] = acoeff;
            sample[1] = ccoeff;
            return;
        }
        for (int SWEEP_NUM = 1; SWEEP_NUM < NUM_SWEEPS; SWEEP_NUM++) {
            // size is the number of a-coefficients and c-coefficients at
            // sweep SWEEP_NUM. for example, if the sample has 8 elements;
            // at sweep 1, we have 4 a-coefficients and 4 c-coefficients;
            // at sweep 2, we have 2 a-coefficients and 2 c-coefficients;
            // at sweep 3, we have 1 a-coefficient and 1 c-coefficient.
            int size = (int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM));
            double[] acoeffs = new double[size];
            double[] ccoeffs = new double[size];
            int ai = 0; // index over acoeffs
            int ci = 0; // index over ccoeffs
            // end is the index of the last a-coefficient in sample[] at
            // sweep SWEEP_NUM. For example, if NUM_SWEEPS = 3, then
            // at sweep 1, end = 2^{3-1+1} - 1 = 7
            // at sweep 2, end = 2^{3-2+1} - 1 = 3
            int end = ((int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM + 1))) - 1;
            for (int i = 0; i <= end; i += 2) {
                acoeffs[ai++] = FSNORM * (sample[i] + sample[i + 1])/2.0;
                ccoeffs[ci++] = FDNORM * (sample[i] - sample[i + 1]);
            }

            // the following for-loop places the a-coeffs into the left half of the array 
            // and c-coeffs into the right half of the array
            // for example, assume that the length of sample is 3 and NUM_SWEEPS = 3, 
            // then at sweep 1, size = 4. Thus,
            // sample[0] = a^{2}_{0}, sample[0+4] = c^{2}_{0}
            // sample[1] = a^{2}_{1}, sample[1+4] = c^{2}_{1},
            // sample[2] = a^{2}_{2}, sample[2+4] = c^{2}_{c},
            // sample[3] = a^{2}_{3}, sample[3+4] = c^{2}_{3}
            // in other words, 
            // sample[0], sample[1], sample[2], sample[3] are the 4 a-coefficients and
            // sample[4], sample[5], sample[6], sample[7] are the 4 c-coefficients
            // at sweep 2, size = 2. Thus,
            // sample[0] = a^{1}_{0}, sample[0+2] = c^{1}_{0}
            // sample[1] = a^{1}_{1}, sample[1+2] = c^{1}_{1}
            for (int i = 0; i < size; i++) {
                sample[i] = acoeffs[i];
                sample[i + size] = ccoeffs[i];
            }
            //System.out.print("Forward SWEEP_NUM: " + SWEEP_NUM + " ");
            //OneDHaar.displaySample(sample);
        }
        // now we compute a^{0}_{0} and c^{0}_{0} at store them
        // in sample[0] and sample[1]
        acoeff = FSNORM * (sample[0] + sample[1])/2.0;
        ccoeff = FDNORM * (sample[0] - sample[1]);
        sample[0] = acoeff;
        sample[1] = ccoeff;
        //System.out.print("Forward SWEEP_NUM: " + NUM_SWEEPS + " ");
        //OneDHaar.displaySample(sample);
    }
    
    // same as orderedNormalizedFastHaarWaveletTransform except takes its input from a
    // a file where each sample is a double written on a separate line.
    public static double[] orderedNormalizedFastHaarWaveletTransform(String filePath) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedNormalizedFastHaarWaveletTransform(signal);
        return signal;
    }
    
    // same as above but does ordered FHWT for a specified number of iterations
    private static void orderedFastHaarWaveletTransformForNumIters(double[] signal, int num_iters) {
        int n = signal.length;
        // if n is not an integral power of 2, then return
        if ( !isPowerOf2(n) ) return;
        // compute the number of sweeps; e.g., if n = 8, then NUM_SWEEPS is 3.
        int NUM_SWEEPS = (int) (Math.log(n) / Math.log(2.0));
        if ( num_iters > NUM_SWEEPS ) return;
        if ( NUM_SWEEPS == 1 ) {
            double acoeff = (signal[0] + signal[1]) / 2.0;
            double ccoeff = (signal[0] - signal[1]) / 2.0;
            signal[0] = acoeff;
            signal[1] = ccoeff;
            return;
        }
        for (int SWEEP_NUM = 1; SWEEP_NUM <= num_iters; SWEEP_NUM++) {
            // size is the number of a-coefficients and c-coefficients at
            // sweep SWEEP_NUM. for example, if the signal has 8 elements;
            // at sweep 1, we have 4 a-coefficients and 4 c-coefficients;
            // at sweep 2, we have 2 a-coefficients and 2 c-coefficients;
            // at sweep 3, we have 1 a-coefficient and 1 c-coefficient.
            int size = (int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM));
            double[] acoeffs = new double[size];
            double[] ccoeffs = new double[size];
            int ai = 0; // index over acoeffs
            int ci = 0; // index over ccoeffs
            // end is the index of the last a-coefficient in signal[] at
            // sweep SWEEP_NUM. For example, if NUM_SWEEPS = 3, then
            // at sweep 1, end = 2^{3-1+1} - 1 = 7
            // at sweep 2, end = 2^{3-2+1} - 1 = 3
            int end = ((int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM + 1))) - 1;
            for (int i = 0; i <= end; i += 2) {
                acoeffs[ai++] = (signal[i] + signal[i + 1]) / 2.0;
                ccoeffs[ci++] = (signal[i] - signal[i + 1]) / 2.0;
            }
            
            for (int i = 0; i < size; i++) {
                signal[i] = acoeffs[i];
                signal[i + size] = ccoeffs[i];
            }
            //System.out.print("Fordward SWEEP_NUM: " + NUM_SWEEPS + " ");
            //OneDHaar.displaySample(signal);
        }
    }
    
    public static double[] orderedFastHaarWaveletTransformForNumIters(String filePath, int num_iters) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedFastHaarWaveletTransformForNumIters(signal, num_iters);
        return signal;
    }
    
    // same as above but does ordered FHWT for a specified number of iterations but
    // normalized.
    private static void orderedNormalizedFastHaarWaveletTransformForNumIters(double[] sample, int num_iters) {
        int n = sample.length;
        // if n is not an integral power of 2, then return
        if ( !isPowerOf2(n) ) return;
        // compute the number of sweeps; e.g., if n = 8, then NUM_SWEEPS is 3.
        int NUM_SWEEPS = (int) (Math.log(n) / Math.log(2.0));
        if ( num_iters > NUM_SWEEPS ) return;
        if ( NUM_SWEEPS == 1 ) {
            double acoeff = FSNORM * (sample[0] + sample[1]) / 2.0;
            double ccoeff = FDNORM * (sample[0] - sample[1]);
            sample[0] = acoeff;
            sample[1] = ccoeff;
            return;
        }
        for (int SWEEP_NUM = 1; SWEEP_NUM <= num_iters; SWEEP_NUM++) {
            // size is the number of a-coefficients and c-coefficients at
            // sweep SWEEP_NUM. for example, if the sample has 8 elements;
            // at sweep 1, we have 4 a-coefficients and 4 c-coefficients;
            // at sweep 2, we have 2 a-coefficients and 2 c-coefficients;
            // at sweep 3, we have 1 a-coefficient and 1 c-coefficient.
            int size = (int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM));
            double[] acoeffs = new double[size];
            double[] ccoeffs = new double[size];
            int ai = 0; // index over acoeffs
            int ci = 0; // index over ccoeffs
            // end is the index of the last a-coefficient in sample[] at
            // sweep SWEEP_NUM. For example, if NUM_SWEEPS = 3, then
            // at sweep 1, end = 2^{3-1+1} - 1 = 7
            // at sweep 2, end = 2^{3-2+1} - 1 = 3
            int end = ((int) Math.pow(2.0, (NUM_SWEEPS - SWEEP_NUM + 1))) - 1;
            for (int i = 0; i <= end; i += 2) {
                acoeffs[ai++] = FSNORM * (sample[i] + sample[i + 1])/2.0;
                ccoeffs[ci++] = FDNORM * (sample[i] - sample[i + 1]);
            }
            
            for (int i = 0; i < size; i++) {
                sample[i] = acoeffs[i];
                sample[i + size] = ccoeffs[i];
            }
            //System.out.print("Fordward SWEEP_NUM: " + NUM_SWEEPS + " ");
            //OneDHaar.displaySample(sample);
        }
    }
    
    public static double[] orderedNormalizedFastHaarWaveletTransformForNumIters(String filePath, int num_iters) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedNormalizedFastHaarWaveletTransformForNumIters(signal, num_iters);
        return signal;
    }

    private static void orderedFastInverseHaarWaveletTransform(double[] sample) {
        int n = sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        for (int L = 1; L <= n; L++) {
            int GAP = (int) (Math.pow(2.0, L - 1));
            //System.out.println("GAP == " + GAP);
            double[] restored_vals = new double[2 * GAP];
            for (int i = 0; i < GAP; i++) {
                double a0 = sample[i] + sample[GAP + i];
                double a1 = sample[i] - sample[GAP + i];
                //System.out.println("a0 = " + "sample[" + i + "] + sample[" + GAP + "]");
                //System.out.println("a1 = " + "sample[" + i + "] - sample[" + GAP + "]");
                restored_vals[2 * i] = a0;
                restored_vals[2 * i + 1] = a1;
                //System.out.println("restoredVals[" + (2 * i) + "] = " + a0);
                //System.out.println("restoredVals[" + (2 * i + 1) + "] = " + a1);
            }
            // copy restoredVals[0],   restoredVals[1], ...,  restoredVals[2*GAP-1] into
            //      sample[0], sampe[1], ..., sample[2*GAP-1]
            arraycopy(restored_vals, 0, sample, 0, 2 * GAP);
            //System.out.print("L == " + L + " ");
            //OneDHaar.displaySample(sample);
        }
    }
    
    public static double[] orderedFastInverseHaarWaveletTransform(String filePath) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedFastInverseHaarWaveletTransform(signal);
        return signal;
    }
    
    // same as above but does ordered FHWT for a specific number of iters.
    private static void orderedFastInverseHaarWaveletTransformForNumIters(double[] signal, int numIters) {
        int n = signal.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (numIters > n) {
            return;
        }
        //System.out.println("START ***************");
        for (int L = 1; L <= numIters; L++) {
            int GAP = (int) (Math.pow(2.0, L - 1));
            //System.out.println("GAP == " + GAP);
            double[] restoredVals = new double[2 * GAP];
            for (int i = 0; i < GAP; i++) {
                double a0 = signal[i] + signal[GAP + i];
                double a1 = signal[i] - signal[GAP + i];
                //System.out.println("a0 = " + "signal[" + i + "] + signal[" + GAP + "] = " + a0);
                //System.out.println("a1 = " + "signal[" + i + "] - signal[" + GAP + "] = " + a1);
                restoredVals[2 * i] = a0;
                restoredVals[2 * i + 1] = a1;
                //System.out.println("restoredVals[" + (2 * i) + "] = " + a0);
                //System.out.println("restoredVals[" + (2 * i + 1) + "] = " + a1);
            }
            // copy restoredVals[0],   restoredVals[1], ...,  restoredVals[2*GAP-1] into
            //      signal[0], sampe[1], ..., signal[2*GAP-1]
            arraycopy(restoredVals, 0, signal, 0, 2 * GAP);
            //System.out.print("Inverse SWEEP_NUM: " + L + " ");
            //OneDHaar.displaySample(signal);
        }
    }
    
    private static void thresholdSignal(double[] signal, double thresh) {
        int n = signal.length;
        double[] thresholdedSignal = new double[n];
        //System.out.println("n = " + n);
        for(int t = 0; t < n; t++) {
            thresholdedSignal[t] = Math.abs(signal[t]) > thresh ? signal[t] : 0;
        }
        
        arraycopy(thresholdedSignal, 0, signal, 0, n);
        double[] o = null;
    }
    // same as above but does ordered FHWT for a specific number of iters.
    public static void orderedFastInverseHaarWaveletTransformForNumIters(double[] signal, int numIters, double thresh) {
        int n = signal.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (numIters > n) {
            return;
        }
        
        thresholdSignal(signal, thresh);
        orderedFastInverseHaarWaveletTransformForNumIters(signal, numIters);
    }
    
    
    
    
    
    public static double[] orderedFastInverseHaarWaveletTransformForNumIters(String filePath, int num_iters) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedFastInverseHaarWaveletTransformForNumIters(signal, num_iters);
        return signal;
    }
    
    // as specified on p. 21 in "Ripples in Mathematics."
    private static void orderedNormalizedFastInverseHaarWaveletTransform(double[] sample) {
        int n = sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        for (int L = 1; L <= n; L++) {
            int GAP = (int) (Math.pow(2.0, L - 1));
            double[] restored_vals = new double[2 * GAP];
            for (int i = 0; i < GAP; i++) {
                double d = IDNORM * sample[GAP + i];
                double s = ISNORM * sample[i];
                //a0 = ISNORM * sample[i] + IDNORM * sample[GAP + i];
                //a1 = ISNORM * sample[i] - IDNORM * sample[GAP + i]/2.0;
                double a0 = s + d / 2;
                double a1 = s - d / 2;
                restored_vals[2 * i] = a0;
                restored_vals[2 * i + 1] = a1;
            }
            // copy restoredVals[0],   restoredVals[1], ...,  restoredVals[2*GAP-1] into
            //      sample[0], sampe[1], ..., sample[2*GAP-1]
            arraycopy(restored_vals, 0, sample, 0, 2 * GAP);
            //System.out.print("L == " + L + " ");
            //OneDHaar.displaySample(sample);
        }
    }
    
    public static double[] orderedNormalizedFastInverseHaarWaveletTransform(String filePath) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedNormalizedFastInverseHaarWaveletTransform(signal);
        return signal;
    }
    
    // same as orderedNormalizedFastInverseHaarWaveletTransform() but goes back for a specified number of iterations.
    private static void orderedNormalizedFastInverseHaarWaveletTransformForNumIters(double[] sample, int num_iters) {
        int n = sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (num_iters > n) {
            return;
        }
        int GAP = (int)(Math.pow(2.0, n-num_iters));
        for (int L = 1; L <= num_iters; L++) {
            double[] restored_vals = new double[2 * GAP];
            for (int i = 0; i < GAP; i++) {
                double d = IDNORM * sample[GAP + i];
                double s = ISNORM * sample[i];
                //a0 = ISNORM * sample[i] + IDNORM * sample[GAP + i];
                //a1 = ISNORM * sample[i] - IDNORM * sample[GAP + i]/2.0;
                double a0 = s + d / 2;
                double a1 = s - d / 2;
                restored_vals[2 * i] = a0;
                restored_vals[2 * i + 1] = a1;
            }
            // copy restoredVals[0],   restoredVals[1], ...,  restoredVals[2*GAP-1] into
            //      sample[0], sampe[1], ..., sample[2*GAP-1]
            arraycopy(restored_vals, 0, sample, 0, 2 * GAP);
            //System.out.print("Inverse SWEEP_NUM: " + L + " ");
            //OneDHaar.displaySample(sample);
            GAP *= 2;
        }
    }
    
    public static double[] orderedNormalizedFastInverseHaarWaveletTransformForNumIters(String filePath, int num_iters) throws IOException {
        double[] signal = fileToPrimitiveDoubles(filePath);
        orderedNormalizedFastInverseHaarWaveletTransformForNumIters(signal, num_iters);
        return signal;
    }

    public static void inPlaceFastInverseHaarWaveletTransform(double[] sample) {
        int n = sample.length;
        n = (int) (Math.log(n) / Math.log(2.0));
        int GAP_SIZE = (int) (Math.pow(2.0, n - 1));
        int JUMP = 2 * GAP_SIZE;
        int NUM_FREQS = 1;
        for (int SWEEP_NUM = n; SWEEP_NUM >= 1; SWEEP_NUM--) {
            for (int K = 0; K < NUM_FREQS; K++) {
                double aPlus = sample[JUMP * K] + sample[JUMP * K + GAP_SIZE];
                double aMinus = sample[JUMP * K] - sample[JUMP * K + GAP_SIZE];
                sample[JUMP * K] = aPlus;
                sample[JUMP * K + GAP_SIZE] = aMinus;
            }
            JUMP = GAP_SIZE;
            GAP_SIZE /= 2;
            NUM_FREQS *= 2;
        }
    }

    // num_iters should be log(n)/log(2) if in-place fast haar transform is to
    // be computed completely.
    public static void inPlaceFastInverseHaarWaveletTransformForNumIters(double[] sample, int num_iters) {
        int n = sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (num_iters < 1 || num_iters > n) {
            return;
        }
        //int GAP_SIZE = (int) (Math.pow(2.0, num_sweeps - 1));
        //int JUMP = 2 * GAP_SIZE;
        //int NUM_FREQS = 1;
        int lower_bound = n - num_iters + 1;
        for (int ITER_NUM = lower_bound; ITER_NUM <= n; ITER_NUM++) {
            int GAP_SIZE = (int) (Math.pow(2.0, n - ITER_NUM));
            int JUMP = 2 * GAP_SIZE;
            int NUM_FREQS = (int) (Math.pow(2.0, ITER_NUM - 1));
            for (int K = 0; K < NUM_FREQS; K++) {
                double aPlus = sample[JUMP * K] + sample[JUMP * K + GAP_SIZE];
                double aMinus = sample[JUMP * K] - sample[JUMP * K + GAP_SIZE];
                sample[JUMP * K] = aPlus;
                sample[JUMP * K + GAP_SIZE] = aMinus;
            }
        }
    }

    public static void doNthIterOfInPlaceFastInverseHaarWaveletTransform(double[] sample, int iter_number) {
        int n = sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (iter_number < 1 || iter_number > n) {
            return;
        }
        int GAP_SIZE = (int) (Math.pow(2.0, n - iter_number));
        int JUMP = 2 * GAP_SIZE;
        int NUM_FREQS = (int) (Math.pow(2.0, iter_number - 1));
        for (int K = 0; K < NUM_FREQS; K++) {
            double aPlus = sample[JUMP * K] + sample[JUMP * K + GAP_SIZE];
            double aMinus = sample[JUMP * K] - sample[JUMP * K + GAP_SIZE];
            sample[JUMP * K] = aPlus;
            sample[JUMP * K + GAP_SIZE] = aMinus;
        }
    }

    // haar_transformed_sample is a sample to which the inplace haar wavelet transform
    // has been applied num_sweeps times. this method reconstructs the original sample in place 
    // by applying fast inverse haar transform a given number of iterations.
    public static void reconstructSampleTransformedInPlaceForNumIters(double[] haar_transformed_sample, int num_iters) {
        int n = haar_transformed_sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (num_iters < 1 || num_iters > n) {
            return;
        }
        int GAP_SIZE = (int) (Math.pow(2.0, num_iters - 1));
        int JUMP = 2 * GAP_SIZE;
        int NUM_FREQS = (int) (Math.pow(2.0, n - num_iters));
        for (int ITER_NUM = 1; ITER_NUM <= num_iters; ITER_NUM++) {
            for (int K = 0; K < NUM_FREQS; K++) {
                double aPlus = haar_transformed_sample[JUMP * K] + haar_transformed_sample[JUMP * K + GAP_SIZE];
                double aMinus = haar_transformed_sample[JUMP * K] - haar_transformed_sample[JUMP * K + GAP_SIZE];
                haar_transformed_sample[JUMP * K] = aPlus;
                haar_transformed_sample[JUMP * K + GAP_SIZE] = aMinus;
            }
            JUMP = GAP_SIZE;
            GAP_SIZE /= 2;
            NUM_FREQS *= 2;
        }
    }

    // Same as the method reconstructSampleTransformedInPlaceForNumIters but with console output messages
    // Each message displays the partially reconstructed array after each reoncstructive
    // sweep.
    public static void reconstructSampleTransformedInPlaceForNumItersWithOutput(double[] haar_transformed_sample, int num_iters) {
        int n = haar_transformed_sample.length;
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2.0));
        if (num_iters < 1 || num_iters > n) {
            return;
        }
        int GAP_SIZE = (int) (Math.pow(2.0, num_iters - 1));
        int JUMP = 2 * GAP_SIZE;
        int NUM_FREQS = (int) (Math.pow(2.0, n - num_iters));
        out.print("Reconstruction Sweep 0: ");
        displaySample(haar_transformed_sample);
        for (int ITER_NUM = 1; ITER_NUM <= num_iters; ITER_NUM++) {
            for (int K = 0; K < NUM_FREQS; K++) {
                double aPlus = haar_transformed_sample[JUMP * K] + haar_transformed_sample[JUMP * K + GAP_SIZE];
                double aMinus = haar_transformed_sample[JUMP * K] - haar_transformed_sample[JUMP * K + GAP_SIZE];
                haar_transformed_sample[JUMP * K] = aPlus;
                haar_transformed_sample[JUMP * K + GAP_SIZE] = aMinus;
            }
            out.print("Reconstruction Sweep " + ITER_NUM + ": ");
            displaySample(haar_transformed_sample);
            JUMP = GAP_SIZE;
            GAP_SIZE /= 2;
            NUM_FREQS *= 2;
        }
    }

    // display ordered frequencies from lowest to highest in the ordered_sample
    // that has been obtained from the original by the ordered haar wavelet
    // transform.  
    public static void displayOrderedFreqsFromOrderedHaar(double[] ordered_sample, PrintStream out) {
        int n = ordered_sample.length;
        if ((n < 2) || !isPowerOf2(n)) {
            return;
        }
        n = (int) (Math.log(n) / Math.log(2));
        out.println(ordered_sample[0]);
        int start = 1;
        for (int sweep_num = 1; sweep_num <= n; sweep_num++) {
            int NUM_FREQS = (int) (Math.pow(2.0, sweep_num - 1));
            for (int i = start; i < (start + NUM_FREQS); i++) {
                out.print(ordered_sample[i] + "\t");
            }
            start += NUM_FREQS;
            out.println();
        }
    }

    // display ordered frequences from lowest to highest in the sample
    // that has been obtained from the original by the in-place fast
    // haar wavelet transform
    public static void displayOrderedFreqsFromInPlaceHaar(double[] in_place_sample, PrintStream out) {
        int n = in_place_sample.length;
        //System.out.println("N = " + n);
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        if (n == 2) {
            out.println(in_place_sample[0]);
            out.println(in_place_sample[1]);
            return;
        }
        out.println(in_place_sample[0]);
        out.println(in_place_sample[n / 2]);
        int START_INDEX = n / 4;
        int NUM_FREQS = 2;
        while (START_INDEX > 1) {
            int ODD = 1;
            for (int K = 0; K < NUM_FREQS; K++) {
                out.print(in_place_sample[START_INDEX * ODD] + "\t");
                ODD += 2;
            }
            out.println();
            START_INDEX /= 2;
            NUM_FREQS *= 2;
        }
        // START_INDEX must be one for the next loop to run
        assert (START_INDEX == 1);
        for (int i = 1; i < n; i += 2) {
            out.print(in_place_sample[i] + "\t");
        }
        out.println();
    }

    // display ordered frequences from lowest to highest in the sample
    // that has been obtained from the original by the in-place fast
    // haar wavelet transform
    public static void displayOrderedFreqsFromInPlaceHaar(float[] in_place_sample, PrintStream out) {
        int n = in_place_sample.length;
        //System.out.println("N = " + n);
        if (n < 2 || !isPowerOf2(n)) {
            return;
        }
        if (n == 2) {
            out.println(in_place_sample[0]);
            out.println(in_place_sample[1]);
            return;
        }
        out.println(in_place_sample[0]);
        out.println(in_place_sample[n / 2]);
        int START_INDEX = n / 4;
        int NUM_FREQS = 2;
        while (START_INDEX > 1) {
            int ODD = 1;
            for (int K = 0; K < NUM_FREQS; K++) {
                out.print(in_place_sample[START_INDEX * ODD] + "\t");
                ODD += 2;
            }
            out.println();
            START_INDEX /= 2;
            NUM_FREQS *= 2;
        }
        // START_INDEX must be one for the next loop to run
        assert (START_INDEX == 1);
        for (int i = 1; i < n; i += 2) {
            out.print(in_place_sample[i] + "\t");
        }
        out.println();
    }

    public static double reconstructSingleValueFromOrderedHaarWaveletTransform(double[] sample, int n, int k) {
        String binstr = Integer.toBinaryString(k);

        if (binstr.length() < n) {
            int diff = n - binstr.length();
            for (int i = 0; i < diff; i++) {
                binstr = '0' + binstr;
            }
        }

        binstr = '0' + binstr;

        char[] binary = binstr.toCharArray();

        double s_k = sample[0];
        int I = (int) Math.pow(2.0, n - 2);
        int J = (int) Math.pow(2.0, n - 1);

        for (int L = 1; L <= n; L++) {
            if (binary[L] == '0') {
                s_k = s_k + sample[J];
                J = J - I;
            } else if (binary[L] == '1') {
                s_k = s_k - sample[J];
                J = J + I;
            }
            if (L < n) {
                I = I / 2;
            }
        }
        return s_k;
    }
    
    // compute 2^n x 2^n matrix for the forward haar transform
    public static double[][] computeForwardHaarTransformMatrix(int n) {
        int size = (int) Math.pow(2, n);
        double[] base_vector = new double[size];
        double[][] fhw = new double[size][size];
        for(int col_num = 0; col_num < size; col_num++) {
            for(int i = 0; i < size; i++) {
                base_vector[i] = i == col_num ? 1 : 0;
            }
            orderedFastHaarWaveletTransform(base_vector);
            for(int row_num = 0; row_num < size; row_num++) {
                fhw[row_num][col_num] = base_vector[row_num];
            }
        }
        
        return fhw;
    }
    
    // compute 2^n x 2^n matrix for the inverse haar transform
    public static double[][] computeInverseHaarTransformMatrix(int n) {
        int size = (int) Math.pow(2, n);
        double[] base_vector = new double[size];
        double[][] ihw = new double[size][size];
        for(int col_num = 0; col_num < size; col_num++) {
            for(int i = 0; i < size; i++) {
                base_vector[i] = i == col_num ? 1 : 0;
            }
            orderedFastInverseHaarWaveletTransform(base_vector);
            for(int row_num = 0; row_num < size; row_num++) {
                ihw[row_num][col_num] = base_vector[row_num];
            }
        }
        
        return ihw;
    }
    
    // apply 2^n x 2^n haar transform matrix (forward or inverse) to 2^n input signal v.
    public static double[] applyHaarTransformMatrix(double[][] htm, double[] v) {
        int num_rows = htm.length;
        if ( num_rows < 1 ) return null;
        int num_cols = htm[0].length;
        if ( num_cols < 1 ) return null;
        if ( num_rows != num_cols ) return null;
        if ( num_rows != v.length ) return null;
        double[] inversed_v = new double[num_cols];
        for(int row = 0; row < num_rows; row++) {
            double dot_product = 0;
            for(int col = 0; col < num_cols; col++) {
                dot_product += htm[row][col]*v[col];
            }
            inversed_v[row] = dot_product;
        }
        return inversed_v;
    }
}