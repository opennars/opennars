/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package automenta.vivisect.timeline;

import nars.util.meter.SignalData;

import java.util.List;

/**
 * Modes: Line Line with vertical pole to base Stacked bar Stacked bar
 * normalized each step Scatter Spectral Event Bubble
 *
 */
public abstract class AxisPlot {
    protected float plotHeight = 1.0f;
    protected float plotWidth = 1.0f;
    protected boolean overlayEnable = true;
    float y = 0;
    float x = 0;

    public interface MultiChart {
        public List<SignalData> getData();
    }
    
    public AxisPlot() {
        plotHeight = 1f;
    }

    public AxisPlot pos(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }
    public AxisPlot size(float w , float h) {
        this.plotWidth = w;
        this.plotHeight = h;
        return this;
    }
    public AxisPlot height(float h) {
        this.plotHeight = h;
        return this;
    }
    public AxisPlot width(float w) {
        this.plotWidth = w;
        return this;
    }
    
    public void setOverlayEnable(boolean overlayEnable) {
        this.overlayEnable = overlayEnable;
    }
    


    public float getHeight() {
        return plotHeight;
    }
    public float getWidth() {
        return plotWidth;
    }    

    

}
