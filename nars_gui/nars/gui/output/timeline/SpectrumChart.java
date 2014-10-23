package nars.gui.output.timeline;

import boofcv.alg.transform.fft.GeneralPurposeFFT_F32_1D;
import com.google.common.primitives.Floats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nars.gui.output.chart.TimeSeries;
import nars.util.NARTrace;


public class SpectrumChart extends LineChart {

    //https://github.com/lessthanoptimal/BoofCV/blob/master/main/ip/test/boofcv/alg/transform/fft/TestGeneralPurposeFFT_F32_1D.java
    //https://github.com/lessthanoptimal/BoofCV/blob/master/main/ip/src/boofcv/alg/transform/fft/GeneralPurposeFFT_F32_1D.java
    
    
    float barWidth = 0.5f;
    private boolean updated = false;
    
    static class Window {
        float[] phase;
        float[] magnitude;        

        Window(float[] input) {
            final int vl = input.length/2;
            magnitude = new float[vl];
            phase = new float[vl];

            for (int i = 0; i < vl; i++) {
                float r = input[i*2];
                float c = input[i*2+1];
                magnitude[i] = (float)Math.sqrt( r*r + c*c );
                phase[i] = (float)Math.atan2( c, r );
            }
        }
    }
    
    List<Window> windows = new ArrayList();
    
    private int windowSize;

    public SpectrumChart(TimeSeries t, int windowSize) {
        super(t);
        this.windowSize = windowSize;
    }

    public SpectrumChart(NARTrace t, String sensors, int windowSize) {        
        super(t, sensors);
        this.windowSize = windowSize;
    }

    protected void update() {
        TimeSeries chart = sensors.get(0);
        
        final float[] c = chart.getValues();
        int numWindows = (int)Math.ceil(c.length / windowSize);
        
        //TODO dont remove existing windows
        windows.clear();
        
        int t = 0;
        for (int w = 0; w < numWindows; w++) {
            final int vl = windowSize;

            float[] input = new float[vl*2];
            for (int i = 0; i < vl; i++)
                input[i*2] = chart.getValue(t++);

            GeneralPurposeFFT_F32_1D x = new GeneralPurposeFFT_F32_1D(vl);
            x.complexForward(input);

                    // phase = atan2( imaginary , real )
            // magnitude = sqrt( real<sup>2</sup> + imaginary<sup>2</sup> )

            windows.add(new Window(input));
        }
        
        
        
    }
    
    public long cycleToWindow(long c) {
        return c / windowSize;
    }
    public long windowToCycle(long w) {
        return w * windowSize;
    }
    
    @Override
    protected void drawData(Timeline2DCanvas l, float timeScale, float yScale, float y) {
    
        TimeSeries chart = sensors.get(0);
        
        if (!updated) {            
            update(); 
            updated = true;
        }
        
        int ccolor = 0;
        
        ccolor = chart.getColor().getRGB();
        l.noStroke();

        long prevWindow = -1;
        float yh = yScale / windowSize;
        
        for (long t = l.cycleStart - (windowSize); t < l.cycleEnd; t++) {
            if (t < 0) continue;
            
            float x = t * timeScale;            
            
            long w = cycleToWindow(t);
            if ((w != prevWindow) && (w < windows.size())) {
                float t2 = t + windowSize;
                float x2 = t2 * timeScale;
                //draw window block
                
                Window win = windows.get((int)w);
                
                float magMax = Floats.max(win.magnitude);
                float yy = yScale;
                for (int f = 0; f < windowSize; f++) {
                    
                    float m = win.magnitude[f] / magMax;          
                    m = (0.25f + 0.75f * m);
                    
                    float phase = (win.phase[f] + (float)Math.PI*2f) / ((float)Math.PI*2f);
                    
                    l.fill((0.2f + 0.3f * phase) * 255f, 0.75f * 255f, m * 255f);
                    
                    yy -= yh;
                    l.rect(x, y + yy, x2 - x, yh);
                }
                
                prevWindow = w;
            }            
        }
    }
    
//    public static void main(String args[]) {
//        float[] v= { 1, 2, 3, 2, 1, 2, 3, 1, 2, 1, 0, 2, 1, 2, 3 };
//        
//        float[] input = new float[v.length*2];
//        for (int i = 0; i < v.length; i++)
//            input[i*2] = v[i];
//        
//        GeneralPurposeFFT_F32_1D x = new GeneralPurposeFFT_F32_1D(v.length);
//	x.complexForward(input);
//        
//        
//        // phase = atan2( imaginary , real )
//        // magnitude = sqrt( real<sup>2</sup> + imaginary<sup>2</sup> )
//        
//        float[] magnitude = new float[v.length];
//        float[] phase = new float[v.length];
//        
//        for (int i = 0; i < v.length; i++) {
//            float r = input[i*2];
//            float c = input[i*2+1];
//            magnitude[i] = (float)Math.sqrt( r*r + c*c );
//            phase[i] = (float)Math.atan2( c, r );
//        }
//        
//        System.out.println(Arrays.toString(magnitude));
//        System.out.println(Arrays.toString(phase));
//    }
}
