package automenta.vivisect.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@SuppressWarnings("AbstractClassNeverImplemented")
public abstract class TimeControl extends NPanel implements ActionListener {

    protected int GUIUpdatePeriodMS = 75;
    protected final char FA_StopCharacter = '\uf04c';
    protected final char FA_FocusCharacter = '\uf11e';
    protected final char FA_ControlCharacter = '\uf085';
    protected final float defaultSpeed = 0.0f; //0.5f;
    //http://astronautweb.co/snippet/font-awesome/
    protected final char FA_PlayCharacter = '\uf04b';
    /**
     * Control buttons
     */
    protected JButton stopButton;
    protected JButton walkButton;
    /**
     * To process the next chunk of output data
     *
     * @param lines The text lines to be displayed
     */
    protected NSliderSwing speedSlider;
    protected boolean allowFullSpeed = true;
    /** in ms */
    protected long lastUpdateTime = -1;
    /** in memory cycles */



    protected float currentSpeed = 0.0f;
    //protected float lastSpeed = 0f;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public TimeControl(LayoutManager l) {
        super(l);
    }

    /** description of the current time */
    public abstract String getTimeText();

    protected NSliderSwing newSpeedSlider() {
            //final StringBuilder sb = new StringBuilder(32);

        NSliderSwing s = new NSliderSwing(0.0f, 0.0f, 1.0f) {


            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }

                return getTimeText();
            }

            @Override
            public void onChange(float v) {
                setSpeed(v);
            }

        };
        speedSlider = s;

        return s;
    }

    public abstract void setSpeed(float nextSpeed);
}
