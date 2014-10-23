/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.timeline;

/**
 * Modes: Line Line with vertical pole to base Stacked bar Stacked bar
 * normalized each step Scatter Spectral Event Bubble
 *
 */
public abstract class Chart {
    float height = 1.0f;

    public Chart() {
        height = 1f;
    }

    public Chart height(float h) {
        this.height = h;
        return this;
    }

    //called during NAR thread
    public void update(Timeline2DCanvas l, float timeScale, float yScale) {
    }

    //called during Swing thread
    public abstract void draw(Timeline2DCanvas l, float y, float timeScale, float yScale);
    
}
