package nars.vision;

import automenta.vivisect.swing.NWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

import java.io.IOException;

/**
 * Created by me on 5/5/15.
 */
public class DesktopOverlay {

    public static void main(String[] args) throws IOException {
        //Indeed, use GLJPanel instead of GLCanvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities glcap = new GLCapabilities(glp);
        glcap.setAlphaBits(8);
        GLJPanel pane = new GLJPanel(glcap);
        pane.setOpaque(false);



        NWindow n = new NWindow("x", pane).show(500,500);


    }
}
