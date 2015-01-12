/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter.event;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Measures the period between start() and end() calls as a ValueMeter value.
 */
public class DurationMeter extends ValueMeter {
    
    double startTime = Double.NaN;
    private final boolean nanoSeconds;
    DescriptiveStatistics stat;
    private final double window;
    private double prev;
    private final boolean frequency;
    boolean strict = false;
    
    public DurationMeter(String id, boolean nanoSeconds, double windowSec, boolean asFrequency) {
        super(id);
        
        
        this.window = windowSec * 1E9;
        this.stat = new DescriptiveStatistics();
        this.nanoSeconds = nanoSeconds;
        this.frequency = asFrequency;
        reset();
    }
    
    public boolean isStarted() { return !Double.isNaN(startTime); }
    
    public synchronized void start() {
        if (strict && isStarted()) {
            startTime = Double.NaN;
            throw new RuntimeException(this + " already started");            
        }
        startTime = PeriodMeter.now(nanoSeconds);
    }
    
    public synchronized void stop() {
        if (strict && !isStarted())
            throw new RuntimeException(this + " not previously started");
        double duration = sinceStart();
        set(frequency ? (1.0 / duration) : duration);
        startTime = Double.NaN;
    }
    
    public double sinceStart() {
        double resolutionTime = nanoSeconds ? 1E9 : 1E3;
        return (PeriodMeter.now(nanoSeconds) - startTime) / resolutionTime;
    }

    
}
