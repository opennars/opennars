package nars.lab.nario;

import javax.swing.JApplet;


public class AppletLauncher extends JApplet
{
    private static final long serialVersionUID = -2238077255106243788L;

    private MarioComponent mario;
    private boolean started = false;

    public void init()
    {
    }

    public void start()
    {
        if (!started)
        {
            started = true;
            mario = new MarioComponent();
            setContentPane(mario);
            setFocusable(false);
            mario.setFocusCycleRoot(true);

            mario.start();
//            addKeyListener(mario);
//            addFocusListener(mario);
        }
    }

    public void stop()
    {
        if (started)
        {
            started = false;
            removeKeyListener(mario);
            mario.stop();
            removeFocusListener(mario);
        }
    }
}