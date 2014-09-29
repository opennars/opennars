package nars.gui.output;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.NAR;
import nars.core.NAR.PluginState;
import nars.gui.NPanel;

/**
 * Manages the activated set of plugins in a NAR, and a menu for adding additional ones
 * and presets of them.
 */
public class PluginPanel extends NPanel {
    private final NAR nar;
    private final JPanel plugins;
    private final JMenuBar menu;

    public PluginPanel(NAR nar) {
        super(new BorderLayout());
        this.nar = nar;
        
        
        menu = new JMenuBar();
        
        plugins = new JPanel();
        plugins.setLayout(new BoxLayout(plugins, BoxLayout.PAGE_AXIS));
        
        add(menu, BorderLayout.NORTH);
        add(plugins, BorderLayout.CENTER);
        
        update();
        
    }

    protected void update() {
        plugins.removeAll();
        
        for (PluginState p : nar.getPlugins()) {
            
        }
    }
    

    @Override
    public void onShowing(boolean b) {
        nar.memory.event.set(new Observer() {
            @Override public void event(Class event, Object[] arguments) {
                if (event == Events.PluginsChange.class)
                    update();
            }            
        }, b, Events.PluginsChange.class);
    }
    
    
    
    
}
